// src/store/useSmartHomeStore.ts
import { create } from "zustand";
import type { Device, Scenario } from "../types";
import {
  DEVICES_CONFIG,
  UST_TOKEN,
  fetchDeviceInfo,
  fetchDifference,
  sendSignal as apiSendSignal,
  publishSignals,
} from "../api/ujin";

interface SmartHomeState {
  devices: Device[];
  scenarios: Scenario[];
  logs: string[];
  lastSyncDt: number; // unix ts последнего pull (для devices/difference)
  isConnected: boolean;

  // Действия
  addLog: (log: string) => void;
  updateSignalLocal: (deviceId: string, signalName: string, value: any) => void;
  sendSignal: (deviceId: string, signalName: string, value: any) => Promise<void>;
  addDevice: (device: Device) => void;
  saveScenario: (scenario: Scenario) => void;
  executeScenarios: (deviceId: string, signalName: string, value: any) => void;

  // API
  initFromApi: () => Promise<void>;   // первичная загрузка всех устройств
  pollDifference: () => Promise<void>; // дельта-опрос (вызывай по интервалу)
}

// Строим начальный массив устройств из конфига (без сигналов — придут из API)
const initialDevices: Device[] = DEVICES_CONFIG.map((cfg) => ({
  id: cfg.id,
  sn: cfg.sn,
  type: cfg.type as Device["type"],
  name: cfg.name,
  room: cfg.room,
  isOnline: false,
  signals: {},
}));

export const useSmartHomeStore = create<SmartHomeState>((set, get) => ({
  devices: initialDevices,
  scenarios: [],
  logs: ["[Ujin OS] Инициализация..."],
  lastSyncDt: Math.floor(Date.now() / 1000) - 60, // смотрим на минуту назад при старте
  isConnected: false,

  addLog: (log) =>
    set((state) => ({
      logs: [...state.logs.slice(-99), `[${new Date().toLocaleTimeString()}] ${log}`],
    })),

  // Обновление только в локальном сторе (без API-вызова)
  updateSignalLocal: (deviceId, signalName, value) => {
    set((state) => ({
      devices: state.devices.map((d) =>
        d.id === deviceId
          ? {
              ...d,
              signals: {
                ...d.signals,
                [signalName]: { ...d.signals[signalName], value },
              },
            }
          : d
      ),
    }));
    get().executeScenarios(deviceId, signalName, value);
  },

  // Отправка команды в платформу + обновление локального стора
  sendSignal: async (deviceId, signalName, value) => {
    const device = get().devices.find((d) => d.id === deviceId);
    if (!device) return;

    const signal = device.signals[signalName];
    if (signal?.mgmt === "r") {
      get().addLog(`⚠️ Сигнал ${signalName} — только чтение, команда не отправлена`);
      return;
    }

    // Оптимистично обновляем UI сразу
    get().updateSignalLocal(deviceId, signalName, value);

    try {
      // apartment/send-signal — штатная команда устройству (api-methods.md §4)
      await apiSendSignal(device.sn, signalName, value, UST_TOKEN);
      get().addLog(`✅ ${device.name}: ${signalName} → ${value}`);
    } catch (err) {
      get().addLog(`❌ Ошибка команды ${signalName}: ${(err as Error).message}`);
    }
  },

  addDevice: (device) =>
    set((state) => ({ devices: [...state.devices, device] })),

  saveScenario: (scenario) =>
    set((state) => ({
      scenarios: [
        ...state.scenarios.filter((s) => s.id !== scenario.id),
        scenario,
      ],
    })),

  executeScenarios: (deviceId, signalName, value) => {
    const { scenarios, sendSignal, addLog } = get();
    scenarios.forEach((scenario) => {
      if (!scenario.isActive) return;
      const triggerNode = scenario.nodes.find(
        (n) =>
          n.type === "triggerNode" &&
          n.data.deviceId === deviceId &&
          n.data.signal === signalName &&
          // eslint-disable-next-line eqeqeq
          n.data.value == value
      );
      if (triggerNode) {
        addLog(`🎬 Сценарий «${scenario.name}» сработал`);
        scenario.nodes
          .filter((n) => n.type === "actionNode")
          .forEach((action) => {
            sendSignal(action.data.deviceId, action.data.signal, action.data.value);
            addLog(
              `→ ${action.data.deviceId}: ${action.data.signal}=${action.data.value}`
            );
          });
      }
    });
  },

  // ── Первичная загрузка ──────────────────────────────────────────────────────
  initFromApi: async () => {
    const { addLog } = get();
    addLog("🔄 Загрузка устройств с Ujin OS...");

    const results = await Promise.allSettled(
      DEVICES_CONFIG.map((cfg) => fetchDeviceInfo(cfg.sn, UST_TOKEN))
    );

    results.forEach((result, i) => {
      const cfg = DEVICES_CONFIG[i];
      if (result.status === "rejected") {
        addLog(`❌ ${cfg.name}: ${result.reason?.message ?? "нет ответа"}`);
        return;
      }

      const info = result.value;
      // Преобразуем массив сигналов из API в Record<string, Signal>
      const signals: Device["signals"] = {};
      (info.signals ?? []).forEach((s) => {
        signals[s.name] = {
          name: s.name,
          value: s.value,
          class: "integrations_devices",
          mgmt: s.mgmt ?? "r",
        };
      });

      set((state) => ({
        devices: state.devices.map((d) =>
          d.id === cfg.id ? { ...d, isOnline: true, signals } : d
        ),
      }));
    });

    set({
      isConnected: true,
      lastSyncDt: Math.floor(Date.now() / 1000),
    });
    addLog("✅ Устройства загружены");
  },

  // ── Дельта-опрос (devices/difference) ──────────────────────────────────────
  pollDifference: async () => {
    const { lastSyncDt, addLog, updateSignalLocal } = get();
    try {
      const diff = await fetchDifference(lastSyncDt, UST_TOKEN);

      diff.forEach((item) => {
        // Найди устройство по sn
        const device = get().devices.find((d) => d.sn === String(item.sn));
        if (!device) return;

        (item.signals ?? []).forEach((s: any) => {
          updateSignalLocal(device.id, s.name, s.value);
        });
      });

      if (diff.length > 0) {
        addLog(`📥 Получено обновлений: ${diff.length} устройств`);
      }

      set({ lastSyncDt: Math.floor(Date.now() / 1000) });
    } catch (err) {
      addLog(`⚠️ Poll error: ${(err as Error).message}`);
    }
  },
}));
