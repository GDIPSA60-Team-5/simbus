"use client";

import React from 'react';

interface AuthLayoutProps {
  children: React.ReactNode;
  title: string;
  subtitle: string;
  features?: Array<{
    icon: React.ComponentType<any>;
    title: string;
    description: string;
    iconColor: string;
  }>;
}

export const AuthLayout = ({ 
  children, 
  title, 
  subtitle, 
  features = [] 
}: AuthLayoutProps) => {
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

      {/* Left side - Branding */}
      <div className="hidden lg:flex flex-1 relative z-10 flex-col justify-center items-center p-12">
        <div className="max-w-md text-center">
          {/* Logo placeholder and branding */}
          <div className="mb-12">
            <div className="w-50 h-50 backdrop-blur-xl rounded-3xl flex items-center justify-center mb-8 mx-auto border shadow-2xl shadow-orange-200">
              <div className="w-full h-full rounded-2xl flex items-center justify-center bg-opacity-50">
                <div className="text-center">
                  <img src="/logo.png" alt="Nimbus" className="w-full h-full object-contain" />
                </div>
              </div>
            </div>
            <p className="text-xl text-gray-700 leading-relaxed">
              {subtitle}
            </p>
          </div>

          {/* Features */}
          {features.length > 0 && (
            <div className="space-y-6 text-left bg-white bg-opacity-20 backdrop-blur-xl rounded-2xl p-6 border border-white border-opacity-40 shadow-xl">
              {features.map((feature, index) => (
                <div key={index} className="flex items-center space-x-4 text-gray-800">
                  <div className={`w-12 h-12 ${feature.iconColor} rounded-full flex items-center justify-center`}>
                    <feature.icon className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">{feature.title}</h3>
                    <p className="text-sm text-gray-600">{feature.description}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Right side - Auth Form */}
      <div className="flex flex-1 items-center justify-center relative z-10 p-4">
        <div className="w-full max-w-md">
          {/* Glass auth card */}
          <div className="bg-white bg-opacity-25 backdrop-blur-2xl rounded-3xl border border-white border-opacity-50 p-8 shadow-2xl shadow-orange-200">
            <div className="text-center mb-8">
              <h2 className="text-4xl font-bold text-gray-900 mb-2">{title}</h2>
            </div>
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};