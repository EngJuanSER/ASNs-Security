# Actualización Backend: Integración Nmap + NVD

## 🔄 Cambios Realizados (Noviembre 2025)

Este documento describe las modificaciones realizadas al backend para **reemplazar Censys/BigQuery por Nmap y NVD API**, usando únicamente fuentes de datos gratuitas y open source.

---

## 📦 Nuevos Servicios Implementados

### 1. NmapService.java
**Propósito**: Ejecutar escaneos de puertos y detectar servicios.

**Funcionalidades**:
- Ejecuta `nmap -sV` para detección de versiones
- Parsea XML de salida
- Extrae: puerto, protocolo, servicio, versión, banner
- Construye CPE 2.3 para correlación con NVD
- Calcula nivel de riesgo por puerto

**Ejemplo de uso**:
```java
@Inject
NmapService nmapService;

List<ServiceDto> services = nmapService.scanTarget("45.33.32.156");
// Retorna servicios detectados con versiones
```

**Configuración** (`application.yml`):
```yaml
nmap:
  timeout: 60
  ports: "22,80,443,8080,3306,5432"
  temp-dir: "/tmp"
```

---

### 2. NVDService.java
**Propósito**: Consultar vulnerabilidades desde NVD (National Vulnerability Database).

**Funcionalidades**:
- Cliente HTTP para NVD API 2.0
- Búsqueda por CPE 2.3
- Parsing de CVSS (v3.1, v3.0, v2.0)
- Extrae CVE ID, severidad, descripción, referencias
- Soporte para API key (mejora rate limits)

**Ejemplo de uso**:
```java
@Inject
NVDService nvdService;

String cpe = "cpe:2.3:a:apache:http_server:2.4.7:*:*:*:*:*:*:*";
List<VulnerabilityDto> vulns = nvdService.searchByCpe(cpe);
// Retorna CVEs asociados a esa versión de Apache
```

**Configuración** (`application.yml`):
```yaml
nvd:
  api-key: ""  # Opcional - mejora límites
  timeout-seconds: 30
  results-per-page: 20
  cache-ttl-minutes: 60
```

---

### 3. ASNService.java
**Propósito**: Obtener información de ASN, ISP y organización.

**Funcionalidades**:
- Consulta ip-api.com (gratuito, 45 req/min)
- Fallback a whois
- Complementa GeoLite2 con datos de red

**Ejemplo de uso**:
```java
@Inject
ASNService asnService;

ASNService.ASNInfo info = asnService.getASNInfo("8.8.8.8");
// info.asn = "AS15169"
// info.isp = "Google LLC"
// info.asnOrg = "Google LLC"
```

---

## 🔧 Servicio Actualizado

### AnalysisService.java (Refactorizado)

**Antes**: Usaba datos mock estáticos
**Ahora**: Integra servicios reales

**Flujo actualizado**:
```
1. NmapService.scanTarget(ip)
   ↓
2. Para cada servicio detectado:
   - Construye CPE
   - NVDService.searchByCpe(cpe)
   - Asocia CVEs al servicio
   ↓
3. GeolocationService.resolve(ip)
   ↓
4. ASNService.getASNInfo(ip)
   → Completa geo.asn, geo.isp, geo.org
   ↓
5. SecurityScoringService.calculateScore(...)
   ↓
6. RecommendationService.generateRecommendations(...)
   ↓
7. metadata.sourcesUsed = ["nmap", "nvd", "geolite2", "ipapi"]
```

**Cambios clave**:
- Línea 42: `nmapService.scanTarget()` reemplaza servicios mock
- Línea 48-75: Loop que correlaciona servicios con CVEs via NVD
- Línea 90-93: Integración de ASNService con GeolocationService
- Línea 120: metadata actualizado con nuevas fuentes

---

## 📝 Configuración

### application.yml (Actualizado)

Se agregaron tres nuevas secciones:

```yaml
# Nueva configuración para Nmap
nmap:
  timeout: 60
  ports: "22,80,443,8080,3306,5432,6379,27017,21,23,25,53,110,143,445,3389"
  temp-dir: "/tmp"

# Nueva configuración para NVD API
nvd:
  api-key: ""  # Opcional: https://nvd.nist.gov/developers/request-an-api-key
  timeout-seconds: 30
  results-per-page: 20
  cache-ttl-minutes: 60

# GeoLite2 (sin cambios)
geolite2:
  city-db: "geo/GeoLite2-City.mmdb"
```

---

## 🚀 Requisitos para Ejecución

### Dependencias del Sistema

1. **Nmap** debe estar instalado:
```bash
# Linux (Debian/Ubuntu)
sudo apt-get install nmap

# Linux (Fedora/RHEL)
sudo dnf install nmap

# macOS
brew install nmap

# Verificar instalación
nmap --version
```

2. **GeoLite2 City Database**:
- Descargar de: https://dev.maxmind.com/geoip/geolite2-free-geolocation-data
- Colocar en: `src/main/resources/geo/GeoLite2-City.mmdb`

3. **Java 21** y **Gradle 8.x**

### Opcional: API Key de NVD

Sin API key: **5 requests cada 30 segundos**
Con API key: **50 requests cada 30 segundos**

Obtener en: https://nvd.nist.gov/developers/request-an-api-key

Agregar a `application.yml`:
```yaml
nvd:
  api-key: "tu-clave-aqui"
```

