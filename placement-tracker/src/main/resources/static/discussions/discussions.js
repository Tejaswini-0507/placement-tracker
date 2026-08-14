/**
 * CareerSpace - Discussions Module Script
 * Pure Vanilla JavaScript (ES6+)
 * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
 *
 * NOTE ON LIKES: the backend's like/unlike endpoints are plain counters with
 * no per-user "did I already like this" tracking, so there's no true toggle
 * state to read back from the server. The like button just increments via
 * POST /messages/{id}/like each click; a small "undo" link calls unlike to
 * correct mistakes. This is a backend limitation, not a frontend bug.
 */

const MY_STUDENT_ID = localStorage.getItem("studentId");
const MESSAGE_REFRESH_INTERVAL_MS = 5000;

let allCompaniesForThreads = [];
let currentCompanyThreads = [];
let currentMyThreads = [];
let selectedCompany = null;
let activeThread = null;
let currentMessages = [];
let messageRefreshTimer = null;
let currentView = "browse";
let pendingCreateFromMyThreads = false;

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("loadCompaniesForThreads", loadCompaniesForThreads);
    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initTabs", initTabs);
    safeInit("initCompanyPicker", initCompanyPicker);
    safeInit("initThreadFormModal", initThreadFormModal);
    safeInit("initThreadDetailModal", initThreadDetailModal);
    safeInit("initDeleteThreadModal", initDeleteThreadModal);
});

function safeInit(name, fn) {
    try { fn(); } catch (error) { console.error(`[discussions.js] "${name}" failed to initialize:`, error); }
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
   TABS
   ========================================================================== */
function initTabs() {
    document.getElementById("tabBrowse").addEventListener("click", () => switchView("browse"));
    document.getElementById("tabMine").addEventListener("click", () => switchView("mine"));

    document.getElementById("newThreadBtn").addEventListener("click", () => {
        pendingCreateFromMyThreads = (currentView === "mine") || !selectedCompany;
        openThreadFormModal();
    });
    document.getElementById("myThreadsNewThreadBtn").addEventListener("click", () => {
        pendingCreateFromMyThreads = true;
        openThreadFormModal();
    });
}

function switchView(view) {
    currentView = view;
    document.getElementById("tabBrowse").classList.toggle("active", view === "browse");
    document.getElementById("tabMine").classList.toggle("active", view === "mine");

    document.getElementById("companyPickerSection").classList.toggle("hidden", view !== "browse" || !!selectedCompany);
    document.getElementById("companyThreadsSection").classList.toggle("hidden", view !== "browse" || !selectedCompany);
    document.getElementById("myThreadsSection").classList.toggle("hidden", view !== "mine");

    if (view === "mine" && currentMyThreads.length === 0) {
        loadMyThreads();
    }
}


/* ==========================================================================
   COMPANY PICKER (also feeds the thread-form company dropdown)
   ========================================================================== */
async function loadCompaniesForThreads() {
    showPickerState("loading");
    try {
        const data = await apiRequest("/companies", { method: "GET" });
        allCompaniesForThreads = Array.isArray(data) ? data : [];
        renderCompanyPicker(allCompaniesForThreads);
        populateThreadCompanySelect();
    } catch (error) {
        console.error("Companies Error:", error);
        document.getElementById("companyPickerErrorMessage").textContent = error.message || "Something went wrong.";
        showPickerState("error");
    }
}

function populateThreadCompanySelect() {
    const select = document.getElementById("threadCompanySelect");
    select.innerHTML = `<option value="" disabled selected>Select a company</option>`;
    allCompaniesForThreads
        .slice()
        .sort((a, b) => (a.name || "").localeCompare(b.name || ""))
        .forEach(company => {
            const option = document.createElement("option");
            option.value = company.id;
            option.textContent = company.name;
            select.appendChild(option);
        });
}

function showPickerState(state) {
    const loading = document.getElementById("companyPickerLoading");
    const empty = document.getElementById("companyPickerEmpty");
    const error = document.getElementById("companyPickerError");
    const grid = document.getElementById("companyPickerGrid");
    const noResults = document.getElementById("companyPickerNoResults");

    [loading, empty, error, grid, noResults].forEach(el => el.classList.add("hidden"));

    if (state === "loading") { loading.innerHTML = renderPickerSkeletons(6); loading.classList.remove("hidden"); }
    else if (state === "empty") empty.classList.remove("hidden");
    else if (state === "error") error.classList.remove("hidden");
    else if (state === "grid") grid.classList.remove("hidden");
    else if (state === "no-results") noResults.classList.remove("hidden");
}

function renderPickerSkeletons(count) {
    const card = `<div class="skeleton-picker-card"><div class="skeleton-line skeleton-picker-logo"></div><div class="skeleton-line skeleton-picker-text"></div></div>`;
    return card.repeat(count);
}

function renderCompanyPicker(companies) {
    if (allCompaniesForThreads.length === 0) { showPickerState("empty"); return; }
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
            const company = allCompaniesForThreads.find(c => String(c.id) === String(id));
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
            renderCompanyPicker(allCompaniesForThreads.filter(c => (c.name || "").toLowerCase().includes(query)));
        }, 200);
    });

    changeCompanyBtn.addEventListener("click", () => {
        selectedCompany = null;
        document.getElementById("companyThreadsSection").classList.add("hidden");
        document.getElementById("companyPickerSection").classList.remove("hidden");
    });

    retryBtn.addEventListener("click", loadCompaniesForThreads);
}

