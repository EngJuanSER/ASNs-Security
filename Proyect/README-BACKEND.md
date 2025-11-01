# DiagSEG - Especificación de API Backend

## Tabla de Contenidos
1. [Introducción](#introducción)
2. [Fuentes de Datos](#fuentes-de-datos)
3. [Endpoint Principal](#endpoint-principal)
4. [Modelos de Datos](#modelos-de-datos)
5. [Cálculo del Security Score](#cálculo-del-security-score)
6. [Flujo de Datos en el Frontend](#flujo-de-datos-en-el-frontend)
7. [Validación y Errores](#validación-y-errores)

---

## Introducción

Este documento especifica la API REST que el backend debe implementar para soportar la aplicación **DiagSEG**, una herramienta de diagnóstico de seguridad para direcciones IP y ASNs.

El frontend está completamente implementado en Vue 3 con TypeScript y consume un único endpoint principal. El objetivo de este documento es proporcionar toda la información necesaria para construir el backend sin requerir cambios en el frontend.

### Contexto del Frontend

El frontend incluye tres vistas principales:

1. **Analysis** (`src/views/Analysis.vue`): Análisis individual de IP/ASN con visualización completa de resultados
2. **IPComparator** (`src/views/IPComparator.vue`): Comparación lado a lado de dos análisis
3. **History** (`src/views/History.vue`): Historial de análisis con estadísticas y gráficos

El frontend maneja:
- Caché local con LocalForage (almacena resultados por 7 días)
- Temas claro/oscuro
- Estados de carga y errores
- Validación de entrada
- Visualizaciones (gráficos, mapas, tablas)

---

## Fuentes de Datos

El backend NO utiliza APIs externas. Los datos provienen de:

### 1. Base de Datos GeoLite2

**Fuente:** MaxMind GeoLite2 (base de datos descargada localmente)

**Información a extraer:**
- País, región, ciudad
- Coordenadas geográficas (latitud, longitud)
- Zona horaria
- Código de país (ISO)

### 2. Google BigQuery - Censys Dataset

**Fuente:** Dataset público de Censys en Google BigQuery

**Información a extraer:**
- Puertos abiertos
- Servicios detectados (HTTP, SSH, DNS, etc.)
- Banners de servicios
- Versiones de software
- Protocolos (TCP/UDP)
- ASN (Autonomous System Number)
- ISP y organización
- Vulnerabilidades conocidas asociadas a servicios/versiones

**Tablas relevantes:**
- `censys-io.universal_internet_dataset.scan_results`
- Información de servicios por IP
- Histórico de escaneos

### Proceso de Análisis

1. Usuario envía IP o ASN
2. Backend busca en GeoLite2 para geolocalización
3. Backend consulta BigQuery Censys para servicios y vulnerabilidades
4. Backend calcula security score basado en factores de riesgo
5. Backend genera recomendaciones de seguridad
6. Backend retorna resultado completo en formato JSON

---

## Endpoint Principal

### Base URL

```
http://localhost:8080/api
```

### POST /api/analysis/analyze

Este es el único endpoint que consume el frontend. Se utiliza en:

**Archivo:** `src/services/api.ts`
**Línea:** 45
**Función:** `analyzeTarget(query: string, type: TargetType): Promise<AnalysisResult>`

**Vistas que lo consumen:**
1. `src/views/Analysis.vue` (línea 280) - Análisis individual
2. `src/views/IPComparator.vue` (línea 350) - Comparación de dos objetivos

### Request

**Método:** POST
**Content-Type:** application/json

**Body:**
```json
{
  "query": "8.8.8.8",
  "type": "ipv4"
}
```

**Campos del Request:**

| Campo | Tipo | Requerido | Descripción | Valores válidos |
|-------|------|-----------|-------------|-----------------|
| query | string | Sí | IP o ASN a analizar | IPv4, IPv6, ASN (formato AS#####) |
| type | string | Sí | Tipo de objetivo | "ipv4", "ipv6", "asn" |

**Ejemplos de queries válidas:**

```json
// IPv4
{"query": "8.8.8.8", "type": "ipv4"}

// IPv6
{"query": "2001:4860:4860::8888", "type": "ipv6"}

// ASN
{"query": "AS15169", "type": "asn"}
```

### Response Exitosa (200 OK)

**Content-Type:** application/json

**Estructura completa:**

```json
{
  "ip": "8.8.8.8",
  "type": "ipv4",
  "securityScore": 85,
  "riskLevel": "low",
  "timestamp": 1698789600000,
  "services": [
    {
      "port": 53,
      "protocol": "udp",
      "service": "dns",
      "version": "Google Public DNS",
      "banner": "DNS Server",
      "vulnerabilities": [],
      "riskLevel": "low"
    },
    {
      "port": 443,
      "protocol": "tcp",
      "service": "https",
      "version": "nginx/1.18.0",
      "banner": "Server: nginx/1.18.0",
      "vulnerabilities": ["CVE-2021-23017"],
      "riskLevel": "medium"
    }
  ],
  "geolocation": {
    "country": "United States",
    "countryCode": "US",
    "region": "California",
    "city": "Mountain View",
    "latitude": 37.4056,
    "longitude": -122.0775,
    "timezone": "America/Los_Angeles",
    "isp": "Google LLC",
    "asn": "AS15169",
    "org": "Google LLC"
  },
  "reputation": [
    {
      "source": "censys",
      "name": "Censys Dataset",
      "status": "clean",
      "statusText": "Clean",
      "score": 100,
      "details": "No malicious activity detected in scan history",
      "lastChecked": 1698789600000
    }
  ],
  "vulnerabilities": [
    {
      "id": "CVE-2021-23017",
      "title": "nginx resolver DNS response",
      "severity": "medium",
      "cvss": 5.6,
      "description": "A security issue in nginx resolver was identified, which might allow an attacker who is able to forge UDP packets from the DNS server",
      "solution": "Update nginx to version 1.20.1 or later",
      "references": [
        "https://nvd.nist.gov/vuln/detail/CVE-2021-23017",
        "https://nginx.org/en/security_advisories.html"
      ]
    }
  ],
  "recommendations": [
    {
      "title": "Actualizar nginx",
      "description": "La versión actual de nginx (1.18.0) tiene vulnerabilidades conocidas. Actualizar a la versión 1.20.1 o superior.",
      "priority": "high",
      "category": "service"
    },
    {
      "title": "Configurar Rate Limiting",
      "description": "Implementar límites de tasa en el servidor DNS para prevenir ataques de amplificación DNS",
      "priority": "medium",
      "category": "network"
    }
  ],
  "metadata": {
    "scanDuration": 2341,
    "sourcesUsed": ["censys", "geolite2"],
    "cached": false
  }
}
```

### Response de Error

**400 Bad Request - Query inválida:**
```json
{
  "error": "Invalid IP address or ASN",
  "message": "The provided query '256.256.256.256' is not a valid IPv4 address",
  "code": "INVALID_INPUT"
}
```

**404 Not Found - No se encontró información:**
```json
{
  "error": "No data found",
  "message": "No information available for the provided query",
  "code": "NOT_FOUND"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred while processing the request",
  "code": "INTERNAL_ERROR"
}
```

### Manejo de Errores en Frontend

El servicio `api.ts` espera estas respuestas:

```typescript
// src/services/api.ts - Líneas 45-70
export const analyzeTarget = async (query: string, type: TargetType): Promise<AnalysisResult> => {
  try {
    const response = await fetch(`${API_BASE_URL}/analysis/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query, type })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Analysis failed');
    }

    return await response.json();
  } catch (error) {
    console.error('Analysis error:', error);
    throw error;
  }
};
```

Las vistas manejan errores mostrando mensajes al usuario y habilitando reintento.

---

## Modelos de Datos

Esta sección describe en detalle cada modelo de datos que el backend debe retornar. Cada campo es utilizado por componentes específicos del frontend.

### AnalysisResult (Objeto Principal)

Este es el objeto raíz que retorna el endpoint. Contiene toda la información del análisis.

```typescript
interface AnalysisResult {
  ip: string;
  type: 'ipv4' | 'ipv6' | 'asn';
  securityScore: number;
  riskLevel: 'low' | 'medium' | 'high';
  timestamp: number;
  services: Service[];
  geolocation: Geolocation;
  reputation: ReputationSource[];
  vulnerabilities: Vulnerability[];
  recommendations: Recommendation[];
  metadata: AnalysisMetadata;
}
```

**Campos:**

| Campo | Tipo | Descripción | Ejemplo | Usado en |
|-------|------|-------------|---------|----------|
| ip | string | IP o ASN analizado | "8.8.8.8" | `Analysis.vue`, `ComparisonSummaryCard.vue` |
| type | string | Tipo de objetivo | "ipv4" | `Analysis.vue` |
| securityScore | number | Score de seguridad (0-100) | 85 | `Analysis.vue` (indicador principal), `ComparisonSummaryCard.vue` |
| riskLevel | string | Nivel de riesgo calculado | "low" | `Analysis.vue`, `ComparisonSummaryCard.vue`, `History.vue` |
| timestamp | number | Timestamp Unix en milisegundos | 1698789600000 | `History.vue`, `HistoryTable.vue` |
| services | array | Lista de servicios detectados | [...] | `ServicesComparison.vue`, `Analysis.vue` |
| geolocation | object | Información geográfica | {...} | `GeolocationMap.vue`, `MapsComparison.vue` |
| reputation | array | Fuentes de reputación | [...] | `ReputationComparison.vue`, `Analysis.vue` |
| vulnerabilities | array | Vulnerabilidades encontradas | [...] | `Analysis.vue` |
| recommendations | array | Recomendaciones de seguridad | [...] | `RecommendationsComparison.vue`, `Analysis.vue` |
| metadata | object | Metadatos del análisis | {...} | `Analysis.vue` |

**Componentes que procesan AnalysisResult completo:**
- `src/views/Analysis.vue` - Muestra todas las secciones
- `src/views/IPComparator.vue` - Compara dos análisis
- `src/components/ComparisonSummaryCard.vue` - Card resumen

---

### Service (Servicio Detectado)

Representa un servicio detectado en la IP analizada.

```typescript
interface Service {
  port: number;
  protocol: 'tcp' | 'udp';
  service: string;
  version?: string;
  banner?: string;
  vulnerabilities: string[];
  riskLevel: 'low' | 'medium' | 'high';
}
```

**Campos:**

| Campo | Tipo | Requerido | Descripción | Ejemplo |
|-------|------|-----------|-------------|---------|
| port | number | Sí | Número de puerto | 443 |
| protocol | string | Sí | Protocolo de transporte | "tcp" |
| service | string | Sí | Nombre del servicio | "https" |
| version | string | No | Versión del software | "nginx/1.18.0" |
| banner | string | No | Banner del servicio | "Server: nginx/1.18.0" |
| vulnerabilities | array | Sí | IDs de CVEs asociados | ["CVE-2021-23017"] |
| riskLevel | string | Sí | Nivel de riesgo | "medium" |

**Valores comunes de service:**
- "http", "https" - Servidores web
- "ssh" - Secure Shell
- "ftp", "ftps" - File Transfer Protocol
- "dns" - Domain Name System
- "smtp" - Mail server
- "mysql", "postgresql" - Bases de datos
- "rdp" - Remote Desktop
- "telnet" - Telnet (alto riesgo)

**Cálculo de riskLevel del servicio:**
- **high**: Puerto conocido como inseguro (Telnet 23, FTP 21) o tiene vulnerabilidades críticas
- **medium**: Servicio con vulnerabilidades conocidas o versión desactualizada
- **low**: Servicio seguro y actualizado

**Usado en:**
- `src/components/ServicesComparison.vue` - Tabla comparativa de servicios
- `src/views/Analysis.vue` - Lista de servicios con badges de riesgo

**Renderizado en frontend:**

<!-- src/components/ServicesComparison.vue - Línea 120 -->
<div v-for="service in result1?.services" :key="service.port">
  <div class="service-port">{{ service.port }}/{{ service.protocol }}</div>
  <div class="service-name">{{ service.service }}</div>
  <div class="service-version">{{ service.version || 'Unknown' }}</div>
  <span :class="['risk-badge', service.riskLevel]">
    {{ service.riskLevel }}
  </span>
</div>

---

### Geolocation (Información Geográfica)

Información de ubicación y red del objetivo.

```typescript
interface Geolocation {
  country: string;
  countryCode: string;
  region: string;
  city: string;
  latitude: number;
  longitude: number;
  timezone: string;
  isp: string;
  asn: string;
  org: string;
}
```

**Campos:**

| Campo | Tipo | Requerido | Descripción | Ejemplo | Fuente |
|-------|------|-----------|-------------|---------|--------|
| country | string | Sí | Nombre del país | "United States" | GeoLite2 |
| countryCode | string | Sí | Código ISO 3166-1 | "US" | GeoLite2 |
| region | string | Sí | Estado/provincia | "California" | GeoLite2 |
| city | string | Sí | Ciudad | "Mountain View" | GeoLite2 |
| latitude | number | Sí | Latitud (-90 a 90) | 37.4056 | GeoLite2 |
| longitude | number | Sí | Longitud (-180 a 180) | -122.0775 | GeoLite2 |
| timezone | string | Sí | Zona horaria IANA | "America/Los_Angeles" | GeoLite2 |
| isp | string | Sí | Proveedor de servicios | "Google LLC" | Censys/BigQuery |
| asn | string | Sí | ASN con formato AS##### | "AS15169" | Censys/BigQuery |
| org | string | Sí | Organización propietaria | "Google LLC" | Censys/BigQuery |

**Importante:**
- Las coordenadas son usadas para renderizar mapas con Leaflet
- El formato de `asn` debe ser "AS" seguido del número (AS15169, no solo 15169)
- `timezone` debe ser un identificador válido de la base de datos IANA

**Usado en:**
- `src/components/GeolocationMap.vue` - Mapa interactivo con Leaflet
- `src/components/MapsComparison.vue` - Comparación de dos mapas
- `src/components/NetworkInfoComparison.vue` - Información de red

**Renderizado en frontend:**

```vue
<!-- src/components/NetworkInfoComparison.vue - Línea 85 -->
<div class="network-item">
  <strong>ASN:</strong>
  <span>{{ result1?.geolocation.asn }}</span>
</div>
<div class="network-item">
  <strong>ISP:</strong>
  <span>{{ result1?.geolocation.isp }}</span>
</div>
<div class="network-item">
  <strong>Organization:</strong>
  <span>{{ result1?.geolocation.org }}</span>
</div>
<div class="network-item">
  <strong>Timezone:</strong>
  <span>{{ result1?.geolocation.timezone }}</span>
</div>

<!-- src/components/GeolocationMap.vue - Línea 45 -->
<l-marker :lat-lng="[geolocation.latitude, geolocation.longitude]">
  <l-popup>
    <strong>{{ geolocation.city }}, {{ geolocation.country }}</strong><br>
    {{ geolocation.org }}
  </l-popup>
</l-marker>
```

---

### ReputationSource (Fuente de Reputación)

Información de reputación de la IP desde diferentes fuentes.

```typescript
interface ReputationSource {
  source: string;
  name: string;
  status: 'clean' | 'suspicious' | 'malicious' | 'unknown';
  statusText: string;
  score: number;
  details: string;
  lastChecked: number;
}
```

**Campos:**

| Campo | Tipo | Requerido | Descripción | Ejemplo |
|-------|------|-----------|-------------|---------|
| source | string | Sí | ID de la fuente | "censys" |
| name | string | Sí | Nombre legible | "Censys Dataset" |
| status | string | Sí | Estado de reputación | "clean" |
| statusText | string | Sí | Texto del estado | "Clean" |
| score | number | Sí | Score 0-100 (100=mejor) | 95 |
| details | string | Sí | Descripción detallada | "No malicious activity detected" |
| lastChecked | number | Sí | Timestamp de verificación | 1698789600000 |

**Valores de status:**

| Status | Descripción | Color en UI | Cuándo usar |
|--------|-------------|-------------|-------------|
| clean | IP limpia sin reportes | Verde | No hay actividad sospechosa |
| suspicious | Actividad sospechosa | Amarillo | Algunos indicadores de riesgo |
| malicious | IP maliciosa confirmada | Rojo | Actividad maliciosa confirmada |
| unknown | Sin información suficiente | Gris | No hay datos disponibles |

**Generación de reputation desde Censys:**

Dado que no hay APIs externas de reputación, el backend debe generar esta información analizando:

1. **Puertos peligrosos abiertos:**
   - Telnet (23), FTP (21) sin cifrado → suspicious
   - Múltiples puertos administrativos expuestos → suspicious

2. **Servicios con vulnerabilidades:**
   - 1-2 vulnerabilidades críticas → suspicious
   - 3+ vulnerabilidades críticas → malicious

3. **Patrones de escaneo:**
   - Si la IP aparece en múltiples escaneos con servicios variando → suspicious

4. **Score calculation:**
   - Empezar con 100
   - Restar puntos por cada factor de riesgo
   - Convertir a status según score final

**Usado en:**
- `src/components/ReputationComparison.vue` - Comparación de reputación
- `src/views/Analysis.vue` - Sección de reputation

**Renderizado en frontend:**

<!-- src/components/ReputationComparison.vue - Línea 95 -->
<div v-for="rep in result1?.reputation" :key="rep.source" class="reputation-source">
  <div class="source-header">
    <span class="source-name">{{ rep.name }}</span>
    <span :class="['status-badge', rep.status]">
      {{ rep.statusText }}
    </span>
  </div>
  <div class="reputation-score">Score: {{ rep.score }}/100</div>
  <div class="reputation-details">{{ rep.details }}</div>
  <div class="last-checked">
    Última verificación: {{ formatDate(rep.lastChecked) }}
  </div>
</div>

---

### Vulnerability (Vulnerabilidad)

Vulnerabilidad de seguridad detectada en servicios.

```typescript
interface Vulnerability {
  id: string;
  title: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  cvss: number;
  description: string;
  solution: string;
  references: string[];
}
```

**Campos:**

| Campo | Tipo | Requerido | Descripción | Ejemplo |
|-------|------|-----------|-------------|---------|
| id | string | Sí | CVE ID oficial | "CVE-2021-23017" |
| title | string | Sí | Título breve | "nginx resolver DNS response" |
| severity | string | Sí | Severidad | "medium" |
| cvss | number | Sí | Score CVSS 3.0 (0-10) | 5.6 |
| description | string | Sí | Descripción técnica | "A security issue in nginx resolver..." |
| solution | string | Sí | Mitigación/solución | "Update nginx to version 1.20.1" |
| references | array | Sí | URLs de referencia | ["https://nvd.nist.gov/..."] |

**Mapeo de CVSS a severity:**

| CVSS Score | Severity |
|------------|----------|
| 9.0 - 10.0 | critical |
| 7.0 - 8.9 | high |
| 4.0 - 6.9 | medium |
| 0.1 - 3.9 | low |

**Obtención de vulnerabilidades:**

Las vulnerabilidades se obtienen correlacionando servicios detectados con bases de datos de CVEs:

1. **Desde Censys/BigQuery:**
   - Buscar el servicio y versión en la tabla de vulnerabilidades
   - Ejemplo: nginx 1.18.0 → buscar CVEs conocidos para esa versión

2. **Fuentes de CVE:**
   - NVD (National Vulnerability Database)
   - CVE.org
   - Vendor-specific advisories

3. **Proceso:**
   ```
   Para cada servicio:
     1. Extraer nombre y versión
     2. Buscar en base de datos local de CVEs
     3. Filtrar por versión afectada
     4. Ordenar por CVSS descendente
     5. Incluir en resultado
   ```

**Usado en:**
- `src/views/Analysis.vue` - Lista de vulnerabilidades con severidad

**Renderizado en frontend:**

```vue
<!-- src/views/Analysis.vue - Línea 450 -->
<div v-if="analysisResult?.vulnerabilities.length > 0" class="vulnerabilities-section">
  <h3>Vulnerabilidades Detectadas</h3>
  <div v-for="vuln in analysisResult.vulnerabilities" :key="vuln.id" class="vulnerability-card">
    <div class="vuln-header">
      <span class="vuln-id">{{ vuln.id }}</span>
      <span :class="['severity-badge', vuln.severity]">
        {{ vuln.severity }}
      </span>
      <span class="cvss-score">CVSS: {{ vuln.cvss }}</span>
    </div>
    <h4>{{ vuln.title }}</h4>
    <p class="vuln-description">{{ vuln.description }}</p>
    <div class="vuln-solution">
      <strong>Solución:</strong> {{ vuln.solution }}
    </div>
    <div class="vuln-references">
      <a v-for="ref in vuln.references" :key="ref" :href="ref" target="_blank">
        {{ ref }}
      </a>
    </div>
  </div>
</div>
```

---

### Recommendation (Recomendación)

Recomendaciones de seguridad generadas por el análisis.

```typescript
interface Recommendation {
  title: string;
  description: string;
  priority: 'low' | 'medium' | 'high';
  category: 'network' | 'service' | 'configuration' | 'security';
}
```

**Campos:**

| Campo | Tipo | Requerido | Descripción | Ejemplo |
|-------|------|-----------|-------------|---------|
| title | string | Sí | Título breve | "Actualizar nginx" |
| description | string | Sí | Descripción detallada | "La versión actual tiene vulnerabilidades..." |
| priority | string | Sí | Prioridad de acción | "high" |
| category | string | Sí | Categoría | "service" |

**Generación de Recomendaciones:**

Las recomendaciones se generan automáticamente basándose en el análisis:

1. **Por Vulnerabilidades:**
   ```
   Si hay vulnerabilidad crítica/alta:
     Título: "Actualizar [servicio]"
     Descripción: "La versión X tiene vulnerabilidad [CVE]. Actualizar a versión Y"
     Priority: critical → high, high → high, medium → medium
     Category: "service"
   ```

2. **Por Puertos Inseguros:**
   ```
   Si puerto 23 (Telnet) abierto:
     Título: "Desactivar Telnet"
     Descripción: "Telnet transmite datos sin cifrar. Usar SSH en su lugar"
     Priority: "high"
     Category: "configuration"
   
   Si puerto 21 (FTP) abierto:
     Título: "Migrar a FTPS o SFTP"
     Descripción: "FTP no cifra credenciales. Usar alternativas seguras"
     Priority: "high"
     Category: "configuration"
   ```

3. **Por Número de Servicios:**
   ```
   Si más de 10 puertos abiertos:
     Título: "Reducir superficie de ataque"
     Descripción: "Se detectaron X servicios expuestos. Cerrar puertos innecesarios"
     Priority: "medium"
     Category: "network"
   ```

4. **Por Servicios Administrativos:**
   ```
   Si puerto 3389 (RDP) o 22 (SSH) abierto:
     Título: "Restringir acceso administrativo"
     Descripción: "Configurar firewall para limitar acceso a IPs confiables"
     Priority: "high"
     Category: "security"
   ```

5. **Por Score de Seguridad:**
   ```
   Si securityScore < 60:
     Título: "Revisión de seguridad completa"
     Descripción: "El score de seguridad es bajo. Realizar auditoría completa"
     Priority: "high"
     Category: "security"
   ```

**Usado en:**
- `src/components/RecommendationsComparison.vue` - Comparación de recomendaciones
- `src/views/Analysis.vue` - Lista de recomendaciones

**Renderizado en frontend:**

<!-- src/components/RecommendationsComparison.vue - Línea 110 -->
<div v-for="rec in result1?.recommendations" :key="rec.title" 
     :class="['recommendation-item', rec.priority]">
  <div class="rec-header">
    <h4>{{ rec.title }}</h4>
    <span :class="['priority-badge', rec.priority]">
      {{ rec.priority }}
    </span>
  </div>
  <p class="rec-description">{{ rec.description }}</p>
  <span class="rec-category">{{ rec.category }}</span>
</div>

---

### AnalysisMetadata (Metadatos)

Información técnica sobre el proceso de análisis.

```typescript
interface AnalysisMetadata {
  scanDuration: number;
  sourcesUsed: string[];
  cached: boolean;
}
```

**Campos:**

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| scanDuration | number | Duración en milisegundos | 2341 |
| sourcesUsed | array | Fuentes consultadas | ["censys", "geolite2"] |
| cached | boolean | Si el resultado es de caché | false |

**Valores de sourcesUsed:**
- "censys" - Dataset de Censys en BigQuery
- "geolite2" - Base de datos GeoLite2

**Usado en:**
- `src/views/Analysis.vue` - Footer con información del análisis

---

## Cálculo del Security Score

El `securityScore` es un valor entre 0 y 100 que indica el nivel de seguridad de la IP/ASN analizado. Un score de 100 indica máxima seguridad, mientras que 0 indica máximo riesgo.

### Algoritmo de Cálculo

El backend debe implementar un sistema de penalizaciones que comience en 100 y reste puntos según factores de riesgo:

```
securityScore = 100
              - penalizaciónPorReputación
              - penalizaciónPorPuertos
              - penalizaciónPorVulnerabilidades
              - penalizaciónPorServicios
```

### Factor 1: Puertos y Servicios (Peso: 30%)

**Puertos de alto riesgo:**

| Puerto | Servicio | Penalización | Razón |
|--------|----------|--------------|-------|
| 23 | Telnet | -15 | Sin cifrado, credenciales en texto plano |
| 21 | FTP | -10 | Credenciales sin cifrar |
| 445 | SMB | -8 | Vector común de ransomware |
| 135 | RPC | -8 | Vulnerable a ataques remotos |
| 3389 | RDP | -10 | Si está expuesto públicamente |
| 5900 | VNC | -10 | Acceso remoto sin seguridad adecuada |

**Puertos administrativos expuestos:**

| Puerto | Servicio | Penalización | Condición |
|--------|----------|--------------|-----------|
| 22 | SSH | -5 | Si no hay restricción de IPs |
| 3306 | MySQL | -8 | Base de datos expuesta |
| 5432 | PostgreSQL | -8 | Base de datos expuesta |
| 27017 | MongoDB | -10 | Historial de exposiciones |
| 6379 | Redis | -10 | Sin autenticación por defecto |

**Cantidad de puertos abiertos:**

```
Si puertos abiertos > 10: -5 puntos
Si puertos abiertos > 20: -10 puntos adicionales
Si puertos abiertos > 50: -15 puntos adicionales
```

### Factor 2: Vulnerabilidades (Peso: 40%)

**Por severidad de CVE:**

| Severity | CVSS Range | Penalización por CVE | Máximo |
|----------|------------|----------------------|--------|
| critical | 9.0-10.0 | -15 puntos | -30 |
| high | 7.0-8.9 | -10 puntos | -30 |
| medium | 4.0-6.9 | -5 puntos | -20 |
| low | 0.1-3.9 | -2 puntos | -10 |

**Ejemplo:**
```
Si hay 2 CVEs críticos y 3 altos:
Penalización = (2 × 15) + (3 × 10) = 30 + 30 = 60
Pero máximo de críticos es 30 y de altos es 30
Penalización real = 30 + 30 = 60 puntos
```

### Factor 3: Servicios Desactualizados (Peso: 20%)

**Servicios con versiones conocidas:**

```
Para cada servicio con versión detectada:
  1. Verificar si hay una versión más reciente
  2. Si está desactualizado:
     - Sin vulnerabilidades conocidas: -3 puntos
     - Con vulnerabilidades: ya penalizado en Factor 2
  3. Servicios críticos desactualizados (SSH, HTTPS): -5 puntos
```

**Servicios sin versión identificable:**

```
Si no se puede determinar la versión: -1 punto
(Indica posible ofuscación o configuración no estándar)
```

### Factor 4: Análisis de Patrones (Peso: 10%)

**Patrones sospechosos:**

```
1. Proxy/VPN detectado: -5 puntos
   (Dificulta trazabilidad)

2. Hosting compartido con múltiples servicios: -3 puntos
   (Mayor superficie de ataque)

3. Cambios frecuentes en servicios expuestos: -5 puntos
   (Posible uso en botnet)

4. Servicios en puertos no estándar: -2 puntos por servicio
   (Posible intento de evasión)
```

### Conversión a Risk Level

Una vez calculado el `securityScore`, se convierte a `riskLevel`:

```
if (securityScore >= 80):
    riskLevel = "low"
elif (securityScore >= 60):
    riskLevel = "medium"
else:
    riskLevel = "high"
```

**Rangos detallados:**

| Risk Level | Score Range | Descripción | Color en UI |
|------------|-------------|-------------|-------------|
| low | 80-100 | Riesgo bajo, configuración segura | Verde |
| medium | 60-79 | Riesgo moderado, requiere atención | Amarillo |
| high | 0-59 | Riesgo alto, requiere acción inmediata | Rojo |

### Ejemplo de Cálculo Completo

**Caso 1: Servidor Web Seguro**

```
IP: 1.2.3.4
Puertos abiertos: 80 (HTTP), 443 (HTTPS)
Servicios: nginx/1.24.0 (actualizado)
Vulnerabilidades: 0

Cálculo:
- Base: 100
- Puertos seguros: 0
- Vulnerabilidades: 0
- Servicios actualizados: 0
- Patrones normales: 0

securityScore = 100
riskLevel = "low"
```

**Caso 2: Servidor con Vulnerabilidades**

```
IP: 5.6.7.8
Puertos abiertos: 22, 80, 443, 3306
Servicios: 
  - SSH (OpenSSH 7.4) - desactualizado
  - HTTP (Apache 2.4.29) - con CVE-2021-44790 (high, CVSS 7.5)
  - MySQL expuesto en puerto 3306
Vulnerabilidades: 1 high

Cálculo:
- Base: 100
- Puerto 3306 expuesto: -8
- Vulnerabilidad high: -10
- Apache desactualizado: -5
- OpenSSH desactualizado: -5

securityScore = 100 - 8 - 10 - 5 - 5 = 72
riskLevel = "medium"
```

**Caso 3: Servidor Comprometido**

```
IP: 9.10.11.12
Puertos abiertos: 21, 23, 445, 3389, 5900, más 25 puertos adicionales
Servicios:
  - Telnet activo
  - FTP sin cifrar
  - RDP expuesto
  - SMB vulnerable
Vulnerabilidades: 2 critical, 4 high, 8 medium

Cálculo:
- Base: 100
- Telnet (23): -15
- FTP (21): -10
- SMB (445): -8
- RDP (3389): -10
- VNC (5900): -10
- Más de 20 puertos: -15
- 2 CVE critical: -30 (máximo)
- 4 CVE high: -30 (máximo)
- 8 CVE medium: -20 (máximo limitado)

securityScore = 100 - 15 - 10 - 8 - 10 - 10 - 15 - 30 - 30 = -28
securityScore = 0 (mínimo)
riskLevel = "high"
```

### Consideraciones de Implementación

1. **Límites:** El score nunca debe ser menor que 0 ni mayor que 100
2. **Redondeo:** Redondear a número entero
3. **Documentación:** Incluir en `metadata.scoringFactors` los factores que afectaron el score (opcional)
4. **Consistencia:** El mismo análisis debe producir el mismo score

---

## Flujo de Datos en el Frontend

Esta sección explica cómo el frontend procesa y muestra los datos retornados por el backend.

### 1. Servicio API (src/services/api.ts)

El frontend tiene un servicio centralizado que maneja todas las llamadas a la API:

```typescript
// Línea 5-10
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export type TargetType = 'ipv4' | 'ipv6' | 'asn';

// Línea 45-70
export const analyzeTarget = async (
  query: string, 
  type: TargetType
): Promise<AnalysisResult> => {
  const response = await fetch(`${API_BASE_URL}/analysis/analyze`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, type })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Analysis failed');
  }

  return await response.json();
};
```

**Configuración:**
- Variable de entorno `VITE_API_BASE_URL` en archivo `.env`
- Default: `http://localhost:8080/api`

### 2. Vista de Análisis (src/views/Analysis.vue)

**Flujo de análisis:**

```typescript
// Línea 50-80
const analyzeTarget = async () => {
  // 1. Validar entrada
  if (!selectedQuery.value || !targetType.value) {
    errorMessage.value = 'Por favor ingrese una IP o ASN válido';
    return;
  }

  // 2. Verificar caché local (LocalForage)
  const cacheKey = `analysis_${selectedQuery.value}`;
  const cached = await analysisCache.getItem(cacheKey);
  
  if (cached && Date.now() - cached.timestamp < 7 * 24 * 60 * 60 * 1000) {
    analysisResult.value = cached;
    isFromCache.value = true;
    return;
  }

  // 3. Mostrar estado de carga
  isLoading.value = true;
  errorMessage.value = null;

  try {
    // 4. Llamar al backend
    const result = await api.analyzeTarget(selectedQuery.value, targetType.value);
    
    // 5. Guardar en caché
    await analysisCache.setItem(cacheKey, {
      ...result,
      timestamp: Date.now()
    });

    // 6. Guardar en historial
    await addToHistory(result);

    // 7. Actualizar UI
    analysisResult.value = result;
    isFromCache.value = false;

  } catch (error) {
    // 8. Manejar error
    errorMessage.value = error.message;
  } finally {
    isLoading.value = false;
  }
};
```

**Componentes que renderizan el resultado:**

1. **Indicador Principal** (Línea 180-220):

<div class="security-score-card">
  <div :class="['score-circle', analysisResult.riskLevel]">
    {{ analysisResult.securityScore }}
  </div>
  <h2>{{ getRiskLevelText(analysisResult.riskLevel) }}</h2>
</div>


2. **Sección de Servicios** (Línea 250-300):

<div v-if="analysisResult.services.length > 0" class="services-section">
  <h3>Servicios Detectados ({{ analysisResult.services.length }})</h3>
  <div class="services-grid">
    <div v-for="service in analysisResult.services" 
         :key="`${service.port}-${service.protocol}`"
         class="service-card">
      <div class="service-header">
        <span class="port">{{ service.port }}/{{ service.protocol }}</span>
        <span :class="['risk-badge', service.riskLevel]">
          {{ service.riskLevel }}
        </span>
      </div>
      <div class="service-name">{{ service.service }}</div>
      <div class="service-version">{{ service.version || 'Unknown' }}</div>
      <div v-if="service.vulnerabilities.length > 0" class="vuln-count">
        {{ service.vulnerabilities.length }} vulnerabilidades
      </div>
    </div>
  </div>
</div>

3. **Mapa de Geolocalización** (Línea 320-330):

<GeolocationMap 
  v-if="analysisResult.geolocation"
  :geolocation="analysisResult.geolocation"
/>

4. **Reputación** (Línea 340-380):

<div class="reputation-section">
  <h3>Reputación</h3>
  <div v-for="rep in analysisResult.reputation" 
       :key="rep.source"
       class="reputation-card">
    <div class="rep-header">
      <h4>{{ rep.name }}</h4>
      <span :class="['status-badge', rep.status]">
        {{ rep.statusText }}
      </span>
    </div>
    <div class="rep-score">
      Score: <strong>{{ rep.score }}/100</strong>
    </div>
    <p class="rep-details">{{ rep.details }}</p>
  </div>
</div>

5. **Vulnerabilidades** (Línea 400-450):

<div v-if="analysisResult.vulnerabilities.length > 0" 
     class="vulnerabilities-section">
  <h3>Vulnerabilidades ({{ analysisResult.vulnerabilities.length }})</h3>
  <div v-for="vuln in analysisResult.vulnerabilities" 
       :key="vuln.id"
       class="vuln-card">
    <div class="vuln-header">
      <span class="vuln-id">{{ vuln.id }}</span>
      <span :class="['severity-badge', vuln.severity]">
        {{ vuln.severity }}
      </span>
      <span class="cvss">CVSS: {{ vuln.cvss }}</span>
    </div>
    <h4>{{ vuln.title }}</h4>
    <p>{{ vuln.description }}</p>
    <div class="solution">
      <strong>Solución:</strong> {{ vuln.solution }}
    </div>
  </div>
</div>

6. **Recomendaciones** (Línea 460-510):

<div v-if="analysisResult.recommendations.length > 0" 
     class="recommendations-section">
  <h3>Recomendaciones</h3>
  <div v-for="rec in analysisResult.recommendations" 
       :key="rec.title"
       :class="['rec-card', rec.priority]">
    <div class="rec-header">
      <h4>{{ rec.title }}</h4>
      <span :class="['priority-badge', rec.priority]">
        {{ rec.priority }}
      </span>
    </div>
    <p>{{ rec.description }}</p>
    <span class="category">{{ rec.category }}</span>
  </div>
</div>

### 3. Vista de Comparación (src/views/IPComparator.vue)

**Flujo de comparación:**

```typescript
// Línea 100-150
const compareTargets = async () => {
  if (!query1.value || !query2.value) return;

  isLoading1.value = true;
  isLoading2.value = true;

  try {
    // Analizar ambos en paralelo
    const [result1, result2] = await Promise.all([
      api.analyzeTarget(query1.value, type1.value),
      api.analyzeTarget(query2.value, type2.value)
    ]);

    analysisResult1.value = result1;
    analysisResult2.value = result2;

    // Determinar ganador (mejor score)
    winner.value = result1.securityScore > result2.securityScore ? 'left' : 'right';

  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    isLoading1.value = false;
    isLoading2.value = false;
  }
};
```

**Componentes de comparación:**

<!-- Línea 200-210 -->
<ComparisonSummaryCard
  :query="query1"
  :result="analysisResult1"
  :loading="isLoading1"
  :is-winner="winner === 'left'"
  winner-class="left"
/>

<ComparisonSummaryCard
  :query="query2"
  :result="analysisResult2"
  :loading="isLoading2"
  :is-winner="winner === 'right'"
  winner-class="right"
/>

<!-- Línea 230 -->
<ServicesComparison
  :result1="analysisResult1"
  :result2="analysisResult2"
  :loading1="isLoading1"
  :loading2="isLoading2"
/>

<!-- Línea 250 -->
<MapsComparison
  :geolocation1="analysisResult1?.geolocation"
  :geolocation2="analysisResult2?.geolocation"
  :loading1="isLoading1"
  :loading2="isLoading2"
/>

<!-- Línea 270 -->
<ReputationComparison
  :result1="analysisResult1"
  :result2="analysisResult2"
  :loading1="isLoading1"
  :loading2="isLoading2"
/>

<!-- Línea 290 -->
<NetworkInfoComparison
  :result1="analysisResult1"
  :result2="analysisResult2"
  :loading1="isLoading1"
  :loading2="isLoading2"
/>

<!-- Línea 310 -->
<RecommendationsComparison
  :result1="analysisResult1"
  :result2="analysisResult2"
  :loading1="isLoading1"
  :loading2="isLoading2"
/>

### 4. Vista de Historial (src/views/History.vue)

**Gestión del historial:**

```typescript
// Línea 40-80
const loadHistory = async () => {
  const history = await historyStorage.getItem('analysis_history') || [];
  
  // Filtrar últimos 7 días
  const sevenDaysAgo = Date.now() - (7 * 24 * 60 * 60 * 1000);
  historyEntries.value = history.filter(entry => 
    entry.timestamp > sevenDaysAgo
  );

  // Calcular estadísticas
  calculateStatistics();
};

// Línea 100-150
const calculateStatistics = () => {
  if (historyEntries.value.length === 0) {
    statistics.value = null;
    return;
  }

  const total = historyEntries.value.length;
  const uniqueIPs = new Set(historyEntries.value.map(e => e.query)).size;
  
  const avgScore = historyEntries.value.reduce(
    (sum, entry) => sum + entry.securityScore, 0
  ) / total;

  const riskDistribution = {
    low: historyEntries.value.filter(e => e.riskLevel === 'low').length,
    medium: historyEntries.value.filter(e => e.riskLevel === 'medium').length,
    high: historyEntries.value.filter(e => e.riskLevel === 'high').length
  };

  statistics.value = {
    totalAnalyses: total,
    uniqueIPs,
    averageScore: Math.round(avgScore),
    riskDistribution
  };
};
```

**Gráficos Chart.js:**

<!-- Línea 200-230: Distribución de Riesgo -->
<Doughnut
  :data="{
    labels: ['Low Risk', 'Medium Risk', 'High Risk'],
    datasets: [{
      data: [
        statistics.riskDistribution.low,
        statistics.riskDistribution.medium,
        statistics.riskDistribution.high
      ],
      backgroundColor: ['#10B981', '#F59E0B', '#EF4444']
    }]
  }"
  :options="riskChartOptions"
