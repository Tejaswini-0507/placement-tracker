/**
 * CareerSpace - Search Module Script
 * Pure Vanilla JavaScript (ES6+)
 * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
 *
 * NOTE ON TOPICS PARSING: SearchResponse.topics comes from
 * ExperienceDocument.topics (a List<String>) via plain Java .toString(),
 * which produces "[Arrays, Strings]" - NOT valid JSON. JSON.parse() on this
 * would throw, so it's parsed manually as a bracketed comma-separated string.
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

let currentPage = 0;
const PAGE_SIZE = 12;
let lastResultPage = null;
let currentResults = [];
let activeResult = null;

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("loadCompaniesForFilter", loadCompaniesForFilter);
    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initSearchControls", initSearchControls);
    safeInit("initPagination", initPagination);
    safeInit("initDetailModal", initDetailModal);
    safeInit("performSearch", () => performSearch());
});

function safeInit(name, fn) {
    try { fn(); } catch (error) { console.error(`[search.js] "${name}" failed to initialize:`, error); }
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
   COMPANY FILTER OPTIONS
   ========================================================================== */
async function loadCompaniesForFilter() {
    const select = document.getElementById("filterCompany");
    try {
        const data = await apiRequest("/companies", { method: "GET" });
        const companies = Array.isArray(data) ? data : [];
        companies
            .slice()
            .sort((a, b) => (a.name || "").localeCompare(b.name || ""))
            .forEach(company => {
                const option = document.createElement("option");
                option.value = company.id;
                option.textContent = company.name;
                select.appendChild(option);
            });
    } catch (error) {
        console.error("Failed to load companies for filter:", error);
    }
}


/* ==========================================================================
   SEARCH CONTROLS
   ========================================================================== */
function initSearchControls() {
    const queryInput = document.getElementById("searchQueryInput");
    const companyFilter = document.getElementById("filterCompany");
    const difficultyFilter = document.getElementById("filterDifficulty");
    const resultFilter = document.getElementById("filterResult");
    const topicsFilter = document.getElementById("filterTopics");
    const sortSelect = document.getElementById("sortSelect");
    const clearBtn = document.getElementById("clearFiltersBtn");
    const retryBtn = document.getElementById("searchRetryBtn");

    let debounceTimer;
    const debouncedSearch = () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => { currentPage = 0; performSearch(); }, 350);
    };

    queryInput.addEventListener("input", debouncedSearch);
    topicsFilter.addEventListener("input", debouncedSearch);

    [companyFilter, difficultyFilter, resultFilter, sortSelect].forEach(el => {
        el.addEventListener("change", () => { currentPage = 0; performSearch(); });
    });

    clearBtn.addEventListener("click", () => {
        queryInput.value = "";
        companyFilter.value = "";
        difficultyFilter.value = "";
        resultFilter.value = "";
        topicsFilter.value = "";
        sortSelect.value = "createdAt-desc";
        currentPage = 0;
        performSearch();
    });

    retryBtn.addEventListener("click", () => performSearch());
}


/* ==========================================================================
   PERFORM SEARCH
   ========================================================================== */