function selectCompany(company) {
    selectedCompany = company;
    document.getElementById("selectedCompanyName").textContent = company.name;
    document.getElementById("companyPickerSection").classList.add("hidden");
    document.getElementById("companyThreadsSection").classList.remove("hidden");
    loadCompanyThreads(company.id);
}


/* ==========================================================================
   THREADS FOR SELECTED COMPANY
   ========================================================================== */
async function loadCompanyThreads(companyId) {
    showThreadsState("loading", "threads");
    try {
        const data = await apiRequest(`/discussions/company/${companyId}`, { method: "GET" });
        currentCompanyThreads = Array.isArray(data) ? data : [];
        sortThreadsPinnedFirst(currentCompanyThreads);

        if (currentCompanyThreads.length === 0) {
            showThreadsState("empty", "threads");
        } else {
            renderThreadsList(currentCompanyThreads, "threadsGrid", false);
            showThreadsState("grid", "threads");
        }
    } catch (error) {
        console.error("Threads Error:", error);
        document.getElementById("threadsErrorMessage").textContent = error.message || "Something went wrong.";
        showThreadsState("error", "threads");
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById("threadsRetryBtn")?.addEventListener("click", () => {
        if (selectedCompany) loadCompanyThreads(selectedCompany.id);
    });
    document.getElementById("emptyStateNewThreadBtn")?.addEventListener("click", () => {
        pendingCreateFromMyThreads = false;
        openThreadFormModal();
    });
});

function sortThreadsPinnedFirst(threads) {
    threads.sort((a, b) => {
        if (!!b.pinned !== !!a.pinned) return (b.pinned ? 1 : 0) - (a.pinned ? 1 : 0);
        return (b.lastActivity || 0) - (a.lastActivity || 0);
    });
}


/* ==========================================================================
   MY THREADS
   ========================================================================== */
async function loadMyThreads() {
    showThreadsState("loading", "myThreads");
    try {
        const data = await apiRequest("/discussions/my-threads", { method: "GET" });
        currentMyThreads = Array.isArray(data) ? data : [];
        sortThreadsPinnedFirst(currentMyThreads);

        if (currentMyThreads.length === 0) {
            showThreadsState("empty", "myThreads");
        } else {
            renderThreadsList(currentMyThreads, "myThreadsGrid", true);
            showThreadsState("grid", "myThreads");
        }
    } catch (error) {
        console.error("My Threads Error:", error);
        document.getElementById("myThreadsErrorMessage").textContent = error.message || "Something went wrong.";
        showThreadsState("error", "myThreads");
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById("myThreadsRetryBtn")?.addEventListener("click", loadMyThreads);
});

