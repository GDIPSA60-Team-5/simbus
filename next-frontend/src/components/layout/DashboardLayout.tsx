"use client";

import React, { useState } from 'react';
import { Header } from '@/components/navigation/Header';
import { Sidebar } from '@/components/navigation/Sidebar';
import { WarmBackground } from '@/components/ui/WarmBackground';

interface DashboardLayoutProps {
  children: React.ReactNode;
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
  sidebarItems: Array<{
    id: string;
    label: string;
    icon: React.ComponentType<any>;
    href?: string;
    onClick?: () => void;
  }>;
  activeSidebarItem: string;
  onSidebarItemClick: (id: string) => void;
  variant?: 'dashboard' | 'users' | 'chatbot' | 'analytics' | 'feedback';
}

export const DashboardLayout = ({
  children,
  title,
  subtitle,
  searchPlaceholder,
  onSearch,
  onRefresh,
  isRefreshing,
  notifications,
  onNotificationClick,
  sidebarItems,
  activeSidebarItem,
  onSidebarItemClick,
  variant = 'dashboard',
}: DashboardLayoutProps) => {
  return (
    <WarmBackground variant={variant}>
      <Header
        title={title}
        subtitle={subtitle}
        searchPlaceholder={searchPlaceholder}
        onSearch={onSearch}
        onRefresh={onRefresh}
        isRefreshing={isRefreshing}
        notifications={notifications}
        onNotificationClick={onNotificationClick}
      />
      
      <div className="flex">
        <Sidebar
          items={sidebarItems}
          activeItem={activeSidebarItem}
          onItemClick={onSidebarItemClick}
        />
        
        <main className="flex-1 p-6">
          {children}
        </main>
      </div>
    </WarmBackground>
  );
};