import React, { useState } from 'react';
import { sendCommand } from '../services/api';

const CommandForm = ({ computer, onClose, onSuccess }) => {
  const [command, setCommand] = useState('');
  const [sending, setSending] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!command.trim()) return;

    setSending(true);
    try {
      await sendCommand(computer.id, command);
      onSuccess?.();
      onClose();
    } catch (error) {
      alert('Ошибка отправки команды');
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg max-w-md w-full">
        <div className="p-6">
          <h3 className="text-lg font-medium mb-4">
            Отправить команду на {computer.name}
          </h3>
          <form onSubmit={handleSubmit}>
            <textarea
              value={command}
              onChange={(e) => setCommand(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono"
              rows="4"
              placeholder="Введите команду (например: dir, ipconfig)"
              disabled={sending}
            />
            <div className="flex justify-end gap-2 mt-4">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-gray-600 hover:text-gray-800"
                disabled={sending}
              >
                Отмена
              </button>
              <button
                type="submit"
                disabled={sending}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {sending ? 'Отправка...' : 'Отправить'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CommandForm;