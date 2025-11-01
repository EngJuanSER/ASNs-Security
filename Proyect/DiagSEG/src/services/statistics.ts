import localforage from 'localforage'
import type { AnalysisResult } from './api'

/**
 * Interfaz para estadísticas de análisis
 */
export interface AnalysisStatistics {
  totalAnalyses: number
  uniqueIPs: number
  averageScore: number
  riskDistribution: {
    safe: number      // >= 70
    warning: number   // 50-69
    danger: number    // < 50
  }
  topCountries: Array<{
    country: string
    count: number
    percentage: number
  }>
  topASNs: Array<{
    asn: string
    org: string
    count: number
  }>
  analysisHistory: Array<{
    date: string
    count: number
    averageScore: number
  }>
  recentAnalyses: AnalysisHistoryEntry[]
}

/**
 * Interfaz para entrada del historial
 */
export interface AnalysisHistoryEntry {
  id: string
  query: string
  ip: string
  type: 'ipv4' | 'ipv6' | 'domain'
  securityScore: number
  timestamp: number
  country: string
  status: 'safe' | 'warning' | 'danger'
  fromCache: boolean
}

/**
 * Interfaz para comparación de IPs
 */
export interface IPComparisonData {
  id: string
  query: string
  result: AnalysisResult
  timestamp: number
}

/**
 * Servicio para gestión de estadísticas y historial
 */
class StatisticsService {
  private historyStore: LocalForage
  private statsStore: LocalForage
  private comparisonStore: LocalForage

  constructor() {
    // Configurar stores separados
    this.historyStore = localforage.createInstance({
      name: 'DiagSEG',
      storeName: 'analysis_history',
      description: 'Historial de análisis realizados'
    })

    this.statsStore = localforage.createInstance({
      name: 'DiagSEG',
      storeName: 'statistics',
      description: 'Estadísticas agregadas'
    })

    this.comparisonStore = localforage.createInstance({
      name: 'DiagSEG',
      storeName: 'comparisons',
      description: 'Datos para comparación de IPs'
    })
  }

  /**
   * Registrar un nuevo análisis en el historial
   */
  async recordAnalysis(query: string, result: AnalysisResult, fromCache: boolean = false): Promise<void> {
    try {
      const entry: AnalysisHistoryEntry = {
        id: `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        query,
        ip: result.ip,
        type: result.type,
        securityScore: result.securityScore,
        timestamp: Date.now(),
        country: result.geolocation.country,
        status: this.getStatusFromScore(result.securityScore),
        fromCache
      }

      // Obtener historial existente
      const history = await this.getAnalysisHistory() || []
      
      // Añadir nueva entrada al principio
      history.unshift(entry)
      
      // Mantener solo los últimos 1000 análisis
      const trimmedHistory = history.slice(0, 1000)
      
      // Guardar historial actualizado
      await this.historyStore.setItem('analysis_history', trimmedHistory)
      
      // Actualizar estadísticas agregadas
      await this.updateAggregatedStats()
      
    } catch (error) {
      console.warn('Error al registrar análisis:', error)
    }
  }

  /**
   * Obtener historial de análisis
   */
  async getAnalysisHistory(): Promise<AnalysisHistoryEntry[]> {
    try {
      return await this.historyStore.getItem('analysis_history') || []
    } catch (error) {
      console.warn('Error al obtener historial:', error)
      return []
    }
  }

  /**
   * Obtener estadísticas agregadas
   */
  async getStatistics(): Promise<AnalysisStatistics> {
    try {
      const history = await this.getAnalysisHistory()
      
      if (history.length === 0) {
        return this.getEmptyStatistics()
      }

      return this.calculateStatistics(history)
    } catch (error) {
      console.warn('Error al calcular estadísticas:', error)
      return this.getEmptyStatistics()
    }
  }

  /**
   * Guardar IP para comparación
   */
  async saveForComparison(query: string, result: AnalysisResult): Promise<string> {
    try {
      const comparisonData: IPComparisonData = {
        id: `comp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        query,
        result,
        timestamp: Date.now()
      }

      await this.comparisonStore.setItem(comparisonData.id, comparisonData)
      return comparisonData.id
    } catch (error) {
      console.warn('Error al guardar para comparación:', error)
      throw new Error('Error al guardar IP para comparación')
    }
  }

