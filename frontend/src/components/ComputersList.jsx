import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getComputers } from '../services/api';
import StatusBadge from './StatusBadge';

const ComputersList = () => {
  const [computers, setComputers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchComputers();
    const interval = setInterval(fetchComputers, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchComputers = async () => {
    try {
      const response = await getComputers();
      setComputers(response.data);
    } catch (error) {
      console.error('Ошибка загрузки:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Статус</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Имя</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">CPU</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">RAM</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Последний сигнал</th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Действия</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {computers.map((computer) => (
            <tr key={computer.id} className="hover:bg-gray-50">
              <td className="px-6 py-4 whitespace-nowrap">
                <StatusBadge lastSeen={computer.lastSeen} />
              </td>
              <td className="px-6 py-4 whitespace-nowrap font-medium">{computer.name}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{computer.ip || 'N/A'}</td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="flex items-center">
                  <div className="w-16 bg-gray-200 rounded-full h-2 mr-2">
                    <div 
                      className="bg-blue-600 rounded-full h-2" 
                      style={{ width: `${computer.cpuLoad || 0}%` }}
                    />
                  </div>
                  <span className="text-sm">{computer.cpuLoad?.toFixed(1)}%</span>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm">{computer.freeRamMb} MB</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {new Date(computer.lastSeen).toLocaleString()}
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <Link
                  to={`/computers/${computer.id}`}
                  className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                >
                  Подробнее
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ComputersList;