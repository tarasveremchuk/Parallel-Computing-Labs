import axios from './axios';

export const groupsAPI = {
  getAll: () => axios.get('/v1/groups'),
  getById: (id) => axios.get(`/v1/groups/${id}`),
  create: (data) => axios.post('/v1/groups', data),
  update: (id, data) => axios.put(`/v1/groups/${id}`, data),
  delete: (id) => axios.delete(`/v1/groups/${id}`),
  addDevice: (groupId, deviceId) => axios.post(`/v1/groups/${groupId}/devices/${deviceId}`),
  removeDevice: (groupId, deviceId) => axios.delete(`/v1/groups/${groupId}/devices/${deviceId}`),
  getDevices: (groupId) => axios.get(`/v1/groups/${groupId}/devices`),
};