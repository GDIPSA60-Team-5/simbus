"use client";

import React, { useState } from 'react';
import {
  Users,
  UserPlus,
  Search,
  Filter,
  MoreVertical,
  Trash2,
  Edit,
  Shield,
  Calendar,
  AlertTriangle,
  Activity,
  Bot,
  Star,
  BarChart3
} from 'lucide-react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { StatCard } from '@/components/dashboard/StatCard';
import { RouteGuard } from '@/components/auth/RouteGuard';
import { useUsers, User } from '@/hooks/useUsers';
import { useStats } from '@/hooks/useStats';
import { useNavigation } from '@/hooks/useNavigation';

type UserFilterType = 'all' | 'admin' | 'user';

const UsersPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [userFilter, setUserFilter] = useState<UserFilterType>('all');
  const [showDeleteModal, setShowDeleteModal] = useState<string | null>(null);
  const [showAddUserModal, setShowAddUserModal] = useState(false);
  const [newUser, setNewUser] = useState({ username: '', email: '', password: '', userType: 'user' });
  const [isCreating, setIsCreating] = useState(false);

  const { users, loading: usersLoading, error: usersError, refresh, deleteUser } = useUsers();
  const { stats, loading: statsLoading } = useStats();
  const { items: sidebarItems, activeItem } = useNavigation();


  // Filter users based on search and filter criteria
  const getFilteredUsers = () => {
    let filtered = users;

    if (userFilter !== 'all') {
      filtered = users.filter(user => user.userType === userFilter);
    }

    if (searchQuery) {
      filtered = filtered.filter(user =>
        (user.userName?.toLowerCase() ?? '').includes(searchQuery.toLowerCase()) ||
        (user.userType?.toLowerCase() ?? '').includes(searchQuery.toLowerCase())
      );
    }

    return filtered;
  };

  const handleDeleteUser = async (userId: string) => {
    try {
      await deleteUser(userId);
      setShowDeleteModal(null);
    } catch (error) {
      alert('Failed to delete user. Please try again.');
    }
  };

  const handleCreateUser = async () => {
    if (!newUser.username || !newUser.email || !newUser.password) {
      alert('Please fill in all fields');
      return;
    }

    setIsCreating(true);
    try {
      const response = await fetch('http://localhost:8080/api/admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(newUser)
      });

      if (response.ok) {
        setShowAddUserModal(false);
        setNewUser({ username: '', email: '', password: '', userType: 'user' });
        refresh();
      } else {
        alert('Failed to create user. Username might already exist.');
      }
    } catch (error) {
      alert('Failed to create user. Please try again.');
    } finally {
      setIsCreating(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getUserTypeColor = (userType?: string): string => {
    switch ((userType ?? '').toLowerCase()) {
      case 'admin':
        return 'bg-red-100 text-red-800';
      case 'user':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };


  const userStats = [
    {
      title: 'Total Users',
      value: stats ? stats.userCount.toLocaleString() : '---',
      change: stats && stats.userCountRecently > 0 ? `+${stats.userCountRecently} recent` : '0 recent',
      icon: Users,
      iconBgColor: 'from-blue-500 to-blue-600',
      color: 'text-blue-600',
      trend: 'up' as const,
    },
    {
      title: 'Active Users',
      value: users.length.toLocaleString(),
      change: 'Currently loaded',
      icon: Shield,
      iconBgColor: 'from-green-500 to-green-600',
      color: 'text-green-600',
      trend: 'neutral' as const,
    },
    {
      title: 'New Users',
      value: stats ? stats.userCountRecently.toLocaleString() : '---',
      change: 'Last 7 days',
      icon: UserPlus,
      iconBgColor: 'from-purple-500 to-purple-600',
      color: 'text-purple-600',
      trend: 'up' as const,
    },
  ];

  return (
    <RouteGuard requireAuth={true}>
      <DashboardLayout
        title="User Management"
        subtitle="Manage and monitor user accounts"
        searchPlaceholder="Search users..."
        onSearch={setSearchQuery}
        onRefresh={refresh}
        isRefreshing={usersLoading || statsLoading}
        notifications={[]}
        onNotificationClick={() => { }}
        sidebarItems={sidebarItems}
        activeSidebarItem={activeItem}
        onSidebarItemClick={() => { }}
        variant="users"
      >
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          {statsLoading ? (
            Array.from({ length: 3 }, (_, i) => (
              <StatCard
                key={i}
                title=""
                value=""
                change=""
                icon={Users}
                loading={true}
              />
            ))
          ) : (
            userStats.map((stat, index) => (
              <StatCard
                key={index}
                title={stat.title}
                value={stat.value}
                change={stat.change}
                icon={stat.icon}
                iconBgColor={stat.iconBgColor}
                color={stat.color}
                trend={stat.trend}
              />
            ))
          )}
        </div>

        {/* Users Table */}
        <div className="backdrop-blur-xl bg-white/40 border border-orange-200/30 rounded-2xl p-6 shadow-lg shadow-orange-100/20">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-bold text-amber-900">User Accounts</h2>
            <div className="flex items-center space-x-2">
              <div className="relative">
                <select
                  value={userFilter}
                  onChange={(e) => setUserFilter(e.target.value as UserFilterType)}
                  className="p-2 rounded-lg bg-white/60 backdrop-blur-sm border placeholder:text-black text-black border-white/30 hover:bg-white/80 transition-all text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="all">All Users</option>
                  <option value="admin">Admins</option>
                  <option value="user">Users</option>
                </select>
              </div>
              <button 
                onClick={() => setShowAddUserModal(true)}
                className="px-4 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all flex items-center space-x-2"
              >
                <UserPlus className="w-4 h-4" />
                <span>Add User</span>
              </button>
            </div>
          </div>

          {usersError && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              <strong className="font-bold">Error: </strong>
              <span className="block sm:inline">{usersError}</span>
            </div>
          )}

          {usersLoading ? (
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p className="text-gray-600 mt-2">Loading users...</p>
            </div>
          ) : getFilteredUsers().length === 0 ? (
            <div className="text-center py-12">
              <Users className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-600">No users found</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-white/30">
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">User</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Type</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Created</th>
                    <th className="text-right py-3 px-4 font-semibold text-gray-700">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {getFilteredUsers().map((user) => (
                    <tr key={user.id} className="border-b border-white/20 hover:bg-white/40 transition-all">
                      <td className="py-4 px-4">
                        <div className="flex items-center space-x-3">
                          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center">
                            <span className="text-white text-sm font-bold">
                              {user.userName.charAt(0).toUpperCase()}
                            </span>
                          </div>
                          <div>
                            <div className="font-medium text-gray-900">{user.userName}</div>
                            <div className="text-sm text-gray-500">ID: {user.id}</div>
                          </div>
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <span className={`inline-block px-2 py-1 rounded-full text-xs font-medium capitalize ${getUserTypeColor(user.userType)}`}>
                          {user.userType}
                        </span>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center space-x-2 text-sm text-gray-600">
                          <Calendar className="w-4 h-4" />
                          <span>{formatDate(user.createdAt)}</span>
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center justify-end space-x-2">
                          <button className="p-2 rounded-lg bg-white/60 border border-white/30 hover:bg-white/80 transition-all">
                            <Edit className="w-4 h-4 text-gray-600" />
                          </button>
                          <button
                            onClick={() => setShowDeleteModal(user.id)}
                            className="p-2 rounded-lg bg-white/60 border border-white/30 hover:bg-red-50 hover:border-red-300 transition-all"
                          >
                            <Trash2 className="w-4 h-4 text-red-600" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Add User Modal */}
        {showAddUserModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-2xl p-6 w-full max-w-md mx-4">
              <div className="flex items-center space-x-3 mb-4">
                <div className="p-3 rounded-full bg-blue-100">
                  <UserPlus className="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <h3 className="text-xl font-bold text-gray-900">Add New User</h3>
                  <p className="text-gray-600">Create a new user account</p>
                </div>
              </div>

              <div className="space-y-4 mb-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
                  <input
                    type="text"
                    value={newUser.username}
                    onChange={(e) => setNewUser({...newUser, username: e.target.value})}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Enter username"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <input
                    type="email"
                    value={newUser.email}
                    onChange={(e) => setNewUser({...newUser, email: e.target.value})}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Enter email"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
                  <input
                    type="password"
                    value={newUser.password}
                    onChange={(e) => setNewUser({...newUser, password: e.target.value})}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Enter password"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">User Type</label>
                  <select
                    value={newUser.userType}
                    onChange={(e) => setNewUser({...newUser, userType: e.target.value})}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="user">User</option>
                    <option value="admin">Admin</option>
                  </select>
                </div>
              </div>

              <div className="flex space-x-3">
                <button
                  onClick={() => {
                    setShowAddUserModal(false);
                    setNewUser({ username: '', email: '', password: '', userType: 'user' });
                  }}
                  className="flex-1 py-2 px-4 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-all"
                  disabled={isCreating}
                >
                  Cancel
                </button>
                <button
                  onClick={handleCreateUser}
                  disabled={isCreating}
                  className="flex-1 py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isCreating ? 'Creating...' : 'Create User'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Delete Confirmation Modal */}
        {showDeleteModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-2xl p-6 w-full max-w-md mx-4">
              <div className="flex items-center space-x-3 mb-4">
                <div className="p-3 rounded-full bg-red-100">
                  <AlertTriangle className="w-6 h-6 text-red-600" />
                </div>
                <div>
                  <h3 className="text-xl font-bold text-gray-900">Delete User</h3>
                  <p className="text-gray-600">This action cannot be undone</p>
                </div>
              </div>

              <p className="text-gray-600 mb-6">
                Are you sure you want to delete this user? All associated data will be permanently removed.
              </p>

              <div className="flex space-x-3">
                <button
                  onClick={() => setShowDeleteModal(null)}
                  className="flex-1 py-2 px-4 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-all"
                >
                  Cancel
                </button>
                <button
                  onClick={() => handleDeleteUser(showDeleteModal)}
                  className="flex-1 py-2 px-4 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-all"
                >
                  Delete User
                </button>
              </div>
            </div>
          </div>
        )}
      </DashboardLayout>
    </RouteGuard>
  );
};

export default UsersPage;