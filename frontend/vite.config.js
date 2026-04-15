import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Auth Service
      '/api/v1/auth': 'http://localhost:8081',
      '/api/v1/users': 'http://localhost:8081',

      // Device Service
      '/api/v1/devices': 'http://localhost:8082',
      '/api/v1/rules': 'http://localhost:8082',
      '/api/v1/groups': 'http://localhost:8082',
      '/api/v1/maintenance': 'http://localhost:8082',
      '/api/v1/commands': 'http://localhost:8082',

      // Telemetry Service
      '/api/v1/telemetry': 'http://localhost:8083',
      '/api/v1/alerts': 'http://localhost:8083',
      '/api/v1/dashboard': 'http://localhost:8083',
      '/api/v1/simulator': 'http://localhost:8083',
      '/api/v1/audit': 'http://localhost:8083',
      '/api/v1/webhooks': 'http://localhost:8083',
      '/api/v1/system': 'http://localhost:8083',
      
    }
  }
})