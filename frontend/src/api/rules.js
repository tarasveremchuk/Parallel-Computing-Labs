import api from './axios';

export const rulesAPI = {
  getAll: (params) => api.get('/v1/rules', { params }),
  getById: (id) => api.get(`/v1/rules/${id}`),
  getByDevice: (deviceId) => api.get(`/v1/rules/device/${deviceId}`),
  create: (data) => api.post('/v1/rules', data),
  update: (id, data) => api.put(`/v1/rules/${id}`, data),
  delete: (id) => api.delete(`/v1/rules/${id}`),
};