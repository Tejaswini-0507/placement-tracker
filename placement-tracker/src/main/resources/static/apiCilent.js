/**
 * CareerSpace - Shared API Client & Toast Utility
 * Pure Vanilla JavaScript (ES6+)
 *
 * Include this script BEFORE any page-specific script that needs to call
 * the backend (e.g. <script src="apiClient.js"></script> then <script src="companies.js"></script>).
 *
 * Provides:
 *   - apiRequest(path, options)  -> authenticated fetch wrapper with 401 handling
 *   - showToast(message, type)   -> reusable toast notification
 *   - escapeHtml(str)            -> safe string interpolation into innerHTML
 */

const API_BASE_URL = "https://placement-tracker-67yr.onrender.com/api";

/* --------------------------------------------------------------------------
   API REQUEST WRAPPER
   -------------------------------------------------------------------------- */

/**
 * Makes an authenticated request to the backend.
 * @param {string} path - path relative to API_BASE_URL, e.g. "/companies"
 * @param {object} options - standard fetch options (method, body, etc.)
 * @param {boolean} requireAuth - attach Authorization header (default true)
 * @returns {Promise<any>} parsed JSON response (or null for empty responses)
 */
async function apiRequest(path, options = {}, requireAuth = true) {
    const token = localStorage.getItem("token");
    const isFormData = (typeof FormData !== "undefined") && (options.body instanceof FormData);

    const headers = {
        ...(isFormData ? {} : { "Content-Type": "application/json" }),
        ...(options.headers || {})
    };

    if (requireAuth) {
        if (!token) {
            // No token at all - session was never established or was cleared
            redirectToLogin();
            throw new Error("Not authenticated");
        }
        headers["Authorization"] = "Bearer " + token;
    }

    let response;
    try {
        response = await fetch(API_BASE_URL + path, {
            cache: "no-store",
            ...options,
            headers
        });
    } catch (networkError) {
        throw new Error("Unable to connect to the server. Please check your connection.");
    }

    // Session expired or invalid token
    if (response.status === 401) {
        redirectToLogin();
        throw new Error("Your session has expired. Please sign in again.");
    }

    // No content (e.g. some DELETE responses)
    if (response.status === 204) {
        return null;
    }

    let data = null;
    try {
        data = await response.json();
    } catch (parseError) {
        data = null;
    }

    if (!response.ok) {
        const message = (data && data.message) ? data.message : `Request failed (${response.status})`;
        throw new Error(message);
    }

    return data;
}

function redirectToLogin() {
    localStorage.removeItem("token");
    localStorage.removeItem("studentId");
    localStorage.removeItem("email");
    localStorage.removeItem("name");
    window.location.href = "login.html";
}

/* --------------------------------------------------------------------------
   TOAST NOTIFICATIONS
   -------------------------------------------------------------------------- */

function ensureToastContainer() {
    let container = document.getElementById("toastContainer");
    if (!container) {
        container = document.createElement("div");
        container.id = "toastContainer";
        container.className = "toast-container";
        document.body.appendChild(container);
    }
    return container;
}

/**
 * @param {string} message
 * @param {"success"|"error"|"info"} type
 * @param {number} duration - ms before auto-dismiss
 */
function showToast(message, type = "info", duration = 3500) {
    const container = ensureToastContainer();

    const icons = { success: "✓", error: "✕", info: "ℹ" };

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || icons.info}</span>
        <span class="toast-message"></span>
        <button class="toast-close" aria-label="Dismiss">&times;</button>
    `;
    toast.querySelector(".toast-message").textContent = message;

    const removeToast = () => {
        toast.classList.add("toast-exit");
        setTimeout(() => toast.remove(), 250);
    };

    toast.querySelector(".toast-close").addEventListener("click", removeToast);
    container.appendChild(toast);

    // Trigger enter animation on next frame
    requestAnimationFrame(() => toast.classList.add("toast-enter"));

    setTimeout(removeToast, duration);
}

/* --------------------------------------------------------------------------
   MISC HELPERS
   -------------------------------------------------------------------------- */

function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    const div = document.createElement("div");
    div.textContent = String(str);
    return div.innerHTML;
}