"use client";
import { apiPost } from "@/lib/apiClient";
import { useState } from "react";
import { Eye, EyeOff, User, Lock, ArrowRight, Bus, Route, Clock, MapPin } from "lucide-react";

type LoginResponse = {
  token: string;
};
export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleLogin = async () => {
    if (!username || !password) {
      setError("Please enter both username and password");
      return;
    }

    setError("");
    setLoading(true);

    try {
      const data = await apiPost<LoginResponse>(`${backendUrl}/api/auth/login`, {
        username,
        password,
      });
      console.log("Login successful", data.token);
      alert("Login successful!");
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: any) => {
    if (e.key === 'Enter') {
      handleLogin();
    }
  };

  return (
    <div className="min-h-screen flex relative overflow-hidden">
      {/* Morning Sun Background */}
      <div className="fixed inset-0 bg-gradient-to-br from-yellow-50 via-orange-50 to-amber-100">
        {/* Sun rays effect */}
        <div className="absolute inset-0">
          <div className="absolute top-0 left-1/4 w-96 h-96 bg-gradient-radial from-yellow-200 via-orange-200 to-transparent opacity-60 rounded-full filter blur-3xl animate-pulse"></div>
          <div className="absolute top-20 right-1/3 w-80 h-80 bg-gradient-radial from-amber-200 via-yellow-100 to-transparent opacity-40 rounded-full filter blur-2xl animate-pulse"></div>
          <div className="absolute bottom-10 left-1/2 w-72 h-72 bg-gradient-radial from-orange-200 via-amber-100 to-transparent opacity-50 rounded-full filter blur-3xl animate-pulse"></div>
        </div>

        {/* Warm light rays */}
        <div className="absolute inset-0 bg-gradient-to-t from-transparent via-orange-50 to-amber-50 opacity-30"></div>

        {/* Subtle texture */}
        <div className="absolute inset-0 opacity-10">
          <div className="absolute inset-0" style={{
            backgroundImage: `radial-gradient(circle at 2px 2px, rgba(251,191,36,0.3) 1px, transparent 0)`,
            backgroundSize: '50px 50px'
          }}></div>
        </div>
      </div>

      {/* Left side - Bus App Branding */}
      <div className="hidden lg:flex flex-1 relative z-10 flex-col justify-center items-center p-12">
        <div className="max-w-md text-center">
          {/* Logo placeholder and branding */}
          <div className="mb-12">
            {/* Your Logo Goes Here */}
            <div className="w-32 h-32 bg-white bg-opacity-40 backdrop-blur-xl rounded-3xl flex items-center justify-center mb-8 mx-auto border border-white border-opacity-60 shadow-2xl shadow-orange-200">
              {/* REPLACE THIS SECTION WITH YOUR LOGO */}
              <div className="w-full h-full rounded-2xl flex items-center justify-center bg-gray-100 bg-opacity-50">
                <div className="text-center">
                  <div className="text-gray-500 text-xs mb-1">YOUR LOGO</div>
                  <div className="text-gray-400 text-xs">GOES HERE</div>
                </div>
              </div>
              {/* END LOGO SECTION */}
            </div>
            <h1 className="text-6xl font-bold mb-4 text-gray-900">
              Nimbus Transit
            </h1>
            <p className="text-xl text-gray-700 leading-relaxed">
              Your journey starts here. Smart, reliable, and always on time.
            </p>
          </div>

          {/* Bus-related features */}
          <div className="space-y-6 text-left bg-white bg-opacity-20 backdrop-blur-xl rounded-2xl p-6 border border-white border-opacity-40 shadow-xl">
            <div className="flex items-center space-x-4 text-gray-800">
              <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                <Route className="w-6 h-6 text-orange-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Real-time Routes</h3>
                <p className="text-sm text-gray-600">Live tracking and updates</p>
              </div>
            </div>
            <div className="flex items-center space-x-4 text-gray-800">
              <div className="w-12 h-12 bg-amber-100 rounded-full flex items-center justify-center">
                <Clock className="w-6 h-6 text-amber-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Smart Scheduling</h3>
                <p className="text-sm text-gray-600">Optimized departure times</p>
              </div>
            </div>
            <div className="flex items-center space-x-4 text-gray-800">
              <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                <MapPin className="w-6 h-6 text-orange-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">GPS Navigation</h3>
                <p className="text-sm text-gray-600">Precise location services</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Right side - Login Form */}
      <div className="flex flex-1 items-center justify-center relative z-10 p-4">
        <div className="w-full max-w-md">
          {/* Glass login card */}
          <div className="bg-white bg-opacity-25 backdrop-blur-2xl rounded-3xl border border-white border-opacity-50 p-8 shadow-2xl shadow-orange-200">
            <div className="text-center mb-8">
              <h2 className="text-4xl font-bold text-gray-900 mb-2">Welcome Back</h2>
              <p className="text-gray-700 text-lg">Sign in to your transit dashboard</p>
            </div>

            <div className="space-y-6">
              {/* Username Input */}
              <div className="relative group">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <User className="h-6 w-6 text-gray-500 group-focus-within:text-orange-600 transition-colors" />
                </div>
                <input
                  type="text"
                  placeholder="Username or Email"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  onKeyPress={handleKeyPress}
                  className="w-full pl-12 pr-4 py-4 bg-white bg-opacity-40 backdrop-blur-xl border border-white border-opacity-60 rounded-2xl text-gray-900 placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-orange-400 transition-all duration-300 hover:bg-opacity-50 shadow-lg"
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
                  className="w-full pl-12 pr-14 py-4 bg-white bg-opacity-40 backdrop-blur-xl border border-white border-opacity-60 rounded-2xl text-gray-900 placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-orange-400 transition-all duration-300 hover:bg-opacity-50 shadow-lg"
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

              {/* Remember Me & Forgot Password */}
              <div className="flex items-center justify-between text-sm">
                <label className="flex items-center text-gray-700 cursor-pointer hover:text-gray-900 transition-colors">
                  <input type="checkbox" className="mr-3 rounded border-gray-300 bg-white bg-opacity-60 text-orange-600 focus:ring-orange-500" />
                  Remember me
                </label>
                <button className="text-gray-700 hover:text-orange-600 transition-colors font-medium">
                  Forgot password?
                </button>
              </div>

              {/* Login Button - Captivating CTA */}
              <button
                onClick={handleLogin}
                disabled={loading}
                className="w-full group relative py-5 bg-gradient-to-r from-orange-500 via-amber-500 to-orange-600 rounded-2xl font-bold text-white shadow-2xl shadow-orange-300 hover:shadow-3xl hover:shadow-orange-400 transition-all duration-500 hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 overflow-hidden transform hover:-translate-y-1"
              >
                <div className="absolute inset-0 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-500 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
                <div className="absolute inset-0 bg-white bg-opacity-20 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
                <div className="relative flex items-center justify-center">
                  {loading ? (
                    <div className="w-7 h-7 border-3 border-white border-opacity-30 border-t-white rounded-full animate-spin"></div>
                  ) : (
                    <div className="flex items-center">
                      <Bus className="mr-3 h-6 w-6 transition-transform group-hover:scale-110" />
                      <span className="text-xl">Start Your Journey</span>
                      <ArrowRight className="ml-3 h-6 w-6 transition-transform group-hover:translate-x-2" />
                    </div>
                  )}
                </div>
              </button>

              {/* Demo Credentials Helper */}
              <div className="mt-4 p-4 bg-white bg-opacity-30 backdrop-blur-xl rounded-2xl border border-white border-opacity-50 shadow-lg">
                <p className="text-gray-800 text-sm text-center">
                  <span className="text-gray-900 font-bold">Demo Access:</span> username: "demo", password: "demo"
                </p>
              </div>
            </div>

            {/* Error Message */}
            {error && (
              <div className="mt-6 p-4 bg-red-100 bg-opacity-60 backdrop-blur-xl border border-red-200 border-opacity-50 rounded-2xl shadow-lg">
                <p className="text-red-800 text-center font-medium">{error}</p>
              </div>
            )}

            {/* Alternative sign-in */}
            <div className="mt-8">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-400 border-opacity-30"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-4 bg-white bg-opacity-50 backdrop-blur-xl text-gray-600 rounded-full border border-white border-opacity-60">Quick Access</span>
                </div>
              </div>

              <div className="mt-6 grid grid-cols-2 gap-4">
                <button className="w-full inline-flex justify-center py-4 px-4 rounded-2xl border border-white border-opacity-60 bg-white bg-opacity-30 backdrop-blur-xl text-gray-700 hover:bg-opacity-40 hover:text-gray-900 transition-all duration-300 shadow-lg hover:shadow-xl">
                  <svg className="h-6 w-6" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                  </svg>
                </button>
                <button className="w-full inline-flex justify-center py-4 px-4 rounded-2xl border border-white border-opacity-60 bg-white bg-opacity-30 backdrop-blur-xl text-gray-700 hover:bg-opacity-40 hover:text-gray-900 transition-all duration-300 shadow-lg hover:shadow-xl">
                  <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z" />
                  </svg>
                </button>
              </div>
            </div>

            {/* Sign Up Link */}
            <div className="mt-8 text-center">
              <p className="text-gray-700">
                New to transit management?{' '}
                <button className="text-orange-600 font-bold hover:text-orange-700 transition-colors">
                  Create account
                </button>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}