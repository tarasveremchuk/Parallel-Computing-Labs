import { useState, useEffect } from 'react';
import { webhooksAPI } from '../../api/webhooks';
import Header from '../../components/layout/Header';
import './Webhooks.css';

export default function WebhooksPage() {
  const [webhooks, setWebhooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [createForm, setCreateForm] = useState({ name: '', url: '', minSeverity: '' });
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { fetchWebhooks(); }, []);

  const fetchWebhooks = async () => {
    try {
      const res = await webhooksAPI.getAll();
      setWebhooks(res.data.data || []);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleCreate = async (e) => {
    e.preventDefault(); setCreating(true); setError('');
    try {
      await webhooksAPI.create({
        name: createForm.name, url: createForm.url,
        minSeverity: createForm.minSeverity || null,
      });
      setShowCreate(false); setCreateForm({ name: '', url: '', minSeverity: '' });
      fetchWebhooks();
    } catch (err) { setError(err.response?.data?.message || 'Failed'); }
    finally { setCreating(false); }
  };

  const handleToggle = async (id) => {
    try { await webhooksAPI.toggle(id); fetchWebhooks(); }
    catch { alert('Failed'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this webhook?')) return;
    try { await webhooksAPI.delete(id); fetchWebhooks(); }
    catch { alert('Failed'); }
  };

  if (loading) return <div className="dashboard-loading"><div className="loading-spinner" /><span>Loading webhooks...</span></div>;

  return (
    <div className="webhooks-page">
      <Header title="Webhooks" subtitle={`${webhooks.length} webhook(s) configured`} />

      <div className="devices-toolbar">
        <div />
        <button className="create-btn" onClick={() => setShowCreate(true)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Add webhook
        </button>
      </div>

      {showCreate && (
        <div className="modal-overlay" onClick={() => setShowCreate(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header"><h3>Create webhook</h3><button className="modal-close" onClick={() => setShowCreate(false)}>×</button></div>
            {error && <div className="modal-error">{error}</div>}
            <form onSubmit={handleCreate} className="modal-form">
              <div className="modal-form-group"><label>Name</label>
                <input type="text" value={createForm.name} onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })} placeholder="e.g. Slack Notification" required /></div>
              <div className="modal-form-group"><label>URL</label>
                <input type="url" value={createForm.url} onChange={(e) => setCreateForm({ ...createForm, url: e.target.value })} placeholder="https://hooks.slack.com/..." required /></div>
              <div className="modal-form-group"><label>Minimum severity (optional)</label>
                <select value={createForm.minSeverity} onChange={(e) => setCreateForm({ ...createForm, minSeverity: e.target.value })}>
                  <option value="">All severities</option>
                  <option value="LOW">Low+</option>
                  <option value="MEDIUM">Medium+</option>
                  <option value="HIGH">High+</option>
                  <option value="CRITICAL">Critical only</option>
                </select></div>
              <div className="modal-actions"><button type="button" className="modal-cancel" onClick={() => setShowCreate(false)}>Cancel</button>
                <button type="submit" className="modal-submit" disabled={creating}>{creating ? 'Creating...' : 'Create'}</button></div>
            </form>
          </div>
        </div>
      )}

      <div className="webhooks-list">
        {webhooks.map((wh) => (
          <div key={wh.id} className={`webhook-card ${!wh.active ? 'inactive' : ''}`}>
            <div className="webhook-left">
              <div className="webhook-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
              </div>
              <div className="webhook-info">
                <span className="webhook-name">{wh.name}</span>
                <span className="webhook-url">{wh.url}</span>
                <div className="webhook-meta">
                  {wh.minSeverity && <span className="webhook-severity">{wh.minSeverity}+</span>}
                  <span>Triggered {wh.triggerCount}x</span>
                  {wh.lastTriggeredAt && <span> · Last: {new Date(wh.lastTriggeredAt).toLocaleString()}</span>}
                </div>
              </div>
            </div>
            <div className="webhook-actions">
              <button className={`toggle-btn ${wh.active ? 'active' : ''}`} onClick={() => handleToggle(wh.id)}>
                <div className="toggle-track"><div className="toggle-thumb" /></div>
              </button>
              <button className="delete-btn" onClick={() => handleDelete(wh.id)}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
              </button>
            </div>
          </div>
        ))}
        {webhooks.length === 0 && <div className="empty-state">No webhooks configured. Create one to receive alert notifications.</div>}
      </div>
    </div>
  );
}