import * as z from 'zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useBoolean } from 'minimal-shared/hooks';
import { zodResolver } from '@hookform/resolvers/zod';

import Box from '@mui/material/Box';
import Link from '@mui/material/Link';
import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';

import { paths } from 'src/routes/paths';
import { useRouter } from 'src/routes/hooks';
import { RouterLink } from 'src/routes/components';

import { Iconify } from 'src/components/iconify';
import { Form, Field, schemaUtils } from 'src/components/hook-form';

import { signUp } from '../../context/jwt';
import { useAuthContext } from '../../hooks';
import { getErrorMessage } from '../../utils';
import { FormHead } from '../../components/form-head';
import { SignUpTerms } from '../../components/sign-up-terms';

// ----------------------------------------------------------------------

export const SignUpSchema = z.object({
  name: z.string().min(2, { message: 'Name must be at least 2 characters!' }).max(100, { message: 'Name must be less than 100 characters!' }),
  email: schemaUtils.email(),
  password: z
    .string()
    .min(1, { message: 'Password is required!' })
    .min(8, { message: 'Password must be at least 8 characters!' })
    .max(128, { message: 'Password must be less than 128 characters!' })
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/, {
      message: 'Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character'
    }),
  confirmPassword: z.string().min(1, { message: 'Password confirmation is required!' }),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

// ----------------------------------------------------------------------

export function JwtSignUpView() {
  const router = useRouter();

  const showPassword = useBoolean();

  const { checkUserSession } = useAuthContext();

  const [errorMessage, setErrorMessage] = useState(null);

  const defaultValues = {
    name: 'Test User',
    email: 'test@example.com',
    password: 'SecurePass123!',
    confirmPassword: 'SecurePass123!',
  };

  const methods = useForm({
    resolver: zodResolver(SignUpSchema),
    defaultValues,
  });

  const {
    handleSubmit,
    formState: { isSubmitting },
  } = methods;

  const onSubmit = handleSubmit(async (data) => {
    try {
      const registrationResult = await signUp({
        email: data.email,
        password: data.password,
        name: data.name,
        confirmPassword: data.confirmPassword,
      });
      
      console.log('Registration successful:', registrationResult);
      
      // Mostrar mensaje de éxito
      setErrorMessage(null);
      
      // Redirigir al login después de registro exitoso
      router.push(paths.auth.jwt.signIn);
      
    } catch (error) {
      console.error('Registration error:', error);
      
      // Manejar errores estructurados del backend
      if (error.errorData) {
        const { message, suggestion, validationErrors } = error.errorData;
        
        if (validationErrors && validationErrors.length > 0) {
          // Mostrar errores de validación específicos
          const validationMessages = validationErrors.map(err => `${err.field}: ${err.message}`).join(', ');
          setErrorMessage(`${message}. ${validationMessages}`);
        } else {
          // Mostrar mensaje principal con sugerencia
          setErrorMessage(suggestion ? `${message}. ${suggestion}` : message);
        }
      } else {
        // Fallback para errores no estructurados
        const feedbackMessage = getErrorMessage(error);
        setErrorMessage(feedbackMessage);
      }
    }
  });

  const renderForm = () => (
    <Box sx={{ gap: 3, display: 'flex', flexDirection: 'column' }}>
      <Field.Text
        name="name"
        label="Nombre completo"
        placeholder="Ingresa tu nombre completo"
        slotProps={{ inputLabel: { shrink: true } }}
      />

      <Field.Text 
        name="email" 
        label="Correo electrónico" 
        placeholder="Ingresa tu correo electrónico"
        slotProps={{ inputLabel: { shrink: true } }} 
      />

      <Field.Text
        name="password"
        label="Contraseña"
        placeholder="8+ caracteres con mayúsculas, minúsculas, dígitos y caracteres especiales"
        type={showPassword.value ? 'text' : 'password'}
        slotProps={{
          inputLabel: { shrink: true },
          input: {
            endAdornment: (
              <InputAdornment position="end">
                <IconButton onClick={showPassword.onToggle} edge="end">
                  <Iconify icon={showPassword.value ? 'solar:eye-bold' : 'solar:eye-closed-bold'} />
                </IconButton>
              </InputAdornment>
            ),
          },
        }}
      />

      <Field.Text
        name="confirmPassword"
        label="Confirmar contraseña"
        placeholder="Confirma tu contraseña"
        type={showPassword.value ? 'text' : 'password'}
        slotProps={{
          inputLabel: { shrink: true },
          input: {
            endAdornment: (
              <InputAdornment position="end">
                <IconButton onClick={showPassword.onToggle} edge="end">
                  <Iconify icon={showPassword.value ? 'solar:eye-bold' : 'solar:eye-closed-bold'} />
                </IconButton>
              </InputAdornment>
            ),
          },
        }}
      />

      <Button
        fullWidth
        color="inherit"
        size="large"
        type="submit"
        variant="contained"
        loading={isSubmitting}
        loadingIndicator="Creando cuenta..."
      >
        Crear cuenta
      </Button>
    </Box>
  );

  return (
    <>
      <FormHead
        title="Únete a MediHelp360"
        description={
          <>
            {`¿Ya tienes una cuenta? `}
            <Link component={RouterLink} href={paths.auth.jwt.signIn} variant="subtitle2">
              Inicia sesión aquí
            </Link>
          </>
        }
        sx={{ textAlign: { xs: 'center', md: 'left' } }}
      />

      {!!errorMessage && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {errorMessage}
        </Alert>
      )}

      <Form methods={methods} onSubmit={onSubmit}>
        {renderForm()}
      </Form>

      <SignUpTerms />
    </>
  );
}
