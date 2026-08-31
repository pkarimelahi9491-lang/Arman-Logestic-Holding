// Arman Entekhab Fleet Management - Core Logic with Electron Desktop & Web Support

const STORAGE_KEYS = {
  DRIVERS: 'arman_fleet_drivers',
  ROUTES: 'arman_fleet_routes',
  AUDIT: 'arman_fleet_audit'
};

// Initial Default Seed Data
const INITIAL_DRIVERS = [
  { id: 1, code: 'D-101', name: 'علیرضا محمدی', personnelCode: '94812', vehicle: 'پژو پارس سفید', plate: 'ایران ۱۳ - ۴۸۵ س ۲۱', phone: '۰۹۱۳۱۱۱۴۴۵۵', active: true, trips: 28, totalAmount: 24500000 },
  { id: 2, code: 'D-102', name: 'حسین رضایی', personnelCode: '94815', vehicle: 'سمند سورن مشکی', plate: 'ایران ۱۳ - ۷۱۲ ب ۳۴', phone: '۰۹۱۳۲۲۲۳۳۴۴', active: true, trips: 34, totalAmount: 31200000 },
  { id: 3, code: 'D-103', name: 'مهدی کاظمی', personnelCode: '94820', vehicle: 'تارا خاکستری', plate: 'ایران ۱۳ - ۹۱۸ ج ۵۵', phone: '۰۹۱۳۳۳۳۷۷۸۸', active: true, trips: 22, totalAmount: 18900000 },
  { id: 4, code: 'D-104', name: 'رضا حسینی', personnelCode: '94829', vehicle: 'پژو ۴۰۵ نقره‌ای', plate: 'ایران ۱۳ - ۱۶۳ د ۶۷', phone: '۰۹۱۳۴۴۴۵۵۶۶', active: true, trips: 30, totalAmount: 27800000 },
  { id: 5, code: 'D-105', name: 'علی اکبری', personnelCode: '94833', vehicle: 'دنا پلاس سفید', plate: 'ایران ۱۳ - ۸۴۱ ق ۱۲', phone: '۰۹۱۳۵۵۵۹۹۰۰', active: true, trips: 26, totalAmount: 23600000 }
];

const INITIAL_ROUTES = [
  { id: 1, code: 'R-101', origin: 'کارخانه اصفهان', destination: 'دفتر مرکزی تهران', price: 850000, description: 'مسیر مستقیم - شیفت روز' },
  { id: 2, code: 'R-102', origin: 'دفتر مرکزی تهران', destination: 'انبار مرکزی کرج', price: 450000, description: 'انتقال کالا و مدارک' },
  { id: 3, code: 'R-103', origin: 'کارخانه مورچه خورت', destination: 'دفتر اصفهان', price: 320000, description: 'تردد اداری پرسنل' },
  { id: 4, code: 'R-104', origin: 'انبار مرکزی کرج', destination: 'شهرک صنعتی کاسپین', price: 620000, description: 'مسیر ویژه تأمین قطعات' },
  { id: 5, code: 'R-105', origin: 'فرودگاه مهرآباد', destination: 'دفتر مرکزی تهران', price: 280000, description: 'ترانسفر مهمانان هلدینگ' }
];

const INITIAL_AUDIT = [
  { id: 101, timestamp: '1403/06/01 08:30', user: 'مدیر ارشد هلدینگ', action: 'قفل نرخ‌ها', detail: 'قفل‌گذاری و تایید نهایی نرخ‌های پایه شهریورماه' },
  { id: 102, timestamp: '1403/06/02 11:15', user: 'سرپرست ناوگان', action: 'ثبت راننده', detail: 'افزودن راننده جدید علیرضا محمدی با کد D-101' },
  { id: 103, timestamp: '1403/06/05 14:00', user: 'امور مالی', action: 'محاسبه تسویه', detail: 'صدور فیش‌های تسویه میاندوره رانندگان ناوگان' }
];

// App State
let drivers = INITIAL_DRIVERS;
let routes = INITIAL_ROUTES;
let auditLogs = INITIAL_AUDIT;
let isElectron = typeof window.electronAPI !== 'undefined' && window.electronAPI.isElectron;

