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
import { testProxyConnection, checkEnvironment } from '../../../utils/proxyTest';

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
        label="Full Name"
        placeholder="Enter your full name"
        slotProps={{ inputLabel: { shrink: true } }}
      />

      <Field.Text 
        name="email" 
        label="Email address" 
        placeholder="Enter your email"
        slotProps={{ inputLabel: { shrink: true } }} 
      />

      <Field.Text
        name="password"
        label="Password"
        placeholder="8+ characters with uppercase, lowercase, digit & special char"
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
        label="Confirm Password"
        placeholder="Confirm your password"
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
        loadingIndicator="Create account..."
      >
        Create account
      </Button>

      {/* Botones de prueba para debugging */}
      {import.meta.env.DEV && (
        <Box sx={{ mt: 2, display: 'flex', gap: 2, flexDirection: 'column' }}>
          <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'center' }}>
            Debug Tools (Development Only)
          </Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button
              size="small"
              variant="outlined"
              onClick={() => checkEnvironment()}
              sx={{ flex: 1 }}
            >
              Check Environment
            </Button>
            <Button
              size="small"
              variant="outlined"
              onClick={() => testProxyConnection()}
              sx={{ flex: 1 }}
            >
              Test Proxy
            </Button>
          </Box>
        </Box>
      )}
    </Box>
  );

  return (
    <>
      <FormHead
        title="Join MediHelp360"
        description={
          <>
            {`Already have an account? `}
            <Link component={RouterLink} href={paths.auth.jwt.signIn} variant="subtitle2">
              Sign in here
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
