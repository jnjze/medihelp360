import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  Box,
  Chip,
  IconButton,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { api } from '../services/api';
import { useNotification } from '../components/NotificationProvider';

interface ServiceStatus {
  name: string;
  displayName: string;
  status: 'HEALTHY' | 'UNHEALTHY' | 'UNKNOWN';
  lastChecked: Date;
  responseTime?: number;
  error?: string;
}

const SystemStatus: React.FC = () => {
  const [services, setServices] = useState<ServiceStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const { showNotification } = useNotification();

  useEffect(() => {
    checkSystemStatus();
    
    // Verificar estado cada 30 segundos
    const interval = setInterval(checkSystemStatus, 30000);
    return () => clearInterval(interval);
  }, []);

  const checkSystemStatus = async () => {
    try {
      setLoading(true);
      
      const servicesToCheck = [
        { key: 'api-gateway', name: 'API Gateway', endpoint: '/actuator/health' },
        { key: 'user-management', name: 'User Management Service', endpoint: '/users/actuator/health' },
        { key: 'sync-a', name: 'Database Sync Service A', endpoint: '/sync-a/actuator/health' },
        { key: 'sync-b', name: 'Database Sync Service B', endpoint: '/sync-b/actuator/health' },
        { key: 'sync-c', name: 'Database Sync Service C', endpoint: '/sync-c/actuator/health' },
      ];

      const statusPromises = servicesToCheck.map(async (service) => {
        const startTime = Date.now();
        try {
          await api.health();  // Simplificado para demo
          const responseTime = Date.now() - startTime;
          
          return {
            name: service.key,
            displayName: service.name,
            status: 'HEALTHY' as const,
            lastChecked: new Date(),
            responseTime,
          };
        } catch (error: any) {
          return {
            name: service.key,
            displayName: service.name,
            status: 'UNHEALTHY' as const,
            lastChecked: new Date(),
            error: error.message || 'Connection failed',
          };
        }
      });

      const results = await Promise.all(statusPromises);
      setServices(results);
      
    } catch (error) {
      console.error('Error checking system status:', error);
      showNotification('Error al verificar estado del sistema', 'error');
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'HEALTHY':
        return <CheckCircleIcon color="success" />;
      case 'UNHEALTHY':
        return <ErrorIcon color="error" />;
      default:
        return <WarningIcon color="warning" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'HEALTHY':
        return 'success';
      case 'UNHEALTHY':
        return 'error';
      default:
        return 'warning';
    }
  };

  const getOverallStatus = () => {
    const unhealthyCount = services.filter(s => s.status === 'UNHEALTHY').length;
    const unknownCount = services.filter(s => s.status === 'UNKNOWN').length;
    
    if (unhealthyCount > 0) return 'UNHEALTHY';
    if (unknownCount > 0) return 'WARNING';
    return 'HEALTHY';
  };

  if (loading && services.length === 0) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4, textAlign: 'center' }}>
        <CircularProgress />
        <Typography variant="h6" sx={{ mt: 2 }}>
          Verificando estado del sistema...
        </Typography>
      </Container>
    );
  }

  const overallStatus = getOverallStatus();

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Estado del Sistema</Typography>
        <IconButton onClick={checkSystemStatus} color="primary" disabled={loading}>
          <RefreshIcon />
        </IconButton>
      </Box>

      {/* Estado general */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2, mb: 4 }}>
        <Box sx={{ flex: 1 }}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Box sx={{ mb: 2 }}>
                {getStatusIcon(overallStatus)}
              </Box>
              <Typography variant="h6" gutterBottom>
                Estado General
              </Typography>
              <Chip
                label={overallStatus}
                color={getStatusColor(overallStatus) as any}
                size="medium"
              />
            </CardContent>
          </Card>
        </Box>

        <Box sx={{ flex: 1 }}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="success.main">
                {services.filter(s => s.status === 'HEALTHY').length}
              </Typography>
              <Typography variant="h6" color="text.secondary">
                Servicios Activos
              </Typography>
            </CardContent>
          </Card>
        </Box>

        <Box sx={{ flex: 1 }}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" color="error.main">
                {services.filter(s => s.status === 'UNHEALTHY').length}
              </Typography>
              <Typography variant="h6" color="text.secondary">
                Servicios con Problemas
              </Typography>
            </CardContent>
          </Card>
        </Box>
      </Box>

      {/* Detalle de servicios */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Detalle de Servicios
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Servicio</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell>Última Verificación</TableCell>
                  <TableCell>Tiempo de Respuesta</TableCell>
                  <TableCell>Información Adicional</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {services.map((service) => (
                  <TableRow key={service.name}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {getStatusIcon(service.status)}
                        {service.displayName}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={service.status}
                        color={getStatusColor(service.status) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {service.lastChecked.toLocaleTimeString()}
                    </TableCell>
                    <TableCell>
                      {service.responseTime ? `${service.responseTime}ms` : '-'}
                    </TableCell>
                    <TableCell>
                      {service.error ? (
                        <Typography variant="body2" color="error">
                          {service.error}
                        </Typography>
                      ) : (
                        <Typography variant="body2" color="success.main">
                          Funcionando correctamente
                        </Typography>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Container>
  );
};

export default SystemStatus;
