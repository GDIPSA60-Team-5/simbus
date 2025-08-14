"use client";

import { usePathname } from 'next/navigation';
import { navigationItems, getActiveNavItem } from '@/lib/navigation';

export const useNavigation = () => {
  const pathname = usePathname();
  const activeItem = getActiveNavItem(pathname);

  return {
    items: navigationItems,
    activeItem,
  };
};