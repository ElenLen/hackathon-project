// src/api/ujin.ts
// Ujin OS — тест-стенд ОАЭ
// Все эндпоинты взяты из api-methods.md

import axios from "axios";

const BASE_URL = "https://api-uae-test.ujin.tech/api";

// UST-токены резидентов (брошюра, таблица 1)
// В продакшне вынеси в .env — не коммить!
export const UST_TOKEN = "ust-2815073-048272461ef140f8b1d680df3bbb88d0";

// Устройства команды (брошюра, раздел 2)
// ext_guid — это device_id в вашем сторе; serialnumber — sn для API
export const DEVICES_CONFIG = [
  { id: "khikhikhakhakery_leak_01",        sn: "17048769610018002", type: "leak",       name: "Датчик протечки",  room: "Санузел" },
  { id: "khikhikhakhakery_barrier_01",     sn: "17037033610008585", type: "barrier",    name: "Шлагбаум",         room: "Двор" },
  { id: "khikhikhakhakery_thermostat_01",  sn: "17036961610007336", type: "thermostat", name: "Термостат",        room: "Гостиная" },
  { id: "khikhikhakhakery_counter_01",     sn: "17048805610016831", type: "counter",    name: "Счётчик",          room: "Коридор" },
  { id: "khikhikhakhakery_light_01",       sn: "17048841600011160", type: "light",      name: "Умный свет",       room: "Гостиная" },
] as const;

const api = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// ── Типы ──────────────────────────────────────────────────────────────────────

export interface UjinResponse<T = any> {
  command: string;
  error: number;
  error_code: string | null;
  message: string;
  data: T;
}

export interface UjinSignal {
  name: string;
  value: any;
  class: string;
  mgmt: "r" | "rw" | "w";
}

export interface UjinDeviceInfo {
  serialnumber: string;
  signals: UjinSignal[];
  task?: Record<string, any>; // очередь команд платформы (только mgmt≠r)
}

// ── 1. Аутентификация ─────────────────────────────────────────────────────────

/** Получить свежий UST-токен по логину/паролю (api-methods.md §1) */
export async function authenticate(login: string, password: string): Promise<string> {
  const res = await api.post<UjinResponse<{ token: string }>>(
    "/auth/authenticate/",
    { login, password }
  );
  if (res.data.error !== 0) throw new Error(res.data.message);
  return res.data.data.token;
}

// ── 3. Чтение сигналов устройства ────────────────────────────────────────────

/**
 * Карточка устройства с текущими сигналами (api-methods.md §3)
 * GET bmsdevice/get-device-info?token=ust&serialnumber=SN&mts=1
 */
export async function fetchDeviceInfo(sn: string, token = UST_TOKEN): Promise<UjinDeviceInfo> {
  const res = await api.get<UjinResponse<UjinDeviceInfo>>(
    "/bmsdevice/get-device-info",
    { params: { token, serialnumber: sn, mts: 1 } }
  );
  if (res.data.error !== 0) throw new Error(res.data.message);
  return res.data.data;
}

/**
 * Дельта значений с момента dt (api-methods.md §5)
 * Основной pull — вызывай каждые 2–5 сек с последним ts
 * GET devices/difference?token=ust&dt=<unix>
 */
export async function fetchDifference(
  dt: number,
  token = UST_TOKEN
): Promise<Array<{ sn: string; signals: UjinSignal[] }>> {
  const res = await api.get<UjinResponse<any>>(
    "/devices/difference",
    { params: { token, dt } }
  );
  if (res.data.error !== 0) throw new Error(res.data.message);
  return res.data.data ?? [];
}

// ── 4. Команды «вниз» (UI → устройство) ─────────────────────────────────────

/**
 * Отправить команду на устройство (api-methods.md §4)
 * GET/POST apartment/send-signal?token=ust&serialnumber=SN&signal=name&value=v
 * Только сигналы с mgmt=rw
 */
export async function sendSignal(
  sn: string,
  signal: string,
  value: any,
  token = UST_TOKEN
): Promise<void> {
  const res = await api.get<UjinResponse>(
    "/apartment/send-signal",
    { params: { token, serialnumber: sn, signal, value } }
  );
  if (res.data.error !== 0) throw new Error(res.data.message);
}

// ── 2. Публикация сигналов (устройство → платформа) ─────────────────────────

/**
 * Пакетная запись сигналов (api-methods.md §2)
 * POST import_device/group-update-device-signal
 * ВАЖНО: поле c — это класс модели "integrations_devices" (не имя команды!)
 * Без правильного c платформа ТИХО теряет значения
 */
export async function publishSignals(
  updates: Array<{ sn: string; signal: string; value: any }>
): Promise<void> {
  const payload = {
    data: updates.map(({ sn, signal, value }) => ({
      sn,           // ext_guid устройства (khikhikhakhakery_leak_01)
      s: signal,    // имя сигнала
      v: value,     // значение
      c: "integrations_devices", // ОБЯЗАТЕЛЬНО — класс модели из словаря
    })),
  };
  const res = await api.post<UjinResponse>(
    "/import_device/group-update-device-signal",
    payload
  );
  if (res.data.error !== 0) throw new Error(res.data.message);
}

// ── 6. Сценарии (autoscripts) ─────────────────────────────────────────────────

/** Список сценариев резидента (api-methods.md §6) */
export async function fetchScenarios(token = UST_TOKEN) {
  const res = await api.get<UjinResponse>("/autoscripts/list", { params: { token } });
  return res.data.data ?? [];
}

/** Активировать/деактивировать сценарий */
export async function activateScenario(
  scenario_id: string | number,
  active: boolean,
  token = UST_TOKEN
) {
  const res = await api.post<UjinResponse>("/autoscripts/activate", {
    token,
    scenario_id,
    state: active ? 1 : 0,
  });
  if (res.data.error !== 0) throw new Error(res.data.message);
}

/**
 * Создать/обновить сценарий «если–то» (api-methods.md §6)
 * Условие: when: "device-<sn>:<signal>=<value>"
 */
export async function saveScenario(
  scenario_id: string | number | null,
  name: string,
  when: string,
  doAction: string,
  token = UST_TOKEN
) {
  const res = await api.post<UjinResponse>("/autoscripts/edit", {
    token,
    scenario_id: scenario_id ?? undefined,
    scenario_type: "device",
    name,
    when,
    do: doAction,
  });
  if (res.data.error !== 0) throw new Error(res.data.message);
  return res.data.data;
}
