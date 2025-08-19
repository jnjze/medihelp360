import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  Box,
  CircularProgress,
  Chip,
} from '@mui/material';
import {
  People as PeopleIcon,
  Security as SecurityIcon,
  MonitorHeart as MonitorIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';
import { api } from '../services/api';
import { useNotification } from '../components/NotificationProvider';

interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  totalRoles: number;
  servicesStatus: {
    [key: string]: 'HEALTHY' | 'UNHEALTHY' | 'UNKNOWN';
  };
}

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const { showNotification } = useNotification();

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      
      // Simular datos por ahora (para evitar errores de conexión durante build)
      const mockStats = {
        totalUsers: 25,
        activeUsers: 18,
        totalRoles: 4,
        servicesStatus: {
          'api-gateway': 'HEALTHY' as const,
          'user-management': 'HEALTHY' as const,
          'sync-a': 'HEALTHY' as const,
          'sync-b': 'HEALTHY' as const,
          'sync-c': 'UNHEALTHY' as const,
        },
      };

      setStats(mockStats);

    } catch (error) {
      console.error('Error loading dashboard data:', error);
      showNotification('Error al cargar datos del dashboard', 'error');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4, textAlign: 'center' }}>
        <CircularProgress />
        <Typography variant="h6" sx={{ mt: 2 }}>
          Cargando dashboard...
        </Typography>
      </Container>
    );
  }

  const StatCard = ({ title, value, icon, color }: any) => (
    <Card sx={{ height: '100%', mb: 2 }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography color="textSecondary" gutterBottom variant="overline">
              {title}
            </Typography>
            <Typography variant="h4" component="h2" color={color}>
              {value}
            </Typography>
          </Box>
          <Box sx={{ color }}>{icon}</Box>
        </Box>
      </CardContent>
    </Card>
  );

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" sx={{ mb: 4 }}>
        Dashboard - MediHelp360
      </Typography>

      {/* Estadísticas principales en layout simple */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2, mb: 4 }}>
        <Box sx={{ flex: 1 }}>
          <StatCard
            title="Total Usuarios"
            value={stats?.totalUsers || 0}
            icon={<PeopleIcon sx={{ fontSize: 40 }} />}
            color="primary.main"
          />
        </Box>

        <Box sx={{ flex: 1 }}>
          <StatCard
            title="Usuarios Activos"
            value={stats?.activeUsers || 0}
            icon={<TrendingUpIcon sx={{ fontSize: 40 }} />}
            color="success.main"
          />
        </Box>

        <Box sx={{ flex: 1 }}>
          <StatCard
            title="Total Roles"
            value={stats?.totalRoles || 0}
            icon={<SecurityIcon sx={{ fontSize: 40 }} />}
            color="info.main"
          />
        </Box>

        <Box sx={{ flex: 1 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Box>
                  <Typography color="textSecondary" gutterBottom variant="overline">
                    Estado Servicios
                  </Typography>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                    {Object.entries(stats?.servicesStatus || {}).map(([service, status]) => (
                      <Chip
                        key={service}
                        label={service}
                        color={status === 'HEALTHY' ? 'success' : 'error'}
                        size="small"
                      />
                    ))}
                  </Box>
                </Box>
                <MonitorIcon sx={{ fontSize: 40, color: 'warning.main' }} />
              </Box>
            </CardContent>
          </Card>
        </Box>
      </Box>

      {/* Información adicional */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Información del Sistema
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
            <Box sx={{ flex: 1 }}>
              <Typography variant="subtitle2">Ambiente:</Typography>
              <Chip
                label={process.env.REACT_APP_ENVIRONMENT || 'development'}
                color={process.env.REACT_APP_ENVIRONMENT === 'production' ? 'success' : 'warning'}
                size="small"
              />
            </Box>
            <Box sx={{ flex: 1 }}>
              <Typography variant="subtitle2">Versión:</Typography>
              <Typography variant="body2">
                {process.env.REACT_APP_VERSION || '1.0.0'}
              </Typography>
            </Box>
            <Box sx={{ flex: 1 }}>
              <Typography variant="subtitle2">API Base URL:</Typography>
              <Typography variant="body2">
                {process.env.REACT_APP_API_BASE_URL}
              </Typography>
            </Box>
            <Box sx={{ flex: 1 }}>
              <Typography variant="subtitle2">Debug Mode:</Typography>
              <Chip
                label={process.env.REACT_APP_DEBUG === 'true' ? 'ON' : 'OFF'}
                color={process.env.REACT_APP_DEBUG === 'true' ? 'warning' : 'default'}
                size="small"
              />
            </Box>
          </Box>
        </CardContent>
      </Card>
    </Container>
  );
};

export default Dashboard;