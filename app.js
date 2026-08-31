// Arman Entekhab Fleet Management Web App Engine
const STORAGE_KEYS = {
  DRIVERS: 'arman_web_drivers',
  ROUTES: 'arman_web_routes',
  WORK_LOGS: 'arman_web_work_logs',
  AUDIT: 'arman_web_audit'
};

// Seed Data
const INITIAL_DRIVERS = [
  { id: 'drv-101', code: 'D-101', name: 'علی رضایی', personnelCode: 'AE-84012', vehicle: 'پژو پارس سفید', plate: '۱۲ ج ۳۴۵ ایران ۲۲', phone: '09121112233', active: true },
  { id: 'drv-102', code: 'D-102', name: 'رضا محمدی', personnelCode: 'AE-84019', vehicle: 'سمند سورن پلاس خاکستری', plate: '۷۷ د ۸۹۱ ایران ۱۱', phone: '09123334455', active: true },
  { id: 'drv-103', code: 'D-103', name: 'حسین کریمی', personnelCode: 'AE-84033', vehicle: 'تارا اتوماتیک مشکی', plate: '۴۵ ط ۶۱۲ ایران ۳۳', phone: '09125556677', active: true },
  { id: 'drv-104', code: 'D-104', name: 'محمد احمدی', personnelCode: 'AE-84048', vehicle: 'دنا پلاس توربو سفید', plate: '۳۳ ب ۷۴۵ ایران ۴۴', phone: '09127778899', active: true }
];

const INITIAL_ROUTES = [
  { id: 'r-1', code: 'THR-ESF-01', origin: 'کارخانه اصفهان', destination: 'دفتر مرکزی تهران', price: 9500000, description: 'مسیر مستقیم اداری' },
  { id: 'r-2', code: 'THR-KRJ-01', origin: 'دفتر مرکزی تهران', destination: 'انبار مرکزی کرج', price: 2800000, description: 'تردد روزانه لجستیک' },
  { id: 'r-3', code: 'ESF-MRCH-01', origin: 'کارخانه مورچه خورت', destination: 'دفتر منطقه‌ای اصفهان', price: 1950000, description: 'سرویس اداری مهندسی' },
  { id: 'r-4', code: 'THR-IKA-01', origin: 'فرودگاه امام خمینی (ره)', destination: 'دفتر مرکزی تهران', price: 3200000, description: 'ترانسفر مهمانان خارجی و مدیران' },
  { id: 'r-5', code: 'THR-QOM-01', origin: 'دفتر مرکزی تهران', destination: 'کارخانه قم', price: 4200000, description: 'بازدید خطوط تولید' }
];

const INITIAL_WORK_LOGS = [
  {
    id: 'log-1',
    driverId: 'drv-101',
    driverName: 'علی رضایی',
    dateJalali: '1403/06/10',
    origin: 'کارخانه اصفهان',
    destination: 'دفتر مرکزی تهران',
    routeCode: 'THR-ESF-01',
    fareAmount: 9500000,
    overtimeHours: 2,
    overtimeAmount: 500000,
    totalAmount: 10000000,
    status: 'APPROVED', // PENDING, APPROVED, REJECTED
    rejectionReason: '',
    createdAt: '1403/06/10 09:30'
  },
  {
    id: 'log-2',
    driverId: 'drv-101',
    driverName: 'علی رضایی',
    dateJalali: '1403/06/11',
    origin: 'دفتر مرکزی تهران',
    destination: 'انبار مرکزی کرج',
    routeCode: 'THR-KRJ-01',
    fareAmount: 2800000,
    overtimeHours: 0,
    overtimeAmount: 0,
    totalAmount: 2800000,
    status: 'PENDING',
    rejectionReason: '',
    createdAt: '1403/06/11 11:15'
  }
];

// App State
let drivers = [];
let routes = [];
let workLogs = [];
let currentRole = 'driver'; // 'driver' or 'admin'
let selectedDriverId = 'drv-101';