/>

<!-- Línea 250-280: Timeline de Análisis -->
<Line
  :data="{
    labels: timelineLabels,
    datasets: [{
      label: 'Security Score',
      data: timelineData,
      borderColor: '#3B82F6',
      backgroundColor: 'rgba(59, 130, 246, 0.1)'
    }]
  }"
  :options="timelineChartOptions"
/>

**Tabla de Historial:**

<!-- Línea 300 -->
<HistoryTable
  :entries="historyEntries"
  :current-page="currentPage"
  :items-per-page="itemsPerPage"
  @load-analysis="loadAnalysis"
  @delete-analysis="deleteAnalysis"
  @update:current-page="currentPage = $event"
/>

### 5. Sistema de Caché

El frontend implementa caché en dos niveles:

**1. Caché de Análisis (7 días):**
```typescript
// src/services/cache.ts - Línea 10-30
import localforage from 'localforage';

const analysisCache = localforage.createInstance({
  name: 'diagseg',
  storeName: 'analysis_cache'
});

export const getCachedAnalysis = async (query: string) => {
  const cacheKey = `analysis_${query}`;
  const cached = await analysisCache.getItem(cacheKey);
  
  if (!cached) return null;
  
  const age = Date.now() - cached.timestamp;
  const maxAge = 7 * 24 * 60 * 60 * 1000; // 7 días
  
  if (age > maxAge) {
    await analysisCache.removeItem(cacheKey);
    return null;
  }
  
  return cached;
};
```

