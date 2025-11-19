<template>
  <div class="history">
    <Navbar />
    
    <main class="main-content">
      <div class="container">
        <!-- Header -->
        <section class="history-header">
          <h1 class="history-title">Historial de Análisis</h1>
          <p class="history-subtitle">
            Visualiza estadísticas y revisa análisis anteriores de seguridad (últimos 7 días)
          </p>
        </section>

        <!-- Loading State -->
        <div v-if="loading" class="loading-history">
          <div class="loading-spinner"></div>
          <p>Cargando estadísticas...</p>
        </div>

        <!-- History Content -->
        <div v-else class="history-content">
          <!-- Summary Cards -->
          <StatsSummary :stats="stats" />

          <!-- Analysis History Table -->
          <section class="analysis-history">
            <div class="history-card">
              <div class="history-card-header">
                <h3 class="history-card-title">Análisis Recientes</h3>
                <div class="history-actions">
                  <button @click="refreshHistory" class="action-btn refresh-btn">
                    Actualizar
                  </button>
                  <button @click="clearAllHistory" class="action-btn clear-btn">
                    Limpiar Todo
                  </button>
                </div>
              </div>
              
              <HistoryTable 
                :analyses="stats.recentAnalyses"
                :current-page="currentPage"
                :items-per-page="itemsPerPage"
                @load-analysis="loadAnalysis"
                @delete-analysis="deleteAnalysis"
                @update:current-page="(page) => currentPage = page"
              />
            </div>
          </section>

          <!-- Charts Grid -->
          <section class="charts-grid">
            <!-- Risk Distribution Chart -->
            <div class="chart-container">
              <h3 class="chart-title">Distribución de Riesgo</h3>
              <div class="chart-wrapper">
                <Doughnut 
                  v-if="riskChartData.datasets[0].data.length > 0"
                  :key="`risk-${chartKey}`"
                  :data="riskChartData" 
                  :options="riskChartOptions" 
                />
                <div v-else class="empty-chart">
                  <p>No hay datos para mostrar</p>
                </div>
              </div>
            </div>

            <!-- Timeline Chart -->
            <div class="chart-container timeline-container">
              <h3 class="chart-title">Actividad en el Tiempo</h3>
              <div class="chart-wrapper">
                <Line 
                  v-if="timelineChartData.datasets[0].data.length > 0"
                  :key="`timeline-${chartKey}`"
                  :data="timelineChartData" 
                  :options="timelineChartOptions" 
                />
                <div v-else class="empty-chart">
                  <p>No hay datos de actividad</p>
                </div>
              </div>
            </div>
          </section>

          <!-- Countries Statistics -->
          <section class="countries-section">
            <div class="countries-card">
              <h3 class="countries-title">Países Más Analizados</h3>
              <div class="countries-list">
                <div 
                  v-for="country in stats.topCountries.slice(0, 10)" 
                  :key="country.country"
                  class="country-item"
                >
                  <div class="country-info">
                    <span class="country-name">{{ country.country }}</span>
                    <span class="country-count">{{ country.count }} análisis</span>
                  </div>
                  <div class="country-bar">
                    <div 
                      class="country-progress" 
                      :style="{ width: `${country.percentage}%` }"
                    ></div>
                  </div>
                  <span class="country-percentage">{{ Math.round(country.percentage) }}%</span>
                </div>
              </div>
            </div>
          </section>

          <!-- Actions -->
          <section class="history-actions">
            <button @click="exportStatistics" class="action-btn export-btn">
              Exportar Estadísticas
            </button>
            <button @click="clearOldData" class="action-btn clear-btn">
              Limpiar Datos Antiguos (>7d)
            </button>
            <router-link to="/compare" class="action-btn compare-btn">
              Comparar IPs
            </router-link>
          </section>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Doughnut, Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler
} from 'chart.js'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import StatsSummary from '@/components/StatsSummary.vue'
import HistoryTable from '@/components/HistoryTable.vue'
import { statisticsService, type AnalysisStatistics, type AnalysisHistoryEntry } from '@/services/statistics'
import { cacheService } from '@/services/cache'

// Registrar componentes de Chart.js
ChartJS.register(
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler
)

// Router
const router = useRouter()

// Estado del componente
const loading = ref(true)
const stats = ref<AnalysisStatistics>({
  totalAnalyses: 0,
  uniqueIPs: 0,
  averageScore: 0,
  riskDistribution: { safe: 0, warning: 0, danger: 0 },
  topCountries: [],
  topASNs: [],
  analysisHistory: [],
  recentAnalyses: []
})

