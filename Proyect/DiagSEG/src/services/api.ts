// Servicio API completo para DiagSEG

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'


// Interfaces
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
  timestamp: number
}

export interface GeolocationData {
  country: string
  countryCode: string
  region: string
  city: string
  latitude: number
  longitude: number
  timezone: string
  isp: string
  asn: string
  asnOrg: string
}

export interface ServiceInfo {
  port: number
  name: string
  version: string
  riskLevel: 'low' | 'medium' | 'high'
  riskText: string
}

export interface ReputationData {
  name: string
  status: 'clean' | 'suspicious' | 'malicious'
  statusText: string
  details?: string
  confidence: number
}

export interface SecurityRecommendation {
  priority: 'high' | 'medium' | 'low' | 'info'
  title: string
  description: string
  action?: string
}

export interface AnalysisResult {
  ip: string
  type: 'ipv4' | 'ipv6' | 'domain'
  securityScore: number
  timestamp: number
  
  // Geolocation
  geolocation: GeolocationData
  
  // Services
  services: ServiceInfo[]
  
  // Reputation
  reputation: ReputationData[]
  
  // Recommendations
  recommendations: SecurityRecommendation[]
}

// ==== Tipos que devuelve el backend real (Quarkus) ====

type BackendTargetType = 'ipv4' | 'ipv6' | 'asn'
type BackendRiskLevel = 'low' | 'medium' | 'high'
type BackendProtocol = 'tcp' | 'udp'

interface BackendService {
  port: number
  protocol: BackendProtocol
  service: string
  version?: string
  banner?: string
  vulnerabilities: string[]
  riskLevel: BackendRiskLevel
}

interface BackendGeolocation {
  country: string
  countryCode: string
  region: string
  city: string
  latitude: number
  longitude: number
  timezone: string
  isp: string
  asn: string
  org: string
}

type BackendReputationStatus = 'clean' | 'suspicious' | 'malicious' | 'unknown'

interface BackendReputationSource {
  source: string
  name: string
  status: BackendReputationStatus
  statusText: string
  score: number
  details: string
  lastChecked: number
}

type BackendVulnSeverity = 'low' | 'medium' | 'high' | 'critical'

interface BackendVulnerability {
  id: string
  title: string
  severity: BackendVulnSeverity
  cvss: number
  description: string
  solution: string
  references: string[]
}

type BackendRecommendationPriority = 'low' | 'medium' | 'high'
type BackendRecommendationCategory = 'network' | 'service' | 'configuration' | 'security'

interface BackendRecommendation {
  title: string
  description: string
  priority: BackendRecommendationPriority
  category: BackendRecommendationCategory
}

interface BackendAnalysisMetadata {
  scanDuration: number
  sourcesUsed: string[]
  cached: boolean
}

interface BackendAnalysisResult {
  ip: string
  type: BackendTargetType
  securityScore: number
  riskLevel: BackendRiskLevel
  timestamp: number
  services: BackendService[]
  geolocation: BackendGeolocation
  reputation: BackendReputationSource[]
  vulnerabilities: BackendVulnerability[]
  recommendations: BackendRecommendation[]
  metadata: BackendAnalysisMetadata
}

// ==== Mapeo de respuesta del backend al modelo actual del front ====

function mapRiskLevelToText(risk: BackendRiskLevel): string {
  switch (risk) {
    case 'low':
      return 'Bajo'
    case 'medium':
      return 'Medio'
    case 'high':
      return 'Alto'
    default:
      return 'Desconocido'
  }
}

function mapBackendToGeolocation(geo: BackendGeolocation): GeolocationData {
  return {
    country: geo.country,
    countryCode: geo.countryCode,
    region: geo.region,
    city: geo.city,
    latitude: geo.latitude,
    longitude: geo.longitude,
    timezone: geo.timezone,
    isp: geo.isp,
    asn: geo.asn,
    // backend: org → front: asnOrg
    asnOrg: geo.org
  }
}

function mapBackendService(service: BackendService): ServiceInfo {
  return {
    port: service.port,
    name: service.service,
    version: service.version || 'Unknown',
    riskLevel: service.riskLevel,
    riskText: mapRiskLevelToText(service.riskLevel)
  }
}

function mapBackendReputation(rep: BackendReputationSource): ReputationData {
  return {
    name: rep.name,
    status: rep.status === 'unknown' ? 'clean' : rep.status,
    statusText: rep.statusText,
    details: rep.details,
    confidence: rep.score // 0-100
  }
}

