import { useState, useEffect } from "react";

const STORAGE_KEY = "ujin_user_v3";

export function useAuth() {
  const [user, setUserState] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (parsed && parsed.name) setUserState(parsed);
      }
    } catch (e) {
      console.error(e);
    }
    setIsLoading(false);
  }, []);

  const setUser = (profile: any) => {
    setUserState(profile);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(profile));
  };

  const logout = () => {
    setUserState(null);
    localStorage.removeItem(STORAGE_KEY);
  };

  return { user, isLoading, logout, setUser };
}