/**
 * CareerSpace - Interview Experiences Module Script
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

let allExperiences = [];
let filteredExperiences = [];
let myApplications = [];
let activeDetailExp = null;
let structuredQCounter = 0;

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("loadApplicationsForPicker", loadApplicationsForPicker);
    safeInit("loadExperiences", loadExperiences);
    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initToolbar", initToolbar);
    safeInit("initExpFormModal", initExpFormModal);
    safeInit("initDetailModal", initDetailModal);
    safeInit("initDeleteModal", initDeleteModal);
});

function safeInit(name, fn) {
    try { fn(); } catch (error) { console.error(`[experiences.js] "${name}" failed to initialize:`, error); }
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
   LOAD APPLICATIONS (for "From Application" picker)
   ========================================================================== */
async function loadApplicationsForPicker() {
    const select = document.getElementById("expApplicationSelect");
    select.innerHTML = `<option value="" disabled selected>Loading applications...</option>`;

    try {
        const data = await apiRequest("/student-application/my-applications", { method: "GET" });
        myApplications = Array.isArray(data) ? data : [];

        if (myApplications.length === 0) {
            select.innerHTML = `<option value="" disabled selected>No applications found - track one first</option>`;
            return;
        }

        select.innerHTML = `<option value="" disabled selected>Select an application</option>`;
        myApplications.forEach(app => {
            const option = document.createElement("option");
            option.value = app.id;
            option.textContent = `${app.companyName} — ${app.positionTitle}`;
            option.dataset.companyId = app.companyId;
            option.dataset.positionId = app.positionId;
            option.dataset.companyName = app.companyName;
            option.dataset.positionTitle = app.positionTitle;
            select.appendChild(option);
        });

    } catch (error) {
        console.error("Failed to load applications for picker:", error);
        select.innerHTML = `<option value="" disabled selected>Failed to load applications</option>`;
        showToast("Couldn't load applications list: " + (error.message || "unknown error"), "error");
    }
}


/* ==========================================================================
   LOAD EXPERIENCES
   ========================================================================== */
async function loadExperiences() {
    showExpState("loading");
    try {
        const data = await apiRequest("/experience/my-experiences", { method: "GET" });
        allExperiences = Array.isArray(data) ? data : [];
        applyExpFiltersAndRender();
    } catch (error) {
        console.error("Experiences Error:", error);
        document.getElementById("expErrorMessage").textContent = error.message || "Something went wrong.";
        showExpState("error");
    }
}

