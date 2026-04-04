/* ═══════════════════════════════════════════════════════════════════
   SIS Frontend — API Client & Utilities
   All API calls route through this module using fetch + JWT headers.
═══════════════════════════════════════════════════════════════════ */

const API_BASE = 'http://localhost:8080/api';

// ── Token management ───────────────────────────────────────────────
const Auth = {
  getToken:    () => localStorage.getItem('sis_token'),
  getRole:     () => localStorage.getItem('sis_role'),
  getUser:     () => localStorage.getItem('sis_user'),
  getUserId:   () => localStorage.getItem('sis_userId'),

  save(token, role, fullName, userId) {
    localStorage.setItem('sis_token',  token);
    localStorage.setItem('sis_role',   role);
    localStorage.setItem('sis_user',   fullName);
    localStorage.setItem('sis_userId', userId);
  },

  clear() {
    ['sis_token','sis_role','sis_user','sis_userId'].forEach(k => localStorage.removeItem(k));
  },

  isLoggedIn() { return !!this.getToken(); },

  /** Redirect to login if not authenticated */
  require() {
    if (!this.isLoggedIn()) {
      const depth = window.location.pathname.includes('/pages/') ? '../index.html' : 'index.html';
      window.location.href = depth;
      return false;
    }
    return true;
  }
};

// ── Core fetch wrapper ─────────────────────────────────────────────
async function api(method, path, body = null) {
  const headers = { 'Content-Type': 'application/json' };
  const token = Auth.getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const opts = { method, headers };
  if (body && method !== 'GET') opts.body = JSON.stringify(body);

  try {
    const res = await fetch(API_BASE + path, opts);

    if (res.status === 401) {
      Auth.clear();
      const depth = window.location.pathname.includes('/pages/') ? '../index.html' : 'index.html';
      window.location.href = depth;
      return null;
    }

    if (res.status === 204) return null;  // No content (DELETE)

    const data = await res.json();

    if (!res.ok) {
      const msg = data.message || data.error || `Error ${res.status}`;
      throw new Error(msg);
    }

    return data;
  } catch (err) {
    if (err.name !== 'TypeError') throw err;     // Re-throw API errors
    throw new Error('Cannot connect to server. Is Spring Boot running?');
  }
}

// Convenience methods
const get    = (path)       => api('GET',    path);
const post   = (path, body) => api('POST',   path, body);
const put    = (path, body) => api('PUT',    path, body);
const del    = (path)       => api('DELETE', path);
const patch  = (path, body) => api('PATCH',  path, body);

// ── API namespaces ─────────────────────────────────────────────────
const API = {
  auth: {
    login:    (username, password) => post('/auth/login', { username, password }),
  },
  dashboard: {
    stats:    () => get('/dashboard/stats'),
  },
  students: {
    getAll:   ()           => get('/students'),
    getById:  (id)         => get(`/students/${id}`),
    search:   (q, page=0)  => get(`/students/search?q=${encodeURIComponent(q)}&page=${page}&size=20`),
    create:   (data)       => post('/students', data),
    update:   (id, data)   => put(`/students/${id}`, data),
    delete:   (id)         => del(`/students/${id}`),
    byDept:   (deptId)     => get(`/students/department/${deptId}`),
  },
  courses: {
    getAll:   ()                    => get('/courses'),
    getById:  (id)                  => get(`/courses/${id}`),
    create:   (data)                => post('/courses', data),
    update:   (id, data)            => put(`/courses/${id}`, data),
    delete:   (id)                  => del(`/courses/${id}`),
    enroll:   (courseId, studentId) => post(`/courses/${courseId}/enroll/${studentId}`),
    enrollments: (courseId)         => get(`/courses/${courseId}/enrollments`),
    studentEnrollments: (studentId) => get(`/courses/student/${studentId}/enrollments`),
  },
  attendance: {
    byStudent:    (sid)        => get(`/attendance/student/${sid}`),
    byCourse:     (sid, cid)   => get(`/attendance/student/${sid}/course/${cid}`),
    percentage:   (sid, cid)   => get(`/attendance/student/${sid}/course/${cid}/percentage`),
    mark:         (data)       => post('/attendance/mark', data),
    byDate:       (cid, date)  => get(`/attendance/course/${cid}/date/${date}`),
  },
  marks: {
    byStudent:    (sid)        => get(`/marks/student/${sid}`),
    byCourse:     (sid, cid)   => get(`/marks/student/${sid}/course/${cid}`),
    reportCard:   (sid)        => get(`/marks/student/${sid}/reportcard`),
    enter:        (data)       => post('/marks/enter', data),
  },
  fees: {
    byStudent:    (sid)        => get(`/fees/student/${sid}`),
    pending:      ()           => get('/fees/pending'),
    balance:      (sid)        => get(`/fees/student/${sid}/balance`),
    create:       (data)       => post('/fees', data),
    pay:          (feeId, data) => post(`/fees/${feeId}/pay`, data),
  },
  users: {
    getAll:   ()         => get('/users'),
    create:   (data)     => post('/users', data),
    update:   (id, data) => put(`/users/${id}`, data),
    delete:   (id)       => del(`/users/${id}`),
  },
  faculty: {
    getAll:   ()         => get('/faculty'),
  },
};

