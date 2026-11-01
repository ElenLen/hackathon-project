// src/store/useSmartHomeStore.ts
import { create } from "zustand";
import type { Device, Scenario } from "../types";
import {
  DEVICES_CONFIG,
  UST_TOKEN,
  fetchDifference,
  publishSignals,
} from "../api/ujin";

interface SmartHomeState {
  devices: Device[];
  scenarios: Scenario[];
  logs: string[];
  lastDt: number;
  isLoaded: boolean;

  // React Flow состояние
  currentNodes: any[];
  currentEdges: any[];
  setCurrentNodes: (nodes: any[]) => void;
  setCurrentEdges: (edges: any[]) => void;
  updateNodeData: (nodeId: string, data: any) => void;

  // Методы
  addLog: (log: string) => void;
  applyDifference: () => Promise<void>;
  sendSignal: (
    deviceId: string,
    signalName: string,
    value: any,
  ) => Promise<void>;
  addDevice: (device: Device) => void;
  saveScenario: (scenario: Scenario) => void;
  executeScenarios: (deviceId: string, signalName: string, value: any) => void;
  triggerScenarioManually: (scenarioId: string) => void;
}

const initialDevices: Device[] = DEVICES_CONFIG.map((cfg) => {
  // Захардкоженные сигналы для каждого типа устройства (для демо)
  const defaultSignals: Record<string, any> = {
    leak: {
      leak: {
        name: "leak",
        value: 0,
        class: "integrations_devices",
        mgmt: "rw",
      },
      battery: {
        name: "battery",
        value: 100,
        class: "integrations_devices",
        mgmt: "r",
      },
    },
    barrier: {
      open: {
        name: "open",
        value: 0,
        class: "integrations_devices",
        mgmt: "rw",
      },
      status: {
        name: "status",
        value: "closed",
        class: "integrations_devices",
        mgmt: "r",
      },
    },
    thermostat: {
      target_temp: {
        name: "target_temp",
        value: 22,
        class: "integrations_devices",
        mgmt: "rw",
      },
      current_temp: {
        name: "current_temp",
        value: 21,
        class: "integrations_devices",
        mgmt: "r",
      },
    },
    counter: {
      total: {
        name: "total",
        value: 0,
        class: "integrations_devices",
        mgmt: "r",
      },
    },
    light: {
      lamp: {
        name: "lamp",
        value: 0,
        class: "integrations_devices",
        mgmt: "rw",
      },
      brightness: {
        name: "brightness",
        value: 0,
        class: "integrations_devices",
        mgmt: "rw",
      },
    },
  };

  return {
    id: cfg.id,
    sn: cfg.sn,
    type: cfg.type as Device["type"],
    name: cfg.name,
    room: cfg.room,
    isOnline: false,
    signals: defaultSignals[cfg.type] || {},
  };
});

