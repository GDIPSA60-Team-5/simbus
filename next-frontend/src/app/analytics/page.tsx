"use client";

import React, { useState } from 'react';
import {
  BarChart3,
  Activity,
  Users,
  Clock,
  Server,
  Zap,
  TrendingUp,
  AlertTriangle,
  Cpu,
  HardDrive,
  Globe,
  Calendar,
  Star,
  Bot
} from 'lucide-react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { StatCard } from '@/components/dashboard/StatCard';
import { RouteGuard } from '@/components/auth/RouteGuard';
import { usePrometheus } from '@/hooks/usePrometheus';
import { useNavigation } from '@/hooks/useNavigation';
import { LineChart } from '@/components/charts/LineChart';

const AnalyticsPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [timeRange, setTimeRange] = useState('24h');

  const { data: metrics, loading, error, refresh } = usePrometheus();
  const { items: sidebarItems, activeItem } = useNavigation();

  // Format numbers for display
  const formatNumber = (num: number | null): string => {
    if (num === null) return '---';
    if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`;
    if (num >= 1000) return `${(num / 1000).toFixed(1)}K`;
    return Math.round(num).toString();
  };

  const formatPercentage = (num: number | null): string => {
    if (num === null) return '---';
    return `${num.toFixed(1)}%`;
  };

  const formatTime = (num: number | null): string => {
    if (num === null) return '---';
    if (num >= 1000) return `${(num / 1000).toFixed(1)}s`;
    return `${Math.round(num)}ms`;
  };

  // Performance stats
  const performanceStats = metrics ? [
    {
      title: 'CPU Usage',
      value: formatPercentage(metrics.cpuUsage),
      change: metrics.cpuUsage && metrics.cpuUsage > 80 ? 'High usage' : 'Normal',
      icon: Cpu,
      iconBgColor: 'from-red-500 to-red-600',
      color: metrics.cpuUsage && metrics.cpuUsage > 80 ? 'text-red-600' : 'text-green-600',
      trend: metrics.cpuUsage && metrics.cpuUsage > 80 ? 'down' as const : 'up' as const,
      description: 'Server CPU utilization'
    },
    {
      title: 'Memory Usage',
      value: metrics.memoryUsage ? `${formatNumber(metrics.memoryUsage)}MB` : '---',
      change: metrics.memoryUsage && metrics.memoryUsage > 1000 ? 'High usage' : 'Normal',
      icon: HardDrive,
      iconBgColor: 'from-blue-500 to-blue-600',
      color: metrics.memoryUsage && metrics.memoryUsage > 1000 ? 'text-orange-600' : 'text-blue-600',
      trend: 'neutral' as const,
      description: 'RAM consumption'
    },
    {
      title: 'Response Time',
      value: formatTime(metrics.responseTime),
      change: metrics.responseTime && metrics.responseTime > 500 ? 'Slow responses' : 'Fast responses',
      icon: Clock,
      iconBgColor: 'from-purple-500 to-purple-600',
      color: metrics.responseTime && metrics.responseTime > 500 ? 'text-red-600' : 'text-green-600',
      trend: metrics.responseTime && metrics.responseTime > 500 ? 'down' as const : 'up' as const,
      description: '95th percentile response time'
    },
    {
      title: 'Error Rate',
      value: formatPercentage(metrics.errorRate),
      change: metrics.errorRate && metrics.errorRate > 5 ? 'High errors' : 'Low errors',
      icon: AlertTriangle,
      iconBgColor: metrics.errorRate && metrics.errorRate > 5 ? 'from-red-500 to-red-600' : 'from-green-500 to-green-600',
      color: metrics.errorRate && metrics.errorRate > 5 ? 'text-red-600' : 'text-green-600',
      trend: metrics.errorRate && metrics.errorRate > 5 ? 'down' as const : 'up' as const,
      description: '5xx error rate'
    }
  ] : [];

  // Traffic stats
  const trafficStats = metrics ? [
    {
      title: 'Active Users',
      value: formatNumber(metrics.currentUsers),
      change: 'Current active',
      icon: Users,
      iconBgColor: 'from-orange-500 to-orange-600',
      color: 'text-orange-600',
      trend: 'up' as const,
      description: 'Currently active users'
    },
    {
      title: 'Requests/sec',
      value: formatNumber(metrics.requestsPerSecond),
      change: 'Real-time',
      icon: Zap,
      iconBgColor: 'from-yellow-500 to-yellow-600',
      color: 'text-yellow-600',
      trend: 'up' as const,
      description: 'Requests per second'
    },
    {
      title: 'Total Requests',
      value: formatNumber(metrics.totalRequests),
      change: 'Last 24h',
      icon: Globe,
      iconBgColor: 'from-indigo-500 to-indigo-600',
      color: 'text-indigo-600',
      trend: 'up' as const,
      description: 'Total requests today'
    }
  ] : [];


  return (
    <RouteGuard requireAuth={true}>
      <DashboardLayout
        title="App Analytics"
        subtitle="Real-time performance and usage analytics"
        searchPlaceholder="Search metrics..."
        onSearch={setSearchQuery}
        onRefresh={refresh}
        isRefreshing={loading}
        notifications={[]}
        onNotificationClick={() => { }}
        sidebarItems={sidebarItems}
        activeSidebarItem={activeItem}
        onSidebarItemClick={() => { }}
        variant="analytics"
      >
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
            <strong className="font-bold">Error fetching metrics: </strong>
            <span className="block sm:inline">{error}</span>
            <button
              onClick={refresh}
              className="ml-4 bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600"
            >
              Retry
            </button>
          </div>
        )}

        {/* Performance Metrics */}
        <div className="mb-8">
          <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
            <Server className="w-5 h-5 mr-2" />
            Performance Metrics
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {loading ? (
              Array.from({ length: 4 }, (_, i) => (
                <StatCard
                  key={i}
                  title=""
                  value=""
                  icon={Activity}
                  loading={true}
                />
              ))
            ) : performanceStats.length > 0 ? (
              performanceStats.map((stat, index) => (
                <StatCard
                  key={index}
                  title={stat.title}
                  value={stat.value}
                  change={stat.change}
                  icon={stat.icon}
                  iconBgColor={stat.iconBgColor}
                  color={stat.color}
                  trend={stat.trend}
                  description={stat.description}
                />
              ))
            ) : (
              <div className="col-span-full text-center text-gray-500 py-8">
                No performance metrics available
              </div>
            )}
          </div>
        </div>

        {/* Traffic Metrics */}
        <div className="mb-8">
          <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
            <TrendingUp className="w-5 h-5 mr-2" />
            Traffic & Usage
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {loading ? (
              Array.from({ length: 3 }, (_, i) => (
                <StatCard
                  key={i}
                  title=""
                  value=""
                  icon={Users}
                  loading={true}
                />
              ))
            ) : trafficStats.length > 0 ? (
              trafficStats.map((stat, index) => (
                <StatCard
                  key={index}
                  title={stat.title}
                  value={stat.value}
                  change={stat.change}
                  icon={stat.icon}
                  iconBgColor={stat.iconBgColor}
                  color={stat.color}
                  trend={stat.trend}
                  description={stat.description}
                />
              ))
            ) : (
              <div className="col-span-full text-center text-gray-500 py-8">
                No traffic metrics available
              </div>
            )}
          </div>
        </div>

        {/* Peak Usage Analysis */}
        {metrics && (metrics.peakHour || metrics.peakDay) && (
          <div className="mb-8">
            <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
              <Calendar className="w-5 h-5 mr-2" />
              Peak Usage Analysis
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {metrics.peakHour && (
                <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
                  <div className="flex items-center space-x-3 mb-4">
                    <div className="p-3 rounded-xl bg-gradient-to-br from-orange-500 to-orange-600">
                      <Clock className="w-6 h-6 text-white" />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">Peak Hour</h3>
                      <p className="text-2xl font-bold text-orange-600">
                        {metrics.peakHour.hour}:00
                      </p>
                    </div>
                  </div>
                  <p className="text-sm text-gray-600">
                    Highest traffic: {formatNumber(metrics.peakHour.requests)} requests
                  </p>
                </div>
              )}

              {metrics.peakDay && (
                <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
                  <div className="flex items-center space-x-3 mb-4">
                    <div className="p-3 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600">
                      <Calendar className="w-6 h-6 text-white" />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">Peak Day</h3>
                      <p className="text-2xl font-bold text-blue-600">
                        {metrics.peakDay.day}
                      </p>
                    </div>
                  </div>
                  <p className="text-sm text-gray-600">
                    Daily requests: {formatNumber(metrics.peakDay.requests)}
                  </p>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Time Series Charts */}
        {metrics && (
          <div className="mb-8">
            <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
              <BarChart3 className="w-5 h-5 mr-2" />
              Performance Trends (24 hours)
            </h2>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <LineChart
                data={metrics.cpuTimeSeries}
                title="CPU Usage"
                color="#EF4444"
                unit="%"
                height={200}
              />
              <LineChart
                data={metrics.memoryTimeSeries}
                title="Memory Usage"
                color="#3B82F6"
                unit="MB"
                height={200}
              />
              <LineChart
                data={metrics.requestsTimeSeries}
                title="Requests per Second"
                color="#F59E0B"
                unit="/s"
                height={200}
              />
              <LineChart
                data={metrics.responseTimeTimeSeries}
                title="Response Time"
                color="#8B5CF6"
                unit="ms"
                height={200}
              />
            </div>
          </div>
        )}

        {/* System Health Summary */}
        <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
            <Activity className="w-5 h-5 mr-2" />
            System Health Summary
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-center">
            <div>
              <div className="text-3xl font-bold text-green-600">
                {metrics ? '✓' : '...'}
              </div>
              <div className="text-sm text-gray-600">System Status</div>
              <div className="text-xs text-gray-500">
                {metrics ? 'All systems operational' : 'Loading...'}
              </div>
            </div>
            <div>
              <div className="text-2xl font-bold text-blue-600">
                {metrics ? formatNumber(metrics.requestsPerSecond) : '---'}
              </div>
              <div className="text-sm text-gray-600">Current Load</div>
              <div className="text-xs text-gray-500">Requests per second</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-purple-600">
                {metrics && metrics.responseTime ? formatTime(metrics.responseTime) : '---'}
              </div>
              <div className="text-sm text-gray-600">Avg Response</div>
              <div className="text-xs text-gray-500">Response time</div>
            </div>
          </div>
        </div>
      </DashboardLayout>
    </RouteGuard>
  );
};

export default AnalyticsPage;