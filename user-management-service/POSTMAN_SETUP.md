# 📮 **Configuración de Postman para MediHelp360**

## 🚀 **Importar Collection y Environment**

### **1. Importar Collection:**
1. Abre **Postman**
2. Haz clic en **"Import"**
3. Selecciona el archivo: `MediHelp360-Postman-Collection.json`
4. Haz clic en **"Import"**

### **2. Importar Environment:**
1. Haz clic en **"Import"** nuevamente
2. Selecciona el archivo: `MediHelp360-Postman-Environment.json`
3. Haz clic en **"Import"**
4. Selecciona el environment **"MediHelp360 - Local Development"** en el dropdown superior derecho

---

## 🔧 **Configuración Inicial**

### **Variables del Environment:**
- **`base_url`**: `http://localhost:8081` (URL del servicio)
- **`admin_email`**: `admin@medihelp360.com` (email del admin)
- **`admin_password`**: `admin123` (contraseña del admin)
- **`access_token`**: Se llena automáticamente después del login
- **`refresh_token`**: Se llena automáticamente después del login
- **`user_id`**: Se llena automáticamente después del login

---

## 🧪 **Orden de Pruebas Recomendado**

### **📋 PASO 1: Verificar Salud del Sistema**
1. **Health Check** - Verificar que el servicio esté funcionando
2. **Info** - Obtener información del servicio
3. **Metrics** - Ver métricas del sistema

### **🔐 PASO 2: Autenticación**
1. **Login - Admin User** - Hacer login con el usuario admin
   - ✅ **IMPORTANTE**: Este request ejecuta automáticamente un script que guarda:
     - `access_token` en las variables
     - `refresh_token` en las variables  
     - `user_id` en las variables
2. **Validate Token** - Verificar que el token sea válido

### **👥 PASO 3: Gestión de Usuarios**
1. **Get All Users** - Listar todos los usuarios
2. **Create New User** - Crear un nuevo usuario médico
3. **Get User by ID** - Obtener usuario específico
4. **Update User** - Actualizar información del usuario
5. **Delete User** - Eliminar usuario (opcional)

### **🔑 PASO 4: Gestión de Roles**
1. **Get All Roles** - Listar todos los roles
2. **Create New Role** - Crear nuevo rol (ej: NURSE)

### **🔄 PASO 5: Funcionalidades Avanzadas**
1. **Refresh Token** - Renovar token de acceso
2. **Logout** - Cerrar sesión

---

## 🎯 **Endpoints Disponibles**

### **🔐 Authentication (`/api/auth`)**
- `POST /login` - Login de usuarios
- `POST /logout` - Logout de usuarios
- `POST /validate` - Validar token JWT
- `POST /refresh` - Renovar token (placeholder)

### **👥 User Management (`/api/users`)**
- `GET /` - Listar todos los usuarios
- `GET /{id}` - Obtener usuario por ID
- `POST /` - Crear nuevo usuario
- `PUT /{id}` - Actualizar usuario
- `DELETE /{id}` - Eliminar usuario

### **🔑 Role Management (`/api/roles`)**
- `GET /` - Listar todos los roles
- `POST /` - Crear nuevo rol

### **📊 System Health (`/actuator`)**
- `GET /health` - Estado de salud
- `GET /info` - Información del servicio
- `GET /metrics` - Métricas del sistema

---

## 🚨 **Solución de Problemas**

### **Error: "Connection refused"**
- Verifica que el `user-management-service` esté ejecutándose
- Confirma que esté en el puerto 8081
- Verifica la URL en `base_url`

### **Error: "401 Unauthorized"**
- Verifica que hayas hecho login exitosamente
- Confirma que el `access_token` esté configurado
- Revisa que el token no haya expirado

### **Error: "404 Not Found"**
- Verifica que la URL base sea correcta
- Confirma que el servicio esté funcionando
- Revisa los logs del servicio

### **Token no se guarda automáticamente**
- Verifica que el environment esté seleccionado
- Confirma que el script de test esté ejecutándose
- Revisa la consola de Postman para mensajes de error

---

## 🔍 **Verificar Funcionamiento**

### **1. Logs del Servicio:**
```bash
# En los logs deberías ver:
2024-08-19 20:30:00 - Login request received for user: admin@medihelp360.com
2024-08-19 20:30:00 - Login successful for user: admin@medihelp360.com
```

### **2. Base de Datos:**
```sql
-- Verificar usuario admin creado
SELECT * FROM users WHERE email = 'admin@medihelp360.com';

-- Verificar sesión creada
SELECT * FROM user_sessions;

-- Verificar log de acceso
SELECT * FROM access_logs WHERE action = 'LOGIN_SUCCESS';
```

### **3. Variables de Postman:**
- Después del login exitoso, las variables deberían llenarse automáticamente
- Verifica en el panel derecho de Postman

---

## 📝 **Notas Importantes**

- **Siempre** ejecuta primero el **Health Check** para verificar conectividad
- **Siempre** ejecuta **Login - Admin User** antes de probar endpoints protegidos
- Los tokens se guardan **automáticamente** después del login exitoso
- El **Refresh Token** está marcado como placeholder (no implementado aún)
- **No elimines** el usuario admin a menos que sepas lo que haces

---

## 🎉 **¡Listo para Probar!**

Con esta configuración podrás probar completamente el sistema de autenticación y gestión de usuarios de MediHelp360. 

**¿Necesitas ayuda con algún endpoint específico?** 🤔
