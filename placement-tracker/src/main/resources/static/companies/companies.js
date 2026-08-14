/**
 * CareerSpace - Companies Module Script
 * Pure Vanilla JavaScript (ES6+)
 * Depends on apiClient.js being loaded first (apiRequest, showToast, escapeHtml)
 */

let allCompanies = [];       // raw data from backend
let filteredCompanies = [];  // after search/filter/sort applied
let activeDetailCompany = null; // company currently open in detail modal
let pendingDeleteId = null;

document.addEventListener('DOMContentLoaded', () => {

    loadProfile();
    loadCompanies();

    initSidebarBehavior();
    initProfileDropdown();
    initToolbar();
    initFormModal();
    initDetailModal();
    initDeleteModal();
});


/* ==========================================================================
   PROFILE (TOPBAR)
   ========================================================================== */
async function loadProfile() {
    try {
        const data = await apiRequest("/profile/me", { method: "GET" });

        document.getElementById("userDisplayName").textContent = data.name;
        document.getElementById("profileName").textContent = data.name;
        document.getElementById("profileEmail").textContent = data.email;
        document.getElementById("profileBranch").textContent = `${data.branch ?? ""} ${data.batch ?? ""}`.trim();

        const initials = (data.name || "")
            .split(" ")
            .filter(Boolean)
            .map(word => word[0])
            .join("")
            .toUpperCase()
            .slice(0, 2);

        document.getElementById("avatarCircle").textContent = initials || "--";

    } catch (error) {
        console.error("Profile Error:", error);
        // apiRequest already redirects to login on 401; other errors fail silently on topbar
    }
}


/* ==========================================================================
   SIDEBAR / DROPDOWN / MOBILE NAV (shared shell behavior)
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
   LOAD COMPANIES
   ========================================================================== */
async function loadCompanies() {
    showState("loading");

    try {
        const data = await apiRequest("/companies", { method: "GET" });
        allCompanies = Array.isArray(data) ? data : [];

        populateIndustryFilter();
        applyFiltersAndRender();

    } catch (error) {
        console.error("Companies Error:", error);
        document.getElementById("companiesErrorMessage").textContent = error.message || "Something went wrong.";
        showState("error");
    }
}

function populateIndustryFilter() {
    const select = document.getElementById("industryFilter");
    const currentValue = select.value;

    const industries = [...new Set(allCompanies.map(c => c.industry).filter(Boolean))].sort();

    select.innerHTML = `<option value="">All Industries</option>` +
        industries.map(ind => `<option value="${escapeHtml(ind)}">${escapeHtml(ind)}</option>`).join('');

    select.value = currentValue;
}


/* ==========================================================================
   STATE MANAGEMENT (loading / empty / error / grid / no-results)
   ========================================================================== */
