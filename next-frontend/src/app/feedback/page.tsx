"use client";

import React, { useState } from 'react';
import {
  Star,
  MessageSquare,
  User,
  Calendar,
  Filter,
  Search,
  TrendingUp,
  ThumbsUp,
  ThumbsDown,
  Eye,
  Reply,
  Flag,
  MoreVertical,
  Bot,
  Navigation,
  Clock,
  Zap,
  Activity,
  Users,
  BarChart3
} from 'lucide-react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { StatCard } from '@/components/dashboard/StatCard';
import { RouteGuard } from '@/components/auth/RouteGuard';
import { useFeedbacks, Feedback } from '@/hooks/useFeedbacks';
import { useStats } from '@/hooks/useStats';
import { useNavigation } from '@/hooks/useNavigation';

type FeedbackFilterType = 'all' | 'chatbot' | 'directions' | 'scheduling' | 'performance' | 'general';
type RatingFilterType = 'all' | '5' | '4' | '3' | '2' | '1';
type SortType = 'newest' | 'oldest' | 'rating_high' | 'rating_low';

const FeedbackPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [featureFilter, setFeatureFilter] = useState<FeedbackFilterType>('all');
  const [ratingFilter, setRatingFilter] = useState<RatingFilterType>('all');
  const [sortBy, setSortBy] = useState<SortType>('newest');
  const [selectedFeedback, setSelectedFeedback] = useState<Feedback | null>(null);
  const [showResponseModal, setShowResponseModal] = useState(false);
  const [responseText, setResponseText] = useState('');

  const { feedbacks, loading: feedbacksLoading, error: feedbacksError, refresh } = useFeedbacks();
  const { stats, loading: statsLoading } = useStats();
  const { items: sidebarItems, activeItem } = useNavigation();

  // Calculate feedback statistics
  const feedbackStats = React.useMemo(() => {
    if (!feedbacks.length) return null;

    const totalFeedbacks = feedbacks.length;
    const avgRating = feedbacks.reduce((sum, f) => sum + f.rating, 0) / totalFeedbacks;
    const ratingDistribution = [5, 4, 3, 2, 1].map(rating => ({
      rating,
      count: feedbacks.filter(f => f.rating === rating).length,
      percentage: (feedbacks.filter(f => f.rating === rating).length / totalFeedbacks) * 100
    }));

    const featureBreakdown = {
      chatbot: feedbacks.filter(f => f.feature === 'chatbot').length,
      directions: feedbacks.filter(f => f.feature === 'directions').length,
      scheduling: feedbacks.filter(f => f.feature === 'scheduling').length,
      performance: feedbacks.filter(f => f.feature === 'performance').length,
      general: feedbacks.filter(f => f.feature === 'general').length,
    };

    const recentFeedbacks = feedbacks.filter(f => {
      const feedbackDate = new Date(f.time);
      const weekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
      return feedbackDate > weekAgo;
    }).length;

    return {
      totalFeedbacks,
      avgRating,
      ratingDistribution,
      featureBreakdown,
      recentFeedbacks
    };
  }, [feedbacks]);

  // Filter and sort feedbacks
  const getFilteredAndSortedFeedbacks = () => {
    let filtered = feedbacks;

    // Filter by search query
    if (searchQuery) {
      filtered = filtered.filter(feedback =>
        feedback.user.toLowerCase().includes(searchQuery.toLowerCase()) ||
        feedback.comment.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Filter by feature
    if (featureFilter !== 'all') {
      filtered = filtered.filter(feedback => feedback.feature === featureFilter);
    }

    // Filter by rating
    if (ratingFilter !== 'all') {
      filtered = filtered.filter(feedback => feedback.rating === parseInt(ratingFilter));
    }

    // Sort
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'newest':
          return new Date(b.time).getTime() - new Date(a.time).getTime();
        case 'oldest':
          return new Date(a.time).getTime() - new Date(b.time).getTime();
        case 'rating_high':
          return b.rating - a.rating;
        case 'rating_low':
          return a.rating - b.rating;
        default:
          return 0;
      }
    });

    return filtered;
  };

  const renderStars = (rating: number, size: 'sm' | 'md' | 'lg' = 'md') => {
    const sizeClasses = {
      sm: 'w-3 h-3',
      md: 'w-4 h-4',
      lg: 'w-5 h-5'
    };

    return (
      <div className="flex">
        {Array.from({ length: 5 }, (_, i) => (
          <Star
            key={i}
            className={`${sizeClasses[size]} ${i < rating
              ? 'text-yellow-400 fill-current'
              : 'text-gray-300'
              }`}
          />
        ))}
      </div>
    );
  };

  const getRatingColor = (rating: number): string => {
    if (rating >= 4) return 'text-green-600 bg-green-100';
    if (rating >= 3) return 'text-yellow-600 bg-yellow-100';
    return 'text-red-600 bg-red-100';
  };

  const getFeatureIcon = (feature: string) => {
    switch (feature) {
      case 'chatbot': return Bot;
      case 'directions': return Navigation;
      case 'scheduling': return Clock;
      case 'performance': return Zap;
      default: return MessageSquare;
    }
  };

  const getFeatureColor = (feature: string): string => {
    switch (feature) {
      case 'chatbot': return 'bg-blue-100 text-blue-800';
      case 'directions': return 'bg-green-100 text-green-800';
      case 'scheduling': return 'bg-purple-100 text-purple-800';
      case 'performance': return 'bg-orange-100 text-orange-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const formatTimeAgo = (timeString: string): string => {
    const now = new Date();
    const feedbackTime = new Date(timeString);
    const diffInHours = Math.floor((now.getTime() - feedbackTime.getTime()) / (1000 * 60 * 60));

    if (diffInHours < 1) return 'Just now';
    if (diffInHours < 24) return `${diffInHours}h ago`;
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) return `${diffInDays}d ago`;
    const diffInWeeks = Math.floor(diffInDays / 7);
    return `${diffInWeeks}w ago`;
  };

  // Feedback overview stats
  const overviewStats = [
    {
      title: 'Total Feedback',
      value: feedbackStats ? feedbackStats.totalFeedbacks.toString() : (stats ? stats.feedbackCount.toString() : '---'),
      change: feedbackStats ? `${feedbackStats.recentFeedbacks} this week` : (stats && stats.feedbackCountRecently > 0 ? `+${stats.feedbackCountRecently} recent` : '0 recent'),
      icon: MessageSquare,
      iconBgColor: 'from-blue-500 to-blue-600',
      color: 'text-blue-600',
      trend: 'up' as const,
      description: 'All user feedback received'
    },
    {
      title: 'Average Rating',
      value: feedbackStats ? feedbackStats.avgRating.toFixed(1) : '---',
      change: feedbackStats && feedbackStats.avgRating >= 4 ? 'Excellent' : feedbackStats && feedbackStats.avgRating >= 3 ? 'Good' : 'Needs improvement',
      icon: Star,
      iconBgColor: 'from-yellow-500 to-yellow-600',
      color: feedbackStats && feedbackStats.avgRating >= 4 ? 'text-green-600' : feedbackStats && feedbackStats.avgRating >= 3 ? 'text-yellow-600' : 'text-red-600',
      trend: feedbackStats && feedbackStats.avgRating >= 4 ? 'up' as const : 'neutral' as const,
      description: 'Overall user satisfaction'
    },
    {
      title: 'Recent Feedback',
      value: feedbackStats ? feedbackStats.recentFeedbacks.toString() : '---',
      change: 'Last 7 days',
      icon: TrendingUp,
      iconBgColor: 'from-green-500 to-green-600',
      color: 'text-green-600',
      trend: 'up' as const,
      description: 'New feedback this week'
    }
  ];

  const handleResponseSubmit = () => {
    if (selectedFeedback && responseText.trim()) {
      // In a real app, this would send the response to the backend
      alert(`Response sent to ${selectedFeedback.user}: ${responseText}`);
      setShowResponseModal(false);
      setResponseText('');
      setSelectedFeedback(null);
    }
  };

  return (
    <RouteGuard requireAuth={true}>
      <DashboardLayout
        title="User Feedback"
        subtitle="Monitor and respond to user feedback"
        searchPlaceholder="Search feedback..."
        onSearch={setSearchQuery}
        onRefresh={refresh}
        isRefreshing={feedbacksLoading || statsLoading}
        notifications={[]}
        onNotificationClick={() => { }}
        sidebarItems={sidebarItems}
        activeSidebarItem={activeItem}
        onSidebarItemClick={() => { }}
        variant="feedback"
      >
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          {statsLoading ? (
            Array.from({ length: 3 }, (_, i) => (
              <StatCard
                key={i}
                title=""
                value=""
                icon={MessageSquare}
                loading={true}
              />
            ))
          ) : (
            overviewStats.map((stat, index) => (
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
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Main Feedback List */}
          <div className="lg:col-span-3">
            <div className="backdrop-blur-xl bg-white/40 border border-orange-200/30 rounded-2xl p-6 shadow-lg shadow-orange-100/20">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-bold text-amber-900">User Feedback</h2>
                <div className="flex items-center space-x-2">
                  <select
                    value={featureFilter}
                    onChange={(e) => setFeatureFilter(e.target.value as FeedbackFilterType)}
                    className="p-2 rounded-lg bg-white/60 placeholder:text-black text-black backdrop-blur-sm border border-white/30 hover:bg-white/80 transition-all text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="all">All Features</option>
                    <option value="chatbot">Chatbot</option>
                    <option value="directions">Directions</option>
                    <option value="scheduling">Scheduling</option>
                    <option value="performance">Performance</option>
                    <option value="general">General</option>
                  </select>

                  <select
                    value={ratingFilter}
                    onChange={(e) => setRatingFilter(e.target.value as RatingFilterType)}
                    className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border placeholder:text-black text-black border-white/30 hover:bg-white/80 transition-all text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="all">All Ratings</option>
                    <option value="5">5 Stars</option>
                    <option value="4">4 Stars</option>
                    <option value="3">3 Stars</option>
                    <option value="2">2 Stars</option>
                    <option value="1">1 Star</option>
                  </select>

                  <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value as SortType)}
                    className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border border-white/30 placeholder:text-black text-black hover:bg-white/80 transition-all text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="newest">Newest First</option>
                    <option value="oldest">Oldest First</option>
                    <option value="rating_high">Highest Rating</option>
                    <option value="rating_low">Lowest Rating</option>
                  </select>
                </div>
              </div>

              {feedbacksError && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                  <strong className="font-bold">Error: </strong>
                  <span className="block sm:inline">{feedbacksError}</span>
                </div>
              )}

              <div className="space-y-4 max-h-96 overflow-y-auto">
                {feedbacksLoading ? (
                  <div className="text-center py-12">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                    <p className="text-gray-600 mt-2">Loading feedback...</p>
                  </div>
                ) : getFilteredAndSortedFeedbacks().length === 0 ? (
                  <div className="text-center py-12">
                    <MessageSquare className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                    <p className="text-gray-600">No feedback found</p>
                    <p className="text-sm text-gray-500">Try adjusting your filters</p>
                  </div>
                ) : (
                  getFilteredAndSortedFeedbacks().map((feedback, index) => {
                    const FeatureIcon = getFeatureIcon(feedback.feature);

                    return (
                      <div
                        key={index}
                        className="p-4 rounded-xl bg-white/40 border border-white/30 hover:bg-white/60 transition-all cursor-pointer"
                        onClick={() => setSelectedFeedback(feedback)}
                      >
                        <div className="flex items-start justify-between mb-3">
                          <div className="flex items-center space-x-3">
                            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center">
                              <span className="text-white text-sm font-bold">
                                {feedback.user.charAt(0).toUpperCase()}
                              </span>
                            </div>
                            <div>
                              <h4 className="font-semibold text-gray-900">{feedback.user}</h4>
                              <p className="text-xs text-gray-500 flex items-center space-x-2">
                                <Calendar className="w-3 h-3" />
                                <span>{formatTimeAgo(feedback.time)}</span>
                              </p>
                            </div>
                          </div>

                          <div className="flex items-center space-x-2">
                            <div className={`flex items-center space-x-1 px-2 py-1 rounded-full text-xs font-medium ${getFeatureColor(feedback.feature)}`}>
                              <FeatureIcon className="w-3 h-3" />
                              <span className="capitalize">{feedback.feature}</span>
                            </div>
                            <div className="flex items-center space-x-1">
                              {renderStars(feedback.rating, 'sm')}
                              <span className={`ml-1 px-2 py-1 rounded-full text-xs font-medium ${getRatingColor(feedback.rating)}`}>
                                {feedback.rating}/5
                              </span>
                            </div>
                          </div>
                        </div>

                        <p className="text-sm text-gray-700 mb-3 line-clamp-2">
                          {feedback.comment}
                        </p>

                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>

          {/* Sidebar Stats */}
          <div className="space-y-6">
            {/* Rating Distribution */}
            {feedbackStats && (
              <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Rating Distribution</h3>
                <div className="space-y-3">
                  {feedbackStats.ratingDistribution.map(({ rating, count, percentage }) => (
                    <div key={rating} className="flex items-center space-x-3">
                      <div className="flex items-center space-x-1 w-16">
                        <span className="text-sm font-medium">{rating}</span>
                        <Star className="w-3 h-3 text-yellow-400 fill-current" />
                      </div>
                      <div className="flex-1">
                        <div className="bg-gray-200 rounded-full h-2">
                          <div
                            className="bg-gradient-to-r from-yellow-400 to-yellow-500 h-2 rounded-full transition-all"
                            style={{ width: `${percentage}%` }}
                          />
                        </div>
                      </div>
                      <div className="text-sm text-gray-600 w-12 text-right">
                        {count}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Feature Breakdown */}
            {feedbackStats && (
              <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Feature Breakdown</h3>
                <div className="space-y-3">
                  {Object.entries(feedbackStats.featureBreakdown).map(([feature, count]) => {
                    const FeatureIcon = getFeatureIcon(feature);
                    const percentage = (count / feedbackStats.totalFeedbacks) * 100;

                    return (
                      <div key={feature} className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          <FeatureIcon className="w-4 h-4 text-gray-600" />
                          <span className="text-sm capitalize">{feature}</span>
                        </div>
                        <div className="flex items-center space-x-2">
                          <div className="w-16 bg-gray-200 rounded-full h-1.5">
                            <div
                              className="bg-blue-500 h-1.5 rounded-full"
                              style={{ width: `${percentage}%` }}
                            />
                          </div>
                          <span className="text-sm font-medium text-gray-900 w-6">
                            {count}
                          </span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Response Modal */}
        {showResponseModal && selectedFeedback && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-2xl p-6 w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-xl font-bold text-gray-900">Respond to Feedback</h3>
                <button
                  onClick={() => setShowResponseModal(false)}
                  className="p-2 hover:bg-gray-100 rounded-lg"
                >
                  ×
                </button>
              </div>

              {/* Original Feedback */}
              <div className="bg-gray-50 rounded-xl p-4 mb-4">
                <div className="flex items-center space-x-3 mb-2">
                  <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center">
                    <span className="text-white text-sm font-bold">
                      {selectedFeedback.user.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">{selectedFeedback.user}</h4>
                    <div className="flex items-center space-x-2">
                      {renderStars(selectedFeedback.rating, 'sm')}
                      <span className="text-xs text-gray-500">{selectedFeedback.time}</span>
                    </div>
                  </div>
                </div>
                <p className="text-gray-700">{selectedFeedback.comment}</p>
              </div>

              {/* Response Form */}
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Your Response
                  </label>
                  <textarea
                    value={responseText}
                    onChange={(e) => setResponseText(e.target.value)}
                    placeholder="Write your response to this feedback..."
                    rows={6}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                  />
                </div>

                <div className="flex space-x-3">
                  <button
                    onClick={() => setShowResponseModal(false)}
                    className="flex-1 py-2 px-4 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-all"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleResponseSubmit}
                    disabled={!responseText.trim()}
                    className="flex-1 py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
                  >
                    <Reply className="w-4 h-4" />
                    <span>Send Response</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </DashboardLayout>
    </RouteGuard>
  );
};

export default FeedbackPage;