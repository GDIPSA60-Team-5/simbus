"use client";

import { useState, useEffect, useCallback } from 'react';
import { prometheusClient } from '@/lib/prometheusClient';

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
      // Common Prometheus queries for web applications
      const queries = {
        // Performance queries
        cpuUsage: 'rate(process_cpu_seconds_total[5m]) * 100',
        memoryUsage: 'process_resident_memory_bytes / 1024 / 1024', // MB
        responseTime: 'histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) * 1000', // ms
        errorRate: 'rate(http_requests_total{status=~"5.."}[5m]) / rate(http_requests_total[5m]) * 100',
        
        // Traffic queries
        currentUsers: 'sum(rate(http_requests_total[1m]))', // Approximation
        requestsPerSecond: 'rate(http_requests_total[1m])',
        totalRequests: 'increase(http_requests_total[24h])',
        
        // Alternative queries if the above don't exist
        altCpuUsage: '(1 - rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100',
        altMemoryUsage: '(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100',
        altRequestsPerSecond: 'rate(prometheus_http_requests_total[1m])',
        altTotalRequests: 'prometheus_http_requests_total',
      };

      // Fetch current values
      const [
        cpuUsage,
        memoryUsage, 
        responseTime,
        errorRate,
        currentUsers,
        requestsPerSecond,
        totalRequests,
        // Fallback queries
        altCpuUsage,
        altMemoryUsage,
        altRequestsPerSecond,
        altTotalRequests
      ] = await Promise.all([
        prometheusClient.getCurrentValue(queries.cpuUsage),
        prometheusClient.getCurrentValue(queries.memoryUsage),
        prometheusClient.getCurrentValue(queries.responseTime),
        prometheusClient.getCurrentValue(queries.errorRate),
        prometheusClient.getCurrentValue(queries.currentUsers),
        prometheusClient.getCurrentValue(queries.requestsPerSecond),
        prometheusClient.getCurrentValue(queries.totalRequests),
        // Fallbacks
        prometheusClient.getCurrentValue(queries.altCpuUsage),
        prometheusClient.getCurrentValue(queries.altMemoryUsage),
        prometheusClient.getCurrentValue(queries.altRequestsPerSecond),
        prometheusClient.getCurrentValue(queries.altTotalRequests),
      ]);

      // Fetch time series data (last 24 hours)
      const [
        cpuTimeSeries,
        memoryTimeSeries,
        requestsTimeSeries,
        responseTimeTimeSeries
      ] = await Promise.all([
        prometheusClient.getTimeSeriesData(cpuUsage !== null ? queries.cpuUsage : queries.altCpuUsage, 24),
        prometheusClient.getTimeSeriesData(memoryUsage !== null ? queries.memoryUsage : queries.altMemoryUsage, 24),
        prometheusClient.getTimeSeriesData(requestsPerSecond !== null ? queries.requestsPerSecond : queries.altRequestsPerSecond, 24),
        prometheusClient.getTimeSeriesData(queries.responseTime, 24),
      ]);

      // Calculate peak hour from requests time series
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

      // Calculate peak day (mock data for demonstration)
      const peakDay: { day: string; requests: number } | null = {
        day: new Date().toLocaleDateString('en-US', { weekday: 'long' }),
        requests: Math.round((totalRequests || altTotalRequests || 0) * 0.8)
      };

      setData({
        // Use fallback values if primary queries fail
        cpuUsage: cpuUsage || altCpuUsage,
        memoryUsage: memoryUsage || altMemoryUsage,
        responseTime,
        errorRate: errorRate || 0, // Default to 0 if not available
        currentUsers: currentUsers || Math.round((requestsPerSecond || altRequestsPerSecond || 0) * 60), // Estimate
        requestsPerSecond: requestsPerSecond || altRequestsPerSecond,
        totalRequests: totalRequests || altTotalRequests,
        cpuTimeSeries,
        memoryTimeSeries,
        requestsTimeSeries,
        responseTimeTimeSeries,
        peakHour,
        peakDay,
      });
      
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch metrics');
      console.error('Prometheus fetch error:', err);
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