import { useState, useEffect, useCallback } from 'react';

import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import Table from '@mui/material/Table';
import Stack from '@mui/material/Stack';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import TableBody from '@mui/material/TableBody';
import Typography from '@mui/material/Typography';
import TableContainer from '@mui/material/TableContainer';
import TablePagination from '@mui/material/TablePagination';
import CircularProgress from '@mui/material/CircularProgress';

import { Iconify } from 'src/components/iconify';
import { Scrollbar } from 'src/components/scrollbar';
import { useSettingsContext } from 'src/components/settings';

import { UserTableRow } from './user-table-row';
import { UserTableHead } from './user-table-head';
import { UserCreateDialog } from './user-create-dialog';
import { apiService } from '../api/api-service';

// ----------------------------------------------------------------------

export function UsersView() {
  const settings = useSettingsContext();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [openCreate, setOpenCreate] = useState(false);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = useCallback(async () => {
    try {
      setLoading(true);
      
      // Mock data para demostración
      const mockUsers = [
        {
          id: 1,
          name: 'Dr. Juan Pérez',
          email: 'juan.perez@medihelp360.com',
          roles: ['ADMIN', 'DOCTOR'],
          status: 'ACTIVE',
          createdAt: '2024-01-15',
        },
        {
          id: 2,
          name: 'María González',
          email: 'maria.gonzalez@medihelp360.com',
          roles: ['USER'],
          status: 'ACTIVE',
          createdAt: '2024-01-20',
        },
        {
          id: 3,
          name: 'Carlos Ruiz',
          email: 'carlos.ruiz@medihelp360.com',
          roles: ['USER'],
          status: 'INACTIVE',
          createdAt: '2024-02-01',
        },
      ];

      setUsers(mockUsers);
    } catch (error) {
      console.error('Error loading users:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleDeleteUser = useCallback(async (id) => {
    try {
      // await apiService.users.delete(id);
      setUsers(prev => prev.filter(user => user.id !== id));
      console.log('User deleted:', id);
    } catch (error) {
      console.error('Error deleting user:', error);
    }
  }, []);

  const handleCreateUser = useCallback(async (userData) => {
    try {
      // const response = await apiService.users.create(userData);
      const newUser = {
        id: Date.now(),
        ...userData,
        createdAt: new Date().toISOString().split('T')[0],
      };
      setUsers(prev => [...prev, newUser]);
      setOpenCreate(false);
      console.log('User created:', newUser);
    } catch (error) {
      console.error('Error creating user:', error);
    }
  }, []);

  const paginatedUsers = users.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  if (loading) {
    return (
      <Container maxWidth={settings.state.themeStretch ? false : 'xl'}>
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <CircularProgress />
          <Typography variant="h6" sx={{ mt: 2 }}>
            Cargando usuarios...
          </Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth={settings.state.themeStretch ? false : 'xl'}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 5 }}>
        <Typography variant="h4">Gestión de Usuarios 👥</Typography>
        <Stack direction="row" spacing={1}>
          <Button
            variant="outlined"
            startIcon={<Iconify icon="solar:refresh-bold" />}
            onClick={loadUsers}
          >
            Actualizar
          </Button>
          <Button
            variant="contained"
            startIcon={<Iconify icon="mingcute:add-line" />}
            onClick={() => setOpenCreate(true)}
          >
            Nuevo Usuario
          </Button>
        </Stack>
      </Stack>

      <Card>
        <Scrollbar>
          <TableContainer sx={{ overflow: 'unset' }}>
            <Table sx={{ minWidth: 800 }}>
              <UserTableHead />
              <TableBody>
                {paginatedUsers.map((user) => (
                  <UserTableRow
                    key={user.id}
                    user={user}
                    onDelete={() => handleDeleteUser(user.id)}
                  />
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Scrollbar>

        <TablePagination
          component="div"
          count={users.length}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          rowsPerPageOptions={[5, 10, 25]}
        />
      </Card>

      <UserCreateDialog
        open={openCreate}
        onClose={() => setOpenCreate(false)}
        onSubmit={handleCreateUser}
      />
    </Container>
  );
}
