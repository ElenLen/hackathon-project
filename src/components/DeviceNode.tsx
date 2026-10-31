import React from 'react';
import { Handle, Position } from 'reactflow';
import { useDeviceStore } from '../store/deviceStore';
import { Power, Activity } from 'lucide-react';

interface DeviceNodeProps {
  data: {
    deviceId: string;
    label: string;
  };
}

export const DeviceNode: React.FC<DeviceNodeProps> = ({ data }) => {
  const device = useDeviceStore((state) => state.devices[data.deviceId]);
  const togglePower = useDeviceStore((state) => state.togglePower);

  if (!device) {
    return (
      <div className="p-3 bg-gray-100 border border-gray-300 rounded shadow">
        <div>Устройство {data.deviceId} не найдено</div>
      </div>
    );
  }

  const isPowerOn = device.state.power;

  return (
    <div className="p-4 bg-white border-2 border-slate-700 rounded-lg shadow-md min-w-[200px]">
      <Handle type="target" position={Position.Top} className="w-3 h-3 bg-blue-500" />
      
      <div className="flex items-center justify-between border-b pb-2 mb-2">
        <span className="font-bold text-sm text-gray-800">{data.label}</span>
        <Activity className={`w-4 h-4 ${isPowerOn ? 'text-green-500' : 'text-gray-400'}`} />
      </div>

      <div className="text-xs space-y-1 mb-3 text-gray-600">
        <div>ID: {device.device_id}</div>
        <div>Статус: <span className="font-semibold">{device.state.status}</span></div>
        {device.state.temperature !== undefined && (
          <div>Температура: <span className="font-semibold">{device.state.temperature} °C</span></div>
        )}
      </div>

      <button
        onClick={() => togglePower(device.device_id, !!isPowerOn)}
        className={`w-full py-1.5 px-3 rounded text-xs font-semibold flex items-center justify-center gap-2 transition-colors ${
          isPowerOn ? 'bg-red-500 hover:bg-red-600 text-white' : 'bg-green-500 hover:bg-green-600 text-white'
        }`}
      >
        <Power className="w-3.5 h-3.5" />
        {isPowerOn ? 'Выключить' : 'Включить'}
      </button>

      <Handle type="source" position={Position.Bottom} className="w-3 h-3 bg-blue-500" />
    </div>
  );
};