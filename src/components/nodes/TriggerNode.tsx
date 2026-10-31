import { Handle, Position } from "reactflow";
import { Zap } from "lucide-react";
import { useSmartHomeStore } from "../../store/useSmartHomeStore";

export default function TriggerNode({ data, id }: { data: any; id: string }) {
  const { devices } = useSmartHomeStore();
  const selectedDevice = devices.find((d) => d.id === data.deviceId);

  // Функция для обновления данных ноды (вызывается из родителя)
  const update = (patch: any) => {
    if (data.onChange) data.onChange(id, patch);
  };

  return (
    <div className="bg-amber-900/50 border-2 border-amber-500 rounded-lg p-3 min-w-[220px] shadow-lg">
      <Handle
        type="source"
        position={Position.Right}
        className="w-3 h-3 bg-amber-400"
      />
      <div className="flex items-center gap-2 mb-2">
        <Zap className="w-4 h-4 text-amber-400" />
        <span className="text-xs font-bold text-amber-400 uppercase">
          Триггер (ЕСЛИ)
        </span>
      </div>

      <select
        value={data.deviceId || ""}
        onChange={(e) => update({ deviceId: e.target.value, signal: "" })}
        className="w-full bg-slate-800 border border-slate-600 rounded px-2 py-1 text-xs text-white mb-1"
      >
        <option value="">Выбери устройство...</option>
        {devices.map((d) => (
          <option key={d.id} value={d.id}>
            {d.name}
          </option>
        ))}
      </select>

      {selectedDevice && (
        <select
          value={data.signal || ""}
          onChange={(e) => update({ signal: e.target.value })}
          className="w-full bg-slate-800 border border-slate-600 rounded px-2 py-1 text-xs text-white mb-1"
        >
          <option value="">Выбери сигнал...</option>
          {Object.keys(selectedDevice.signals).map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      )}

      <input
        type="text"
        placeholder="Значение (1, 0, 25...)"
        value={data.value ?? ""}
        onChange={(e) => update({ value: e.target.value })}
        className="w-full bg-slate-800 border border-slate-600 rounded px-2 py-1 text-xs text-white"
      />
    </div>
  );
}
