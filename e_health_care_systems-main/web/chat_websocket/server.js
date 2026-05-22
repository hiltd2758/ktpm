import express from 'express';
import http from 'http';
import mysql from 'mysql2/promise';
import path, { dirname } from 'path';
import { fileURLToPath } from 'url';
import jwt from 'jsonwebtoken';
import cookie from 'cookie';
import { Server } from 'socket.io';
import dotenv from 'dotenv';
import cors from 'cors';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

dotenv.config();

const doctorSecret = Buffer.from(process.env['jwt.doctor.secret'], 'base64');
const patientSecret = Buffer.from(process.env['jwt.patient.secret'], 'base64');

const app = express();

const corsOptions = {
    origin: "http://localhost:8080",
    credentials: true
};

app.use(cors(corsOptions));

// --- Serve static files from the 'public' directoy ---
app.use(express.static(path.join(__dirname, 'public')));

const server = http.createServer(app);
const io = new Server(server, {
    cookie: true, // Allow the server to read cookies from the handshake
    cors: corsOptions
});

const PORT = process.env.PORT || 3000;

// Configurating MYSQL Pool
const dbPool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '0862264719Phi',
    database: process.env.DB_DATABASE || 'e_health_care',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// --- Socket.IO Authentication Middleware ---
io.use((socket, next) => {
    // 1. Check for token in handshake auth (the preferred way for clients)
    let token = socket.handshake.auth.token;

    // 2. Fallback to checking cookies
    if (!token) {
        const cookieHeader = socket.handshake.auth.cookie;
        if (cookieHeader) {
            const cookies = cookie.parse(cookieHeader);
            token = cookies['jwt-doctor-token'] || cookies['jwt-patient-token'];
        }
    }

    // 3. Fallback to checking query parameter (for easy debugging)
    if (!token && socket.handshake.query.token) {
        token = socket.handshake.query.token;
    }
    
    if (!token) {
        return next(new Error('Authentication error: Token not found. Please provide a token in the cookie, auth handshake, or query parameter.'));
    }

    // --- Token Verification Logic ---
    // Try verifying as a doctor first
    jwt.verify(token, doctorSecret, { algorithms: ['HS384'] }, (errDoctor, decodedDoctor) => {
        if (!errDoctor && decodedDoctor) {
            // The token is valid and contains the role. Pass it and the raw token to the socket.
            socket.user = { ...decodedDoctor, token: token };   
            return next();
        }

        // If doctor verification fails, try verifying as a patient
        jwt.verify(token, patientSecret, { algorithms: ['HS384'] }, (errPatient, decodedPatient) => {
            if (!errPatient && decodedPatient) {
                // The token is valid and contains the role. Pass it and the raw token to the socket.
                socket.user = { ...decodedPatient, token: token };
                return next();
            }

            // If both verifications fail, the token is invalid or the secrets are missing.
            if (!process.env['jwt.doctor.secret'] || !process.env['jwt.patient.secret']) {
                console.error("Server Configuration Error: JWT secret keys are not defined in the .env file.");
                return next(new Error('Authentication error: Server is misconfigured.'));
            }

            console.error("JWT Verification Failed. Doctor Error:", errDoctor?.message, "| Patient Error:", errPatient?.message);
            return next(new Error('Authentication error: Invalid token.'));
        });
    });
});

io.on('connection', (socket) => {
    console.log(`A user ${socket.id} connected.`);

    // The client will request its user info right after connecting.
    socket.on('request_user_info', () => {
        socket.emit('user_info', socket.user);
    });

    socket.on('join_room', async (room) => {
        socket.join(room);
        console.log(`User ${socket.user.sub} joined room: ${room}`);
        try {
            const [history] = await dbPool.query(
                'SELECT id, room, sender, message, timestamp FROM messages WHERE room = ? ORDER BY timestamp ASC',
            [room]
            );
            socket.emit('load_history', history);
        } catch (error) {
            console.error('Error fetching chat history:', error);
        }
    });

    socket.on('message', async (data) => {
        // Use the authenticated user's ID (subject of the token) as the sender
        const senderId = socket.user.sub;
        const { room, message } = data;
        const timestamp = new Date();

        try {
            await dbPool.query(
                'INSERT INTO messages (room, sender, message, timestamp) VALUES(?, ?, ?, ?)', 
                [room, senderId, message, timestamp]
            );
            const newMessage = { room, sender: senderId, message, timestamp };
            io.to(room).emit('receive_message', newMessage);
        } catch (error) {
            console.error('Error saving messages:', error);
        }
    });

    socket.on('disconnect', () => {
        console.log(`User disconnected: ${socket.id}.`);
    });
});

server.listen(PORT, () => {
    console.log(`Chat server is running on port ${PORT}`);
});
