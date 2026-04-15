import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { devicesAPI } from '../../api/devices';
import { alertsAPI } from '../../api/alerts';
import { telemetryAPI } from '../../api/telemetry';
import { commandsAPI } from '../../api/commands';
import { maintenanceAPI } from '../../api/maintenance';
import { useAuth } from '../../context/AuthContext';
import Header from '../../components/layout/Header';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import './Devices.css';
import { accessAPI } from '../../api/access';
import { usersAPI } from '../../api/users';

export default function DeviceDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [device, setDevice] = useState(null);
  const [stats, setStats] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [readings, setReadings] = useState([]);
  const [commands, setCommands] = useState([]);
  const [maintenance, setMaintenance] = useState([]);
  const [accessList, setAccessList] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [showGrantAccess, setShowGrantAccess] = useState(false);
  const [accessForm, setAccessForm] = useState({ userId: '', permission: 'READ' });
  const [grantingAccess, setGrantingAccess] = useState(false);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [selectedMetric, setSelectedMetric] = useState('');

  // Modals
  const [showSendTelemetry, setShowSendTelemetry] = useState(false);
  const [showSendCommand, setShowSendCommand] = useState(false);
  const [showScheduleMaint, setShowScheduleMaint] = useState(false);

  // Forms
  const [telemetryForm, setTelemetryForm] = useState({ metricType: 'TEMPERATURE', value: '', unit: '°C' });
  const [commandForm, setCommandForm] = useState({ commandType: 'RESTART', payload: '' });
  const [maintForm, setMaintForm] = useState({ startTime: '', endTime: '', reason: '' });

  // States
  const [sendingTelemetry, setSendingTelemetry] = useState(false);
  const [sendingCommand, setSendingCommand] = useState(false);
  const [schedulingMaint, setSchedulingMaint] = useState(false);
  const [telemetryResult, setTelemetryResult] = useState(null);

  const metricTypes = ['TEMPERATURE', 'CPU_USAGE', 'MEMORY_USAGE', 'NETWORK_TRAFFIC', 'VOLTAGE', 'HUMIDITY', 'DISK_USAGE'];
  const unitDefaults = { TEMPERATURE: '°C', CPU_USAGE: '%', MEMORY_USAGE: '%', VOLTAGE: 'V', HUMIDITY: '%', NETWORK_TRAFFIC: 'Mbps', DISK_USAGE: '%' };
  const commandTypes = ['RESTART', 'RECALIBRATE', 'UPDATE_FIRMWARE', 'RESET_CONFIG', 'DIAGNOSTIC', 'SHUTDOWN'];
  const severityColor = { CRITICAL: '#ef4444', HIGH: '#f97316', MEDIUM: '#f59e0b', LOW: '#3b82f6' };
  const commandStatusColor = { PENDING: '#f59e0b', SENT: '#3b82f6', ACKNOWLEDGED: '#10b981', FAILED: '#ef4444', EXPIRED: '#71717a' };

  useEffect(() => { fetchData(); }, [id]);
  useEffect(() => { if (activeTab === 'telemetry') fetchReadings(); }, [activeTab, selectedMetric]);
  useEffect(() => { if (activeTab === 'commands') fetchCommands(); }, [activeTab]);
  useEffect(() => { if (activeTab === 'maintenance') fetchMaintenance(); }, [activeTab]);
  useEffect(() => {
    if (activeTab === 'access') { fetchAccess(); fetchUsers(); }
  }, [activeTab]);
  const fetchData = async () => {
    try {
      const [deviceRes, statsRes, alertsRes] = await Promise.all([
        devicesAPI.getById(id),
        devicesAPI.getStats(id).catch(() => ({ data: { data: null } })),
        alertsAPI.getByDevice(id, { size: 10 }),
      ]);
      setDevice(deviceRes.data.data);
      setStats(statsRes.data.data);
      setAlerts(alertsRes.data.content || []);
    } catch (err) {
      console.error('DeviceDetail load error:', err);
      console.error('Response:', err.response?.data);
    } finally {
      setLoading(false);
    }
  };

  const fetchReadings = async () => {
    try {
      const params = { size: 100, sortBy: 'timestamp', direction: 'desc' };
      if (selectedMetric) params.metricType = selectedMetric;
      const res = await telemetryAPI.getByDevice(id, params);
      setReadings(res.data.content || []);
    } catch (err) { console.error(err); }
  };

  const fetchCommands = async () => {
    try {
      const res = await commandsAPI.getByDevice(id, { size: 50 });
      setCommands(res.data.content || []);
    } catch (err) { console.error(err); }
  };

  const fetchMaintenance = async () => {
    try {
      const res = await maintenanceAPI.getByDevice(id, { size: 50 });
      setMaintenance(res.data.content || []);
    } catch (err) { console.error(err); }
  };
  const fetchAccess = async () => {
    try {
      const res = await accessAPI.getByDevice(id);
      setAccessList(res.data.data || []);
    } catch (err) { console.error(err); }
  };

  const fetchUsers = async () => {
    try {
      const res = await usersAPI.getAll({ size: 100 });
      setAllUsers(res.data.content || []);
    } catch (err) { console.error(err); }
  };

  const handleGrantAccess = async (e) => {
    e.preventDefault();
    setGrantingAccess(true);
    try {
      await accessAPI.grant(id, { userId: accessForm.userId, permission: accessForm.permission });
      setShowGrantAccess(false);
      setAccessForm({ userId: '', permission: 'READ' });
      fetchAccess();
    } catch (err) { alert(err.response?.data?.message || 'Failed'); }
    finally { setGrantingAccess(false); }
  };

  const handleUpdatePermission = async (userId, permission) => {
    try {
      await accessAPI.update(id, userId, { permission });
      fetchAccess();
    } catch (err) { alert('Failed'); }
  };

  const handleRevokeAccess = async (userId) => {
    if (!window.confirm('Revoke access?')) return;
    try {
      await accessAPI.revoke(id, userId);
      fetchAccess();
    } catch (err) { alert('Failed'); }
  };

  const getUserName = (userId) => {
    const u = allUsers.find((user) => user.id === userId);
    return u?.username || userId.substring(0, 8) + '...';
  };
  const handleHeartbeat = async () => {
    try { const res = await devicesAPI.heartbeat(id); setDevice(res.data.data); } catch { alert('Failed'); }
  };

  const handleSendTelemetry = async (e) => {
    e.preventDefault();
    setSendingTelemetry(true); setTelemetryResult(null);
    try {
      const res = await telemetryAPI.ingest({ deviceId: id, metricType: telemetryForm.metricType, value: parseFloat(telemetryForm.value), unit: telemetryForm.unit || null });
      const d = res.data.data;
      setTelemetryResult({ success: true, anomaly: d.anomaly, message: d.anomaly ? 'Anomaly detected! Alert created.' : 'Reading ingested successfully.' });
      setTelemetryForm({ ...telemetryForm, value: '' });
      fetchData(); if (activeTab === 'telemetry') fetchReadings();
    } catch (err) {
      setTelemetryResult({ success: false, message: err.response?.data?.message || 'Failed' });
    } finally { setSendingTelemetry(false); }
  };

  const handleSendCommand = async (e) => {
    e.preventDefault();
    setSendingCommand(true);
    try {
      await commandsAPI.send({ deviceId: id, commandType: commandForm.commandType, payload: commandForm.payload || null });
      setShowSendCommand(false); setCommandForm({ commandType: 'RESTART', payload: '' });
      fetchCommands();
    } catch (err) { alert(err.response?.data?.message || 'Failed'); }
    finally { setSendingCommand(false); }
  };

  const handleScheduleMaint = async (e) => {
    e.preventDefault();
    setSchedulingMaint(true);
    try {
      await maintenanceAPI.schedule({ deviceId: id, startTime: maintForm.startTime, endTime: maintForm.endTime, reason: maintForm.reason || null });
      setShowScheduleMaint(false); setMaintForm({ startTime: '', endTime: '', reason: '' });
      fetchMaintenance();
    } catch (err) { alert(err.response?.data?.message || 'Failed'); }
    finally { setSchedulingMaint(false); }
  };

  const handleCancelMaint = async (maintId) => {
    if (!window.confirm('Cancel this maintenance window?')) return;
    try { await maintenanceAPI.cancel(maintId); fetchMaintenance(); } catch { alert('Failed'); }
  };

  const handleAcknowledge = async (cmdId) => {
    try { await commandsAPI.acknowledge(cmdId, { response: 'Acknowledged via UI' }); fetchCommands(); } catch { alert('Failed'); }
  };

  const handleResolve = async (alertId) => {
    try { await alertsAPI.resolve(alertId, { note: 'Resolved from device detail' }); fetchData(); } catch { alert('Failed'); }
  };

  const buildChartData = () => [...readings].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp)).map((r) => ({
    time: new Date(r.timestamp).toLocaleTimeString(), value: r.value, anomaly: r.anomaly ? r.value : null,
  }));

  const CustomTooltip = ({ active, payload }) => {
    if (!active || !payload?.length) return null;
    const d = payload[0].payload;
    return (<div className="chart-tooltip"><div className="tooltip-time">{d.time}</div>
      <div className="tooltip-value">{d.value}{d.anomaly != null && <span className="tooltip-anomaly">ANOMALY</span>}</div></div>);
  };

  if (loading) return <div className="dashboard-loading"><div className="loading-spinner" /><span>Loading...</span></div>;
  if (!device) return null;

  const tabs = [
    { id: 'overview', label: 'Overview' },
    { id: 'telemetry', label: 'Telemetry' },
    { id: 'alerts', label: `Alerts (${alerts.length})` },
    { id: 'commands', label: 'Commands' },
    { id: 'maintenance', label: 'Maintenance' },
    { id: 'access', label: 'Access' },

  ];

  return (
    <div className="device-detail-page">
      <button className="back-btn" onClick={() => navigate('/devices')}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        Back to devices
      </button>
      <Header title={device.name} subtitle={`${device.type} · ${device.location}`} />

      <div className="detail-tabs">
        {tabs.map((tab) => (
          <button key={tab.id} className={`detail-tab ${activeTab === tab.id ? 'active' : ''}`} onClick={() => setActiveTab(tab.id)}>{tab.label}</button>
        ))}
      </div>

      {/* === OVERVIEW === */}
      {activeTab === 'overview' && (
        <>
          <div className="detail-info-row">
            <div className="detail-info-card"><span className="detail-info-label">Status</span>
              <span className={`device-status status-${device.status.toLowerCase()}`}><span className="status-indicator" />{device.status}</span></div>
            <div className="detail-info-card"><span className="detail-info-label">Firmware</span><span className="detail-info-value mono">{device.firmwareVersion}</span></div>
            <div className="detail-info-card"><span className="detail-info-label">Health</span>
              <span className={`detail-info-value ${(stats?.healthScore ?? 100) >= 80 ? 'text-green' : (stats?.healthScore ?? 100) >= 50 ? 'text-yellow' : 'text-red'}`}>{stats?.healthScore ?? '—'}%</span></div>
            <div className="detail-info-card"><span className="detail-info-label">Readings</span><span className="detail-info-value">{stats?.totalReadings ?? 0}</span></div>
            <div className="detail-info-card"><span className="detail-info-label">Anomalies</span><span className="detail-info-value text-red">{stats?.anomalyCount ?? 0}</span></div>
          </div>
          <div className="detail-actions">
            <button className="action-btn" onClick={handleHeartbeat}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              Heartbeat</button>
            <button className="action-btn action-btn-primary" onClick={() => setShowSendTelemetry(true)}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
              Send telemetry</button>
          </div>
          {stats?.metrics && Object.keys(stats.metrics).length > 0 && (
            <div className="detail-section"><h3 className="section-title">Metric statistics</h3>
              <div className="metrics-grid">
                {Object.entries(stats.metrics).map(([metric, data]) => (
                  <div key={metric} className="metric-stat-card">
                    <div className="metric-stat-header"><span className="metric-stat-name">{metric.replace('_', ' ')}</span><span className="metric-stat-count">{data.readingCount}</span></div>
                    <div className="metric-stat-values">
                      <div className="metric-stat-item"><span className="metric-stat-label">Avg</span><span className="metric-stat-val">{data.avg ?? '—'}</span></div>
                      <div className="metric-stat-item"><span className="metric-stat-label">Min</span><span className="metric-stat-val">{data.min ?? '—'}</span></div>
                      <div className="metric-stat-item"><span className="metric-stat-label">Max</span><span className="metric-stat-val">{data.max ?? '—'}</span></div>
                      <div className="metric-stat-item"><span className="metric-stat-label">Last</span><span className="metric-stat-val highlight">{data.lastValue ?? '—'}</span></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {/* === TELEMETRY === */}
      {activeTab === 'telemetry' && (
        <>
          <div className="devices-toolbar" style={{ marginBottom: '1rem' }}>
            <div className="devices-filters">
              <select className="filter-select" value={selectedMetric} onChange={(e) => setSelectedMetric(e.target.value)}>
                <option value="">All metrics</option>
                {metricTypes.map((m) => <option key={m} value={m}>{m.replace('_', ' ')}</option>)}
              </select>
            </div>
            <button className="action-btn action-btn-primary" onClick={() => setShowSendTelemetry(true)}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              Send reading</button>
          </div>
          {readings.length > 0 && (
            <div className="telemetry-chart-card">
              <div className="card-header-row"><h3>{selectedMetric ? selectedMetric.replace('_', ' ') : 'All metrics'}</h3><span className="card-badge">{readings.length} points</span></div>
              <div className="chart-container">
                <ResponsiveContainer width="100%" height={280}>
                  <LineChart data={buildChartData()}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1a1a1e" />
                    <XAxis dataKey="time" stroke="#3f3f46" tick={{ fontSize: 11, fill: '#52525b' }} interval="preserveStartEnd" />
                    <YAxis stroke="#3f3f46" tick={{ fontSize: 11, fill: '#52525b' }} />
                    <Tooltip content={<CustomTooltip />} />
                    <Line type="monotone" dataKey="value" stroke="#10b981" strokeWidth={1.5} dot={false} activeDot={{ r: 4, fill: '#10b981', stroke: '#09090b', strokeWidth: 2 }} />
                    <Line type="monotone" dataKey="anomaly" stroke="#ef4444" strokeWidth={0} dot={{ r: 5, fill: '#ef4444', stroke: '#09090b', strokeWidth: 2 }} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
          <div className="devices-table-wrapper">
            <table className="devices-table"><thead><tr><th>Metric</th><th>Value</th><th>Unit</th><th>Status</th><th>Timestamp</th></tr></thead>
              <tbody>{readings.map((r) => (
                <tr key={r.id}><td><span className="alert-metric-badge">{r.metricType?.replace('_', ' ')}</span></td>
                  <td className="cell-mono">{r.value}</td><td className="cell-muted">{r.unit}</td>
                  <td>{r.anomaly ? <span className="anomaly-badge">ANOMALY</span> : <span className="normal-badge">Normal</span>}</td>
                  <td className="cell-muted">{new Date(r.timestamp).toLocaleString()}</td></tr>
              ))}</tbody></table>
            {readings.length === 0 && <div className="empty-state">No readings yet</div>}
          </div>
        </>
      )}

      {/* === ALERTS === */}
      {activeTab === 'alerts' && (
        <div className="alerts-list">
          {alerts.map((alert) => (
            <div key={alert.id} className={`alert-card ${alert.resolved ? 'resolved' : ''}`}>
              <div className="alert-severity-bar" style={{ background: severityColor[alert.severity] }} />
              <div className="alert-content">
                <div className="alert-top-row">
                  <div className="alert-badges">
                    <span className="alert-severity-badge" style={{ background: `${severityColor[alert.severity]}14`, color: severityColor[alert.severity], borderColor: `${severityColor[alert.severity]}26` }}>{alert.severity}</span>
                    <span className="alert-metric-badge">{alert.metricType?.replace('_', ' ')}</span>
                  </div>
                  <span className="alert-time">{new Date(alert.createdAt).toLocaleString()}</span>
                </div>
                <p className="alert-message">{alert.message}</p>
                <div className="alert-details">
                  <span className="alert-detail">Value: <strong>{alert.actualValue}</strong></span>
                  {alert.thresholdMin != null && <span className="alert-detail">Min: {alert.thresholdMin}</span>}
                  {alert.thresholdMax != null && <span className="alert-detail">Max: {alert.thresholdMax}</span>}
                </div>
                <div className="alert-bottom-row">
                  <span className={`alert-status-badge ${alert.resolved ? 'resolved' : 'open'}`}>{alert.resolved ? 'Resolved' : 'Open'}</span>
                  {!alert.resolved && <button className="alert-resolve-btn" onClick={() => handleResolve(alert.id)}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="20 6 9 17 4 12"/></svg>Resolve</button>}
                </div>
              </div>
            </div>
          ))}
          {alerts.length === 0 && <div className="empty-state">No alerts for this device</div>}
        </div>
      )}

      {/* === COMMANDS === */}
      {activeTab === 'commands' && (
        <>
          <div className="devices-toolbar" style={{ marginBottom: '1rem' }}>
            <div />
            {(user?.role === 'ADMIN' || user?.role === 'OPERATOR') && (
              <button className="action-btn action-btn-primary" onClick={() => setShowSendCommand(true)}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                Send command</button>
            )}
          </div>
          <div className="commands-list">
            {commands.map((cmd) => (
              <div key={cmd.id} className="command-card">
                <div className="command-left">
                  <div className="command-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                  </div>
                  <div className="command-info">
                    <div className="command-type">{cmd.commandType}</div>
                    <div className="command-meta">
                      {new Date(cmd.createdAt).toLocaleString()}
                      {cmd.payload && <span> · {cmd.payload}</span>}
                    </div>
                    {cmd.response && <div className="command-response">Response: {cmd.response}</div>}
                  </div>
                </div>
                <div className="command-right">
                  <span className="command-status-badge" style={{ background: `${commandStatusColor[cmd.status]}14`, color: commandStatusColor[cmd.status], borderColor: `${commandStatusColor[cmd.status]}26` }}>
                    {cmd.status}
                  </span>
                  {cmd.status === 'PENDING' && (
                    <button className="action-btn-sm" onClick={() => handleAcknowledge(cmd.id)} title="Simulate acknowledgement">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="20 6 9 17 4 12"/></svg>
                    </button>
                  )}
                </div>
              </div>
            ))}
            {commands.length === 0 && <div className="empty-state">No commands sent to this device</div>}
          </div>
        </>
      )}

      {/* === MAINTENANCE === */}
      {activeTab === 'maintenance' && (
        <>
          <div className="devices-toolbar" style={{ marginBottom: '1rem' }}>
            <div />
            {user?.role === 'ADMIN' && (
              <button className="action-btn action-btn-primary" onClick={() => setShowScheduleMaint(true)}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                Schedule maintenance</button>
            )}
          </div>
          <div className="maintenance-list">
            {maintenance.map((m) => {
              const now = new Date();
              const start = new Date(m.startTime);
              const end = new Date(m.endTime);
              const isActive = !m.cancelled && now >= start && now <= end;
              const isUpcoming = !m.cancelled && now < start;
              const isPast = !m.cancelled && now > end;
              return (
                <div key={m.id} className={`maintenance-card ${m.cancelled ? 'cancelled' : isActive ? 'active' : ''}`}>
                  <div className="maintenance-left">
                    <div className={`maintenance-indicator ${isActive ? 'active' : isUpcoming ? 'upcoming' : isPast ? 'past' : 'cancelled'}`} />
                    <div className="maintenance-info">
                      <div className="maintenance-time">
                        {start.toLocaleString()} — {end.toLocaleString()}
                      </div>
                      <div className="maintenance-reason">{m.reason || 'No reason specified'}</div>
                      <div className="maintenance-status-text">
                        {m.cancelled ? 'Cancelled' : isActive ? 'Active now' : isUpcoming ? 'Upcoming' : 'Completed'}
                      </div>
                    </div>
                  </div>
                  {!m.cancelled && (isActive || isUpcoming) && user?.role === 'ADMIN' && (
                    <button className="delete-btn" onClick={() => handleCancelMaint(m.id)} title="Cancel">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    </button>
                  )}
                </div>
              );
            })}
            {maintenance.length === 0 && <div className="empty-state">No maintenance windows scheduled</div>}
          </div>
        </>
      )}
      {/* === ACCESS === */}
      {activeTab === 'access' && (
        <>
          <div className="devices-toolbar" style={{ marginBottom: '1rem' }}>
            <div className="access-owner">
              <span className="access-owner-label">Owner:</span>
              <span className="access-owner-name">{getUserName(device.ownerId)}</span>
            </div>
            {user?.role === 'ADMIN' && (
              <button className="action-btn action-btn-primary" onClick={() => setShowGrantAccess(true)}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="8.5" cy="7" r="4"/>
                  <line x1="20" y1="8" x2="20" y2="14"/>
                  <line x1="23" y1="11" x2="17" y2="11"/>
                </svg>
                Grant access
              </button>
            )}
          </div>

          <div className="access-list">
            {accessList.map((acc) => (
              <div key={acc.id} className="access-card">
                <div className="access-left">
                  <div className="access-avatar">{getUserName(acc.userId).charAt(0).toUpperCase()}</div>
                  <div className="access-info">
                    <span className="access-username">{getUserName(acc.userId)}</span>
                    <span className="access-granted">Granted {new Date(acc.grantedAt).toLocaleDateString()}</span>
                  </div>
                </div>
                <div className="access-right">
                  <select
                    className="access-permission-select"
                    value={acc.permission}
                    onChange={(e) => handleUpdatePermission(acc.userId, e.target.value)}
                    disabled={user?.role !== 'ADMIN'}
                  >
                    <option value="READ">READ</option>
                    <option value="OPERATE">OPERATE</option>
                    <option value="MANAGE">MANAGE</option>
                  </select>
                  {user?.role === 'ADMIN' && (
                    <button className="delete-btn" onClick={() => handleRevokeAccess(acc.userId)}>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                        <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                  )}
                </div>
              </div>
            ))}
            {accessList.length === 0 && <div className="empty-state">No shared access. Only the owner has access to this device.</div>}
          </div>

          {showGrantAccess && (
            <div className="modal-overlay" onClick={() => setShowGrantAccess(false)}>
              <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header"><h3>Grant access</h3><button className="modal-close" onClick={() => setShowGrantAccess(false)}>×</button></div>
                <form onSubmit={handleGrantAccess} className="modal-form">
                  <div className="modal-form-group"><label>User</label>
                    <select value={accessForm.userId} onChange={(e) => setAccessForm({ ...accessForm, userId: e.target.value })} required>
                      <option value="" disabled>Select user...</option>
                      {allUsers.filter(u => u.id !== device.ownerId && !accessList.find(a => a.userId === u.id))
                        .map((u) => <option key={u.id} value={u.id}>{u.username} ({u.role})</option>)}
                    </select>
                  </div>
                  <div className="modal-form-group"><label>Permission level</label>
                    <div className="permission-selector">
                      {['READ', 'OPERATE', 'MANAGE'].map((perm) => (
                        <button type="button" key={perm}
                          className={`permission-option ${accessForm.permission === perm ? 'selected' : ''}`}
                          onClick={() => setAccessForm({ ...accessForm, permission: perm })}>
                          <span className="permission-name">{perm}</span>
                          <span className="permission-desc">
                            {perm === 'READ' && 'View device data and telemetry'}
                            {perm === 'OPERATE' && 'Send telemetry, resolve alerts'}
                            {perm === 'MANAGE' && 'Full access, can share with others'}
                          </span>
                        </button>
                      ))}
                    </div>
                  </div>
                  <div className="modal-actions"><button type="button" className="modal-cancel" onClick={() => setShowGrantAccess(false)}>Cancel</button>
                    <button type="submit" className="modal-submit" disabled={grantingAccess || !accessForm.userId}>
                      {grantingAccess ? 'Granting...' : 'Grant access'}</button></div>
                </form>
              </div>
            </div>
          )}
        </>
      )}
      {/* === SEND TELEMETRY MODAL === */}
      {showSendTelemetry && (
        <div className="modal-overlay" onClick={() => { setShowSendTelemetry(false); setTelemetryResult(null); }}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header"><h3>Send telemetry</h3><button className="modal-close" onClick={() => { setShowSendTelemetry(false); setTelemetryResult(null); }}>×</button></div>
            {telemetryResult && (
              <div className={`telemetry-result ${telemetryResult.success ? (telemetryResult.anomaly ? 'anomaly' : 'success') : 'error'}`}>
                <span className="telemetry-result-icon">{telemetryResult.anomaly ? '⚠' : telemetryResult.success ? '✓' : '✗'}</span>
                {telemetryResult.message}
              </div>
            )}
            <form onSubmit={handleSendTelemetry} className="modal-form">
              <div className="modal-form-group"><label>Metric</label>
                <select value={telemetryForm.metricType} onChange={(e) => setTelemetryForm({ ...telemetryForm, metricType: e.target.value, unit: unitDefaults[e.target.value] || '' })}>
                  {metricTypes.map((m) => <option key={m} value={m}>{m.replace('_', ' ')}</option>)}</select></div>
              <div className="form-row-2">
                <div className="modal-form-group"><label>Value</label><input type="number" step="any" value={telemetryForm.value} onChange={(e) => setTelemetryForm({ ...telemetryForm, value: e.target.value })} placeholder="42.5" required /></div>
                <div className="modal-form-group"><label>Unit</label><input type="text" value={telemetryForm.unit} onChange={(e) => setTelemetryForm({ ...telemetryForm, unit: e.target.value })} /></div>
              </div>
              <div className="modal-actions"><button type="button" className="modal-cancel" onClick={() => setShowSendTelemetry(false)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={sendingTelemetry}>{sendingTelemetry ? 'Sending...' : 'Send'}</button></div>
            </form>
          </div>
        </div>
      )}

      {/* === SEND COMMAND MODAL === */}
      {showSendCommand && (
        <div className="modal-overlay" onClick={() => setShowSendCommand(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header"><h3>Send command</h3><button className="modal-close" onClick={() => setShowSendCommand(false)}>×</button></div>
            <form onSubmit={handleSendCommand} className="modal-form">
              <div className="modal-form-group"><label>Command type</label>
                <select value={commandForm.commandType} onChange={(e) => setCommandForm({ ...commandForm, commandType: e.target.value })}>
                  {commandTypes.map((c) => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}</select></div>
              <div className="modal-form-group"><label>Payload (optional)</label>
                <input type="text" value={commandForm.payload} onChange={(e) => setCommandForm({ ...commandForm, payload: e.target.value })} placeholder="e.g. firmware v2.2.0" /></div>
              <div className="modal-actions"><button type="button" className="modal-cancel" onClick={() => setShowSendCommand(false)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={sendingCommand}>{sendingCommand ? 'Sending...' : 'Send command'}</button></div>
            </form>
          </div>
        </div>
      )}

      {/* === SCHEDULE MAINTENANCE MODAL === */}
      {showScheduleMaint && (
        <div className="modal-overlay" onClick={() => setShowScheduleMaint(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header"><h3>Schedule maintenance</h3><button className="modal-close" onClick={() => setShowScheduleMaint(false)}>×</button></div>
            <form onSubmit={handleScheduleMaint} className="modal-form">
              <div className="form-row-2">
                <div className="modal-form-group"><label>Start time</label><input type="datetime-local" value={maintForm.startTime} onChange={(e) => setMaintForm({ ...maintForm, startTime: e.target.value })} required /></div>
                <div className="modal-form-group"><label>End time</label><input type="datetime-local" value={maintForm.endTime} onChange={(e) => setMaintForm({ ...maintForm, endTime: e.target.value })} required /></div>
              </div>
              <div className="modal-form-group"><label>Reason (optional)</label>
                <input type="text" value={maintForm.reason} onChange={(e) => setMaintForm({ ...maintForm, reason: e.target.value })} placeholder="e.g. Firmware update" /></div>
              <div className="modal-actions"><button type="button" className="modal-cancel" onClick={() => setShowScheduleMaint(false)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={schedulingMaint}>{schedulingMaint ? 'Scheduling...' : 'Schedule'}</button></div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}