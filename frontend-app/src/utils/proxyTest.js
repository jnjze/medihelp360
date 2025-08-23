// Utility para probar la configuración del proxy
export const testProxyConnection = async () => {
  try {
    console.log('🧪 Testing proxy connection...');
    
    // Test 1: Verificar que el proxy esté funcionando
    const testUrl = '/auth/register';
    console.log('📍 Test URL:', testUrl);
    console.log('🌐 Base URL:', import.meta.env.VITE_SERVER_URL || 'relative (proxy)');
    
    // Test 2: Hacer una llamada de prueba
    const response = await fetch(testUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'TestPass123!',
        name: 'Test User',
        confirmPassword: 'TestPass123!'
      })
    });
    
    console.log('✅ Proxy test response:', {
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries())
    });
    
    return { success: true, response };
    
  } catch (error) {
    console.error('❌ Proxy test failed:', error);
    return { success: false, error };
  }
};

// Función para verificar la configuración del entorno
export const checkEnvironment = () => {
  console.log('🔍 Environment Check:');
  console.log('  - NODE_ENV:', import.meta.env.NODE_ENV);
  console.log('  - DEV:', import.meta.env.DEV);
  console.log('  - VITE_SERVER_URL:', import.meta.env.VITE_SERVER_URL);
  console.log('  - Current URL:', window.location.href);
  console.log('  - Origin:', window.location.origin);
};