function showThreadsState(state, prefix) {
    const loading = document.getElementById(`${prefix}Loading`);
    const empty = document.getElementById(`${prefix}Empty`);
    const error = document.getElementById(`${prefix}Error`);
    const grid = document.getElementById(`${prefix}Grid`);

    [loading, empty, error, grid].forEach(el => el.classList.add("hidden"));

    if (state === "loading") { loading.innerHTML = renderThreadSkeletons(3); loading.classList.remove("hidden"); }
    else if (state === "empty") empty.classList.remove("hidden");
    else if (state === "error") error.classList.remove("hidden");
    else if (state === "grid") grid.classList.remove("hidden");
}

function renderThreadSkeletons(count) {
    const card = `<div class="skeleton-thread-card"><div class="skeleton-line" style="height:14px;width:60%;"></div></div>`;
    return card.repeat(count);
}


/* ==========================================================================
   RENDER THREAD CARDS
   ========================================================================== */
function renderThreadsList(threads, containerId, showCompanyTag) {
    const container = document.getElementById(containerId);

    container.innerHTML = threads.map(thread => `
        <div class="thread-card" data-id="${thread.id}">
            <div class="thread-card-top">
                <div class="thread-card-title-row">
                    ${thread.pinned ? '<span class="thread-pin-icon">📌</span>' : ''}
                    <span class="thread-card-title">${escapeHtml(thread.title)}</span>
                </div>
            </div>
            ${showCompanyTag ? `<div class="thread-card-company">${escapeHtml(thread.companyName)}</div>` : ''}
            <p class="thread-card-desc">${escapeHtml(thread.description || "")}</p>
            <div class="thread-card-tags">
                ${thread.interviewRound ? `<span class="thread-tag">${escapeHtml(thread.interviewRound)}</span>` : ''}
                ${thread.topic ? `<span class="thread-tag">${escapeHtml(thread.topic)}</span>` : ''}
            </div>
            <div class="thread-card-footer">
                <span>💬 ${thread.messageCount || 0} messages</span>
                <span>By ${escapeHtml(thread.createdByStudentName || "someone")} · ${formatRelativeTime(thread.lastActivity)}</span>
            </div>
        </div>
    `).join('');

    container.querySelectorAll('.thread-card').forEach(card => {
        card.addEventListener('click', () => {
            const id = card.getAttribute('data-id');
            const thread = threads.find(t => String(t.id) === String(id));
            if (thread) openThreadDetailModal(thread);
        });
    });
}

function formatDate(epochMillis) {
    if (!epochMillis) return "—";
    return new Date(epochMillis).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatTime(epochMillis) {
    if (!epochMillis) return "";
    return new Date(epochMillis).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
}

function formatRelativeTime(epochMillis) {
    if (!epochMillis) return "—";
    const diffMs = Date.now() - epochMillis;
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return "just now";
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffHr = Math.floor(diffMin / 60);
    if (diffHr < 24) return `${diffHr}h ago`;
    const diffDay = Math.floor(diffHr / 24);
    if (diffDay < 7) return `${diffDay}d ago`;
    return formatDate(epochMillis);
}


/* ==========================================================================
   CREATE / EDIT THREAD MODAL
   ========================================================================== */
function initThreadFormModal() {
    const modal = document.getElementById("threadFormModal");
    const form = document.getElementById("threadForm");
    const closeBtn = document.getElementById("closeThreadFormModalBtn");
    const cancelBtn = document.getElementById("cancelThreadFormBtn");

    closeBtn.addEventListener("click", closeThreadFormModal);
    cancelBtn.addEventListener("click", closeThreadFormModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeThreadFormModal(); });

    form.addEventListener("submit", handleThreadFormSubmit);
}

