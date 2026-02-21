import React, { useState, useEffect } from 'react';
import { getComputers } from './services/api';
import ComputersList from './components/ComputersList';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import ComputerDetails from './components/ComputerDetails';

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
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        <nav className="bg-white shadow-sm">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16 items-center">
              <Link to="/" className="text-xl font-semibold text-gray-800">
                Remote Admin Panel
              </Link>
            </div>
          </div>
        </nav>

        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            <Route path="/" element={<ComputersList />} />
            <Route path="/computers/:id" element={<ComputerDetails />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;