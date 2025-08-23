import TableRow from '@mui/material/TableRow';
import TableHead from '@mui/material/TableHead';
import TableCell from '@mui/material/TableCell';

// ----------------------------------------------------------------------

export function UserTableHead() {
  return (
    <TableHead>
      <TableRow>
        <TableCell>ID</TableCell>
        <TableCell>Nombre</TableCell>
        <TableCell>Email</TableCell>
        <TableCell>Roles</TableCell>
        <TableCell>Estado</TableCell>
        <TableCell>Fecha Creación</TableCell>
        <TableCell align="right">Acciones</TableCell>
      </TableRow>
    </TableHead>
  );
}
