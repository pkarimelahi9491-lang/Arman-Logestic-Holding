const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
  isElectron: true,
  
  // Database Operations
  loadDatabase: () => ipcRenderer.invoke('db:load'),
  saveDatabase: (data) => ipcRenderer.invoke('db:save', data),
  getDatabasePath: () => ipcRenderer.invoke('db:getPath'),

  // Native Dialogs
  showSaveDialog: (options) => ipcRenderer.invoke('dialog:save', options),
  showOpenDialog: (options) => ipcRenderer.invoke('dialog:open', options),

  // File Exports & Imports
  exportFile: (options) => ipcRenderer.invoke('file:export', options),
  importFile: (options) => ipcRenderer.invoke('file:import', options),

  // System
  printPage: () => ipcRenderer.invoke('system:print'),
  getAppVersion: () => ipcRenderer.invoke('system:version')
});
