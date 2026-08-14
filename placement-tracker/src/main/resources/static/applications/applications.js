/**
 * CareerSpace - Applications Module Script
 * Pure Vanilla JavaScript (ES6+)
 * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
 */

const STATUS_META = {
    APPLIED:                   { label: "Applied",                  cls: "status-applied" },
    OA_SCHEDULED:               { label: "OA Scheduled",             cls: "status-oa-scheduled" },
    OA_COMPLETED:                { label: "OA Completed",             cls: "status-oa-completed" },
    INTERVIEW_SCHEDULED:         { label: "Interview Scheduled",      cls: "status-interview-scheduled" },
    INTERVIEW_COMPLETED:         { label: "Interview Completed",      cls: "status-interview-completed" },
    RESULT_WAITING:              { label: "Result Waiting",           cls: "status-result-waiting" },
    SELECTED:                    { label: "Selected",                 cls: "status-selected" },
    REJECTED:                    { label: "Rejected",                 cls: "status-rejected" },
    OFFER_RECEIVED:               { label: "Offer Received",           cls: "status-offer-received" },
    OFFER_ACCEPTED:               { label: "Offer Accepted",           cls: "status-offer-accepted" },
    OFFER_DECLINED:               { label: "Offer Declined",           cls: "status-offer-declined" },
    JOINING_LETTER_RECEIVED:      { label: "Joining Letter Received",  cls: "status-joining-letter-received" }
};

let allApplications = [];
let filteredApplications = [];
let allCompaniesForPicker = [];
let activeDetailApp = null;

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("populateStatusFilterAndUpdateSelect", populateStatusFilterAndUpdateSelect);
    safeInit("loadCompaniesForPicker", loadCompaniesForPicker);
    safeInit("loadApplications", loadApplications);

    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initToolbar", initToolbar);
    safeInit("initAddAppModal", initAddAppModal);
    safeInit("initDetailModal", initDetailModal);
    safeInit("initDeleteAppModal", initDeleteAppModal);
    safeInit("initUpdateStatusModal", initUpdateStatusModal);
});

function safeInit(name, fn) {
    try {
        fn();
    } catch (error) {
        console.error(`[applications.js] "${name}" failed to initialize:`, error);
    }
}


/* ==========================================================================
   PROFILE (TOPBAR) - identical pattern to companies.js
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


/* ==========================================================================
   SIDEBAR / DROPDOWN
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
   STATUS DROPDOWNS (filter + update-modal select)
   ========================================================================== */
function populateStatusFilterAndUpdateSelect() {
    const filterSelect = document.getElementById("statusFilter");
    const updateSelect = document.getElementById("updateStatusSelect");
    const addSelect = document.getElementById("appStatusSelect");

    Object.entries(STATUS_META).forEach(([value, meta]) => {
        const filterOption = document.createElement("option");
        filterOption.value = value;
        filterOption.textContent = meta.label;
        filterSelect.appendChild(filterOption);

        const updateOption = document.createElement("option");
        updateOption.value = value;
        updateOption.textContent = meta.label;
        updateSelect.appendChild(updateOption);

        const addOption = document.createElement("option");
        addOption.value = value;
        addOption.textContent = meta.label;
        if (value === "APPLIED") addOption.selected = true;
        addSelect.appendChild(addOption);
    });
}


/* ==========================================================================
   LOAD COMPANIES (for the Add Application picker)
   ========================================================================== */
async function loadCompaniesForPicker() {
    const select = document.getElementById("appCompanySelect");
    select.innerHTML = `<option value="" disabled selected>Loading companies...</option>`;

    try {
        const data = await apiRequest("/companies", { method: "GET" });
        allCompaniesForPicker = Array.isArray(data) ? data : [];

        if (allCompaniesForPicker.length === 0) {
            select.innerHTML = `<option value="" disabled selected>No companies found - add one first</option>`;
            return;
        }

        select.innerHTML = `<option value="" disabled selected>Select a company</option>`;

        allCompaniesForPicker
            .slice()
            .sort((a, b) => (a.name || "").localeCompare(b.name || ""))
            .forEach(company => {
                const option = document.createElement("option");
                option.value = company.id;
                option.textContent = `${company.name} — ${company.hiringFor}`;
                select.appendChild(option);
            });

    } catch (error) {
        console.error("Failed to load companies for picker:", error);
        select.innerHTML = `<option value="" disabled selected>Failed to load companies</option>`;
        showToast("Couldn't load companies list: " + (error.message || "unknown error"), "error");
    }
}


