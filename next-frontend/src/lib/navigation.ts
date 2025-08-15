import { 
  Activity, 
  Bot, 
  Star, 
  BarChart3 
} from 'lucide-react';

export interface NavigationItem {
  id: string;
  label: string;
  icon: React.ComponentType<any>;
  href: string;
}

export const navigationItems: NavigationItem[] = [
  { id: 'overview', label: 'Overview', icon: Activity, href: '/dashboard' },
  { id: 'analytics', label: 'App Analytics', icon: BarChart3, href: '/analytics' },
  { id: 'chatbot', label: 'Chatbot Analytics', icon: Bot, href: '/chatbot' },
  { id: 'feedback', label: 'User Feedback', icon: Star, href: '/feedback' },
];

export const getActiveNavItem = (pathname: string): string => {
  // Remove leading slash and get first segment
  const path = pathname.replace(/^\//, '').split('/')[0];
  
  // Map paths to navigation item IDs
  switch (path) {
    case 'dashboard':
      return 'overview';
    case 'chatbot':
      return 'chatbot';
    case 'feedback':
      return 'feedback';
    case 'analytics':
      return 'analytics';
    default:
      return 'overview';
  }
};