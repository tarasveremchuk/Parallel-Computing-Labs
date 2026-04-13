import api from './axios';

export const authAPI = {
  login: (data) => api.post('/v1/auth/login', data),
  register: (data) => api.post('/v1/auth/register', data),
  getMe: () => api.get('/v1/auth/me'),
  updateProfile: (data) => api.put('/v1/auth/me', data),
  changePassword: (data) => api.put('/v1/auth/me/password', data),
  forgotPassword: (data) => api.post('/v1/auth/forgot-password', data),
  resetPassword: (data) => api.post('/v1/auth/reset-password', data),
};