import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import Layout from './components/layout/Layout';
import Dashboard from './pages/dashboard/Dashboard';
import DeviceList from './pages/devices/DeviceList';
import DeviceDetail from './pages/devices/DeviceDetail';
import AlertsPage from './pages/alerts/AlertsPage';
import TelemetryPage from './pages/telemetry/TelemetryPage';
import RulesPage from './pages/rules/RulesPage';
import UsersPage from './pages/users/UsersPage';
import SettingsPage from './pages/settings/SettingsPage';
import GroupsPage from './pages/groups/GroupsPage';
import WebhooksPage from './pages/webhooks/WebhooksPage';
function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: '#09090b',
        color: '#52525b',
        fontSize: '13px',
      }}>
        Loading...
      </div>
    );
  }

  if (!user) return <Navigate to="/login" />;
  return children;
}

function PublicRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return null;
  if (user) return <Navigate to="/" />;
  return children;
}

function Placeholder({ title }) {
  return (
    <div style={{ padding: '2rem 0' }}>
      <h1 style={{ fontSize: '22px', fontWeight: 600, color: '#fafafa', marginBottom: '0.5rem', letterSpacing: '-0.03em' }}>{title}</h1>
      <p style={{ fontSize: '13px', color: '#52525b' }}>Coming soon</p>
    </div>
  );
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
      <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
      <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<Dashboard />} />
<Route path="devices" element={<DeviceList />} />
<Route path="devices/:id" element={<DeviceDetail />} />  
<Route path="telemetry" element={<TelemetryPage />} />
<Route path="alerts" element={<AlertsPage />} />        
<Route path="rules" element={<RulesPage />} />
<Route path="users" element={<UsersPage />} />
<Route path="groups" element={<GroupsPage />} />
        <Route path="webhooks" element={<WebhooksPage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;