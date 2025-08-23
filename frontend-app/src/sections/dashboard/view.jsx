import { useState, useEffect } from 'react';

import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import Grid from '@mui/material/Grid';
import Chip from '@mui/material/Chip';
import Stack from '@mui/material/Stack';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import CardContent from '@mui/material/CardContent';
import CircularProgress from '@mui/material/CircularProgress';

import { Iconify } from 'src/components/iconify';
import { useSettingsContext } from 'src/components/settings';

// ----------------------------------------------------------------------

export function DashboardView() {
  const settings = useSettingsContext();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);

      // Simular datos por ahora
      const mockStats = {
        totalUsers: 25,
        activeUsers: 18,
        totalRoles: 4,
        servicesStatus: {
          'api-gateway': 'HEALTHY',
          'user-management': 'HEALTHY',
          'sync-a': 'HEALTHY',
          'sync-b': 'HEALTHY',
          'sync-c': 'UNHEALTHY',
        },
      };

      setStats(mockStats);
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ title, value, icon, color }) => (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Stack direction="row" alignItems="center" justifyContent="space-between">
          <Stack spacing={1}>
            <Typography color="text.secondary" variant="overline">
              {title}
            </Typography>
            <Typography variant="h4" sx={{ color }}>
              {value}
            </Typography>
          </Stack>
          <Box sx={{ color }}>
            <Iconify icon={icon} width={48} height={48} />
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <Container maxWidth={settings.state.themeStretch ? false : 'xl'}>
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <CircularProgress />
          <Typography variant="h6" sx={{ mt: 2 }}>
            Cargando dashboard...
          </Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth={settings.state.themeStretch ? false : 'xl'}>
      <Typography variant="h4" sx={{ mb: 5 }}>
        Dashboard MediHelp360 🏥
      </Typography>

      <Grid container spacing={3}>
        {/* Estadísticas principales */}
        <Grid xs={12} sm={6} md={3}>
          <StatCard
            title="Total Usuarios"
            value={stats?.totalUsers || 0}
            icon="solar:users-group-rounded-bold"
            color="primary.main"
          />
        </Grid>

        <Grid xs={12} sm={6} md={3}>
          <StatCard
            title="Usuarios Activos"
            value={stats?.activeUsers || 0}
            icon="solar:user-check-rounded-bold"
            color="success.main"
          />
        </Grid>

        <Grid xs={12} sm={6} md={3}>
          <StatCard
            title="Total Roles"
            value={stats?.totalRoles || 0}
            icon="solar:shield-check-bold"
            color="info.main"
          />
        </Grid>

        <Grid xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="center" justifyContent="space-between">
                <Stack spacing={1}>
                  <Typography color="text.secondary" variant="overline">
                    Estado Servicios
                  </Typography>
                  <Stack spacing={1}>
                    {Object.entries(stats?.servicesStatus || {}).map(([service, status]) => (
                      <Chip
                        key={service}
                        label={service}
                        color={status === 'HEALTHY' ? 'success' : 'error'}
                        size="small"
                        variant="soft"
                      />
                    ))}
                  </Stack>
                </Stack>
                <Iconify
                  icon="solar:heart-pulse-bold"
                  width={48}
                  height={48}
                  sx={{ color: 'warning.main' }}
                />
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Información del sistema */}
        <Grid xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Información del Sistema
              </Typography>
              <Grid container spacing={3}>
                <Grid xs={12} sm={6} md={3}>
                  <Stack spacing={1}>
                    <Typography variant="subtitle2">Ambiente:</Typography>
                    <Chip
                      label={import.meta.env.VITE_ENV || 'development'}
                      color={import.meta.env.VITE_ENV === 'production' ? 'success' : 'warning'}
                      size="small"
                    />
                  </Stack>
                </Grid>
                <Grid xs={12} sm={6} md={3}>
                  <Stack spacing={1}>
                    <Typography variant="subtitle2">Versión:</Typography>
                    <Typography variant="body2">
                      {import.meta.env.VITE_APP_VERSION || '1.0.0'}
                    </Typography>
                  </Stack>
                </Grid>
                <Grid xs={12} sm={6} md={3}>
                  <Stack spacing={1}>
                    <Typography variant="subtitle2">API Base URL:</Typography>
                    <Typography variant="body2">
                      {import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}
                    </Typography>
                  </Stack>
                </Grid>
                <Grid xs={12} sm={6} md={3}>
                  <Stack spacing={1}>
                    <Typography variant="subtitle2">Frontend:</Typography>
                    <Chip label="Vite + React" color="info" size="small" />
                  </Stack>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
}
