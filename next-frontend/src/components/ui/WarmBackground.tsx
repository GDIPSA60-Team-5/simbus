"use client";

import React from 'react';

type VariantType = 'default' | 'dashboard' | 'users' | 'chatbot' | 'analytics' | 'feedback';

interface WarmBackgroundProps {
  children: React.ReactNode;
  variant?: VariantType;
}

interface GradientSet {
  primary: string;
  secondary: string;
  tertiary: string;
  quaternary: string;
  accent?: string; // Made optional instead of explicitly undefined
}

export const WarmBackground: React.FC<WarmBackgroundProps> = ({
  children,
  variant = 'default'
}) => {
  const getGradientStyles = (): GradientSet => {
    const baseGradients: Record<VariantType, GradientSet> = {
      default: {
        primary: 'radial-gradient(circle at 20% 20%, rgba(255, 154, 0, 0.3) 0%, transparent 50%)',
        secondary: 'radial-gradient(circle at 80% 40%, rgba(255, 193, 7, 0.25) 0%, transparent 50%)',
        tertiary: 'radial-gradient(circle at 40% 80%, rgba(255, 183, 77, 0.2) 0%, transparent 50%)',
        quaternary: 'radial-gradient(circle at 90% 90%, rgba(255, 138, 101, 0.15) 0%, transparent 50%)',
      },
      dashboard: {
        primary: 'radial-gradient(circle at 15% 25%, rgba(255, 154, 0, 0.35) 0%, transparent 55%)',
        secondary: 'radial-gradient(circle at 85% 30%, rgba(255, 193, 7, 0.3) 0%, transparent 50%)',
        tertiary: 'radial-gradient(circle at 30% 85%, rgba(255, 183, 77, 0.25) 0%, transparent 50%)',
        quaternary: 'radial-gradient(circle at 75% 75%, rgba(255, 138, 101, 0.2) 0%, transparent 50%)',
        accent: 'radial-gradient(circle at 60% 10%, rgba(255, 206, 84, 0.15) 0%, transparent 40%)',
      },
      users: {
        primary: 'radial-gradient(circle at 25% 15%, rgba(59, 130, 246, 0.3) 0%, transparent 50%)',
        secondary: 'radial-gradient(circle at 75% 35%, rgba(255, 154, 0, 0.28) 0%, transparent 50%)',
        tertiary: 'radial-gradient(circle at 45% 85%, rgba(147, 197, 253, 0.22) 0%, transparent 50%)',
        quaternary: 'radial-gradient(circle at 85% 85%, rgba(255, 183, 77, 0.18) 0%, transparent 50%)',
      },
      chatbot: {
        primary: 'radial-gradient(circle at 20% 30%, rgba(139, 92, 246, 0.3) 0%, transparent 50%)',
        secondary: 'radial-gradient(circle at 80% 20%, rgba(255, 154, 0, 0.28) 0%, transparent 50%)',
        tertiary: 'radial-gradient(circle at 35% 80%, rgba(196, 181, 253, 0.22) 0%, transparent 50%)',
        quaternary: 'radial-gradient(circle at 90% 90%, rgba(255, 193, 7, 0.18) 0%, transparent 50%)',
      },
      analytics: {
        primary: 'radial-gradient(circle at 30% 20%, rgba(16, 185, 129, 0.3) 0%, transparent 50%)',
        secondary: 'radial-gradient(circle at 70% 40%, rgba(255, 154, 0, 0.28) 0%, transparent 50%)',
        tertiary: 'radial-gradient(circle at 20% 90%, rgba(110, 231, 183, 0.22) 0%, transparent 50%)',
        quaternary: 'radial-gradient(circle at 85% 80%, rgba(255, 183, 77, 0.18) 0%, transparent 50%)',
      },
      feedback: {
        primary: 'radial-gradient(circle at 25% 25%, rgba(245, 158, 11, 0.3) 0%, transparent 50%)',
        secondary: 'radial-gradient(circle at 75% 30%, rgba(255, 154, 0, 0.28) 0%, transparent 50%)',
        tertiary: 'radial-gradient(circle at 40% 85%, rgba(253, 224, 71, 0.22) 0%, transparent 50%)',
        quaternary: 'radial-gradient(circle at 90% 85%, rgba(255, 138, 101, 0.18) 0%, transparent 50%)',
      },
    };

    return baseGradients[variant];
  };

  const gradients = getGradientStyles();

  return (
    <div className="min-h-screen relative overflow-hidden">
      {/* Base warm background */}
      <div
        className="fixed inset-0 -z-10"
        style={{
          background: 'linear-gradient(135deg, #FFF8F0 0%, #FFF4E6 25%, #FFFBF0 50%, #FFF8F0 75%, #FFF2E6 100%)'
        }}
      />

      {/* Animated radial gradients */}
      <div
        className="fixed inset-0 -z-10 opacity-80"
        style={{
          background: `
            ${gradients.primary},
            ${gradients.secondary},
            ${gradients.tertiary},
            ${gradients.quaternary}
            ${gradients.accent ? `, ${gradients.accent}` : ''}
          `,
          animation: 'float 20s ease-in-out infinite'
        }}
      />

      {/* Additional floating orbs for extra warmth */}
      <div className="fixed inset-0 -z-10 opacity-60">
        <div
          className="absolute w-96 h-96 rounded-full"
          style={{
            top: '10%',
            left: '5%',
            background: 'radial-gradient(circle, rgba(255, 206, 84, 0.1) 0%, transparent 70%)',
            animation: 'float-slow 25s ease-in-out infinite'
          }}
        />
        <div
          className="absolute w-80 h-80 rounded-full"
          style={{
            top: '60%',
            right: '10%',
            background: 'radial-gradient(circle, rgba(255, 183, 77, 0.12) 0%, transparent 70%)',
            animation: 'float-reverse 22s ease-in-out infinite'
          }}
        />
        <div
          className="absolute w-64 h-64 rounded-full"
          style={{
            bottom: '20%',
            left: '30%',
            background: 'radial-gradient(circle, rgba(255, 154, 0, 0.08) 0%, transparent 70%)',
            animation: 'float 18s ease-in-out infinite'
          }}
        />
      </div>

      {/* Content */}
      <div className="relative z-10">
        {children}
      </div>

      <style jsx>{`
        @keyframes float {
          0%, 100% { transform: translateY(0px) translateX(0px) rotate(0deg); }
          33% { transform: translateY(-20px) translateX(10px) rotate(1deg); }
          66% { transform: translateY(10px) translateX(-5px) rotate(-1deg); }
        }
        
        @keyframes float-slow {
          0%, 100% { transform: translateY(0px) translateX(0px) scale(1); }
          50% { transform: translateY(-30px) translateX(15px) scale(1.05); }
        }
        
        @keyframes float-reverse {
          0%, 100% { transform: translateY(0px) translateX(0px) rotate(0deg); }
          33% { transform: translateY(15px) translateX(-8px) rotate(-0.5deg); }
          66% { transform: translateY(-10px) translateX(12px) rotate(0.5deg); }
        }
      `}</style>
    </div>
  );
};