function mapBackendRecommendation(rec: BackendRecommendation): SecurityRecommendation {
  return {
    priority: rec.priority, // 'low' | 'medium' | 'high'
    title: rec.title,
    description: rec.description,
    action: undefined
  }
}

function mapBackendAnalysisResult(
  backend: BackendAnalysisResult
): AnalysisResult {
  return {
    ip: backend.ip,
    // Tu modelo: 'ipv4' | 'ipv6' | 'domain'
    type: backend.type === 'ipv4' ? 'ipv4' : 'ipv6',
    securityScore: backend.securityScore,
    timestamp: backend.timestamp,
    geolocation: mapBackendToGeolocation(backend.geolocation),
    services: backend.services.map(mapBackendService),
    reputation: backend.reputation.map(mapBackendReputation),
    recommendations: backend.recommendations.map(mapBackendRecommendation)
  }
}


/**
 * Utilidad para validar formato de IP
 */
export function isValidIP(ip: string): boolean {
  const ipv4Regex = /^(\d{1,3}\.){3}\d{1,3}$/
  const ipv6Regex = /^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$/
  
  return ipv4Regex.test(ip) || ipv6Regex.test(ip)
}

/**
 * Utilidad para validar formato de dominio
 */
export function isValidDomain(domain: string): boolean {
  const domainRegex = /^[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]?(\.[a-zA-Z]{2,})+$/
  return domainRegex.test(domain)
}

/**
 * Utilidad para simular delay en desarrollo
 */
function delay(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * Determina el tipo de query (IP o dominio)
 */
export function getQueryType(query: string): 'ipv4' | 'ipv6' | 'domain' | 'invalid' {
  const trimmed = query.trim()
  
  if (/^(\d{1,3}\.){3}\d{1,3}$/.test(trimmed)) {
    return 'ipv4'
  }
  
  if (/^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$/.test(trimmed)) {
    return 'ipv6'
  }
  
  if (isValidDomain(trimmed)) {
    return 'domain'
  }
  
  return 'invalid'
}

// Error handling
export class ApiError extends Error {
  constructor(
    public code: string,
    message: string,
    public statusCode?: number
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

// Base API Service
export class BaseApiService {
  protected async makeRequest<T>(
    url: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    const startTime = Date.now()
    
    try {
      const response = await fetch(url, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          ...options.headers
        }
      })

      if (!response.ok) {
        throw new ApiError(
          'HTTP_ERROR',
          `HTTP ${response.status}: ${response.statusText}`,
          response.status
        )
      }

      const data = await response.json()
      
      return {
        success: true,
        data,
        timestamp: Date.now() - startTime
      }
    } catch (error) {
      console.error('API Request failed:', error)
      
      if (error instanceof ApiError) {
        return {
          success: false,
          error: error.message,
          timestamp: Date.now() - startTime
        }
      }

      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
        timestamp: Date.now() - startTime
      }
    }
  }
}

// Mock data generator for development
export class MockDataService {
  static generateGeolocation(ip: string): GeolocationData {
    // Generate realistic mock data based on common IP ranges
    const mockData: Record<string, Partial<GeolocationData>> = {
      '8.8.8.8': {
        country: 'Estados Unidos',
        countryCode: 'US',
        region: 'California',
        city: 'Mountain View',
        latitude: 37.4056,
        longitude: -122.0775,
        timezone: 'America/Los_Angeles',
        isp: 'Google LLC',
        asn: 'AS15169',
        asnOrg: 'Google LLC'
      },
      '1.1.1.1': {
        country: 'Estados Unidos',
        countryCode: 'US',
        region: 'California',
        city: 'San Francisco',
        latitude: 37.7621,
        longitude: -122.3971,
        timezone: 'America/Los_Angeles',
        isp: 'Cloudflare Inc',
        asn: 'AS13335',
        asnOrg: 'Cloudflare Inc'
      }
    }

    return {
      country: 'Estados Unidos',
      countryCode: 'US',
      region: 'California',
      city: 'Mountain View',
      latitude: 37.4056,
      longitude: -122.0775,
      timezone: 'America/Los_Angeles',
      isp: 'Google LLC',
      asn: 'AS15169',
      asnOrg: 'Google LLC',
      ...mockData[ip]
    }
  }

  static generateServices(ip: string): ServiceInfo[] {
    const commonServices: ServiceInfo[] = [
      {
        port: 53,
        name: 'DNS',
        version: 'Google DNS',
        riskLevel: 'low',
        riskText: 'Bajo'
      },
      {
        port: 443,
        name: 'HTTPS',
        version: 'HTTP/2',
        riskLevel: 'low',
        riskText: 'Bajo'
      }
    ]

    return commonServices
  }

