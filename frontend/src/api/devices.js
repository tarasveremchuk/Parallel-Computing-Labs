import axios from './axios';

export const devicesAPI = {
  getAll: (params) => axios.get('/v1/devices', { params }),
  getById: (id) => axios.get(`/v1/devices/${id}`),
  create: (data) => axios.post('/v1/devices', data),
  update: (id, data) => axios.put(`/v1/devices/${id}`, data),
  delete: (id) => axios.delete(`/v1/devices/${id}`),
  heartbeat: (id) => axios.post(`/v1/devices/${id}/heartbeat`),
  getStats: (id) => axios.get(`/v1/telemetry/device/${id}/stats`),
};