const currentPage = ref(1)
const itemsPerPage = 20
const chartKey = ref(0) // Key para forzar re-render de gráficos

// Computed properties
const safePercentage = computed(() => {
  const total = stats.value.totalAnalyses
  if (total === 0) return 0
  return Math.round((stats.value.riskDistribution.safe / total) * 100)
})

// Chart data
const riskChartData = computed(() => ({
  labels: ['Seguras', 'Advertencia', 'Peligrosas'],
  datasets: [{
    data: [
      stats.value.riskDistribution.safe,
      stats.value.riskDistribution.warning,
      stats.value.riskDistribution.danger
    ],
    backgroundColor: [
      '#10B981', // Verde
      '#F59E0B', // Amarillo
      '#EF4444'  // Rojo
    ],
    borderWidth: 0,
    hoverOffset: 4
  }]
}))

const riskChartOptions = computed(() => {
  // Forzar recalculo cuando cambia chartKey
  const _ = chartKey.value
  
  const textColor = getComputedStyle(document.documentElement).getPropertyValue('--text-primary').trim()
  const bgColor = getComputedStyle(document.documentElement).getPropertyValue('--bg-primary').trim()
  const secondaryColor = getComputedStyle(document.documentElement).getPropertyValue('--text-secondary').trim()
  const borderColor = getComputedStyle(document.documentElement).getPropertyValue('--border-primary').trim()
  
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          padding: 20,
          usePointStyle: true,
          color: textColor,
          font: {
            size: 13,
            weight: 600 as const
          }
        }
      },
      tooltip: {
        backgroundColor: bgColor,
        titleColor: textColor,
        bodyColor: secondaryColor,
        borderColor: borderColor,
        borderWidth: 1,
        callbacks: {
          label: (context: any) => {
            const total = stats.value.totalAnalyses
            const percentage = total > 0 ? Math.round((context.raw / total) * 100) : 0
            return `${context.label}: ${context.raw} (${percentage}%)`
          }
        }
      }
    }
  }
})

const timelineChartData = computed(() => ({
  labels: stats.value.analysisHistory.map(h => {
    const date = new Date(h.date)
    return date.toLocaleDateString('es-ES', { month: 'short', day: 'numeric' })
  }),
  datasets: [{
    label: 'Análisis',
    data: stats.value.analysisHistory.map(h => h.count),
    borderColor: '#3B82F6',
    backgroundColor: 'rgba(59, 130, 246, 0.1)',
    fill: true,
    tension: 0.4
  }, {
    label: 'Score Promedio',
    data: stats.value.analysisHistory.map(h => h.averageScore),
    borderColor: '#10B981',
    backgroundColor: 'rgba(16, 185, 129, 0.1)',
    fill: false,
    tension: 0.4,
    yAxisID: 'y1'
  }]
}))

const timelineChartOptions = computed(() => {
  // Forzar recalculo cuando cambia chartKey
  const _ = chartKey.value
  
  const textColor = getComputedStyle(document.documentElement).getPropertyValue('--text-primary').trim()
  const secondaryColor = getComputedStyle(document.documentElement).getPropertyValue('--text-secondary').trim()
  const gridColor = getComputedStyle(document.documentElement).getPropertyValue('--border-primary').trim()
  const bgColor = getComputedStyle(document.documentElement).getPropertyValue('--bg-primary').trim()
  const borderColor = getComputedStyle(document.documentElement).getPropertyValue('--border-primary').trim()
  
  return {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      mode: 'index' as const,
      intersect: false
    },
    scales: {
      x: {
        display: true,
        grid: {
          color: gridColor
        },
        ticks: {
          color: secondaryColor,
          font: {
            size: 12,
            weight: 500 as const
          }
        }
      },
      y: {
        type: 'linear' as const,
        display: true,
        position: 'left' as const,
        title: {
          display: true,
          text: 'Número de Análisis',
          color: textColor,
          font: {
            size: 13,
            weight: 600 as const
          }
        },
        grid: {
          color: gridColor
        },
        ticks: {
          color: secondaryColor,
          font: {
            size: 12,
            weight: 500 as const
          }
        }
      },
      y1: {
        type: 'linear' as const,
        display: true,
        position: 'right' as const,
        title: {
          display: true,
          text: 'Score Promedio',
          color: textColor,
          font: {
            size: 13,
            weight: 600 as const
          }
        },
        grid: {
          drawOnChartArea: false
        },
        ticks: {
          color: secondaryColor,
          font: {
            size: 12,
            weight: 500 as const
          }
        }
      }
    },
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          padding: 20,
          usePointStyle: true,
          color: textColor,
          font: {
            size: 13,
            weight: 600 as const
          }
        }
      },
      tooltip: {
        backgroundColor: bgColor,
        titleColor: textColor,
        bodyColor: secondaryColor,
        borderColor: borderColor,
        borderWidth: 1
      }
    }
  }
})

