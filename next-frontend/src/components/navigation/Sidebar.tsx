"use client";

import React from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

interface SidebarItem {
  id: string;
  label: string;
  icon: React.ComponentType<any>;
  href?: string;
  onClick?: () => void;
}

interface SidebarProps {
  items: SidebarItem[];
  activeItem: string;
  onItemClick: (id: string) => void;
  className?: string;
}

export const Sidebar = ({ items, activeItem, onItemClick, className = '' }: SidebarProps) => {
  const router = useRouter();

  const handleItemClick = (item: SidebarItem) => {
    onItemClick(item.id);
    
    // Handle navigation
    if (item.href) {
      router.push(item.href);
    }
    
    // Call custom onClick if provided
    item.onClick?.();
  };

  return (
    <aside className={`w-64 min-h-screen backdrop-blur-xl bg-white/30 border-r border-orange-200/30 shadow-lg ${className}`}>
      <nav className="p-6">
        <ul className="space-y-3">
          {items.map((item) => {
            const buttonContent = (
              <>
                <item.icon className="w-5 h-5" />
                <span className="font-medium">{item.label}</span>
              </>
            );

            const buttonClasses = `w-full flex items-center space-x-3 px-4 py-3 rounded-xl text-left transition-all duration-300 ${
              activeItem === item.id
                ? 'bg-gradient-to-r from-orange-500 to-amber-500 text-white shadow-lg shadow-orange-500/25 backdrop-blur-sm'
                : 'text-amber-900 hover:bg-white/50 hover:backdrop-blur-md hover:shadow-md hover:shadow-orange-200/20 backdrop-blur-sm border border-white/20'
            }`;

            return (
              <li key={item.id}>
                {item.href ? (
                  <Link href={item.href} className={buttonClasses}>
                    {buttonContent}
                  </Link>
                ) : (
                  <button
                    onClick={() => handleItemClick(item)}
                    className={buttonClasses}
                  >
                    {buttonContent}
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      </nav>
    </aside>
  );
};