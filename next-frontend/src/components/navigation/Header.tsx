"use client";

import React, { useState } from 'react';
import { Search, Bell, Settings, RefreshCw, Smartphone } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';

interface HeaderProps {
  title?: string;
  subtitle?: string;
  searchPlaceholder?: string;
  onSearch?: (query: string) => void;
  onRefresh?: () => void;
  isRefreshing?: boolean;
  notifications?: Array<{
    id: number;
    title: string;
    message: string;
    type: 'info' | 'warning' | 'success' | 'error';
    time: string;
    read: boolean;
  }>;
  onNotificationClick?: (id: number) => void;
}

export const Header = ({
  title = "Nimbus",
  subtitle = "Admin Dashboard",
  searchPlaceholder = "Search...",
  onSearch,
  onRefresh,
  isRefreshing = false,
  notifications = [],
  onNotificationClick
}: HeaderProps) => {
  const { logout } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [showNotifications, setShowNotifications] = useState(false);
  const [showSettings, setShowSettings] = useState(false);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value;
    setSearchQuery(query);
    onSearch?.(query);
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  const getNotificationTypeColor = (type: string): string => {
    switch (type) {
      case 'success': return 'text-green-600 bg-green-50';
      case 'warning': return 'text-orange-600 bg-orange-50';
      case 'error': return 'text-red-600 bg-red-50';
      default: return 'text-blue-600 bg-blue-50';
    }
  };

  return (
    <header className="backdrop-blur-xl bg-white/30 border-b border-orange-200/30 sticky top-0 z-50 shadow-sm shadow-orange-100/20">
      <div className="flex items-center justify-between px-6 py-4">
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-orange-500 to-amber-600 flex items-center justify-center shadow-lg shadow-orange-500/25">
              <Smartphone className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-amber-900">{title}</h1>
              <p className="text-sm text-amber-700/80">{subtitle}</p>
            </div>
          </div>
        </div>

        <div className="flex items-center space-x-4">
          {/* Search */}
          {onSearch && (
            <div className="relative">
              <Search className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-amber-600" />
              <input
                type="text"
                placeholder={searchPlaceholder}
                value={searchQuery}
                onChange={handleSearchChange}
                className="pl-10 pr-4 py-2 bg-white/40 backdrop-blur-md border border-orange-200/40 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-400/50 focus:border-orange-400/50 text-amber-900 placeholder-amber-600/60"
              />
            </div>
          )}

          {/* Refresh Button */}
          {onRefresh && (
            <button
              onClick={onRefresh}
              disabled={isRefreshing}
              className="p-2 rounded-lg bg-white/40 backdrop-blur-md border border-orange-200/40 hover:bg-white/60 hover:shadow-md hover:shadow-orange-200/20 transition-all duration-300 disabled:opacity-50"
            >
              {isRefreshing ? (
                <LoadingSpinner size="sm" />
              ) : (
                <RefreshCw className="w-5 h-5 text-amber-700" />
              )}
            </button>
          )}

          {/* Settings */}
          <div className="relative">
            <button
              onClick={() => setShowSettings(!showSettings)}
              className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all"
            >
              <Settings className="w-5 h-5 text-gray-600" />
            </button>

            {/* Settings Dropdown */}
            {showSettings && (
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-xl border border-gray-200 z-50">
                <div className="p-2">
                  <button
                    onClick={logout}
                    className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-lg"
                  >
                    Logout
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};