// Database Persistence layer
async function initDatabase() {
  if (isElectron) {
    try {
      const desktopData = await window.electronAPI.loadDatabase();
      if (desktopData && desktopData.drivers && desktopData.routes) {
        drivers = desktopData.drivers;
        routes = desktopData.routes;
        auditLogs = desktopData.auditLogs || [];
        console.log("Loaded state from Electron desktop database file.");
      } else {
        // First run in Electron: persist initial state
        await window.electronAPI.saveDatabase({ drivers, routes, auditLogs });
      }
    } catch (e) {
      console.error("Error loading Electron DB, falling back to LocalStorage:", e);
      loadFromLocalStorage();
    }
  } else {
    loadFromLocalStorage();
  }

  updatePlatformUI();
  renderAll();
}

function loadFromLocalStorage() {
  const localDrivers = localStorage.getItem(STORAGE_KEYS.DRIVERS);
  const localRoutes = localStorage.getItem(STORAGE_KEYS.ROUTES);
  const localAudit = localStorage.getItem(STORAGE_KEYS.AUDIT);

  if (localDrivers) drivers = JSON.parse(localDrivers);
  if (localRoutes) routes = JSON.parse(localRoutes);
  if (localAudit) auditLogs = JSON.parse(localAudit);
}

async function saveState() {
  // Always save to LocalStorage
  localStorage.setItem(STORAGE_KEYS.DRIVERS, JSON.stringify(drivers));
  localStorage.setItem(STORAGE_KEYS.ROUTES, JSON.stringify(routes));
  localStorage.setItem(STORAGE_KEYS.AUDIT, JSON.stringify(auditLogs));

  // If running in Desktop EXE (Electron), persist to AppData filesystem
  if (isElectron) {
    try {
      await window.electronAPI.saveDatabase({ drivers, routes, auditLogs });
    } catch (e) {
      console.error("Failed to save to Electron database file:", e);
    }
  }
}

// Helpers
function formatMoney(amount) {
  return Number(amount || 0).toLocaleString('fa-IR');
}

function toPersianDigits(str) {
  const persianDigits = ['۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'];
  return String(str).replace(/[0-9]/g, w => persianDigits[+w]);
}

// Update UI platform badge
function updatePlatformUI() {
  const platformBadge = document.getElementById('platformBadge');
  if (platformBadge) {
    if (isElectron) {
      platformBadge.innerHTML = `<i class="fa-brands fa-windows" style="color:#60a5fa;"></i> <span>نسخه دسکتاپ ویندوز (EXE)</span>`;
      platformBadge.style.background = 'rgba(59, 130, 246, 0.2)';
      platformBadge.style.borderColor = 'rgba(59, 130, 246, 0.4)';
    } else {
      platformBadge.innerHTML = `<i class="fa-solid fa-globe" style="color:#34d399;"></i> <span>نسخه تحت وب (GitHub Pages)</span>`;
      platformBadge.style.background = 'rgba(16, 185, 129, 0.15)';
      platformBadge.style.borderColor = 'rgba(16, 185, 129, 0.35)';
    }
  }
}

// Tab Switching
function switchTab(tabId) {
  document.querySelectorAll('.nav-tab').forEach(tab => {
    tab.classList.toggle('active', tab.dataset.tab === tabId);
  });
  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.toggle('active', pane.id === `tab-${tabId}`);
  });
}

document.querySelectorAll('.nav-tab').forEach(tab => {
  tab.addEventListener('click', () => switchTab(tab.dataset.tab));
});

// Render All Components
function renderAll() {
  renderStats();
  renderRoutes();
  renderDrivers();
  renderFinance();
  renderAudit();
}

