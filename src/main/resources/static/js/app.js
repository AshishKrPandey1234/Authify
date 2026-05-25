// --- Global Application State Tracking ---
let currentMode = "LOGIN"; // Modes: LOGIN, SIGNUP, VERIFY

// Toggles interface states between Login and Signup inputs smoothly
function toggleForm(event) {
    if (event) event.preventDefault();
    clearInputs();

    const usernameGroup = document.getElementById("username-group");
    const formTitle = document.getElementById("form-title");
    const primaryBtn = document.getElementById("primary-btn");
    const toggleLinkWrapper = document.getElementById("toggle-link-wrapper");

    if (currentMode === "LOGIN") {
        currentMode = "SIGNUP";
        formTitle.innerText = "Create Account";
        primaryBtn.innerText = "Register";
        usernameGroup.classList.remove("hidden");
        toggleLinkWrapper.innerHTML = 'Already have an account? <a href="#" onclick="toggleForm(event)">Login Here</a>';
    } else {
        currentMode = "LOGIN";
        formTitle.innerText = "Login to Authify";
        primaryBtn.innerText = "Login";
        usernameGroup.classList.add("hidden");
        toggleLinkWrapper.innerHTML = 'Don\'t have an account? <a href="#" onclick="toggleForm(event)">Sign Up</a>';
    }
}

// Switch view state cleanly to the OTP Verification mode after a successful signup
function switchToVerifyMode() {
    currentMode = "VERIFY";
    document.getElementById("form-title").innerText = "Verify Email OTP";
    document.getElementById("primary-btn").innerText = "Verify Account";
    document.getElementById("username-group").classList.add("hidden");
    document.getElementById("password-group").classList.add("hidden");
    document.getElementById("otp-group").classList.remove("hidden");
    document.getElementById("toggle-link-wrapper").classList.add("hidden");
}

// --- Main Form Action Router ---
function handlePrimaryAction() {
    if (currentMode === "LOGIN") handleLogin();
    else if (currentMode === "SIGNUP") handleSignup();
    else if (currentMode === "VERIFY") handleVerification();
}

// --- API Request Layer (Communicating with our Spring Boot Backend) ---

// 1. SIGNUP REQUEST HANDLER
function handleSignup() {
    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    // INTERVIEW POINT: Using standard browser Fetch API to make a asynchronous POST request with JSON
    fetch("/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, email, password }) // Maps directly to RegisterRequest DTO
    })
    .then(response => {
        if (!response.ok) return response.text().then(text => { throw new Error(text) });
        return response.text();
    })
    .then(message => {
        alert(message);
        switchToVerifyMode(); // Move to OTP input screen on success
    })
    .catch(error => alert("Registration Failed: " + error.message));
}

// 2. OTP VERIFICATION REQUEST HANDLER
function handleVerification() {
    const email = document.getElementById("email").value;
    const otp = document.getElementById("otp").value;

    fetch("/api/auth/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, otp }) // Maps directly to VerifyRequest DTO
    })
    .then(response => {
        if (!response.ok) return response.text().then(text => { throw new Error(text) });
        return response.text();
    })
    .then(message => {
        alert(message);
        currentMode = "SIGNUP"; // Temporary toggle helper to flip back into standard login interface
        toggleForm(null);
    })
    .catch(error => alert("Verification Error: " + error.message));
}

// 3. LOGIN REQUEST HANDLER (CRITICAL INTERVIEW STEP)
function handleLogin() {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }) // Maps directly to LoginRequest DTO
    })
    .then(response => {
        if (!response.ok) return response.text().then(text => { throw new Error(text) });
        return response.json(); // Backend returns an AuthResponse object containing JWT string
    })
    .then(data => {
        // INTERVIEW POINT: Storing the received stateless JWT token into browser local storage securely
        localStorage.setItem("authify_jwt", data.token);
        loadDashboardContent(); // Load content immediately after storing token
    })
    .catch(error => alert("Login Rejected: " + error.message));
}

// --- Protected Content Loader Engine ---
function loadDashboardContent() {
    // INTERVIEW POINT: Extract the token from local storage
    const token = localStorage.getItem("authify_jwt");
    if (!token) return;

    // Fetch the secret, protected home content data
    fetch("/api/home-content", {
        method: "GET",
        headers: {
            // INTERVIEW POINT: Attaching the token as an Authorization Bearer header so our JwtAuthenticationFilter intercepts it
            "Authorization": "Bearer " + token
        }
    })
    .then(response => {
        if (!response.ok) throw new Error("Unauthorized access token session expired.");
        return response.json();
    })
   .then(data => {
           console.log("Backend Content Data:", data);

           // Map perfectly to the real keys returned by your HomeController
           document.getElementById("quote-text").innerText = `"${data.feature}"`;
           document.getElementById("quote-author").innerText = `- Project: ${data.project}`;
           document.getElementById("welcome-msg").innerText = `${data.message} | Developer: ${data.developer}`;

           document.getElementById("auth-card").classList.add("hidden");
           document.getElementById("dashboard-card").classList.remove("hidden");
       })
    .catch(error => {
        console.error(error);
        logout();
    });
}

// --- LOGOUT HANDLER ---
function logout() {
    // INTERVIEW POINT: Logging out a stateless JWT system means destroying the client-side reference
    localStorage.removeItem("authify_jwt");

    // Reset view visibility flags instantly
    document.getElementById("auth-card").classList.remove("hidden");
    document.getElementById("dashboard-card").classList.add("hidden");

    currentMode = "SIGNUP";
    toggleForm(null); // Reset layout parameters cleanly back into default form orientation
}

// Utility screen input cleaner
function clearInputs() {
    document.getElementById("username").value = "";
    document.getElementById("password").value = "";
    document.getElementById("otp").value = "";
}

// AUTO-LOGIN CHECK: If a user refreshes the page but has an active valid token, bypass auth screens automatically
window.onload = function() {
    if (localStorage.getItem("authify_jwt")) {
        loadDashboardContent();
    }
}