  static generateReputation(ip: string): ReputationData[] {
    return [
      {
        name: 'VirusTotal',
        status: 'clean',
        statusText: 'Limpio',
        details: 'Sin detecciones maliciosas',
        confidence: 95
      },
      {
        name: 'AbuseIPDB',
        status: 'clean',
        statusText: 'Confiable',
        details: 'Servicios de DNS público conocidos',
        confidence: 98
      }
    ]
  }

  static generateRecommendations(ip: string): SecurityRecommendation[] {
    return [
      {
        priority: 'info',
        title: 'Servidor DNS Público',
        description: 'Esta IP corresponde a un servidor DNS público de Google, ampliamente utilizado y confiable.',
        action: 'Monitorear uso normal'
      }
    ]
  }

  static calculateSecurityScore(data: {
    reputation: ReputationData[]
    services: ServiceInfo[]
    geolocation: GeolocationData
  }): number {
    let score = 100

    // Reputation factor (40%)
    const avgReputation = data.reputation.reduce((acc, rep) => {
      const statusScore = rep.status === 'clean' ? 100 : rep.status === 'suspicious' ? 50 : 0
      return acc + (statusScore * rep.confidence / 100)
    }, 0) / data.reputation.length

    // Services factor (30%)
    const serviceScore = data.services.reduce((acc, service) => {
      const riskScore = service.riskLevel === 'low' ? 100 : service.riskLevel === 'medium' ? 70 : 30
      return acc + riskScore
    }, 0) / data.services.length

    // Geographic context (10%)
    const geoScore = 90 // Base score for known countries

    // Calculate weighted score
    score = (avgReputation * 0.4) + (serviceScore * 0.3) + (80 * 0.2) + (geoScore * 0.1)

    return Math.round(Math.max(0, Math.min(100, score)))
  }
}

// Main Analysis Service
// Main Analysis Service
export class AnalysisService extends BaseApiService {
  async analyzeIP(query: string): Promise<ApiResponse<AnalysisResult>> {
    const startTime = Date.now()

    try {
      const queryType = getQueryType(query)
      if (queryType === 'invalid') {
        throw new ApiError('INVALID_INPUT', 'Formato de IP o dominio inválido')
      }

      // 👉 Si es dominio, seguimos usando MOCK como antes
      if (queryType === 'domain') {
        await delay(500)

        const resolvedIP = '8.8.8.8' // resolución mock por ahora

        const geolocation = MockDataService.generateGeolocation(resolvedIP)
        const services = MockDataService.generateServices(resolvedIP)
        const reputation = MockDataService.generateReputation(resolvedIP)
        const recommendations = MockDataService.generateRecommendations(resolvedIP)

        const securityScore = MockDataService.calculateSecurityScore({
          reputation,
          services,
          geolocation
        })

        const result: AnalysisResult = {
          ip: resolvedIP,
          type: queryType,
          securityScore,
          timestamp: Date.now(),
          geolocation,
          services,
          reputation,
          recommendations
        }

        return {
          success: true,
          data: result,
          timestamp: Date.now() - startTime
        }
      }

      // 👉 Si es IP v4/v6, llamamos al backend real
      await delay(300) // opcional, solo para UX de loading

      const payload = {
        query: query.trim(),
        type: queryType // 'ipv4' | 'ipv6' – coincide con el backend
      }

      const response = await fetch(`${API_BASE_URL}/analysis/analyze`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      if (!response.ok) {
        let errorBody: any = null
        try {
          errorBody = await response.json()
        } catch {
          // ignoramos si no es JSON
        }

        if (errorBody && errorBody.code && errorBody.message) {
          // Errores que vienen del backend (INVALID_INPUT, NOT_FOUND, etc.)
          throw new ApiError(errorBody.code, errorBody.message, response.status)
        }

        throw new ApiError(
          'HTTP_ERROR',
          `HTTP ${response.status}: ${response.statusText}`,
          response.status
        )
      }

      const backendResult: BackendAnalysisResult = await response.json()
      const result: AnalysisResult = mapBackendAnalysisResult(backendResult)

      return {
        success: true,
        data: result,
        timestamp: Date.now() - startTime
      }
    } catch (error) {
      console.error('Analysis failed:', error)

      if (error instanceof ApiError) {
        return {
          success: false,
          error: error.message,
          timestamp: Date.now() - startTime
        }
      }

      return {
        success: false,
        error: error instanceof Error ? error.message : 'Error desconocido',
        timestamp: Date.now() - startTime
      }
    }
  }
}

// Export singleton instance
export const analysisService = new AnalysisService()