/* ==========================================================================
   LOAD APPLICATIONS
   ========================================================================== */
async function loadApplications() {
    showAppState("loading");

    try {
        const data = await apiRequest("/student-application/my-applications", { method: "GET" });
        allApplications = Array.isArray(data) ? data : [];
        applyAppFiltersAndRender();
    } catch (error) {
        console.error("Applications Error:", error);
        document.getElementById("appsErrorMessage").textContent = error.message || "Something went wrong.";
        showAppState("error");
    }
}


/* ==========================================================================
   STATE MANAGEMENT
   ========================================================================== */
function showAppState(state) {
    const loading = document.getElementById("appsLoading");
    const empty = document.getElementById("appsEmpty");
    const error = document.getElementById("appsError");
    const table = document.getElementById("appsTableWrapper");
    const noResults = document.getElementById("appsNoResults");

    [loading, empty, error, table, noResults].forEach(el => el.classList.add("hidden"));

    if (state === "loading") {
        loading.innerHTML = '<div class="apps-skeleton-row"></div>'.repeat(5);
        loading.classList.remove("hidden");
    } else if (state === "empty") {
        empty.classList.remove("hidden");
    } else if (state === "error") {
        error.classList.remove("hidden");
    } else if (state === "table") {
        table.classList.remove("hidden");
    } else if (state === "no-results") {
        noResults.classList.remove("hidden");
    }
}


/* ==========================================================================
   TOOLBAR: SEARCH / FILTER / SORT
   ========================================================================== */
function initToolbar() {
    const searchInput = document.getElementById("appSearchInput");
    const statusFilter = document.getElementById("statusFilter");
    const sortSelect = document.getElementById("appSortSelect");
    const addBtn = document.getElementById("addApplicationBtn");
    const emptyStateAddBtn = document.getElementById("emptyStateAddBtn");
    const retryBtn = document.getElementById("appsRetryBtn");
    const clearFiltersBtn = document.getElementById("appsClearFiltersBtn");

    let debounceTimer;
    searchInput.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(applyAppFiltersAndRender, 200);
    });

    statusFilter.addEventListener("change", applyAppFiltersAndRender);
    sortSelect.addEventListener("change", applyAppFiltersAndRender);

    addBtn.addEventListener("click", openAddAppModal);
    emptyStateAddBtn.addEventListener("click", openAddAppModal);
    retryBtn.addEventListener("click", loadApplications);

    clearFiltersBtn.addEventListener("click", () => {
        searchInput.value = "";
        statusFilter.value = "";
        sortSelect.value = "applied-desc";
        applyAppFiltersAndRender();
    });
}

function applyAppFiltersAndRender() {
    const query = document.getElementById("appSearchInput").value.trim().toLowerCase();
    const status = document.getElementById("statusFilter").value;
    const sortBy = document.getElementById("appSortSelect").value;

    filteredApplications = allApplications.filter(app => {
        const matchesQuery = !query ||
            (app.companyName || "").toLowerCase().includes(query) ||
            (app.positionTitle || "").toLowerCase().includes(query);
        const matchesStatus = !status || app.status === status;
        return matchesQuery && matchesStatus;
    });

    filteredApplications.sort((a, b) => {
        switch (sortBy) {
            case "applied-asc":
                return (a.createdAt || 0) - (b.createdAt || 0);
            case "updated-desc":
                return (b.updatedAt || 0) - (a.updatedAt || 0);
            case "company-asc":
                return (a.companyName || "").localeCompare(b.companyName || "");
            case "applied-desc":
            default:
                return (b.createdAt || 0) - (a.createdAt || 0);
        }
    });

    if (allApplications.length === 0) {
        showAppState("empty");
        return;
    }

    if (filteredApplications.length === 0) {
        showAppState("no-results");
        return;
    }

    renderApplicationsTable();
    showAppState("table");
}