function openThreadFormModal(thread = null) {
    document.getElementById("threadForm").reset();
    clearThreadFormErrors();

    document.getElementById("threadFormTitle").textContent = thread ? "Edit Thread" : "New Thread";
    document.getElementById("threadId").value = thread ? thread.id : "";

    const companySelect = document.getElementById("threadCompanySelect");
    const companyGroup = document.getElementById("threadCompanyGroup");

    if (thread) {
        // Editing: lock company (backend doesn't support changing it meaningfully)
        companySelect.value = thread.companyId;
        companySelect.disabled = true;
        companyGroup.style.opacity = "0.7";

        document.getElementById("threadRound").value = thread.interviewRound || "";
        document.getElementById("threadTopic").value = thread.topic || "";
        document.getElementById("threadTitle").value = thread.title || "";
        document.getElementById("threadDescription").value = thread.description || "";

    } else if (!pendingCreateFromMyThreads && selectedCompany) {
        // Creating from within a company's thread list: prefill + lock
        companySelect.value = selectedCompany.id;
        companySelect.disabled = true;
        companyGroup.style.opacity = "0.7";
    } else {
        companySelect.disabled = false;
        companyGroup.style.opacity = "1";
    }

    document.getElementById("threadFormModal").classList.remove("hidden");
}

function closeThreadFormModal() {
    document.getElementById("threadFormModal").classList.add("hidden");
    document.getElementById("threadCompanySelect").disabled = false;
    document.getElementById("threadCompanyGroup").style.opacity = "1";
}

function clearThreadFormErrors() {
    document.querySelectorAll("#threadForm .field-error").forEach(el => el.textContent = "");
    document.querySelectorAll("#threadForm .is-invalid").forEach(el => el.classList.remove("is-invalid"));
}

function validateThreadForm() {
    clearThreadFormErrors();
    let isValid = true;

    const companyId = document.getElementById("threadCompanySelect").value;
    const title = document.getElementById("threadTitle").value.trim();
    const description = document.getElementById("threadDescription").value.trim();

    if (!companyId) {
        document.getElementById("threadCompanySelect").classList.add("is-invalid");
        document.getElementById("threadCompanyError").textContent = "Please select a company.";
        isValid = false;
    }
    if (!title) {
        document.getElementById("threadTitle").classList.add("is-invalid");
        document.getElementById("threadTitleError").textContent = "Title is required.";
        isValid = false;
    }
    if (!description) {
        document.getElementById("threadDescription").classList.add("is-invalid");
        document.getElementById("threadDescriptionError").textContent = "Description is required.";
        isValid = false;
    }

    return isValid;
}

async function handleThreadFormSubmit(e) {
    e.preventDefault();
    if (!validateThreadForm()) return;

    const id = document.getElementById("threadId").value;
    const submitBtn = document.getElementById("submitThreadBtn");
    const btnText = submitBtn.querySelector(".btn-text");
    const spinner = document.getElementById("threadFormSpinner");

    const payload = {
        companyId: document.getElementById("threadCompanySelect").value,
        studentId: MY_STUDENT_ID,
        interviewRound: document.getElementById("threadRound").value.trim() || null,
        topic: document.getElementById("threadTopic").value.trim() || null,
        title: document.getElementById("threadTitle").value.trim(),
        description: document.getElementById("threadDescription").value.trim()
    };

    submitBtn.disabled = true;
    btnText.textContent = id ? "Saving..." : "Creating...";
    spinner.classList.remove("hidden");

    try {
        if (id) {
            await apiRequest(`/discussions/threads/${id}`, { method: "PUT", body: JSON.stringify(payload) });
            showToast("Thread updated.", "success");
        } else {
            await apiRequest("/discussions/threads", { method: "POST", body: JSON.stringify(payload) });
            showToast("Thread created.", "success");
        }

        closeThreadFormModal();

        if (selectedCompany) await loadCompanyThreads(selectedCompany.id);
        if (currentView === "mine" || pendingCreateFromMyThreads) await loadMyThreads();

    } catch (error) {
        showToast(error.message || "Failed to save thread.", "error");
    } finally {
        submitBtn.disabled = false;
        btnText.textContent = "Create Thread";
        spinner.classList.add("hidden");
    }
}


