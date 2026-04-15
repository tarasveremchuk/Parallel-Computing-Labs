import axios from './axios';

export const accessAPI = {
  getByDevice: (deviceId) => axios.get(`/v1/devices/${deviceId}/access`),
  grant: (deviceId, data) => axios.post(`/v1/devices/${deviceId}/access`, data),
  update: (deviceId, userId, data) => axios.put(`/v1/devices/${deviceId}/access/${userId}`, data),
  revoke: (deviceId, userId) => axios.delete(`/v1/devices/${deviceId}/access/${userId}`),
};