function login() {
  alert("Login clicked (connect backend later)");
}

function registerUser() {
  alert("Register clicked (connect backend later)");
}
function openPolicy(id) {
  window.location.href = "/user/policy-details?id=" + id;
}
function submitClaim() {
  alert("Claim submitted successfully (backend later)");
}

function payNow() {
  alert("Redirecting to payment gateway (mock)");
}
function createTicket() {
  alert("Support ticket created (backend later)");
}

function saveProfile() {
  alert("Profile updated successfully");
}
function approveAction(type) {
  alert(type + " approved");
}

function rejectAction(type) {
  alert(type + " rejected");
}

function exportCSV() {
  alert("Exporting CSV (mock)");
}

function saveSettings() {
  alert("Settings saved");
}