/* ==========================================================================
   THREAD DETAIL MODAL (CHAT)
   ========================================================================== */
function initThreadDetailModal() {
    const modal = document.getElementById("threadDetailModal");
    const closeBtn = document.getElementById("closeThreadDetailModalBtn");
    const pinBtn = document.getElementById("pinThreadBtn");
    const editBtn = document.getElementById("editThreadBtn");
    const deleteBtn = document.getElementById("deleteThreadBtn");
    const composeForm = document.getElementById("chatComposeForm");

    closeBtn.addEventListener("click", closeThreadDetailModal);
    modal.addEventListener("click", (e) => { if (e.target === modal) closeThreadDetailModal(); });

    pinBtn.addEventListener("click", handleTogglePin);
    editBtn.addEventListener("click", () => {
        closeThreadDetailModal();
        pendingCreateFromMyThreads = false;
        openThreadFormModal(activeThread);
    });
    deleteBtn.addEventListener("click", () => {
        document.getElementById("deleteThreadModal").classList.remove("hidden");
    });

    composeForm.addEventListener("submit", handleSendMessage);
}

function openThreadDetailModal(thread) {
    activeThread = thread;

    document.getElementById("detailThreadTitle").textContent = thread.title;
    const metaParts = [thread.companyName];
    if (thread.interviewRound) metaParts.push(thread.interviewRound);
    if (thread.topic) metaParts.push(thread.topic);
    document.getElementById("detailThreadMeta").textContent = metaParts.join(" · ");
    document.getElementById("detailThreadDescription").textContent = thread.description || "";

    updatePinButton();

    const isOwner = String(thread.createdByStudentId) === String(MY_STUDENT_ID);
    document.getElementById("editThreadBtn").classList.toggle("hidden", !isOwner);
    document.getElementById("deleteThreadBtn").classList.toggle("hidden", !isOwner);

    document.getElementById("threadDetailModal").classList.remove("hidden");

    loadMessages(thread.id);
    messageRefreshTimer = setInterval(() => loadMessages(thread.id, true), MESSAGE_REFRESH_INTERVAL_MS);
}

function closeThreadDetailModal() {
    document.getElementById("threadDetailModal").classList.add("hidden");
    if (messageRefreshTimer) {
        clearInterval(messageRefreshTimer);
        messageRefreshTimer = null;
    }
    activeThread = null;
    currentMessages = [];
}

function updatePinButton() {
    const pinBtn = document.getElementById("pinThreadBtn");
    pinBtn.classList.toggle("active", !!activeThread.pinned);
    pinBtn.title = activeThread.pinned ? "Unpin thread" : "Pin thread";
}

async function handleTogglePin() {
    if (!activeThread) return;
    const action = activeThread.pinned ? "unpin" : "pin";
    try {
        const updated = await apiRequest(`/discussions/threads/${activeThread.id}/${action}`, { method: "POST" });
        activeThread = updated;
        updatePinButton();
        updateThreadInLists(updated);
        showToast(updated.pinned ? "Thread pinned." : "Thread unpinned.", "success");
    } catch (error) {
        showToast(error.message || "Failed to update pin status.", "error");
    }
}

function updateThreadInLists(updated) {
    [currentCompanyThreads, currentMyThreads].forEach(list => {
        const idx = list.findIndex(t => t.id === updated.id);
        if (idx !== -1) list[idx] = { ...list[idx], ...updated };
    });
    if (selectedCompany && currentView === "browse") {
        sortThreadsPinnedFirst(currentCompanyThreads);
        renderThreadsList(currentCompanyThreads, "threadsGrid", false);
    }
    if (currentMyThreads.length > 0) {
        sortThreadsPinnedFirst(currentMyThreads);
        renderThreadsList(currentMyThreads, "myThreadsGrid", true);
    }
}


/* ==========================================================================
   MESSAGES (re-fetch full list every 5s instead of the buggy shared-state
   poll endpoint - see file header note)
   ========================================================================== */
