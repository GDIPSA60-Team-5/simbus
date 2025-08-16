"use client";

import { useEffect, useCallback, useState } from 'react';
import { actuatorClient } from '@/lib/actuatorClient';
import { apiGet } from '@/lib/apiClient';

export interface StatsResponse {
  userCount: number;
  userCountRecently: number;
  feedbackCount: number;
  feedbackCountRecently: number;
  botRequestCount: number;
  botSuccessCount: number;
  botSuccessRate: number;
  avgResponseTimeMs: number;
  minResponseTimeMs: number;
  maxResponseTimeMs: number;
  // Add system metrics from Actuator
  systemCpuUsage?: number;
  processCpuUsage?: number;
  jvmMemoryUsed?: number;
  activeHttpRequests?: number;
  httpRequestCount?: number;
}

export const useStats = () => {
  const [data, setData] = useState<StatsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = useCallback(async (): Promise<StatsResponse> => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '/';

    try {
      // Fetch business logic stats from your existing API
      const businessStats = await apiGet<StatsResponse>(`${backendUrl}api/stats`);
      console.log('Business stats:', businessStats);

      // Fetch system metrics from Spring Actuator
      console.log('Fetching actuator metrics...');
      const [
        systemCpuUsage,
        processCpuUsage,
        jvmMemoryUsed,
        activeHttpRequests,
        httpRequestCount
      ] = await Promise.all([
        actuatorClient.getSystemCpuUsage(),
        actuatorClient.getProcessCpuUsage(),
        actuatorClient.getJvmMemoryUsed('heap'),
        actuatorClient.getHttpRequestsActive(),
        actuatorClient.getHttpRequestCount()
      ]);

      console.log('Actuator metrics results:', {
        systemCpuUsage,
        processCpuUsage,
        jvmMemoryUsed,
        activeHttpRequests,
        httpRequestCount
      });

      return {
        ...businessStats,
        systemCpuUsage: systemCpuUsage || undefined,
        processCpuUsage: processCpuUsage || undefined,
        jvmMemoryUsed: jvmMemoryUsed || undefined,
        activeHttpRequests: activeHttpRequests || undefined,
        httpRequestCount: httpRequestCount || undefined,
      };
    } catch (err) {
      // If Actuator metrics fail, fallback to just business stats
      console.warn('Actuator metrics unavailable, using business stats only:', err);
      return await apiGet<StatsResponse>(`${backendUrl}api/stats`);
    }
  }, []);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const stats = await fetchStats();
      setData(stats);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch stats');
    } finally {
      setLoading(false);
    }
  }, [fetchStats]);

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