async function performSearch() {
    showSearchState("loading");

    const [sortBy, sortOrder] = document.getElementById("sortSelect").value.split("-");
    const topicsRaw = document.getElementById("filterTopics").value.trim();
    const topics = topicsRaw ? topicsRaw.split(",").map(t => t.trim()).filter(Boolean) : null;

    const payload = {
        query: document.getElementById("searchQueryInput").value.trim() || null,
        companyId: document.getElementById("filterCompany").value || null,
        difficultyRating: document.getElementById("filterDifficulty").value || null,
        result: document.getElementById("filterResult").value || null,
        topics: topics,
        page: currentPage,
        size: PAGE_SIZE,
        sortBy: sortBy,
        sortOrder: sortOrder
    };

    try {
        const resultPage = await apiRequest("/search/experiences", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        lastResultPage = resultPage;
        currentResults = resultPage.content || [];

        renderSearchMeta(resultPage);
        renderPagination(resultPage);

        if (currentResults.length === 0) {
            showSearchState("empty");
        } else {
            renderResultsGrid();
            showSearchState("grid");
        }

    } catch (error) {
        console.error("Search Error:", error);
        document.getElementById("searchErrorMessage").textContent = error.message || "Something went wrong.";
        showSearchState("error");
    }
}

function showSearchState(state) {
    const loading = document.getElementById("searchLoading");
    const empty = document.getElementById("searchEmpty");
    const error = document.getElementById("searchError");
    const grid = document.getElementById("searchResultsGrid");
    const pagination = document.getElementById("paginationControls");
    const meta = document.getElementById("searchMeta");

    [loading, empty, error, grid].forEach(el => el.classList.add("hidden"));
    pagination.classList.add("hidden");
    meta.classList.add("hidden");

    if (state === "loading") {
        loading.innerHTML = renderSkeletons(6);
        loading.classList.remove("hidden");
    } else if (state === "empty") {
        empty.classList.remove("hidden");
        meta.classList.remove("hidden");
    } else if (state === "error") {
        error.classList.remove("hidden");
    } else if (state === "grid") {
        grid.classList.remove("hidden");
        pagination.classList.remove("hidden");
        meta.classList.remove("hidden");
    }
}

function renderSkeletons(count) {
    const card = `
        <div class="skeleton-exp-card">
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-badge"></div>
            <div class="skeleton-line skeleton-text"></div>
        </div>
    `;
    return card.repeat(count);
}

function renderSearchMeta(resultPage) {
    const meta = document.getElementById("searchMeta");
    meta.textContent = `${resultPage.totalElements} result${resultPage.totalElements === 1 ? '' : 's'} found`;
}


/* ==========================================================================
   RESULTS GRID
   ========================================================================== */
function renderResultsGrid() {
    const grid = document.getElementById("searchResultsGrid");

    grid.innerHTML = currentResults.map(res => {
        const diffMeta = DIFFICULTY_META[res.difficultyRating] || { label: res.difficultyRating, cls: "diff-medium" };
        const resultMeta = RESULT_META[res.result] || { label: res.result, cls: "result-pending" };

        return `
            <div class="exp-card" data-id="${res.id}">
                <div class="exp-card-top">
                    <div>
                        <div class="exp-card-company">${escapeHtml(res.companyName)}</div>
                        <div class="exp-card-position">${escapeHtml(res.positionTitle || "")}</div>
                    </div>
                    <div class="exp-card-pills">
                        <span class="difficulty-pill ${diffMeta.cls}">${escapeHtml(diffMeta.label)}</span>
                    </div>
                </div>

                <div class="exp-card-round">${escapeHtml(res.interviewRoundName || "Round")}</div>

                <p class="exp-card-summary">${escapeHtml(res.experienceSummary || "")}</p>

                <div class="exp-card-footer">
                    <span class="result-pill ${resultMeta.cls}">${escapeHtml(resultMeta.label)}</span>
                    <div class="exp-card-votes">
                        <span>👍 ${res.upvotes || 0}</span>
                        <span>👎 ${res.downvotes || 0}</span>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    grid.querySelectorAll('.exp-card').forEach(card => {
        card.addEventListener('click', () => {
            const id = card.getAttribute('data-id');
            const result = currentResults.find(r => String(r.id) === String(id));
            if (result) openDetailModal(result);
        });
    });
}


/* ==========================================================================
   PAGINATION
   ========================================================================== */
function initPagination() {
    document.getElementById("prevPageBtn").addEventListener("click", () => {
        if (currentPage > 0) {
            currentPage--;
            performSearch();
        }
    });

    document.getElementById("nextPageBtn").addEventListener("click", () => {
        if (lastResultPage && !lastResultPage.last) {
            currentPage++;
            performSearch();
        }
    });
}

function renderPagination(resultPage) {
    document.getElementById("pageIndicator").textContent =
        `Page ${(resultPage.page || 0) + 1} of ${Math.max(1, resultPage.totalPages || 1)}`;
    document.getElementById("prevPageBtn").disabled = !!resultPage.first;
    document.getElementById("nextPageBtn").disabled = !!resultPage.last;
}


/* ==========================================================================
   TOPICS PARSING (see file header note - this is NOT JSON)
   ========================================================================== */
function parseJavaListString(raw) {
    if (!raw || typeof raw !== "string") return [];
    const trimmed = raw.trim();
    if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return [];
    const inner = trimmed.slice(1, -1).trim();
    if (!inner) return [];
    return inner.split(",").map(s => s.trim()).filter(Boolean);
}

function formatDate(epochMillis) {
    if (!epochMillis) return "—";
    return new Date(epochMillis).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}


/* ==========================================================================
   DETAIL MODAL (READ-ONLY, VOTE ENABLED)
   ========================================================================== */
function initDetailModal() {
    const modal = document.getElementById("searchDetailModal");
    const closeBtn = document.getElementById("closeDetailModalBtn");
    const upvoteBtn = document.getElementById("detailUpvoteBtn");
    const downvoteBtn = document.getElementById("detailDownvoteBtn");

    closeBtn.addEventListener("click", closeDetailModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeDetailModal(); });

    upvoteBtn.addEventListener("click", () => handleVote("upvote"));
    downvoteBtn.addEventListener("click", () => handleVote("downvote"));
}

function openDetailModal(result) {
    activeResult = result;
    const diffMeta = DIFFICULTY_META[result.difficultyRating] || { label: result.difficultyRating, cls: "diff-medium" };
    const resultMeta = RESULT_META[result.result] || { label: result.result, cls: "result-pending" };

    document.getElementById("detailCompany").textContent = result.companyName || "Experience";
    document.getElementById("detailPosition").textContent = result.positionTitle || "";
    document.getElementById("detailRound").textContent = result.interviewRoundName || "";

    const diffPill = document.getElementById("detailDifficulty");
    diffPill.className = `difficulty-pill ${diffMeta.cls}`;
    diffPill.textContent = diffMeta.label;

    const resultPill = document.getElementById("detailResult");
    resultPill.className = `result-pill ${resultMeta.cls}`;
    resultPill.textContent = resultMeta.label;

    document.getElementById("detailDuration").textContent = result.durationMinutes ? `${result.durationMinutes} min` : "—";
    document.getElementById("detailProblems").textContent = result.totalProblemsAsked ?? "—";
    document.getElementById("detailStudent").textContent = result.studentName || "Anonymous";
    document.getElementById("detailScore").textContent = result.score != null ? result.score.toFixed(2) : "—";

    document.getElementById("detailQuestionsAsked").textContent = result.questionsAsked || "—";
    document.getElementById("detailSummary").textContent = result.experienceSummary || "—";

    const resources = result.helpfulResources;
    document.getElementById("detailResourcesBlock").classList.toggle("hidden", !resources);
    document.getElementById("detailResources").textContent = resources || "";

    const topics = parseJavaListString(result.topics);
    const topicsBlock = document.getElementById("detailTopicsBlock");
    const topicsContainer = document.getElementById("detailTopics");
    if (topics.length > 0) {
        topicsBlock.classList.remove("hidden");
        topicsContainer.innerHTML = topics.map(t => `<span class="resume-company-tag">${escapeHtml(t)}</span>`).join('');
    } else {
        topicsBlock.classList.add("hidden");
    }

    document.getElementById("detailUpvoteCount").textContent = result.upvotes || 0;
    document.getElementById("detailDownvoteCount").textContent = result.downvotes || 0;

    document.getElementById("searchDetailModal").classList.remove("hidden");
}

function closeDetailModal() {
    document.getElementById("searchDetailModal").classList.add("hidden");
}

async function handleVote(direction) {
    if (!activeResult) return;

    try {
        // Vote endpoint lives on the InterviewExperienceController, not
        // Search - ExperienceDocument.id mirrors the real experience id
        const updated = await apiRequest(`/experience/${direction}/${activeResult.id}`, { method: "POST" });

        activeResult.upvotes = updated.upvotes;
        activeResult.downvotes = updated.downvotes;

        document.getElementById("detailUpvoteCount").textContent = updated.upvotes || 0;
        document.getElementById("detailDownvoteCount").textContent = updated.downvotes || 0;

        const idx = currentResults.findIndex(r => r.id === activeResult.id);
        if (idx !== -1) {
            currentResults[idx].upvotes = updated.upvotes;
            currentResults[idx].downvotes = updated.downvotes;
        }
        renderResultsGrid();

    } catch (error) {
        showToast(error.message || "Failed to register vote.", "error");
    }
}