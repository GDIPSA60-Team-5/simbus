"use client";

import { useEffect } from 'react';
import { useApi } from './useApi';
import { apiGet, apiDelete } from '@/lib/apiClient';

export interface User {
  id: string;
  userName: string;
  userType: string;
  createdAt: string;
}

export const useUsers = () => {
  const { data, loading, error, execute } = useApi<User[]>();

  const fetchUsers = async (): Promise<User[]> => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
    return await apiGet<User[]>(`${backendUrl}/api/admin/users`);
  };

  const refresh = () => execute(fetchUsers);

  const deleteUser = async (userId: string): Promise<void> => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
    await apiDelete(`${backendUrl}/api/admin/users/${userId}`);
    // Refresh the list after deletion
    refresh();
  };

  useEffect(() => {
    refresh();
  }, []);

  return {
    users: data || [],
    loading,
    error,
    refresh,
    deleteUser,
  };
};