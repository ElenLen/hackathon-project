export const DEVICES_CONFIG = [
  { id: "khikhikhakhakery_leak_01", sn: "17048769610018002", type: "leak", name: "Датчик протечки", room: "Санузел" },
  { id: "khikhikhakhakery_barrier_01", sn: "17037033610008585", type: "barrier", name: "Шлагбаум", room: "Двор" },
  { id: "khikhikhakhakery_thermostat_01", sn: "17036961610007336", type: "thermostat", name: "Термостат", room: "Гостиная" },
  { id: "khikhikhakhakery_counter_01", sn: "17048805610016831", type: "counter", name: "Счётчик", room: "Коридор" },
  { id: "khikhikhakhakery_light_01", sn: "17048841600011160", type: "light", name: "Умный свет", room: "Гостиная" },
] as const;

export interface AuthUser {
  user_fullname: string;
  user_name: string;
  user_surname: string;
  user_phone: string;
  token: string;
  apartment: Array<{ id: string; title: string; complex_title: string; building_address: string }>;
}

export async function authenticate(login: string): Promise<AuthUser | null> {
  if (!login) return null;
  return {
    user_fullname: "Пользователь",
    user_name: login,
    user_surname: "",
    user_phone: "+7 (999) 000-00-00",
    token: "offline_mock_token",
    apartment: [{ id: "apt_01", title: "Кв. 10", complex_title: "ЖК Умный Дом", building_address: "ул. Ленина, 1" }],
  };
}