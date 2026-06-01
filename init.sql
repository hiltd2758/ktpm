GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- Seed Admin (password: password123)
INSERT INTO admin (email, password, ROLE) VALUES
    ('admin@ehealth.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN');

-- Seed Doctor (password: password123)
INSERT INTO Doctor (email, password, first_name, last_name, field, phone, address, ROLE, avatar) VALUES
    ('doctor01@ehealth.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyen', 'Van A', 'Cardiology', '0901234567', 'Ho Chi Minh City', 'ROLE_DOCTOR', 'default-avatar.png');

-- Seed Patient (password: securePass123)
INSERT INTO Patient (email, password, first_name, last_name, phone, address, date_of_birth, medical_history, avatar) VALUES
    ('newpatient@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Tran', 'Thi B', '0912345678', 'Ha Noi', '1995-08-20', 'No significant history', 'default-avatar.png');