/* ==========================================================================
   RENDER TABLE
   ========================================================================== */
function renderApplicationsTable() {
    const tbody = document.getElementById("appsTableBody");

    tbody.innerHTML = filteredApplications.map(app => {
        const meta = STATUS_META[app.status] || { label: app.status, cls: "status-applied" };
        return `
            <tr class="app-row" data-id="${app.id}">
                <td><strong>${escapeHtml(app.companyName)}</strong></td>
                <td>${escapeHtml(app.positionTitle)}</td>
                <td><span class="status-pill ${meta.cls}">${escapeHtml(meta.label)}</span></td>
                <td>${formatDate(app.createdAt)}</td>
                <td>${formatDate(app.updatedAt)}</td>
            </tr>
        `;
    }).join('');

    tbody.querySelectorAll('.app-row').forEach(row => {
        row.addEventListener('click', () => {
            const id = row.getAttribute('data-id');
            const app = allApplications.find(a => String(a.id) === String(id));
            if (app) openDetailModal(app);
        });
    });
}

function formatDate(epochMillis) {
    if (!epochMillis) return "—";
    const date = new Date(epochMillis);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function epochToDateInputValue(epochMillis) {
    if (!epochMillis) return "";
    const date = new Date(epochMillis);
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

function dateInputValueToEpoch(value) {
    if (!value) return null;
    return new Date(value + "T00:00:00").getTime();
}


/* ==========================================================================
   ADD APPLICATION MODAL
   ========================================================================== */
function initAddAppModal() {
    const modal = document.getElementById("addAppModal");
    const form = document.getElementById("addAppForm");
    const closeBtn = document.getElementById("closeAddAppModalBtn");
    const cancelBtn = document.getElementById("cancelAddAppBtn");

    closeBtn.addEventListener("click", closeAddAppModal);
    cancelBtn.addEventListener("click", closeAddAppModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeAddAppModal(); });

    form.addEventListener("submit", handleAddAppSubmit);
}

function openAddAppModal() {
    document.getElementById("addAppForm").reset();
    clearAddAppErrors();
    document.getElementById("addAppModal").classList.remove("hidden");
}

function closeAddAppModal() {
    document.getElementById("addAppModal").classList.add("hidden");
}

function clearAddAppErrors() {
    document.querySelectorAll("#addAppForm .field-error").forEach(el => el.textContent = "");
    document.querySelectorAll("#addAppForm .is-invalid").forEach(el => el.classList.remove("is-invalid"));
}

function validateAddAppForm() {
    clearAddAppErrors();
    let isValid = true;

    const companyId = document.getElementById("appCompanySelect").value;
    const status = document.getElementById("appStatusSelect").value;

    if (!companyId) {
        document.getElementById("appCompanySelect").classList.add("is-invalid");
        document.getElementById("appCompanyError").textContent = "Please select a company.";
        isValid = false;
    }
    if (!status) {
        document.getElementById("appStatusSelect").classList.add("is-invalid");
        document.getElementById("appStatusError").textContent = "Please select a status.";
        isValid = false;
    }

    return isValid;
}

async function handleAddAppSubmit(e) {
    e.preventDefault();
    if (!validateAddAppForm()) return;

    const submitBtn = document.getElementById("submitAddAppBtn");
    const btnText = submitBtn.querySelector(".btn-text");
    const spinner = document.getElementById("addAppSpinner");

    const payload = {
        companyId: document.getElementById("appCompanySelect").value,
        status: document.getElementById("appStatusSelect").value,
        statusUpdatedAt: Date.now(),
        notes: document.getElementById("appNotes").value.trim() || null
    };

    submitBtn.disabled = true;
    btnText.textContent = "Tracking...";
    spinner.classList.remove("hidden");

    try {
        await apiRequest("/student-application", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        showToast("Application tracked successfully.", "success");
        closeAddAppModal();
        await loadApplications();
    } catch (error) {
        showToast(error.message || "Failed to track application.", "error");
    } finally {
        submitBtn.disabled = false;
        btnText.textContent = "Track Application";
        spinner.classList.add("hidden");
    }
}


/* ==========================================================================
   DETAIL MODAL
   ========================================================================== */
function initDetailModal() {
    const modal = document.getElementById("appDetailModal");
    const closeBtn = document.getElementById("closeAppDetailModalBtn");
    const deleteBtn = document.getElementById("deleteApplicationBtn");
    const updateBtn = document.getElementById("updateStatusBtn");

    closeBtn.addEventListener("click", closeDetailModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeDetailModal(); });

    updateBtn.addEventListener("click", () => {
        closeDetailModal();
        openUpdateStatusModal(activeDetailApp);
    });
    deleteBtn.addEventListener("click", handleDeleteApplication);
}

function openDetailModal(app) {
    activeDetailApp = app;
    const meta = STATUS_META[app.status] || { label: app.status, cls: "status-applied" };

    document.getElementById("detailAppCompany").textContent = app.companyName || "Application";
    document.getElementById("detailAppPosition").textContent = app.positionTitle || "—";
//    document.getElementById("detailAppLocation").textContent = "";

    const pill = document.getElementById("detailAppStatusPill");
    pill.className = `status-pill ${meta.cls}`;
    pill.textContent = meta.label;

    document.getElementById("detailAppNotes").textContent = app.notes || "No notes added.";

    const milestones = [
        { label: "Applied", value: app.createdAt, icon: "📩" },
        { label: "OA Scheduled", value: app.oaScheduledDate, icon: "📝" },
        { label: "OA Completed", value: app.oaCompletedDate, icon: "✅" },
        { label: "Interview Scheduled", value: app.interviewScheduledDate, icon: "🗓️" },
        { label: "Interview Completed", value: app.interviewCompletedDate, icon: "🎤" },
        { label: "Result Received", value: app.resultReceivedDate, icon: "📬" }
    ].filter(m => m.value);

    const timeline = document.getElementById("appTimeline");
    timeline.innerHTML = milestones.map(m => `
        <div class="timeline-milestone">
            <span class="timeline-milestone-icon">${m.icon}</span>
            <span class="timeline-milestone-label">${m.label}</span>
            <span class="timeline-milestone-date">${formatDate(m.value)}</span>
        </div>
    `).join('') || '<p style="font-size:0.85rem; color:var(--secondary-text);">No milestones recorded yet.</p>';

    document.getElementById("appDetailModal").classList.remove("hidden");
}

function closeDetailModal() {
    document.getElementById("appDetailModal").classList.add("hidden");
}

async function handleDeleteApplication() {
    if (!activeDetailApp) return;

    document.getElementById("deleteAppCompanyName").textContent =
        activeDetailApp.companyName || "this application";

    document.getElementById("deleteAppModal").classList.remove("hidden");
}

function initDeleteAppModal() {
    const modal = document.getElementById("deleteAppModal");
    const cancelBtn = document.getElementById("cancelDeleteAppBtn");
    const confirmBtn = document.getElementById("confirmDeleteAppBtn");

    cancelBtn.addEventListener("click", () => {
        modal.classList.add("hidden");
    });

    confirmBtn.addEventListener("click", confirmDeleteApplication);

    modal.addEventListener("click", (e) => {
        if (e.target === modal) {
            modal.classList.add("hidden");
        }
    });
}

async function confirmDeleteApplication() {
    if (!activeDetailApp) return;

    try {
        await apiRequest(`/student-application/${activeDetailApp.id}`, {
            method: "DELETE"
        });

        showToast("Application deleted successfully.", "success");

        document.getElementById("deleteAppModal").classList.add("hidden");
        closeDetailModal();
        activeDetailApp = null;

        await loadApplications();

    } catch (error) {
        console.error("Delete Application Error:", error);
        showToast(error.message || "Failed to delete application.", "error");
    }
}


/* ==========================================================================
   UPDATE STATUS MODAL
   ========================================================================== */
function initUpdateStatusModal() {
    const modal = document.getElementById("updateStatusModal");
    const form = document.getElementById("updateStatusForm");
    const closeBtn = document.getElementById("closeUpdateStatusModalBtn");
    const cancelBtn = document.getElementById("cancelUpdateStatusBtn");

    closeBtn.addEventListener("click", closeUpdateStatusModal);
    cancelBtn.addEventListener("click", closeUpdateStatusModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeUpdateStatusModal(); });

    form.addEventListener("submit", handleUpdateStatusSubmit);
}