// Init
document.addEventListener('DOMContentLoaded', () => {
  loadData();
  setupEventListeners();
  renderAll();
});

function loadData() {
  const savedDrivers = localStorage.getItem(STORAGE_KEYS.DRIVERS);
  const savedRoutes = localStorage.getItem(STORAGE_KEYS.ROUTES);
  const savedLogs = localStorage.getItem(STORAGE_KEYS.WORK_LOGS);

  drivers = savedDrivers ? JSON.parse(savedDrivers) : INITIAL_DRIVERS;
  routes = savedRoutes ? JSON.parse(savedRoutes) : INITIAL_ROUTES;
  workLogs = savedLogs ? JSON.parse(savedLogs) : INITIAL_WORK_LOGS;

  if (drivers.length > 0 && !selectedDriverId) {
    selectedDriverId = drivers[0].id;
  }
}

function saveData() {
  localStorage.setItem(STORAGE_KEYS.DRIVERS, JSON.stringify(drivers));
  localStorage.setItem(STORAGE_KEYS.ROUTES, JSON.stringify(routes));
  localStorage.setItem(STORAGE_KEYS.WORK_LOGS, JSON.stringify(workLogs));
}

function formatMoney(num) {
  return Number(num || 0).toLocaleString('fa-IR') + ' ریال';
}

function formatToman(num) {
  const toman = Math.round(Number(num || 0) / 10);
  return Number(toman).toLocaleString('fa-IR') + ' تومان';
}

