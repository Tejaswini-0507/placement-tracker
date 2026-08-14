/**
 * CareerSpace - Student Dashboard Script
 * Pure Vanilla JavaScript (ES6+)
 * Depends on apiClient.js (apiRequest, showToast, escapeHtml)
 *
 * Replaces the old placeholder DashboardData with real data from
 * GET /api/dashboard (DashboardService.getDashBoard()).
 *
 * KNOWN BACKEND QUIRKS THIS FILE WORKS AROUND (not fixed here):
 * - RecentExperienceResponse.topics can NPE server-side if an experience has
 *   no topics saved (DashboardService.experienceToRecent() doesn't null-check
 *   before calling .toString()). If your dashboard 500s, this is why.
 * - DashboardStats.appliedCount is always identical to totalApplications
 *   (not a true "still just applied" count) - ignored here, computed properly
 *   from totalApplications/interviewsCount/rejectedCount instead.
 * - ResumeVersionResponse.isCurrent is hardcoded true for every resume -
 *   ignored here, "current" is computed client-side as highest versionNumber.
 */

// Full 12-value ApplicationStatus enum mapped onto the 5 existing status-pill
// CSS classes already defined in dashboard.css (no new CSS added)
const STATUS_PILL_MAP = {
    APPLIED:                  { label: "Applied",              cls: "applied" },
    OA_SCHEDULED:              { label: "OA Scheduled",         cls: "review" },
    OA_COMPLETED:               { label: "OA Completed",         cls: "review" },
    RESULT_WAITING:             { label: "Result Waiting",       cls: "review" },
    INTERVIEW_SCHEDULED:        { label: "Interview Scheduled",  cls: "interview" },
    INTERVIEW_COMPLETED:        { label: "Interview Completed",  cls: "interview" },
    SELECTED:                   { label: "Selected",             cls: "offer" },
    OFFER_RECEIVED:              { label: "Offer Received",       cls: "offer" },
    OFFER_ACCEPTED:              { label: "Offer Accepted",       cls: "offer" },
    JOINING_LETTER_RECEIVED:     { label: "Joined",               cls: "offer" },
    REJECTED:                    { label: "Rejected",             cls: "rejected" },
    OFFER_DECLINED:              { label: "Offer Declined",       cls: "rejected" }
};

const OFFER_STATUSES = ["SELECTED", "OFFER_RECEIVED", "OFFER_ACCEPTED", "JOINING_LETTER_RECEIVED"];

document.addEventListener('DOMContentLoaded', () => {
    safeInit("loadProfile", loadProfile);
    safeInit("loadDashboard", loadDashboard);
    safeInit("initSidebarBehavior", initSidebarBehavior);
    safeInit("initProfileDropdown", initProfileDropdown);
    safeInit("initSidebarActiveState", initSidebarActiveState);
    safeInit("initQuickActions", initQuickActions);
    safeInit("initCircularProgressAnimations", initCircularProgressAnimations);
    safeInit("initCounterAnimations", initCounterAnimations);
});

function safeInit(name, fn) {
    try { fn(); } catch (error) { console.error(`[dashboard.js] "${name}" failed to initialize:`, error); }
}


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
    }
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

function initSidebarActiveState() {
    const sidebarLinks = document.querySelectorAll('.sidebar-link');
    sidebarLinks.forEach(link => {
        link.addEventListener('click', () => {
            if (window.innerWidth <= 1024) {
                document.getElementById('dashSidebar')?.classList.remove('open');
                document.getElementById('sidebarBackdrop')?.classList.remove('active');
            }
        });
    });
}

function initQuickActions() {
    document.getElementById('actionTrackApp')?.addEventListener('click', () => window.location.href = 'applications.html');
    document.getElementById('actionUploadResume')?.addEventListener('click', () => window.location.href = 'resumes.html');
    document.getElementById('actionBrowseCompanies')?.addEventListener('click', () => window.location.href = 'companies.html');
    document.getElementById('actionReadExp')?.addEventListener('click', () => window.location.href = 'experiences.html');
}


/* ==========================================================================
   LOAD DASHBOARD DATA
   ========================================================================== */
