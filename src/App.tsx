import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Settings, Plus, RefreshCw, Palette, Globe, Share2, Instagram, Flag, Info, ArrowLeft, Copy, Check } from 'lucide-react';
import { COLORS, INITIAL_SETTINGS, type WidgetSettings } from './constants';

const WIDGET_DATA = {
  arabic: "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
  english: "\"Indeed, with hardship [will be] ease.\"",
  source: "Quran 94:6",
  explanation: "بشارة عظيمة، أن مع كل ضيق وعسر يوجد يسر مصاحب له، بل إن العسر محفوف بيسرين: يسر قبله ويسر بعده."
};

export default function App() {
  const [view, setView] = React.useState<'widget' | 'detail' | 'settings'>('widget');
  const [settings, setSettings] = React.useState<WidgetSettings>(INITIAL_SETTINGS);

  return (
    <div className="relative w-full max-w-md mx-auto min-h-screen bg-background overflow-hidden flex flex-col">
      <AnimatePresence mode="wait text-white">
        {view === 'widget' ? (
          <motion.div
            key="widget-view"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="flex-grow flex flex-col"
          >
            <WidgetView 
              settings={settings} 
              onOpenDetail={() => setView('detail')} 
            />
          </motion.div>
        ) : view === 'detail' ? (
          <motion.div
            key="detail-view"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            className="flex-grow flex flex-col"
          >
            <DetailView
              onBack={() => setView('widget')}
              onOpenSettings={() => setView('settings')}
            />
          </motion.div>
        ) : (
          <motion.div
            key="settings-view"
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="flex-grow flex flex-col"
          >
            <SettingsView 
              settings={settings} 
              setSettings={setSettings} 
              onBack={() => setView('detail')} 
            />
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

function WidgetView({ settings, onOpenDetail }: { settings: WidgetSettings, onOpenDetail: () => void }) {
  const bgOpacity = settings.transparency / 100;
  
  return (
    <div className="flex-grow flex items-center justify-center p-6 relative">
      <button 
        id="widget-container"
        onClick={onOpenDetail}
        className="w-full aspect-[4/3] rounded-3xl flex flex-col justify-between p-10 relative overflow-hidden group shadow-2xl transition-all duration-500 text-left hover:scale-[1.02] active:scale-[0.98]"
        style={{ 
          backgroundColor: settings.backgroundColor.includes('rgba') 
            ? settings.backgroundColor.replace(/[\d.]+\)$/, `${bgOpacity})`)
            : `${settings.backgroundColor}${Math.round(bgOpacity * 255).toString(16).padStart(2, '0')}`,
          backdropFilter: 'blur(12px)'
        }}
      >
        {/* Subtle Background Glow */}
        <div className="absolute -top-24 -left-24 w-64 h-64 bg-white/5 rounded-full blur-3xl opacity-50 transition-opacity group-hover:opacity-100 duration-1000" />
        
        <div className="flex-grow" />

        <div className="flex flex-col items-center text-center space-y-6 z-10 w-full">
          <p className="text-3xl md:text-4xl text-white font-heading arabic-text leading-relaxed tracking-wider" dir="rtl">
            {WIDGET_DATA.arabic}
          </p>
          <p className="text-lg text-on-surface-variant max-w-[90%] font-sans leading-relaxed">
            {WIDGET_DATA.english}
          </p>
        </div>

        <div className="flex-grow flex items-end justify-end w-full z-10">
          <span className="text-xs uppercase tracking-[0.2em] text-outline font-medium">
            {WIDGET_DATA.source}
          </span>
        </div>
      </button>
    </div>
  );
}

function DetailView({ onBack, onOpenSettings }: { onBack: () => void, onOpenSettings: () => void }) {
  const [copied, setCopied] = React.useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(`${WIDGET_DATA.arabic}\n${WIDGET_DATA.english}\n${WIDGET_DATA.source}`);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="flex-grow flex flex-col h-full bg-background overflow-y-auto">
      <header className="px-6 h-16 flex items-center justify-between sticky top-0 bg-background/80 backdrop-blur-md z-10">
        <button onClick={onBack} className="p-2 -ml-2 rounded-full hover:bg-white/5 text-white active:scale-90 transition-transform">
          <ArrowLeft size={24} />
        </button>
        <button onClick={onOpenSettings} className="p-2 -mr-2 rounded-full hover:bg-white/5 text-white active:scale-90 transition-transform">
          <Settings size={24} />
        </button>
      </header>

      <main className="p-8 space-y-10">
        <div className="space-y-6 text-center">
          <p className="text-4xl md:text-5xl text-white font-heading arabic-text leading-tight" dir="rtl">
            {WIDGET_DATA.arabic}
          </p>
          <p className="text-xl text-on-surface-variant font-sans italic">
            {WIDGET_DATA.english}
          </p>
          <p className="text-sm uppercase tracking-widest text-outline">
            {WIDGET_DATA.source}
          </p>
        </div>

        <div className="bg-surface-container rounded-2xl p-6 space-y-4 border border-white/5">
          <h3 className="text-xs uppercase tracking-widest text-on-surface-variant font-bold">Explanation / التفسير</h3>
          <p className="text-on-background leading-relaxed text-lg" dir="rtl">
            {WIDGET_DATA.explanation}
          </p>
        </div>

        <button 
          onClick={handleCopy}
          className="w-full py-4 rounded-xl bg-white/10 hover:bg-white/20 text-white font-medium flex items-center justify-center space-x-3 transition-all active:scale-[0.98]"
        >
          {copied ? <Check size={20} className="text-green-400" /> : <Copy size={20} />}
          <span>{copied ? 'Copied' : 'Copy Verse'}</span>
        </button>
      </main>
    </div>
  );
}

function SettingsView({ settings, setSettings, onBack }: { 
  settings: WidgetSettings, 
  setSettings: React.Dispatch<React.SetStateAction<WidgetSettings>>,
  onBack: () => void 
}) {
  return (
    <div className="flex-grow flex flex-col h-full bg-background z-50 overflow-y-auto">
      <header className="bg-black/50 backdrop-blur-md sticky top-0 z-10">
        <div className="flex items-center justify-between px-6 h-16">
          <button onClick={onBack} className="p-2 -ml-2 rounded-full hover:bg-white/5 text-white active:scale-90 transition-transform">
            <ArrowLeft size={24} />
          </button>
          <h1 className="font-heading font-bold text-white text-lg">Settings</h1>
          <div className="w-10" />
        </div>
      </header>

      <main className="p-6 space-y-8">
        <div className="bg-surface-container rounded-2xl p-6 space-y-6 border border-white/5 shadow-xl">
          <div className="space-y-4">
            <label className="text-[10px] uppercase tracking-widest text-on-surface-variant font-bold">Background Color</label>
            <div className="flex flex-wrap gap-4">
              {COLORS.map((color) => (
                <button
                  key={color.id}
                  onClick={() => setSettings(s => ({ ...s, backgroundColor: color.value }))}
                  className={`w-10 h-10 rounded-full transition-all border-4 ${
                    settings.backgroundColor === color.value 
                      ? 'border-white scale-110 shadow-lg' 
                      : 'border-transparent scale-100 hover:scale-105'
                  }`}
                  style={{ backgroundColor: color.value }}
                  title={color.name}
                />
              ))}
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <label className="text-[10px] uppercase tracking-widest text-on-surface-variant font-bold">Transparency</label>
              <span className="text-sm font-medium text-white">{settings.transparency}%</span>
            </div>
            <div className="relative">
              <input
                type="range"
                min="0"
                max="100"
                value={settings.transparency}
                onChange={(e) => setSettings(s => ({ ...s, transparency: parseInt(e.target.value) }))}
                className="w-full h-1 bg-surface-container-low rounded-lg appearance-none cursor-pointer accent-white"
                style={{
                  background: `linear-gradient(to right, #fff ${settings.transparency}%, #1b2b3f ${settings.transparency}%)`
                }}
              />
              <div className="flex justify-between mt-2 px-1">
                <span className="text-[10px] text-outline uppercase font-medium">Clear</span>
                <span className="text-[10px] text-outline uppercase font-medium">Solid</span>
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-1">
          <SettingsItem icon={<Plus size={22} />} label="Add a new widget" />
          <SettingsItem icon={<RefreshCw size={22} />} label="Change Content / Ayah" />
          <div className="h-4" />
          <SettingsItem icon={<Palette size={22} />} label="Appearance" />
          <SettingsItem icon={<Globe size={22} />} label="Language" value="English" />
          <div className="h-4" />
          <SettingsItem icon={<Share2 size={22} />} label="Share App" />
          <SettingsItem icon={<Instagram size={22} />} label="Follow our page" />
          <SettingsItem icon={<Flag size={22} />} label="Report a problem" />
          <div className="h-4" />
          <SettingsItem icon={<Info size={22} />} label="How to use" />
        </div>
      </main>
    </div>
  );
}

function SettingsItem({ icon, label, value }: { icon: React.ReactNode, label: string, value?: string }) {
  return (
    <button className="flex items-center justify-between w-full p-4 rounded-xl hover:bg-white/5 active:bg-white/10 transition-colors group">
      <div className="flex items-center space-x-4">
        <span className="text-on-surface-variant group-hover:text-white transition-colors">{icon}</span>
        <span className="text-lg text-on-background font-medium">{label}</span>
      </div>
      {value && <span className="text-sm text-on-surface-variant font-medium">{value}</span>}
    </button>
  );
}
