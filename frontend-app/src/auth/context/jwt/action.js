import axios, { endpoints } from 'src/lib/axios';

import { setSession } from './utils';
import { JWT_STORAGE_KEY } from './constant';

// ----------------------------------------------------------------------

/** **************************************
 * Sign in
 *************************************** */
export const signInWithPassword = async ({ email, password }) => {
  try {
    const params = { email, password };

    const res = await axios.post(endpoints.auth.login, params);

    const { accessToken, refreshToken, user } = res.data;

    if (!accessToken) {
      throw new Error('Access token not found in response');
    }

    setSession(accessToken);
    
    // Opcional: guardar información del usuario
    if (user) {
      sessionStorage.setItem('user', JSON.stringify(user));
    }
    
    return res.data;
  } catch (error) {
    console.error('Error during sign in:', error);
    throw error;
  }
};

/** **************************************
 * Sign up
 *************************************** */
export const signUp = async ({ email, password, name, confirmPassword }) => {
  const params = {
    email,
    password,
    name,
    confirmPassword,
  };

  try {
    const res = await axios.post(endpoints.auth.register, params);

    // El backend devuelve RegisterResponse, no accessToken
    const { id, email: userEmail, name: userName, status, roles, message } = res.data;

    if (!id) {
      throw new Error('User ID not found in response');
    }

    console.log('Registration successful:', { id, userEmail, userName, status, roles, message });
    
    // No guardamos token aquí, solo registramos al usuario
    // El usuario deberá hacer login después del registro
    
    return res.data;
  } catch (error) {
    console.error('Error during sign up:', error);
    throw error;
  }
};

/** **************************************
 * Sign out
 *************************************** */
export const signOut = async () => {
  try {
    await setSession(null);
  } catch (error) {
    console.error('Error during sign out:', error);
    throw error;
  }
};
