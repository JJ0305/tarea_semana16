let csrfToken = "";

const notice = document.querySelector("#notice");
const loginPanel = document.querySelector("#login-panel");
const appPanel = document.querySelector("#app-panel");

function showNotice(message, success) {
  notice.textContent = message;
  notice.classList.toggle("success", Boolean(success));
}

async function refreshCsrf() {
  const response = await fetch("/api/auth/csrf");
  if (!response.ok) throw new Error("No se pudo iniciar una sesión segura.");
  csrfToken = (await response.json()).token;
}

async function api(url, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body) headers.set("Content-Type", "application/json");
  if (csrfToken && options.method && options.method !== "GET") headers.set("X-XSRF-TOKEN", csrfToken);
  const response = await fetch(url, { ...options, headers, credentials: "same-origin" });
  if (response.status === 401) {
    showApp(false);
    throw new Error("Tu sesión terminó. Inicia sesión nuevamente.");
  }
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.error || "No se pudo completar la operación.");
  }
  return response.status === 204 ? null : response.json();
}

function showApp(authenticated, username = "") {
  loginPanel.classList.toggle("hidden", authenticated);
  appPanel.classList.toggle("hidden", !authenticated);
  document.querySelector("#logout").classList.toggle("hidden", !authenticated);
  document.querySelector("#welcome").textContent = authenticated ? `Sesión iniciada como ${username}` : "";
}

async function loadData() {
  const [clients, requests] = await Promise.all([api("/api/clients"), api("/api/requests")]);
  document.querySelector("#clients-body").innerHTML = clients.map(client =>
    `<tr><td>${escapeHtml(client.name)}</td><td>${escapeHtml(client.email)}</td><td>${escapeHtml(client.phone || "—")}</td></tr>`
  ).join("");
  document.querySelector("#client-select").innerHTML = '<option value="">Selecciona un cliente</option>' +
    clients.map(client => `<option value="${client.id}">${escapeHtml(client.name)} (${escapeHtml(client.email)})</option>`).join("");
  document.querySelector("#requests-body").innerHTML = requests.map(request =>
    `<tr><td>${escapeHtml(request.clientName)}</td><td>${escapeHtml(request.subject)}</td><td>${escapeHtml(request.description)}</td><td>${escapeHtml(request.status)}</td><td>${new Date(request.createdAt).toLocaleString("es")}</td></tr>`
  ).join("");
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
}

document.querySelector("#login-form").addEventListener("submit", async event => {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.currentTarget));
  try {
    await api("/api/auth/login", { method: "POST", body: JSON.stringify(data) });
    const user = await api("/api/auth/me");
    showApp(true, user.username);
    await loadData();
    showNotice("", true);
  } catch (error) {
    showNotice(error.message);
  }
});

document.querySelector("#client-form").addEventListener("submit", async event => {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.currentTarget));
  try {
    await api("/api/clients", { method: "POST", body: JSON.stringify(data) });
    event.currentTarget.reset();
    await loadData();
    showNotice("Cliente registrado.", true);
  } catch (error) {
    showNotice(error.message);
  }
});

document.querySelector("#request-form").addEventListener("submit", async event => {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.currentTarget));
  data.clientId = Number(data.clientId);
  try {
    await api("/api/requests", { method: "POST", body: JSON.stringify(data) });
    event.currentTarget.reset();
    await loadData();
    showNotice("Solicitud registrada.", true);
  } catch (error) {
    showNotice(error.message);
  }
});

document.querySelector("#logout").addEventListener("click", async () => {
  try {
    await api("/api/auth/logout", { method: "POST" });
    await refreshCsrf();
    showApp(false);
    showNotice("Sesión cerrada.", true);
  } catch (error) {
    showNotice(error.message);
  }
});

(async () => {
  try {
    await refreshCsrf();
    const user = await api("/api/auth/me");
    showApp(true, user.username);
    await loadData();
  } catch {
    showApp(false);
  }
})();
