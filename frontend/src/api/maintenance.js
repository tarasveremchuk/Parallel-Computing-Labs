import axios from './axios';

export const maintenanceAPI = {
  getAll: (params) => axios.get('/v1/maintenance', { params }),
  getByDevice: (deviceId, params) => axios.get(`/v1/maintenance/device/${deviceId}`, { params }),
  getActive: () => axios.get('/v1/maintenance/active'),
  schedule: (data) => axios.post('/v1/maintenance', data),
  cancel: (id) => axios.post(`/v1/maintenance/${id}/cancel`),
};