**2. Historial Persistente:**
```typescript
// src/services/history.ts - Línea 15-40
export const addToHistory = async (result: AnalysisResult) => {
  const history = await historyStorage.getItem('analysis_history') || [];
  
  const entry = {
    query: result.ip,
    type: result.type,
    securityScore: result.securityScore,
    riskLevel: result.riskLevel,
    timestamp: result.timestamp,
    servicesCount: result.services.length,
    vulnerabilitiesCount: result.vulnerabilities.length
  };
  
  // Agregar al inicio
  history.unshift(entry);
  
  // Mantener solo últimos 100
  if (history.length > 100) {
    history.splice(100);
  }
  
  await historyStorage.setItem('analysis_history', history);
};
```

---

## Validación y Errores

### Validación de Entrada

El backend debe validar el campo `query` según el `type` especificado:

**1. IPv4:**
```
Regex: ^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$

Válidos:
- 8.8.8.8
- 192.168.1.1
- 10.0.0.1

Inválidos:
- 256.256.256.256
- 192.168.1
- 192.168.1.1.1
```

**2. IPv6:**
```
Regex: ^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|::)$

Válidos:
- 2001:4860:4860::8888
- ::1
- fe80::1

Inválidos:
- 2001:4860:4860::8888::1
- gggg::/64
```