async function loadMessages(threadId, isBackgroundRefresh = false) {
    try {
        const data = await apiRequest(`/messages/thread/${threadId}`, { method: "GET" });
        currentMessages = Array.isArray(data) ? data : [];
        renderMessages();
    } catch (error) {
        if (!isBackgroundRefresh) {
            document.getElementById("chatMessages").innerHTML = `<div class="chat-loading">Couldn't load messages: ${escapeHtml(error.message || "unknown error")}</div>`;
        }
        console.error("Messages Error:", error);
    }
}

function renderMessages() {
    const container = document.getElementById("chatMessages");
    const wasNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 80;

    if (currentMessages.length === 0) {
        container.innerHTML = `<div class="chat-loading">No messages yet. Start the conversation!</div>`;
        return;
    }

    container.innerHTML = currentMessages.map(msg => {
        const isOwn = String(msg.studentId) === String(MY_STUDENT_ID);
        return `
            <div class="chat-message ${isOwn ? 'own' : ''}" data-id="${msg.id}">
                <div class="chat-message-meta">
                    <span class="chat-message-author">${escapeHtml(msg.studentName)}</span>
                    <span>${formatTime(msg.createdAt)}</span>
                    ${msg.isEdited ? '<span>(edited)</span>' : ''}
                </div>
                <div class="chat-message-bubble" id="bubble-${msg.id}">${escapeHtml(msg.message)}</div>
                <div class="chat-message-actions">
                    <span class="chat-like-btn" data-action="like" data-id="${msg.id}">👍 <span class="like-count">${msg.likes || 0}</span></span>
                    <span class="chat-msg-action-link" data-action="unlike" data-id="${msg.id}">undo like</span>
                    ${isOwn ? `<span class="chat-msg-action-link" data-action="edit" data-id="${msg.id}">Edit</span>` : ''}
                    ${isOwn ? `<span class="chat-msg-action-link" data-action="delete" data-id="${msg.id}">Delete</span>` : ''}
                </div>
            </div>
        `;
    }).join('');

    container.querySelectorAll('[data-action="like"]').forEach(el => el.addEventListener('click', () => handleLikeMessage(el.dataset.id, "like")));
    container.querySelectorAll('[data-action="unlike"]').forEach(el => el.addEventListener('click', () => handleLikeMessage(el.dataset.id, "unlike")));
    container.querySelectorAll('[data-action="edit"]').forEach(el => el.addEventListener('click', () => startEditMessage(el.dataset.id)));
    container.querySelectorAll('[data-action="delete"]').forEach(el => el.addEventListener('click', (e) => handleDeleteMessage(el.dataset.id, e.target)));

    if (wasNearBottom) container.scrollTop = container.scrollHeight;
}

async function handleSendMessage(e) {
    e.preventDefault();
    const input = document.getElementById("chatComposeInput");
    const text = input.value.trim();
    if (!text || !activeThread) return;

    const sendBtn = document.getElementById("chatSendBtn");
    sendBtn.disabled = true;

    try {
        await apiRequest("/messages", {
            method: "POST",
            body: JSON.stringify({ threadId: activeThread.id, message: text })
        });
        input.value = "";
        await loadMessages(activeThread.id);

        activeThread.messageCount = (activeThread.messageCount || 0) + 1;
        updateThreadInLists(activeThread);

    } catch (error) {
        showToast(error.message || "Failed to send message.", "error");
    } finally {
        sendBtn.disabled = false;
    }
}

async function handleLikeMessage(messageId, direction) {
    try {
        const updated = await apiRequest(`/messages/${messageId}/${direction}`, { method: "POST" });
        const idx = currentMessages.findIndex(m => m.id === updated.id);
        if (idx !== -1) currentMessages[idx] = updated;
        renderMessages();
    } catch (error) {
        showToast(error.message || "Failed to update like.", "error");
    }
}

