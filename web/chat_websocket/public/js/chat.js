// --- Authentication: Pass token from URL query for debugging ---
const urlParams = new URLSearchParams(window.location.search);
const tokenFromQuery = urlParams.get('token');

const socket = io({
    auth: {
        token: tokenFromQuery
    }
});

// The base URL of your Java backend API
const API_BASE_URL = 'http://localhost:8080';

let currentUser = null; // This will be set on connection
let currentRoom = null;
let chatPartner = null;

// --- DOM Elements ---
const form = document.getElementById('form');
const input = document.getElementById('input');
const messages = document.getElementById('messages');
const chatWindow = document.getElementById('chat-window');
const usersToChatList = document.getElementById('users-to-chat');
const chatArea = document.getElementById('chat-area');
const selectPrompt = document.getElementById('select-prompt');
const chatPartnerName = document.getElementById('chat-partner-name');

// --- Socket Event Handlers ---

// This event is fired upon a successful, authenticated connection
socket.on('connect', () => {
    console.log('Successfully connected to chat server!');
    socket.emit('request_user_info');
});

// Handle connection errors (e.g., invalid or missing token)
socket.on('connect_error', (err) => {
    console.error('Connection Failed:', err.message);
    document.body.innerHTML = `<div style="text-align: center; padding: 50px; font-family: sans-serif;">
                                <h1>Authentication Error</h1>
                                <p>${err.message}</p>
                                <p>You must be logged in to access the chat.</p>
                               </div>`;
});

// Listen for the server's response with the user info
socket.on('user_info', (user) => {
    console.log('Received user info:', user);
    currentUser = user; // Set the global currentUser object
    fetchAndPopulateUserList(currentUser.role); // Fetch users based on the authenticated role
});

socket.on('load_history', (history) => {
    messages.innerHTML = '';
    history.forEach(displayMessage);
});

socket.on('receive_message', (data) => {
    if (data.room === currentRoom) {
        displayMessage(data);
    }
});

// --- UI Functions ---
function startChat(partner) {
    // Prevent re-initializing the chat if it's already active with this partner
    if (!currentUser || (chatPartner && chatPartner.email === partner.email)) return;

    chatPartner = partner;
    // currentUser.sub is the email of the logged-in user, from the auth token.

    // Create a consistent room name by sorting the two user emails.
    const ids = [currentUser.sub, chatPartner.email].sort();
    currentRoom = `chat_${ids[0]}_${ids[1]}`;

    socket.emit('join_room', currentRoom);

    // Update the UI to show the chat window
    chatArea.style.display = 'flex';
    selectPrompt.style.display = 'none';
    chatPartnerName.textContent = chatPartner.fullName; // Display the partner's full name

    // Highlight the current conversation in the user list
    document.querySelectorAll('#users-to-chat li').forEach(li => li.classList.remove('active-chat'));
    const conversationElement = document.getElementById(`conversation_${partner.email}`);
    if (conversationElement) {
        conversationElement.classList.add('active-chat');
    }
}

function displayMessage(data) {
    const item = document.createElement('li');
    item.textContent = data.message;
    item.className = data.sender === currentUser.sub ? 'sent' : 'received';
    messages.appendChild(item);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

form.addEventListener('submit', (e) => {
    e.preventDefault();
    if (input.value && currentRoom) {
        socket.emit('message', { room: currentRoom, message: input.value });
        input.value = '';
    }
});

// --- Fetch and Populate User List ---
async function fetchAndPopulateUserList(userRole) {
    let apiUrl = '';
    if (userRole.toLowerCase().includes('doctor')) {
        apiUrl = `${API_BASE_URL}/api/patients`; // Endpoint to get all patients for a doctor
    } else if (userRole.toLowerCase().includes('patient')) {
        apiUrl = `${API_BASE_URL}/api/doctors`; // Endpoint to get all doctors for a patient
    } else {
        console.error('Unknown user role:', userRole);
        return;
    }
    
    try {
        if (!currentUser || !currentUser.token) {
            throw new Error("Authentication token not found on client.");
        }

        const response = await fetch(apiUrl, {
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }
        const users = await response.json();
        
        usersToChatList.innerHTML = ''; // Clear existing list
        users.forEach(user => {
            // Don't list the current user themselves
            if (user.email === currentUser.sub) {
                return;
            }

            const li = document.createElement('li');
            li.textContent = user.fullName; // Use fullName for display
            li.dataset.id = user.email;      // Use email as the unique identifier
            li.id = `conversation_${user.email}`; // Use email for the element ID
            li.onclick = () => startChat(user);
            usersToChatList.appendChild(li);
        });
    } catch (error) {
        console.error('Error fetching users to chat:', error);
        // Display a more specific error message to the user if available
        let errorMessage = 'Error loading users. Please try again.';
        if (error.message) {
            errorMessage += ` Details: ${error.message}`;
        }
        usersToChatList.innerHTML = `<li>${errorMessage}</li>`;
    }
}

// --- Initial Setup ---
function initializeApp() {
    // Initial UI setup, actual user list will be fetched after authentication
    // The `fetchAndPopulateUserList` function will be called once `currentUser` is set.
}

initializeApp();
