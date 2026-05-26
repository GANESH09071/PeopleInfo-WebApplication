// PeopleInfo - Main JS

// Auto-dismiss alerts
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.alert').forEach(alert => {
    setTimeout(() => {
      alert.style.transition = 'opacity .5s';
      alert.style.opacity = '0';
      setTimeout(() => alert.remove(), 500);
    }, 4000);
  });

  // Active nav link
  const path = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(link => {
    if (link.getAttribute('href') === path) link.classList.add('active');
  });

  // Modal close on overlay click
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', e => {
      if (e.target === overlay) overlay.classList.remove('open');
    });
  });
});

function openModal(id) { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

function confirmDelete(formId) {
  if (confirm('Are you sure you want to delete this record?')) {
    document.getElementById(formId).submit();
  }
}

function setLeaveAction(leaveId, action) {
  document.getElementById('leaveId').value = leaveId;
  document.getElementById('leaveAction').value = action;
  document.getElementById('leaveActionTitle').textContent =
    action === 'approve' ? 'Approve Leave Request' : 'Reject Leave Request';
  document.getElementById('leaveSubmitBtn').className =
    'btn ' + (action === 'approve' ? 'btn-success' : 'btn-danger');
  document.getElementById('leaveSubmitBtn').textContent =
    action === 'approve' ? 'Approve' : 'Reject';
  openModal('leaveActionModal');
}

function submitLeaveAction() {
  const id = document.getElementById('leaveId').value;
  const action = document.getElementById('leaveAction').value;
  const comments = document.getElementById('leaveComments').value;
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = `/hr/leaves/${id}/${action}`;
  const c = document.createElement('input');
  c.type = 'hidden'; c.name = 'comments'; c.value = comments;
  form.appendChild(c);
  document.body.appendChild(form);
  form.submit();
}

// Live Timer Logic
document.addEventListener('DOMContentLoaded', () => {
  const timerElement = document.getElementById('liveTimer');
  if (!timerElement) return;

  const isTracking = timerElement.getAttribute('data-tracking') === 'true';
  const accumulatedSeconds = parseInt(timerElement.getAttribute('data-accumulated') || '0', 10);
  const lastCheckinStr = timerElement.getAttribute('data-last-checkin');

  let checkinTime = lastCheckinStr ? new Date(lastCheckinStr).getTime() : 0;

  function updateTimer() {
    let totalSeconds = accumulatedSeconds;
    if (isTracking && checkinTime > 0) {
      totalSeconds += Math.floor((Date.now() - checkinTime) / 1000);
    }
    
    const h = Math.floor(totalSeconds / 3600).toString().padStart(2, '0');
    const m = Math.floor((totalSeconds % 3600) / 60).toString().padStart(2, '0');
    const s = Math.floor(totalSeconds % 60).toString().padStart(2, '0');
    
    timerElement.textContent = `${h}:${m}:${s}`;
  }

  updateTimer();
  if (isTracking) {
    setInterval(updateTimer, 1000);
  }
});
