// src/api/ujin.ts
import axios from "axios";

export const BASE_URL = "/api";
export const UST_TOKEN = "ust-2815073-048272461ef140f8b1d680df3bbb88d0";

export const DEVICES_CONFIG = [
  {
    id: "khikhikhakhakery_leak_01",
    sn: "17048769610018002",
    type: "leak",
    name: "Датчик протечки",
    room: "Санузел",
  },
  {
    id: "khikhikhakhakery_barrier_01",
    sn: "17037033610008585",
    type: "barrier",
    name: "Шлагбаум",
    room: "Двор",
  },
  {
    id: "khikhikhakhakery_thermostat_01",
    sn: "17036961610007336",
    type: "thermostat",
    name: "Термостат",
    room: "Гостиная",
  },
  {
    id: "khikhikhakhakery_counter_01",
    sn: "17048805610016831",
    type: "counter",
    name: "Счётчик",
    room: "Коридор",
  },
  {
    id: "khikhikhakhakery_light_01",
    sn: "17048841600011160",
    type: "light",
    name: "Умный свет",
    room: "Гостиная",
  },
] as const;

const api = axios.create({ baseURL: BASE_URL });

export interface DifferenceItem {
  serialnumber: string;
  signal: string;
  value: string | number;
  dt: number;
  reload_full_device: number;
}

export interface DifferenceResponse {
  difference: DifferenceItem[];
  time: [number, number];
  "reload-fulldata": number;
  timestamp: number;
}

export async function fetchDifference(
  dt: number,
  token = UST_TOKEN,
): Promise<DifferenceResponse> {
  const res = await api.get("/devices/difference", { params: { token, dt } });
  if (res.data.error !== 0) throw new Error(res.data.message);
  return res.data.data as DifferenceResponse;
}

export async function publishSignals(
  updates: Array<{ sn: string; signal: string; value: any }>,
  token = UST_TOKEN,
): Promise<void> {
  const res = await api.post(
    "/import_device/group-update-device-signal",
    {
      data: updates.map(({ sn, signal, value }) => ({
        sn,
        s: signal,
        v: value,
        c: "integrations_devices",
      })),
    },
    { params: { token }, headers: { "Content-Type": "application/json" } },
  );
  if (res.data.error !== 0) throw new Error(res.data.message);
}
