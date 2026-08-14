/**
 * CareerSpace - Browse Experiences Module Script
 * Pure Vanilla JavaScript (ES6+)
 * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
 */

const DIFFICULTY_META = {
    EASY:   { label: "Easy",   cls: "diff-easy" },
    MEDIUM: { label: "Medium", cls: "diff-medium" },
    HARD:   { label: "Hard",   cls: "diff-hard" },
    EXPERT: { label: "Expert", cls: "diff-expert" }
};

const RESULT_META = {
    PASSED:       { label: "Passed",       cls: "result-passed" },
    FAILED:       { label: "Failed",       cls: "result-failed" },
    PENDING:      { label: "Pending",      cls: "result-pending" },
    WAITING_LIST: { label: "Waiting List", cls: "result-waiting-list" }
};

let allCompaniesForBrowse = [];
let currentCompanyExperiences = [];
let selectedCompany = null;
let activeBrowseExp = null;

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("loadCompaniesForBrowse", loadCompaniesForBrowse);
    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initCompanyPicker", initCompanyPicker);
    safeInit("initBrowseDetailModal", initBrowseDetailModal);
});

function safeInit(name, fn) {
    try { fn(); } catch (error) { console.error(`[browse-experiences.js] "${name}" failed to initialize:`, error); }
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
   STEP 1: COMPANY PICKER
   ========================================================================== */
async function loadCompaniesForBrowse() {
    showPickerState("loading");
    try {
        const data = await apiRequest("/companies", { method: "GET" });
        allCompaniesForBrowse = Array.isArray(data) ? data : [];
        renderCompanyPicker(allCompaniesForBrowse);
    } catch (error) {
        console.error("Companies Error:", error);
        document.getElementById("companyPickerErrorMessage").textContent = error.message || "Something went wrong.";
        showPickerState("error");
    }
}

function showPickerState(state) {
    const loading = document.getElementById("companyPickerLoading");
    const empty = document.getElementById("companyPickerEmpty");
    const error = document.getElementById("companyPickerError");
    const grid = document.getElementById("companyPickerGrid");
    const noResults = document.getElementById("companyPickerNoResults");

    [loading, empty, error, grid, noResults].forEach(el => el.classList.add("hidden"));

    if (state === "loading") {
        loading.innerHTML = renderPickerSkeletons(6);
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

function renderPickerSkeletons(count) {
    const card = `
        <div class="skeleton-picker-card">
            <div class="skeleton-line skeleton-picker-logo"></div>
            <div class="skeleton-line skeleton-picker-text"></div>
        </div>
    `;
    return card.repeat(count);
}

function renderCompanyPicker(companies) {
    if (allCompaniesForBrowse.length === 0) { showPickerState("empty"); return; }
    if (companies.length === 0) { showPickerState("no-results"); return; }

    const grid = document.getElementById("companyPickerGrid");
    grid.innerHTML = companies.map(company => `
        <div class="picker-card" data-id="${company.id}">
            <div class="picker-card-logo">${getInitials(company.name)}</div>
            <div class="picker-card-info">
                <div class="picker-card-name">${escapeHtml(company.name)}</div>
                ${company.industry ? `<div class="picker-card-industry">${escapeHtml(company.industry)}</div>` : ''}
            </div>
        </div>
    `).join('');

    grid.querySelectorAll('.picker-card').forEach(card => {
        card.addEventListener('click', () => {
            const id = card.getAttribute('data-id');
            const company = allCompaniesForBrowse.find(c => String(c.id) === String(id));
            if (company) selectCompany(company);
        });
    });

    showPickerState("grid");
}

function getInitials(name) {
    if (!name) return "?";
    return name.split(" ").filter(Boolean).map(w => w[0]).join("").toUpperCase().slice(0, 2);
}

function initCompanyPicker() {
    const searchInput = document.getElementById("companyPickerSearch");
    const changeCompanyBtn = document.getElementById("changeCompanyBtn");
    const retryBtn = document.getElementById("companyPickerRetryBtn");

    let debounceTimer;
    searchInput.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            const query = searchInput.value.trim().toLowerCase();
            const filtered = allCompaniesForBrowse.filter(c => (c.name || "").toLowerCase().includes(query));
            renderCompanyPicker(filtered);
        }, 200);
    });

    changeCompanyBtn.addEventListener("click", () => {
        selectedCompany = null;
        document.getElementById("companyExperiencesSection").classList.add("hidden");
        document.getElementById("companyPickerSection").classList.remove("hidden");
    });

    retryBtn.addEventListener("click", loadCompaniesForBrowse);
}