function setupEventListeners() {
  // Portal switcher
  document.getElementById('btnRoleDriver').addEventListener('click', () => switchRole('driver'));
  document.getElementById('btnRoleAdmin').addEventListener('click', () => switchRole('admin'));

  // Nav tabs
  document.querySelectorAll('.nav-tab').forEach(tab => {
    tab.addEventListener('click', (e) => {
      document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
      
      tab.classList.add('active');
      const target = tab.getAttribute('data-tab');
      document.getElementById('tab-' + target).classList.add('active');
    });
  });

  // Driver Selector
  const driverSelect = document.getElementById('driverSelect');
  if (driverSelect) {
    driverSelect.addEventListener('change', (e) => {
      selectedDriverId = e.target.value;
      renderDriverPortal();
    });
  }

  // Route selector in driver form to auto fill price
  const routeSelect = document.getElementById('logRouteSelect');
  if (routeSelect) {
    routeSelect.addEventListener('change', (e) => {
      const selectedRoute = routes.find(r => r.code === e.target.value);
      if (selectedRoute) {
        document.getElementById('logOrigin').value = selectedRoute.origin;
        document.getElementById('logDestination').value = selectedRoute.destination;
        document.getElementById('logFare').value = selectedRoute.price;
        updateTripTotal();
      }
    });
  }

  const overtimeInput = document.getElementById('logOvertimeHours');
  if (overtimeInput) {
    overtimeInput.addEventListener('input', updateTripTotal);
  }

  // Reverse route button
  const reverseBtn = document.getElementById('btnReverseRoute');
  if (reverseBtn) {
    reverseBtn.addEventListener('click', () => {
      const origin = document.getElementById('logOrigin').value;
      const dest = document.getElementById('logDestination').value;
      document.getElementById('logOrigin').value = dest;
      document.getElementById('logDestination').value = origin;
    });
  }

  // Submit Trip Form
  const tripForm = document.getElementById('tripForm');
  if (tripForm) {
    tripForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const origin = document.getElementById('logOrigin').value.trim();
      const destination = document.getElementById('logDestination').value.trim();
      const fareAmount = Number(document.getElementById('logFare').value) || 0;
      const overtimeHours = Number(document.getElementById('logOvertimeHours').value) || 0;
      const dateJalali = document.getElementById('logDate').value.trim();
      const overtimeAmount = overtimeHours * 250000;
      const totalAmount = fareAmount + overtimeAmount;

      const driver = drivers.find(d => d.id === selectedDriverId);

      const newLog = {
        id: 'log-' + Date.now(),
        driverId: selectedDriverId,
        driverName: driver ? driver.name : 'راننده ناوگان',
        dateJalali: dateJalali || '1403/06/12',
        origin,
        destination,
        routeCode: document.getElementById('logRouteSelect').value || 'CUSTOM',
        fareAmount,
        overtimeHours,
        overtimeAmount,
        totalAmount,
        status: 'PENDING',
        rejectionReason: '',
        createdAt: new Date().toLocaleTimeString('fa-IR')
      };

      workLogs.unshift(newLog);
      saveData();
      renderAll();
      alert('سفر با موفقیت ثبت شد و در وضعیت در انتظار بررسی مدیر قرار گرفت.');
    });
  }

  // Add Route Form (Admin)
  const addRouteForm = document.getElementById('addRouteForm');
  if (addRouteForm) {
    addRouteForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const code = document.getElementById('routeCode').value.trim();
      const origin = document.getElementById('routeOrigin').value.trim();
      const destination = document.getElementById('routeDest').value.trim();
      const price = Number(document.getElementById('routePrice').value) || 0;
      const desc = document.getElementById('routeDesc').value.trim();

      routes.push({
        id: 'r-' + Date.now(),
        code,
        origin,
        destination,
        price,
        description: desc
      });

      saveData();
      renderRoutesTable();
      renderRouteSelects();
      addRouteForm.reset();
      alert('مسیر و تعرفه جدید با موفقیت در سیستم ثبت گردید.');
    });
  }

  // Add Driver Form (Admin)
  const addDriverForm = document.getElementById('addDriverForm');
  if (addDriverForm) {
    addDriverForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const name = document.getElementById('driverName').value.trim();
      const personnelCode = document.getElementById('driverPersonnel').value.trim();
      const vehicle = document.getElementById('driverVehicle').value.trim();
      const plate = document.getElementById('driverPlate').value.trim();
      const phone = document.getElementById('driverPhone').value.trim();

      drivers.push({
        id: 'drv-' + Date.now(),
        code: 'D-' + (100 + drivers.length + 1),
        name,
        personnelCode,
        vehicle,
        plate,
        phone,
        active: true
      });

      saveData();
      renderDriversTable();
      renderDriverSelects();
      addDriverForm.reset();
      alert('راننده جدید با موفقیت به ناوگان اضافه شد.');
    });
  }

  // Backup JSON download
  const backupBtn = document.getElementById('btnBackupData');
  if (backupBtn) {
    backupBtn.addEventListener('click', () => {
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify({ drivers, routes, workLogs }, null, 2));
      const downloadAnchor = document.createElement('a');
      downloadAnchor.setAttribute("href", dataStr);
      downloadAnchor.setAttribute("download", "entekhab_fleet_backup.json");
      document.body.appendChild(downloadAnchor);
      downloadAnchor.click();
      downloadAnchor.remove();
    });
  }
}

function updateTripTotal() {
  const fare = Number(document.getElementById('logFare').value) || 0;
  const hours = Number(document.getElementById('logOvertimeHours').value) || 0;
  const overtime = hours * 250000;
  const total = fare + overtime;
  document.getElementById('totalPreview').innerText = formatMoney(total) + ' (' + formatToman(total) + ')';
}

function switchRole(role) {
  currentRole = role;
  if (role === 'driver') {
    document.getElementById('btnRoleDriver').classList.add('active');
    document.getElementById('btnRoleAdmin').classList.remove('active');
    document.getElementById('portalDriver').style.display = 'block';
    document.getElementById('portalAdmin').style.display = 'none';
  } else {
    document.getElementById('btnRoleAdmin').classList.add('active');
    document.getElementById('btnRoleDriver').classList.remove('active');
    document.getElementById('portalDriver').style.display = 'none';
    document.getElementById('portalAdmin').style.display = 'block';
  }
}

