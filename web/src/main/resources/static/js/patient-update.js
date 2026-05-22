function togglePasswordVisibility() {
    const input = document.getElementById("password");
    const button = document.querySelector(".toggle-password");
    const isPassword = input.type === "password";

    // Đổi type
    input.type = isPassword ? "text" : "password";

    // Đổi icon
    button.innerHTML = isPassword
        ? `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20"
                viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.47 10.47 0 0 1 12 20c-7 0-11-8-11-8a22.8 22.8 0 0 1 5.06-6.06"></path>
                <path d="M1 1l22 22"></path>
                <path d="M10.58 10.58A3 3 0 0 0 13.42 13.42"></path>
           </svg>`
        : `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20"
                viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12z"></path>
                <circle cx="12" cy="12" r="3"></circle>
           </svg>`;
}

