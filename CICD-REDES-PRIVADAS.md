# 🔒 CI/CD para Servidores en Redes Privadas

## 🚨 Problema
Tu servidor Jenkins está en una **red privada** y GitHub no puede alcanzarlo directamente para enviar webhooks.

## 🎯 Soluciones Disponibles

### 1️⃣ **Git Polling (Más Simple) ⭐ RECOMENDADO**

Jenkins revisa automáticamente el repositorio cada cierto tiempo.

**✅ Ventajas:**
- Configuración muy simple
- No requiere servidores adicionales
- Funciona desde cualquier red privada
- No hay puntos de falla externos

**❌ Desventajas:**
- Delay de hasta X minutos para deploy
- Consume más recursos de Jenkins

**🔧 Configuración:**

Ya está configurado en tu `Jenkinsfile`:
```groovy
triggers {
    // Revisar cada 5 minutos si hay cambios
    pollSCM('H/5 * * * *')
    
    // Solo en horas laborales (opcional)
    // pollSCM('H/10 8-18 * * 1-5')
}
```

**📋 Cómo usar:**
1. ✅ **Ya está listo** - tu Jenkins revisará GitHub cada 5 minutos
2. Haz `git push` a `main` o `develop`
3. Espera máximo 5 minutos
4. Jenkins detectará cambios y ejecutará pipeline

---

### 2️⃣ **Deploy Manual Inteligente (Backup Perfecto)**

Script que detecta cambios automáticamente y solo rebuilda si es necesario.

**✅ Ventajas:**
- Control total sobre cuándo deployar
- Backup y rollback automático
- Detección inteligente de cambios
- Funciona sin Jenkins

**❌ Desventajas:**
- Requiere ejecución manual
- Una persona debe trigger el deploy

**🔧 Uso:**

```bash
# Deploy automático si hay cambios
./scripts/manual-deploy.sh

# Deploy desde repo específico
./scripts/manual-deploy.sh https://github.com/tu-usuario/medihelp360.git

# Forzar rebuild aunque no haya cambios
./scripts/manual-deploy.sh https://github.com/tu-usuario/medihelp360.git main force

# Ver estado actual
./scripts/manual-deploy.sh status

# Rollback si algo sale mal
./scripts/manual-deploy.sh rollback
```

---

### 3️⃣ **Túnel SSH Reverse (Avanzado)**

Crear un túnel desde tu servidor privado hacia un servidor público.

**✅ Ventajas:**
- Webhooks en tiempo real (instantáneo)
- Funciona como si fuera servidor público
- Configurable con cualquier VPS barato

**❌ Desventajas:**
- Requiere servidor público adicional
- Configuración más compleja
- Punto de falla adicional

**🔧 Configuración:**

```bash
# Setup completo (necesitas un VPS público)
./scripts/setup-reverse-tunnel.sh mi-servidor-publico.com tunnel-user

# Test de conexión
./scripts/setup-reverse-tunnel.sh mi-servidor-publico.com tunnel-user test

# Iniciar túnel
./scripts/setup-reverse-tunnel.sh mi-servidor-publico.com tunnel-user start
```

---

## 🎯 **¿Cuál Elegir?**

### 🥇 **Para Empezar: Git Polling**
```bash
# Ya está configurado - solo haz commit
git add .
git commit -m "Deploy changes"
git push origin main

# Jenkins detectará en máximo 5 minutos
```

### 🥈 **Para Control Total: Manual Deploy**
```bash
# Ejecutar cuando quieras deployar
./scripts/manual-deploy.sh
```

### 🥉 **Para Webhooks Reales: Túnel SSH**
```bash
# Si tienes un VPS público
./scripts/setup-reverse-tunnel.sh mi-vps.com usuario
```

---

## 📊 Comparación Detallada

