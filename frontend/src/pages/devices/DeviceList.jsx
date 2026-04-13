import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { devicesAPI } from '../../api/devices';
import { useAuth } from '../../context/AuthContext';
import Header from '../../components/layout/Header';
import './Devices.css';

export default function DeviceList() {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({ status: '', type: '' });
  const [showCreate, setShowCreate] = useState(false);
  const [createForm, setCreateForm] = useState({ name: '', type: 'SENSOR', location: '', firmwareVersion: '' });
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState('');
  const [editDevice, setEditDevice] = useState(null);
const [editForm, setEditForm] = useState({ name: '', type: '', location: '', firmwareVersion: '' });
const [editing, setEditing] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const fetchDevices = async () => {
    try {
      const params = {};
      if (filter.status) params.status = filter.status;
      if (filter.type) params.type = filter.type;
      const res = await devicesAPI.getAll(params);
setDevices(res.data.content || []);    } catch (err) {
      console.error('Failed to load devices:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDevices();
  }, [filter]);

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreating(true);
    setError('');
    try {
      await devicesAPI.create(createForm);
      setShowCreate(false);
      setCreateForm({ name: '', type: 'SENSOR', location: '', firmwareVersion: '' });
      fetchDevices();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create device');
    } finally {
      setCreating(false);
    }
  };
  const openEdit = (device) => {
    setEditDevice(device);
    setEditForm({
      name: device.name,
      type: device.type,
      location: device.location,
      firmwareVersion: device.firmwareVersion || '',
    });
    setError('');
  };

  const handleEdit = async (e) => {
    e.preventDefault();
    setEditing(true);
    setError('');
    try {
      await devicesAPI.update(editDevice.id, editForm);
      setEditDevice(null);
      fetchDevices();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update device');
    } finally {
      setEditing(false);
    }
  };
  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete device "${name}"?`)) return;
    try {
      await devicesAPI.delete(id);
      fetchDevices();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete device');
    }
  };

  const statusColors = {
    ONLINE: 'status-online',
    OFFLINE: 'status-offline',
    MAINTENANCE: 'status-maintenance',
    ERROR: 'status-error',
  };

  const typeIcons = {
    SENSOR: '◈',
    ROUTER: '⬡',
    GATEWAY: '△',
    ACTUATOR: '◇',
    CAMERA: '○',
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner" />
        <span>Loading devices...</span>
      </div>
    );
  }

  return (
    <div className="devices-page">
      <Header title="Devices" subtitle={`${devices.length} registered device(s)`} />

      <div className="devices-toolbar">
        <div className="devices-filters">
          <select
            value={filter.status}
            onChange={(e) => setFilter({ ...filter, status: e.target.value })}
            className="filter-select"
          >
            <option value="">All statuses</option>
            <option value="ONLINE">Online</option>
            <option value="OFFLINE">Offline</option>
            <option value="MAINTENANCE">Maintenance</option>
            <option value="ERROR">Error</option>
          </select>

          <select
            value={filter.type}
            onChange={(e) => setFilter({ ...filter, type: e.target.value })}
            className="filter-select"
          >
            <option value="">All types</option>
            <option value="SENSOR">Sensor</option>
            <option value="ROUTER">Router</option>
            <option value="GATEWAY">Gateway</option>
            <option value="ACTUATOR">Actuator</option>
            <option value="CAMERA">Camera</option>
          </select>
        </div>

        {user?.role === 'ADMIN' && (
          <button className="create-btn" onClick={() => setShowCreate(true)}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Add device
          </button>
        )}
      </div>

      {showCreate && (
        <div className="modal-overlay" onClick={() => setShowCreate(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Register new device</h3>
              <button className="modal-close" onClick={() => setShowCreate(false)}>×</button>
            </div>

            {error && <div className="modal-error">{error}</div>}

            <form onSubmit={handleCreate} className="modal-form">
              <div className="modal-form-group">
                <label>Device name</label>
                <input
                  type="text"
                  value={createForm.name}
                  onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                  placeholder="e.g. Temperature Sensor A1"
                  required
                />
              </div>

              <div className="modal-form-group">
                <label>Type</label>
                <select
                  value={createForm.type}
                  onChange={(e) => setCreateForm({ ...createForm, type: e.target.value })}
                >
                  <option value="SENSOR">Sensor</option>
                  <option value="ROUTER">Router</option>
                  <option value="GATEWAY">Gateway</option>
                  <option value="ACTUATOR">Actuator</option>
                  <option value="CAMERA">Camera</option>
                </select>
              </div>

              <div className="modal-form-group">
                <label>Location</label>
                <input
                  type="text"
                  value={createForm.location}
                  onChange={(e) => setCreateForm({ ...createForm, location: e.target.value })}
                  placeholder="e.g. Server Room - Rack 1"
                  required
                />
              </div>

              <div className="modal-form-group">
                <label>Firmware version</label>
                <input
                  type="text"
                  value={createForm.firmwareVersion}
                  onChange={(e) => setCreateForm({ ...createForm, firmwareVersion: e.target.value })}
                  placeholder="e.g. 2.1.3"
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="modal-cancel" onClick={() => setShowCreate(false)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={creating}>
                  {creating ? 'Creating...' : 'Register device'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      {editDevice && (
        <div className="modal-overlay" onClick={() => setEditDevice(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Edit device</h3>
              <button className="modal-close" onClick={() => setEditDevice(null)}>×</button>
            </div>

            {error && <div className="modal-error">{error}</div>}

            <form onSubmit={handleEdit} className="modal-form">
              <div className="modal-form-group">
                <label>Device name</label>
                <input
                  type="text"
                  value={editForm.name}
                  onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                  required
                />
              </div>

              <div className="modal-form-group">
                <label>Type</label>
                <select
                  value={editForm.type}
                  onChange={(e) => setEditForm({ ...editForm, type: e.target.value })}
                >
                  <option value="SENSOR">Sensor</option>
                  <option value="ROUTER">Router</option>
                  <option value="GATEWAY">Gateway</option>
                  <option value="ACTUATOR">Actuator</option>
                  <option value="CAMERA">Camera</option>
                </select>
              </div>

              <div className="modal-form-group">
                <label>Location</label>
                <input
                  type="text"
                  value={editForm.location}
                  onChange={(e) => setEditForm({ ...editForm, location: e.target.value })}
                  required
                />
              </div>

              <div className="modal-form-group">
                <label>Firmware version</label>
                <input
                  type="text"
                  value={editForm.firmwareVersion}
                  onChange={(e) => setEditForm({ ...editForm, firmwareVersion: e.target.value })}
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="modal-cancel" onClick={() => setEditDevice(null)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={editing}>
                  {editing ? 'Saving...' : 'Save changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="devices-table-wrapper">
        <table className="devices-table">
          <thead>
            <tr>
              <th>Device</th>
              <th>Type</th>
              <th>Status</th>
              <th>Location</th>
              <th>Firmware</th>
              <th>Last seen</th>
              {user?.role === 'ADMIN' && <th></th>}
            </tr>
          </thead>
          <tbody>
            {devices.map((device) => (
              <tr key={device.id} onClick={() => navigate(`/devices/${device.id}`)} className="device-row">
                <td>
                  <div className="device-name-cell">
                    <span className="device-type-icon">{typeIcons[device.type] || '◈'}</span>
                    <span className="device-name">{device.name}</span>
                  </div>
                </td>
                <td><span className="device-type-badge">{device.type}</span></td>
                <td>
                  <span className={`device-status ${statusColors[device.status]}`}>
                    <span className="status-indicator" />
                    {device.status}
                  </span>
                </td>
                <td className="cell-muted">{device.location}</td>
                <td className="cell-mono">{device.firmwareVersion}</td>
                <td className="cell-muted">{device.lastSeenAt ? new Date(device.lastSeenAt).toLocaleString() : '—'}</td>
                {user?.role === 'ADMIN' && (
                  <td>
                    <div className="row-actions">
                      <button
                        className="edit-btn"
                        onClick={(e) => { e.stopPropagation(); openEdit(device); }}
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                      </button>
                      <button
                        className="delete-btn"
                        onClick={(e) => { e.stopPropagation(); handleDelete(device.id, device.name); }}
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                        </svg>
                      </button>
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>

        {devices.length === 0 && (
          <div className="empty-state">No devices found</div>
        )}
      </div>
    </div>
  );
}