async function loadDashboard() {
    try {
        const stats = await apiRequest("/dashboard", { method: "GET" });

        renderLastUpdated(stats.lastUpdated);
        renderOverviewCircles(stats);
        renderKpiCards(stats);
        renderStatusDistributionChart(stats);
        renderRecentApplications(stats.recentApplications || []);
        renderRecentExperiences(stats.recentExperiences || []);
        renderResumeVersions(stats.resumeVersions || []);

    } catch (error) {
        console.error("Dashboard Error:", error);
        showToast("Couldn't load your dashboard: " + (error.message || "unknown error"), "error");
    }
}

function renderLastUpdated(epochMillis) {
    const el = document.getElementById("lastUpdated");
    if (!el) return;
    if (!epochMillis) { el.textContent = "Last updated: just now"; return; }
    const date = new Date(epochMillis);
    el.textContent = `Last updated: ${date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} at ${date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}`;
}


/* ==========================================================================
   OVERVIEW CIRCULAR CARDS
   ========================================================================== */
function renderOverviewCircles(stats) {
    const total = stats.totalApplications || 0;
    const offers = stats.offersCount || 0;
    const interviews = stats.interviewsCount || 0;
    const rejected = stats.rejectedCount || 0;

    const interviewRate = total > 0 ? Math.round((interviews / total) * 100) : 0;
    const offerRate = total > 0 ? Math.round((offers / total) * 100) : 0;
    const successRate = stats.applicationSuccessRate != null ? Math.round(stats.applicationSuccessRate) : 0;
    const rejectionRate = total > 0 ? Math.round((rejected / total) * 100) : 0;

    setCirclePercentage("circleInterview", "percentInterview", interviewRate);
    setCirclePercentage("circleOffer", "percentOffer", offerRate);
    setCirclePercentage("circleSuccess", "percentSuccess", successRate);
    setCirclePercentage("circleRejected", "percentRejected", rejectionRate);
}

function setCirclePercentage(circleId, textId, percentage) {
    const circle = document.getElementById(circleId);
    const text = document.getElementById(textId);
    if (circle) circle.setAttribute("data-percentage", String(percentage));
    if (text) text.textContent = `${percentage}%`;
}


/* ==========================================================================
   KPI CARDS
   ========================================================================== */
function renderKpiCards(stats) {
    const total = stats.totalApplications || 0;
    const offers = stats.offersCount || 0;
    const interviews = stats.interviewsCount || 0;
    const rejected = stats.rejectedCount || 0;

    // Backend's appliedCount is a duplicate of totalApplications (bug) -
    // compute the real "applied, not yet progressed" bucket ourselves
    const appliedOnly = Math.max(0, total - interviews - rejected);

    setCounter("kpiTotalApplications", total);
    setCounter("kpiOffers", offers);
    setCounter("kpiInterviews", interviews);
    setCounter("kpiApplied", appliedOnly);
    setCounter("kpiRejected", rejected);
    setCounter("kpiExperiences", stats.totalExperiences || 0);
    setCounter("kpiResumes", stats.totalResumeVersions || 0);

    // Derive honest subtext from recentApplications (only last 5 available -
    // scoped language reflects that, not claimed as a full-history stat)
    const recentApps = stats.recentApplications || [];
    const uniqueCompanies = new Set(recentApps.map(a => a.companyName)).size;
    const totalAppsDesc = document.getElementById("kpiTotalAppsDesc");
    if (totalAppsDesc) {
        totalAppsDesc.textContent = uniqueCompanies > 0
            ? `Across ${uniqueCompanies} recent ${uniqueCompanies === 1 ? 'company' : 'companies'}`
            : "Start tracking today";
    }

    const offerCompanies = recentApps
        .filter(a => OFFER_STATUSES.includes(a.status))
        .map(a => a.companyName);
    const offersDesc = document.getElementById("kpiOffersDesc");
    if (offersDesc) {
        offersDesc.textContent = offerCompanies.length > 0
            ? offerCompanies.slice(0, 2).join(" & ")
            : "Keep going — offers will show here";
    }
}

