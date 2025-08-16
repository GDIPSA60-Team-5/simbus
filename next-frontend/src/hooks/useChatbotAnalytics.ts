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

      const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
      const data = await apiGet<ResponseTypeCount[]>(`${backendUrl}/api/chatbot-analytics/response-types`);

      setResponseTypes(data);
    } catch (err) {
      console.error('Error fetching chatbot analytics:', err);
      setError(err instanceof Error ? err.message : 'An error occurred');
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