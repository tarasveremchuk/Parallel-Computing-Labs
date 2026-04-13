import api from './axios';

export const usersAPI = {
  getAll: (params) => api.get('/v1/users', { params }),
  getById: (id) => api.get(`/v1/users/${id}`),
  changeRole: (id, data) => api.put(`/v1/users/${id}/role`, data),
  deactivate: (id) => api.delete(`/v1/users/${id}`),
  resetPassword: (id, data) => api.put(`/v1/users/${id}/reset-password`, data),
};