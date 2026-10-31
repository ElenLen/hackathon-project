import { useState } from "react";
import { useSmartHomeStore } from "../store/useSmartHomeStore";
import {
  Droplets,
  Lightbulb,
  Thermometer,
  ShieldAlert,
  Gauge,
} from "lucide-react";

// Иконки для разных типов устройств
const deviceIcons: Record<string, any> = {
  leak: Droplets,
  light: Lightbulb,
  thermostat: Thermometer,
  barrier: ShieldAlert,
  counter: Gauge,
};

export default function Dashboard() {
  const { devices, updateSignal, logs } = useSmartHomeStore();
  const [selectedDevice, setSelectedDevice] = useState<string | null>(null);

  // Группируем устройства по комнатам
  const rooms = devices.reduce(
    (acc, device) => {
      if (!acc[device.room]) acc[device.room] = [];
      acc[device.room].push(device);
      return acc;
    },
    {} as Record<string, typeof devices>,
  );

  return (
    <div className="max-w-7xl mx-auto">
      <h1 className="text-3xl font-bold mb-6 text-cyan-400">Умный дом</h1>

      {/* Логи Ujin OS (для демонстрации интеграции) */}
      <div className="bg-slate-800/50 rounded-lg p-4 mb-6 border border-slate-700">
        <h3 className="text-sm font-semibold text-slate-400 mb-2">
          📡 Логи Ujin OS
        </h3>
        <div className="h-24 overflow-y-auto text-xs font-mono text-green-400 space-y-1">
          {logs.slice(-5).map((log, i) => (
            <div key={i}>{log}</div>
          ))}
        </div>
      </div>

      {/* Сетка комнат */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Object.entries(rooms).map(([roomName, roomDevices]) => (
          <div
            key={roomName}
            className="bg-slate-800 rounded-xl p-4 border border-slate-700"
          >
            <h2 className="text-lg font-semibold mb-3 text-slate-200">
              {roomName}
            </h2>
            <div className="space-y-2">
              {roomDevices.map((device) => {
                const Icon = deviceIcons[device.type] || Lightbulb;
                const isAlert = device.signals.leak?.value === 1;
                return (
                  <div
                    key={device.id}
                    onClick={() => setSelectedDevice(device.id)}
                    className={`flex items-center gap-3 p-3 rounded-lg cursor-pointer transition-all ${
                      isAlert
                        ? "bg-red-900/50 border border-red-500 animate-pulse"
                        : "bg-slate-700 hover:bg-slate-600 border border-transparent"
                    }`}
                  >
                    <Icon className="w-5 h-5 text-cyan-400" />
                    <div className="flex-1">
                      <div className="font-medium text-sm">{device.name}</div>
                      <div className="text-xs text-slate-400">
                        {isAlert
                          ? "⚠️ Тревога!"
                          : device.isOnline
                            ? "Онлайн"
                            : "Офлайн"}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* Модалка-эмулятор */}
      {selectedDevice && (
        <EmulatorModal
          deviceId={selectedDevice}
          onClose={() => setSelectedDevice(null)}
          updateSignal={updateSignal}
        />
      )}
    </div>
  );
}

// Компонент эмулятора
function EmulatorModal({
  deviceId,
  onClose,
  updateSignal,
}: {
  deviceId: string;
  onClose: () => void;
  updateSignal: (id: string, signal: string, value: any) => void;
}) {
  const device = useSmartHomeStore((state) =>
    state.devices.find((d) => d.id === deviceId),
  );
  if (!device) return null;

  return (
    <div
      className="fixed inset-0 bg-black/70 flex items-center justify-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-slate-800 rounded-xl p-6 w-full max-w-md border border-slate-600"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-xl font-bold mb-4 text-cyan-400">
          🎛 Эмулятор: {device.name}
        </h2>
        <p className="text-xs text-slate-400 mb-4">SN: {device.sn}</p>

        <div className="space-y-4">
          {Object.entries(device.signals).map(([signalName, signal]) => (
            <div key={signalName}>
              <label className="block text-sm text-slate-300 mb-1">
                {signalName} (mgmt: {signal.mgmt})
              </label>
              {signal.mgmt === "rw" ? (
                <button
                  onClick={() => {
                    const newVal = signal.value === 1 ? 0 : 1;
                    updateSignal(device.id, signalName, newVal);
                  }}
                  className={`w-full py-2 rounded-lg font-semibold transition-all ${
                    signal.value === 1
                      ? "bg-red-600 hover:bg-red-700"
                      : "bg-green-600 hover:bg-green-700"
                  }`}
                >
                  {signal.value === 1 ? "Выключить (1)" : "Включить (0)"}
                </button>
              ) : (
                <div className="text-slate-500 text-sm">
                  Только чтение: {signal.value}
                </div>
              )}
            </div>
          ))}
        </div>

        <button
          onClick={onClose}
          className="mt-6 w-full py-2 bg-slate-600 hover:bg-slate-500 rounded-lg font-semibold"
        >
          Закрыть
        </button>
      </div>
    </div>
  );
}
