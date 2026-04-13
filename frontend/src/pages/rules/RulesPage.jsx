import { useState, useEffect } from 'react';
import { rulesAPI } from '../../api/rules';
import { useAuth } from '../../context/AuthContext';
import Header from '../../components/layout/Header';
import './Rules.css';

export default function RulesPage() {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [createForm, setCreateForm] = useState({
    metricType: 'TEMPERATURE', minValue: '', maxValue: '', description: ''
  });
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState('');
  const [editRule, setEditRule] = useState(null);
  const [editForm2, setEditForm2] = useState({ metricType: '', minValue: '', maxValue: '', description: '' });
  const [editing, setEditing] = useState(false);
  const { user } = useAuth();

  const metricTypes = ['TEMPERATURE', 'CPU_USAGE', 'MEMORY_USAGE', 'NETWORK_TRAFFIC', 'VOLTAGE', 'HUMIDITY', 'DISK_USAGE'];

  const fetchRules = async () => {
    try {
      const res = await rulesAPI.getAll({ size: 50 });
      setRules(res.data.content || []);
    } catch (err) {
      console.error('Failed to load rules:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreating(true);
    setError('');
    try {
      const payload = {
        metricType: createForm.metricType,
        description: createForm.description || null,
        minValue: createForm.minValue ? parseFloat(createForm.minValue) : null,
        maxValue: createForm.maxValue ? parseFloat(createForm.maxValue) : null,
      };
      await rulesAPI.create(payload);
      setShowCreate(false);
      setCreateForm({ metricType: 'TEMPERATURE', minValue: '', maxValue: '', description: '' });
      fetchRules();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create rule');
    } finally {
      setCreating(false);
    }
  };

  const openEditRule = (rule) => {
    setEditRule(rule);
    setEditForm2({
      metricType: rule.metricType,
      minValue: rule.minValue != null ? String(rule.minValue) : '',
      maxValue: rule.maxValue != null ? String(rule.maxValue) : '',
      description: rule.description || '',
    });
    setError('');
  };

  const handleEditRule = async (e) => {
    e.preventDefault();
    setEditing(true);
    setError('');
    try {
      await rulesAPI.update(editRule.id, {
        metricType: editForm2.metricType,
        minValue: editForm2.minValue ? parseFloat(editForm2.minValue) : null,
        maxValue: editForm2.maxValue ? parseFloat(editForm2.maxValue) : null,
        description: editForm2.description || null,
      });
      setEditRule(null);
      fetchRules();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update rule');
    } finally {
      setEditing(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this rule?')) return;
    try {
      await rulesAPI.delete(id);
      fetchRules();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete rule');
    }
  };

  const handleToggle = async (rule) => {
    try {
      await rulesAPI.update(rule.id, { active: !rule.active });
      fetchRules();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update rule');
    }
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner" />
        <span>Loading rules...</span>
      </div>
    );
  }

  return (
    <div className="rules-page">
      <Header title="Threshold Rules" subtitle={`${rules.length} rule(s) configured`} />

      {user?.role === 'ADMIN' && (
        <div className="devices-toolbar">
          <div />
          <button className="create-btn" onClick={() => setShowCreate(true)}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Add rule
          </button>
        </div>
      )}

      {showCreate && (
        <div className="modal-overlay" onClick={() => setShowCreate(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Create threshold rule</h3>
              <button className="modal-close" onClick={() => setShowCreate(false)}>×</button>
            </div>
            {error && <div className="modal-error">{error}</div>}
            <form onSubmit={handleCreate} className="modal-form">
              <div className="modal-form-group">
                <label>Metric type</label>
                <select
                  value={createForm.metricType}
                  onChange={(e) => setCreateForm({ ...createForm, metricType: e.target.value })}
                >
                  {metricTypes.map((m) => (
                    <option key={m} value={m}>{m.replace('_', ' ')}</option>
                  ))}
                </select>
              </div>
              <div className="form-row-2">
                <div className="modal-form-group">
                  <label>Min value</label>
                  <input
                    type="number"
                    step="any"
                    value={createForm.minValue}
                    onChange={(e) => setCreateForm({ ...createForm, minValue: e.target.value })}
                    placeholder="e.g. -10"
                  />
                </div>
                <div className="modal-form-group">
                  <label>Max value</label>
                  <input
                    type="number"
                    step="any"
                    value={createForm.maxValue}
                    onChange={(e) => setCreateForm({ ...createForm, maxValue: e.target.value })}
                    placeholder="e.g. 85"
                  />
                </div>
              </div>
              <div className="modal-form-group">
                <label>Description</label>
                <input
                  type="text"
                  value={createForm.description}
                  onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
                  placeholder="e.g. Temperature threshold for server room"
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="modal-cancel" onClick={() => setShowCreate(false)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={creating}>
                  {creating ? 'Creating...' : 'Create rule'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="rules-list">
        {rules.map((rule) => (
          <div key={rule.id} className={`rule-card ${!rule.active ? 'inactive' : ''}`}>
            <div className="rule-left">
              <div className="rule-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
              </div>
              <div className="rule-info">
                <div className="rule-metric">{rule.metricType?.replace('_', ' ')}</div>
                <div className="rule-desc">{rule.description || 'No description'}</div>
              </div>
            </div>

            <div className="rule-thresholds">
              {rule.minValue != null && (
                <div className="threshold-item">
                  <span className="threshold-label">Min</span>
                  <span className="threshold-value">{rule.minValue}</span>
                </div>
              )}
              {rule.maxValue != null && (
                <div className="threshold-item">
                  <span className="threshold-label">Max</span>
                  <span className="threshold-value">{rule.maxValue}</span>
                </div>
              )}
            </div>

            <div className="rule-actions">
              <button
                className={`toggle-btn ${rule.active ? 'active' : ''}`}
                onClick={() => handleToggle(rule)}
                title={rule.active ? 'Deactivate' : 'Activate'}
              >
                <div className="toggle-track">
                  <div className="toggle-thumb" />
                </div>
              </button>
              {user?.role === 'ADMIN' && (
                <button className="edit-btn" onClick={() => openEditRule(rule)}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
              )}
              {user?.role === 'ADMIN' && (
                <button className="delete-btn" onClick={() => handleDelete(rule.id)}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              )}
            </div>
          </div>
        ))}

        {rules.length === 0 && (
          <div className="empty-state">No threshold rules configured.</div>
        )}
      </div>

      {editRule && (
        <div className="modal-overlay" onClick={() => setEditRule(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Edit rule</h3>
              <button className="modal-close" onClick={() => setEditRule(null)}>×</button>
            </div>
            {error && <div className="modal-error">{error}</div>}
            <form onSubmit={handleEditRule} className="modal-form">
              <div className="modal-form-group">
                <label>Metric type</label>
                <select
                  value={editForm2.metricType}
                  onChange={(e) => setEditForm2({ ...editForm2, metricType: e.target.value })}
                >
                  {metricTypes.map((m) => (
                    <option key={m} value={m}>{m.replace('_', ' ')}</option>
                  ))}
                </select>
              </div>
              <div className="form-row-2">
                <div className="modal-form-group">
                  <label>Min value</label>
                  <input
                    type="number"
                    step="any"
                    value={editForm2.minValue}
                    onChange={(e) => setEditForm2({ ...editForm2, minValue: e.target.value })}
                    placeholder="e.g. -10"
                  />
                </div>
                <div className="modal-form-group">
                  <label>Max value</label>
                  <input
                    type="number"
                    step="any"
                    value={editForm2.maxValue}
                    onChange={(e) => setEditForm2({ ...editForm2, maxValue: e.target.value })}
                    placeholder="e.g. 85"
                  />
                </div>
              </div>
              <div className="modal-form-group">
                <label>Description</label>
                <input
                  type="text"
                  value={editForm2.description}
                  onChange={(e) => setEditForm2({ ...editForm2, description: e.target.value })}
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="modal-cancel" onClick={() => setEditRule(null)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={editing}>
                  {editing ? 'Saving...' : 'Save changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}