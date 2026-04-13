import api from './axios';

export const dashboardAPI = {
  get: () => api.get('/v1/dashboard'),
};