// Methods
const loadStatistics = async () => {
  try {
    loading.value = true
    
    // Siempre cargar estadísticas de los últimos 7 días
    stats.value = await statisticsService.getStatisticsForPeriod(7)
  } catch (error) {
    console.error('Error cargando estadísticas:', error)
  } finally {
    loading.value = false
  }
}

const loadAnalysis = async (analysis: AnalysisHistoryEntry) => {
  try {
    // Redirigir a la página de análisis SIN parámetro reload
    // Esto permite que use el caché si existe
    router.push({
      path: '/analysis',
      query: { 
        q: analysis.query
        // NO incluir reload=true para que use caché
      }
    })
  } catch (error) {
    console.error('Error cargando análisis:', error)
    // Redirigir de todos modos
    router.push({
      path: '/analysis',
      query: { q: analysis.query }
    })
  }
}

const deleteAnalysis = async (analysisId: string) => {
  if (!confirm('¿Estás seguro de que quieres eliminar este análisis del historial?')) {
    return
  }
  
  try {
    const deleted = await statisticsService.deleteAnalysis(analysisId)
    if (deleted) {
      await loadStatistics()
      alert('Análisis eliminado exitosamente')
    } else {
      alert('No se pudo eliminar el análisis')
    }
  } catch (error) {
    console.error('Error eliminando análisis:', error)
    alert('Error al eliminar el análisis')
  }
}

const refreshHistory = async () => {
  await loadStatistics()
}

const clearAllHistory = async () => {
  if (!confirm('¿Estás seguro de que quieres eliminar todo el historial? Esta acción no se puede deshacer.')) {
    return
  }
  
  try {
    await statisticsService.cleanOldHistory(0) // Eliminar todo
    await loadStatistics()
    alert('Historial eliminado exitosamente')
  } catch (error) {
    console.error('Error limpiando historial:', error)
    alert('Error al limpiar el historial')
  }
}

const exportStatistics = async () => {
  try {
    const data = await statisticsService.exportStatistics()
    const dataStr = JSON.stringify(data, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement('a')
    link.href = url
    link.download = `diagseg-historial-${new Date().toISOString().split('T')[0]}.json`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Error exportando estadísticas:', error)
    alert('Error al exportar estadísticas')
  }
}

const clearOldData = async () => {
  if (!confirm('¿Estás seguro de que quieres limpiar datos antiguos (>7 días)?')) {
    return
  }
  
  try {
    const removed = await statisticsService.cleanOldHistory(7)
    alert(`Se eliminaron ${removed} registros antiguos`)
    await loadStatistics()
  } catch (error) {
    console.error('Error limpiando datos:', error)
    alert('Error al limpiar datos antiguos')
  }
}

// Lifecycle
onMounted(async () => {
  // Auto-limpiar datos antiguos al cargar
  await statisticsService.autoCleanOldData()
  await loadStatistics()
  
  // Observar cambios de tema
  const observer = new MutationObserver(() => {
    chartKey.value++ // Forzar re-render de gráficos cuando cambia el tema
  })
  
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  })
  
  // Cleanup
  return () => observer.disconnect()
})
</script>

<style scoped>
.history {
  min-height: 100vh;
  background: var(--bg-body);
}

.main-content {
  flex: 1;
  padding: 2rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Header */
.history-header {
  text-align: center;
  margin-bottom: 3rem;
}

.history-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.history-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin-bottom: 2rem;
}

.period-selector {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.period-btn {
  padding: 0.5rem 1rem;
  border: 2px solid var(--border-primary);
  background: var(--bg-primary);
  color: var(--text-secondary);
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.period-btn:hover,
.period-btn.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

/* Loading */
.loading-history {
  text-align: center;
  padding: 4rem;
}

.loading-spinner {
  width: 3rem;
  height: 3rem;
  border: 3px solid var(--border-primary);
  border-top: 3px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Stats Summary */
.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  background: var(--bg-primary);
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Analysis History Table */
.analysis-history {
  margin-bottom: 3rem;
}

.history-card {
  background: var(--bg-primary);
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
  overflow: hidden;
}

.history-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-primary);
}

.history-card-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.history-actions {
  display: flex;
  gap: 0.75rem;
}

.history-table-container {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
}

.history-table th {
  background: var(--bg-secondary);
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-primary);
  font-size: 0.9rem;
}

