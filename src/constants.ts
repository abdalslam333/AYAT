export const COLORS = [
  { id: 'dark-blue', value: '#0b1c30', name: 'Dark Blue' },
  { id: 'slate', value: '#1b2b3f', name: 'Slate' },
  { id: 'deep-navy', value: '#031427', name: 'Deep Navy' },
  { id: 'steel', value: '#26364a', name: 'Steel' },
  { id: 'transparent-black', value: 'rgba(0, 0, 0, 0.4)', name: 'Transparent Black' },
];

export interface WidgetSettings {
  backgroundColor: string;
  transparency: number;
  language: 'English' | 'Arabic';
}

export const INITIAL_SETTINGS: WidgetSettings = {
  backgroundColor: COLORS[0].value,
  transparency: 60,
  language: 'English',
};
