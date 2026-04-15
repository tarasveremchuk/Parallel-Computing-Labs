import axios from './axios';

export const auditAPI = {
  getAll: (params) => axios.get('/v1/audit', { params }),
};