function showState(state) {
    const loading = document.getElementById("companiesLoading");
    const empty = document.getElementById("companiesEmpty");
    const error = document.getElementById("companiesError");
    const grid = document.getElementById("companiesGrid");
    const noResults = document.getElementById("noResultsState");

    loading.classList.add("hidden");
    empty.classList.add("hidden");
    error.classList.add("hidden");
    grid.classList.add("hidden");
    noResults.classList.add("hidden");

    if (state === "loading") {
        loading.innerHTML = renderSkeletons(6);
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
        <div class="skeleton-card">
            <div class="company-card-top">
                <div class="skeleton-line skeleton-avatar"></div>
                <div style="flex:1;">
                    <div class="skeleton-line skeleton-title"></div>
                    <div class="skeleton-line skeleton-badge"></div>
                </div>
            </div>
            <div class="skeleton-line skeleton-text"></div>
            <div class="skeleton-line skeleton-text-short"></div>
        </div>
    `;
    return card.repeat(count);
}


/* ==========================================================================
   TOOLBAR: SEARCH / FILTER / SORT
   ========================================================================== */
function initToolbar() {
    const searchInput = document.getElementById("companySearchInput");
    const industryFilter = document.getElementById("industryFilter");
    const sortSelect = document.getElementById("sortSelect");
    const addBtn = document.getElementById("addCompanyBtn");
    const emptyStateAddBtn = document.getElementById("emptyStateAddBtn");
    const retryBtn = document.getElementById("retryLoadBtn");
    const clearFiltersBtn = document.getElementById("clearFiltersBtn");

    let debounceTimer;
    searchInput.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(applyFiltersAndRender, 200);
    });

    industryFilter.addEventListener("change", applyFiltersAndRender);
    sortSelect.addEventListener("change", applyFiltersAndRender);

    addBtn.addEventListener("click", () => openFormModal());
    emptyStateAddBtn.addEventListener("click", () => openFormModal());
    retryBtn.addEventListener("click", loadCompanies);

    clearFiltersBtn.addEventListener("click", () => {
        searchInput.value = "";
        industryFilter.value = "";
        sortSelect.value = "name-asc";
        applyFiltersAndRender();
    });
}

function applyFiltersAndRender() {
    const query = document.getElementById("companySearchInput").value.trim().toLowerCase();
    const industry = document.getElementById("industryFilter").value;
    const sortBy = document.getElementById("sortSelect").value;

    filteredCompanies = allCompanies.filter(c => {
        const matchesQuery = !query || (c.name || "").toLowerCase().includes(query);
        const matchesIndustry = !industry || c.industry === industry;
        return matchesQuery && matchesIndustry;
    });

    filteredCompanies.sort((a, b) => {
        switch (sortBy) {
            case "name-desc":
                return (b.name || "").localeCompare(a.name || "");
            case "applicants-desc":
                return (b.totalApplicants || 0) - (a.totalApplicants || 0);
            case "selected-desc":
                return (b.totalSelected || 0) - (a.totalSelected || 0);
            case "difficulty-desc":
                return (b.averageDifficulty || 0) - (a.averageDifficulty || 0);
            case "name-asc":
            default:
                return (a.name || "").localeCompare(b.name || "");
        }
    });

    if (allCompanies.length === 0) {
        showState("empty");
        return;
    }

    if (filteredCompanies.length === 0) {
        showState("no-results");
        return;
    }

    renderCompaniesGrid();
    showState("grid");
}


/* ==========================================================================
   RENDER COMPANY CARDS
   ========================================================================== */
function renderCompaniesGrid() {
    const grid = document.getElementById("companiesGrid");

    grid.innerHTML = filteredCompanies.map(company => {
        const initials = getInitials(company.name);
        const logoHtml = company.logoUrl
            ? `<img src="${escapeHtml(company.logoUrl)}" alt="${escapeHtml(company.name)} logo" onerror="this.parentElement.textContent='${initials}'">`
            : initials;

        const selectionRate = calculateSelectionRate(company.totalApplicants, company.totalSelected);
        const difficultyDisplay = company.averageDifficulty != null
            ? `${Number(company.averageDifficulty).toFixed(1)} / 5`
            : "—";

        return `
            <div class="company-card" data-id="${company.id}">
                <div class="company-card-top">
                    <div class="company-logo-box">${logoHtml}</div>
                    <div class="company-card-title-group">
                        <div class="company-card-name">${escapeHtml(company.name)}</div>
                        ${company.industry ? `<span class="company-card-industry">${escapeHtml(company.industry)}</span>` : ''}
                    </div>
                </div>

                ${company.headQuarters ? `<div class="company-card-hq">📍 ${escapeHtml(company.headQuarters)}</div>` : ''}

                ${company.hiringFor ? `
                    <div class="company-card-hiring">
                        <strong>Hiring For</strong>
                        ${escapeHtml(company.hiringFor)}
                    </div>
                ` : ''}

                <div class="company-card-footer">
                    <div class="company-card-stat">
                        <span class="company-card-stat-label">Package</span>
                        <span class="company-card-stat-value">${company.packagesOffered ? escapeHtml(company.packagesOffered) : '—'}</span>
                    </div>
                    <div class="company-card-stat">
                        <span class="company-card-stat-label">Selected</span>
                        <span class="company-card-stat-value">${selectionRate}</span>
                    </div>
                    <div class="company-card-stat">
                        <span class="company-card-stat-label">Difficulty</span>
                        <span class="company-card-stat-value">${difficultyDisplay}</span>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    grid.querySelectorAll('.company-card').forEach(card => {
        card.addEventListener('click', () => {
            const id = card.getAttribute('data-id');
            const company = allCompanies.find(c => String(c.id) === String(id));
            if (company) openDetailModal(company);
        });
    });
}

function getInitials(name) {
    if (!name) return "?";
    return name.split(" ").filter(Boolean).map(w => w[0]).join("").toUpperCase().slice(0, 2);
}

function calculateSelectionRate(applicants, selected) {
    if (!applicants || !selected) return "—";
    return `${selected} / ${applicants}`;
}


/* ==========================================================================
   FORM MODAL (ADD / EDIT)
   ========================================================================== */
function initFormModal() {
    const modal = document.getElementById("companyFormModal");
    const form = document.getElementById("companyForm");
    const closeBtn = document.getElementById("closeFormModalBtn");
    const cancelBtn = document.getElementById("cancelFormBtn");

    closeBtn.addEventListener("click", closeFormModal);
    cancelBtn.addEventListener("click", closeFormModal);
    modal.addEventListener("click", (e) => {
        if (e.target === modal) closeFormModal();
    });

    form.addEventListener("submit", handleFormSubmit);
}

function openFormModal(company = null) {
    const form = document.getElementById("companyForm");
    form.reset();
    clearFormErrors();

    document.getElementById("companyFormTitle").textContent = company ? "Edit Company" : "Add Company";
    document.getElementById("companyId").value = company ? company.id : "";

    if (company) {
        document.getElementById("companyName").value = company.name || "";
        document.getElementById("companyIndustry").value = company.industry || "";
        document.getElementById("companyHeadquarters").value = company.headQuarters || "";
        document.getElementById("companyWebsite").value = company.website || "";
//        document.getElementById("companyLogoUrl").value = company.logoUrl || "";
        document.getElementById("companyDescription").value = company.description || "";
        document.getElementById("companyHiringFor").value = company.hiringFor || "";
        document.getElementById("companyPackages").value = company.packagesOffered || "";
        document.getElementById("companyDifficulty").value = company.averageDifficulty ?? "";
        document.getElementById("companyTotalApplicants").value = company.totalApplicants ?? "";
        document.getElementById("companyTotalSelected").value = company.totalSelected ?? "";
    }

    document.getElementById("companyFormModal").classList.remove("hidden");
}

function closeFormModal() {
    document.getElementById("companyFormModal").classList.add("hidden");
}

function clearFormErrors() {
    document.querySelectorAll("#companyForm .field-error").forEach(el => el.textContent = "");
    document.querySelectorAll("#companyForm .is-invalid").forEach(el => el.classList.remove("is-invalid"));
}

function setFieldError(fieldId, errorId, message) {
    document.getElementById(fieldId).classList.add("is-invalid");
    document.getElementById(errorId).textContent = message;
}

function validateForm() {
    clearFormErrors();
    let isValid = true;

    const name = document.getElementById("companyName").value.trim();
    const industry = document.getElementById("companyIndustry").value.trim();
    const headQuarters = document.getElementById("companyHeadquarters").value.trim();
    const description = document.getElementById("companyDescription").value.trim();
    const hiringFor = document.getElementById("companyHiringFor").value.trim();
    const difficulty = document.getElementById("companyDifficulty").value;
    const totalApplicants = document.getElementById("companyTotalApplicants").value;
    const totalSelected = document.getElementById("companyTotalSelected").value;

    if (!name) { setFieldError("companyName", "companyNameError", "Company name is required."); isValid = false; }
    if (!industry) { setFieldError("companyIndustry", "companyIndustryError", "Industry is required."); isValid = false; }
    if (!headQuarters) { setFieldError("companyHeadquarters", "companyHeadquartersError", "Headquarters is required."); isValid = false; }
    if (!description) { setFieldError("companyDescription", "companyDescriptionError", "Description is required."); isValid = false; }
    if (!hiringFor) { setFieldError("companyHiringFor", "companyHiringForError", "Hiring for is required."); isValid = false; }

    if (difficulty !== "" && Number(difficulty) <= 0) {
        setFieldError("companyDifficulty", "companyDifficultyError", "Must be a positive number.");
        isValid = false;
    }
    if (totalApplicants !== "" && Number(totalApplicants) <= 0) {
        setFieldError("companyTotalApplicants", "companyTotalApplicantsError", "Must be a positive number.");
        isValid = false;
    }
    if (totalSelected !== "" && Number(totalSelected) <= 0) {
        setFieldError("companyTotalSelected", "companyTotalSelectedError", "Must be a positive number.");
        isValid = false;
    }
    if (totalApplicants !== "" && totalSelected !== "" && Number(totalSelected) > Number(totalApplicants)) {
        setFieldError("companyTotalSelected", "companyTotalSelectedError", "Cannot exceed total applicants.");
        isValid = false;
    }

    return isValid;
}

async function handleFormSubmit(e) {
    e.preventDefault();

    if (!validateForm()) return;

    const id = document.getElementById("companyId").value;
    const submitBtn = document.getElementById("submitCompanyBtn");
    const btnText = submitBtn.querySelector(".btn-text");
    const spinner = document.getElementById("companyFormSpinner");

    const payload = {
        name: document.getElementById("companyName").value.trim(),
        industry: document.getElementById("companyIndustry").value.trim(),
        headQuarters: document.getElementById("companyHeadquarters").value.trim(),
        website: document.getElementById("companyWebsite").value.trim() || null,
//        logoUrl: document.getElementById("companyLogoUrl").value.trim() || null,
        description: document.getElementById("companyDescription").value.trim(),
        hiringFor: document.getElementById("companyHiringFor").value.trim(),
        packagesOffered: document.getElementById("companyPackages").value.trim() || null,
        averageDifficulty: document.getElementById("companyDifficulty").value !== ""
            ? Number(document.getElementById("companyDifficulty").value) : null,
        totalApplicants: document.getElementById("companyTotalApplicants").value !== ""
            ? Number(document.getElementById("companyTotalApplicants").value) : null,
        totalSelected: document.getElementById("companyTotalSelected").value !== ""
            ? Number(document.getElementById("companyTotalSelected").value) : null
    };

    submitBtn.disabled = true;
    btnText.textContent = id ? "Saving..." : "Creating...";
    spinner.classList.remove("hidden");

    try {
        if (id) {
            await apiRequest(`/companies/${id}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });
            showToast("Company updated successfully.", "success");
        } else {
            await apiRequest("/companies", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            showToast("Company created successfully.", "success");
        }

        closeFormModal();
        await loadCompanies();

    } catch (error) {
        showToast(error.message || "Failed to save company.", "error");
    } finally {
        submitBtn.disabled = false;
        btnText.textContent = "Save Company";
        spinner.classList.add("hidden");
    }
}


/* ==========================================================================
   DETAIL MODAL
   ========================================================================== */
function initDetailModal() {
    const modal = document.getElementById("companyDetailModal");
    const closeBtn = document.getElementById("closeDetailModalBtn");
    const editBtn = document.getElementById("editCompanyBtn");
    const deleteBtn = document.getElementById("deleteCompanyBtn");

    closeBtn.addEventListener("click", closeDetailModal);
    modal.addEventListener("click", (e) => {
        if (e.target === modal) closeDetailModal();
    });

    editBtn.addEventListener("click", () => {
        closeDetailModal();
        openFormModal(activeDetailCompany);
    });

    deleteBtn.addEventListener("click", () => {
        closeDetailModal();
        openDeleteModal(activeDetailCompany);
    });
}

function openDetailModal(company) {
    activeDetailCompany = company;

    document.getElementById("detailCompanyName").textContent = company.name || "Company";
    document.getElementById("detailLogoBox").textContent = getInitials(company.name);
    document.getElementById("detailIndustry").textContent = company.industry || "—";
    document.getElementById("detailHeadquarters").textContent = company.headQuarters ? `📍 ${company.headQuarters}` : "";
    document.getElementById("detailDescription").textContent = company.description || "No description provided.";
    document.getElementById("detailHiringFor").textContent = company.hiringFor || "—";
    document.getElementById("detailPackages").textContent = company.packagesOffered || "—";
    document.getElementById("detailDifficulty").textContent = company.averageDifficulty != null
        ? `${Number(company.averageDifficulty).toFixed(1)} / 5` : "—";
    document.getElementById("detailSelectionRate").textContent = calculateSelectionRate(company.totalApplicants, company.totalSelected);

    const websiteLink = document.getElementById("detailWebsite");
    if (company.website) {
        websiteLink.href = company.website;
        websiteLink.classList.remove("hidden");
    } else {
        websiteLink.classList.add("hidden");
    }

    document.getElementById("companyDetailModal").classList.remove("hidden");
}

function closeDetailModal() {
    document.getElementById("companyDetailModal").classList.add("hidden");
}


/* ==========================================================================
   DELETE CONFIRM MODAL
   ========================================================================== */
function initDeleteModal() {
    const modal = document.getElementById("deleteConfirmModal");
    const cancelBtn = document.getElementById("cancelDeleteBtn");
    const confirmBtn = document.getElementById("confirmDeleteBtn");

    cancelBtn.addEventListener("click", closeDeleteModal);
    modal.addEventListener("click", (e) => {
        if (e.target === modal) closeDeleteModal();
    });

    confirmBtn.addEventListener("click", handleConfirmDelete);
}

function openDeleteModal(company) {
    pendingDeleteId = company.id;
    document.getElementById("deleteCompanyName").textContent = company.name || "this company";
    document.getElementById("deleteConfirmModal").classList.remove("hidden");
}

function closeDeleteModal() {
    pendingDeleteId = null;
    document.getElementById("deleteConfirmModal").classList.add("hidden");
}

async function handleConfirmDelete() {
    if (!pendingDeleteId) return;

    const confirmBtn = document.getElementById("confirmDeleteBtn");
    const btnText = confirmBtn.querySelector(".btn-text");
    const spinner = document.getElementById("deleteSpinner");

    confirmBtn.disabled = true;
    btnText.textContent = "Deleting...";
    spinner.classList.remove("hidden");

    try {
        await apiRequest(`/companies/${pendingDeleteId}`, { method: "DELETE" });
        showToast("Company deleted.", "success");
        closeDeleteModal();
        await loadCompanies();

    } catch (error) {
        showToast(error.message || "Failed to delete company.", "error");
    } finally {
        confirmBtn.disabled = false;
        btnText.textContent = "Delete";
        spinner.classList.add("hidden");
    }
}