// Render Stats
function renderStats() {
  const activeCount = drivers.filter(d => d.active).length;
  document.getElementById('statActiveDrivers').innerText = `${toPersianDigits(activeCount)} نفر`;
  document.getElementById('statTotalRoutes').innerText = `${toPersianDigits(routes.length)} مسیر`;
  
  const totalTrips = drivers.reduce((acc, d) => acc + (d.trips || 0), 0);
  document.getElementById('statMonthlyTrips').innerText = `${toPersianDigits(totalTrips)} سفر`;

  const totalAmount = drivers.reduce((acc, d) => acc + (d.totalAmount || 0), 0);
  document.getElementById('statTotalHoldingsAmount').innerText = formatMoney(totalAmount);
  document.getElementById('financeTotalPayable').innerText = `${formatMoney(totalAmount)} تومان`;
}

// Render Routes Table
function renderRoutes(filterText = '') {
  const tbody = document.getElementById('routesTableBody');
  tbody.innerHTML = '';

  const filtered = routes.filter(r => 
    r.code.toLowerCase().includes(filterText.toLowerCase()) ||
    r.origin.includes(filterText) ||
    r.destination.includes(filterText)
  );

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:30px; color:#94a3b8;">موردی یافت نشد.</td></tr>`;
    return;
  }

  filtered.forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${r.code}</strong></td>
      <td>${r.origin}</td>
      <td>${r.destination}</td>
      <td><span style="color:#34d399; font-weight:bold;">${formatMoney(r.price)}</span></td>
      <td><small style="color:#94a3b8;">${r.description || '-'}</small></td>
      <td>
        <button class="btn-secondary" style="padding:4px 10px; font-size:11px;" onclick="editRoute(${r.id})">
          <i class="fa-solid fa-pen"></i> ویرایش
        </button>
        <button class="btn-secondary" style="padding:4px 10px; font-size:11px; color:#f87171;" onclick="deleteRoute(${r.id})">
          <i class="fa-solid fa-trash"></i>
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// Render Drivers Table
function renderDrivers(filterText = '') {
  const tbody = document.getElementById('driversTableBody');
  tbody.innerHTML = '';

  const filtered = drivers.filter(d => 
    d.name.includes(filterText) ||
    d.code.toLowerCase().includes(filterText.toLowerCase()) ||
    d.personnelCode.includes(filterText)
  );

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:30px; color:#94a3b8;">راننده‌ای یافت نشد.</td></tr>`;
    return;
  }

  filtered.forEach(d => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${d.code}</strong></td>
      <td>${d.name}</td>
      <td>${toPersianDigits(d.personnelCode)}</td>
      <td>${d.vehicle} (${toPersianDigits(d.plate)})</td>
      <td>${toPersianDigits(d.phone)}</td>
      <td>
        <span style="display:inline-flex; align-items:center; gap:6px; padding:4px 10px; border-radius:999px; font-size:11px; background:${d.active ? 'rgba(16,185,129,0.15)' : 'rgba(239,68,68,0.15)'}; color:${d.active ? '#34d399' : '#f87171'};">
          <span style="width:6px; height:6px; border-radius:50%; background:${d.active ? '#10b981' : '#ef4444'};"></span>
          ${d.active ? 'فعال' : 'غیرفعال'}
        </span>
      </td>
      <td>
        <button class="btn-secondary" style="padding:4px 8px; font-size:11px;" onclick="toggleDriverActive(${d.id})">
          ${d.active ? 'غیرفعال‌سازی' : 'فعال‌سازی'}
        </button>
        <button class="btn-secondary" style="padding:4px 8px; font-size:11px;" onclick="editDriver(${d.id})">
          <i class="fa-solid fa-pen"></i>
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// Render Finance Table
function renderFinance() {
  const tbody = document.getElementById('financeTableBody');
  tbody.innerHTML = '';

  drivers.forEach(d => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${d.code}</td>
      <td><strong>${d.name}</strong></td>
      <td>${toPersianDigits(d.personnelCode)}</td>
      <td>${toPersianDigits(24)} روز</td>
      <td>${toPersianDigits(d.trips || 25)} مأموریت</td>
      <td><strong style="color:#6ee7b7;">${formatMoney(d.totalAmount || 20000000)}</strong></td>
      <td>
        <span style="background:rgba(59,130,246,0.15); color:#93c5fd; padding:4px 10px; border-radius:999px; font-size:11px;">
          تأیید شده / آماده واریز
        </span>
      </td>
      <td>
        <button class="btn-secondary" style="padding:4px 10px; font-size:11px;" onclick="showSlip(${d.id})">
          <i class="fa-solid fa-receipt"></i> مشاهده فیش
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// Render Audit Logs
function renderAudit() {
  const tbody = document.getElementById('auditTableBody');
  tbody.innerHTML = '';

  auditLogs.forEach(log => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>#${log.id}</td>
      <td><small style="color:#94a3b8;">${toPersianDigits(log.timestamp)}</small></td>
      <td><strong>${log.user}</strong></td>
      <td><span style="background:rgba(255,255,255,0.08); padding:3px 8px; border-radius:6px; font-size:11px;">${log.action}</span></td>
      <td>${log.detail}</td>
    `;
    tbody.appendChild(tr);
  });
}

// Modals
function openModal(id) { document.getElementById(id).classList.add('active'); }
function closeModal(id) { document.getElementById(id).classList.remove('active'); }

function openAddRouteModal() {
  document.getElementById('routeModalTitle').innerText = 'ثبت مسیر مصوب جدید';
  document.getElementById('routeForm').reset();
  document.getElementById('routeFormId').value = '';
  openModal('routeModal');
}

function editRoute(id) {
  const r = routes.find(x => x.id === id);
  if (!r) return;
  document.getElementById('routeModalTitle').innerText = 'ویرایش مسیر مصوب';
  document.getElementById('routeFormId').value = r.id;
  document.getElementById('routeCodeInput').value = r.code;
  document.getElementById('routeOriginInput').value = r.origin;
  document.getElementById('routeDestInput').value = r.destination;
  document.getElementById('routePriceInput').value = r.price;
  document.getElementById('routeDescInput').value = r.description || '';
  openModal('routeModal');
}

async function handleSaveRoute(e) {
  e.preventDefault();
  const idVal = document.getElementById('routeFormId').value;
  const newRoute = {
    id: idVal ? parseInt(idVal) : Date.now(),
    code: document.getElementById('routeCodeInput').value.trim(),
    origin: document.getElementById('routeOriginInput').value.trim(),
    destination: document.getElementById('routeDestInput').value.trim(),
    price: parseInt(document.getElementById('routePriceInput').value),
    description: document.getElementById('routeDescInput').value.trim()
  };

  if (idVal) {
    const idx = routes.findIndex(x => x.id === parseInt(idVal));
    if (idx !== -1) routes[idx] = newRoute;
  } else {
    routes.push(newRoute);
  }

  auditLogs.unshift({
    id: Date.now() % 10000,
    timestamp: 'هم اکنون',
    user: 'مدیریت ناوگان',
    action: idVal ? 'ویرایش مسیر' : 'افزودن مسیر',
    detail: `مسیر ${newRoute.code} (${newRoute.origin} به ${newRoute.destination}) با نرخ ${formatMoney(newRoute.price)} تومان`
  });

  await saveState();
  renderRoutes();
  renderStats();
  renderAudit();
  closeModal('routeModal');
}

async function deleteRoute(id) {
  if (confirm('آیا از حذف این نرخ مصوب اطمینان دارید؟')) {
    routes = routes.filter(r => r.id !== id);
    await saveState();
    renderRoutes();
    renderStats();
  }
}

function openAddDriverModal() {
  document.getElementById('driverModalTitle').innerText = 'ثبت راننده جدید ناوگان';
  document.getElementById('driverForm').reset();
  document.getElementById('driverFormId').value = '';
  openModal('driverModal');
}

function editDriver(id) {
  const d = drivers.find(x => x.id === id);
  if (!d) return;
  document.getElementById('driverModalTitle').innerText = 'ویرایش مشخصات راننده';
  document.getElementById('driverFormId').value = d.id;
  document.getElementById('driverNameInput').value = d.name;
  document.getElementById('driverCodeInput').value = d.code;
  document.getElementById('driverPersonnelInput').value = d.personnelCode;
  document.getElementById('driverPhoneInput').value = d.phone;
  document.getElementById('driverVehicleInput').value = d.vehicle;
  document.getElementById('driverPlateInput').value = d.plate;
  openModal('driverModal');
}

async function handleSaveDriver(e) {
  e.preventDefault();
  const idVal = document.getElementById('driverFormId').value;
  const newDriver = {
    id: idVal ? parseInt(idVal) : Date.now(),
    code: document.getElementById('driverCodeInput').value.trim(),
    name: document.getElementById('driverNameInput').value.trim(),
    personnelCode: document.getElementById('driverPersonnelInput').value.trim(),
    phone: document.getElementById('driverPhoneInput').value.trim(),
    vehicle: document.getElementById('driverVehicleInput').value.trim(),
    plate: document.getElementById('driverPlateInput').value.trim(),
    active: true,
    trips: idVal ? (drivers.find(x => x.id === parseInt(idVal))?.trips || 25) : 0,
    totalAmount: idVal ? (drivers.find(x => x.id === parseInt(idVal))?.totalAmount || 0) : 0
  };

  if (idVal) {
    const idx = drivers.findIndex(x => x.id === parseInt(idVal));
    if (idx !== -1) drivers[idx] = newDriver;
  } else {
    drivers.push(newDriver);
  }

  await saveState();
  renderDrivers();
  renderFinance();
  renderStats();
  closeModal('driverModal');
}

async function toggleDriverActive(id) {
  const d = drivers.find(x => x.id === id);
  if (d) {
    d.active = !d.active;
    await saveState();
    renderDrivers();
    renderStats();
  }
}

// CSV Export for Finance (Compatible with Excel & Electron Save Dialog)
async function exportFinanceCsv() {
  let csvContent = '\uFEFFکد راننده,نام راننده,کد پرسنلی,تعداد مأموریت,جمع کل کارکرد به تومان\n';
  drivers.forEach(d => {
    csvContent += `"${d.code}","${d.name}","${d.personnelCode}",${d.trips || 20},${d.totalAmount || 18000000}\n`;
  });

  const defaultFilename = `Holding_Arman_Settlement_${new Date().toISOString().slice(0,10)}.csv`;

  if (isElectron) {
    const res = await window.electronAPI.exportFile({
      defaultName: defaultFilename,
      content: csvContent,
      filters: [{ name: 'CSV Excel File', extensions: ['csv'] }]
    });
    if (res.success) {
      alert(`فایل اکسل تسویه مالی با موفقیت در مسیر زیر ذخیره شد:\n${res.filePath}`);
    }
  } else {
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', defaultFilename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}

// Full Database Backup Export (JSON)
async function exportFullDatabaseBackup() {
  const backupData = {
    appName: "Arman Entekhab Fleet Admin",
    version: "1.0.0",
    exportDate: new Date().toISOString(),
    drivers,
    routes,
    auditLogs
  };
  const jsonStr = JSON.stringify(backupData, null, 2);
  const defaultFilename = `Arman_Fleet_Backup_${new Date().toISOString().slice(0,10)}.json`;

  if (isElectron) {
    const res = await window.electronAPI.exportFile({
      defaultName: defaultFilename,
      content: jsonStr,
      filters: [{ name: 'JSON Backup File', extensions: ['json'] }]
    });
    if (res.success) {
      alert(`پشتیبان کامل پایگاه‌داده با موفقیت در سیستم ذخیره گردید:\n${res.filePath}`);
    }
  } else {
    const blob = new Blob([jsonStr], { type: 'application/json;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', defaultFilename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}

// Full Database Restore (JSON)
async function importFullDatabaseBackup() {
  if (isElectron) {
    const res = await window.electronAPI.importFile({
      filters: [{ name: 'JSON Backup', extensions: ['json'] }]
    });
    if (res.success && res.content) {
      try {
        const parsed = JSON.parse(res.content);
        if (parsed.drivers && parsed.routes) {
          drivers = parsed.drivers;
          routes = parsed.routes;
          auditLogs = parsed.auditLogs || [];
          await saveState();
          renderAll();
          alert('بازیابی پایگاه‌داده با موفقیت انجام شد.');
        } else {
          alert('فرمت فایل پشتیبان نامعتبر است.');
        }
      } catch (e) {
        alert('خطا در خواندن فایل JSON.');
      }
    }
  } else {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = (e) => {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = async (event) => {
        try {
          const parsed = JSON.parse(event.target.result);
          if (parsed.drivers && parsed.routes) {
            drivers = parsed.drivers;
            routes = parsed.routes;
            auditLogs = parsed.auditLogs || [];
            await saveState();
            renderAll();
            alert('بازیابی اطلاعات با موفقیت انجام شد.');
          }
        } catch (err) {
          alert('خطا در پردازش فایل پشتیبان.');
        }
      };
      reader.readAsText(file);
    };
    input.click();
  }
}

// CSV Import for Routes
function openCsvModal() {
  openModal('csvModal');
}

async function handleImportCsv() {
  const text = document.getElementById('csvTextarea').value;
  const lines = text.split('\n');
  let addedCount = 0;

  lines.forEach(line => {
    const parts = line.split(/[,،]/);
    if (parts.length >= 4) {
      const code = parts[0].trim();
      const origin = parts[1].trim();
      const destination = parts[2].trim();
      const price = parseInt(parts[3].replace(/[^0-9]/g, ''));
      const desc = parts[4] ? parts[4].trim() : '';

      if (code && origin && destination && price) {
        routes.push({
          id: Date.now() + Math.floor(Math.random() * 1000),
          code, origin, destination, price, description: desc
        });
        addedCount++;
      }
    }
  });

  if (addedCount > 0) {
    await saveState();
    renderRoutes();
    renderStats();
    closeModal('csvModal');
    alert(`تعداد ${addedCount} مسیر با موفقیت به ماتریس نرخ‌ها اضافه گردید.`);
  } else {
    alert('فرمت فایل نامعتبر است.');
  }
}

// Show Driver Slip & Native Print
function showSlip(driverId) {
  const d = drivers.find(x => x.id === driverId);
  if (!d) return;

  const html = `
    <div style="text-align:center; border-bottom:1px solid rgba(255,255,255,0.15); padding-bottom:12px; margin-bottom:12px;">
      <h3 style="margin:0; font-size:17px; color:#f8fafc;">هلدینگ آرمان انتخاب</h3>
      <p style="margin:4px 0 0 0; color:#94a3b8; font-size:12px;">فیش تسویه کارکرد راننده - دوره شهریور ۱۴۰۳</p>
    </div>
    <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
      <div><strong>نام راننده:</strong> ${d.name}</div>
      <div><strong>کد پرسنلی:</strong> ${toPersianDigits(d.personnelCode)}</div>
      <div><strong>کد راننده:</strong> ${d.code}</div>
      <div><strong>خودرو:</strong> ${d.vehicle}</div>
      <div><strong>شماره تماس:</strong> ${toPersianDigits(d.phone)}</div>
      <div><strong>تعداد سفر:</strong> ${toPersianDigits(d.trips || 24)} سفر</div>
    </div>
    <div style="margin-top:16px; background:rgba(16,185,129,0.15); border:1px solid #10b981; border-radius:10px; padding:12px; text-align:center;">
      <span style="font-size:12px; color:#a7f3d0;">مبلغ خالص قابل پرداخت:</span>
      <h2 style="color:#ecfdf5; margin:6px 0 0 0;">${formatMoney(d.totalAmount || 20000000)} تومان</h2>
    </div>
  `;

  document.getElementById('slipContent').innerHTML = html;
  openModal('slipModal');
}

function triggerPrint() {
  if (isElectron) {
    window.electronAPI.printPage();
  } else {
    window.print();
  }
}

// Search Inputs
document.getElementById('routeSearchInput')?.addEventListener('input', (e) => {
  renderRoutes(e.target.value);
});

document.getElementById('driverSearchInput')?.addEventListener('input', (e) => {
  renderDrivers(e.target.value);
});

// Bind Backup / Restore buttons
document.getElementById('exportAllJsonBtn')?.addEventListener('click', exportFullDatabaseBackup);
document.getElementById('importAllJsonBtn')?.addEventListener('click', importFullDatabaseBackup);

// Initialization on DOM Ready
window.addEventListener('DOMContentLoaded', () => {
  initDatabase();
});