function selectCompany(company) {
    selectedCompany = company;
    document.getElementById("selectedCompanyName").textContent = company.name;
    document.getElementById("companyPickerSection").classList.add("hidden");
    document.getElementById("companyExperiencesSection").classList.remove("hidden");
    loadCompanyExperiences(company.id);
}


/* ==========================================================================
   STEP 2: PUBLIC EXPERIENCES FOR SELECTED COMPANY
   ========================================================================== */
async function loadCompanyExperiences(companyId) {
    showBrowseExpState("loading");
    try {
        const data = await apiRequest(`/experience/company/${companyId}`, { method: "GET" });
        currentCompanyExperiences = Array.isArray(data) ? data : [];
        currentCompanyExperiences.sort((a, b) => (b.upvotes || 0) - (a.upvotes || 0));

        if (currentCompanyExperiences.length === 0) {
            showBrowseExpState("empty");
        } else {
            renderBrowseExpGrid();
            showBrowseExpState("grid");
        }
    } catch (error) {
        console.error("Company Experiences Error:", error);
        document.getElementById("browseExpErrorMessage").textContent = error.message || "Something went wrong.";
        showBrowseExpState("error");
    }
}

function showBrowseExpState(state) {
    const loading = document.getElementById("browseExpLoading");
    const empty = document.getElementById("browseExpEmpty");
    const error = document.getElementById("browseExpError");
    const grid = document.getElementById("browseExpGrid");

    [loading, empty, error, grid].forEach(el => el.classList.add("hidden"));

    if (state === "loading") {
        loading.innerHTML = renderExpSkeletons(3);
        loading.classList.remove("hidden");
    } else if (state === "empty") {
        empty.classList.remove("hidden");
    } else if (state === "error") {
        error.classList.remove("hidden");
    } else if (state === "grid") {
        grid.classList.remove("hidden");
    }
}

function renderExpSkeletons(count) {
    const card = `
        <div class="skeleton-exp-card">
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-badge"></div>
            <div class="skeleton-line skeleton-text"></div>
        </div>
    `;
    return card.repeat(count);
}

