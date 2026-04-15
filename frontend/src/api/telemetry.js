import api from './axios';

export const telemetryAPI = {
  getAll: (params) => api.get('/v1/telemetry', { params }),
  getById: (id) => api.get(`/v1/telemetry/${id}`),
  ingest: (data) => api.post('/v1/telemetry', data),
  ingestBatch: (data) => api.post('/v1/telemetry/batch', data),
  getByDevice: (deviceId, params) => api.get(`/v1/telemetry/device/${deviceId}`, { params }),
  getAnomalies: (params) => api.get('/v1/telemetry/anomalies', { params }),
  delete: (id) => api.delete(`/v1/telemetry/${id}`),
};