  /**
   * Obtener datos guardados para comparación
   */
  async getComparisonData(): Promise<IPComparisonData[]> {
    try {
      const keys = await this.comparisonStore.keys()
      const comparisons: IPComparisonData[] = []

      for (const key of keys) {
        const data = await this.comparisonStore.getItem<IPComparisonData>(key)
        if (data) {
          comparisons.push(data)
        }
      }

      // Ordenar por timestamp descendente y mantener solo los últimos 20
      return comparisons
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 20)
    } catch (error) {
      console.warn('Error al obtener datos de comparación:', error)
      return []
    }
  }

  /**
   * Eliminar datos de comparación
   */
  async removeComparisonData(id: string): Promise<void> {
    try {
      await this.comparisonStore.removeItem(id)
    } catch (error) {
      console.warn('Error al eliminar datos de comparación:', error)
    }
  }

  /**
   * Eliminar un análisis específico del historial
   */
  async deleteAnalysis(analysisId: string): Promise<boolean> {
    try {
      const history = await this.getAnalysisHistory()
      const originalLength = history.length
      
      const filteredHistory = history.filter(entry => entry.id !== analysisId)
      
      if (filteredHistory.length < originalLength) {
        await this.historyStore.setItem('analysis_history', filteredHistory)
        await this.updateAggregatedStats()
        return true
      }
      
      return false
    } catch (error) {
      console.warn('Error al eliminar análisis:', error)
      return false
    }
  }

  /**
   * Limpiar historial antiguo
   */
  async cleanOldHistory(daysToKeep: number = 30): Promise<number> {
    try {
      const history = await this.getAnalysisHistory()
      const cutoffTime = Date.now() - (daysToKeep * 24 * 60 * 60 * 1000)
      
      const filteredHistory = history.filter(entry => entry.timestamp > cutoffTime)
      const removedCount = history.length - filteredHistory.length

      if (removedCount > 0) {
        await this.historyStore.setItem('analysis_history', filteredHistory)
        await this.updateAggregatedStats()
      }

      return removedCount
    } catch (error) {
      console.warn('Error al limpiar historial:', error)
      return 0
    }
  }

  /**
   * Obtener estadísticas de un período específico
   */
  async getStatisticsForPeriod(days: number): Promise<AnalysisStatistics> {
    try {
      const history = await this.getAnalysisHistory()
      const cutoffTime = Date.now() - (days * 24 * 60 * 60 * 1000)
      
      const periodHistory = history.filter(entry => entry.timestamp > cutoffTime)
      
      if (periodHistory.length === 0) {
        return this.getEmptyStatistics()
      }

      return this.calculateStatistics(periodHistory)
    } catch (error) {
      console.warn('Error al calcular estadísticas del período:', error)
      return this.getEmptyStatistics()
    }
  }

  /**
   * Exportar todas las estadísticas
   */
  async exportStatistics(): Promise<{
    statistics: AnalysisStatistics
    history: AnalysisHistoryEntry[]
    comparisons: IPComparisonData[]
  }> {
    return {
      statistics: await this.getStatistics(),
      history: await this.getAnalysisHistory(),
      comparisons: await this.getComparisonData()
    }
  }

  /**
   * Calcular estadísticas a partir del historial
   */
  private calculateStatistics(history: AnalysisHistoryEntry[]): AnalysisStatistics {
    const totalAnalyses = history.length
    const uniqueIPs = new Set(history.map(h => h.ip)).size
    const averageScore = history.reduce((sum, h) => sum + h.securityScore, 0) / totalAnalyses

    // Distribución de riesgo
    const riskDistribution = {
      safe: history.filter(h => h.status === 'safe').length,
      warning: history.filter(h => h.status === 'warning').length,
      danger: history.filter(h => h.status === 'danger').length
    }

    // Top países
    const countryCount = new Map<string, number>()
    history.forEach(h => {
      countryCount.set(h.country, (countryCount.get(h.country) || 0) + 1)
    })

    const topCountries = Array.from(countryCount.entries())
      .map(([country, count]) => ({
        country,
        count,
        percentage: (count / totalAnalyses) * 100
      }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 10)

    // Historial por días (últimos 30 días)
    const analysisHistory = this.generateDailyHistory(history, 30)

    return {
      totalAnalyses,
      uniqueIPs,
      averageScore: Math.round(averageScore * 100) / 100,
      riskDistribution,
      topCountries,
      topASNs: [], // Se implementará cuando tengamos datos ASN consistentes
      analysisHistory,
      recentAnalyses: history.slice(0, 10)
    }
  }

  /**
   * Generar historial diario
   */
  private generateDailyHistory(history: AnalysisHistoryEntry[], days: number): Array<{
    date: string
    count: number
    averageScore: number
  }> {
    const dailyData = new Map<string, { count: number; totalScore: number }>()
    
    // Últimos N días
    for (let i = 0; i < days; i++) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      const dateStr = date.toISOString().split('T')[0]
      dailyData.set(dateStr, { count: 0, totalScore: 0 })
    }

    // Agrupar análisis por día
    history.forEach(entry => {
      const date = new Date(entry.timestamp).toISOString().split('T')[0]
      const existing = dailyData.get(date)
      if (existing) {
        existing.count++
        existing.totalScore += entry.securityScore
      }
    })

    // Convertir a array y calcular promedios
    return Array.from(dailyData.entries())
      .map(([date, data]) => ({
        date,
        count: data.count,
        averageScore: data.count > 0 ? Math.round((data.totalScore / data.count) * 100) / 100 : 0
      }))
      .sort((a, b) => a.date.localeCompare(b.date))
  }

  /**
   * Obtener estadísticas vacías
   */
  private getEmptyStatistics(): AnalysisStatistics {
    return {
      totalAnalyses: 0,
      uniqueIPs: 0,
      averageScore: 0,
      riskDistribution: { safe: 0, warning: 0, danger: 0 },
      topCountries: [],
      topASNs: [],
      analysisHistory: [],
      recentAnalyses: []
    }
  }

  /**
   * Auto-limpieza de datos antiguos (ejecutar al inicializar)
   */
  async autoCleanOldData(): Promise<void> {
    try {
      // Limpiar datos más antiguos de 7 días automáticamente
      await this.cleanOldHistory(7)
      // Limpiar caché expirado
      await this.cleanExpiredCache()
    } catch (error) {
      console.warn('Error en auto-limpieza:', error)
    }
  }

  /**
   * Limpiar caché expirado del storage
   */
  private async cleanExpiredCache(): Promise<void> {
    try {
      // Esta función se implementaría junto con el cache service
      console.log('Limpieza de caché expirado ejecutada')
    } catch (error) {
      console.warn('Error limpiando caché expirado:', error)
    }
  }

  /**
   * Obtener status desde score
   */
  private getStatusFromScore(score: number): 'safe' | 'warning' | 'danger' {
    if (score >= 70) return 'safe'
    if (score >= 50) return 'warning'
    return 'danger'
  }

  /**
   * Actualizar estadísticas agregadas (para caché)
   */
  private async updateAggregatedStats(): Promise<void> {
    try {
      const stats = await this.getStatistics()
      await this.statsStore.setItem('cached_stats', {
        ...stats,
        lastUpdated: Date.now()
      })
    } catch (error) {
      console.warn('Error al actualizar estadísticas agregadas:', error)
    }
  }
}

// Instancia singleton del servicio de estadísticas
export const statisticsService = new StatisticsService()

export default statisticsService