function renderBrowseExpGrid() {
    const grid = document.getElementById("browseExpGrid");

    grid.innerHTML = currentCompanyExperiences.map(exp => {
        const diffMeta = DIFFICULTY_META[exp.difficultyRating] || { label: exp.difficultyRating, cls: "diff-medium" };
        const resultMeta = RESULT_META[exp.result] || { label: exp.result, cls: "result-pending" };

        return `
            <div class="exp-card" data-id="${exp.id}">
                <div class="exp-card-top">
                    <div>
                        <div class="exp-card-company">${escapeHtml(exp.positionName || "Position")}</div>
                        <div class="exp-card-position">Shared by ${escapeHtml(exp.studentName || "a student")}</div>
                    </div>
                    <div class="exp-card-pills">
                        <span class="difficulty-pill ${diffMeta.cls}">${escapeHtml(diffMeta.label)}</span>
                    </div>
                </div>

                <div class="exp-card-round">${formatRoundLabel(exp)}</div>

                <p class="exp-card-summary">${escapeHtml(exp.experienceSummary || "")}</p>

                <div class="exp-card-footer">
                    <span class="result-pill ${resultMeta.cls}">${escapeHtml(resultMeta.label)}</span>
                    <div class="exp-card-votes">
                        <span>👍 ${exp.upvotes || 0}</span>
                        <span>👎 ${exp.downvotes || 0}</span>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    grid.querySelectorAll('.exp-card').forEach(card => {
        card.addEventListener('click', () => {
            const id = card.getAttribute('data-id');
            const exp = currentCompanyExperiences.find(e => String(e.id) === String(id));
            if (exp) openBrowseDetailModal(exp);
        });
    });
}

function formatDate(epochMillis) {
    if (!epochMillis) return "—";
    return new Date(epochMillis).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatRoundLabel(exp) {
    const name = exp.interviewRoundName || "Round";
    const number = exp.interviewRoundNumber;
    return number != null ? `Round ${number}: ${escapeHtml(name)}` : escapeHtml(name);
}

function parseQuestionsJson(questionsJsonNode) {
    if (!questionsJsonNode) return [];
    if (Array.isArray(questionsJsonNode)) return questionsJsonNode;
    return [];
}


/* ==========================================================================
   READ-ONLY DETAIL MODAL (VOTE ENABLED)
   ========================================================================== */
function initBrowseDetailModal() {
    const modal = document.getElementById("browseDetailModal");
    const closeBtn = document.getElementById("closeBrowseDetailModalBtn");
    const upvoteBtn = document.getElementById("browseUpvoteBtn");
    const downvoteBtn = document.getElementById("browseDownvoteBtn");

    closeBtn.addEventListener("click", closeBrowseDetailModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeBrowseDetailModal(); });

    upvoteBtn.addEventListener("click", () => handleBrowseVote("upvote"));
    downvoteBtn.addEventListener("click", () => handleBrowseVote("downvote"));
}

function openBrowseDetailModal(exp) {
    activeBrowseExp = exp;
    const diffMeta = DIFFICULTY_META[exp.difficultyRating] || { label: exp.difficultyRating, cls: "diff-medium" };
    const resultMeta = RESULT_META[exp.result] || { label: exp.result, cls: "result-pending" };

    document.getElementById("browseDetailCompany").textContent = exp.companyName || "Experience";
    document.getElementById("browseDetailPosition").textContent = exp.positionName || "";
    document.getElementById("browseDetailRound").textContent = formatRoundLabel(exp);

    const diffPill = document.getElementById("browseDetailDifficulty");
    diffPill.className = `difficulty-pill ${diffMeta.cls}`;
    diffPill.textContent = diffMeta.label;

    const resultPill = document.getElementById("browseDetailResult");
    resultPill.className = `result-pill ${resultMeta.cls}`;
    resultPill.textContent = resultMeta.label;

    document.getElementById("browseDetailDate").textContent = formatDate(exp.dateExperienced);
    document.getElementById("browseDetailDuration").textContent = exp.durationMinutes ? `${exp.durationMinutes} min` : "—";
    document.getElementById("browseDetailProblems").textContent = exp.totalProblemsAsked ?? "—";
    document.getElementById("browseDetailStudent").textContent = exp.studentName || "Anonymous";

    document.getElementById("browseDetailQuestionsAsked").textContent = exp.questionsAsked || "—";
    document.getElementById("browseDetailSummary").textContent = exp.experienceSummary || "—";

    const resources = exp.helpfulResources;
    document.getElementById("browseDetailResourcesBlock").classList.toggle("hidden", !resources);
    document.getElementById("browseDetailResources").textContent = resources || "";

    const structuredQuestions = parseQuestionsJson(exp.questionsJson);
    const sqBlock = document.getElementById("browseDetailStructuredQBlock");
    const sqList = document.getElementById("browseDetailStructuredQList");
    if (structuredQuestions.length > 0) {
        sqBlock.classList.remove("hidden");
        sqList.innerHTML = structuredQuestions.map(q => `
            <div class="structured-q-view">
                <strong>${escapeHtml(q.question)}</strong>
                ${q.notes ? `<span>${escapeHtml(q.notes)}</span>` : ''}
            </div>
        `).join('');
    } else {
        sqBlock.classList.add("hidden");
    }

    const topics = Array.isArray(exp.topics) ? exp.topics : [];
    const topicsBlock = document.getElementById("browseDetailTopicsBlock");
    const topicsContainer = document.getElementById("browseDetailTopics");
    if (topics.length > 0) {
        topicsBlock.classList.remove("hidden");
        topicsContainer.innerHTML = topics.map(t => `<span class="resume-company-tag">${escapeHtml(t)}</span>`).join('');
    } else {
        topicsBlock.classList.add("hidden");
    }

    document.getElementById("browseUpvoteCount").textContent = exp.upvotes || 0;
    document.getElementById("browseDownvoteCount").textContent = exp.downvotes || 0;

    document.getElementById("browseDetailModal").classList.remove("hidden");
}

function closeBrowseDetailModal() {
    document.getElementById("browseDetailModal").classList.add("hidden");
}

async function handleBrowseVote(direction) {
    if (!activeBrowseExp) return;

    try {
        const updated = await apiRequest(`/experience/${direction}/${activeBrowseExp.id}`, { method: "POST" });
        activeBrowseExp = updated;

        document.getElementById("browseUpvoteCount").textContent = updated.upvotes || 0;
        document.getElementById("browseDownvoteCount").textContent = updated.downvotes || 0;

        const idx = currentCompanyExperiences.findIndex(e => e.id === updated.id);
        if (idx !== -1) currentCompanyExperiences[idx] = updated;
        renderBrowseExpGrid();

    } catch (error) {
        showToast(error.message || "Failed to register vote.", "error");
    }
}