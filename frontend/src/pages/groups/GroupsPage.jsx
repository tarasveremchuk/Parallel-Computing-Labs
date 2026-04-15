import { useState, useEffect } from 'react';
import { groupsAPI } from '../../api/groups';
import { devicesAPI } from '../../api/devices';
import { useAuth } from '../../context/AuthContext';
import Header from '../../components/layout/Header';
import './Groups.css';
import { commandsAPI } from '../../api/commands';

export default function GroupsPage() {
  const [groups, setGroups] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [createForm, setCreateForm] = useState({ name: '', description: '', color: '#10b981' });
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState('');
  const [expandedGroup, setExpandedGroup] = useState(null);
  const [groupDevices, setGroupDevices] = useState({});
  const [showAddDevice, setShowAddDevice] = useState(null);
  const [sendingGroupCmd, setSendingGroupCmd] = useState(null);
  const { user } = useAuth();

  const colors = ['#10b981', '#3b82f6', '#f59e0b', '#ef4444', '#a855f7', '#ec4899', '#14b8a6', '#f97316'];

  useEffect(() => {
    fetchGroups();
    fetchDevices();
  }, []);

  const fetchGroups = async () => {
    try {
      const res = await groupsAPI.getAll();
      setGroups(res.data.data || []);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };
  const handleGroupCommand = async (groupId, commandType) => {
    const devIds = groupDevices[groupId] || [];
    if (devIds.length === 0) { alert('No devices in group'); return; }
    if (!window.confirm(`Send ${commandType} to ${devIds.length} device(s)?`)) return;

    setSendingGroupCmd(groupId);
    let success = 0, failed = 0;
    for (const deviceId of devIds) {
      try {
        await commandsAPI.send({ deviceId, commandType, payload: `Group command: ${commandType}` });
        success++;
      } catch { failed++; }
    }
    setSendingGroupCmd(null);
    alert(`Command sent: ${success} success, ${failed} failed`);
  };
  const fetchDevices = async () => {
    try {
      const res = await devicesAPI.getAll({ size: 100 });
      setDevices(res.data.content || []);
    } catch (err) { console.error(err); }
  };

  const fetchGroupDevices = async (groupId) => {
    try {
      const res = await groupsAPI.getById(groupId);
      const deviceIds = res.data.data?.deviceIds || [];
      setGroupDevices((prev) => ({ ...prev, [groupId]: deviceIds }));
    } catch (err) { console.error(err); }
  };

  const handleExpand = async (groupId) => {
    if (expandedGroup === groupId) {
      setExpandedGroup(null);
      return;
    }
    setExpandedGroup(groupId);
    await fetchGroupDevices(groupId);
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreating(true); setError('');
    try {
      await groupsAPI.create(createForm);
      setShowCreate(false);
      setCreateForm({ name: '', description: '', color: '#10b981' });
      fetchGroups();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed');
    } finally { setCreating(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this group?')) return;
    try { await groupsAPI.delete(id); fetchGroups(); }
    catch (err) { alert('Failed'); }
  };

  const handleAddDevice = async (groupId, deviceId) => {
    try {
      await groupsAPI.addDevice(groupId, deviceId);
      fetchGroupDevices(groupId);
      setShowAddDevice(null);
    } catch (err) { alert(err.response?.data?.message || 'Failed'); }
  };

  const handleRemoveDevice = async (groupId, deviceId) => {
    try {
      await groupsAPI.removeDevice(groupId, deviceId);
      fetchGroupDevices(groupId);
    } catch (err) { alert('Failed'); }
  };

  const getDeviceName = (deviceId) => {
    const d = devices.find((dev) => dev.id === deviceId);
    return d?.name || deviceId.substring(0, 8) + '...';
  };

  const getAvailableDevices = (groupId) => {
    const inGroup = groupDevices[groupId] || [];
    return devices.filter((d) => !inGroup.includes(d.id));
  };

  if (loading) return <div className="dashboard-loading"><div className="loading-spinner" /><span>Loading groups...</span></div>;

  return (
    <div className="groups-page">
      <Header title="Device Groups" subtitle={`${groups.length} group(s)`} />

      {user?.role === 'ADMIN' && (
        <div className="devices-toolbar">
          <div />
          <button className="create-btn" onClick={() => setShowCreate(true)}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Add group
          </button>
        </div>
      )}

      {showCreate && (
        <div className="modal-overlay" onClick={() => setShowCreate(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header"><h3>Create group</h3><button className="modal-close" onClick={() => setShowCreate(false)}>×</button></div>
            {error && <div className="modal-error">{error}</div>}
            <form onSubmit={handleCreate} className="modal-form">
              <div className="modal-form-group"><label>Name</label>
                <input type="text" value={createForm.name} onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })} placeholder="e.g. Server Room" required /></div>
              <div className="modal-form-group"><label>Description</label>
                <input type="text" value={createForm.description} onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })} placeholder="Optional description" /></div>
              <div className="modal-form-group"><label>Color</label>
                <div className="color-picker">{colors.map((c) => (
                  <button type="button" key={c} className={`color-dot ${createForm.color === c ? 'selected' : ''}`}
                    style={{ background: c }} onClick={() => setCreateForm({ ...createForm, color: c })} />
                ))}</div></div>
              <div className="modal-actions"><button type="button" className="modal-cancel" onClick={() => setShowCreate(false)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={creating}>{creating ? 'Creating...' : 'Create group'}</button></div>
            </form>
          </div>
        </div>
      )}

      <div className="groups-list">
        {groups.map((group) => {
          const isExpanded = expandedGroup === group.id;
          const devIds = groupDevices[group.id] || [];
          return (
            <div key={group.id} className="group-card">
              <div className="group-header" onClick={() => handleExpand(group.id)}>
                <div className="group-left">
                  <div className="group-color" style={{ background: group.color || '#10b981' }} />
                  <div className="group-info">
                    <span className="group-name">{group.name}</span>
                    <span className="group-desc">{group.description || 'No description'}</span>
                  </div>
                </div>
                <div className="group-right">
                  <span className="group-count">
                    {isExpanded ? devIds.length : '...'} devices
                    {isExpanded && devIds.length > 0 && (() => {
                      const groupDevs = devIds.map(id => devices.find(d => d.id === id)).filter(Boolean);
                      const online = groupDevs.filter(d => d.status === 'ONLINE').length;
                      return <span className="group-online-count"> · {online} online</span>;
                    })()}
                  </span>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#52525b" strokeWidth="2"
                    style={{ transform: isExpanded ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }}>
                    <path d="M6 9l6 6 6-6"/></svg>
                  {user?.role === 'ADMIN' && (
                    <button className="delete-btn" onClick={(e) => { e.stopPropagation(); handleDelete(group.id); }}>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                        <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                    </button>
                  )}
                </div>
              </div>
              {isExpanded && (
                <div className="group-devices">
                  {devIds.map((devId) => (
                    <div key={devId} className="group-device-item">
                      <span className="group-device-name">{getDeviceName(devId)}</span>
                      {user?.role === 'ADMIN' && (
                        <button className="group-remove-btn" onClick={() => handleRemoveDevice(group.id, devId)}>Remove</button>
                      )}
                    </div>
                  ))}
                  {devIds.length > 0 && (user?.role === 'ADMIN' || user?.role === 'OPERATOR') && (
                    <div className="group-actions-bar">
                      <span className="group-actions-label">Group actions:</span>
                      {['RESTART', 'DIAGNOSTIC', 'RECALIBRATE'].map((cmd) => (
                        <button key={cmd} className="group-cmd-btn"
                          disabled={sendingGroupCmd === group.id}
                          onClick={() => handleGroupCommand(group.id, cmd)}>
                          {cmd.toLowerCase()}
                        </button>
                      ))}
                    </div>
                  )}
                  {devIds.length === 0 && <div className="group-empty">No devices in this group</div>}
                  {user?.role === 'ADMIN' && (
                    showAddDevice === group.id ? (
                      <div className="group-add-form">
                        <select className="filter-select" onChange={(e) => { if (e.target.value) handleAddDevice(group.id, e.target.value); }}
                          defaultValue="">
                          <option value="" disabled>Select device to add...</option>
                          {getAvailableDevices(group.id).map((d) => (
                            <option key={d.id} value={d.id}>{d.name}</option>
                          ))}
                        </select>
                        <button className="group-cancel-add" onClick={() => setShowAddDevice(null)}>Cancel</button>
                      </div>
                    ) : (
                      <button className="group-add-btn" onClick={() => setShowAddDevice(group.id)}>
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                        Add device
                      </button>
                    )
                  )}
                </div>
              )}
            </div>
          );
        })}
        {groups.length === 0 && <div className="empty-state">No device groups created yet</div>}
      </div>
    </div>
  );
}