function showExpState(state) {
    const loading = document.getElementById("expLoading");
    const empty = document.getElementById("expEmpty");
    const error = document.getElementById("expError");
    const grid = document.getElementById("expGrid");
    const noResults = document.getElementById("expNoResults");

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
        <div class="skeleton-exp-card">
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-badge"></div>
            <div class="skeleton-line skeleton-text"></div>
            <div class="skeleton-line skeleton-text-short"></div>
        </div>
    `;
    return card.repeat(count);
}


/* ==========================================================================
   TOOLBAR: SEARCH / FILTER
   ========================================================================== */
function initToolbar() {
    const searchInput = document.getElementById("expSearchInput");
    const difficultyFilter = document.getElementById("difficultyFilter");
    const resultFilter = document.getElementById("resultFilter");
    const addBtn = document.getElementById("addExperienceBtn");
    const emptyStateAddBtn = document.getElementById("emptyStateAddBtn");
    const retryBtn = document.getElementById("expRetryBtn");
    const clearFiltersBtn = document.getElementById("expClearFiltersBtn");

    let debounceTimer;
    searchInput.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(applyExpFiltersAndRender, 200);
    });

    difficultyFilter.addEventListener("change", applyExpFiltersAndRender);
    resultFilter.addEventListener("change", applyExpFiltersAndRender);

    addBtn.addEventListener("click", () => openExpFormModal());
    emptyStateAddBtn.addEventListener("click", () => openExpFormModal());
    retryBtn.addEventListener("click", loadExperiences);

    clearFiltersBtn.addEventListener("click", () => {
        searchInput.value = "";
        difficultyFilter.value = "";
        resultFilter.value = "";
        applyExpFiltersAndRender();
    });
}

function applyExpFiltersAndRender() {
    const query = document.getElementById("expSearchInput").value.trim().toLowerCase();
    const difficulty = document.getElementById("difficultyFilter").value;
    const result = document.getElementById("resultFilter").value;

    filteredExperiences = allExperiences.filter(exp => {
        const matchesQuery = !query ||
            (exp.companyName || "").toLowerCase().includes(query) ||
            (exp.positionName || "").toLowerCase().includes(query);
        const matchesDifficulty = !difficulty || exp.difficultyRating === difficulty;
        const matchesResult = !result || exp.result === result;
        return matchesQuery && matchesDifficulty && matchesResult;
    });

    filteredExperiences.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));

    if (allExperiences.length === 0) { showExpState("empty"); return; }
    if (filteredExperiences.length === 0) { showExpState("no-results"); return; }

    renderExperiencesGrid();
    showExpState("grid");
}


/* ==========================================================================
   RENDER CARDS
   ========================================================================== */
function renderExperiencesGrid() {
    const grid = document.getElementById("expGrid");

    grid.innerHTML = filteredExperiences.map(exp => {
        const diffMeta = DIFFICULTY_META[exp.difficultyRating] || { label: exp.difficultyRating, cls: "diff-medium" };
        const resultMeta = RESULT_META[exp.result] || { label: exp.result, cls: "result-pending" };

        return `
            <div class="exp-card" data-id="${exp.id}">
                <div class="exp-card-top">
                    <div>
                        <div class="exp-card-company">${escapeHtml(exp.companyName)}</div>
                        <div class="exp-card-position">${escapeHtml(exp.positionName || "")}</div>
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
            const exp = allExperiences.find(e => String(e.id) === String(id));
            if (exp) openDetailModal(exp);
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
   STRUCTURED QUESTIONS (repeatable Q+Notes rows)
   ========================================================================== */
function addStructuredQuestionRow(question = "", notes = "") {
    const list = document.getElementById("structuredQuestionsList");
    const rowId = `sq_${structuredQCounter++}`;

    const row = document.createElement("div");
    row.className = "structured-q-row";
    row.dataset.rowId = rowId;
    row.innerHTML = `
        <div class="structured-q-fields">
            <input type="text" class="form-control sq-question" placeholder="Question asked">
            <textarea class="form-control textarea-control sq-notes" rows="2" placeholder="Your answer / notes (optional)"></textarea>
        </div>
        <button type="button" class="structured-q-remove" aria-label="Remove question">&times;</button>
    `;

    row.querySelector(".sq-question").value = question;
    row.querySelector(".sq-notes").value = notes;
    row.querySelector(".structured-q-remove").addEventListener("click", () => row.remove());

    list.appendChild(row);
}

function clearStructuredQuestions() {
    document.getElementById("structuredQuestionsList").innerHTML = "";
}

function collectStructuredQuestions() {
    const rows = document.querySelectorAll("#structuredQuestionsList .structured-q-row");
    const result = [];
    rows.forEach(row => {
        const question = row.querySelector(".sq-question").value.trim();
        const notes = row.querySelector(".sq-notes").value.trim();
        if (question) result.push({ question, notes });
    });
    return result;
}


/* ==========================================================================
   ADD / EDIT FORM MODAL
   ========================================================================== */
function initExpFormModal() {
    const modal = document.getElementById("expFormModal");
    const form = document.getElementById("expForm");
    const closeBtn = document.getElementById("closeExpFormModalBtn");
    const cancelBtn = document.getElementById("cancelExpFormBtn");
    const addQBtn = document.getElementById("addQuestionRowBtn");
    const applicationSelect = document.getElementById("expApplicationSelect");

    closeBtn.addEventListener("click", closeExpFormModal);
    cancelBtn.addEventListener("click", closeExpFormModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeExpFormModal(); });

    addQBtn.addEventListener("click", () => addStructuredQuestionRow());

    applicationSelect.addEventListener("change", () => {
        const selected = applicationSelect.selectedOptions[0];
        if (selected) {
            document.getElementById("expCompanyId").value = selected.dataset.companyId || "";
            document.getElementById("expPositionId").value = selected.dataset.positionId || "";
        }
    });

    form.addEventListener("submit", handleExpFormSubmit);
}

