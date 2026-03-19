import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCommandHistory, sendCommand } from '../services/api';
import { Terminal, Clock, CheckCircle, XCircle, AlertCircle, ArrowLeft } from 'lucide-react';

const CommandsPage = () => {
  const { id } = useParams();
  const [commands, setCommands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCommand, setSelectedCommand] = useState(null);
  const [newCommand, setNewCommand] = useState('');
  const [sending, setSending] = useState(false);
  const [computer, setComputer] = useState(null);

  useEffect(() => {
    fetchCommands();
    const interval = setInterval(fetchCommands, 5000);
    return () => clearInterval(interval);
  }, [id]);

  const fetchCommands = async () => {
    try {
      const response = await getCommandHistory(id);
      setCommands(response.data);
      if (response.data.length > 0 && !computer) {
        setComputer(response.data[0].computer);
      }
    } catch (error) {
      console.error('Ошибка загрузки команд:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSendCommand = async (e) => {
    e.preventDefault();
    if (!newCommand.trim()) return;

    setSending(true);
    try {
      await sendCommand(id, newCommand);
      setNewCommand('');
      setTimeout(fetchCommands, 1000);
    } catch (error) {
      alert('Ошибка отправки команды');
    } finally {
      setSending(false);
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'PENDING':
        return <Clock className="w-5 h-5 text-yellow-500" />;
      case 'ERROR':
        return <XCircle className="w-5 h-5 text-red-500" />;
      default:
        return <AlertCircle className="w-5 h-5 text-gray-500" />;
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'COMPLETED': return 'Выполнено';
      case 'PENDING': return 'Ожидание';
      case 'ERROR': return 'Ошибка';
      default: return status;
    }
  };

  // Функция для определения, нужно ли показывать "Посмотреть полностью"
  const needsFullView = (result) => {
    return result && (result.length > 200 || result.split('\n').length > 10);
  };

  // Обрезаем результат для предпросмотра
  const getPreview = (result) => {
    if (!result) return 'Нет результата';
    const lines = result.split('\n');
    if (lines.length > 5) {
      return lines.slice(0, 5).join('\n') + '\n...';
    }
    if (result.length > 200) {
      return result.substring(0, 200) + '...';
    }
    return result;
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
      {/* Шапка с навигацией */}
      <div className="mb-6">
        <Link to={`/computers/${id}`} className="inline-flex items-center text-blue-600 hover:text-blue-800">
          <ArrowLeft className="w-4 h-4 mr-1" />
          Назад к компьютеру
        </Link>
      </div>

      {/* Информация о компьютере */}
      {computer && (
        <div className="bg-white rounded-lg shadow p-4 mb-6">
          <h2 className="text-xl font-semibold flex items-center gap-2">
            <Terminal className="w-5 h-5" />
            Команды на {computer.name}
          </h2>
          <p className="text-sm text-gray-500 mt-1">IP: {computer.ip || 'Не определен'}</p>
        </div>
      )}

      {/* Форма отправки новой команды */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <form onSubmit={handleSendCommand} className="flex gap-2">
          <input
            type="text"
            value={newCommand}
            onChange={(e) => setNewCommand(e.target.value)}
            placeholder="Введите команду (например: ping 127.0.0.1)"
            className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={sending}
          />
          <button
            type="submit"
            disabled={sending || !newCommand.trim()}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
          >
            {sending ? 'Отправка...' : 'Выполнить'}
          </button>
        </form>
      </div>

      {/* Список команд */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold">История команд</h3>
        
        {commands.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-lg">
            <Terminal className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500">История команд пуста</p>
            <p className="text-gray-400 text-sm mt-1">Отправьте первую команду</p>
          </div>
        ) : (
          commands.map((cmd) => (
            <div key={cmd.id} className="bg-white rounded-lg shadow overflow-hidden">
              {/* Заголовок команды */}
              <div className="bg-gray-50 px-4 py-3 border-b flex items-center justify-between">
                <div className="flex items-center gap-3">
                  {getStatusIcon(cmd.status)}
                  <span className="font-mono font-medium">{cmd.commandText}</span>
                </div>
                <div className="flex items-center gap-3">
                  <span className={`text-xs px-2 py-1 rounded-full ${
                    cmd.status === 'COMPLETED' ? 'bg-green-100 text-green-700' :
                    cmd.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                    'bg-red-100 text-red-700'
                  }`}>
                    {getStatusText(cmd.status)}
                  </span>
                  <span className="text-xs text-gray-500">
                    {new Date(cmd.createdAt).toLocaleString()}
                  </span>
                </div>
              </div>

              {/* Результат команды */}
              {cmd.result && (
                <div className="p-4 bg-gray-50 border-b">
                  <div className="font-mono text-sm whitespace-pre-wrap bg-gray-100 p-3 rounded">
                    {getPreview(cmd.result)}
                  </div>
                  {needsFullView(cmd.result) && (
                    <button
                      onClick={() => setSelectedCommand(cmd)}
                      className="mt-2 text-blue-600 hover:text-blue-800 text-sm font-medium"
                    >
                      Посмотреть полностью →
                    </button>
                  )}
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* Модальное окно для полного результата */}
      {selectedCommand && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] flex flex-col">
            <div className="p-4 border-b flex justify-between items-center">
              <div>
                <h4 className="font-semibold text-lg">Результат команды</h4>
                <p className="text-sm text-gray-500 font-mono mt-1">{selectedCommand.commandText}</p>
              </div>
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
            <div className="p-4 border-t flex justify-end">
              <button
                onClick={() => setSelectedCommand(null)}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Закрыть
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CommandsPage;