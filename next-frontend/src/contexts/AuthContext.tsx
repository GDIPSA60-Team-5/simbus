"use client";

import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import {
  decodeJWT,
  isTokenExpired,
  getTokenExpirationTime,
  getUsernameFromToken,
  isValidTokenFormat
} from '@/utils/tokenUtils';

interface User {
  username: string;
  token: string;
  userType?: string;
  expiresAt?: number;
}

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  isAdmin: boolean;
  loading: boolean;
  setAuthCookie: (token: string) => void;
  clearAuthCookie: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuthContext = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuthContext must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: React.ReactNode;
}

// Helper function to validate and set user from token
const validateAndSetUser = async (token: string, setUser: (user: User | null) => void): Promise<boolean> => {
  if (!isValidTokenFormat(token)) {
    console.warn('Invalid token format');
    return false;
  }

  if (isTokenExpired(token)) {
    console.warn('Token is expired');
    return false;
  }

  const username = getUsernameFromToken(token);
  if (!username) {
    console.warn('Could not extract username from token');
    return false;
  }

  try {
    // Fetch user details including userType
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '/';
    const response = await fetch(`${backendUrl}api/auth/me`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });

    if (response.ok) {
      const userData = await response.json();
      const decoded = decodeJWT(token);
      setUser({
        username: userData.username,
        userType: userData.userType,
        token,
        expiresAt: decoded?.exp ? decoded.exp * 1000 : undefined
      });
      return true;
    } else {
      console.warn('Failed to fetch user details');
      // Fallback to basic user info from token
      const decoded = decodeJWT(token);
      setUser({
        username,
        token,
        expiresAt: decoded?.exp ? decoded.exp * 1000 : undefined
      });
      return true;
    }
  } catch (error) {
    console.warn('Error fetching user details:', error);
    // Fallback to basic user info from token
    const decoded = decodeJWT(token);
    setUser({
      username,
      token,
      expiresAt: decoded?.exp ? decoded.exp * 1000 : undefined
    });
    return true;
  }
};

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  // Set auth cookie (both in localStorage and as HTTP cookie)
  const setAuthCookie = useCallback((token: string) => {
    // Store in localStorage for immediate access
    localStorage.setItem('authToken', token);

    // Set HTTP cookie for middleware
    const decoded = decodeJWT(token);
    const maxAge = decoded?.exp ? decoded.exp - Math.floor(Date.now() / 1000) : 86400; // 1 day default

    document.cookie = `authToken=${token}; path=/; max-age=${maxAge}; SameSite=Lax${location.protocol === 'https:' ? '; Secure' : ''}`;
  }, []);

  // Clear auth cookie
  const clearAuthCookie = useCallback(() => {
    localStorage.removeItem('authToken');
    document.cookie = 'authToken=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
  }, []);

  // Auto logout with cleanup
  const logout = useCallback(() => {
    console.log('Logging out user');
    clearAuthCookie();
    setUser(null);
    router.push('/login');
  }, [clearAuthCookie, router]);

  // Initialize authentication state
  useEffect(() => {
    let isMounted = true;

    const initializeAuth = async () => {
      if (!isMounted) return;

      const token = localStorage.getItem('authToken');

      if (token) {
        const isValid = await validateAndSetUser(token, setUser);
        if (!isValid) {
          localStorage.removeItem('authToken');
          document.cookie = 'authToken=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
        }
      }

      if (isMounted) {
        setLoading(false);
      }
    };

    initializeAuth();

    return () => {
      isMounted = false;
    };
  }, []); // Empty dependency array - only run once on mount

  // Auto-logout on token expiration
  useEffect(() => {
    if (user?.token) {
      const timeUntilExpiry = getTokenExpirationTime(user.token);

      if (timeUntilExpiry && timeUntilExpiry > 0) {
        const timeoutId = setTimeout(() => {
          console.log('Token expired, automatically logging out');
          logout();
        }, timeUntilExpiry);

        return () => clearTimeout(timeoutId);
      } else if (timeUntilExpiry === 0) {
        // Token is already expired
        logout();
      }
    }
  }, [user?.token, logout]);

  const login = useCallback(async (username: string, password: string): Promise<void> => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '/';

    const response = await fetch(`${backendUrl}api/admin/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Login failed' }));
      throw new Error(errorData.error || 'Login failed');
    }

    const data = await response.json();
    const token = data.token;

    if (!token) {
      throw new Error('No token received from server');
    }

    // Set auth cookie
    setAuthCookie(token);

    // Validate and set user from token
    const isValid = await validateAndSetUser(token, setUser);
    if (!isValid) {
      throw new Error('Invalid token received from server');
    }
  }, [setAuthCookie]);

  const value: AuthContextType = {
    user,
    login,
    logout,
    isAuthenticated: !!user && !loading,
    isAdmin: user?.userType === 'admin',
    loading,
    setAuthCookie,
    clearAuthCookie,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};