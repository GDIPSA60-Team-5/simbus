"use client";
import { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Eye, EyeOff, User, Lock, ArrowRight, Bus, Users, MessageSquare, Star } from "lucide-react";
import { AuthLayout } from "@/components/layout/AuthLayout";
import { useAuth } from "@/hooks/useAuth";

// Separate component that uses useSearchParams
function LoginForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const router = useRouter();
  const searchParams = useSearchParams();
  const { login, isAuthenticated, loading: authLoading } = useAuth();

  // Redirect if already authenticated
  useEffect(() => {
    if (!authLoading && isAuthenticated) {
      router.replace('/dashboard');
    }
  }, [isAuthenticated, authLoading, router]);

  const handleLogin = async () => {
    if (!username || !password) {
      setError("Please enter both username and password");
      return;
    }

    setError("");
    setLoading(true);

    try {
      await login(username, password);

      // Get redirect URL from search params or default to dashboard
      const redirectTo = searchParams.get('redirect') || '/dashboard';
      router.push(redirectTo);
    } catch (err: any) {
      setError(err.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: any) => {
    if (e.key === 'Enter') {
      handleLogin();
    }
  };

  const features = [
    {
      icon: Users,
      title: "User Analytics",
      description: "Track active users and engagement",
      iconColor: "bg-orange-100 text-orange-600"
    },
    {
      icon: MessageSquare,
      title: "Chatbot Performance",
      description: "Monitor AI interactions and success rates",
      iconColor: "bg-blue-100 text-blue-600"
    },
    {
      icon: Star,
      title: "User Feedback",
      description: "Real-time ratings and reviews",
      iconColor: "bg-yellow-100 text-yellow-600"
    }
  ];

  // Show loading spinner while checking authentication
  if (authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-yellow-50 via-orange-50 to-amber-100">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-orange-200 border-t-orange-600 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <AuthLayout
      title="Admin Portal"
      subtitle="Monitor, analyze, and optimize your Nimbus app ecosystem."
      features={features}
    >
      <div className="text-center mb-8">
        <p className="text-gray-700 text-lg">Access your product dashboard</p>
      </div>

      <div className="space-y-6">
        {/* Username Input */}
        <div className="relative group">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <User className="h-6 w-6 text-gray-500 group-focus-within:text-orange-600 transition-colors" />
          </div>
          <input
            type="text"
            placeholder="Admin Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            onKeyPress={handleKeyPress}
            className="w-full pl-12 pr-4 py-4 bg-opacity-40 backdrop-blur-xl border border-white border-opacity-60 rounded-2xl text-gray-900 placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-orange-400 transition-all duration-300 hover:bg-opacity-50 shadow-lg"
            required
          />
        </div>

        {/* Password Input */}
        <div className="relative group">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <Lock className="h-6 w-6 text-gray-500 group-focus-within:text-orange-600 transition-colors" />
          </div>
          <input
            type={showPassword ? "text" : "password"}
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyPress={handleKeyPress}
            className="w-full pl-12 pr-14 py-4 bg-opacity-40 backdrop-blur-xl border border-white border-opacity-60 rounded-2xl text-gray-900 placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-orange-400 transition-all duration-300 hover:bg-opacity-50 shadow-lg"
            required
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-500 hover:text-orange-600 transition-colors"
          >
            {showPassword ? <EyeOff className="h-6 w-6" /> : <Eye className="h-6 w-6" />}
          </button>
        </div>


        {/* Login / View Analytics Button */}
        <button
          onClick={handleLogin}
          disabled={loading}
          className="w-full relative py-5 bg-gradient-to-r from-orange-500 via-amber-500 to-orange-600 rounded-2xl font-bold text-white shadow-2xl shadow-orange-300 hover:shadow-3xl hover:shadow-orange-400 transition-all duration-500 hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 overflow-hidden"
        >
          {/* Gradient hover overlay */}
          <div className="absolute inset-0 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-500 opacity-0 group-hover:opacity-100 transition-opacity duration-500 rounded-2xl pointer-events-none"></div>

          {/* Subtle shine overlay */}
          <div className="absolute inset-0 bg-white bg-opacity-10 opacity-0 group-hover:opacity-100 transition-opacity duration-500 rounded-2xl pointer-events-none"></div>

          <div className="relative flex items-center justify-center">
            {loading ? (
              <div className="w-7 h-7 border-3 border-white border-opacity-30 border-t-white rounded-full animate-spin"></div>
            ) : (
              <div className="flex items-center">
                <Bus className="mr-3 h-6 w-6 transition-transform hover:scale-110" />
                <span className="text-xl">View Analytics</span>
                <ArrowRight className="ml-3 h-6 w-6 transition-transform hover:translate-x-2" />
              </div>
            )}
          </div>
        </button>

      </div>

      {/* Error Message */}
      {error && (
        <div className="mt-6 p-4 bg-red-100 bg-opacity-60 backdrop-blur-xl border border-red-200 border-opacity-50 rounded-2xl shadow-lg">
          <p className="text-red-800 text-center font-medium">{error}</p>
        </div>
      )}

      {/* Sign Up Link */}
      <div className="mt-8 text-center">
        <p className="text-gray-700">
          Need admin access?{' '}
          <button className="text-orange-600 font-bold hover:text-orange-700 transition-colors">
            Contact Support
          </button>
        </p>
      </div>
    </AuthLayout>
  );
}

// Loading fallback component
function LoginLoadingFallback() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-yellow-50 via-orange-50 to-amber-100">
      <div className="text-center">
        <div className="w-16 h-16 border-4 border-orange-200 border-t-orange-600 rounded-full animate-spin mx-auto mb-4"></div>
        <p className="text-gray-600">Loading login page...</p>
      </div>
    </div>
  );
}

// Main page component wrapped with Suspense
export default function LoginPage() {
  return (
    <Suspense fallback={<LoginLoadingFallback />}>
      <LoginForm />
    </Suspense>
  );
}