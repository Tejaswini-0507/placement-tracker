/**
 * CareerSpace - Profile Page Script
 * Pure Vanilla JavaScript (ES6+)
 * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
 *
 * Endpoints used:
 *   GET /profile/me   -> UserProfileResponse
 *   PUT /profile/me   -> UserProfileResponse (body: UserProfileRequest)
 *
 * NOTE: UserProfileService.updateMyProfile() never writes the `email` field
 * back to the entity even though UserProfileRequest carries one, so the
 * email input in the edit form is rendered disabled/read-only here and is
 * intentionally left out of the PUT payload.
 */

let currentProfile = null;

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initEditModal", initEditModal);
});

function safeInit(name, fn) {
    try { fn(); } catch (error) { console.error(`[profile.js] "${name}" failed to initialize:`, error); }
}


/* ==========================================================================
   LOAD PROFILE
   ========================================================================== */
async function loadProfile() {
    toggleState("loading");

    try {
        const data = await apiRequest("/profile/me", { method: "GET" });
        currentProfile = data;

        renderTopbar(data);
        renderProfileView(data);
        toggleState("content");

    } catch (error) {
        console.error("Profile Error:", error);
        const msgEl = document.getElementById("profileErrorMessage");
        if (msgEl) msgEl.textContent = error.message || "Something went wrong while fetching your data.";
        toggleState("error");
    }
}

function toggleState(state) {
    const loading = document.getElementById("profileLoading");
    const error = document.getElementById("profileError");
    const content = document.getElementById("profileContent");

    if (loading) loading.classList.toggle("hidden", state !== "loading");
    if (error) error.classList.toggle("hidden", state !== "error");
    if (content) content.classList.toggle("hidden", state !== "content");
}

document.getElementById("retryProfileBtn")?.addEventListener("click", loadProfile);


/* ==========================================================================
   RENDER: TOPBAR (avatar + dropdown)
   ========================================================================== */
function renderTopbar(data) {
    const initials = getInitials(data.name);

    document.getElementById("userDisplayName").textContent = data.name || "—";
    document.getElementById("avatarCircle").textContent = initials;

    document.getElementById("profileNameMenu").textContent = data.name || "—";
    document.getElementById("profileEmailMenu").textContent = data.email || "—";
    document.getElementById("profileBranchMenu").textContent = `${data.branch ?? ""} ${data.batch ?? ""}`.trim() || "—";
}


/* ==========================================================================
   RENDER: PROFILE VIEW
   ========================================================================== */
function renderProfileView(data) {
    const initials = getInitials(data.name);

    document.getElementById("profileAvatarLg").textContent = initials;
    document.getElementById("profileHeaderName").textContent = data.name || "—";
    document.getElementById("profileHeaderEmail").textContent = data.email || "—";

    const badgesEl = document.getElementById("profileHeaderBadges");
    const badges = [];
    if (data.branch) badges.push(data.branch);
    if (data.batch) badges.push(`Batch ${data.batch}`);
    badgesEl.innerHTML = badges.length > 0
        ? badges.map(b => `<span class="profile-badge">${escapeHtml(b)}</span>`).join('')
        : `<span class="profile-badge">No branch/batch set</span>`;

    document.getElementById("profileBioText").textContent = data.bio || "No bio added yet.";

    document.getElementById("infoBranch").textContent = data.branch || "—";
    document.getElementById("infoBatch").textContent = data.batch || "—";
    document.getElementById("infoPhone").textContent = data.phoneNumber || "—";
    document.getElementById("infoCreatedAt").textContent = formatDate(data.createdAt);
    document.getElementById("infoUpdatedAt").textContent = formatDate(data.updatedAt);

    renderLink("linkedinLink", data.linkedinUrl);
    renderLink("githubLink", data.githubUrl);

    const noLinksMsg = document.getElementById("noLinksMessage");
    if (noLinksMsg) noLinksMsg.classList.toggle("hidden", !!(data.linkedinUrl || data.githubUrl));
}

function renderLink(elementId, url) {
    const el = document.getElementById(elementId);
    if (!el) return;
    if (url) {
        el.href = url;
        el.classList.remove("hidden");
    } else {
        el.classList.add("hidden");
    }
}


/* ==========================================================================
   EDIT MODAL
   ========================================================================== */
function initEditModal() {
    const modal = document.getElementById("profileFormModal");
    const editBtn = document.getElementById("editProfileBtn");
    const closeBtn = document.getElementById("closeProfileModalBtn");
    const cancelBtn = document.getElementById("cancelProfileFormBtn");
    const form = document.getElementById("profileForm");
    const bioInput = document.getElementById("formBio");
    const bioCharCount = document.getElementById("bioCharCount");

    const openModal = () => {
        if (!currentProfile) return;
        populateForm(currentProfile);
        clearFieldErrors();
        modal.classList.remove("hidden");
    };

    const closeModal = () => modal.classList.add("hidden");

    editBtn?.addEventListener("click", openModal);
    closeBtn?.addEventListener("click", closeModal);
    cancelBtn?.addEventListener("click", closeModal);
    modal?.addEventListener("click", (e) => { if (e.target === modal) closeModal(); });

    bioInput?.addEventListener("input", () => {
        if (bioCharCount) bioCharCount.textContent = `${bioInput.value.length} / 500`;
    });

    form?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await submitProfileForm(closeModal);
    });
}