**3. ASN:**
```
Regex: ^AS[0-9]{1,10}$

Válidos:
- AS15169
- AS13335
- AS1234567890

Inválidos:
- 15169 (falta prefijo AS)
- AS (sin número)
- AS12345678901 (más de 10 dígitos)
```

### Códigos de Error

El backend debe retornar estos códigos en el campo `code`:

| Code | HTTP Status | Descripción | Cuándo usar |
|------|-------------|-------------|-------------|
| INVALID_INPUT | 400 | Query no válida | Regex no coincide |
| NOT_FOUND | 404 | Sin datos | No hay info en BigQuery/GeoLite2 |
| INTERNAL_ERROR | 500 | Error del servidor | Excepciones no manejadas |
| DATABASE_ERROR | 500 | Error de BD | Falla en consulta BigQuery |
| GEOLOCATION_ERROR | 500 | Error geolocalización | Falla en GeoLite2 |

### Mensajes de Error en Español

El frontend muestra los mensajes directamente al usuario, por lo que deben estar en español:

```json
{
  "error": "IP inválida",
  "message": "La dirección IP '256.256.256.256' no es válida",
  "code": "INVALID_INPUT"
}

{
  "error": "No se encontró información",
  "message": "No hay datos disponibles para el ASN 'AS99999'",
  "code": "NOT_FOUND"
}

{
  "error": "Error del servidor",
  "message": "Ocurrió un error al procesar la solicitud. Por favor intente nuevamente",
  "code": "INTERNAL_ERROR"
}
```