function renderAll() {
  renderDriverSelects();
  renderRouteSelects();
  renderDriverPortal();
  renderAdminApprovalTable();
  renderRoutesTable();
  renderDriversTable();
  renderFinanceTable();
}

function renderDriverSelects() {
  const driverSelect = document.getElementById('driverSelect');
  if (!driverSelect) return;
  driverSelect.innerHTML = drivers.map(d => `<option value="${d.id}" ${d.id === selectedDriverId ? 'selected' : ''}>${d.name} (${d.vehicle})</option>`).join('');
}

function renderRouteSelects() {
  const routeSelect = document.getElementById('logRouteSelect');
  if (!routeSelect) return;
  routeSelect.innerHTML = '<option value="">-- انتخاب از مسیرهای مصوب --</option>' + routes.map(r => `<option value="${r.code}">${r.origin} ➔ ${r.destination} (${formatMoney(r.price)})</option>`).join('');
}

function renderDriverPortal() {
  const currentDriver = drivers.find(d => d.id === selectedDriverId);
  if (!currentDriver) return;

  document.getElementById('driverInfoBadge').innerText = `${currentDriver.name} | خودرو: ${currentDriver.vehicle} | پلاک: ${currentDriver.plate}`;

  const driverLogs = workLogs.filter(l => l.driverId === selectedDriverId);
  const totalFares = driverLogs.reduce((sum, l) => sum + (l.status === 'APPROVED' ? l.totalAmount : 0), 0);
  const pendingCount = driverLogs.filter(l => l.status === 'PENDING').length;
  const approvedCount = driverLogs.filter(l => l.status === 'APPROVED').length;

  document.getElementById('driverStatTrips').innerText = driverLogs.length + ' سفر';
  document.getElementById('driverStatIncome').innerText = formatToman(totalFares);
  document.getElementById('driverStatApproved').innerText = approvedCount + ' تأیید شده';
  document.getElementById('driverStatPending').innerText = pendingCount + ' در انتظار';

  const listContainer = document.getElementById('driverTripList');
  if (driverLogs.length === 0) {
    listContainer.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--text-muted);">هنوز سفری برای این راننده ثبت نشده است.</td></tr>';
    return;
  }

  listContainer.innerHTML = driverLogs.map(log => {
    let badgeClass = 'badge-pending';
    let badgeText = 'در انتظار بررسی';
    if (log.status === 'APPROVED') { badgeClass = 'badge-approved'; badgeText = 'تأیید شده'; }
    if (log.status === 'REJECTED') { badgeClass = 'badge-rejected'; badgeText = 'رد شده: ' + (log.rejectionReason || 'عدم تطابق'); }

    return `
      <tr>
        <td>${log.dateJalali}</td>
        <td><strong>${log.origin}</strong> ➔ <strong>${log.destination}</strong></td>
        <td>${log.overtimeHours > 0 ? log.overtimeHours + ' ساعت' : '-'}</td>
        <td><strong>${formatMoney(log.totalAmount)}</strong></td>
        <td><span class="badge ${badgeClass}">${badgeText}</span></td>
        <td>
          ${log.status === 'PENDING' ? `<button class="btn btn-outline btn-sm" onclick="deleteWorkLog('${log.id}')"><i class="fa-solid fa-trash"></i></button>` : '<i class="fa-solid fa-lock" style="color:var(--accent-gold);"></i> قفل'}
        </td>
      </tr>
    `;
  }).join('');
}

window.deleteWorkLog = function(id) {
  if (confirm('آیا از حذف این مأموریت اطمینان دارید؟')) {
    workLogs = workLogs.filter(l => l.id !== id);
    saveData();
    renderAll();
  }
};

