/**
 * CareerSpace - Analytics Module Script
 * Pure Vanilla JavaScript (ES6+) + Chart.js (only sanctioned external library)
 * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
 *
 * NOTE ON BACKEND DATA QUALITY:
 * - MyExperienceResponse.passedCount/failedCount are computed server-side with
 *   a broken enum-vs-String comparison and will always be 0. This file ignores
 *   those two fields entirely and recomputes pass/fail/pending/waiting counts
 *   client-side from the per-experience `result` strings inside
 *   experiencesByCompany, which ARE correct.
 * - MyResumeResponse.ResumeDetailResponse.isCurrent is hardcoded true for
 *   every resume server-side. This file ignores it and determines "current"
 *   client-side as the highest versionNumber.
 */

let charts = {};

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initRetryButton", initRetryButton);
    safeInit("loadAnalytics", loadAnalytics);
});

function safeInit(name, fn) {
    try { fn(); } catch (error) { console.error(`[analytics.js] "${name}" failed to initialize:`, error); }
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

function initRetryButton() {
    document.getElementById("analyticsRetryBtn").addEventListener("click", loadAnalytics);
}


/* ==========================================================================
   LOAD ALL THREE AGGREGATE ENDPOINTS
   ========================================================================== */
async function loadAnalytics() {
    showState("loading");

    try {
        const [applications, experiences, resumes] = await Promise.all([
            apiRequest("/my-applications", { method: "GET" }),
            apiRequest("/my-experiences", { method: "GET" }),
            apiRequest("/my-resumes", { method: "GET" })
        ]);

        renderKpis(applications, experiences, resumes);
        renderApplicationStatusChart(applications);
        renderOutcomesChart(experiences);
        renderExperiencesByCompanyChart(experiences);
        renderResumeTable(resumes);

        showState("content");

    } catch (error) {
        console.error("Analytics Error:", error);
        document.getElementById("analyticsErrorMessage").textContent = error.message || "Something went wrong.";
        showState("error");
    }
}

function showState(state) {
    const loading = document.getElementById("analyticsLoading");
    const error = document.getElementById("analyticsError");
    const content = document.getElementById("analyticsContent");

    [loading, error, content].forEach(el => el.classList.add("hidden"));

    if (state === "loading") loading.classList.remove("hidden");
    else if (state === "error") error.classList.remove("hidden");
    else if (state === "content") content.classList.remove("hidden");
}


/* ==========================================================================
   KPI CARDS
   ========================================================================== */
function renderKpis(applications, experiences, resumes) {
    document.getElementById("kpiTotalApplications").textContent = applications.totalApplications ?? 0;
    document.getElementById("kpiSuccessRate").textContent = `${applications.successRate ?? 0}%`;
    document.getElementById("kpiTotalExperiences").textContent = experiences.totalExperiences ?? 0;
    document.getElementById("kpiTotalResumes").textContent = resumes.totalVersions ?? 0;

    const avgDiff = experiences.averageDifficulty;
    if (avgDiff != null && experiences.totalExperiences > 0) {
        document.getElementById("kpiAvgDifficulty").textContent = `${avgDiff.toFixed(1)} (${difficultyLabelFromScore(avgDiff)})`;
    } else {
        document.getElementById("kpiAvgDifficulty").textContent = "--";
    }
}

// Mirrors AnalyticsService's numeric thresholds (EASY<=3, MEDIUM<=6, HARD<=8, else EXPERT)
function difficultyLabelFromScore(score) {
    if (score <= 3) return "Easy";
    if (score <= 6) return "Medium";
    if (score <= 8) return "Hard";
    return "Expert";
}


/* ==========================================================================
   CHART 1: APPLICATION STATUS DISTRIBUTION
   ========================================================================== */
function renderApplicationStatusChart(applications) {
    const canvas = document.getElementById("applicationStatusChart");
    const emptyEl = document.getElementById("applicationsChartEmpty");

    if (!applications.totalApplications || applications.totalApplications === 0) {
        canvas.parentElement.classList.add("hidden");
        emptyEl.classList.remove("hidden");
        return;
    }
    canvas.parentElement.classList.remove("hidden");
    emptyEl.classList.add("hidden");

    const statusCount = applications.statusCount || {};
    const labels = ["Applied", "Interview", "Offer", "Rejected"];
    const keys = ["APPLIED", "INTERVIEW", "OFFER", "REJECTED"];
    const data = keys.map(k => statusCount[k] || 0);
    const colors = ["#2563EB", "#F59E0B", "#10B981", "#EF4444"];

    destroyChart("applicationStatusChart");
    charts.applicationStatusChart = new Chart(canvas, {
        type: "doughnut",
        data: {
            labels,
            datasets: [{ data, backgroundColor: colors, borderWidth: 0 }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: "bottom", labels: { boxWidth: 12, padding: 16, font: { size: 12 } } }
            }
        }
    });
}


