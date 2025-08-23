import { useState, useCallback } from 'react';

export const useCorsError = () => {
  const [corsError, setCorsError] = useState(null);

  const handleCorsError = useCallback((error) => {
    if (error.isCorsError) {
      setCorsError({
        message: 'CORS Error: Unable to connect to backend',
        details: 'This usually means the backend is not running or CORS is not configured properly.',
        solutions: [
          'Make sure the backend is running on port 8080',
          'Check that the proxy configuration in Vite is working',
          'Verify the backend CORS configuration',
          'Try refreshing the page or restarting the dev server'
        ],
        originalError: error.originalError
      });
      return true;
    }
    return false;
  }, []);

  const clearCorsError = useCallback(() => {
    setCorsError(null);
  }, []);

  return {
    corsError,
    handleCorsError,
    clearCorsError
  };
};