function renderAdminApprovalTable() {
  const pendingContainer = document.getElementById('adminApprovalList');
  if (!pendingContainer) return;

  const pendingLogs = workLogs.filter(l => l.status === 'PENDING');
  document.getElementById('adminPendingCountBadge').innerText = pendingLogs.length;

  if (pendingLogs.length === 0) {
    pendingContainer.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:24px;">تمامی کارکردها بررسی و تعیین تکلیف شده‌اند. کارکرد جدیدی در صف بررسی نیست.</td></tr>';
    return;
  }

  pendingContainer.innerHTML = pendingLogs.map(log => `
    <tr>
      <td><strong>${log.driverName}</strong></td>
      <td>${log.dateJalali}</td>
      <td>${log.origin} ➔ ${log.destination}</td>
      <td>${log.overtimeHours > 0 ? log.overtimeHours + ' ساعت' : '-'}</td>
      <td><strong style="color:var(--accent-gold);">${formatMoney(log.totalAmount)}</strong></td>
      <td><span class="badge badge-pending">در انتظار</span></td>
      <td>
        <div style="display:flex;gap:6px;">
          <button class="btn btn-green btn-sm" onclick="approveLog('${log.id}')"><i class="fa-solid fa-check"></i> تأیید</button>
          <button class="btn btn-outline btn-sm" style="color:#f87171;" onclick="rejectLog('${log.id}')"><i class="fa-solid fa-xmark"></i> رد</button>
        </div>
      </td>
    </tr>
  `).join('');
}

window.approveLog = function(id) {
  const log = workLogs.find(l => l.id === id);
  if (log) {
    log.status = 'APPROVED';
    saveData();
    renderAll();
  }
};

window.rejectLog = function(id) {
  const reason = prompt('لطفاً علت رد کارکرد را وارد نمایید (مثلاً: مغایرت با برگه خروج، نرخ اشتباه):', 'مغایرت در زمان تردد');
  if (reason !== null) {
    const log = workLogs.find(l => l.id === id);
    if (log) {
      log.status = 'REJECTED';
      log.rejectionReason = reason;
      saveData();
      renderAll();
    }
  }
};

function renderRoutesTable() {
  const tbody = document.getElementById('routesList');
  if (!tbody) return;
  tbody.innerHTML = routes.map(r => `
    <tr>
      <td><code>${r.code}</code></td>
      <td><strong>${r.origin}</strong></td>
      <td><strong>${r.destination}</strong></td>
      <td><strong style="color:var(--accent-gold);">${formatMoney(r.price)}</strong></td>
      <td>${r.description || '-'}</td>
    </tr>
  `).join('');
}

function renderDriversTable() {
  const tbody = document.getElementById('driversList');
  if (!tbody) return;
  tbody.innerHTML = drivers.map(d => `
    <tr>
      <td><code>${d.code}</code></td>
      <td><strong>${d.name}</strong></td>
      <td>${d.personnelCode}</td>
      <td>${d.vehicle}</td>
      <td><span class="badge badge-active">${d.plate}</span></td>
      <td>${d.phone}</td>
    </tr>
  `).join('');
}

function renderFinanceTable() {
  const tbody = document.getElementById('financeList');
  if (!tbody) return;

  tbody.innerHTML = drivers.map(d => {
    const dLogs = workLogs.filter(l => l.driverId === d.id && l.status === 'APPROVED');
    const gross = dLogs.reduce((sum, l) => sum + l.totalAmount, 0);
    const tax = Math.round(gross * 0.05);
    const net = gross - tax;

    return `
      <tr>
        <td><strong>${d.name}</strong> (${d.code})</td>
        <td>${dLogs.length} سفر</td>
        <td>${formatMoney(gross)}</td>
        <td style="color:#f87171;">${formatMoney(tax)}</td>
        <td><strong style="color:#34d399;">${formatMoney(net)}</strong> <br><small style="color:var(--text-secondary);">${formatToman(net)}</small></td>
        <td>
          <button class="btn btn-primary btn-sm" onclick="alert('فیش تسویه با موفقیت برای راننده صادر شد.')"><i class="fa-solid fa-receipt"></i> صدور تسویه</button>
        </td>
      </tr>
    `;
  }).join('');
}
