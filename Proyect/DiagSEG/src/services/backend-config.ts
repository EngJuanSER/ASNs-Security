/**
 * Configuración para integración futura con backend real
 * Este archivo contiene la configuración y estructura necesaria
 * para conectar con APIs externas y servicios backend
 */

// Importar tipos de la API actual
import type { AnalysisResult, GeolocationData, ServiceInfo, ReputationData, SecurityRecommendation } from './api'

/**
 * Configuración de APIs externas
 */
export interface APIConfiguration {
  censys: {
    baseUrl: string
    apiKey?: string
    secret?: string
    endpoints: {
      hosts: string
      certificates: string
      search: string
    }
  }
  abuseipdb: {
    baseUrl: string
    apiKey?: string
    endpoints: {
      check: string
      report: string
      blacklist: string
    }
  }
  shodan: {
    baseUrl: string
    apiKey?: string
    endpoints: {
      host: string
      search: string
      scan: string
    }
  }
  whois: {
    baseUrl: string
    endpoints: {
      lookup: string
    }
  }
  geolite2: {
    dbPath?: string
    updateUrl?: string
  }
}

/**
 * Configuración por defecto (para desarrollo)
 */
export const defaultConfig: APIConfiguration = {
  censys: {
    baseUrl: 'https://search.censys.io/api/v2',
    endpoints: {
      hosts: '/hosts',
      certificates: '/certificates',
      search: '/search'
    }
  },
  abuseipdb: {
    baseUrl: 'https://api.abuseipdb.com/api/v2',
    endpoints: {
      check: '/check',
      report: '/report',
      blacklist: '/blacklist'
    }
  },
  shodan: {
    baseUrl: 'https://api.shodan.io',
    endpoints: {
      host: '/shodan/host',
      search: '/shodan/host/search',
      scan: '/shodan/scan'
    }
  },
  whois: {
    baseUrl: 'https://api.whoisjson.com',
    endpoints: {
      lookup: '/v1/whois'
    }
  },
  geolite2: {
    dbPath: './data/GeoLite2-City.mmdb',
    updateUrl: 'https://download.maxmind.com/app/geoip_download'
  }
}

/**
 * Estructura para análisis de vulnerabilidades
 */
export interface VulnerabilityAnalysis {
  cveList: Array<{
    id: string
    severity: 'low' | 'medium' | 'high' | 'critical'
    score: number
    description: string
    published: string
    references: string[]
  }>
  exploitAvailable: boolean
  patchAvailable: boolean
  lastUpdated: string
}

/**
 * Estructura para análisis de certificados SSL
 */
export interface SSLCertificateAnalysis {
  valid: boolean
  issuer: string
  subject: string
  validFrom: string
  validTo: string
  algorithm: string
  keySize: number
  serialNumber: string
  fingerprint: string
  chain: Array<{
    subject: string
    issuer: string
    validFrom: string
    validTo: string
  }>
  vulnerabilities: string[]
}

/**
 * Estructura para análisis de DNS
 */
export interface DNSAnalysis {
  records: {
    A: string[]
    AAAA: string[]
    MX: Array<{ priority: number; exchange: string }>
    TXT: string[]
    NS: string[]
    CNAME: string[]
    SOA?: {
      mname: string
      rname: string
      serial: number
      refresh: number
      retry: number
      expire: number
      minimum: number
    }
  }
  dnssec: boolean
  recursionAvailable: boolean
  responseTime: number
  serverLocation: string
}

/**
 * Estructura extendida para resultados completos del backend
 */
export interface CompleteAnalysisResult extends AnalysisResult {
  // Análisis adicionales disponibles cuando se conecte al backend
  vulnerabilitiesAnalysis?: VulnerabilityAnalysis
  sslCertificate?: SSLCertificateAnalysis
  dnsAnalysis?: DNSAnalysis
  
  // Metadatos del análisis
  analysisDepth: 'basic' | 'standard' | 'deep'
  dataSource: 'cache' | 'api' | 'mixed'
  processingTime: number
  
  // Puntuaciones detalladas
  scores: {
    reputation: number
    services: number
    vulnerabilities: number
    geography: number
    ssl: number
    dns: number
    overall: number
  }
}

/**
 * Configuración del algoritmo de scoring
 */