// ═══════════════════════════════════════════════════════════════════
// UI Utilities
// ═══════════════════════════════════════════════════════════════════

/** Show a toast notification */
function toast(message, type = 'info') {
  const container = document.getElementById('toast-container') ||
    (() => { const el = document.createElement('div'); el.id = 'toast-container'; document.body.appendChild(el); return el; })();

  const icons = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `<span>${icons[type] || 'ℹ️'}</span><span>${message}</span>`;
  container.appendChild(el);
  setTimeout(() => { el.style.opacity = '0'; el.style.transform = 'translateX(100%)'; el.style.transition = '.3s'; setTimeout(() => el.remove(), 300); }, 3500);
}

/** Open a modal by id */
function openModal(id)  { document.getElementById(id)?.classList.add('open'); }
function closeModal(id) { document.getElementById(id)?.classList.remove('open'); }

/** Show inline loading state in a container */
function showLoading(containerId) {
  const el = document.getElementById(containerId);
  if (el) el.innerHTML = `<div class="loading-overlay"><div class="spinner"></div><span>Loading…</span></div>`;
}

/** Show empty state */
function showEmpty(containerId, msg = 'No records found') {
  const el = document.getElementById(containerId);
  if (el) el.innerHTML = `
    <div class="empty-state">
      <div class="empty-icon">📭</div>
      <h3>${msg}</h3>
      <p>Try adjusting your search or add new records.</p>
    </div>`;
}

/** Render a badge for various statuses */
function statusBadge(status) {
  const map = {
    ACTIVE: 'badge-success', DROPPED: 'badge-danger', COMPLETED: 'badge-navy',
    PRESENT: 'badge-success', ABSENT: 'badge-danger', LATE: 'badge-warning',
    PAID: 'badge-success', PARTIAL: 'badge-warning', PENDING: 'badge-danger',
    ADMIN: 'badge-navy', FACULTY: 'badge-amber', STUDENT: 'badge-info',
    O: 'badge-success', 'A+': 'badge-success', A: 'badge-success',
    'B+': 'badge-info', B: 'badge-info', C: 'badge-warning', F: 'badge-danger',
  };
  return `<span class="badge ${map[status] || 'badge-navy'}">${status}</span>`;
}

/** Format a date string to locale */
function fmtDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

/** Initials from full name */
function initials(name = '') {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
}

/** Format currency (INR) */
function fmtCurrency(n) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n || 0);
}

/** Debounce helper for search inputs */
function debounce(fn, delay = 350) {
  let timer;
  return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), delay); };
}

/** Populate a <select> element from an array */
function populateSelect(selectEl, items, valueFn, labelFn, placeholder = 'Select…') {
  selectEl.innerHTML = `<option value="">— ${placeholder} —</option>`;
  items.forEach(item => {
    const opt = document.createElement('option');
    opt.value = valueFn(item);
    opt.textContent = labelFn(item);
    selectEl.appendChild(opt);
  });
}

// ── Sidebar active link ────────────────────────────────────────────
function initSidebar() {
  const current = window.location.pathname.split('/').pop();
  document.querySelectorAll('.nav-item[data-page]').forEach(item => {
    if (item.dataset.page === current) item.classList.add('active');
    item.addEventListener('click', () => {
      window.location.href = item.dataset.page;
    });
  });

  // User info
  const userName = document.getElementById('sidebar-username');
  const userRole = document.getElementById('sidebar-role');
  const userAv   = document.getElementById('sidebar-avatar');
  if (userName) userName.textContent = Auth.getUser() || 'User';
  if (userRole) userRole.textContent = Auth.getRole() || '';
  if (userAv)   userAv.textContent   = initials(Auth.getUser() || 'U');

  // Hide User Management from non-admins
  const usersNav = document.getElementById('users-nav-item');
  if (usersNav && Auth.getRole() !== 'ADMIN') {
    usersNav.style.display = 'none';
  }

  // Hamburger toggle
  const ham  = document.getElementById('hamburger');
  const side = document.getElementById('sidebar');
  ham?.addEventListener('click', () => side?.classList.toggle('open'));

  // Logout
  document.getElementById('logout-btn')?.addEventListener('click', () => {
    Auth.clear();
    window.location.href = '../index.html';
  });
}

// ── Tab switching ──────────────────────────────────────────────────
function initTabs(containerSelector = '.tabs') {
  document.querySelectorAll(containerSelector + ' .tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const target = btn.dataset.tab;
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById(target)?.classList.add('active');
    });
  });
}

// ── Close modal on overlay click ──────────────────────────────────
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('open');
  }
});
