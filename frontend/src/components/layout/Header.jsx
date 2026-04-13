import { useAuth } from '../../context/AuthContext';
import './Layout.css';

export default function Header({ title, subtitle }) {
  const { user } = useAuth();

  return (
    <header className="header">
      <div className="header-left">
        <h1 className="header-title">{title}</h1>
        {subtitle && <p className="header-subtitle">{subtitle}</p>}
      </div>
      <div className="header-right">
        <div className="header-status">
          <div className="header-status-dot" />
          <span>System online</span>
        </div>
      </div>
    </header>
  );
}