function setCounter(elementId, value) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.setAttribute("data-target", String(value));
    el.textContent = "0"; // reset so the counter animation has something to animate from
}


/* ==========================================================================
   APPLICATION STATUS DISTRIBUTION (BAR CHART)
   ========================================================================== */
function renderStatusDistributionChart(stats) {
    const container = document.getElementById("statusChartBars");
    if (!container) return;

    const total = stats.totalApplications || 0;
    const offers = stats.offersCount || 0;
    const interviews = stats.interviewsCount || 0;
    const rejected = stats.rejectedCount || 0;

    // interviewsCount includes offer-stage applications per backend logic,
    // so split into mutually exclusive buckets that sum to total
    const interviewOnly = Math.max(0, interviews - offers);
    const appliedOnly = Math.max(0, total - interviews - rejected);

    const buckets = [
        { label: "Applied", count: appliedOnly, cls: "fill-blue" },
        { label: "Interview", count: interviewOnly, cls: "fill-purple" },
        { label: "Offer", count: offers, cls: "fill-green" },
        { label: "Rejected", count: rejected, cls: "fill-red" }
    ];

    const maxCount = Math.max(1, ...buckets.map(b => b.count));

    container.innerHTML = buckets.map(b => {
        const heightPct = Math.round((b.count / maxCount) * 100);
        return `
            <div class="chart-bar-group">
                <div class="bar-fill-track">
                    <div class="bar-fill ${b.cls}" style="height: ${heightPct}%;"></div>
                </div>
                <span class="bar-label">${b.label} (${b.count})</span>
            </div>
        `;
    }).join('');
}


/* ==========================================================================
   RECENT APPLICATIONS TABLE
   ========================================================================== */
function renderRecentApplications(recentApplications) {
    const tbody = document.getElementById('recentAppsTbody');
    if (!tbody) return;

    if (recentApplications.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--secondary-text); padding: 24px;">No applications tracked yet. <a href="applications.html" class="link-more">Track your first one →</a></td></tr>`;
        return;
    }

    tbody.innerHTML = recentApplications.map(app => {
        const meta = STATUS_PILL_MAP[app.status] || { label: app.status, cls: "applied" };
        const initials = getInitials(app.companyName);
        return `
            <tr>
                <td>
                    <div class="table-company">
                        <div class="company-logo-badge">${initials}</div>
                        <strong>${escapeHtml(app.companyName)}</strong>
                    </div>
                </td>
                <td>—</td>
                <td><span class="status-pill ${meta.cls}">${escapeHtml(meta.label)}</span></td>
                <td>${formatDate(app.createdAt)}</td>
                <td><span class="resume-tag">—</span></td>
                <td>
                    <a href="applications.html" class="btn-icon-action">👁️ View</a>
                </td>
            </tr>
        `;
    }).join('');
}


/* ==========================================================================
   RECENT INTERVIEW EXPERIENCES TIMELINE
   ========================================================================== */
function renderRecentExperiences(recentExperiences) {
    const container = document.getElementById('experiencesTimeline');
    if (!container) return;

    if (recentExperiences.length === 0) {
        container.innerHTML = `<p style="color:var(--secondary-text); font-size:0.85rem; text-align:center; padding: 20px 0;">No interview experiences logged yet.</p>`;
        return;
    }

    container.innerHTML = recentExperiences.map(exp => {
        // experienceSummary isn't part of this response - build a truthful
        // snippet from the fields that ARE actually returned, rather than
        // fabricating preview text
        const parts = [];
        if (exp.difficultyLevel) parts.push(`Difficulty: ${capitalize(exp.difficultyLevel)}`);
        if (exp.result) parts.push(`Result: ${capitalize(exp.result)}`);
        const topics = parseTopics(exp.topics);
        if (topics.length > 0) parts.push(`Topics: ${topics.join(', ')}`);
        const snippet = parts.length > 0 ? parts.join(' · ') : "No further details recorded.";

        return `
            <div class="exp-item">
                <div class="exp-marker">💼</div>
                <div class="exp-content">
                    <h4>${escapeHtml(exp.companyName)}${exp.interviewRound ? ` • ${escapeHtml(exp.interviewRound)}` : ''}</h4>
                    <div class="exp-meta">Logged on ${formatDate(exp.createdAt)} · 👍 ${exp.upvotes || 0} 👎 ${exp.downvotes || 0}</div>
                    <p class="exp-snippet">${escapeHtml(snippet)}</p>
                    <a href="experiences.html" class="btn-read-more">Read Full Log →</a>
                </div>
            </div>
        `;
    }).join('');
}

