import axios from './axios';

export const systemAPI = {
  health: () => axios.get('/v1/system/health'),
  exportTelemetry: () => axios.get('/v1/system/export/telemetry', { responseType: 'blob' }),
  exportAlerts: () => axios.get('/v1/system/export/alerts', { responseType: 'blob' }),
};