.history-table td {
  padding: 1rem;
  border-bottom: 1px solid var(--border-primary);
  vertical-align: middle;
}

.history-row:hover {
  background: var(--bg-secondary);
}

.ip-cell {
  font-family: monospace;
  font-weight: 600;
  color: var(--text-primary);
  min-width: 150px;
}

.type-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.type-badge.ipv4 { background: #DBEAFE; color: #1E40AF; }
.type-badge.ipv6 { background: #F3E8FF; color: #7C3AED; }
.type-badge.domain { background: #D1FAE5; color: #065F46; }

.score-container {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 80px;
}

.score-value {
  font-weight: 700;
  font-size: 1.1rem;
}

.score-value.score-high { color: #10B981; }
.score-value.score-medium { color: #F59E0B; }
.score-value.score-low { color: #EF4444; }

.score-bar {
  height: 4px;
  background: var(--bg-secondary);
  border-radius: 2px;
  overflow: hidden;
}

.score-progress {
  height: 100%;
  transition: width 0.3s ease;
}

.score-progress.score-high { background: #10B981; }
.score-progress.score-medium { background: #F59E0B; }
.score-progress.score-low { background: #EF4444; }

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.safe { background: #D1FAE5; color: #065F46; }
.status-badge.warning { background: #FEF3C7; color: #92400E; }
.status-badge.danger { background: #FEE2E2; color: #991B1B; }

.date-cell {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 120px;
}

.date-text {
  font-weight: 500;
  color: var(--text-primary);
}

.time-ago {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.cache-badge,
.api-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 500;
}

.cache-badge {
  background: rgba(34, 197, 94, 0.1);
  color: #15803d;
}

.api-badge {
  background: rgba(59, 130, 246, 0.1);
  color: #1d4ed8;
}

.actions-cell {
  display: flex;
  gap: 0.5rem;
}

.load-btn,
.delete-btn {
  padding: 0.5rem 0.75rem;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-btn {
  background: #3B82F6;
  color: white;
}

.load-btn:hover {
  background: #2563EB;
  transform: translateY(-1px);
}

.delete-btn {
  background: #EF4444;
  color: white;
}

.delete-btn:hover {
  background: #DC2626;
  transform: translateY(-1px);
}

.refresh-btn {
  background: #10B981;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: #059669;
  transform: translateY(-1px);
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid var(--border-primary);
}

.pagination-btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-primary);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  color: var(--text-secondary);
  font-weight: 500;
}

/* Empty State */
.empty-history {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.empty-text {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.empty-cta {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  background: var(--color-primary);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.empty-cta:hover {
  background: #667eea;
  transform: translateY(-2px);
}

/* Charts */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 2rem;
  margin-bottom: 3rem;
}

.chart-container {
  background: var(--bg-primary);
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.timeline-container {
  grid-column: span 1;
}

.chart-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.chart-wrapper {
  height: 300px;
  position: relative;
}

.empty-chart {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

/* Countries */
.countries-section {
  margin-bottom: 3rem;
}

.countries-card {
  background: var(--bg-primary);
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.countries-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.countries-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.country-item {
  display: grid;
  grid-template-columns: 1fr auto 60px;
  align-items: center;
  gap: 1rem;
}

.country-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.country-name {
  font-weight: 600;
  color: var(--text-primary);
}

.country-count {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.country-bar {
  height: 8px;
  background: var(--bg-secondary);
  border-radius: 4px;
  overflow: hidden;
  min-width: 100px;
}

.country-progress {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), #3B82F6);
  transition: width 0.3s ease;
}

.country-percentage {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-align: right;
}

/* Actions */
.history-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.export-btn {
  background: #3B82F6;
  color: white;
}

.export-btn:hover {
  background: #2563EB;
  transform: translateY(-2px);
}

.clear-btn {
  background: #EF4444;
  color: white;
}

.clear-btn:hover {
  background: #DC2626;
  transform: translateY(-2px);
}

.compare-btn {
  background: #10B981;
  color: white;
}

.compare-btn:hover {
  background: #059669;
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .history-card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .history-actions {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .history-title {
    font-size: 2rem;
  }
  
  .stats-summary {
    grid-template-columns: 1fr;
  }
  
  .history-table {
    font-size: 0.9rem;
  }
  
  .history-table th,
  .history-table td {
    padding: 0.75rem 0.5rem;
  }
  
  .actions-cell {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
