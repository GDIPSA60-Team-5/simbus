"use client";

import React, { useState, useEffect } from 'react';
import {
  Users,
  MessageSquare,
  Star,
  Activity,
  Bot,
  BarChart3,
  Bell,
  X,
  Send,
  MoreVertical,
  CheckCircle
} from 'lucide-react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { StatCard } from '@/components/dashboard/StatCard';
import { RouteGuard } from '@/components/auth/RouteGuard';
import { useStats } from '@/hooks/useStats';
import { useFeedbacks } from '@/hooks/useFeedbacks';
import { useNavigation } from '@/hooks/useNavigation';

type TabType = 'overview' | 'users' | 'chatbot' | 'feedback' | 'analytics';
type FeedbackFilterType = 'all' | 'chatbot' | 'directions' | 'scheduling' | 'performance';

interface Notification {
  id: number;
  title: string;
  message: string;
  type: 'info' | 'warning' | 'success' | 'error';
  time: string;
  read: boolean;
}

const NimbusAdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [feedbackFilter, setFeedbackFilter] = useState<FeedbackFilterType>('all');
  const [showSendNotification, setShowSendNotification] = useState(false);
  const [newNotificationTitle, setNewNotificationTitle] = useState('');
  const [newNotificationMessage, setNewNotificationMessage] = useState('');
  const [notifications, setNotifications] = useState<Notification[]>([]);

  // Use our custom hooks
  const { stats, loading: statsLoading, error: statsError, refresh: refreshStats } = useStats();
  const { feedbacks, loading: feedbacksLoading, error: feedbacksError, refresh: refreshFeedbacks } = useFeedbacks();
  const { items: sidebarItems, activeItem } = useNavigation();


  // Initialize notifications
  useEffect(() => {
    setNotifications([
      {
        id: 1,
        title: 'High Traffic Alert',
        message: 'Increased user activity detected during rush hour',
        type: 'info',
        time: '10 minutes ago',
        read: false
      },
      {
        id: 2,
        title: 'Chatbot Update',
        message: 'New AI model deployed successfully',
        type: 'success',
        time: '2 hours ago',
        read: false
      },
      {
        id: 3,
        title: 'Server Performance',
        message: 'Response time slightly elevated',
        type: 'warning',
        time: '4 hours ago',
        read: true
      }
    ]);
  }, []);


  const statsCards = stats ? [
    {
      title: 'Total Users',
      value: stats.userCount.toLocaleString(),
      change: stats.userCountRecently > 0 ? `+${((stats.userCountRecently / stats.userCount) * 100).toFixed(1)}%` : '0%',
      icon: Users,
      iconBgColor: 'from-blue-500 to-blue-600',
      color: 'text-blue-600',
      trend: 'up' as const,
    },
    {
      title: 'System CPU Usage',
      value: stats.systemCpuUsage ? `${(stats.systemCpuUsage * 100).toFixed(1)}%` : 'N/A',
      change: 'Real-time',
      icon: Activity,
      iconBgColor: 'from-green-500 to-green-600',
      color: 'text-green-600',
      trend: 'neutral' as const,
    },
    {
      title: 'Chatbot Requests',
      value: stats.botRequestCount.toLocaleString(),
      change: `${stats.botSuccessRate.toFixed(1)}% success`,
      icon: MessageSquare,
      iconBgColor: 'from-purple-500 to-purple-600',
      color: 'text-purple-600',
      trend: 'neutral' as const,
    },
    {
      title: 'Active HTTP Requests',
      value: stats.activeHttpRequests?.toLocaleString() || '0',
      change: 'Live',
      icon: Activity,
      iconBgColor: 'from-yellow-500 to-yellow-600',
      color: 'text-yellow-600',
      trend: 'neutral' as const,
    }
  ] : [];

  const getFilteredFeedbacks = () => {
    let filtered = feedbacks;

    if (feedbackFilter !== 'all') {
      filtered = feedbacks.filter(feedback =>
        feedback.feature === feedbackFilter
      );
    }

    if (searchQuery) {
      filtered = filtered.filter(feedback =>
        feedback.user.toLowerCase().includes(searchQuery.toLowerCase()) ||
        feedback.comment.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    return filtered;
  };

  const handleRefresh = async () => {
    await Promise.all([refreshStats(), refreshFeedbacks()]);
  };

  const handleSendNotification = () => {
    if (newNotificationTitle && newNotificationMessage) {
      const newNotification: Notification = {
        id: notifications.length + 1,
        title: newNotificationTitle,
        message: newNotificationMessage,
        type: 'success',
        time: 'Just now',
        read: false
      };
      setNotifications([newNotification, ...notifications]);
      setNewNotificationTitle('');
      setNewNotificationMessage('');
      setShowSendNotification(false);
      alert('Notification sent successfully!');
    }
  };

  const markNotificationAsRead = (id: number) => {
    setNotifications(notifications.map(notif =>
      notif.id === id ? { ...notif, read: true } : notif
    ));
  };

  const getRatingColor = (rating: number): string => {
    if (rating >= 4) return 'text-green-600';
    if (rating >= 3) return 'text-orange-600';
    return 'text-red-600';
  };

  const renderStars = (rating: number) => {
    return Array.from({ length: 5 }, (_, i) => (
      <Star
        key={i}
        className={`w-4 h-4 ${i < rating ? 'text-yellow-400 fill-current' : 'text-gray-300'}`}
      />
    ));
  };

  return (
    <RouteGuard requireAuth={true}>
      <DashboardLayout
        title="Nimbus"
        subtitle="Product Dashboard"
        searchPlaceholder="Search users, feedback..."
        onSearch={setSearchQuery}
        onRefresh={handleRefresh}
        isRefreshing={statsLoading || feedbacksLoading}
        notifications={notifications}
        onNotificationClick={markNotificationAsRead}
        sidebarItems={sidebarItems}
        activeSidebarItem={activeItem}
        onSidebarItemClick={() => { }}
        variant="dashboard"
      >

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {statsError && (
            <div className="col-span-full bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              <strong className="font-bold">Stats Error: </strong>
              <span className="block sm:inline">{statsError}</span>
              <button
                onClick={refreshStats}
                className="ml-4 bg-red-500 text-white px-2 py-1 rounded text-sm hover:bg-red-600"
              >
                Retry
              </button>
            </div>
          )}

          {statsLoading ? (
            Array.from({ length: 4 }, (_, i) => (
              <StatCard
                key={i}
                title=""
                value=""
                icon={Users}
                loading={true}
              />
            ))
          ) : statsCards.length > 0 ? (
            statsCards.map((stat, index) => (
              <StatCard
                key={index}
                title={stat.title}
                value={stat.value}
                change={stat.change}
                icon={stat.icon}
                iconBgColor={stat.iconBgColor}
                color={stat.color}
                trend={stat.trend}
                loading={false}
              />
            ))
          ) : (
            <div className="col-span-full text-center text-gray-500 py-8">
              No stats data available
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* User Feedback */}
          <div className="lg:col-span-2 backdrop-blur-xl bg-white/40 border border-orange-200/30 rounded-2xl p-6 shadow-lg shadow-orange-100/20">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-amber-900">Recent User Feedback</h2>
              <div className="flex items-center space-x-2">
                <div className="relative">
                  <select
                    value={feedbackFilter}
                    onChange={(e) => setFeedbackFilter(e.target.value as FeedbackFilterType)}
                    className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 text-black"
                  >
                    <option value="" disabled hidden className="text-black">
                      Select a Feature
                    </option>
                    <option value="all">All Features</option>
                    <option value="chatbot">Chatbot</option>
                    <option value="directions">Directions</option>
                    <option value="scheduling">Scheduling</option>
                    <option value="performance">Performance</option>
                  </select>

                </div>
              </div>
            </div>

            <div className="space-y-4 max-h-96 overflow-y-auto">
              {feedbacksLoading ? (
                <div className="text-center py-8">Loading feedbacks...</div>
              ) : feedbacksError ? (
                <div className="text-center py-8 text-red-600">Error loading feedbacks: {feedbacksError}</div>
              ) : getFilteredFeedbacks().length === 0 ? (
                <div className="text-center py-8 text-gray-500">No feedbacks found</div>
              ) : (
                getFilteredFeedbacks().map((feedback, index) => (
                  <div key={index} className="p-4 rounded-xl bg-white/40 border border-white/30 hover:bg-white/60 transition-all">
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-orange-500 to-amber-600 flex items-center justify-center">
                          <span className="text-white text-sm font-bold">{feedback.user.charAt(0)}</span>
                        </div>
                        <div>
                          <h4 className="font-semibold text-gray-900">{feedback.user}</h4>
                          <p className="text-xs text-gray-500">{feedback.time}</p>
                        </div>
                      </div>
                      <div className="flex items-center space-x-2">
                        <div className="flex">{renderStars(feedback.rating)}</div>
                        <span className={`text-sm font-medium px-2 py-1 rounded-full bg-white/60 capitalize ${getRatingColor(feedback.rating)}`}>
                          {feedback.feature}
                        </span>
                      </div>
                    </div>
                    <p className="text-sm text-gray-700">{feedback.comment}</p>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* System Metrics from Spring Actuator */}
          <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-6">System Metrics</h2>
            <div className="space-y-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-blue-600">
                  {stats?.processCpuUsage ? `${(stats.processCpuUsage * 100).toFixed(1)}%` : '---'}
                </div>
                <div className="text-sm text-gray-600">Process CPU Usage</div>
              </div>

              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">
                  {stats?.jvmMemoryUsed ? `${(stats.jvmMemoryUsed / (1024 * 1024)).toFixed(0)} MB` : '---'}
                </div>
                <div className="text-sm text-gray-600">JVM Heap Memory</div>
              </div>

              <div className="text-center">
                <div className="text-2xl font-bold text-purple-600">
                  {stats?.httpRequestCount?.toLocaleString() || '---'}
                </div>
                <div className="text-sm text-gray-600">Total HTTP Requests</div>
              </div>
            </div>
          </div>
        </div>

        {/* Chatbot Analytics */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6 hover:bg-white/80 transition-all cursor-pointer">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-3 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600">
                <Bot className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="font-bold text-gray-900">Chatbot Success Rate</h3>
                <p className="text-2xl font-bold text-blue-600">
                  {stats ? `${stats.botSuccessRate.toFixed(1)}%` : '---'}
                </p>
              </div>
            </div>
            <p className="text-sm text-gray-600">Successful query resolutions</p>
          </div>

          <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6 hover:bg-white/80 transition-all cursor-pointer">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-3 rounded-xl bg-gradient-to-br from-green-500 to-green-600">
                <MessageSquare className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="font-bold text-gray-900">Bot Requests</h3>
                <p className="text-2xl font-bold text-green-600">
                  {stats ? stats.botRequestCount.toLocaleString() : '---'}
                </p>
              </div>
            </div>
            <p className="text-sm text-gray-600">Total chatbot interactions</p>
          </div>

          <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6 hover:bg-white/80 transition-all cursor-pointer">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-3 rounded-xl bg-gradient-to-br from-purple-500 to-purple-600">
                <CheckCircle className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="font-bold text-gray-900">Successful Requests</h3>
                <p className="text-2xl font-bold text-purple-600">
                  {stats ? stats.botSuccessCount.toLocaleString() : '---'}
                </p>
              </div>
            </div>
            <p className="text-sm text-gray-600">Successfully handled queries</p>
          </div>
        </div>
      </DashboardLayout>
    </RouteGuard>
  );
};

export default NimbusAdminDashboard;