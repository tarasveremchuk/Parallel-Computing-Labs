import api from './axios';

export const devicesAPI = {
  getAll: (params) => api.get('/v1/devices', { params }),
  getById: (id) => api.get(`/v1/devices/${id}`),
  create: (data) => api.post('/v1/devices', data),
  update: (id, data) => api.put(`/v1/devices/${id}`, data),
  delete: (id) => api.delete(`/v1/devices/${id}`),
  heartbeat: (id) => api.post(`/v1/devices/${id}/heartbeat`),
  updateStatus: (id, status) => api.post(`/v1/devices/${id}/status`, null, { params: { status } }),
  getStats: (id) => api.get(`/v1/devices/${id}/stats`),
  getAccess: (id) => api.get(`/v1/devices/${id}/access`),
  grantAccess: (id, data) => api.post(`/v1/devices/${id}/access`, data),
  updateAccess: (deviceId, userId, permission) =>
    api.put(`/v1/devices/${deviceId}/access/${userId}`, null, { params: { permission } }),
  revokeAccess: (deviceId, userId) => api.delete(`/v1/devices/${deviceId}/access/${userId}`),
};