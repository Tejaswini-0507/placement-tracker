/**
 * CareerSpace - Login Page Logic
 * Pure Vanilla JavaScript (ES6+)
 */

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('togglePassword');
    const rememberMeCheck = document.getElementById('rememberMe');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnSpinner = document.getElementById('btnSpinner');
    const authAlert = document.getElementById('authAlert');

    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');

    // 1. Password Visibility Toggle
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', () => {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');

            const eyeIcon = togglePasswordBtn.querySelector('.eye-icon');
            const eyeOffIcon = togglePasswordBtn.querySelector('.eye-off-icon');

            if (isPassword) {
                eyeIcon.classList.add('hidden');
                eyeOffIcon.classList.remove('hidden');
                togglePasswordBtn.setAttribute('aria-label', 'Hide password');
            } else {
                eyeIcon.classList.remove('hidden');
                eyeOffIcon.classList.add('hidden');
                togglePasswordBtn.setAttribute('aria-label', 'Show password');
            }
        });
    }

    // Email regex helper
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    // Show Alert Banner
    function showAlert(message, type = 'error') {
        authAlert.textContent = message;
        authAlert.className = `auth-alert ${type}`;
    }

    function clearAlert() {
        authAlert.textContent = '';
        authAlert.className = 'auth-alert';
    }

    // Live Field Validation
    emailInput.addEventListener('input', () => {
        if (!emailInput.value.trim()) {
            emailError.textContent = 'Email address is required.';
            emailInput.classList.add('is-invalid');
        } else if (!isValidEmail(emailInput.value.trim())) {
            emailError.textContent = 'Please enter a valid email address.';
            emailInput.classList.add('is-invalid');
        } else {
            emailError.textContent = '';
            emailInput.classList.remove('is-invalid');
        }
    });

    passwordInput.addEventListener('input', () => {
        if (!passwordInput.value) {
            passwordError.textContent = 'Password is required.';
            passwordInput.classList.add('is-invalid');
        } else {
            passwordError.textContent = '';
            passwordInput.classList.remove('is-invalid');
        }
    });

    // Restore Remembered Email
    const savedEmail = localStorage.getItem('careerspace_remember_email');
    if (savedEmail) {
        emailInput.value = savedEmail;
        rememberMeCheck.checked = true;
    }

    // 2. Form Submission Handler
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert();

        let isValid = true;
        const emailVal = emailInput.value.trim();
        const passwordVal = passwordInput.value;

        // Email validation
        if (!emailVal) {
            emailError.textContent = 'Email address is required.';
            emailInput.classList.add('is-invalid');
            isValid = false;
        } else if (!isValidEmail(emailVal)) {
            emailError.textContent = 'Please enter a valid email address.';
            emailInput.classList.add('is-invalid');
            isValid = false;
        }

        // Password validation
        if (!passwordVal) {
            passwordError.textContent = 'Password is required.';
            passwordInput.classList.add('is-invalid');
            isValid = false;
        }

        if (!isValid) {
            loginForm.classList.add('shake');
            showAlert('Please fix the errors above before signing in.', 'error');
            setTimeout(() => loginForm.classList.remove('shake'), 400);
            return;
        }

        // Trigger Loading State
        submitBtn.disabled = true;
        btnText.textContent = 'Signing In...';
        btnSpinner.classList.remove('hidden');

        // Save Remember Me state
        if (rememberMeCheck.checked) {
            localStorage.setItem('careerspace_remember_email', emailVal);
        } else {
            localStorage.removeItem('careerspace_remember_email');
        }

        // Simulate API Login Verification
        try {

            const response = await fetch("https://placement-tracker-67yr.onrender.com/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: emailVal,
                    password: passwordVal
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Invalid email or password");
            }

            // Save login information
            localStorage.setItem("token", data.token);
            localStorage.setItem("studentId", data.studentId);
            localStorage.setItem("email", data.email);
            localStorage.setItem("name", data.name);

            showAlert(data.message || "Login successful!", "success");

            // Small delay so the success message is visible
            setTimeout(() => {
                window.location.href = "dashboard.html";
            }, 800);

        } catch (error) {

            showAlert(error.message, "error");

        } finally {

            btnSpinner.classList.add("hidden");
            btnText.textContent = "Sign In";
            submitBtn.disabled = false;

        }
    });

});
