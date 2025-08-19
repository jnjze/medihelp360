import { useState } from 'react';

import Stack from '@mui/material/Stack';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import TextField from '@mui/material/TextField';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';

// ----------------------------------------------------------------------

export function UserCreateDialog({ open, onClose, onSubmit }) {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    status: 'ACTIVE',
    roles: ['USER'],
  });

  const handleChange = (field) => (event) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
  };

  const handleSubmit = () => {
    if (formData.name && formData.email) {
      onSubmit(formData);
      setFormData({
        name: '',
        email: '',
        status: 'ACTIVE',
        roles: ['USER'],
      });
    }
  };

  const handleClose = () => {
    setFormData({
      name: '',
      email: '',
      status: 'ACTIVE',
      roles: ['USER'],
    });
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Crear Nuevo Usuario</DialogTitle>
      
      <DialogContent>
        <Stack spacing={3} sx={{ mt: 1 }}>
          <TextField
            label="Nombre completo"
            value={formData.name}
            onChange={handleChange('name')}
            fullWidth
            required
          />
          
          <TextField
            label="Email"
            type="email"
            value={formData.email}
            onChange={handleChange('email')}
            fullWidth
            required
          />
          
          <FormControl fullWidth>
            <InputLabel>Estado</InputLabel>
            <Select
              value={formData.status}
              label="Estado"
              onChange={handleChange('status')}
            >
              <MenuItem value="ACTIVE">Activo</MenuItem>
              <MenuItem value="INACTIVE">Inactivo</MenuItem>
              <MenuItem value="PENDING">Pendiente</MenuItem>
            </Select>
          </FormControl>
        </Stack>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={handleClose}>
          Cancelar
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained"
          disabled={!formData.name || !formData.email}
        >
          Crear Usuario
        </Button>
      </DialogActions>
    </Dialog>
  );
}