| Método | Tiempo Deploy | Complejidad | Costo | Automatización |
|--------|---------------|-------------|-------|----------------|
| **Git Polling** | 0-5 minutos | 🟢 Baja | 🟢 $0 | 🟡 Semi-auto |
| **Manual Deploy** | Inmediato | 🟢 Baja | 🟢 $0 | 🔴 Manual |
| **SSH Tunnel** | Inmediato | 🔴 Alta | 🟡 $5-10/mes | 🟢 Total |

---

## 🚀 **Implementación Rápida**

### Opción A: Git Polling (5 minutos)
```bash
# Ya está listo ✅
# Solo haz commits y espera máximo 5 minutos
```

### Opción B: Manual Deploy (2 minutos)
```bash
# Editar URL del repo
nano scripts/manual-deploy.sh  # Cambiar línea 8: REPO_URL="https://github.com/TU_USUARIO/medihelp360.git"

# Ejecutar deploy
./scripts/manual-deploy.sh
```

### Opción C: SSH Tunnel (30 minutos)
```bash
# 1. Conseguir VPS público (DigitalOcean, AWS, etc.)
# 2. Crear usuario tunnel
# 3. Ejecutar setup
./scripts/setup-reverse-tunnel.sh TU_VPS.com tunnel

# 4. Configurar webhook en GitHub:
#    URL: http://TU_VPS.com:9090/github-webhook/
```

---

## ⚡ **Start Quick - Git Polling**

**Tu sistema YA ESTÁ CONFIGURADO** para Git Polling:

1. **Edita código** y haz commit:
```bash
git add .
git commit -m "Nueva funcionalidad"
git push origin main
```

2. **Jenkins detectará automáticamente** (máximo 5 minutos)

3. **Pipeline se ejecutará automáticamente**:
   - ✅ Build de imágenes
   - ✅ Tests
   - ✅ Push al registry
   - ✅ Deploy a producción
   - ✅ Health checks

---

## 🔧 **Configuraciones Específicas**

### Cambiar Frecuencia de Polling
```groovy
// En Jenkinsfile, línea 6-10:
triggers {
    pollSCM('H/2 * * * *')    // Cada 2 minutos
    // o
    pollSCM('H/10 * * * *')   // Cada 10 minutos
    // o
    pollSCM('H/5 9-17 * * 1-5') // Solo horario laboral
}
```

### Configurar Manual Deploy
```bash
# Editar repo URL
sed -i 's/tu-usuario/TU_GITHUB_USERNAME/g' scripts/manual-deploy.sh

# Test deploy
./scripts/manual-deploy.sh status
```

---

## 🎉 **Recomendación Final**

**Para tu caso (servidor en red privada):**

### 🥇 **Usa Git Polling** 
- ✅ Ya está configurado
- ✅ Cero configuración adicional
- ✅ Deploy en máximo 5 minutos
- ✅ Completamente automático

### 🔄 **Backup con Manual Deploy**
- ✅ Para deploys urgentes
- ✅ Para rollbacks rápidos
- ✅ Para debugging

**¡Tu CI/CD ya está funcionando! Solo haz commits y en máximo 5 minutos se desplegará automáticamente!** 🚀

---

## 📞 **Troubleshooting**

### Jenkins no detecta cambios
```bash
# Verificar configuración polling
curl http://localhost:8090/job/medihelp360-pipeline/config.xml | grep pollSCM

# Ver logs de polling
curl http://localhost:8090/job/medihelp360-pipeline/scmPollLog/
```

### Manual deploy falla
```bash
# Ver status detallado
./scripts/manual-deploy.sh status

# Ejecutar rollback
./scripts/manual-deploy.sh rollback
```

### SSH Tunnel problemas
```bash
# Test de conexión
./scripts/setup-reverse-tunnel.sh VPS_IP usuario test

# Ver logs del túnel
sudo journalctl -u jenkins-tunnel -f  # Linux
tail -f ~/Library/Logs/jenkins-tunnel.log  # macOS
``` 