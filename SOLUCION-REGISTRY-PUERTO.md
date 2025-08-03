# 🔧 Solución: Puerto 5000 Ocupado en macOS

## 🚨 **Problema Identificado**
El puerto 5000 está ocupado por **macOS ControlCenter (AirPlay)**, por lo que no podemos usar ese puerto para nuestro Docker Registry.

## ✅ **Solución Implementada**

### **1. Registry Funcionando en Puerto 5001**
```bash
# Registry ahora corre en puerto 5001
docker run -d -p 5001:5000 --name local-registry --restart=unless-stopped registry:2

# Verificar que funciona
curl http://localhost:5001/v2/
```

### **2. Actualizar Configuraciones**

Necesitas cambiar `localhost:5000` por `localhost:5001` en estos archivos:

#### **Jenkinsfile**
```groovy
environment {
    DOCKER_REGISTRY = 'localhost:5001'  // Cambiar de 5000 a 5001
    // ... resto igual
}
```

#### **scripts/deploy.sh**
```bash
DOCKER_REGISTRY=${DOCKER_REGISTRY:-localhost:5001}  # Cambiar de 5000 a 5001
```

#### **scripts/manual-deploy.sh**
```bash
# Cambiar todas las referencias de localhost:5000 a localhost:5001
# En las líneas donde dice:
cd api-gateway && docker build -t localhost:5001/medihelp360-api-gateway:$BUILD_TAG . && ...
```

#### **start-medihelp360.sh**
```bash
# Cambiar las referencias de localhost:5000 a localhost:5001
docker build -t "localhost:5001/medihelp360-$service:latest" .
```

### **3. Comandos de Actualización Rápida**

```bash
# Actualizar Jenkinsfile
sed -i '' 's/localhost:5000/localhost:5001/g' Jenkinsfile

# Actualizar scripts
sed -i '' 's/localhost:5000/localhost:5001/g' scripts/deploy.sh
sed -i '' 's/localhost:5000/localhost:5001/g' scripts/manual-deploy.sh
sed -i '' 's/localhost:5000/localhost:5001/g' start-medihelp360.sh

# Verificar cambios
grep -r "localhost:500" . --exclude-dir=.git
```

### **4. URLs Actualizadas**

- 📦 **Registry API**: http://localhost:5001/v2/
- 📦 **Registry UI**: http://localhost:5001 (si está configurado)

## 🎯 **Alternativa: Deshabilitar AirPlay en macOS**

Si prefieres usar el puerto 5000 original:

1. **System Preferences** → **Sharing**
2. Desmarca **"AirPlay Receiver"**
3. Reinicia y usa puerto 5000

## ✅ **Estado Actual**

- ✅ Registry funcionando en puerto 5001
- ⚠️ Necesita actualizar configuraciones
- ✅ Sin conflictos de puertos

## 🚀 **Próximos Pasos**

1. Ejecutar comandos de actualización arriba
2. Rebuildar imágenes con nuevo registry
3. Continuar con deployment normal 