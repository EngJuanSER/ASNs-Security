// Servicio API completo para DiagSEG

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
export class AnalysisService extends BaseApiService {
  async analyzeIP(query: string): Promise<ApiResponse<AnalysisResult>> {
    const startTime = Date.now()

    try {
      // Validate input
      const queryType = getQueryType(query)
      if (queryType === 'invalid') {
        throw new ApiError('INVALID_INPUT', 'Formato de IP o dominio inválido')
      }

      // Simulate shorter delay for comparisons
      await delay(500) // Reduced from default delay

      // Resolve domain to IP if needed
      let resolvedIP = query
      if (queryType === 'domain') {
        // For now, use mock resolution
        resolvedIP = '8.8.8.8' // Mock resolution
      }

      // Generate mock data (in production, these would be real API calls)
      const geolocation = MockDataService.generateGeolocation(resolvedIP)
      const services = MockDataService.generateServices(resolvedIP)
      const reputation = MockDataService.generateReputation(resolvedIP)
      const recommendations = MockDataService.generateRecommendations(resolvedIP)

      // Calculate security score
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
