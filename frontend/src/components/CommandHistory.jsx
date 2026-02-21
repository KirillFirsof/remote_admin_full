import React, { useState, useEffect } from 'react';
import axios from 'axios';

const CommandHistory = ({ computerId }) => {
  const [commands, setCommands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCommand, setSelectedCommand] = useState(null);

  useEffect(() => {
    fetchCommands();
    const interval = setInterval(fetchCommands, 3000);
    return () => clearInterval(interval);
  }, [computerId]);

  const fetchCommands = async () => {
    try {
      // TODO: Добавить эндпоинт на бэкенде для получения команд по computerId
      // Пока заглушка
      const response = await axios.get(`http://localhost:8080/api/commands?computerId=${computerId}`);
      setCommands(response.data);
    } catch (error) {
      console.error('Ошибка загрузки команд:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const colors = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      COMPLETED: 'bg-green-100 text-green-800',
      ERROR: 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold mb-4">История команд</h3>
      
      {loading && commands.length === 0 ? (
        <div className="flex justify-center py-8">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
        </div>
      ) : commands.length === 0 ? (
        <p className="text-gray-500 text-center py-8">История команд пуста</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Команда</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Статус</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Время</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Результат</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {commands.map((cmd) => (
                <tr key={cmd.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-sm">{cmd.commandText}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 text-xs rounded-full ${getStatusBadge(cmd.status)}`}>
                      {cmd.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-500">
                    {new Date(cmd.createdAt).toLocaleString()}
                  </td>
                  <td className="px-4 py-3">
                    {cmd.result ? (
                      <button
                        onClick={() => setSelectedCommand(cmd)}
                        className="text-blue-600 hover:text-blue-800 text-sm"
                      >
                        Просмотр
                      </button>
                    ) : (
                      <span className="text-gray-400 text-sm">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Модальное окно с результатом */}
      {selectedCommand && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[80vh] flex flex-col">
            <div className="p-4 border-b flex justify-between items-center">
              <h4 className="font-semibold">Результат команды: {selectedCommand.commandText}</h4>
              <button
                onClick={() => setSelectedCommand(null)}
                className="text-gray-500 hover:text-gray-700"
              >
                ✕
              </button>
            </div>
            <div className="p-4 overflow-auto">
              <pre className="bg-gray-50 p-4 rounded-lg text-sm font-mono whitespace-pre-wrap">
                {selectedCommand.result}
              </pre>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CommandHistory;