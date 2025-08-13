import { TrendingUp, TrendingDown } from 'lucide-react';
import { LoadingSkeleton } from '@/components/ui/LoadingSkeleton';

interface StatCardProps {
  title: string;
  value: string;
  change?: string;
  icon: React.ComponentType<any>;
  color?: string;
  iconBgColor?: string;
  loading?: boolean;
  trend?: 'up' | 'down' | 'neutral' | 'none';
  description?: string;
  onClick?: () => void;
}

export const StatCard = ({ 
  title, 
  value, 
  change, 
  icon: Icon, 
  color = 'text-gray-600',
  iconBgColor = 'from-orange-500 to-amber-600',
  loading = false,
  trend = 'neutral',
  description,
  onClick
}: StatCardProps) => {
  if (loading) {
    return (
      <div className="backdrop-blur-xl bg-white/40 border border-orange-200/30 rounded-2xl p-6 shadow-lg shadow-orange-100/20">
        <div className="flex items-center justify-between mb-4">
          <div className="p-3 rounded-xl bg-orange-200/50 animate-pulse">
            <div className="w-6 h-6" />
          </div>
          <LoadingSkeleton width="w-4" height="h-4" />
        </div>
        <div>
          <LoadingSkeleton height="h-8" className="mb-2" />
          <LoadingSkeleton height="h-4" className="mb-1" />
          <LoadingSkeleton width="w-16" height="h-3" />
        </div>
      </div>
    );
  }

  const getTrendIcon = () => {
    switch (trend) {
      case 'up':
        return <TrendingUp className={`w-4 h-4 text-emerald-600`} />;
      case 'down':
        return <TrendingDown className={`w-4 h-4 text-red-500`} />;
      case 'neutral':
        return <TrendingUp className={`w-4 h-4 ${color}`} />;
      case 'none':
      default:
        return null;
    }
  };

  return (
    <div 
      className={`backdrop-blur-xl bg-white/40 border border-orange-200/30 rounded-2xl p-6 hover:bg-white/50 hover:shadow-lg hover:shadow-orange-200/25 transition-all duration-300 ${onClick ? 'cursor-pointer' : ''} shadow-md shadow-orange-100/20`}
      onClick={onClick}
    >
      <div className="flex items-center justify-between mb-4">
        <div className={`p-3 rounded-xl bg-gradient-to-br ${iconBgColor} shadow-lg`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
        {getTrendIcon()}
      </div>
      <div>
        <h3 className="text-2xl font-bold text-amber-900">{value}</h3>
        <div className="text-sm mb-1 text-amber-800/80">{title}</div>
        {change && <span className={`text-sm font-medium ${color.replace('text-gray-600', 'text-amber-700')}`}>{change}</span>}
        {description && <p className="text-xs text-amber-700/60 mt-1">{description}</p>}
      </div>
    </div>
  );
};