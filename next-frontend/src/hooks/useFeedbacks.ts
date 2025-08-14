"use client";

import { useEffect } from 'react';
import { useApi } from './useApi';
import { useAuth } from './useAuth';
import { apiGet } from '@/lib/apiClient';

export interface Feedback {
  id: string;
  user: string;
  rating: number;
  comment: string;
  feature: string;
  time: string;
}

export const useFeedbacks = () => {
  const { data, loading, error, execute } = useApi<Feedback[]>();

  const determineFeatureFromTags = (tagList: string): string => {
    if (!tagList) return 'general';
    const tags = tagList.toLowerCase();
    if (tags.includes('chatbot') || tags.includes('ai')) return 'chatbot';
    if (tags.includes('direction') || tags.includes('navigation')) return 'directions';
    if (tags.includes('schedule') || tags.includes('timing')) return 'scheduling';
    if (tags.includes('performance') || tags.includes('speed')) return 'performance';
    return 'general';
  };

  const fetchFeedbacks = async (): Promise<Feedback[]> => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';
    const feedbacks = await apiGet<any[]>(`${backendUrl}/api/admin/feedbacks`);

    // Transform to match our Feedback interface
    return feedbacks.map((fb: any, index: number) => ({
      id: fb.id || `FB-${String(index + 1).padStart(3, '0')}`,
      user: fb.userName || 'Unknown User',
      rating: fb.rating || 5,
      comment: fb.feedbackText || '',
      feature: determineFeatureFromTags(fb.tagList) || 'general',
      time: fb.submittedAt ? new Date(fb.submittedAt).toLocaleString() : 'Unknown time'
    }));
  };

  const refresh = () => execute(fetchFeedbacks);

  useEffect(() => {
    refresh();
  }, []);

  return {
    feedbacks: data || [],
    loading,
    error,
    refresh,
  };
};