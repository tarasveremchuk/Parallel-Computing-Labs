import api from './axios';

export const simulatorAPI = {
  run: (readingsPerDevice = 10) =>
    api.post(`/v1/simulator/run`, null, { params: { readingsPerDevice } }),
};