function openUpdateStatusModal(app) {
    document.getElementById("updateAppId").value = app.id;
    document.getElementById("updateStatusSelect").value = app.status;
    document.getElementById("updateOaScheduled").value = epochToDateInputValue(app.oaScheduledDate);
    document.getElementById("updateOaCompleted").value = epochToDateInputValue(app.oaCompletedDate);
    document.getElementById("updateInterviewScheduled").value = epochToDateInputValue(app.interviewScheduledDate);
    document.getElementById("updateInterviewCompleted").value = epochToDateInputValue(app.interviewCompletedDate);
    document.getElementById("updateResultReceived").value = epochToDateInputValue(app.resultReceivedDate);
    document.getElementById("updateOfferAccepted").checked = !!app.offerAccepted;
    document.getElementById("updateNotes").value = app.notes || "";

    document.getElementById("updateStatusError").textContent = "";
    document.getElementById("updateStatusSelect").classList.remove("is-invalid");

    document.getElementById("updateStatusModal").classList.remove("hidden");
}

function closeUpdateStatusModal() {
    document.getElementById("updateStatusModal").classList.add("hidden");
}

async function handleUpdateStatusSubmit(e) {
    e.preventDefault();

    const statusSelect = document.getElementById("updateStatusSelect");
    if (!statusSelect.value) {
        statusSelect.classList.add("is-invalid");
        document.getElementById("updateStatusError").textContent = "Please select a status.";
        return;
    }

    const id = document.getElementById("updateAppId").value;
    const submitBtn = document.getElementById("submitUpdateStatusBtn");
    const btnText = submitBtn.querySelector(".btn-text");
    const spinner = document.getElementById("updateStatusSpinner");

    const payload = {
        status: statusSelect.value,
        statusUpdatedAt: Date.now(),
        oaScheduledDate: dateInputValueToEpoch(document.getElementById("updateOaScheduled").value),
        oaCompletedDate: dateInputValueToEpoch(document.getElementById("updateOaCompleted").value),
        interviewScheduledDate: dateInputValueToEpoch(document.getElementById("updateInterviewScheduled").value),
        interviewCompletedDate: dateInputValueToEpoch(document.getElementById("updateInterviewCompleted").value),
        resultReceivedDate: dateInputValueToEpoch(document.getElementById("updateResultReceived").value),
        offerAccepted: document.getElementById("updateOfferAccepted").checked,
        notes: document.getElementById("updateNotes").value.trim() || null
    };

    submitBtn.disabled = true;
    btnText.textContent = "Saving...";
    spinner.classList.remove("hidden");

    try {
        await apiRequest(`/student-application/${id}`, {
            method: "PUT",
            body: JSON.stringify(payload)
        });
        showToast("Application status updated.", "success");
        closeUpdateStatusModal();
        await loadApplications();
    } catch (error) {
        showToast(error.message || "Failed to update status.", "error");
    } finally {
        submitBtn.disabled = false;
        btnText.textContent = "Save Update";
        spinner.classList.add("hidden");
    }
}