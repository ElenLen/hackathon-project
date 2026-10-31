import { create } from "zustand";
import type { Device, Scenario } from "../types"; // убедись, что путь правильный

interface SmartHomeState {
  devices: Device[];
  scenarios: Scenario[];
  logs: string[];
  updateSignal: (deviceId: string, signalName: string, value: any) => void;
  addDevice: (device: Device) => void;
  saveScenario: (scenario: Scenario) => void;
  addLog: (log: string) => void;
  executeScenarios: (deviceId: string, signalName: string, value: any) => void;
}

export const useSmartHomeStore = create<SmartHomeState>((set, get) => ({
  // Явно типизируем массив устройств, чтобы TS не ругался
  devices: [
    {
      id: "khikhikhakhakery_leak_01",
      sn: "17048769610018002",
      type: "leak",
      name: "Датчик протечки",
      room: "Санузел",
      isOnline: true,
      signals: {
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
    },
    {
      id: "khikhikhakhakery_light_01",
      sn: "17048841600011160",
      type: "light",
      name: "Умный свет",
      room: "Гостиная",
      isOnline: true,
      signals: {
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
    },
    // ДОБАВЬ ОСТАЛЬНЫЕ УСТРОЙСТВА ИЗ ДОСТУПОВ, НЕ ЗАБУДЬ ПРО room!
    // Например:
    // {
    //   id: 'khikhikhakhakery_barrier_01',
    //   sn: '17037033610008585',
    //   type: 'barrier',
    //   name: 'Шлагбаум',
    //   room: 'Двор',
    //   isOnline: true,
    //   signals: {
    //     open: { name: 'open', value: 0, class: 'integrations_devices', mgmt: 'rw' }
    //   }
    // }
  ] as Device[], // <-- Явное приведение типа (если массив пустой или неполный)

  scenarios: [],
  logs: ["[Ujin OS] Инициализация системы..."],

  addLog: (log) => set((state) => ({ logs: [...state.logs, log] })),

  updateSignal: (deviceId, signalName, value) => {
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
          : d,
      ),
    }));
    get().executeScenarios(deviceId, signalName, value);
  },

  addDevice: (device) =>
    set((state) => ({ devices: [...state.devices, device] })),

  saveScenario: (scenario) =>
    set((state) => ({ scenarios: [...state.scenarios, scenario] })),

  executeScenarios: (deviceId, signalName, value) => {
    const { scenarios, updateSignal, addLog } = get();

    scenarios.forEach((scenario) => {
      if (!scenario.isActive) return;

      const triggerNode = scenario.nodes.find(
        (n) =>
          n.type === "triggerNode" &&
          n.data.deviceId === deviceId &&
          n.data.signal === signalName &&
          n.data.value == value,
      );

      if (triggerNode) {
        addLog(`[Сценарий] Сработал триггер "${scenario.name}"`);

        const actionNodes = scenario.nodes.filter(
          (n) => n.type === "actionNode",
        );
        actionNodes.forEach((action) => {
          updateSignal(
            action.data.deviceId,
            action.data.signal,
            action.data.value,
          );
          addLog(
            `[Сценарий] Выполнено действие: ${action.data.deviceId} -> ${action.data.signal}=${action.data.value}`,
          );
        });
      }
    });
  },
}));
