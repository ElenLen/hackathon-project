import axios from "axios";

export const BASE_URL = "/api";

// ==================== ТОКЕН ====================
/**
 * Приоритет получения токена:
 * 1. localStorage (токен из авторизации пользователя)
 * 2. .env (VITE_UST_TOKEN — для локальной разработки)
 * 3. Пустая строка (если ничего нет — API вернет ошибку, и мы ее покажем)
 */
export function getUstToken(): string {
  // 1. Пробуем из localStorage
  try {
    const stored = localStorage.getItem("ujin_user_v3");
    if (stored) {
      const parsed = JSON.parse(stored);
      if (parsed.token) return parsed.token;
    }
  } catch {
    // ignore
  }

  // 2. Пробуем из .env
  const envToken = import.meta.env.VITE_UST_TOKEN;
  if (envToken) return envToken;

  // 3. Нет токена
  return "";
}

// Для обратной совместимости, но лучше использовать getUstToken()
export const UST_TOKEN = getUstToken();

// ==================== КОНФИГ УСТРОЙСТВ ====================
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

// ==================== AXIOS ====================
const api = axios.create({ baseURL: BASE_URL });

// ==================== ТИПЫ ====================
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

// ==================== СИНХРОНИЗАЦИЯ ====================
export async function fetchDifference(
  dt: number,
  token?: string,
): Promise<DifferenceResponse> {
  const actualToken = token || getUstToken();
  if (!actualToken) throw new Error("Токен не найден. Авторизуйтесь заново.");

  const res = await api.get("/devices/difference", {
    params: { token: actualToken, dt },
  });
  if (res.data.error !== 0) throw new Error(res.data.message);
  return res.data.data as DifferenceResponse;
}

export async function publishSignals(
  updates: Array<{ sn: string; signal: string; value: any }>,
  token?: string,
): Promise<void> {
  const actualToken = token || getUstToken();
  if (!actualToken) throw new Error("Токен не найден. Авторизуйтесь заново.");

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
    {
      params: { token: actualToken },
      headers: { "Content-Type": "application/json" },
    },
  );
  if (res.data.error !== 0) throw new Error(res.data.message);
}

// ==================== АВТОРИЗАЦИЯ ====================
export interface AuthUser {
  user_fullname: string;
  user_name: string;
  user_surname: string;
  user_phone: string;
  token: string;
  apartment: Array<{
    id: string;
    title: string;
    complex_title: string;
    building_address: string;
  }>;
}

export async function authenticate(
  login: string,
  password: string,
): Promise<AuthUser | null> {
  try {
    const res = await api.post(
      "/auth/authenticate",
      new URLSearchParams({ login, password }),
      { headers: { "Content-Type": "application/x-www-form-urlencoded" } },
    );
    if (res.data.error !== 0) return null;
    return res.data.data.user as AuthUser;
  } catch (err) {
    console.error("Auth error:", err);
    return null;
  }
}
