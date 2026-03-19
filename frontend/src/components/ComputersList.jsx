import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getComputers } from '../services/api';
import { Monitor, Cpu, HardDrive, Wifi, WifiOff } from 'lucide-react';

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

  const isOnline = (lastSeen) => {
    return lastSeen && (new Date() - new Date(lastSeen)) < 60000;
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4">
      <h2 className="text-2xl font-bold mb-6 text-gray-800">
        Компьютеры ({computers.length})
      </h2>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {computers.map((computer) => {
          const online = isOnline(computer.lastSeen);
          
          return (
            <div
              key={computer.id}
              className="bg-white rounded-xl shadow-lg hover:shadow-xl transition-shadow duration-300 overflow-hidden border border-gray-100"
            >
              {/* Верхняя полоска с цветом статуса */}
              <div className={`h-2 ${online ? 'bg-green-500' : 'bg-gray-300'}`} />
              
              {/* Основной контент */}
              <div className="p-5">
                {/* Заголовок с именем и статусом */}
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center space-x-2">
                    <Monitor className={`w-5 h-5 ${online ? 'text-blue-600' : 'text-gray-400'}`} />
                    <h3 className="font-semibold text-lg text-gray-800 truncate">
                      {computer.name}
                    </h3>
                  </div>
                  <div className="flex items-center">
                    {online ? (
                      <Wifi className="w-4 h-4 text-green-500" />
                    ) : (
                      <WifiOff className="w-4 h-4 text-gray-400" />
                    )}
                  </div>
                </div>

                {/* IP адрес */}
                <div className="mb-3 text-sm text-gray-600">
                  <span className="font-mono bg-gray-100 px-2 py-1 rounded">
                    {computer.ip || 'IP не определен'}
                  </span>
                </div>

                {/* Метрики CPU и RAM */}
                <div className="space-y-3 mb-4">
                  {/* CPU */}
                  <div>
                    <div className="flex items-center justify-between text-sm mb-1">
                      <div className="flex items-center space-x-1">
                        <Cpu className="w-4 h-4 text-gray-500" />
                        <span className="text-gray-600">CPU</span>
                      </div>
                      <span className="font-medium">{computer.cpuLoad?.toFixed(1)}%</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className={`rounded-full h-2 transition-all duration-500 ${
                          (computer.cpuLoad || 0) > 80 ? 'bg-red-500' : 'bg-blue-600'
                        }`}
                        style={{ width: `${Math.min(computer.cpuLoad || 0, 100)}%` }}
                      />
                    </div>
                  </div>

                  {/* RAM */}
                  <div>
                    <div className="flex items-center justify-between text-sm mb-1">
                      <div className="flex items-center space-x-1">
                        <HardDrive className="w-4 h-4 text-gray-500" />
                        <span className="text-gray-600">RAM</span>
                      </div>
                      <span className="font-medium">{computer.freeRamMb} MB</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-green-500 rounded-full h-2"
                        style={{ width: `${Math.min((computer.freeRamMb || 0) / 16, 100)}%` }}
                      />
                    </div>
                  </div>
                </div>

                {/* Время последнего сигнала */}
                <div className="text-xs text-gray-500 mb-4">
                  Последний сигнал: {new Date(computer.lastSeen).toLocaleString()}
                </div>

                {/* Кнопки действий */}
                <div className="flex space-x-2">
                  <Link
                    to={`/computers/${computer.id}`}
                    className="flex-1 bg-blue-600 hover:bg-blue-700 text-white text-center py-2 px-4 rounded-lg transition-colors text-sm font-medium"
                  >
                    Подробнее
                  </Link>
                  <Link
                    to={`/computers/${computer.id}/commands`}
                    className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 text-center py-2 px-4 rounded-lg transition-colors text-sm font-medium"
                  >
                    Команды
                  </Link>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Сообщение если нет компьютеров */}
      {computers.length === 0 && !loading && (
        <div className="text-center py-12">
          <Monitor className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <p className="text-gray-500 text-lg">Нет активных компьютеров</p>
          <p className="text-gray-400 text-sm mt-2">
            Запустите агент на удаленном компьютере
          </p>
        </div>
      )}
    </div>
  );
};

export default ComputersList;