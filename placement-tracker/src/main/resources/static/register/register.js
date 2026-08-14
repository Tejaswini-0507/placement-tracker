/**
 * CareerSpace - Register Page Logic
 * Pure Vanilla JavaScript (ES6+)
 */

document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const fullNameInput = document.getElementById('fullName');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('togglePassword');

    // Branch Searchable Dropdown Elements
    const branchWrapper = document.getElementById('branchSelectWrapper');
    const branchInput = document.getElementById('branchInput');
    const branchDropdown = document.getElementById('branchDropdown');
    const branchSearch = document.getElementById('branchSearch');
    const branchOptions = document.getElementById('branchOptions');

    // Batch & Terms Elements
    const batchSelect = document.getElementById('batchSelect');
    const termsCheck = document.getElementById('termsCheck');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnSpinner = document.getElementById('btnSpinner');
    const authAlert = document.getElementById('authAlert');

    // Field Error Elements
    const fullNameError = document.getElementById('fullNameError');
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    const branchError = document.getElementById('branchError');
    const batchError = document.getElementById('batchError');
    const termsError = document.getElementById('termsError');

    // Password Requirement Items
    const reqLength = document.getElementById('reqLength');
    const reqUpper = document.getElementById('reqUpper');
    const reqLower = document.getElementById('reqLower');
    const reqNumber = document.getElementById('reqNumber');
    const reqSpecial = document.getElementById('reqSpecial');

    // Email Helper
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

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

    // 2. Password Live Validation & Criteria Checklist
    function checkPasswordCriteria(password) {
        const hasLength = password.length >= 8;
        const hasUpper = /[A-Z]/.test(password);
        const hasLower = /[a-z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);

        updateReqStatus(reqLength, hasLength);
        updateReqStatus(reqUpper, hasUpper);
        updateReqStatus(reqLower, hasLower);
        updateReqStatus(reqNumber, hasNumber);
        updateReqStatus(reqSpecial, hasSpecial);

        return hasLength && hasUpper && hasLower && hasNumber && hasSpecial;
    }

    function updateReqStatus(el, isValid) {
        if (isValid) {
            el.classList.add('valid');
        } else {
            el.classList.remove('valid');
        }
    }

    passwordInput.addEventListener('input', () => {
        const val = passwordInput.value;
        const isValid = checkPasswordCriteria(val);
        if (!val) {
            passwordError.textContent = 'Password is required.';
            passwordInput.classList.add('is-invalid');
        } else if (!isValid) {
            passwordError.textContent = 'Password does not meet all requirements.';
            passwordInput.classList.add('is-invalid');
        } else {
            passwordError.textContent = '';
            passwordInput.classList.remove('is-invalid');
        }
        validateForm();
    });


    // 3. Searchable Branch Dropdown Logic
    branchInput.addEventListener('click', () => {
        branchWrapper.classList.toggle('open');
        branchDropdown.classList.toggle('hidden');
        if (!branchDropdown.classList.contains('hidden')) {
            branchSearch.focus();
        }
    });

    branchSearch.addEventListener('input', () => {
        const query = branchSearch.value.toLowerCase().trim();
        const items = branchOptions.querySelectorAll('li');

        items.forEach(item => {
            const text = item.textContent.toLowerCase();
            if (text.includes(query)) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        });
    });

    branchOptions.addEventListener('click', (e) => {
        if (e.target && e.target.nodeName === 'LI') {
            const selectedVal = e.target.getAttribute('data-value');
            branchInput.value = selectedVal;
            branchError.textContent = '';
            branchInput.classList.remove('is-invalid');

            // Highlight selected option
            branchOptions.querySelectorAll('li').forEach(li => li.classList.remove('selected'));
            e.target.classList.add('selected');

            // Close dropdown
            branchDropdown.classList.add('hidden');
            branchWrapper.classList.remove('open');
            validateForm();
        }
    });

    // Close dropdown on outside click
    document.addEventListener('click', (e) => {
        if (!branchWrapper.contains(e.target)) {
            branchDropdown.classList.add('hidden');
            branchWrapper.classList.remove('open');
        }
    });


    // 4. Live Field Listeners for Form Enablement
    fullNameInput.addEventListener('input', () => {
        if (!fullNameInput.value.trim()) {
            fullNameError.textContent = 'Full Name is required.';
            fullNameInput.classList.add('is-invalid');
        } else {
            fullNameError.textContent = '';
            fullNameInput.classList.remove('is-invalid');
        }
        validateForm();
    });

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
        validateForm();
    });

    batchSelect.addEventListener('change', () => {
        if (!batchSelect.value) {
            batchError.textContent = 'Please select your batch.';
            batchSelect.classList.add('is-invalid');
        } else {
            batchError.textContent = '';
            batchSelect.classList.remove('is-invalid');
        }
        validateForm();
    });

    termsCheck.addEventListener('change', () => {
        if (!termsCheck.checked) {
            termsError.textContent = 'You must agree to the Terms and Privacy Policy.';
        } else {
            termsError.textContent = '';
        }
        validateForm();
    });


    // 5. Global Form Validation Check
    function validateForm() {
        const isNameValid = fullNameInput.value.trim().length > 0;
        const isEmailValid = isValidEmail(emailInput.value.trim());
        const isPasswordValid = checkPasswordCriteria(passwordInput.value);
        const isBranchValid = branchInput.value.trim().length > 0;
        const isBatchValid = batchSelect.value.length > 0;
        const isTermsValid = termsCheck.checked;

        const isFormValid = isNameValid && isEmailValid && isPasswordValid && isBranchValid && isBatchValid && isTermsValid;
        submitBtn.disabled = !isFormValid;
        return isFormValid;
    }


    // 6. Form Submit Handler
    registerForm.addEventListener('submit', (e) => {
        e.preventDefault();

        if (!validateForm()) {
            registerForm.classList.add('shake');
            authAlert.textContent = 'Please complete all required fields correctly.';
            authAlert.className = 'auth-alert error';
            setTimeout(() => registerForm.classList.remove('shake'), 400);
            return;
        }

        // Loading State
        submitBtn.disabled = true;
        btnText.textContent = 'Creating Account...';
        btnSpinner.classList.remove('hidden');

        // Simulate Account Creation
//        setTimeout(() => {
//            btnSpinner.classList.add('hidden');
//            btnText.textContent = 'Create Account';
//            submitBtn.disabled = false;
//
//            authAlert.textContent = 'Account created successfully! Redirecting to sign in...';
//            authAlert.className = 'auth-alert success';
//
//            setTimeout(() => {
//                window.location.href = 'login.html';
//            }, 1500);
//        }, 1500);

        const registrationData = {
            name: fullNameInput.value.trim(),
            email: emailInput.value.trim(),
            password: passwordInput.value,
            branch: branchInput.value,
            batch: Number(batchSelect.value)
        };

        fetch("http://localhost:8081/api/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(registrationData)
        })
        .then(async (response) => {

            const data = await response.json();

            btnSpinner.classList.add('hidden');
            btnText.textContent = 'Create Account';

            if (response.ok) {

                authAlert.textContent = "Registration Successful!";
                authAlert.className = "auth-alert success";

                setTimeout(() => {
                    window.location.href = "login.html";
                }, 1500);

            } else {

                submitBtn.disabled = false;
                authAlert.textContent = data.message || "Registration Failed";
                authAlert.className = "auth-alert error";
            }

        })
        .catch((error) => {

            console.error(error);

            btnSpinner.classList.add('hidden');
            btnText.textContent = 'Create Account';
            submitBtn.disabled = false;

            authAlert.textContent = "Unable to connect to the backend.";
            authAlert.className = "auth-alert error";

        });
    });

});
