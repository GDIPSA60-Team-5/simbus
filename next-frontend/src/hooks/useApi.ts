"use client";

import { useState, useCallback } from 'react';

interface ApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

export const useApi = <T>() => {
  const [state, setState] = useState<ApiState<T>>({
    data: null,
    loading: false,
    error: null,
  });

  const execute = useCallback(async (apiCall: () => Promise<T>) => {
    console.log('🔄 useApi: Starting API call');
    setState(prev => ({ ...prev, loading: true, error: null }));

    try {
      const data = await apiCall();
      console.log('✅ useApi: API call successful', data);
      setState({ data, loading: false, error: null });
      return data;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'An error occurred';
      console.error('❌ useApi: API call failed', errorMessage);
      setState({ data: null, loading: false, error: errorMessage });
      throw error;
    }
  }, []);

  const reset = useCallback(() => {
    setState({ data: null, loading: false, error: null });
  }, []);

  return {
    ...state,
    execute,
    reset,
  };
};

export const useApiCall = <T>(apiCall: () => Promise<T>, deps: any[] = []) => {
  const { data, loading, error, execute } = useApi<T>();

  const refresh = useCallback(() => {
    execute(apiCall);
  }, [execute, apiCall]);

  return {
    data,
    loading,
    error,
    refresh,
  };
};