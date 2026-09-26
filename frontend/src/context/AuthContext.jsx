import { createContext, useContext, useEffect, useState } from "react";
import { TOKEN_STORAGE_KEY } from "../api/client";

const AuthContext = createContext(null);
const STORAGE_KEY = "sysbank_user";

export function AuthProvider({ children }) {
  const [user, setUserState] = useState(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    try {
      if (user) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    } catch {
      // ignore storage errors (private browsing, quota, etc.)
    }
  }, [user]);

  // A profile-update response has no token field and must never clear the
  // existing session's token - only persist a new one when actually present
  // (i.e. a login response).
  const setUser = (nextUser) => {
    if (nextUser?.token) {
      try {
        localStorage.setItem(TOKEN_STORAGE_KEY, nextUser.token);
      } catch {
        // ignore storage errors
      }
    }
    if (nextUser) {
      const { token, ...userWithoutToken } = nextUser;
      setUserState(userWithoutToken);
    } else {
      setUserState(null);
    }
  };

  const logout = () => {
    try {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    } catch {
      // ignore storage errors
    }
    setUserState(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
