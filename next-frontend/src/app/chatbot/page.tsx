"use client";

import React, { useState } from 'react';
import {
  Bot,
  MessageSquare,
  CheckCircle,
  XCircle,
  TrendingUp,
  Clock,
  Users,
  Activity,
  Star,
  BarChart3,
  Calendar,
  Filter
} from 'lucide-react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { StatCard } from '@/components/dashboard/StatCard';
import { RouteGuard } from '@/components/auth/RouteGuard';
import { useStats } from '@/hooks/useStats';
import { useNavigation } from '@/hooks/useNavigation';

const ChatbotAnalyticsPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [timeFilter, setTimeFilter] = useState('7d');
  
  const { stats, loading: statsLoading, error: statsError, refresh } = useStats();
  const { items: sidebarItems, activeItem } = useNavigation();


  // Mock data for additional chatbot metrics (in a real app, this would come from API)
  const mockAnalytics = {
    avgResponseTime: '2.3s',
    popularQueries: [
      { query: 'Bus schedule to NUS', count: 245, percentage: 85 },
      { query: 'Nearest bus stop', count: 189, percentage: 78 },
      { query: 'Route planning', count: 156, percentage: 65 },
      { query: 'Traffic updates', count: 134, percentage: 58 },
      { query: 'Bus fare information', count: 98, percentage: 45 },
    ],
    hourlyActivity: [
      { hour: '6AM', requests: 45 },
      { hour: '7AM', requests: 78 },
      { hour: '8AM', requests: 156 },
      { hour: '9AM', requests: 234 },
      { hour: '10AM', requests: 189 },
      { hour: '11AM', requests: 167 },
      { hour: '12PM', requests: 201 },
      { hour: '1PM', requests: 178 },
      { hour: '2PM', requests: 145 },
      { hour: '3PM', requests: 167 },
      { hour: '4PM', requests: 189 },
      { hour: '5PM', requests: 223 },
      { hour: '6PM', requests: 267 },
      { hour: '7PM', requests: 234 },
      { hour: '8PM', requests: 178 },
      { hour: '9PM', requests: 134 },
      { hour: '10PM', requests: 89 },
      { hour: '11PM', requests: 56 },
    ],
  };

  const chatbotStats = stats ? [
    {
      title: 'Total Requests',
      value: stats.botRequestCount.toLocaleString(),
      change: '+12% vs last week',
      icon: MessageSquare,
      iconBgColor: 'from-blue-500 to-blue-600',
      color: 'text-blue-600',
      trend: 'up' as const,
      description: 'Total chatbot interactions'
    },
    {
      title: 'Success Rate',
      value: `${stats.botSuccessRate.toFixed(1)}%`,
      change: '+2.3% vs last week',
      icon: CheckCircle,
      iconBgColor: 'from-green-500 to-green-600',
      color: 'text-green-600',
      trend: 'up' as const,
      description: 'Successfully resolved queries'
    },
    {
      title: 'Avg Response Time',
      value: mockAnalytics.avgResponseTime,
      change: '-0.5s vs last week',
      icon: Clock,
      iconBgColor: 'from-purple-500 to-purple-600',
      color: 'text-purple-600',
      trend: 'down' as const,
      description: 'Average response time'
    },
    {
      title: 'Failed Requests',
      value: (stats.botRequestCount - stats.botSuccessCount).toLocaleString(),
      change: `${((stats.botRequestCount - stats.botSuccessCount) / stats.botRequestCount * 100).toFixed(1)}% of total`,
      icon: XCircle,
      iconBgColor: 'from-red-500 to-red-600',
      color: 'text-red-600',
      trend: 'neutral' as const,
      description: 'Queries that failed to resolve'
    },
  ] : [];

  const maxActivity = Math.max(...mockAnalytics.hourlyActivity.map(h => h.requests));

  return (
    <RouteGuard requireAuth={true}>
      <DashboardLayout
        title="Chatbot Analytics"
        subtitle="Monitor AI assistant performance and usage patterns"
        searchPlaceholder="Search analytics..."
        onSearch={setSearchQuery}
        onRefresh={refresh}
        isRefreshing={statsLoading}
        notifications={[]}
        onNotificationClick={() => {}}
        sidebarItems={sidebarItems}
        activeSidebarItem={activeItem}
        onSidebarItemClick={() => {}}
        variant="chatbot"
      >
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {statsError && (
            <div className="col-span-full bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              <strong className="font-bold">Stats Error: </strong>
              <span className="block sm:inline">{statsError}</span>
            </div>
          )}
          
          {statsLoading ? (
            Array.from({ length: 4 }, (_, i) => (
              <StatCard
                key={i}
                title=""
                value=""
                icon={Bot}
                loading={true}
              />
            ))
          ) : chatbotStats.length > 0 ? (
            chatbotStats.map((stat, index) => (
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
                loading={false}
              />
            ))
          ) : (
            <div className="col-span-full text-center text-gray-500 py-8">
              No chatbot stats available
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Popular Queries */}
          <div className="lg:col-span-2 backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-gray-900">Popular Queries</h2>
              <div className="flex items-center space-x-2">
                <select
                  value={timeFilter}
                  onChange={(e) => setTimeFilter(e.target.value)}
                  className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="24h">Last 24 hours</option>
                  <option value="7d">Last 7 days</option>
                  <option value="30d">Last 30 days</option>
                </select>
              </div>
            </div>

            <div className="space-y-4">
              {mockAnalytics.popularQueries.map((query, index) => (
                <div key={index} className="p-4 rounded-xl bg-white/40 border border-white/30 hover:bg-white/60 transition-all">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center space-x-3">
                      <div className="flex items-center justify-center w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 text-white text-sm font-bold">
                        {index + 1}
                      </div>
                      <div>
                        <p className="font-medium text-gray-900">{query.query}</p>
                        <p className="text-sm text-gray-600">{query.count} requests</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-gray-900">{query.percentage}%</p>
                      <p className="text-xs text-gray-500">success rate</p>
                    </div>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-gradient-to-r from-blue-500 to-blue-600 h-2 rounded-full transition-all"
                      style={{ width: `${query.percentage}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Quick Stats */}
          <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-6">Performance Metrics</h2>
            <div className="space-y-6">
              <div className="text-center">
                <div className="text-3xl font-bold text-green-600">
                  {stats ? `${stats.botSuccessRate.toFixed(1)}%` : '---'}
                </div>
                <div className="text-sm text-gray-600 mb-2">Overall Success Rate</div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div 
                    className="bg-gradient-to-r from-green-500 to-green-600 h-2 rounded-full"
                    style={{ width: `${stats ? stats.botSuccessRate : 0}%` }}
                  />
                </div>
              </div>
              
              <div className="text-center">
                <div className="text-2xl font-bold text-purple-600">{mockAnalytics.avgResponseTime}</div>
                <div className="text-sm text-gray-600">Avg Response Time</div>
              </div>
              
              <div className="text-center">
                <div className="text-2xl font-bold text-orange-600">
                  {stats ? Math.round(stats.botRequestCount / 7).toLocaleString() : '---'}
                </div>
                <div className="text-sm text-gray-600">Daily Average</div>
              </div>
            </div>
          </div>
        </div>

        {/* Hourly Activity Chart */}
        <div className="mt-8 backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-bold text-gray-900">Hourly Activity</h2>
            <div className="flex items-center space-x-2">
              <Calendar className="w-4 h-4 text-gray-600" />
              <span className="text-sm text-gray-600">Today</span>
            </div>
          </div>
          
          <div className="grid grid-cols-6 md:grid-cols-9 lg:grid-cols-18 gap-2">
            {mockAnalytics.hourlyActivity.map((activity, index) => (
              <div key={index} className="text-center">
                <div className="flex flex-col items-center justify-end h-32 mb-2">
                  <div 
                    className="w-8 bg-gradient-to-t from-blue-500 to-blue-400 rounded-t-lg transition-all hover:from-blue-600 hover:to-blue-500"
                    style={{ 
                      height: `${(activity.requests / maxActivity) * 100}%`,
                      minHeight: '8px'
                    }}
                    title={`${activity.hour}: ${activity.requests} requests`}
                  />
                </div>
                <div className="text-xs text-gray-600 font-medium">{activity.hour}</div>
                <div className="text-xs text-gray-500">{activity.requests}</div>
              </div>
            ))}
          </div>
        </div>
      </DashboardLayout>
    </RouteGuard>
  );
};

export default ChatbotAnalyticsPage;