function openExpFormModal(experience = null) {
    document.getElementById("expForm").reset();
    clearExpFormErrors();
    clearStructuredQuestions();

    const applicationSelect = document.getElementById("expApplicationSelect");

    document.getElementById("expFormTitle").textContent = experience ? "Edit Interview Experience" : "Log Interview Experience";
    document.getElementById("expId").value = experience ? experience.id : "";

    if (experience) {
        document.getElementById("expCompanyId").value = experience.companyId;
        document.getElementById("expPositionId").value = experience.positionId;

        // Lock application selection during edit - backend doesn't act on
        // changes to company/position on update, only round/details change
        const matchingOption = [...applicationSelect.options].find(
            opt => opt.dataset.companyId === experience.companyId && opt.dataset.positionId === experience.positionId
        );
        if (matchingOption) {
            applicationSelect.value = matchingOption.value;
        }
        applicationSelect.disabled = true;

        document.getElementById("expRoundName").value = experience.interviewRoundName || "";
        document.getElementById("expDateExperienced").value = epochToDateInputValue(experience.dateExperienced);
        document.getElementById("expDifficulty").value = experience.difficultyRating || "";
        document.getElementById("expDuration").value = experience.durationMinutes ?? "";
        document.getElementById("expTotalProblems").value = experience.totalProblemsAsked ?? "";
        document.getElementById("expQuestionsAsked").value = experience.questionsAsked || "";
        document.getElementById("expTopics").value = parseTopicsToCsv(experience.topics);
        document.getElementById("expSummary").value = experience.experienceSummary || "";
        document.getElementById("expHelpfulResources").value = experience.helpfulResources || "";
        document.getElementById("expInterviewerFeedback").value = experience.interviewerFeedback || "";
        document.getElementById("expResult").value = experience.result || "";
        document.getElementById("expResultDate").value = epochToDateInputValue(experience.resultReceivedDate);
        document.getElementById("expIsPublic").checked = experience.isPublic !== false;

        const structuredQuestions = parseQuestionsJson(experience.questionsJson);
        structuredQuestions.forEach(q => addStructuredQuestionRow(q.question, q.notes));

    } else {
        applicationSelect.disabled = false;
        document.getElementById("expIsPublic").checked = true;
    }

    document.getElementById("expFormModal").classList.remove("hidden");
}

function closeExpFormModal() {
    document.getElementById("expFormModal").classList.add("hidden");
    document.getElementById("expApplicationSelect").disabled = false;
}

function clearExpFormErrors() {
    document.querySelectorAll("#expForm .field-error").forEach(el => el.textContent = "");
    document.querySelectorAll("#expForm .is-invalid").forEach(el => el.classList.remove("is-invalid"));
}

function parseTopicsToCsv(topicsJsonNode) {
    if (!topicsJsonNode) return "";
    if (Array.isArray(topicsJsonNode)) return topicsJsonNode.join(", ");
    return "";
}

function parseQuestionsJson(questionsJsonNode) {
    if (!questionsJsonNode) return [];
    if (Array.isArray(questionsJsonNode)) return questionsJsonNode;
    return [];
}

function validateExpForm() {
    clearExpFormErrors();
    let isValid = true;

    const fields = [
        ["expApplicationSelect", "expApplicationError", "Please select an application."],
        ["expRoundName", "expRoundNameError", "Round name is required."],
        ["expRoundNumber", "expRoundNumberError", "Round number is required."],
        ["expDateExperienced", "expDateError", "Date experienced is required."],
        ["expDifficulty", "expDifficultyError", "Please select a difficulty."],
        ["expQuestionsAsked", "expQuestionsAskedError", "Questions asked summary is required."],
        ["expSummary", "expSummaryError", "Experience summary is required."],
        ["expResult", "expResultError", "Please select a result."]
    ];

    fields.forEach(([fieldId, errorId, message]) => {
        const el = document.getElementById(fieldId);
        if (!el.value || !el.value.trim || el.value.trim() === "") {
            if (el.value === "" || el.value === null) {
                el.classList.add("is-invalid");
                document.getElementById(errorId).textContent = message;
                isValid = false;
            }
        }
    });

    // Company/position come from the application select's dataset, not its
    // value directly - re-check they landed in the hidden fields
    if (!document.getElementById("expCompanyId").value || !document.getElementById("expPositionId").value) {
        document.getElementById("expApplicationSelect").classList.add("is-invalid");
        document.getElementById("expApplicationError").textContent = "Please select an application.";
        isValid = false;
    }

    return isValid;
}

