export type DeviceType = "leak" | "light" | "thermostat" | "barrier" | "counter" | "valve";

export interface Signal {
  name: string;
  value: any;
  class: "integrations_devices";
  mgmt: "r" | "rw";
}

export interface Device {
  id: string;
  sn: string;
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
  nodes: any[];
  edges: any[];
}