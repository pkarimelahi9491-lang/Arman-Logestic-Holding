const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('path');
const fs = require('fs');

let mainWindow;
const dbFilePath = () => path.join(app.getPath('userData'), 'fleet_database.json');
const dbBackupPath = () => path.join(app.getPath('userData'), 'fleet_database.bak');

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1320,
    height: 880,
    minWidth: 1024,
    minHeight: 720,
    title: "سامانه یکپارچه مأموریت و ناوگان - هلدینگ آرمان انتخاب (نسخه دسکتاپ ویندوز)",
    backgroundColor: "#07101E",
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: false
    }
  });

  mainWindow.loadFile(path.join(__dirname, 'index.html'));
  mainWindow.setMenuBarVisibility(false);
}

// Database IPC Handlers
ipcMain.handle('db:getPath', async () => {
  return dbFilePath();
});

ipcMain.handle('db:load', async () => {
  try {
    const file = dbFilePath();
    if (fs.existsSync(file)) {
      const data = fs.readFileSync(file, 'utf8');
      return JSON.parse(data);
    }
    return null; // Signals client to use initial seed data
  } catch (err) {
    console.error("Error loading database file:", err);
    return null;
  }
});

ipcMain.handle('db:save', async (event, data) => {
  try {
    const file = dbFilePath();
    const backup = dbBackupPath();
    const jsonStr = JSON.stringify(data, null, 2);

    // Create backup if main exists
    if (fs.existsSync(file)) {
      fs.copyFileSync(file, backup);
    }

    fs.writeFileSync(file, jsonStr, 'utf8');
    return { success: true, path: file };
  } catch (err) {
    console.error("Error saving database file:", err);
    return { success: false, error: err.message };
  }
});

// File Dialogs & System Handlers
ipcMain.handle('file:export', async (event, { defaultName, content, filters }) => {
  try {
    const { canceled, filePath } = await dialog.showSaveDialog(mainWindow, {
      title: 'ذخیره خروجی فایل',
      defaultPath: defaultName || 'export.csv',
      filters: filters || [
        { name: 'CSV Excel File', extensions: ['csv'] },
        { name: 'JSON Backup', extensions: ['json'] },
        { name: 'All Files', extensions: ['*'] }
      ]
    });

    if (canceled || !filePath) return { canceled: true };

    fs.writeFileSync(filePath, content, 'utf8');
    return { success: true, filePath };
  } catch (err) {
    console.error("Export file error:", err);
    return { success: false, error: err.message };
  }
});

ipcMain.handle('file:import', async (event, { filters }) => {
  try {
    const { canceled, filePaths } = await dialog.showOpenDialog(mainWindow, {
      title: 'انتخاب فایل جهت ورود اطلاعات',
      properties: ['openFile'],
      filters: filters || [
        { name: 'Supported Files', extensions: ['csv', 'json', 'txt'] },
        { name: 'All Files', extensions: ['*'] }
      ]
    });

    if (canceled || !filePaths || filePaths.length === 0) return { canceled: true };

    const content = fs.readFileSync(filePaths[0], 'utf8');
    return { success: true, content, filePath: filePaths[0] };
  } catch (err) {
    console.error("Import file error:", err);
    return { success: false, error: err.message };
  }
});

ipcMain.handle('system:print', async () => {
  if (mainWindow) {
    mainWindow.webContents.print({ silent: false, printBackground: true });
    return true;
  }
  return false;
});

ipcMain.handle('system:version', () => app.getVersion());

app.whenReady().then(() => {
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});