async function handleExpFormSubmit(e) {
    e.preventDefault();
    if (!validateExpForm()) return;

    const id = document.getElementById("expId").value;
    const submitBtn = document.getElementById("submitExpBtn");
    const btnText = submitBtn.querySelector(".btn-text");
    const spinner = document.getElementById("expFormSpinner");

    const structuredQuestions = collectStructuredQuestions();
    const topicsCsv = document.getElementById("expTopics").value.trim();

    const payload = {
        companyId: document.getElementById("expCompanyId").value,
        positionId: document.getElementById("expPositionId").value,
        roundName: document.getElementById("expRoundName").value.trim(),
        roundNumber: parseInt(document.getElementById("expRoundNumber").value, 10),
        dateExperienced: dateInputValueToEpoch(document.getElementById("expDateExperienced").value),
        difficultyRating: document.getElementById("expDifficulty").value,
        durationMinutes: document.getElementById("expDuration").value ? parseInt(document.getElementById("expDuration").value, 10) : null,
        totalProblemsAsked: document.getElementById("expTotalProblems").value ? parseInt(document.getElementById("expTotalProblems").value, 10) : null,
        questionsAsked: document.getElementById("expQuestionsAsked").value.trim(),
        questionsJson: structuredQuestions.length > 0 ? JSON.stringify(structuredQuestions) : null,
        topics: topicsCsv || null,
        experienceSummary: document.getElementById("expSummary").value.trim(),
        helpfulResources: document.getElementById("expHelpfulResources").value.trim() || null,
        interviewerFeedback: document.getElementById("expInterviewerFeedback").value.trim() || null,
        result: document.getElementById("expResult").value,
        resultReceivedDate: dateInputValueToEpoch(document.getElementById("expResultDate").value),
        isPublic: document.getElementById("expIsPublic").checked
    };

    submitBtn.disabled = true;
    btnText.textContent = id ? "Saving..." : "Logging...";
    spinner.classList.remove("hidden");

    try {
        if (id) {
            await apiRequest(`/experience/${id}`, { method: "PUT", body: JSON.stringify(payload) });
            showToast("Experience updated successfully.", "success");
        } else {
            await apiRequest("/experience", { method: "POST", body: JSON.stringify(payload) });
            showToast("Experience logged successfully.", "success");
        }
        closeExpFormModal();
        await loadExperiences();
    } catch (error) {
        showToast(error.message || "Failed to save experience.", "error");
    } finally {
        submitBtn.disabled = false;
        btnText.textContent = "Save Experience";
        spinner.classList.add("hidden");
    }
}


/* ==========================================================================
   DETAIL MODAL
   ========================================================================== */
function initDetailModal() {
    const modal = document.getElementById("expDetailModal");
    const closeBtn = document.getElementById("closeExpDetailModalBtn");
    const editBtn = document.getElementById("editExpBtn");
    const upvoteBtn = document.getElementById("detailUpvoteBtn");
    const downvoteBtn = document.getElementById("detailDownvoteBtn");
    const deleteBtn = document.getElementById("deleteExpBtn");

    closeBtn.addEventListener("click", closeDetailModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeDetailModal(); });

    editBtn.addEventListener("click", () => {
        closeDetailModal();
        openExpFormModal(activeDetailExp);
    });

    deleteBtn.addEventListener("click", () => {
        closeDetailModal();
        openDeleteExpModal(activeDetailExp);
    });

    upvoteBtn.addEventListener("click", () => handleVote("upvote"));
    downvoteBtn.addEventListener("click", () => handleVote("downvote"));
}