/* ==========================================================================
   CHART 2: INTERVIEW OUTCOMES (computed client-side, backend counts broken)
   ========================================================================== */
function flattenExperiences(experiences) {
    const byCompany = experiences.experiencesByCompany || {};
    return Object.values(byCompany).flat();
}

function renderOutcomesChart(experiences) {
    const canvas = document.getElementById("outcomesChart");
    const emptyEl = document.getElementById("outcomesChartEmpty");

    const flat = flattenExperiences(experiences);

    if (flat.length === 0) {
        canvas.parentElement.classList.add("hidden");
        emptyEl.classList.remove("hidden");
        return;
    }
    canvas.parentElement.classList.remove("hidden");
    emptyEl.classList.add("hidden");

    const counts = { PASSED: 0, FAILED: 0, PENDING: 0, WAITING_LIST: 0 };
    flat.forEach(exp => {
        if (counts.hasOwnProperty(exp.result)) counts[exp.result]++;
    });

    destroyChart("outcomesChart");
    charts.outcomesChart = new Chart(canvas, {
        type: "doughnut",
        data: {
            labels: ["Passed", "Failed", "Pending", "Waiting List"],
            datasets: [{
                data: [counts.PASSED, counts.FAILED, counts.PENDING, counts.WAITING_LIST],
                backgroundColor: ["#10B981", "#EF4444", "#2563EB", "#8B5CF6"],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: "bottom", labels: { boxWidth: 12, padding: 16, font: { size: 12 } } }
            }
        }
    });
}


/* ==========================================================================
   CHART 3: EXPERIENCES BY COMPANY
   ========================================================================== */
function renderExperiencesByCompanyChart(experiences) {
    const canvas = document.getElementById("experiencesByCompanyChart");
    const emptyEl = document.getElementById("companyChartEmpty");

    const byCompany = experiences.experiencesByCompany || {};
    const entries = Object.entries(byCompany);

    if (entries.length === 0) {
        canvas.parentElement.classList.add("hidden");
        emptyEl.classList.remove("hidden");
        return;
    }
    canvas.parentElement.classList.remove("hidden");
    emptyEl.classList.add("hidden");

    entries.sort((a, b) => b[1].length - a[1].length);

    const labels = entries.map(([company]) => company);
    const data = entries.map(([, list]) => list.length);

    destroyChart("experiencesByCompanyChart");
    charts.experiencesByCompanyChart = new Chart(canvas, {
        type: "bar",
        data: {
            labels,
            datasets: [{
                label: "Experiences",
                data,
                backgroundColor: "#2563EB",
                borderRadius: 6,
                maxBarThickness: 36
            }]
        },
        options: {
            indexAxis: "y",
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { beginAtZero: true, ticks: { stepSize: 1 } }
            }
        }
    });
}

function destroyChart(key) {
    if (charts[key]) {
        charts[key].destroy();
        delete charts[key];
    }
}


/* ==========================================================================
   RESUME VERSIONS TABLE
   ========================================================================== */
function renderResumeTable(resumes) {
    const wrapper = document.getElementById("resumesTableWrapper");
    const emptyEl = document.getElementById("resumesTableEmpty");
    const tbody = document.getElementById("resumesTableBody");

    const list = resumes.resumes || [];

    if (list.length === 0) {
        wrapper.classList.add("hidden");
        emptyEl.classList.remove("hidden");
        return;
    }
    wrapper.classList.remove("hidden");
    emptyEl.classList.add("hidden");

    const highestVersion = Math.max(...list.map(r => r.versionNumber || 0));

    tbody.innerHTML = list.map(resume => {
        const fileName = extractFileName(resume.fileUrl);
        const isCurrent = resume.versionNumber === highestVersion;
        return `
            <tr>
                <td>
                    v${resume.versionNumber}
                    ${isCurrent ? '<span class="current-version-badge">Latest</span>' : ''}
                </td>
                <td>${escapeHtml(fileName)}</td>
                <td>${formatFileSize(resume.fileSizeBytes)}</td>
                <td>${resume.notes ? escapeHtml(resume.notes) : '<span style="color:var(--secondary-text);">—</span>'}</td>
                <td></td>
            </tr>
        `;
    }).join('');
}

function extractFileName(fileUrl) {
    if (!fileUrl) return "resume";
    const afterLastSlash = fileUrl.split(/[/\\]/).pop();
    const underscoreIndex = afterLastSlash.indexOf('_');
    return underscoreIndex !== -1 ? afterLastSlash.slice(underscoreIndex + 1) : afterLastSlash;
}

function formatFileSize(bytes) {
    if (!bytes || bytes <= 0) return "0 B";
    const units = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${units[i]}`;
}