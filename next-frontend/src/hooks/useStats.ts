"use client";

import { useEffect, useCallback } from 'react';
import { useApi } from './useApi';
import { apiGet } from '@/lib/apiClient';

export interface StatsResponse {
  userCount: number;
  userCountRecently: number;
  feedbackCount: number;
  feedbackCountRecently: number;
  botRequestCount: number;
  botSuccessCount: number;
  botSuccessRate: number;
}

export const useStats = () => {
  const { data, loading, error, execute } = useApi<StatsResponse>();

  const fetchStats = useCallback(async (): Promise<StatsResponse> => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080/api';
    const url = `${backendUrl}/stats`;
    
    return await apiGet<StatsResponse>(url);
  }, []);

  const refresh = useCallback(() => {
    execute(fetchStats);
  }, [execute, fetchStats]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return {
    stats: data,
    loading,
    error,
    refresh,
  };
};