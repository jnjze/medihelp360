# 🚀 MediHelp360 - Inicio Rápido

## 🎯 **Acceso a la aplicación:**

### **🌐 URLs principales:**
- **Frontend React**: http://localhost:4040
- **API Gateway**: http://localhost:8080
- **Consul UI**: http://localhost:8500

### **📊 Bases de datos (Modo desarrollo):**
- **PostgreSQL Users**: localhost:5432
- **PostgreSQL Sync A**: localhost:5433
- **MySQL Sync B**: localhost:3306
- **MongoDB Sync C**: localhost:27017

## ⚡ **Comandos rápidos:**

### **Habilitar acceso completo (desarrollo):**
```bash
./scripts/toggle-database-access.sh enable
```

### **Modo seguro (producción):**
```bash
./scripts/toggle-database-access.sh disable
```

### **Ver estado actual:**
```bash
./scripts/toggle-database-access.sh status
```

## 🔧 **Desarrollo del Frontend:**

### **Desarrollo directo con npm:**
```bash
cd frontend-app
npm start
# Abre http://localhost:3000
```

### **Desarrollo con Docker:**
```bash
./scripts/toggle-database-access.sh enable
# Frontend disponible en http://localhost:4040
```

## 📝 **Notas importantes:**

- **Puerto 3000**: Grafana y desarrollo local del frontend (npm start)
- **Puerto 4040**: Frontend en Docker modo desarrollo
- **Puerto 8080**: API Gateway (punto de entrada principal)
- **Puerto 8500**: Consul UI para administración
