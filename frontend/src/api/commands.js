import axios from './axios';

export const commandsAPI = {
  send: (data) => axios.post('/v1/commands', data),
  getByDevice: (deviceId, params) => axios.get(`/v1/commands/device/${deviceId}`, { params }),
  getById: (id) => axios.get(`/v1/commands/${id}`),
  acknowledge: (id, data) => axios.post(`/v1/commands/${id}/acknowledge`, data),
};