function populateForm(data) {
    document.getElementById("formName").value = data.name || "";
    document.getElementById("formEmail").value = data.email || "";
    document.getElementById("formBranch").value = data.branch || "";
    document.getElementById("formBatch").value = data.batch ?? "";
    document.getElementById("formPhone").value = data.phoneNumber || "";
    document.getElementById("formLinkedin").value = data.linkedinUrl || "";
    document.getElementById("formGithub").value = data.githubUrl || "";
    document.getElementById("formBio").value = data.bio || "";

    const bioCharCount = document.getElementById("bioCharCount");
    if (bioCharCount) bioCharCount.textContent = `${(data.bio || "").length} / 500`;
}

function clearFieldErrors() {
    document.querySelectorAll(".field-error").forEach(el => el.textContent = "");
    document.querySelectorAll(".form-control").forEach(el => el.classList.remove("input-error"));
}

function setFieldError(inputId, message) {
    const input = document.getElementById(inputId);
    const errorEl = document.getElementById(`${inputId}Error`);
    if (input) input.classList.add("input-error");
    if (errorEl) errorEl.textContent = message;
}

function validateForm(values) {
    clearFieldErrors();
    let valid = true;

    if (!values.name || !values.name.trim()) {
        setFieldError("formName", "Name is required.");
        valid = false;
    }

    if (values.batch !== null && (values.batch < 1900 || values.batch > 2100)) {
        setFieldError("formBatch", "Enter a valid year.");
        valid = false;
    }

    if (values.linkedinUrl && !isValidUrl(values.linkedinUrl)) {
        setFieldError("formLinkedin", "Enter a valid URL.");
        valid = false;
    }

    if (values.githubUrl && !isValidUrl(values.githubUrl)) {
        setFieldError("formGithub", "Enter a valid URL.");
        valid = false;
    }

    return valid;
}

function isValidUrl(str) {
    try { new URL(str); return true; } catch { return false; }
}

async function submitProfileForm(closeModal) {
    const batchRaw = document.getElementById("formBatch").value;

    const values = {
        name: document.getElementById("formName").value.trim(),
        branch: document.getElementById("formBranch").value.trim() || null,
        batch: batchRaw ? parseInt(batchRaw, 10) : null,
        phoneNumber: document.getElementById("formPhone").value.trim() || null,
        linkedinUrl: document.getElementById("formLinkedin").value.trim() || null,
        githubUrl: document.getElementById("formGithub").value.trim() || null,
        bio: document.getElementById("formBio").value.trim() || null
    };

    if (!validateForm(values)) return;

    const submitBtn = document.getElementById("submitProfileBtn");
    const spinner = document.getElementById("profileFormSpinner");
    const btnText = submitBtn?.querySelector(".btn-text");

    setSubmitting(submitBtn, spinner, btnText, true);

    try {
        // email intentionally omitted - backend ignores it on update, and the
        // input is disabled/read-only in the form
        const updated = await apiRequest("/profile/me", {
            method: "PUT",
            body: JSON.stringify(values)
        });

        currentProfile = updated;
        renderTopbar(updated);
        renderProfileView(updated);
        showToast("Profile updated successfully.", "success");
        closeModal();

    } catch (error) {
        console.error("Update Profile Error:", error);
        showToast("Couldn't save your profile: " + (error.message || "unknown error"), "error");
    } finally {
        setSubmitting(submitBtn, spinner, btnText, false);
    }
}

function setSubmitting(submitBtn, spinner, btnText, isSubmitting) {
    if (submitBtn) submitBtn.disabled = isSubmitting;
    if (spinner) spinner.classList.toggle("hidden", !isSubmitting);
    if (btnText) btnText.textContent = isSubmitting ? "Saving..." : "Save Changes";
}


/* ==========================================================================
   SIDEBAR / DROPDOWN (shared shell pattern)
   ========================================================================== */
function initSidebarBehavior() {
    const sidebarToggle = document.getElementById('sidebarToggle');
    const dashSidebar = document.getElementById('dashSidebar');
    const sidebarBackdrop = document.getElementById('sidebarBackdrop');

    if (sidebarToggle && dashSidebar && sidebarBackdrop) {
        const toggleSidebar = () => {
            dashSidebar.classList.toggle('open');
            sidebarBackdrop.classList.toggle('active');
        };
        sidebarToggle.addEventListener('click', toggleSidebar);
        sidebarBackdrop.addEventListener('click', toggleSidebar);
    }
}

function initProfileDropdown() {
    const avatarBtn = document.getElementById('avatarBtn');
    const profileDropdown = document.getElementById('profileDropdown');
    const logoutLink = document.getElementById('logoutLink');

    if (avatarBtn && profileDropdown) {
        avatarBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const isOpen = !profileDropdown.classList.contains('hidden');
            profileDropdown.classList.toggle('hidden', isOpen);
            avatarBtn.setAttribute('aria-expanded', String(!isOpen));
        });

        document.addEventListener('click', (e) => {
            if (!profileDropdown.contains(e.target) && !avatarBtn.contains(e.target)) {
                profileDropdown.classList.add('hidden');
                avatarBtn.setAttribute('aria-expanded', 'false');
            }
        });
    }

    if (logoutLink) {
        logoutLink.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem("token");
            localStorage.removeItem("studentId");
            localStorage.removeItem("email");
            localStorage.removeItem("name");
            window.location.href = "login.html";
        });
    }
}


/* ==========================================================================
   SHARED HELPERS
   ========================================================================== */
function getInitials(name) {
    if (!name) return "--";
    return name.split(" ").filter(Boolean).map(w => w[0]).join("").toUpperCase().slice(0, 2);
}

function formatDate(epochMillis) {
    if (!epochMillis) return "—";
    return new Date(epochMillis).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}