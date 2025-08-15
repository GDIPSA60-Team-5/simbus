"use client";

import React, { useState } from 'react';
import {
  Bot,
  MessageSquare,
  CheckCircle,
  XCircle,
  Clock,
  Navigation,
  MessageCircle,
  AlertCircle,
  MapPin,
  Bus,
} from 'lucide-react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { StatCard } from '@/components/dashboard/StatCard';
import { RouteGuard } from '@/components/auth/RouteGuard';
import { useStats } from '@/hooks/useStats';
import { useNavigation } from '@/hooks/useNavigation';
import { useChatbotAnalytics } from '@/hooks/useChatbotAnalytics';

const ChatbotAnalyticsPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [timeFilter, setTimeFilter] = useState('7d');

  const { stats, loading: statsLoading, error: statsError, refresh } = useStats();
  const { items: sidebarItems, activeItem } = useNavigation();
  const { responseTypes, loading: analyticsLoading, error: analyticsError, refresh: refreshAnalytics } = useChatbotAnalytics();

  // Helper function to get icon and display name for response types
  const getResponseTypeInfo = (type: string) => {
    switch (type) {
      case 'directions':
        return { icon: Navigation, name: 'Directions', color: 'text-blue-600', bgColor: 'from-blue-50 to-blue-100' };
      case 'next-bus':
        return { icon: Bus, name: 'Bus Info', color: 'text-green-600', bgColor: 'from-green-50 to-green-100' };
      case 'commute-plan':
        return { icon: MapPin, name: 'Commute Planning', color: 'text-purple-600', bgColor: 'from-purple-50 to-purple-100' };
      case 'message':
        return { icon: MessageCircle, name: 'General Messages', color: 'text-orange-600', bgColor: 'from-orange-50 to-orange-100' };
      case 'error':
        return { icon: AlertCircle, name: 'Errors', color: 'text-red-600', bgColor: 'from-red-50 to-red-100' };
      default:
        return { icon: MessageSquare, name: type, color: 'text-gray-600', bgColor: 'from-gray-50 to-gray-100' };
    }
  };

  // Mock data for additional chatbot metrics (in a real app, this would come from API)
  const mockAnalytics = {
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
      value: `${(stats.avgResponseTimeMs / 1000).toFixed(2)}s`,
      change: `Min: ${(stats.minResponseTimeMs / 1000).toFixed(2)}s | Max: ${(stats.maxResponseTimeMs / 1000).toFixed(2)}s`,
      icon: Clock,
      iconBgColor: 'from-purple-500 to-purple-600',
      color: 'text-purple-600',
      trend: 'neutral' as const,
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
        onRefresh={() => {
          refresh();
          refreshAnalytics();
        }}
        isRefreshing={statsLoading}
        notifications={[]}
        onNotificationClick={() => { }}
        sidebarItems={sidebarItems}
        activeSidebarItem={activeItem}
        onSidebarItemClick={() => { }}
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

        {/* Response Time Analytics */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-6">Response Time Analytics</h2>
            <div className="space-y-4">
              <div className="flex justify-between items-center p-4 bg-gradient-to-r from-purple-50 to-purple-100 rounded-lg">
                <div>
                  <div className="text-sm font-medium text-gray-700">Average Response Time</div>
                  <div className="text-2xl font-bold text-purple-600">
                    {stats ? `${(stats.avgResponseTimeMs / 1000).toFixed(2)}s` : '---'}
                  </div>
                </div>
                <Clock className="w-8 h-8 text-purple-500" />
              </div>
              
              <div className="flex justify-between items-center p-4 bg-gradient-to-r from-green-50 to-green-100 rounded-lg">
                <div>
                  <div className="text-sm font-medium text-gray-700">Fastest Response</div>
                  <div className="text-2xl font-bold text-green-600">
                    {stats ? `${(stats.minResponseTimeMs / 1000).toFixed(2)}s` : '---'}
                  </div>
                </div>
                <CheckCircle className="w-8 h-8 text-green-500" />
              </div>
              
              <div className="flex justify-between items-center p-4 bg-gradient-to-r from-orange-50 to-orange-100 rounded-lg">
                <div>
                  <div className="text-sm font-medium text-gray-700">Slowest Response</div>
                  <div className="text-2xl font-bold text-orange-600">
                    {stats ? `${(stats.maxResponseTimeMs / 1000).toFixed(2)}s` : '---'}
                  </div>
                </div>
                <XCircle className="w-8 h-8 text-orange-500" />
              </div>
            </div>
          </div>
          
          {/* Quick Stats */}
          <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-6">Performance Overview</h2>
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
                <div className="text-2xl font-bold text-blue-600">
                  {stats ? stats.botRequestCount.toLocaleString() : '---'}
                </div>
                <div className="text-sm text-gray-600">Total Requests</div>
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

        {/* Response Type Distribution */}
        <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Query Type Distribution</h2>
          
          {analyticsError && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              <strong className="font-bold">Analytics Error: </strong>
              <span className="block sm:inline">{analyticsError}</span>
            </div>
          )}

          {analyticsLoading ? (
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p className="text-gray-600 mt-2">Loading analytics...</p>
            </div>
          ) : responseTypes.length === 0 ? (
            <div className="text-center py-12">
              <Bot className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-600">No query data available yet</p>
            </div>
          ) : (
            <div className="space-y-4">
              {responseTypes.map((responseType, index) => {
                const typeInfo = getResponseTypeInfo(responseType.id);
                const IconComponent = typeInfo.icon;
                const totalRequests = responseTypes.reduce((sum, rt) => sum + rt.count, 0);
                const percentage = totalRequests > 0 ? (responseType.count / totalRequests * 100) : 0;
                
                return (
                  <div key={responseType.id} className={`flex items-center justify-between p-4 bg-gradient-to-r ${typeInfo.bgColor} rounded-lg`}>
                    <div className="flex items-center space-x-3">
                      <IconComponent className={`w-6 h-6 ${typeInfo.color}`} />
                      <div>
                        <div className="font-medium text-gray-900">{typeInfo.name}</div>
                        <div className="text-sm text-gray-600">{responseType.count.toLocaleString()} queries</div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className={`text-xl font-bold ${typeInfo.color}`}>
                        {percentage.toFixed(1)}%
                      </div>
                      <div className="w-32 bg-white/50 rounded-full h-2 mt-1">
                        <div
                          className={`bg-gradient-to-r ${typeInfo.bgColor.replace('50', '500').replace('100', '600')} h-2 rounded-full transition-all duration-300`}
                          style={{ width: `${Math.min(percentage, 100)}%` }}
                        />
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </DashboardLayout>
    </RouteGuard >
  );
};

export default ChatbotAnalyticsPage;