import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api',
});

export const getComputers = () => API.get('/computers');

export const sendCommand = (computerId, command) => 
  API.post(`/computers/${computerId}/command`, { commandText: command });

export default API;