import axios from './axios';

export const webhooksAPI = {
  getAll: () => axios.get('/v1/webhooks'),
  create: (data) => axios.post('/v1/webhooks', data),
  toggle: (id) => axios.post(`/v1/webhooks/${id}/toggle`),
  delete: (id) => axios.delete(`/v1/webhooks/${id}`),
};