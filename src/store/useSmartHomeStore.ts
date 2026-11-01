import { create } from "zustand";
import type { Device, Scenario } from "../types";
import { DEVICES_CONFIG } from "../api/ujin";

interface SmartHomeState {
  devices: Device[];
  scenarios: Scenario[];
  logs: string[];
  isLoaded: boolean;
  currentNodes: any[];
  currentEdges: any[];
  setCurrentNodes: (nodes: any[]) => void;
  setCurrentEdges: (edges: any[]) => void;
  updateNodeData: (nodeId: string, data: any) => void;
  addLog: (log: string) => void;
  applyDifference: () => Promise<void>;
  sendSignal: (deviceId: string, signalName: string, value: any) => Promise<void>;
  addDevice: (device: Device) => void;
  saveScenario: (scenario: Scenario) => void;
  executeScenarios: (deviceId: string, signalName: string, value: any) => void;
  triggerScenarioManually: (scenarioId: string) => void;
}

const initialDevices: Device[] = DEVICES_CONFIG.map((cfg) => {
  const defaultSignals: Record<string, any> = {
    leak: {
      leak: { name: "leak", value: 0, class: "integrations_devices", mgmt: "rw" },
      battery: { name: "battery", value: 100, class: "integrations_devices", mgmt: "r" },
    },
    barrier: {
      open: { name: "open", value: 0, class: "integrations_devices", mgmt: "rw" },
      status: { name: "status", value: "closed", class: "integrations_devices", mgmt: "r" },
    },
    thermostat: {
      target_temp: { name: "target_temp", value: 22, class: "integrations_devices", mgmt: "rw" },
      current_temp: { name: "current_temp", value: 21, class: "integrations_devices", mgmt: "r" },
    },
    counter: {
      total: { name: "total", value: 0, class: "integrations_devices", mgmt: "r" },
    },
    light: {
      lamp: { name: "lamp", value: 0, class: "integrations_devices", mgmt: "rw" },
      brightness: { name: "brightness", value: 0, class: "integrations_devices", mgmt: "rw" },
    },
  };
  return {
    id: cfg.id,
    sn: cfg.sn,
    type: cfg.type as Device["type"],
    name: cfg.name,
    room: cfg.room,
    isOnline: true,
    signals: defaultSignals[cfg.type] || {},
  };
});

export const useSmartHomeStore = create<SmartHomeState>((set, get) => ({
  devices: initialDevices,
  scenarios: [],
  logs: ["[Офлайн режим] Приложение готово к работе"],
  isLoaded: true,
  currentNodes: [],
  currentEdges: [],

  setCurrentNodes: (nodes) => set({ currentNodes: nodes }),
  setCurrentEdges: (edges) => set({ currentEdges: edges }),
  updateNodeData: (nodeId, data) =>
    set((s) => ({
      currentNodes: s.currentNodes.map((n) => (n.id === nodeId ? { ...n, data: { ...n.data, ...data } } : n)),
    })),

  addLog: (log) =>
    set((s) => ({
      logs: [...s.logs.slice(-99), `[${new Date().toLocaleTimeString()}] ${log}`],
    })),

  applyDifference: async () => {
    set({ isLoaded: true });
  },

  sendSignal: async (deviceId, signalName, value) => {
    const { addLog } = get();
    const device = get().devices.find((d) => d.id === deviceId);
    if (!device) return;

    if (device.signals[signalName]?.mgmt === "r") {
      addLog(`⚠️ ${signalName} — только чтение`);
      return;
    }

    set((s) => ({
      devices: s.devices.map((d) =>
        d.id === deviceId
          ? { ...d, signals: { ...d.signals, [signalName]: { ...d.signals[signalName], value } } }
          : d
      ),
    }));

    addLog(`✅ ${device.name}: ${signalName} → ${value}`);
    get().executeScenarios(deviceId, signalName, value);
  },

  addDevice: (device) => set((s) => ({ devices: [...s.devices, device] })),

  saveScenario: (scenario) =>
    set((s) => ({
      scenarios: [...s.scenarios.filter((sc) => sc.id !== scenario.id), scenario],
    })),

  executeScenarios: (deviceId, signalName, value) => {
    const { scenarios, sendSignal, addLog } = get();
    scenarios.forEach((scenario) => {
      if (!scenario.isActive) return;
      const trigger = scenario.nodes.find(
        (n) => n.type === "triggerNode" && n.data.deviceId === deviceId && n.data.signal === signalName && n.data.value == value
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