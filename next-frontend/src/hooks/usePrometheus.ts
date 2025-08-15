"use client";

import { useState, useEffect, useCallback } from 'react';
import { actuatorClient } from '@/lib/actuatorClient';

export interface AnalyticsMetrics {
  // Performance metrics
  cpuUsage: number | null;
  memoryUsage: number | null;
  responseTime: number | null;
  errorRate: number | null;
  
  // Traffic metrics
  currentUsers: number | null;
  requestsPerSecond: number | null;
  totalRequests: number | null;
  
  // Time series data for charts
  cpuTimeSeries: [number, number][];
  memoryTimeSeries: [number, number][];
  requestsTimeSeries: [number, number][];
  responseTimeTimeSeries: [number, number][];
  
  // Peak analysis
  peakHour: { hour: number; requests: number } | null;
  peakDay: { day: string; requests: number } | null;
}

export interface UsePrometheusResult {
  data: AnalyticsMetrics | null;
  loading: boolean;
  error: string | null;
  refresh: () => void;
}

export const usePrometheus = (): UsePrometheusResult => {
  const [data, setData] = useState<AnalyticsMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMetrics = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Fetch metrics from Spring Actuator
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

      // Convert JVM memory from bytes to MB
      const memoryUsageMB = jvmMemoryUsed ? jvmMemoryUsed / (1024 * 1024) : null;
      
      // Convert CPU usage to percentage
      const cpuValue = processCpuUsage || systemCpuUsage;
      const cpuUsagePercent = cpuValue ? cpuValue * 100 : null;

      // Generate mock time series data for demonstration
      // In a real application, you'd want to store historical data
      const now = Date.now();
      const generateTimeSeries = (value: number | null, variance: number = 0.2): [number, number][] => {
        if (value === null) return [];
        const points: [number, number][] = [];
        for (let i = 23; i >= 0; i--) {
          const timestamp = now - (i * 60 * 60 * 1000); // Hourly data for 24 hours
          const randomVariation = (Math.random() - 0.5) * variance * value;
          points.push([timestamp, Math.max(0, value + randomVariation)]);
        }
        return points;
      };

      const cpuTimeSeries = generateTimeSeries(cpuUsagePercent, 0.3);
      const memoryTimeSeries = generateTimeSeries(memoryUsageMB, 0.1);
      const requestsTimeSeries = generateTimeSeries(httpRequestCount || 100, 0.5);
      const responseTimeTimeSeries = generateTimeSeries(50, 0.8); // Mock response time

      // Calculate peak hour from time series data
      let peakHour: { hour: number; requests: number } | null = null;
      if (requestsTimeSeries.length > 0) {
        const hourlyData = new Map<number, number>();
        requestsTimeSeries.forEach(([timestamp, value]) => {
          const hour = new Date(timestamp).getHours();
          hourlyData.set(hour, (hourlyData.get(hour) || 0) + value);
        });
        
        let maxRequests = 0;
        let maxHour = 0;
        hourlyData.forEach((requests, hour) => {
          if (requests > maxRequests) {
            maxRequests = requests;
            maxHour = hour;
          }
        });
        
        if (maxRequests > 0) {
          peakHour = { hour: maxHour, requests: Math.round(maxRequests) };
        }
      }

      // Calculate peak day
      const peakDay: { day: string; requests: number } | null = {
        day: new Date().toLocaleDateString('en-US', { weekday: 'long' }),
        requests: Math.round((httpRequestCount || 100) * 0.8)
      };

      setData({
        cpuUsage: cpuUsagePercent,
        memoryUsage: memoryUsageMB,
        responseTime: 50, // Mock value - could be calculated from http.server.requests metrics
        errorRate: 0, // Mock value - could be calculated from http.server.requests with error status
        currentUsers: activeHttpRequests, // Active requests as a proxy for current users
        requestsPerSecond: httpRequestCount ? Math.round(httpRequestCount / (24 * 60 * 60)) : null, // Rough estimate
        totalRequests: httpRequestCount,
        cpuTimeSeries,
        memoryTimeSeries,
        requestsTimeSeries,
        responseTimeTimeSeries,
        peakHour,
        peakDay,
      });
      
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch metrics');
      console.error('Actuator fetch error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const refresh = useCallback(() => {
    fetchMetrics();
  }, [fetchMetrics]);

  useEffect(() => {
    fetchMetrics();
    
    // Refresh every 30 seconds
    const interval = setInterval(fetchMetrics, 30000);
    
    return () => clearInterval(interval);
  }, [fetchMetrics]);

  return {
    data,
    loading,
    error,
    refresh,
  };
};