function openDetailModal(exp) {
    activeDetailExp = exp;
    const diffMeta = DIFFICULTY_META[exp.difficultyRating] || { label: exp.difficultyRating, cls: "diff-medium" };
    const resultMeta = RESULT_META[exp.result] || { label: exp.result, cls: "result-pending" };

    document.getElementById("detailExpCompany").textContent = exp.companyName || "Experience";
    document.getElementById("detailExpPosition").textContent = exp.positionName || "";
    document.getElementById("detailExpRound").textContent = formatRoundLabel(exp);

    const diffPill = document.getElementById("detailExpDifficulty");
    diffPill.className = `difficulty-pill ${diffMeta.cls}`;
    diffPill.textContent = diffMeta.label;

    const resultPill = document.getElementById("detailExpResult");
    resultPill.className = `result-pill ${resultMeta.cls}`;
    resultPill.textContent = resultMeta.label;

    document.getElementById("detailExpDate").textContent = formatDate(exp.dateExperienced);
    document.getElementById("detailExpDuration").textContent = exp.durationMinutes ? `${exp.durationMinutes} min` : "—";
    document.getElementById("detailExpProblems").textContent = exp.totalProblemsAsked ?? "—";
    document.getElementById("detailExpVisibility").textContent = exp.isPublic ? "Public" : "Private";

    document.getElementById("detailExpQuestionsAsked").textContent = exp.questionsAsked || "—";
    document.getElementById("detailExpSummary").textContent = exp.experienceSummary || "—";

    const resources = exp.helpfulResources;
    document.getElementById("detailResourcesBlock").classList.toggle("hidden", !resources);
    document.getElementById("detailExpResources").textContent = resources || "";

    const feedback = exp.interviewerFeedback;
    document.getElementById("detailFeedbackBlock").classList.toggle("hidden", !feedback);
    document.getElementById("detailExpFeedback").textContent = feedback || "";

    const structuredQuestions = parseQuestionsJson(exp.questionsJson);
    const sqBlock = document.getElementById("detailStructuredQBlock");
    const sqList = document.getElementById("detailStructuredQList");
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
    const topicsBlock = document.getElementById("detailTopicsBlock");
    const topicsContainer = document.getElementById("detailExpTopics");
    if (topics.length > 0) {
        topicsBlock.classList.remove("hidden");
        topicsContainer.innerHTML = topics.map(t => `<span class="resume-company-tag">${escapeHtml(t)}</span>`).join('');
    } else {
        topicsBlock.classList.add("hidden");
    }

    document.getElementById("detailUpvoteCount").textContent = exp.upvotes || 0;
    document.getElementById("detailDownvoteCount").textContent = exp.downvotes || 0;

    document.getElementById("expDetailModal").classList.remove("hidden");
}

function closeDetailModal() {
    document.getElementById("expDetailModal").classList.add("hidden");
}

async function handleVote(direction) {
    if (!activeDetailExp) return;

    try {
        const updated = await apiRequest(`/experience/${direction}/${activeDetailExp.id}`, { method: "POST" });
        activeDetailExp = updated;

        document.getElementById("detailUpvoteCount").textContent = updated.upvotes || 0;
        document.getElementById("detailDownvoteCount").textContent = updated.downvotes || 0;

        const idx = allExperiences.findIndex(e => e.id === updated.id);
        if (idx !== -1) allExperiences[idx] = updated;
        applyExpFiltersAndRender();

    } catch (error) {
        showToast(error.message || "Failed to register vote.", "error");
    }
}


/* ==========================================================================
   DELETE CONFIRM MODAL
   ========================================================================== */
let pendingDeleteExpId = null;

function initDeleteModal() {
    const modal = document.getElementById("deleteExpModal");
    const cancelBtn = document.getElementById("cancelDeleteExpBtn");
    const confirmBtn = document.getElementById("confirmDeleteExpBtn");

    cancelBtn.addEventListener("click", closeDeleteExpModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeDeleteExpModal(); });
    confirmBtn.addEventListener("click", handleConfirmDeleteExp);
}

function openDeleteExpModal(exp) {
    pendingDeleteExpId = exp.id;
    document.getElementById("deleteExpModal").classList.remove("hidden");
}

function closeDeleteExpModal() {
    pendingDeleteExpId = null;
    document.getElementById("deleteExpModal").classList.add("hidden");
}

async function handleConfirmDeleteExp() {
    if (!pendingDeleteExpId) return;

    const confirmBtn = document.getElementById("confirmDeleteExpBtn");
    const btnText = confirmBtn.querySelector(".btn-text");
    const spinner = document.getElementById("deleteExpSpinner");

    confirmBtn.disabled = true;
    btnText.textContent = "Deleting...";
    spinner.classList.remove("hidden");

    try {
        await apiRequest(`/experience/${pendingDeleteExpId}`, { method: "DELETE" });
        showToast("Experience deleted.", "success");
        closeDeleteExpModal();
        await loadExperiences();
    } catch (error) {
        showToast(error.message || "Failed to delete experience.", "error");
    } finally {
        confirmBtn.disabled = false;
        btnText.textContent = "Delete";
        spinner.classList.add("hidden");
    }
}