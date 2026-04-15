import { useState, useEffect } from 'react';
import { telemetryAPI } from '../../api/telemetry';
import { devicesAPI } from '../../api/devices';
import Header from '../../components/layout/Header';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import './Telemetry.css';
import { systemAPI } from '../../api/system';
export default function TelemetryPage() {
  const [readings, setReadings] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({ deviceId: '', metricType: '' });
  const [chartData, setChartData] = useState([]);

  useEffect(() => {
    const fetchDevices = async () => {
      try {
        const res = await devicesAPI.getAll({ size: 100 });
        setDevices(res.data.content || []);
      } catch (err) {
        console.error('Failed to load devices:', err);
      }
    };
    fetchDevices();
  }, []);

  const fetchReadings = async () => {
    setLoading(true);
    try {
      let res;
      if (filter.deviceId) {
        const params = { size: 100, sortBy: 'timestamp', direction: 'desc' };
        if (filter.metricType) params.metricType = filter.metricType;
        res = await telemetryAPI.getByDevice(filter.deviceId, params);
      } else {
        res = await telemetryAPI.getAll({ size: 100, sortBy: 'timestamp', direction: 'desc' });
      }
      const data = res.data.content || [];
      setReadings(data);
      buildChartData(data);
    } catch (err) {
      console.error('Failed to load telemetry:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReadings();
  }, [filter]);

  const buildChartData = (data) => {
    const sorted = [...data].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    const mapped = sorted.map((r) => ({
      time: new Date(r.timestamp).toLocaleTimeString(),
      value: r.value,
      anomaly: r.anomaly ? r.value : null,
      metric: r.metricType,
    }));
    setChartData(mapped);
  };

  const getDeviceName = (deviceId) => {
    const device = devices.find((d) => d.id === deviceId);
    return device?.name || deviceId?.substring(0, 8) + '...';
  };
  const handleExportTelemetry = async () => {
    try {
      const res = await systemAPI.exportTelemetry();
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a = document.createElement('a');
      a.href = url;
      a.download = 'telemetry_export.csv';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert('Export failed');
    }
  };
  const metricTypes = ['TEMPERATURE', 'CPU_USAGE', 'MEMORY_USAGE', 'NETWORK_TRAFFIC', 'VOLTAGE', 'HUMIDITY', 'DISK_USAGE'];

  const CustomTooltip = ({ active, payload }) => {
    if (!active || !payload?.length) return null;
    const data = payload[0].payload;
    return (
      <div className="chart-tooltip">
        <div className="tooltip-time">{data.time}</div>
        <div className="tooltip-metric">{data.metric?.replace('_', ' ')}</div>
        <div className="tooltip-value">
          {data.value}
          {data.anomaly != null && <span className="tooltip-anomaly">ANOMALY</span>}
        </div>
      </div>
    );
  };

  return (
    <div className="telemetry-page">
      <Header title="Telemetry" subtitle={`${readings.length} reading(s)`} />

<div className="devices-toolbar">
        <div className="devices-filters">
          <select
            value={filter.deviceId}
            onChange={(e) => setFilter({ ...filter, deviceId: e.target.value })}
            className="filter-select"
          >
            <option value="">All devices</option>
            {devices.map((d) => (
              <option key={d.id} value={d.id}>{d.name}</option>
            ))}
          </select>

          <select
            value={filter.metricType}
            onChange={(e) => setFilter({ ...filter, metricType: e.target.value })}
            className="filter-select"
          >
            <option value="">All metrics</option>
            {metricTypes.map((m) => (
              <option key={m} value={m}>{m.replace('_', ' ')}</option>
            ))}
          </select>
        </div>

        <button className="export-btn" onClick={handleExportTelemetry}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          Export CSV
        </button>
      </div>

      {chartData.length > 0 && (
        <div className="telemetry-chart-card">
          <div className="card-header-row">
            <h3>Telemetry chart</h3>
            <span className="card-badge">{chartData.length} points</span>
          </div>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1a1a1e" />
                <XAxis
                  dataKey="time"
                  stroke="#3f3f46"
                  tick={{ fontSize: 11, fill: '#52525b' }}
                  interval="preserveStartEnd"
                />
                <YAxis
                  stroke="#3f3f46"
                  tick={{ fontSize: 11, fill: '#52525b' }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Line
                  type="monotone"
                  dataKey="value"
                  stroke="#10b981"
                  strokeWidth={1.5}
                  dot={false}
                  activeDot={{ r: 4, fill: '#10b981', stroke: '#09090b', strokeWidth: 2 }}
                />
                <Line
                  type="monotone"
                  dataKey="anomaly"
                  stroke="#ef4444"
                  strokeWidth={0}
                  dot={{ r: 5, fill: '#ef4444', stroke: '#09090b', strokeWidth: 2 }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      <div className="telemetry-table-card">
        <div className="card-header-row">
          <h3>Recent readings</h3>
        </div>
        <div className="devices-table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table className="devices-table">
            <thead>
              <tr>
                <th>Device</th>
                <th>Metric</th>
                <th>Value</th>
                <th>Unit</th>
                <th>Status</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {readings.map((r) => (
                <tr key={r.id}>
                  <td className="device-name">{getDeviceName(r.deviceId)}</td>
                  <td>
                    <span className="alert-metric-badge">{r.metricType?.replace('_', ' ')}</span>
                  </td>
                  <td className="cell-mono">{r.value}</td>
                  <td className="cell-muted">{r.unit}</td>
                  <td>
                    {r.anomaly ? (
                      <span className="anomaly-badge">ANOMALY</span>
                    ) : (
                      <span className="normal-badge">Normal</span>
                    )}
                  </td>
                  <td className="cell-muted">{new Date(r.timestamp).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {readings.length === 0 && (
            <div className="empty-state">No telemetry data. Run a simulation first.</div>
          )}
        </div>
      </div>
    </div>
  );
}