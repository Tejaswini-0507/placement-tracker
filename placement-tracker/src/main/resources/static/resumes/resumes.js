/**
 * CareerSpace - Resume Versions Module Script
 * Pure Vanilla JavaScript (ES6+)
 * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
 *
 * NOTE ON DOWNLOAD ENDPOINT: the backend's ResumeResponse only returns a raw
 * server filesystem path (fileUrl), not a browser-reachable URL, and no
 * download endpoint was confirmed. This assumes:
 *     GET /api/resume-version/{id}/download
 * If that path is wrong, update DOWNLOAD_ENDPOINT_TEMPLATE below - it's the
 * only place this assumption lives.
 */

const DOWNLOAD_ENDPOINT_TEMPLATE = (id) => `/resume-version/${id}/download`;
const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
const ALLOWED_EXTENSIONS = ['.pdf', '.docx'];

let allResumes = [];
let filteredResumes = [];
let selectedFile = null;
let pendingDeleteId = null;

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("loadResumes", loadResumes);
    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initToolbar", initToolbar);
    safeInit("initUploadModal", initUploadModal);
    safeInit("initDeleteModal", initDeleteModal);
});

function safeInit(name, fn) {
    try { fn(); } catch (error) { console.error(`[resumes.js] "${name}" failed to initialize:`, error); }
}


/* ==========================================================================
   PROFILE / SIDEBAR / DROPDOWN (shared shell pattern)
   ========================================================================== */
async function loadProfile() {
    try {
        const data = await apiRequest("/profile/me", { method: "GET" });
        document.getElementById("userDisplayName").textContent = data.name;
        document.getElementById("profileName").textContent = data.name;
        document.getElementById("profileEmail").textContent = data.email;
        document.getElementById("profileBranch").textContent = `${data.branch ?? ""} ${data.batch ?? ""}`.trim();
        const initials = (data.name || "").split(" ").filter(Boolean).map(w => w[0]).join("").toUpperCase().slice(0, 2);
        document.getElementById("avatarCircle").textContent = initials || "--";
    } catch (error) {
        console.error("Profile Error:", error);
    }
}

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
   LOAD RESUMES
   ========================================================================== */
async function loadResumes() {
    showResumeState("loading");
    try {
        const data = await apiRequest("/resume-version/my-resumes", { method: "GET" });
        allResumes = Array.isArray(data) ? data : [];
        applyResumeFiltersAndRender();
    } catch (error) {
        console.error("Resumes Error:", error);
        document.getElementById("resumesErrorMessage").textContent = error.message || "Something went wrong.";
        showResumeState("error");
    }
}

function showResumeState(state) {
    const loading = document.getElementById("resumesLoading");
    const empty = document.getElementById("resumesEmpty");
    const error = document.getElementById("resumesError");
    const grid = document.getElementById("resumesGrid");
    const noResults = document.getElementById("resumesNoResults");

    [loading, empty, error, grid, noResults].forEach(el => el.classList.add("hidden"));

    if (state === "loading") {
        loading.innerHTML = renderSkeletons(4);
        loading.classList.remove("hidden");
    } else if (state === "empty") {
        empty.classList.remove("hidden");
    } else if (state === "error") {
        error.classList.remove("hidden");
    } else if (state === "grid") {
        grid.classList.remove("hidden");
    } else if (state === "no-results") {
        noResults.classList.remove("hidden");
    }
}

function renderSkeletons(count) {
    const card = `
        <div class="skeleton-resume-card">
            <div class="resume-card-top">
                <div class="skeleton-line skeleton-icon"></div>
                <div style="flex:1;">
                    <div class="skeleton-line skeleton-title"></div>
                    <div class="skeleton-line skeleton-badge"></div>
                </div>
            </div>
            <div class="skeleton-line skeleton-text"></div>
        </div>
    `;
    return card.repeat(count);
}


/* ==========================================================================
   TOOLBAR: SEARCH / SORT
   ========================================================================== */
