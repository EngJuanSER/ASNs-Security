import localforage from 'localforage'
import type { AnalysisResult } from './api'

/**
 * Servicio de caché para almacenamiento persistente de resultados de análisis
 * Utiliza LocalForage para almacenamiento en IndexedDB con fallback a localStorage
 */
class CacheService {
  private store: LocalForage

  constructor() {
    // Configurar LocalForage para el almacenamiento
    this.store = localforage.createInstance({
      name: 'DiagSEG',
      storeName: 'analysis_cache',
      description: 'Cache de resultados de análisis de seguridad'
    })
  }

  /**
   * Generar clave única para el caché basada en la consulta
   */
  private generateCacheKey(query: string): string {
    return `analysis_${query.toLowerCase().trim()}`
  }

  /**
   * Obtener resultado del caché
   */
  async get(query: string): Promise<AnalysisResult | null> {
    try {
      const cacheKey = this.generateCacheKey(query)
      const cached = await this.store.getItem<{
        data: AnalysisResult
        timestamp: number
        ttl: number
      }>(cacheKey)

      if (!cached) {
        return null
      }

      // Verificar si el caché ha expirado (TTL configurable, por defecto 30 minutos)
      const now = Date.now()
      if (now > cached.timestamp + cached.ttl) {
        await this.remove(query)
        return null
      }

      return cached.data
    } catch (error) {
      console.warn('Error al leer del caché:', error)
      return null
    }
  }

  /**
   * Guardar resultado en el caché
   */
  async set(query: string, data: AnalysisResult, ttlMinutes: number = 30): Promise<void> {
    try {
      const cacheKey = this.generateCacheKey(query)
      const cacheEntry = {
        data,
        timestamp: Date.now(),
        ttl: ttlMinutes * 60 * 1000 // Convertir minutos a milisegundos
      }

      await this.store.setItem(cacheKey, cacheEntry)
    } catch (error) {
      console.warn('Error al guardar en caché:', error)
    }
  }

  /**
   * Eliminar entrada específica del caché
   */
  async remove(query: string): Promise<void> {
    try {
      const cacheKey = this.generateCacheKey(query)
      await this.store.removeItem(cacheKey)
    } catch (error) {
      console.warn('Error al eliminar del caché:', error)
    }
  }

  /**
   * Limpiar todo el caché
   */
  async clear(): Promise<void> {
    try {
      await this.store.clear()
    } catch (error) {
      console.warn('Error al limpiar caché:', error)
    }
  }

  /**
   * Obtener estadísticas del caché
   */
  async getStats(): Promise<{
    totalEntries: number
    totalSize: number
    entries: Array<{
      query: string
      timestamp: number
      size: number
    }>
  }> {
    try {
      const keys = await this.store.keys()
      const entries: Array<{
        query: string
        timestamp: number
        size: number
      }> = []

      let totalSize = 0

      for (const key of keys) {
        const item = await this.store.getItem(key)
        if (item) {
          const size = JSON.stringify(item).length
          totalSize += size
          
          entries.push({
            query: key.replace('analysis_', ''),
            timestamp: (item as any).timestamp || 0,
            size
          })
        }
      }

      return {
        totalEntries: keys.length,
        totalSize,
        entries: entries.sort((a, b) => b.timestamp - a.timestamp)
      }
    } catch (error) {
      console.warn('Error al obtener estadísticas del caché:', error)
      return {
        totalEntries: 0,
        totalSize: 0,
        entries: []
      }
    }
  }

  /**
   * Verificar si una consulta está en caché y es válida
   */
  async has(query: string): Promise<boolean> {
    const result = await this.get(query)
    return result !== null
  }

  /**
   * Obtener todas las consultas en caché
   */
  async getAllQueries(): Promise<string[]> {
    try {
      const keys = await this.store.keys()
      return keys.map(key => key.replace('analysis_', ''))
    } catch (error) {
      console.warn('Error al obtener consultas del caché:', error)
      return []
    }
  }

  /**
   * Limpiar entradas expiradas del caché
   */
  async cleanExpired(): Promise<number> {
    try {
      const keys = await this.store.keys()
      let cleaned = 0
      const now = Date.now()

      for (const key of keys) {
        const item = await this.store.getItem<{
          timestamp: number
          ttl: number
        }>(key)

        if (item && now > item.timestamp + item.ttl) {
          await this.store.removeItem(key)
          cleaned++
        }
      }

      return cleaned
    } catch (error) {
      console.warn('Error al limpiar caché expirado:', error)
      return 0
    }
  }
}

// Instancia singleton del servicio de caché
export const cacheService = new CacheService()

// Limpiar caché expirado al cargar la aplicación
cacheService.cleanExpired()

export default cacheService
