import { useState, useRef, useEffect } from "react";
import { User, LogOut, Shield, ChevronDown, Building2 } from "lucide-react";
import type { UserProfile } from "../hooks/useAuth";

interface UserMenuProps {
  user: UserProfile;
  onLogout: () => void;
}

export default function UserMenu({
  user,
  onLogout,
}: {
  user: any;
  onLogout: () => void;
}) {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="relative" ref={menuRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-3 py-1.5 bg-slate-700 hover:bg-slate-600 rounded-lg transition-colors"
      >
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center text-white text-xs font-bold">
          {user.initials}
        </div>
        <div className="text-left hidden sm:block">
          <div className="text-sm font-medium text-white leading-tight">
            {user.name}
          </div>
          <div className="text-xs text-slate-400 leading-tight">
            {user.role}
          </div>
        </div>
        <ChevronDown
          className={`w-4 h-4 text-slate-400 transition-transform ${isOpen ? "rotate-180" : ""}`}
        />
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-72 bg-slate-800 border border-slate-700 rounded-xl shadow-2xl overflow-hidden z-50">
          <div className="p-4 border-b border-slate-700">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center text-white font-bold">
                {user.initials}
              </div>
              <div>
                <div className="text-sm font-medium text-white">
                  {user.name}
                </div>
                <div className="text-xs text-slate-400">{user.role}</div>
              </div>
            </div>
          </div>

          <div className="p-2 space-y-1">
            <div className="flex items-center gap-2 px-3 py-2 text-sm text-slate-300">
              <Building2 className="w-4 h-4 text-cyan-400 shrink-0" />
              <div className="flex flex-col">
                <span className="text-xs text-slate-500">Объект</span>
                <span>{user.complexTitle}</span>
              </div>
            </div>
            <div className="flex items-center gap-2 px-3 py-2 text-sm text-slate-300">
              <Shield className="w-4 h-4 text-cyan-400 shrink-0" />
              <div className="flex flex-col">
                <span className="text-xs text-slate-500">Помещение</span>
                <span>{user.apartmentTitle}</span>
              </div>
            </div>
            <div className="flex items-center gap-2 px-3 py-2 text-sm text-slate-300">
              <User className="w-4 h-4 text-cyan-400 shrink-0" />
              <div className="flex flex-col">
                <span className="text-xs text-slate-500">Телефон</span>
                <span>{user.phone || "—"}</span>
              </div>
            </div>
          </div>

          <div className="border-t border-slate-700 p-2">
            <button
              onClick={onLogout}
              className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-400 hover:bg-red-900/20 rounded-lg transition-colors"
            >
              <LogOut className="w-4 h-4" />
              Выйти из системы
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
