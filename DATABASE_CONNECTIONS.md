# 🗄️ Conexiones a Bases de Datos - MediHelp360

## 🔧 **Modo Desarrollo (Acceso Externo Habilitado)**

### Para habilitar acceso externo:
```bash
./scripts/toggle-database-access.sh enable
```

### 📊 **PostgreSQL - User Management**
```
Host: localhost
Port: 5432
Database: users_db
Username: users_user
Password: users_pass
```

**Conexión URL:**
```
postgresql://users_user:users_pass@localhost:5432/users_db
```

### 📊 **PostgreSQL - Database Sync Service A**
```
Host: localhost
Port: 5433
Database: sync_db_a
Username: sync_user_a
Password: sync_pass_a
```

**Conexión URL:**
```
postgresql://sync_user_a:sync_pass_a@localhost:5433/sync_db_a
```

### 📊 **MySQL - Database Sync Service B**
```
Host: localhost
Port: 3306
Database: sync_db_b
Username: sync_user_b
Password: sync_pass_b
Root Password: root_password
```

**Conexión URL:**
```
mysql://sync_user_b:sync_pass_b@localhost:3306/sync_db_b
```

### 📊 **MongoDB - Database Sync Service C**
```
Host: localhost
Port: 27017
Database: sync_db_c
Username: sync_user_c
Password: sync_pass_c
```

**Conexión URL:**
```
mongodb://sync_user_c:sync_pass_c@localhost:27017/sync_db_c
```

---

## 🛠️ **Herramientas Recomendadas**

### **DBeaver (PostgreSQL & MySQL)**
```bash
# Descargar desde: https://dbeaver.io/

# Configuración PostgreSQL Users:
- Driver: PostgreSQL
- Host: localhost
- Port: 5432
- Database: users_db
- Username: users_user
- Password: users_pass

# Configuración PostgreSQL Sync A:
- Driver: PostgreSQL  
- Host: localhost
- Port: 5433
- Database: sync_db_a
- Username: sync_user_a
- Password: sync_pass_a

# Configuración MySQL Sync B:
- Driver: MySQL
- Host: localhost
- Port: 3306
- Database: sync_db_b
- Username: sync_user_b
- Password: sync_pass_b
```

### **MongoDB Compass**
```bash
# Descargar desde: https://www.mongodb.com/products/compass

# String de conexión:
mongodb://sync_user_c:sync_pass_c@localhost:27017/sync_db_c
```

### **pgAdmin (Solo PostgreSQL)**
```bash
# Descargar desde: https://www.pgadmin.org/

# Agregar servidores:
1. Users DB - localhost:5432
2. Sync A DB - localhost:5433
```

---

## 🔍 **Comandos de Verificación**

### **Verificar conexiones activas:**
```bash
# Ver puertos abiertos
./scripts/toggle-database-access.sh status

# Ver contenedores en ejecución
docker ps

# Probar conexión PostgreSQL
psql -h localhost -p 5432 -U users_user -d users_db

# Probar conexión MySQL
mysql -h localhost -P 3306 -u sync_user_b -p sync_db_b

# Probar conexión MongoDB
mongosh "mongodb://sync_user_c:sync_pass_c@localhost:27017/sync_db_c"
```

### **Ver logs de bases de datos:**
```bash
# PostgreSQL
docker logs postgres-users-dev
docker logs postgres-sync-a-dev

# MySQL
docker logs mysql-sync-b-dev

# MongoDB
docker logs mongo-sync-c-dev
```

---

## 🔒 **Modo Producción (Solo Acceso Interno)**

### Para deshabilitar acceso externo:
```bash
./scripts/toggle-database-access.sh disable
```

En modo producción:
- ❌ No hay acceso externo a bases de datos
- ✅ Solo microservicios pueden conectarse
- ✅ Mayor seguridad
- ✅ Aislamiento de red completo

---

## 📊 **Scripts de Base de Datos**

### **Backup automático:**
```bash
# PostgreSQL
docker exec postgres-users-dev pg_dump -U users_user users_db > backup_users.sql
docker exec postgres-sync-a-dev pg_dump -U sync_user_a sync_db_a > backup_sync_a.sql

# MySQL  
docker exec mysql-sync-b-dev mysqldump -u sync_user_b -p sync_db_b > backup_sync_b.sql

# MongoDB
docker exec mongo-sync-c-dev mongodump --uri="mongodb://sync_user_c:sync_pass_c@localhost:27017/sync_db_c"
```

### **Restaurar datos:**
```bash
# PostgreSQL
docker exec -i postgres-users-dev psql -U users_user -d users_db < backup_users.sql

# MySQL
docker exec -i mysql-sync-b-dev mysql -u sync_user_b -p sync_db_b < backup_sync_b.sql

# MongoDB
docker exec -i mongo-sync-c-dev mongorestore --uri="mongodb://sync_user_c:sync_pass_c@localhost:27017/sync_db_c"
```

---

## ⚡ **Tips de Desarrollo**

1. **Usar modo desarrollo** para debugging y desarrollo local
2. **Cambiar a modo producción** antes de hacer deploy
3. **Hacer backup** antes de cambios importantes
4. **Verificar logs** si hay problemas de conexión
5. **Usar herramientas gráficas** para exploración de datos

**¡Ahora puedes acceder fácilmente a todas las bases de datos para desarrollo!** 🎯