---

## Resumen de Requisitos

### Datos Requeridos

1. **Base de datos GeoLite2 (MaxMind):**
   - Descarga: https://dev.maxmind.com/geoip/geolite2-free-geolocation-data
   - Formato: MMDB
   - Actualización: Mensual recomendado
   - Datos: país, región, ciudad, coordenadas, timezone

2. **Google BigQuery - Censys Dataset:**
   - Dataset: `censys-io.universal_internet_dataset`
   - Tablas: `scan_results`, `services`
   - Autenticación: Cuenta de servicio Google Cloud
   - Datos: puertos, servicios, banners, ASN, ISP

### Endpoint Único

```
POST /api/analysis/analyze

Request:
{
  "query": "string",
  "type": "ipv4" | "ipv6" | "asn"
}

Response: AnalysisResult (ver sección Modelos de Datos)
```

### Proceso de Análisis

```
1. Validar entrada
2. Consultar GeoLite2 para geolocalización
3. Consultar BigQuery Censys para servicios/ASN
4. Correlacionar servicios con CVEs
5. Calcular securityScore
6. Generar recomendaciones
7. Construir ReputationSource
8. Retornar JSON completo
```

### Configuración del Frontend

El frontend espera que el backend esté disponible en:

```bash
# .env file
VITE_API_BASE_URL=http://localhost:8080/api
```

Para desarrollo local:
- Backend: http://localhost:8080
- Frontend: http://localhost:5173

---

Versión: 2.0
Última actualización: 31 de Octubre de 2025
Estado Frontend: Completamente Funcional
Estado Backend: Pendiente de Implementación
