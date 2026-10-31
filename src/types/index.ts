export type DeviceType =
  | "leak"
  | "light"
  | "thermostat"
  | "barrier"
  | "counter"
  | "valve";

export interface Signal {
  name: string;
  value: any;
  class: "integrations_devices"; // Важно из доки!
  mgmt: "r" | "rw"; // r - только чтение, rw - можно управлять
}

export interface Device {
  id: string; // ext_guid (например, khikhikhakhakery_leak_01)
  sn: string; // serial number (из доступов)
  type: DeviceType;
  name: string;
  room: string;
  signals: Record<string, Signal>;
  isOnline: boolean;
}

export interface Scenario {
  id: string;
  name: string;
  isActive: boolean;
  nodes: any[]; // Пока оставим any для React Flow
  edges: any[];
}
