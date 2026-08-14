

/**
* CareerSpace - Help Page Script
* Pure Vanilla JavaScript (ES6+)
* Depends on apiClient.js (apiRequest)
*/

const REPORT_FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLScLo-PGjNlEbety6G4yuKv03PWpXUaU8fU5sJ1mbXb3JvoiLw/viewform";
const CONTACT_EMAIL = "careerspace05@gmail.com";

document.addEventListener('DOMContentLoaded', () => {
safeInit("loadProfile", loadProfile);
safeInit("initSidebarBehavior", initSidebarBehavior);
safeInit("initProfileDropdown", initProfileDropdown);
safeInit("initHelpCards", initHelpCards);
});

function safeInit(name, fn) {
try { fn(); } catch (error) { console.error(`[help.js] "${name}" failed to initialize:`, error); }
}


/* ==========================================================================
PROFILE (TOPBAR) - shared shell pattern
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
HELP CARDS
========================================================================== */
function initHelpCards() {
const reportCard = document.getElementById("submitReportCard");
const contactCard = document.getElementById("contactUsCard");

if (reportCard) {
reportCard.addEventListener("click", () => {
window.open(REPORT_FORM_URL, "_blank", "noopener,noreferrer");
});
}

if (contactCard) {
contactCard.addEventListener("click", () => {
window.location.href = `mailto:${CONTACT_EMAIL}`;
});
}
}