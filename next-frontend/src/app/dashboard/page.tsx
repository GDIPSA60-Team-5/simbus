"use client";
import React, { useState, useEffect } from 'react';
import {
  Smartphone,
  Users,
  MessageSquare,
  TrendingUp,
  Calendar,
  Settings,
  Bell,
  Search,
  Filter,
  MoreVertical,
  Activity,
  Star,
  AlertTriangle,
  CheckCircle,
  BarChart3,
  MapPin,
  Clock,
  Bot,
  X,
  Send,
  Plus,
  RefreshCw
} from 'lucide-react';

interface Stat {
  title: string;
  value: string;
  change: string;
  icon: React.ComponentType<any>;
  color: string;
}

interface ActivityItem {
  id: number;
  action: string;
  time: string;
  status: 'success' | 'warning' | 'info' | 'error';
  icon: React.ComponentType<any>;
}

interface Feedback {
  id: string;
  user: string;
  rating: number;
  comment: string;
  feature: string;
  time: string;
}

interface Notification {
  id: number;
  title: string;
  message: string;
  type: 'info' | 'warning' | 'success' | 'error';
  time: string;
  read: boolean;
}

type TabType = 'overview' | 'users' | 'chatbot' | 'feedback' | 'analytics';
type FilterType = 'all' | 'today' | 'week' | 'month';
type FeedbackFilterType = 'all' | 'chatbot' | 'directions' | 'scheduling' | 'performance';

const NimbusAdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('overview');
  const [searchQuery, setSearchQuery] = useState('');
  const [showNotifications, setShowNotifications] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [showSendNotification, setShowSendNotification] = useState(false);
  const [activityFilter, setActivityFilter] = useState<FilterType>('all');
  const [feedbackFilter, setFeedbackFilter] = useState<FeedbackFilterType>('all');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [newNotificationTitle, setNewNotificationTitle] = useState('');
  const [newNotificationMessage, setNewNotificationMessage] = useState('');
  const [notifications, setNotifications] = useState<Notification[]>([]);

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

  const stats: Stat[] = [
    {
      title: 'Total Users',
      value: '12,847',
      change: '+18.2%',
      icon: Users,
      color: 'text-orange-600'
    },
    {
      title: 'Daily Active Users',
      value: '3,421',
      change: '+12.8%',
      icon: Smartphone,
      color: 'text-orange-600'
    },
    {
      title: 'Chatbot Interactions',
      value: '8,936',
      change: '+25.4%',
      icon: MessageSquare,
      color: 'text-blue-600'
    },
    {
      title: 'Route Requests',
      value: '5,672',
      change: '+15.7%',
      icon: MapPin,
      color: 'text-green-600'
    }
  ];

  const allActivity: ActivityItem[] = [
    {
      id: 1,
      action: 'New user registered from Singapore',
      time: '2 minutes ago',
      status: 'success',
      icon: Users
    },
    {
      id: 2,
      action: 'Chatbot helped schedule daily commute',
      time: '5 minutes ago',
      status: 'info',
      icon: Bot
    },
    {
      id: 3,
      action: 'User reported route issue',
      time: '12 minutes ago',
      status: 'warning',
      icon: AlertTriangle
    },
    {
      id: 4,
      action: 'High app usage during rush hour',
      time: '18 minutes ago',
      status: 'success',
      icon: TrendingUp
    },
    {
      id: 5,
      action: 'Feedback received: 5-star rating',
      time: '25 minutes ago',
      status: 'success',
      icon: Star
    },
    {
      id: 6,
      action: 'System maintenance completed',
      time: '1 hour ago',
      status: 'success',
      icon: CheckCircle
    },
    {
      id: 7,
      action: 'New route added to database',
      time: '2 hours ago',
      status: 'info',
      icon: MapPin
    }
  ];

  const allFeedbacks: Feedback[] = [
    { id: 'FB-001', user: 'Alex Chen', rating: 5, comment: 'The chatbot is amazing! Helped me plan my daily route perfectly.', feature: 'chatbot', time: '2 hours ago' },
    { id: 'FB-002', user: 'Sarah Lim', rating: 4, comment: 'Love the real-time directions, but would like more bus stop info.', feature: 'directions', time: '4 hours ago' },
    { id: 'FB-003', user: 'David Wong', rating: 5, comment: 'Scheduling my commute has never been easier. Great app!', feature: 'scheduling', time: '6 hours ago' },
    { id: 'FB-004', user: 'Maria Santos', rating: 3, comment: 'Good app but sometimes slow during peak hours.', feature: 'performance', time: '8 hours ago' },
    { id: 'FB-005', user: 'John Tan', rating: 5, comment: 'The chatbot understands my needs perfectly. Highly recommended!', feature: 'chatbot', time: '1 day ago' },
    { id: 'FB-006', user: 'Lisa Park', rating: 4, comment: 'Great directions feature, very accurate!', feature: 'directions', time: '1 day ago' },
    { id: 'FB-007', user: 'Mike Johnson', rating: 2, comment: 'App crashes occasionally during scheduling.', feature: 'scheduling', time: '2 days ago' }
  ];

  const navItems: Array<{ id: TabType; label: string; icon: React.ComponentType<any> }> = [
    { id: 'overview', label: 'Overview', icon: Activity },
    { id: 'users', label: 'Users', icon: Users },
    { id: 'chatbot', label: 'Chatbot Analytics', icon: Bot },
    { id: 'feedback', label: 'User Feedback', icon: Star },
    { id: 'analytics', label: 'App Analytics', icon: BarChart3 },
  ];

  // Filter functions
  const getFilteredActivity = () => {
    let filtered = allActivity;

    if (activityFilter !== 'all') {
      const now = new Date();
      filtered = allActivity.filter(item => {
        const itemTime = new Date();
        if (activityFilter === 'today') {
          return itemTime.toDateString() === now.toDateString();
        } else if (activityFilter === 'week') {
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
          return itemTime >= weekAgo;
        } else if (activityFilter === 'month') {
          const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
          return itemTime >= monthAgo;
        }
        return true;
      });
    }

    if (searchQuery) {
      filtered = filtered.filter(item =>
        item.action.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    return filtered;
  };

  const getFilteredFeedbacks = () => {
    let filtered = allFeedbacks;

    if (feedbackFilter !== 'all') {
      filtered = allFeedbacks.filter(feedback =>
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

  // Event handlers
  const handleRefresh = async () => {
    setIsRefreshing(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    setIsRefreshing(false);
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

  const unreadCount = notifications.filter(n => !n.read).length;

  const getRatingColor = (rating: number): string => {
    if (rating >= 4) return 'text-green-600';
    if (rating >= 3) return 'text-orange-600';
    return 'text-red-600';
  };

  const getActivityStatusColor = (status: ActivityItem['status']): string => {
    switch (status) {
      case 'success': return 'text-green-600';
      case 'warning': return 'text-orange-600';
      case 'error': return 'text-red-600';
      default: return 'text-blue-600';
    }
  };

  const getNotificationTypeColor = (type: Notification['type']): string => {
    switch (type) {
      case 'success': return 'text-green-600 bg-green-50';
      case 'warning': return 'text-orange-600 bg-orange-50';
      case 'error': return 'text-red-600 bg-red-50';
      default: return 'text-blue-600 bg-blue-50';
    }
  };

  const handleTabChange = (tabId: TabType): void => {
    setActiveTab(tabId);
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
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-gray-100">
      {/* Header */}
      <header className="backdrop-blur-md bg-white/80 border-b border-white/20 sticky top-0 z-50">
        <div className="flex items-center justify-between px-6 py-4">
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-orange-500 to-amber-600 flex items-center justify-center">
                <Smartphone className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Nimbus</h1>
                <p className="text-sm text-gray-500">Product Dashboard</p>
              </div>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            {/* Search */}
            <div className="relative">
              <Search className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Search users, feedback..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10 pr-4 py-2 bg-white/60 backdrop-blur-sm border border-white/30 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/50 focus:border-orange-500/50"
              />
            </div>

            {/* Refresh Button */}
            <button
              onClick={handleRefresh}
              className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all"
            >
              <RefreshCw className={`w-5 h-5 text-gray-600 ${isRefreshing ? 'animate-spin' : ''}`} />
            </button>

            {/* Notifications */}
            <div className="relative">
              <button
                onClick={() => setShowNotifications(!showNotifications)}
                className="relative p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all"
              >
                <Bell className="w-5 h-5 text-gray-600" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 w-5 h-5 bg-orange-500 text-white text-xs rounded-full flex items-center justify-center">
                    {unreadCount}
                  </span>
                )}
              </button>

              {/* Notifications Dropdown */}
              {showNotifications && (
                <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-xl border border-gray-200 z-50">
                  <div className="p-4 border-b border-gray-200">
                    <h3 className="text-lg font-semibold">Notifications</h3>
                  </div>
                  <div className="max-h-96 overflow-y-auto">
                    {notifications.map((notification) => (
                      <div
                        key={notification.id}
                        onClick={() => markNotificationAsRead(notification.id)}
                        className={`p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50 ${!notification.read ? 'bg-blue-50' : ''}`}
                      >
                        <div className={`inline-block px-2 py-1 rounded-full text-xs mb-2 ${getNotificationTypeColor(notification.type)}`}>
                          {notification.type}
                        </div>
                        <h4 className="font-semibold text-gray-900">{notification.title}</h4>
                        <p className="text-sm text-gray-600">{notification.message}</p>
                        <p className="text-xs text-gray-400 mt-1">{notification.time}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Settings */}
            <button
              onClick={() => setShowSettings(!showSettings)}
              className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all"
            >
              <Settings className="w-5 h-5 text-gray-600" />
            </button>
          </div>
        </div>
      </header>

      <div className="flex">
        {/* Sidebar */}
        <aside className="w-64 min-h-screen backdrop-blur-md bg-white/40 border-r border-white/20">
          <nav className="p-6">
            <ul className="space-y-2">
              {navItems.map((item) => (
                <li key={item.id}>
                  <button
                    onClick={() => handleTabChange(item.id)}
                    className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl text-left transition-all ${activeTab === item.id
                      ? 'bg-orange-500 text-white shadow-lg'
                      : 'text-gray-700 hover:bg-white/60 backdrop-blur-sm'
                      }`}
                  >
                    <item.icon className="w-5 h-5" />
                    <span className="font-medium">{item.label}</span>
                  </button>
                </li>
              ))}
            </ul>
          </nav>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-6">
          {/* Stats Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {stats.map((stat: Stat, index: number) => (
              <div key={index} className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6 hover:bg-white/80 transition-all cursor-pointer">
                <div className="flex items-center justify-between mb-4">
                  <div className="p-3 rounded-xl bg-gradient-to-br from-orange-500 to-amber-600">
                    <stat.icon className="w-6 h-6 text-white" />
                  </div>
                  <TrendingUp className={`w-4 h-4 ${stat.color}`} />
                </div>
                <div>
                  <h3 className="text-2xl font-bold text-gray-900">{stat.value}</h3>
                  <p className="text-gray-600 text-sm mb-1">{stat.title}</p>
                  <span className={`text-sm font-medium ${stat.color}`}>{stat.change}</span>
                </div>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* User Feedback */}
            <div className="lg:col-span-2 backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-bold text-gray-900">Recent User Feedback</h2>
                <div className="flex items-center space-x-2">
                  {/* Feedback Filter */}
                  <div className="relative">
                    <select
                      value={feedbackFilter}
                      onChange={(e) => setFeedbackFilter(e.target.value as FeedbackFilterType)}
                      className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
                    >
                      <option value="all">All Features</option>
                      <option value="chatbot">Chatbot</option>
                      <option value="directions">Directions</option>
                      <option value="scheduling">Scheduling</option>
                      <option value="performance">Performance</option>
                    </select>
                  </div>
                  <button className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all">
                    <MoreVertical className="w-4 h-4 text-gray-600" />
                  </button>
                </div>
              </div>

              <div className="space-y-4 max-h-96 overflow-y-auto">
                {getFilteredFeedbacks().map((feedback: Feedback, index: number) => (
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
                ))}
              </div>
            </div>

            {/* Recent Activity */}
            <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-bold text-gray-900">Recent Activity</h2>
                <div className="relative">
                  <select
                    value={activityFilter}
                    onChange={(e) => setActivityFilter(e.target.value as FilterType)}
                    className="p-1 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 text-xs focus:outline-none"
                  >
                    <option value="all">All Time</option>
                    <option value="today">Today</option>
                    <option value="week">This Week</option>
                    <option value="month">This Month</option>
                  </select>
                </div>
              </div>

              <div className="space-y-4 max-h-80 overflow-y-auto">
                {getFilteredActivity().map((activity: ActivityItem) => (
                  <div key={activity.id} className="flex items-start space-x-3">
                    <div className={`p-2 rounded-lg bg-white/60 ${getActivityStatusColor(activity.status)}`}>
                      <activity.icon className="w-4 h-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm text-gray-900 font-medium">{activity.action}</p>
                      <p className="text-xs text-gray-500">{activity.time}</p>
                    </div>
                  </div>
                ))}
              </div>

              <button
                onClick={() => setActivityFilter('all')}
                className="w-full mt-6 py-3 bg-gradient-to-r from-orange-500 to-amber-600 text-white font-semibold rounded-xl hover:from-orange-600 hover:to-amber-700 transition-all shadow-lg"
              >
                View All Activity
              </button>
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
                  <p className="text-2xl font-bold text-blue-600">94.2%</p>
                </div>
              </div>
              <p className="text-sm text-gray-600">Successful query resolutions</p>
            </div>

            <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6 hover:bg-white/80 transition-all cursor-pointer">
              <div className="flex items-center space-x-3 mb-4">
                <div className="p-3 rounded-xl bg-gradient-to-br from-green-500 to-green-600">
                  <Calendar className="w-6 h-6 text-white" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-900">Daily Schedules</h3>
                  <p className="text-2xl font-bold text-green-600">2,847</p>
                </div>
              </div>
              <p className="text-sm text-gray-600">Commutes scheduled today</p>
            </div>

            <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6 hover:bg-white/80 transition-all cursor-pointer">
              <div className="flex items-center space-x-3 mb-4">
                <div className="p-3 rounded-xl bg-gradient-to-br from-purple-500 to-purple-600">
                  <Clock className="w-6 h-6 text-white" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-900">Avg Response Time</h3>
                  <p className="text-2xl font-bold text-purple-600">1.2s</p>
                </div>
              </div>
              <p className="text-sm text-gray-600">Chatbot response speed</p>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
            <button
              onClick={() => alert('Redirecting to Chatbot Configuration...')}
              className="p-6 backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl hover:bg-white/80 transition-all text-left"
            >
              <MessageSquare className="w-8 h-8 text-orange-500 mb-3" />
              <h3 className="font-semibold text-gray-900">Update Chatbot</h3>
              <p className="text-sm text-gray-600">Improve AI responses and features</p>
            </button>

            <button
              onClick={() => setShowSendNotification(true)}
              className="p-6 backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl hover:bg-white/80 transition-all text-left"
            >
              <Bell className="w-8 h-8 text-orange-500 mb-3" />
              <h3 className="font-semibold text-gray-900">Send Notification</h3>
              <p className="text-sm text-gray-600">Broadcast updates to users</p>
            </button>

            <button
              onClick={() => alert('Opening Detailed Analytics Report...')}
              className="p-6 backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl hover:bg-white/80 transition-all text-left"
            >
              <BarChart3 className="w-8 h-8 text-orange-500 mb-3" />
              <h3 className="font-semibold text-gray-900">View Reports</h3>
              <p className="text-sm text-gray-600">Detailed analytics and insights</p>
            </button>
          </div>
        </main>
      </div>

      {/* Send Notification Modal */}
      {showSendNotification && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-6 w-full max-w-md mx-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-xl font-bold text-gray-900">Send Notification</h3>
              <button
                onClick={() => setShowSendNotification(false)}
                className="p-2 hover:bg-gray-100 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Title</label>
                <input
                  type="text"
                  value={newNotificationTitle}
                  onChange={(e) => setNewNotificationTitle(e.target.value)}
                  placeholder="Enter notification title"
                  className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Message</label>
                <textarea
                  value={newNotificationMessage}
                  onChange={(e) => setNewNotificationMessage(e.target.value)}
                  placeholder="Enter notification message"
                  rows={4}
                  className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                />
              </div>

              <div className="flex space-x-3">
                <button
                  onClick={() => setShowSendNotification(false)}
                  className="flex-1 py-2 px-4 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-all"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSendNotification}
                  disabled={!newNotificationTitle || !newNotificationMessage}
                  className="flex-1 py-2 px-4 bg-gradient-to-r from-orange-500 to-amber-600 text-white rounded-lg hover:from-orange-600 hover:to-amber-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
                >
                  <Send className="w-4 h-4" />
                  <span>Send</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Settings Modal */}
      {showSettings && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-6 w-full max-w-md mx-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-xl font-bold text-gray-900">Settings</h3>
              <button
                onClick={() => setShowSettings(false)}
                className="p-2 hover:bg-gray-100 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-700">Email Notifications</span>
                <input type="checkbox" defaultChecked className="rounded" />
              </div>

              <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-700">Push Notifications</span>
                <input type="checkbox" defaultChecked className="rounded" />
              </div>

              <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-700">Auto Refresh</span>
                <input type="checkbox" defaultChecked className="rounded" />
              </div>

              <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-700">Dark Mode</span>
                <input type="checkbox" className="rounded" />
              </div>

              <button className="w-full py-3 bg-gradient-to-r from-orange-500 to-amber-600 text-white rounded-lg hover:from-orange-600 hover:to-amber-700 transition-all">
                Save Settings
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default NimbusAdminDashboard;