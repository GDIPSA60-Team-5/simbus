"use client";

import React from 'react';

interface DataPoint {
  timestamp: number;
  value: number;
}

interface LineChartProps {
  data: [number, number][];
  title: string;
  color?: string;
  unit?: string;
  height?: number;
  showGrid?: boolean;
  className?: string;
}

export const LineChart: React.FC<LineChartProps> = ({
  data,
  title,
  color = '#3B82F6',
  unit = '',
  height = 200,
  showGrid = true,
  className = ''
}) => {
  if (data.length === 0) {
    return (
      <div className={`backdrop-blur-xl bg-white/40 border border-orange-200/30 rounded-2xl p-6 shadow-lg shadow-orange-100/20 ${className}`}>
        <h3 className="text-lg font-semibold text-amber-900 mb-4">{title}</h3>
        <div className="flex items-center justify-center text-amber-600/60" style={{ height }}>
          No data available
        </div>
      </div>
    );
  }

  const maxValue = Math.max(...data.map(([, value]) => value));
  const minValue = Math.min(...data.map(([, value]) => value));
  const range = maxValue - minValue || 1;
  const padding = 40;

  // Generate SVG path for the line
  const generatePath = (points: [number, number][]): string => {
    if (points.length === 0) return '';

    const width = 400;
    const chartHeight = height - padding * 2;
    
    const pathData = points.map(([timestamp, value], index) => {
      const x = (index / (points.length - 1)) * width + padding;
      const y = padding + chartHeight - ((value - minValue) / range) * chartHeight;
      return index === 0 ? `M ${x} ${y}` : `L ${x} ${y}`;
    });

    return pathData.join(' ');
  };

  // Generate grid lines
  const generateGridLines = () => {
    const lines = [];
    const gridCount = 5;
    const width = 400;
    const chartHeight = height - padding * 2;

    for (let i = 0; i <= gridCount; i++) {
      const y = padding + (i / gridCount) * chartHeight;
      lines.push(
        <line
          key={`grid-${i}`}
          x1={padding}
          y1={y}
          x2={width + padding}
          y2={y}
          stroke="#E5E7EB"
          strokeWidth="1"
          opacity="0.5"
        />
      );
    }

    return lines;
  };

  // Format timestamp for display
  const formatTime = (timestamp: number): string => {
    return new Date(timestamp).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // Format value for display
  const formatValue = (value: number): string => {
    if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`;
    if (value >= 1000) return `${(value / 1000).toFixed(1)}K`;
    return value.toFixed(1);
  };

  return (
    <div className={`backdrop-blur-xl bg-white/40 border border-orange-200/30 rounded-2xl p-6 shadow-lg shadow-orange-100/20 ${className}`}>
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-amber-900">{title}</h3>
        <div className="text-sm text-amber-700">
          {data.length > 0 && (
            <span className="font-medium" style={{ color }}>
              {formatValue(data[data.length - 1][1])}{unit}
            </span>
          )}
        </div>
      </div>

      <div className="relative">
        <svg 
          width="100%" 
          height={height}
          viewBox={`0 0 ${400 + padding * 2} ${height}`}
          className="overflow-visible"
        >
          {/* Grid lines */}
          {showGrid && generateGridLines()}

          {/* Main line path */}
          <path
            d={generatePath(data.slice(-50))} // Show last 50 points for better performance
            fill="none"
            stroke={color}
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* Area fill */}
          <path
            d={`${generatePath(data.slice(-50))} L ${400 + padding} ${height - padding} L ${padding} ${height - padding} Z`}
            fill={color}
            fillOpacity="0.1"
          />

          {/* Data points */}
          {data.slice(-20).map(([timestamp, value], index) => {
            const x = (index / (Math.min(data.length, 20) - 1)) * 400 + padding;
            const y = padding + (height - padding * 2) - ((value - minValue) / range) * (height - padding * 2);
            
            return (
              <circle
                key={index}
                cx={x}
                cy={y}
                r="3"
                fill={color}
                className="hover:r-5 transition-all cursor-pointer"
              >
                <title>{`${formatTime(timestamp)}: ${formatValue(value)}${unit}`}</title>
              </circle>
            );
          })}

          {/* Y-axis labels */}
          {[0, 0.25, 0.5, 0.75, 1].map((ratio, index) => {
            const value = minValue + ratio * range;
            const y = padding + (height - padding * 2) * (1 - ratio);
            
            return (
              <text
                key={index}
                x={padding - 10}
                y={y + 4}
                textAnchor="end"
                className="text-xs fill-gray-500"
              >
                {formatValue(value)}
              </text>
            );
          })}
        </svg>

        {/* Time labels */}
        <div className="flex justify-between text-xs text-amber-600/70 mt-2">
          <span>{data.length > 0 ? formatTime(data[0][0]) : ''}</span>
          <span>{data.length > 0 ? formatTime(data[data.length - 1][0]) : ''}</span>
        </div>
      </div>

      {/* Legend */}
      <div className="flex items-center justify-between mt-4 text-xs text-amber-600/70">
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2">
            <div 
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: color }}
            />
            <span>Current: {data.length > 0 ? `${formatValue(data[data.length - 1][1])}${unit}` : '---'}</span>
          </div>
          <div>
            Max: {formatValue(maxValue)}{unit}
          </div>
          <div>
            Min: {formatValue(minValue)}{unit}
          </div>
        </div>
        <div>
          {data.length} data points
        </div>
      </div>
    </div>
  );
};