function startEditMessage(messageId) {
    const msg = currentMessages.find(m => m.id === messageId);
    if (!msg) return;

    const bubble = document.getElementById(`bubble-${messageId}`);
    bubble.innerHTML = `
        <textarea class="form-control textarea-control edit-msg-textarea" rows="2">${escapeHtml(msg.message)}</textarea>
        <div style="display:flex; gap:8px; margin-top:8px;">
            <button type="button" class="btn btn-primary btn-sm" data-save="${messageId}">Save</button>
            <button type="button" class="btn btn-secondary btn-sm" data-cancel="${messageId}">Cancel</button>
        </div>
    `;

    bubble.querySelector('[data-save]').addEventListener('click', () => saveEditedMessage(messageId));
    bubble.querySelector('[data-cancel]').addEventListener('click', renderMessages);
}

async function saveEditedMessage(messageId) {
    const bubble = document.getElementById(`bubble-${messageId}`);
    const newText = bubble.querySelector('.edit-msg-textarea').value.trim();
    if (!newText) return;

    try {
        const updated = await apiRequest(`/messages/${messageId}`, {
            method: "PUT",
            body: JSON.stringify({ threadId: activeThread.id, message: newText })
        });
        const idx = currentMessages.findIndex(m => m.id === updated.id);
        if (idx !== -1) currentMessages[idx] = updated;
        renderMessages();
        showToast("Message updated.", "success");
    } catch (error) {
        showToast(error.message || "Failed to update message.", "error");
    }
}

function handleDeleteMessage(messageId, linkEl) {
    if (linkEl.dataset.confirming === "true") {
        deleteMessage(messageId);
        return;
    }
    linkEl.dataset.confirming = "true";
    const originalText = linkEl.textContent;
    linkEl.textContent = "Confirm?";
    linkEl.style.color = "#DC2626";
    setTimeout(() => {
        if (linkEl.dataset.confirming === "true") {
            linkEl.dataset.confirming = "false";
            linkEl.textContent = originalText;
            linkEl.style.color = "";
        }
    }, 3000);
}

async function deleteMessage(messageId) {
    try {
        await apiRequest(`/messages/${messageId}`, { method: "DELETE" });
        currentMessages = currentMessages.filter(m => m.id !== messageId);
        renderMessages();

        if (activeThread) {
            activeThread.messageCount = Math.max(0, (activeThread.messageCount || 1) - 1);
            updateThreadInLists(activeThread);
        }
        showToast("Message deleted.", "success");
    } catch (error) {
        showToast(error.message || "Failed to delete message.", "error");
    }
}


/* ==========================================================================
   DELETE THREAD CONFIRM MODAL
   ========================================================================== */
function initDeleteThreadModal() {
    const modal = document.getElementById("deleteThreadModal");
    const cancelBtn = document.getElementById("cancelDeleteThreadBtn");
    const confirmBtn = document.getElementById("confirmDeleteThreadBtn");

    cancelBtn.addEventListener("click", () => modal.classList.add("hidden"));
    modal.addEventListener("click", (e) => { if (e.target === modal) modal.classList.add("hidden"); });
    confirmBtn.addEventListener("click", handleConfirmDeleteThread);
}

async function handleConfirmDeleteThread() {
    if (!activeThread) return;

    const confirmBtn = document.getElementById("confirmDeleteThreadBtn");
    const btnText = confirmBtn.querySelector(".btn-text");
    const spinner = document.getElementById("deleteThreadSpinner");

    confirmBtn.disabled = true;
    btnText.textContent = "Deleting...";
    spinner.classList.remove("hidden");

    try {
        await apiRequest(`/discussions/threads/${activeThread.id}`, { method: "DELETE" });
        showToast("Thread deleted.", "success");
        document.getElementById("deleteThreadModal").classList.add("hidden");
        closeThreadDetailModal();

        if (selectedCompany) await loadCompanyThreads(selectedCompany.id);
        await loadMyThreads();

    } catch (error) {
        showToast(error.message || "Failed to delete thread.", "error");
    } finally {
        confirmBtn.disabled = false;
        btnText.textContent = "Delete";
        spinner.classList.add("hidden");
    }
}