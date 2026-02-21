import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getComputers, sendCommand } from '../services/api';
import StatusBadge from './StatusBadge';
import CommandHistory from './CommandHistory';

const ComputerDetails = () => {
  const { id } = useParams();
  const [computer, setComputer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [command, setCommand] = useState('');
  const [sending, setSending] = useState(false);

  useEffect(() => {
    fetchComputer();
    const interval = setInterval(fetchComputer, 5000);
    return () => clearInterval(interval);
  }, [id]);

  const fetchComputer = async () => {
    try {
      const response = await getComputers();
      const found = response.data.find(c => c.id === parseInt(id));
      setComputer(found);
    } catch (error) {
      console.error('Ошибка загрузки:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSendCommand = async (e) => {
    e.preventDefault();
    if (!command.trim()) return;

    setSending(true);
    try {
      await sendCommand(computer.id, command);
      setCommand('');
      // Обновим историю команд
      setTimeout(fetchComputer, 1000);
    } catch (error) {
      alert('Ошибка отправки команды');
    } finally {
      setSending(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!computer) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">Компьютер не найден</p>
        <Link to="/" className="text-blue-600 hover:text-blue-800 mt-4 inline-block">
          ← Вернуться к списку
        </Link>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6">
        <Link to="/" className="text-blue-600 hover:text-blue-800 flex items-center gap-1">
          ← Назад к списку
        </Link>
      </div>

      {/* Информация о компьютере */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-2xl font-semibold mb-4">{computer.name}</h2>
        
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-sm text-gray-500">IP адрес</p>
            <p className="font-medium">{computer.ip || 'N/A'}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Статус</p>
            <StatusBadge lastSeen={computer.lastSeen} />
          </div>
          <div>
            <p className="text-sm text-gray-500">Загрузка CPU</p>
            <div className="flex items-center gap-2">
              <div className="w-32 bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-blue-600 rounded-full h-2" 
                  style={{ width: `${computer.cpuLoad || 0}%` }}
                />
              </div>
              <span>{computer.cpuLoad?.toFixed(1)}%</span>
            </div>
          </div>
          <div>
            <p className="text-sm text-gray-500">Свободная RAM</p>
            <p className="font-medium">{computer.freeRamMb} MB</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Последний сигнал</p>
            <p className="font-medium">{new Date(computer.lastSeen).toLocaleString()}</p>
          </div>
        </div>
      </div>

      {/* Форма отправки команды */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h3 className="text-lg font-semibold mb-4">Отправить команду</h3>
        <form onSubmit={handleSendCommand} className="flex gap-2">
          <input
            type="text"
            value={command}
            onChange={(e) => setCommand(e.target.value)}
            placeholder="Введите команду (например: dir, ipconfig)"
            className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={sending}
          />
          <button
            type="submit"
            disabled={sending || !command.trim()}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
          >
            {sending ? 'Отправка...' : 'Выполнить'}
          </button>
        </form>
      </div>

      {/* История команд */}
      <CommandHistory computerId={computer.id} />
    </div>
  );
};

export default ComputerDetails;