export interface ScoringConfiguration {
  weights: {
    reputation: number      // Peso de la reputación (0-1)
    services: number       // Peso de servicios detectados (0-1)
    vulnerabilities: number // Peso de vulnerabilidades (0-1)
    geography: number      // Peso de geolocalización (0-1)
    ssl: number           // Peso de certificados SSL (0-1)
    dns: number           // Peso de configuración DNS (0-1)
  }
  thresholds: {
    safe: number          // Puntuación mínima considerada segura
    warning: number       // Puntuación mínima para advertencia
    danger: number        // Puntuación considerada peligrosa
  }
  penalties: {
    knownMalicious: number     // Penalización por IP maliciosa conocida
    openVulnerabilities: number // Penalización por vulnerabilidades
    expiredSSL: number         // Penalización por SSL expirado
    suspiciousLocation: number  // Penalización por ubicación sospechosa
  }
}

/**
 * Configuración por defecto del scoring
 */
export const defaultScoringConfig: ScoringConfiguration = {
  weights: {
    reputation: 0.35,
    services: 0.20,
    vulnerabilities: 0.25,
    geography: 0.05,
    ssl: 0.10,
    dns: 0.05
  },
  thresholds: {
    safe: 70,
    warning: 50,
    danger: 30
  },
  penalties: {
    knownMalicious: 50,
    openVulnerabilities: 30,
    expiredSSL: 15,
    suspiciousLocation: 10
  }
}

/**
 * Interface para el cliente de backend
 */
export interface BackendClient {
  // Configuración
  configure(config: Partial<APIConfiguration>): void
  
  // Análisis básico
  analyzeIP(ip: string): Promise<CompleteAnalysisResult>
  analyzeDomain(domain: string): Promise<CompleteAnalysisResult>
  
  // Análisis específicos
  getVulnerabilities(target: string): Promise<VulnerabilityAnalysis>
  getSSLCertificate(domain: string): Promise<SSLCertificateAnalysis>
  getDNSRecords(domain: string): Promise<DNSAnalysis>
  
  // Gestión de bases de datos
  updateGeoDatabase(): Promise<boolean>
  updateVulnerabilityDatabase(): Promise<boolean>
  
  // Utilidades
  validateAPIKeys(): Promise<{ [key: string]: boolean }>
  getAPILimits(): Promise<{ [key: string]: { remaining: number; reset: Date } }>
}

/**
 * Configuración de la base de datos local
 */
export interface DatabaseConfiguration {
  path: string
  tables: {
    analysisCache: string
    geolocationData: string
    vulnerabilityData: string
    reputationData: string
    apiLimits: string
  }
  retentionDays: number
  maxSize: string // e.g., "500MB"
}

/**
 * Configuración por defecto de la base de datos
 */
export const defaultDatabaseConfig: DatabaseConfiguration = {
  path: './data/diagseg.db',
  tables: {
    analysisCache: 'analysis_cache',
    geolocationData: 'geolocation_data',
    vulnerabilityData: 'vulnerability_data',
    reputationData: 'reputation_data',
    apiLimits: 'api_limits'
  },
  retentionDays: 30,
  maxSize: "500MB"
}

/**
 * Configuración de logging para el backend
 */
export interface LoggingConfiguration {
  level: 'debug' | 'info' | 'warn' | 'error'
  destinations: Array<'console' | 'file' | 'database'>
  format: 'json' | 'text'
  rotation: {
    enabled: boolean
    maxSize: string
    maxFiles: number
  }
}

/**
 * Plan de migración del frontend al backend completo
 */
export const migrationPlan = {
  phase1: {
    description: 'Integración básica con APIs externas',
    tasks: [
      'Configurar claves API para Censys, AbuseIPDB, Shodan',
      'Implementar cliente HTTP con rate limiting',
      'Crear caché de resultados en base de datos local',
      'Migrar de datos mock a datos reales'
    ],
    estimated: '2-3 semanas'
  },
  phase2: {
    description: 'Análisis avanzado y optimización',
    tasks: [
      'Implementar análisis de vulnerabilidades con CVE',
      'Agregar análisis de certificados SSL',
      'Implementar análisis DNS completo',
      'Optimizar algoritmo de scoring'
    ],
    estimated: '3-4 semanas'
  },
  phase3: {
    description: 'Base de datos y escalabilidad',
    tasks: [
      'Configurar base de datos GeoLite2 local',
      'Implementar sistema de actualizaciones automáticas',
      'Agregar métricas y monitoring',
      'Optimizar rendimiento y caching'
    ],
    estimated: '2-3 semanas'
  },
  phase4: {
    description: 'Funcionalidades avanzadas',
    tasks: [
      'Sistema de alertas y notificaciones',
      'API REST para integración externa',
      'Dashboard de administración',
      'Exportación avanzada (Excel, CSV, XML)'
    ],
    estimated: '4-5 semanas'
  }
}

// Re-exportar tipos para compatibilidad
export type { AnalysisResult, GeolocationData, ServiceInfo, ReputationData, SecurityRecommendation }
