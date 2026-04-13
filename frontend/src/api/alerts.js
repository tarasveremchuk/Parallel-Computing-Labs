import api from './axios';

export const alertsAPI = {
  getAll: (params) => api.get('/v1/alerts', { params }),
  getById: (id) => api.get(`/v1/alerts/${id}`),
  getByDevice: (deviceId, params) => api.get(`/v1/alerts/device/${deviceId}`, { params }),
  resolve: (id, data) => api.post(`/v1/alerts/${id}/resolve`, data),
  delete: (id) => api.delete(`/v1/alerts/${id}`),
  getStats: () => api.get('/v1/alerts/stats'),
};