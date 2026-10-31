export interface DemoScenario {
    id: string;
    title: string;
    description: string;
    targetAudience: 'Жилец' | 'Управляющая Компания' | 'Инженер';
    nodesCount: number;
    config: any;
  }
  
  export const DEMO_SCENARIOS: DemoScenario[] = [
    {
      id: 'leak-protection',
      title: 'Защита от протечки (Аварийный)',
      description:
        'При фиксации протечки датчиком протечки автоматически перекрывать шаровые краны ввода и отправлять экстренное уведомление в диспетчерскую УК.',
      targetAudience: 'Управляющая Компания',
      nodesCount: 4,
      config: {
        trigger: { device: 'water_leak_sensor', event: 'leak_detected', value: true },
        actions: [
          { device: 'main_water_valve', action: 'close' },
          { service: 'ujin_notification', priority: 'HIGH', message: 'Авария: Протечка в кв. 104!' }
        ]
      }
    },
    {
      id: 'energy-saver',
      title: 'Режим «Никого нет дома» (Энергосбережение)',
      description:
        'По нажатию мастер-выключателя у двери отключить некритичные группы освещения, перевести термоголовки отопления в эко-режим (+18°C).',
      targetAudience: 'Жилец',
      nodesCount: 5,
      config: {
        trigger: { device: 'master_switch', event: 'press_long' },
        actions: [
          { device: 'all_lights', action: 'turn_off' },
          { device: 'thermostat', action: 'set_temperature', value: 18 }
        ]
      }
    },
    {
      id: 'auto-meter-readings',
      title: 'Ежемесячный сбор показаний счетчиков',
      description:
        'Каждое 25-е число месяца опрашивать счетчики ХВС/ГВС/Электроэнергии и передавать данные напрямую в АСКУЭ / 1С Управляющей Компании.',
      targetAudience: 'Управляющая Компания',
      nodesCount: 3,
      config: {
        trigger: { cron: '0 0 25 * *' },
        actions: [
          { device: 'water_meter', action: 'read_values' },
          { service: 'ujin_billing_export', target: 'uk_1c' }
        ]
      }
    }
  ];