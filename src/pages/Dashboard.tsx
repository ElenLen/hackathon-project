// src/pages/Dashboard.tsx
import { useEffect, useRef, useState } from "react";
import { useSmartHomeStore } from "../store/useSmartHomeStore";
import {
  Droplets,
  Lightbulb,
  Thermometer,
  ShieldAlert,
  Gauge,
  RefreshCw,
  Wifi,
  WifiOff,
} from "lucide-react";

const deviceIcons: Record<string, any> = {
  leak: Droplets,
  light: Lightbulb,
  thermostat: Thermometer,
  barrier: ShieldAlert,
  counter: Gauge,
};

const POLL_INTERVAL_MS = 3000; // опрос devices/difference каждые 3 сек

export default function Dashboard() {
  const { devices, sendSignal, logs, isConnected, initFromApi, pollDifference } =
    useSmartHomeStore();
  const [selectedDevice, setSelectedDevice] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // Первичная загрузка при маунте
  useEffect(() => {
    (async () => {
      setLoading(true);
      await initFromApi();
      setLoading(false);
    })();
  }, []);

  // Запускаем polling после успешной инициализации
  useEffect(() => {
    if (!isConnected) return;
    pollRef.current = setInterval(pollDifference, POLL_INTERVAL_MS);
    return () => {
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [isConnected]);

  const rooms = devices.reduce(
    (acc, device) => {
      if (!acc[device.room]) acc[device.room] = [];
      acc[device.room].push(device);
      return acc;
    },
    {} as Record<string, typeof devices>
  );

  return (
    <div className="max-w-7xl mx-auto">
      {/* Заголовок + статус */}
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-cyan-400">Умный дом</h1>
        <div className="flex items-center gap-2 text-sm">
          {loading ? (
            <RefreshCw className="w-4 h-4 animate-spin text-cyan-400" />
          ) : isConnected ? (
            <Wifi className="w-4 h-4 text-green-400" />
          ) : (
            <WifiOff className="w-4 h-4 text-red-400" />
          )}
          <span className={isConnected ? "text-green-400" : "text-slate-400"}>
            {loading ? "Подключение..." : isConnected ? "Ujin OS — онлайн" : "Нет соединения"}
          </span>
        </div>
      </div>

      {/* Логи */}
      <div className="bg-slate-800/50 rounded-lg p-4 mb-6 border border-slate-700">
        <h3 className="text-sm font-semibold text-slate-400 mb-2">📡 Логи Ujin OS</h3>
        <div className="h-24 overflow-y-auto text-xs font-mono text-green-400 space-y-1">
          {logs.slice(-10).map((log, i) => (
            <div key={i}>{log}</div>
          ))}
        </div>
      </div>

      {/* Комнаты */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Object.entries(rooms).map(([roomName, roomDevices]) => (
          <div
            key={roomName}
            className="bg-slate-800 rounded-xl p-4 border border-slate-700"
          >
            <h2 className="text-lg font-semibold mb-3 text-slate-200">{roomName}</h2>
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
                    <Icon className="w-5 h-5 text-cyan-400 shrink-0" />
                    <div className="flex-1 min-w-0">
                      <div className="font-medium text-sm truncate">{device.name}</div>
                      <div className="text-xs text-slate-400">
                        {!device.isOnline
                          ? "Офлайн"
                          : isAlert
                          ? "⚠️ Тревога!"
                          : `${Object.keys(device.signals).length} сигналов`}
                      </div>
                    </div>
                    {/* Краткое состояние первого rw-сигнала */}
                    {Object.values(device.signals).find((s) => s.mgmt === "rw") && (
                      <span
                        className={`text-xs px-2 py-0.5 rounded-full ${
                          Object.values(device.signals).find((s) => s.mgmt === "rw")?.value === 1
                            ? "bg-green-700 text-green-100"
                            : "bg-slate-600 text-slate-300"
                        }`}
                      >
                        {Object.values(device.signals).find((s) => s.mgmt === "rw")?.value === 1
                          ? "ВКЛ"
                          : "ВЫКЛ"}
                      </span>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* Модалка устройства */}
      {selectedDevice && (
        <DeviceModal
          deviceId={selectedDevice}
          onClose={() => setSelectedDevice(null)}
          onSendSignal={sendSignal}
        />
      )}
    </div>
  );
}

function DeviceModal({
  deviceId,
  onClose,
  onSendSignal,
}: {
  deviceId: string;
  onClose: () => void;
  onSendSignal: (id: string, signal: string, value: any) => Promise<void>;
}) {
  const device = useSmartHomeStore((s) => s.devices.find((d) => d.id === deviceId));
  const [pending, setPending] = useState<string | null>(null);
  if (!device) return null;

  const handleToggle = async (signalName: string, currentValue: any) => {
    setPending(signalName);
    await onSendSignal(device.id, signalName, currentValue === 1 ? 0 : 1);
    setPending(null);
  };

  return (
    <div
      className="fixed inset-0 bg-black/70 flex items-center justify-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-slate-800 rounded-xl p-6 w-full max-w-md border border-slate-600"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-xl font-bold mb-1 text-cyan-400">🎛 {device.name}</h2>
        <p className="text-xs text-slate-400 mb-1">SN: {device.sn}</p>
        <p className="text-xs text-slate-500 mb-4">ID: {device.id}</p>

        <div className="space-y-3">
          {Object.entries(device.signals).map(([signalName, signal]) => (
            <div key={signalName} className="bg-slate-700/50 rounded-lg p-3">
              <div className="flex items-center justify-between mb-1">
                <span className="text-sm text-slate-300 font-mono">{signalName}</span>
                <span className="text-xs text-slate-500 bg-slate-800 px-2 py-0.5 rounded">
                  {signal.mgmt}
                </span>
              </div>
              {signal.mgmt === "rw" || signal.mgmt === "w" ? (
                <button
                  disabled={pending === signalName}
                  onClick={() => handleToggle(signalName, signal.value)}
                  className={`w-full py-2 rounded-lg font-semibold text-sm transition-all disabled:opacity-50 ${
                    signal.value === 1
                      ? "bg-red-600 hover:bg-red-700"
                      : "bg-green-600 hover:bg-green-700"
                  }`}
                >
                  {pending === signalName
                    ? "Отправка..."
                    : signal.value === 1
                    ? "Выключить (сейчас: 1)"
                    : "Включить (сейчас: 0)"}
                </button>
              ) : (
                <div className="text-slate-400 text-sm">
                  Значение: <span className="text-white font-mono">{signal.value}</span>
                </div>
              )}
            </div>
          ))}

          {Object.keys(device.signals).length === 0 && (
            <p className="text-slate-500 text-sm text-center py-4">
              Сигналы не загружены
            </p>
          )}
        </div>

        <button
          onClick={onClose}
          className="mt-4 w-full py-2 bg-slate-600 hover:bg-slate-500 rounded-lg font-semibold text-sm"
        >
          Закрыть
        </button>
      </div>
    </div>
  );
}