function initToolbar() {
    const searchInput = document.getElementById("resumeSearchInput");
    const sortSelect = document.getElementById("resumeSortSelect");
    const uploadBtn = document.getElementById("uploadResumeBtn");
    const emptyStateUploadBtn = document.getElementById("emptyStateUploadBtn");
    const retryBtn = document.getElementById("resumesRetryBtn");
    const clearSearchBtn = document.getElementById("resumesClearSearchBtn");

    let debounceTimer;
    searchInput.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(applyResumeFiltersAndRender, 200);
    });

    sortSelect.addEventListener("change", applyResumeFiltersAndRender);
    uploadBtn.addEventListener("click", openUploadModal);
    emptyStateUploadBtn.addEventListener("click", openUploadModal);
    retryBtn.addEventListener("click", loadResumes);

    clearSearchBtn.addEventListener("click", () => {
        searchInput.value = "";
        applyResumeFiltersAndRender();
    });
}

function applyResumeFiltersAndRender() {
    const query = document.getElementById("resumeSearchInput").value.trim().toLowerCase();
    const sortBy = document.getElementById("resumeSortSelect").value;

    filteredResumes = allResumes.filter(r => {
        const notesMatch = (r.notes || "").toLowerCase().includes(query);
        const versionMatch = String(r.versionNumber).includes(query);
        return !query || notesMatch || versionMatch;
    });

    filteredResumes.sort((a, b) => {
        switch (sortBy) {
            case "version-asc": return (a.versionNumber || 0) - (b.versionNumber || 0);
            case "size-desc": return (b.fileSizeBytes || 0) - (a.fileSizeBytes || 0);
            case "version-desc":
            default: return (b.versionNumber || 0) - (a.versionNumber || 0);
        }
    });

    if (allResumes.length === 0) { showResumeState("empty"); return; }
    if (filteredResumes.length === 0) { showResumeState("no-results"); return; }

    renderResumesGrid();
    showResumeState("grid");
}


/* ==========================================================================
   RENDER RESUME CARDS
   ========================================================================== */
function renderResumesGrid() {
    const grid = document.getElementById("resumesGrid");

    grid.innerHTML = filteredResumes.map(resume => {
        const fileName = extractFileName(resume.fileUrl);
        const fileSize = formatFileSize(resume.fileSizeBytes);
        const companies = parseUsedForCompanies(resume.usedForCompanies);

        return `
            <div class="resume-card" data-id="${resume.id}">
                <div class="resume-card-top">
                    <div class="resume-card-icon">${fileName.toLowerCase().endsWith('.docx') ? '📝' : '📄'}</div>
                    <div class="resume-card-title-group">
                        <div class="resume-card-filename" title="${escapeHtml(fileName)}">${escapeHtml(fileName)}</div>
                        <div class="resume-card-meta">${escapeHtml(fileSize)}</div>
                    </div>
                    <span class="resume-version-badge">v${resume.versionNumber}</span>
                </div>

                ${resume.notes ? `<p class="resume-card-notes">${escapeHtml(resume.notes)}</p>` : ''}

                ${companies.length > 0 ? `
                    <div class="resume-card-tags">
                        ${companies.map(c => `<span class="resume-company-tag">${escapeHtml(c)}</span>`).join('')}
                    </div>
                ` : ''}

                <div class="resume-card-footer">
                    <button class="btn btn-secondary btn-sm resume-download-btn" data-id="${resume.id}" data-filename="${escapeHtml(fileName)}">⬇️ Download</button>
                    <button class="btn btn-danger btn-sm resume-delete-btn" data-id="${resume.id}" data-filename="${escapeHtml(fileName)}">🗑️ Delete</button>
                </div>
            </div>
        `;
    }).join('');

    grid.querySelectorAll('.resume-download-btn').forEach(btn => {
        btn.addEventListener('click', () => downloadResume(btn.dataset.id, btn.dataset.filename));
    });

    grid.querySelectorAll('.resume-delete-btn').forEach(btn => {
        btn.addEventListener('click', () => openDeleteModal(btn.dataset.id, btn.dataset.filename));
    });
}

function extractFileName(fileUrl) {
    if (!fileUrl) return "resume";
    const afterLastSlash = fileUrl.split(/[/\\]/).pop();
    const underscoreIndex = afterLastSlash.indexOf('_');
    // Backend prepends "{uuid}_" - UUIDs use hyphens only, so the first
    // underscore reliably separates the UUID prefix from the original filename
    return underscoreIndex !== -1 ? afterLastSlash.slice(underscoreIndex + 1) : afterLastSlash;
}

