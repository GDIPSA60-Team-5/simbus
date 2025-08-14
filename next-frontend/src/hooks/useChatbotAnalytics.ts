"use client";

import { useState, useEffect } from 'react';
import { apiGet } from '@/lib/apiClient';

export interface ResponseTypeCount {
  id: string;
  count: number;
  responseType: string;
}

export const useChatbotAnalytics = () => {
  const [responseTypes, setResponseTypes] = useState<ResponseTypeCount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchResponseTypes = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';
      const data = await apiGet<ResponseTypeCount[]>(`${backendUrl}/api/chatbot-analytics/response-types`);
      
      setResponseTypes(data);
    } catch (err) {
      console.error('Error fetching chatbot analytics:', err);
      setError(err instanceof Error ? err.message : 'An error occurred');
      
      // Fallback to mock data for demonstration
      setResponseTypes([
        { id: 'directions', count: 45, responseType: 'directions' },
        { id: 'next-bus', count: 32, responseType: 'next-bus' },
        { id: 'message', count: 28, responseType: 'message' },
        { id: 'commute-plan', count: 15, responseType: 'commute-plan' },
        { id: 'error', count: 8, responseType: 'error' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResponseTypes();
  }, []);

  return {
    responseTypes,
    loading,
    error,
    refresh: fetchResponseTypes,
  };
};