function parseTopics(raw) {
    if (!raw) return [];
    try {
        if (Array.isArray(raw)) return raw;
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed : [];
    } catch {
        return [];
    }
}

function capitalize(str) {
    if (!str) return "";
    return str.charAt(0) + str.slice(1).toLowerCase().replace(/_/g, ' ');
}


/* ==========================================================================
   RESUME VERSIONS LIST
   ========================================================================== */
function renderResumeVersions(resumeVersions) {
    const container = document.getElementById('resumesList');
    if (!container) return;

    if (resumeVersions.length === 0) {
        container.innerHTML = `<p style="color:var(--secondary-text); font-size:0.85rem; text-align:center; padding: 20px 0;">No resumes uploaded yet.</p>`;
        return;
    }

    // Backend's isCurrent is hardcoded true for every resume - compute the
    // real "latest" ourselves
    const highestVersion = Math.max(...resumeVersions.map(r => r.versionNumber || 0));

    container.innerHTML = resumeVersions.map(res => {
        const fileName = extractFileName(res.fileUrl);
        const isPrimary = res.versionNumber === highestVersion;
        return `
            <div class="resume-item-card">
                <div class="resume-left">
                    <span class="pdf-icon-box">📄</span>
                    <div class="resume-details">
                        <strong>${escapeHtml(fileName)}</strong>
                        <span>Version ${res.versionNumber} • ${formatFileSize(res.fileSizeBytes)}</span>
                    </div>
                </div>
                <div class="resume-actions">
                    ${isPrimary ? '<span class="tag-primary">Latest</span>' : ''}
                    <a href="resumes.html" class="btn btn-secondary btn-sm">View</a>
                </div>
            </div>
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


/* ==========================================================================
   SHARED HELPERS
   ========================================================================== */
function getInitials(name) {
    if (!name) return "?";
    return name.split(" ").filter(Boolean).map(w => w[0]).join("").toUpperCase().slice(0, 2);
}

function formatDate(epochMillis) {
    if (!epochMillis) return "—";
    return new Date(epochMillis).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}


/* ==========================================================================
   ANIMATIONS (unchanged from original - reads data-percentage/data-target
   attributes, which are now set to real values before these observers fire)
   ========================================================================== */
function initCircularProgressAnimations() {
    const circleFills = document.querySelectorAll('.circle-fill');
    const radius = 42;
    const circumference = 2 * Math.PI * radius;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const circle = entry.target;
                const percentage = parseInt(circle.getAttribute('data-percentage'), 10) || 0;
                const offset = circumference - (percentage / 100) * circumference;
                circle.style.strokeDashoffset = offset;
            }
        });
    }, { threshold: 0.2 });

    circleFills.forEach(circle => {
        circle.style.strokeDasharray = circumference;
        circle.style.strokeDashoffset = circumference;
        observer.observe(circle);
    });
}

function initCounterAnimations() {
    const counters = document.querySelectorAll('.counter');
    let animated = false;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting && !animated) {
                counters.forEach(counter => {
                    const target = parseInt(counter.getAttribute('data-target'), 10) || 0;
                    const duration = 1500;
                    const stepTime = 30;
                    const steps = duration / stepTime;
                    const increment = target / steps;
                    let current = 0;

                    const timer = setInterval(() => {
                        current += increment;
                        if (current >= target) {
                            counter.textContent = target;
                            clearInterval(timer);
                        } else {
                            counter.textContent = Math.floor(current);
                        }
                    }, stepTime);
                });
                animated = true;
            }
        });
    }, { threshold: 0.2 });

    const statsSection = document.getElementById('statsGrid');
    if (statsSection) observer.observe(statsSection);
}