function formatFileSize(bytes) {
    if (!bytes || bytes <= 0) return "0 B";
    const units = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${units[i]}`;
}

function parseUsedForCompanies(raw) {
    if (!raw) return [];
    try {
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed.map(String) : [];
    } catch {
        return [];
    }
}


/* ==========================================================================
   DOWNLOAD (assumed endpoint - see note at top of file)
   ========================================================================== */
async function downloadResume(id, fileName) {
    const token = localStorage.getItem("token");
    try {
        const response = await fetch(`${API_BASE_URL}${DOWNLOAD_ENDPOINT_TEMPLATE(id)}`, {
            method: "GET",
            headers: { "Authorization": "Bearer " + token },
            cache: "no-store"
        });

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            throw new Error(`Download endpoint returned ${response.status}`);
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName || "resume";
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);

    } catch (error) {
        console.error("Download error:", error);
        showToast(
            "Couldn't download the file. The download endpoint may not exist on the backend yet (expected GET /api/resume-version/{id}/download).",
            "error",
            6000
        );
    }
}


/* ==========================================================================
   UPLOAD MODAL (dropzone + form)
   ========================================================================== */
function initUploadModal() {
    const modal = document.getElementById("uploadResumeModal");
    const form = document.getElementById("uploadResumeForm");
    const closeBtn = document.getElementById("closeUploadModalBtn");
    const cancelBtn = document.getElementById("cancelUploadBtn");
    const dropzone = document.getElementById("dropzone");
    const fileInput = document.getElementById("resumeFileInput");
    const removeBtn = document.getElementById("dropzoneRemoveBtn");

    closeBtn.addEventListener("click", closeUploadModal);
    cancelBtn.addEventListener("click", closeUploadModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeUploadModal(); });

    dropzone.addEventListener("click", (e) => {
        if (e.target === removeBtn) return;
        fileInput.click();
    });

    fileInput.addEventListener("change", () => {
        if (fileInput.files && fileInput.files[0]) handleFileSelected(fileInput.files[0]);
    });

    dropzone.addEventListener("dragover", (e) => {
        e.preventDefault();
        dropzone.classList.add("dropzone-active");
    });
    dropzone.addEventListener("dragleave", () => dropzone.classList.remove("dropzone-active"));
    dropzone.addEventListener("drop", (e) => {
        e.preventDefault();
        dropzone.classList.remove("dropzone-active");
        if (e.dataTransfer.files && e.dataTransfer.files[0]) handleFileSelected(e.dataTransfer.files[0]);
    });

    removeBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        clearSelectedFile();
    });

    form.addEventListener("submit", handleUploadSubmit);
}

function openUploadModal() {
    document.getElementById("uploadResumeForm").reset();
    clearSelectedFile();
    clearUploadErrors();

    const nextVersion = allResumes.length > 0
        ? Math.max(...allResumes.map(r => r.versionNumber || 0)) + 1
        : 1;
    document.getElementById("resumeVersionNumber").value = nextVersion;

    document.getElementById("uploadResumeModal").classList.remove("hidden");
}

function closeUploadModal() {
    document.getElementById("uploadResumeModal").classList.add("hidden");
}

function clearUploadErrors() {
    document.querySelectorAll("#uploadResumeForm .field-error").forEach(el => el.textContent = "");
    document.querySelectorAll("#uploadResumeForm .is-invalid").forEach(el => el.classList.remove("is-invalid"));
    document.getElementById("dropzone").classList.remove("is-invalid");
}

function handleFileSelected(file) {
    const lowerName = file.name.toLowerCase();
    const validExtension = ALLOWED_EXTENSIONS.some(ext => lowerName.endsWith(ext));

    if (!validExtension) {
        document.getElementById("resumeFileError").textContent = "Only .pdf and .docx files are allowed.";
        return;
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
        document.getElementById("resumeFileError").textContent = "File size exceeds 5MB limit.";
        return;
    }

    document.getElementById("resumeFileError").textContent = "";
    selectedFile = file;

    document.getElementById("dropzoneEmpty").classList.add("hidden");
    document.getElementById("dropzoneFile").classList.remove("hidden");
    document.getElementById("dropzoneFileName").textContent = file.name;
    document.getElementById("dropzoneFileSize").textContent = formatFileSize(file.size);
}

function clearSelectedFile() {
    selectedFile = null;
    document.getElementById("resumeFileInput").value = "";
    document.getElementById("dropzoneEmpty").classList.remove("hidden");
    document.getElementById("dropzoneFile").classList.add("hidden");
}

async function handleUploadSubmit(e) {
    e.preventDefault();
    clearUploadErrors();

    let isValid = true;

    if (!selectedFile) {
        document.getElementById("resumeFileError").textContent = "Please select a file to upload.";
        isValid = false;
    }

    const versionInput = document.getElementById("resumeVersionNumber");
    const versionNumber = parseInt(versionInput.value, 10);
    if (!versionNumber || versionNumber <= 0) {
        versionInput.classList.add("is-invalid");
        document.getElementById("resumeVersionError").textContent = "Version number must be positive.";
        isValid = false;
    } else if (allResumes.some(r => r.versionNumber === versionNumber)) {
        versionInput.classList.add("is-invalid");
        document.getElementById("resumeVersionError").textContent = `Version ${versionNumber} already exists.`;
        isValid = false;
    }

    if (!isValid) return;

    const usedForRaw = document.getElementById("resumeUsedFor").value.trim();
    const usedForArray = usedForRaw ? usedForRaw.split(',').map(s => s.trim()).filter(Boolean) : [];

    const formData = new FormData();
    formData.append("file", selectedFile);
    formData.append("versionNumber", versionNumber);
    if (document.getElementById("resumeNotes").value.trim()) {
        formData.append("notes", document.getElementById("resumeNotes").value.trim());
    }
    if (usedForArray.length > 0) {
        formData.append("usedForCompanies", JSON.stringify(usedForArray));
    }

    const submitBtn = document.getElementById("submitUploadBtn");
    const btnText = submitBtn.querySelector(".btn-text");
    const spinner = document.getElementById("uploadSpinner");

    submitBtn.disabled = true;
    btnText.textContent = "Uploading...";
    spinner.classList.remove("hidden");

    try {
        await apiRequest("/resume-version/upload", {
            method: "POST",
            body: formData
        });
        showToast("Resume uploaded successfully.", "success");
        closeUploadModal();
        await loadResumes();
    } catch (error) {
        showToast(error.message || "Failed to upload resume.", "error");
    } finally {
        submitBtn.disabled = false;
        btnText.textContent = "Upload Resume";
        spinner.classList.add("hidden");
    }
}


/* ==========================================================================
   DELETE CONFIRM MODAL
   ========================================================================== */
function initDeleteModal() {
    const modal = document.getElementById("deleteResumeModal");
    const cancelBtn = document.getElementById("cancelDeleteResumeBtn");
    const confirmBtn = document.getElementById("confirmDeleteResumeBtn");

    cancelBtn.addEventListener("click", closeDeleteModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeDeleteModal(); });
    confirmBtn.addEventListener("click", handleConfirmDelete);
}

function openDeleteModal(id, fileName) {
    pendingDeleteId = id;
    document.getElementById("deleteResumeName").textContent = fileName || "this resume";
    document.getElementById("deleteResumeModal").classList.remove("hidden");
}

function closeDeleteModal() {
    pendingDeleteId = null;
    document.getElementById("deleteResumeModal").classList.add("hidden");
}

async function handleConfirmDelete() {
    if (!pendingDeleteId) return;

    const confirmBtn = document.getElementById("confirmDeleteResumeBtn");
    const btnText = confirmBtn.querySelector(".btn-text");
    const spinner = document.getElementById("deleteResumeSpinner");

    confirmBtn.disabled = true;
    btnText.textContent = "Deleting...";
    spinner.classList.remove("hidden");

    try {
        await apiRequest(`/resume-version/${pendingDeleteId}`, { method: "DELETE" });
        showToast("Resume deleted.", "success");
        closeDeleteModal();
        await loadResumes();
    } catch (error) {
        showToast(error.message || "Failed to delete resume.", "error");
    } finally {
        confirmBtn.disabled = false;
        btnText.textContent = "Delete";
        spinner.classList.add("hidden");
    }
}