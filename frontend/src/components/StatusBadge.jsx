import React from 'react';

const StatusBadge = ({ lastSeen }) => {
  const isOnline = lastSeen && 
    (new Date() - new Date(lastSeen)) < 60000; // 1 минута

  return (
    <div className="flex items-center">
      <div className={`h-2 w-2 rounded-full mr-2 ${
        isOnline ? 'bg-green-500 animate-pulse' : 'bg-gray-300'
      }`} />
      <span className={`text-xs ${
        isOnline ? 'text-green-700' : 'text-gray-500'
      }`}>
        {isOnline ? 'Online' : 'Offline'}
      </span>
    </div>
  );
};

export default StatusBadge;