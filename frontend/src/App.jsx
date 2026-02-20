import React, { useState, useEffect } from 'react';
import { getComputers } from './services/api';
import ComputersTable from './components/ComputersTable';

function App() {
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

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <h1 className="text-xl font-semibold text-gray-800">
              Remote Admin Panel
            </h1>
            <span className="text-sm text-gray-500">
              {computers.length} компьютеров
            </span>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {loading ? (
          <div className="flex justify-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        ) : (
          <ComputersTable computers={computers} onCommandSent={fetchComputers} />
        )}
      </main>
    </div>
  );
}

export default App;