// src/store/useSmartHomeStore.ts
import { create } from "zustand";
import type { Device, Scenario } from "../types";
import { DEVICES_CONFIG, UST_TOKEN, fetchDifference, publishSignals } from "../api/ujin";

interface SmartHomeState {
  devices: Device[];
  scenarios: Scenario[];
  logs: string[];
  lastDt: number;       // timestamp последнего difference — передаём в следующий запрос
  isLoaded: boolean;

  addLog: (log: string) => void;
  applyDifference: () => Promise<void>; // единственный метод синхронизации с API
  sendSignal: (deviceId: string, signalName: string, value: any) => Promise<void>;
  addDevice: (device: Device) => void;
  saveScenario: (scenario: Scenario) => void;
  executeScenarios: (deviceId: string, signalName: string, value: any) => void;
}

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
  lastDt: 0, // 0 = взять всю историю с начала
  isLoaded: false,

  addLog: (log) =>
    set((s) => ({
      logs: [...s.logs.slice(-99), `[${new Date().toLocaleTimeString()}] ${log}`],
    })),

  // Единственный рабочий метод: вызывай при старте и по интервалу
  applyDifference: async () => {
    const { lastDt, addLog } = get();
    try {
      const data = await fetchDifference(lastDt, UST_TOKEN);

      // Применяем каждый сигнал из дельты к локальному стору
      data.difference.forEach((item) => {
        const device = get().devices.find((d) => d.sn === String(item.serialnumber));
        if (!device) return;

        set((s) => ({
          devices: s.devices.map((d) =>
            d.id === device.id
              ? {
                  ...d,
                  isOnline: true,
                  signals: {
                    ...d.signals,
                    [item.signal]: {
                      name: item.signal,
                      value: item.value,
                      class: "integrations_devices",
                      // difference не возвращает mgmt — ставим rw для известных сигналов
                      mgmt: ["leak", "lamp", "brightness", "open", "target_temp"].includes(item.signal)
                        ? "rw"
                        : "r",
                    },
                  },
                }
              : d
          ),
        }));

        get().executeScenarios(device.id, item.signal, item.value);
      });

      // Сохраняем timestamp для следующего запроса
      set({ lastDt: data.timestamp, isLoaded: true });

      if (lastDt === 0) {
        addLog(`✅ Загружено ${data.difference.length} сигналов`);
      } else if (data.difference.length > 0) {
        addLog(`📥 Обновлено: ${data.difference.length} сигналов`);
      }
    } catch (err) {
      addLog(`❌ API: ${(err as Error).message}`);
    }
  },

  // Команды вниз через send-signal на этом стенде не работают.
  // Публикуем как сигнал устройства через group-update (работает),
  // и обновляем локальный стор сразу (оптимистично).
  sendSignal: async (deviceId, signalName, value) => {
    const { addLog } = get();
    const device = get().devices.find((d) => d.id === deviceId);
    if (!device) return;

    if (device.signals[signalName]?.mgmt === "r") {
      addLog(`⚠️ ${signalName} — только чтение`);
      return;
    }

    // Обновляем UI сразу
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
          : d
      ),
    }));

    get().executeScenarios(deviceId, signalName, value);

    // Публикуем на платформу
    try {
      await publishSignals([{ sn: device.sn, signal: signalName, value }]);
      addLog(`✅ ${device.name}: ${signalName} → ${value}`);
    } catch (err) {
      addLog(`❌ Publish: ${(err as Error).message}`);
    }
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
        (n) =>
          n.type === "triggerNode" &&
          n.data.deviceId === deviceId &&
          n.data.signal === signalName &&
          // eslint-disable-next-line eqeqeq
          n.data.value == value
      );
      if (trigger) {
        addLog(`🎬 Сценарий «${scenario.name}» сработал`);
        scenario.nodes
          .filter((n) => n.type === "actionNode")
          .forEach((a) => sendSignal(a.data.deviceId, a.data.signal, a.data.value));
      }
    });
  },
}));