export const useSmartHomeStore = create<SmartHomeState>((set, get) => ({
  devices: initialDevices,
  scenarios: [],
  logs: ["[Ujin OS] Инициализация..."],
  lastDt: 0,
  isLoaded: false,

  currentNodes: [],
  currentEdges: [],

  // ============ REACT FLOW ============
  setCurrentNodes: (nodes) => set({ currentNodes: nodes }),
  setCurrentEdges: (edges) => set({ currentEdges: edges }),
  updateNodeData: (nodeId, data) =>
    set((s) => ({
      currentNodes: s.currentNodes.map((n) =>
        n.id === nodeId ? { ...n, data: { ...n.data, ...data } } : n,
      ),
    })),

  // ============ ЛОГИ ============
  addLog: (log) =>
    set((s) => ({
      logs: [
        ...s.logs.slice(-99),
        `[${new Date().toLocaleTimeString()}] ${log}`,
      ],
    })),

  // ============ СИНХРОНИЗАЦИЯ С UJIN OS ============
  applyDifference: async () => {
    const { lastDt, addLog } = get();
    try {
      const data = await fetchDifference(lastDt, UST_TOKEN);

      // Если API вернул пустоту — просто ставим статус "онлайн" для всех устройств
      if (!data.difference || data.difference.length === 0) {
        // 1. Помечаем ВСЕ устройства как онлайн (чтобы демо работало)
        set((s) => ({
          devices: s.devices.map((d) => ({ ...d, isOnline: true })),
          isLoaded: true,
        }));
        // if (lastDt === 0)
        //   addLog(`✅ Устройства инициализированы (локальные сигналы)`);
        // return;

        // 2. Если API вернул пустоту — просто выходим
        if (!data.difference || data.difference.length === 0) {
          if (lastDt === 0)
            addLog(`✅ Устройства инициализированы (локальные сигналы)`);
          return;
        }
      }

      data.difference.forEach((item) => {
        const device = get().devices.find(
          (d) => d.sn === String(item.serialnumber),
        );
        if (!device) return;

        // Игнорируем служебные сигналы
        if (item.signal === "push-connect" || item.signal === "dt_create")
          return;

        const cleanSignalName = item.signal.replace(
          /^integrations_devices_/,
          "",
        );

        set((s) => ({
          devices: s.devices.map((d) => {
            if (d.id !== device.id) return d;
            return {
              ...d,
              isOnline: true,
              signals: {
                ...d.signals, // Сохраняем старые сигналы!
                [cleanSignalName]: {
                  name: cleanSignalName,
                  value: item.value,
                  class: "integrations_devices",
                  mgmt: [
                    "leak",
                    "lamp",
                    "brightness",
                    "open",
                    "target_temp",
                    "valve",
                    "mode",
                    "fan_speed",
                  ].includes(cleanSignalName)
                    ? "rw"
                    : "r",
                },
              },
            };
          }),
        }));
        get().executeScenarios(device.id, cleanSignalName, item.value);
      });

      set({ lastDt: data.timestamp, isLoaded: true });

      if (lastDt === 0) {
        addLog(`✅ Загружено ${data.difference.length} сигналов`);
      } else if (data.difference.length > 0) {
        addLog(`📥 Обновлено: ${data.difference.length} сигналов`);
      }
    } catch (err) {
      // Если API упал — ставим "онлайн" для всех, чтобы демо работало
      set((s) => ({
        devices: s.devices.map((d) => ({ ...d, isOnline: true })),
        isLoaded: true,
      }));
      addLog(`⚠️ API недоступен, используем локальные данные`);
    }
  },

  // ============ ОТПРАВКА СИГНАЛА ============
  sendSignal: async (deviceId, signalName, value) => {
    const { addLog } = get();
    const device = get().devices.find((d) => d.id === deviceId);
    if (!device) return;

    if (device.signals[signalName]?.mgmt === "r") {
      addLog(`⚠️ ${signalName} — только чтение`);
      return;
    }

    // Оптимистичное обновление UI
    set((s) => ({
      devices: s.devices.map((d) =>
        d.id === deviceId
          ? {
              ...d,
              signals: {
                ...d.signals,
                [signalName]: { ...d.signals[signalName], value },
              },
            }
          : d,
      ),
    }));

    get().executeScenarios(deviceId, signalName, value);

    // Публикация в Ujin OS
    try {
      await publishSignals([{ sn: device.sn, signal: signalName, value }]);
      addLog(`✅ ${device.name}: ${signalName} → ${value}`);
    } catch (err) {
      addLog(`❌ Publish: ${(err as Error).message}`);
    }
  },

  // ============ УСТРОЙСТВА ============
  addDevice: (device) => set((s) => ({ devices: [...s.devices, device] })),

  // ============ СЦЕНАРИИ ============
  saveScenario: (scenario) =>
    set((s) => ({
      scenarios: [
        ...s.scenarios.filter((sc) => sc.id !== scenario.id),
        scenario,
      ],
    })),

  executeScenarios: (deviceId, signalName, value) => {
    const { scenarios, sendSignal, addLog } = get();
    scenarios.forEach((scenario) => {
      if (!scenario.isActive) return;
      const trigger = scenario.nodes.find(
        (n) =>
          n.type === "triggerNode" &&
          n.data.deviceId === deviceId &&
          n.data.signal === signalName &&
          // eslint-disable-next-line eqeqeq
          n.data.value == value,
      );
      if (trigger) {
        addLog(`🎬 Сценарий «${scenario.name}» сработал`);
        scenario.nodes
          .filter((n) => n.type === "actionNode")
          .forEach((a) => {
            if (a.data.deviceId && a.data.signal) {
              sendSignal(a.data.deviceId, a.data.signal, a.data.value ?? 1);
            }
          });
      }
    });
  },

  triggerScenarioManually: (scenarioId) => {
    const scenario = get().scenarios.find((s) => s.id === scenarioId);
    if (!scenario) return;
    const { sendSignal, addLog } = get();
    addLog(`🧪 Ручной запуск сценария «${scenario.name}»`);
    scenario.nodes
      .filter((n) => n.type === "actionNode")
      .forEach((a) => {
        if (a.data.deviceId && a.data.signal) {
          sendSignal(a.data.deviceId, a.data.signal, a.data.value ?? 1);
        }
      });
  },
}));
