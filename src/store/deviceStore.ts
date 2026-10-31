import { create } from 'zustand';
import type { DeviceState } from '../api/websocket';
import { wsClient } from '../api/websocket';

interface DeviceStore {
  devices: Record<string, DeviceState>;
  setDevices: (devices: DeviceState[]) => void;
  updateDevice: (device: DeviceState) => void;
  togglePower: (deviceId: string, currentPower: boolean) => void;
}

export const useDeviceStore = create<DeviceStore>((set) => ({
  devices: {},
  setDevices: (deviceList) => {
    const devMap: Record<string, DeviceState> = {};
    deviceList.forEach((d) => (devMap[d.device_id] = d));
    set({ devices: devMap });
  },
  updateDevice: (device) =>
    set((state) => ({
      devices: { ...state.devices, [device.device_id]: device },
    })),
  togglePower: (deviceId, currentPower) => {
    wsClient.sendCommand(deviceId, 'set_power', { power: !currentPower });
  },
}));