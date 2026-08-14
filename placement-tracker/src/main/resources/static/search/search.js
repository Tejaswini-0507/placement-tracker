///**
// * CareerSpace - Search Experiences Page Script
// * Pure Vanilla JavaScript (ES6+)
// * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
// *
// * Endpoint used:
// *   POST /search/experiences  (body: SearchRequest) -> SearchResultPage
// *
// * NOTES ON BACKEND SHAPES THIS FILE WORKS AROUND:
// * - SearchResponse.topics is a raw Java List.toString() from the server
// *   (e.g. "[Java, Arrays, DP]" or "[]"), not JSON - parseTopicsString()
// *   below strips the brackets and splits on ", " rather than JSON.parse().
// * - SearchResponse.difficultyRating and .result arrive as plain strings
// *   (already enum.name()-shaped, e.g. "MEDIUM", "PASSED").
// *
// * ASSUMPTION FLAGGED: no company-list or upvote/downvote controller was
// * provided. This file assumes:
// *   GET  /companies                    -> array of { id, name, ... } for the
// *                                          company filter dropdown
// *   POST /experiences/{id}/upvote      -> updated vote counts
// *   POST /experiences/{id}/downvote    -> updated vote counts
// * If your actual endpoints differ, update loadCompanyFilter() and
// * castVote() below - everything else (search, filters, pagination, detail
// * view) only depends on the SearchController you shared.
// */
//
//const state = {
//    query: "",
//    companyId: "",
//    difficultyRating: "",
//    result: "",
//    topics: [],
//    sortBy: "createdAt",
//    sortOrder: "desc",
//    page: 0,
//    size: 12
//};
//
//let lastResultPage = null;
//let currentDetailItem = null;
//let searchDebounceTimer = null;
//
//document.addEventListener('DOMContentLoaded', () => {
//    safeInit("loadTopbarProfile", loadTopbarProfile);
//    safeInit("initSidebarBehavior", initSidebarBehavior);
//    safeInit("initProfileDropdown", initProfileDropdown);
//    safeInit("loadCompanyFilter", loadCompanyFilter);
//    safeInit("initFilterControls", initFilterControls);
//    safeInit("initPagination", initPagination);
//    safeInit("initDetailModal", initDetailModal);
//    safeInit("performSearch", performSearch);
//});
//
//function safeInit(name, fn) {
//    try { fn(); } catch (error) { console.error(`[search.js] "${name}" failed to initialize:`, error); }
//}
//
//
///* ==========================================================================
//   TOPBAR PROFILE (shared shell pattern)
//   ========================================================================== */
//async function loadTopbarProfile() {
//    try {
//        const data = await apiRequest("/profile/me", { method: "GET" });
//        const initials = getInitials(data.name);
//
//        document.getElementById("userDisplayName").textContent = data.name || "—";
//        document.getElementById("avatarCircle").textContent = initials;
//        document.getElementById("profileName").textContent = data.name || "—";
//        document.getElementById("profileEmail").textContent = data.email || "—";
//        document.getElementById("profileBranch").textContent = `${data.branch ?? ""} ${data.batch ?? ""}`.trim() || "—";
//    } catch (error) {
//        console.error("Profile Error:", error);
//    }
//}
//
//function getInitials(name) {
//    if (!name) return "--";
//    return name.split(" ").filter(Boolean).map(w => w[0]).join("").toUpperCase().slice(0, 2);
//}
//
//
///* ==========================================================================
//   SIDEBAR / DROPDOWN (shared shell pattern)
//   ========================================================================== */
//function initSidebarBehavior() {
//    const sidebarToggle = document.getElementById('sidebarToggle');
//    const dashSidebar = document.getElementById('dashSidebar');
//    const sidebarBackdrop = document.getElementById('sidebarBackdrop');
//
//    if (sidebarToggle && dashSidebar && sidebarBackdrop) {
//        const toggleSidebar = () => {
//            dashSidebar.classList.toggle('open');
//            sidebarBackdrop.classList.toggle('active');
//        };
//        sidebarToggle.addEventListener('click', toggleSidebar);
//        sidebarBackdrop.addEventListener('click', toggleSidebar);
//    }
//}
//
//function initProfileDropdown() {
//    const avatarBtn = document.getElementById('avatarBtn');
//    const profileDropdown = document.getElementById('profileDropdown');
//    const logoutLink = document.getElementById('logoutLink');
//
//    if (avatarBtn && profileDropdown) {
//        avatarBtn.addEventListener('click', (e) => {
//            e.stopPropagation();
//            const isOpen = !profileDropdown.classList.contains('hidden');
//            profileDropdown.classList.toggle('hidden', isOpen);
//            avatarBtn.setAttribute('aria-expanded', String(!isOpen));
//        });
//
//        document.addEventListener('click', (e) => {
//            if (!profileDropdown.contains(e.target) && !avatarBtn.contains(e.target)) {
//                profileDropdown.classList.add('hidden');
//                avatarBtn.setAttribute('aria-expanded', 'false');
//            }
//        });
//    }
//
//    if (logoutLink) {
//        logoutLink.addEventListener('click', (e) => {
//            e.preventDefault();
//            localStorage.removeItem("token");
//            localStorage.removeItem("studentId");
//            localStorage.removeItem("email");
//            localStorage.removeItem("name");
//            window.location.href = "login.html";
//        });
//    }
//}
//
//
///* ==========================================================================
//   COMPANY FILTER DROPDOWN
//   ========================================================================== */
//async function loadCompanyFilter() {
//    const select = document.getElementById("filterCompany");
//    if (!select) return;
//
//    try {
//        const companies = await apiRequest("/companies", { method: "GET" });
//        const list = Array.isArray(companies) ? companies : (companies.content || []);
//
//        list
//            .slice()
//            .sort((a, b) => (a.name || "").localeCompare(b.name || ""))
//            .forEach(company => {
//                const option = document.createElement("option");
//                option.value = company.id;
//                option.textContent = company.name;
//                select.appendChild(option);
//            });
//    } catch (error) {
//        // Non-fatal - search still works without the company filter populated
//        console.error("Company Filter Error:", error);
//    }
//}
//
//
///* ==========================================================================
//   FILTER CONTROLS
//   ========================================================================== */
//function initFilterControls() {
//    const queryInput = document.getElementById("searchQueryInput");
//    const companySelect = document.getElementById("filterCompany");
//    const difficultySelect = document.getElementById("filterDifficulty");
//    const resultSelect = document.getElementById("filterResult");
//    const topicsInput = document.getElementById("filterTopics");
//    const sortSelect = document.getElementById("sortSelect");
//    const clearBtn = document.getElementById("clearFiltersBtn");
//
//    queryInput?.addEventListener("input", () => {
//        clearTimeout(searchDebounceTimer);
//        searchDebounceTimer = setTimeout(() => {
//            state.query = queryInput.value.trim();
//            state.page = 0;
//            performSearch();
//        }, 400);
//    });
//
//    companySelect?.addEventListener("change", () => {
//        state.companyId = companySelect.value;
//        state.page = 0;
//        performSearch();
//    });
//
//    difficultySelect?.addEventListener("change", () => {
//        state.difficultyRating = difficultySelect.value;
//        state.page = 0;
//        performSearch();
//    });
//
//    resultSelect?.addEventListener("change", () => {
//        state.result = resultSelect.value;
//        state.page = 0;
//        performSearch();
//    });
//
//    topicsInput?.addEventListener("input", () => {
//        clearTimeout(searchDebounceTimer);
//        searchDebounceTimer = setTimeout(() => {
//            state.topics = topicsInput.value.split(",").map(t => t.trim()).filter(Boolean);
//            state.page = 0;
//            performSearch();
//        }, 400);
//    });
//
//    sortSelect?.addEventListener("change", () => {
//        const [sortBy, sortOrder] = sortSelect.value.split("-");
//        state.sortBy = sortBy;
//        state.sortOrder = sortOrder;
//        state.page = 0;
//        performSearch();
//    });
//
//    clearBtn?.addEventListener("click", () => {
//        state.query = "";
//        state.companyId = "";
//        state.difficultyRating = "";
//        state.result = "";
//        state.topics = [];
//        state.sortBy = "createdAt";
//        state.sortOrder = "desc";
//        state.page = 0;
//
//        if (queryInput) queryInput.value = "";
//        if (companySelect) companySelect.value = "";
//        if (difficultySelect) difficultySelect.value = "";
//        if (resultSelect) resultSelect.value = "";
//        if (topicsInput) topicsInput.value = "";
//        if (sortSelect) sortSelect.value = "createdAt-desc";
//
//        performSearch();
//    });
//}
//
//
///* ==========================================================================
//   PAGINATION
//   ========================================================================== */
//function initPagination() {
//    document.getElementById("prevPageBtn")?.addEventListener("click", () => {
//        if (state.page > 0) {
//            state.page -= 1;
//            performSearch();
//        }
//    });
//
//    document.getElementById("nextPageBtn")?.addEventListener("click", () => {
//        if (lastResultPage && !lastResultPage.last) {
//            state.page += 1;
//            performSearch();
//        }
//    });
//}
//
//function renderPagination(resultPage) {
//    const controls = document.getElementById("paginationControls");
//    const indicator = document.getElementById("pageIndicator");
//    const prevBtn = document.getElementById("prevPageBtn");
//    const nextBtn = document.getElementById("nextPageBtn");
//
//    if (!controls) return;
//
//    if (!resultPage || resultPage.totalElements === 0) {
//        controls.classList.add("hidden");
//        return;
//    }
//
//    controls.classList.remove("hidden");
//    const totalPages = Math.max(1, resultPage.totalPages || 1);
//    if (indicator) indicator.textContent = `Page ${resultPage.page + 1} of ${totalPages}`;
//    if (prevBtn) prevBtn.disabled = !!resultPage.first;
//    if (nextBtn) nextBtn.disabled = !!resultPage.last;
//}
//
//
///* ==========================================================================
//   PERFORM SEARCH
//   ========================================================================== */
//async function performSearch() {
//    toggleResultState("loading");
//
//    const requestBody = {
//        query: state.query || null,
//        companyId: state.companyId || null,
//        difficultyRating: state.difficultyRating || null,
//        topics: state.topics.length > 0 ? state.topics : null,
//        result: state.result || null,
//        isPublic: true,
//        page: state.page,
//        size: state.size,
//        sortBy: state.sortBy,
//        sortOrder: state.sortOrder
//    };
//
//    try {
//        const resultPage = await apiRequest("/search/experiences", {
//            method: "POST",
//            body: JSON.stringify(requestBody)
//        });
//
//        lastResultPage = resultPage;
//        renderResults(resultPage);
//        renderPagination(resultPage);
//
//        const meta = document.getElementById("searchMeta");
//        if (meta) {
//            meta.classList.toggle("hidden", !resultPage.totalElements);
//            meta.innerHTML = resultPage.totalElements
//                ? `Found <strong>${resultPage.totalElements}</strong> ${resultPage.totalElements === 1 ? 'result' : 'results'}`
//                : '';
//        }
//
//        toggleResultState((resultPage.content || []).length === 0 ? "empty" : "results");
//
//    } catch (error) {
//        console.error("Search Error:", error);
//        const msgEl = document.getElementById("searchErrorMessage");
//        if (msgEl) msgEl.textContent = error.message || "Something went wrong while searching.";
//        toggleResultState("error");
//    }
//}
//
//document.getElementById("searchRetryBtn")?.addEventListener("click", performSearch);
//
//function toggleResultState(state_) {
//    const loading = document.getElementById("searchLoading");
//    const empty = document.getElementById("searchEmpty");
//    const error = document.getElementById("searchError");
//    const grid = document.getElementById("searchResultsGrid");
//
//    if (loading) {
//        loading.classList.toggle("hidden", state_ !== "loading");
//        if (state_ === "loading") {
//            loading.innerHTML = Array.from({ length: 6 }).map(() => `<div class="skeleton-exp-card"></div>`).join('');
//        }
//    }
//    if (empty) empty.classList.toggle("hidden", state_ !== "empty");
//    if (error) error.classList.toggle("hidden", state_ !== "error");
//    if (grid) grid.classList.toggle("hidden", state_ !== "results");
//}
//
//
///* ==========================================================================
//   RENDER RESULTS
//   ========================================================================== */
//function renderResults(resultPage) {
//    const grid = document.getElementById("searchResultsGrid");
//    if (!grid) return;
//
//    const items = resultPage.content || [];
//    grid.innerHTML = items.map((item, index) => {
//        const topics = parseTopicsString(item.topics);
//        const snippet = item.experienceSummary || item.questionsAsked || "No summary provided.";
//
//        return `
//            <div class="experience-card" data-index="${index}">
//                <div class="exp-card-header">
//                    <div class="exp-card-title">
//                        <h3>${escapeHtml(item.companyName || "Unknown Company")}</h3>
//                        <span>${escapeHtml(item.positionTitle || "")}${item.interviewRoundName ? ` • ${escapeHtml(item.interviewRoundName)}` : ''}</span>
//                    </div>
//                </div>
//
//                <div class="exp-card-badges">
//                    ${item.difficultyRating ? `<span class="difficulty-pill ${escapeHtml(item.difficultyRating)}">${escapeHtml(capitalize(item.difficultyRating))}</span>` : ''}
//                    ${item.result ? `<span class="result-pill ${escapeHtml(item.result)}">${escapeHtml(capitalize(item.result))}</span>` : ''}
//                </div>
//
//                <p class="exp-card-snippet">${escapeHtml(snippet)}</p>
//
//                ${topics.length > 0 ? `
//                    <div class="exp-card-tags">
//                        ${topics.slice(0, 4).map(t => `<span class="exp-card-tag">${escapeHtml(t)}</span>`).join('')}
//                    </div>
//                ` : ''}
//
//                <div class="exp-card-footer">
//                    <span>${escapeHtml(item.studentName || "Anonymous")}</span>
//                    <div class="exp-card-votes">
//                        <span>👍 ${item.upvotes || 0}</span>
//                        <span>👎 ${item.downvotes || 0}</span>
//                    </div>
//                </div>
//            </div>
//        `;
//    }).join('');
//
//    grid.querySelectorAll(".experience-card").forEach(card => {
//        card.addEventListener("click", () => {
//            const idx = parseInt(card.getAttribute("data-index"), 10);
//            openDetailModal(items[idx]);
//        });
//    });
//}
//
//function parseTopicsString(raw) {
//    if (!raw) return [];
//    if (Array.isArray(raw)) return raw;
//    const trimmed = String(raw).trim();
//    const stripped = trimmed.startsWith("[") && trimmed.endsWith("]")
//        ? trimmed.slice(1, -1)
//        : trimmed;
//    return stripped.split(",").map(t => t.trim()).filter(Boolean);
//}
//
//function capitalize(str) {
//    if (!str) return "";
//    return str.charAt(0) + str.slice(1).toLowerCase().replace(/_/g, ' ');
//}
//
//
///* ==========================================================================
//   DETAIL MODAL
//   ========================================================================== */
//function initDetailModal() {
//    const modal = document.getElementById("searchDetailModal");
//    const closeBtn = document.getElementById("closeDetailModalBtn");
//
//    closeBtn?.addEventListener("click", closeDetailModal);
//    modal?.addEventListener("click", (e) => { if (e.target === modal) closeDetailModal(); });
//
//    document.getElementById("detailUpvoteBtn")?.addEventListener("click", () => castVote("upvote"));
//    document.getElementById("detailDownvoteBtn")?.addEventListener("click", () => castVote("downvote"));
//}
//
//function openDetailModal(item) {
//    currentDetailItem = item;
//    const topics = parseTopicsString(item.topics);
//
//    document.getElementById("detailCompany").textContent = item.companyName || "Unknown Company";
//    document.getElementById("detailDifficulty").textContent = capitalize(item.difficultyRating) || "—";
//    document.getElementById("detailDifficulty").className = `difficulty-pill ${item.difficultyRating || ''}`;
//    document.getElementById("detailResult").textContent = capitalize(item.result) || "—";
//    document.getElementById("detailResult").className = `result-pill ${item.result || ''}`;
//    document.getElementById("detailPosition").textContent = item.positionTitle || "—";
//    document.getElementById("detailRound").textContent = item.interviewRoundName || "—";
//
//    document.getElementById("detailDuration").textContent = item.durationMinutes ? `${item.durationMinutes} min` : "—";
//    document.getElementById("detailProblems").textContent = item.totalProblemsAsked ?? "—";
//    document.getElementById("detailStudent").textContent = item.studentName || "Anonymous";
//    document.getElementById("detailScore").textContent = item.score != null ? item.score.toFixed(2) : "—";
//
//    document.getElementById("detailQuestionsAsked").textContent = item.questionsAsked || "No questions recorded.";
//    document.getElementById("detailSummary").textContent = item.experienceSummary || "No summary provided.";
//
//    const resourcesBlock = document.getElementById("detailResourcesBlock");
//    if (item.helpfulResources) {
//        resourcesBlock.classList.remove("hidden");
//        document.getElementById("detailResources").textContent = item.helpfulResources;
//    } else {
//        resourcesBlock.classList.add("hidden");
//    }
//
//    const topicsBlock = document.getElementById("detailTopicsBlock");
//    const topicsEl = document.getElementById("detailTopics");
//    if (topics.length > 0) {
//        topicsBlock.classList.remove("hidden");
//        topicsEl.innerHTML = topics.map(t => `<span class="exp-card-tag">${escapeHtml(t)}</span>`).join('');
//    } else {
//        topicsBlock.classList.add("hidden");
//    }
//
//    document.getElementById("detailUpvoteCount").textContent = item.upvotes || 0;
//    document.getElementById("detailDownvoteCount").textContent = item.downvotes || 0;
//
//    document.getElementById("searchDetailModal")?.classList.remove("hidden");
//}
//
//function closeDetailModal() {
//    document.getElementById("searchDetailModal")?.classList.add("hidden");
//    currentDetailItem = null;
//}
//
//async function castVote(direction) {
//    if (!currentDetailItem) return;
//
//    // ASSUMPTION: POST /experiences/{id}/upvote and /downvote - adjust to
//    // match your actual voting endpoint if it differs.
//    try {
//        const updated = await apiRequest(`/experiences/${currentDetailItem.id}/${direction}`, { method: "POST" });
//
//        currentDetailItem.upvotes = updated?.upvotes ?? currentDetailItem.upvotes;
//        currentDetailItem.downvotes = updated?.downvotes ?? currentDetailItem.downvotes;
//
//        document.getElementById("detailUpvoteCount").textContent = currentDetailItem.upvotes || 0;
//        document.getElementById("detailDownvoteCount").textContent = currentDetailItem.downvotes || 0;
//
//        if (lastResultPage) {
//            const match = (lastResultPage.content || []).find(i => i.id === currentDetailItem.id);
//            if (match) {
//                match.upvotes = currentDetailItem.upvotes;
//                match.downvotes = currentDetailItem.downvotes;
//                renderResults(lastResultPage);
//            }
//        }
//    } catch (error) {
//        console.error("Vote Error:", error);
//        showToast("Couldn't record your vote: " + (error.message || "unknown error"), "error");
//    }
//}