---

## 🧪 Pruebas

### 1. Probar Nmap manualmente
```bash
nmap -sV -p 22,80,443 --open -T4 -oX /tmp/test.xml scanme.nmap.org
cat /tmp/test.xml
```

### 2. Probar NVD API
```bash
curl "https://services.nvd.nist.gov/rest/json/cves/2.0?cpeName=cpe:2.3:a:apache:http_server:2.4.7:*:*:*:*:*:*:*&resultsPerPage=5"
```

### 3. Ejecutar backend
```bash
./gradlew clean build
./gradlew quarkusDev
```

### 4. Probar endpoint
```bash
curl -X POST http://localhost:8080/api/analysis/analyze \
  -H "Content-Type: application/json" \
  -d '{"query": "45.33.32.156", "type": "ipv4"}'
```

**IP de prueba recomendada**: `45.33.32.156` (scanme.nmap.org - autorizado para escaneo público)

---

## 📊 Respuesta Esperada

```json
{
  "ip": "45.33.32.156",
  "type": "ipv4",
  "securityScore": 75,
  "riskLevel": "medium",
  "services": [
    {
      "port": 22,
      "protocol": "tcp",
      "service": "ssh",
      "version": "OpenSSH 6.6.1p1",
      "vulnerabilities": ["CVE-2015-5600", "CVE-2016-6210"],
      "riskLevel": "medium"
    },
    {
      "port": 80,
      "protocol": "tcp",
      "service": "http",
      "version": "Apache httpd 2.4.7",
      "vulnerabilities": ["CVE-2017-3167", "CVE-2017-3169"],
      "riskLevel": "medium"
    }
  ],
  "geolocation": {
    "country": "United States",
    "city": "Fremont",
    "asn": "AS63949",
    "isp": "Linode LLC"
  },
  "vulnerabilities": [
    {
      "id": "CVE-2015-5600",
      "severity": "high",
      "cvss": 8.5,
      "description": "MaxAuthTries limit bypass..."
    }
  ],
  "metadata": {
    "scanDuration": 23456,
    "sourcesUsed": ["nmap", "nvd", "geolite2", "ipapi"]
  }
}
```

---

## ⚠️ Consideraciones Importantes

### Seguridad y Ética
- ⚠️ **Solo escanear IPs autorizadas**
- ✅ Usar `scanme.nmap.org` para pruebas
- ❌ No escanear sin permiso (ilegal en muchas jurisdicciones)

### Performance
- Escaneos toman **15-60 segundos** dependiendo de la IP
- NVD API puede tener latencia variable
- Considerar implementar cache para resultados

### Rate Limits
| Servicio | Sin API Key | Con API Key |
|----------|-------------|-------------|
| NVD API | 5 req/30s | 50 req/30s |
| ip-api.com | 45 req/min | N/A |

### Permisos
- Algunos escaneos Nmap requieren **root/sudo**
- `-sS` (SYN scan) requiere privilegios
- `-sV` (version detection) puede requerir privilegios para ciertos puertos

---

## 🐛 Troubleshooting

### Error: "nmap: command not found"
**Solución**: Instalar Nmap (ver sección Requisitos)

### Error: "GeoLite2 City DB no encontrada"
**Solución**: Descargar y colocar en `src/main/resources/geo/GeoLite2-City.mmdb`

### Error: "NVD API rate limit exceeded"
**Solución**: 
- Esperar 30 segundos
- Obtener API key gratuita
- Implementar cache

### Escaneos muy lentos
**Solución**:
- Reducir lista de puertos en `application.yml`
- Aumentar timeout: `nmap.timeout: 120`
- Usar `-T4` o `-T3` (ya configurado por defecto)

### Sin resultados de vulnerabilidades
**Posibles causas**:
- Servicio no tiene CPE mapeado (verificar logs)
- Versión muy antigua/nueva sin CVEs registrados
- NVD API temporalmente no disponible (verificar con curl manual)

---

## 📚 Referencias

- [Nmap Official Documentation](https://nmap.org/book/)
- [NVD API Documentation](https://nvd.nist.gov/developers/vulnerabilities)
- [CPE Specification](https://nvd.nist.gov/products/cpe)
- [MaxMind GeoLite2](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data)
- [ip-api.com Docs](https://ip-api.com/docs)

---

## 📈 Comparación con Versión Anterior

| Aspecto | Antes (Mock/Censys) | Ahora (Nmap/NVD) |
|---------|---------------------|------------------|
| Servicios | Mock estático | ✅ Detección real |
| Vulnerabilidades | 1 CVE hardcoded | ✅ Base NVD completa |
| Costo | Requiere permisos | ✅ 100% gratuito |
| Precisión | N/A | ✅ Datos en tiempo real |
| ASN/ISP | "Unknown" | ✅ Resolución real |

---

## 🎯 Mejoras Futuras Sugeridas

- [ ] Implementar cache con Redis para resultados
- [ ] Agregar WebSockets para escaneos asíncronos
- [ ] Soporte para escaneo de múltiples IPs (batch)
- [ ] Métricas de performance con Micrometer
- [ ] Rate limiting interno para proteger APIs externas
- [ ] Logs estructurados (JSON) para análisis

---

**Última actualización**: Noviembre 2025
**Autores**: EngJuanSER y NicoG2023
**Proyecto**: DiagSEG - ASNs Security Analysis
