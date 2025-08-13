interface LoadingSkeletonProps {
  className?: string;
  width?: string;
  height?: string;
}

export const LoadingSkeleton = ({ 
  className = '', 
  width = 'w-full', 
  height = 'h-4' 
}: LoadingSkeletonProps) => {
  return (
    <div className={`${width} ${height} bg-gray-300 rounded animate-pulse ${className}`} />
  );
};

export const StatCardSkeleton = () => {
  return (
    <div className="backdrop-blur-md bg-white/60 border border-white/30 rounded-2xl p-6">
      <div className="flex items-center justify-between mb-4">
        <div className="p-3 rounded-xl bg-gray-300 animate-pulse">
          <div className="w-6 h-6" />
        </div>
        <LoadingSkeleton width="w-4" height="h-4" />
      </div>
      <div>
        <LoadingSkeleton height="h-8" className="mb-2" />
        <LoadingSkeleton height="h-4" className="mb-1" />
        <LoadingSkeleton width="w-16" height="h-3" />
      </div>
    </div>
  );
};