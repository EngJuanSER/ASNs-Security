<template>
  <div class="ip-comparator">
    <Navbar />
    
    <main class="main-content">
      <div class="container">
        <!-- Header -->
        <section class="comparator-header">
          <h1 class="comparator-title">Comparador de IPs</h1>
          <p class="comparator-subtitle">
            Compara hasta dos direcciones IP o dominios lado a lado
          </p>
        </section>

        <!-- Search Section -->
        <section class="search-section">
          <div class="search-grid">
            <!-- IP 1 -->
            <div class="search-column">
              <h3 class="search-title">Primera IP/Dominio</h3>
              <SearchBar 
                @search="handleSearch1"
                :placeholder="'Ej: 8.8.8.8 o google.com'"
                :loading="loading1"
              />
              
              <!-- Saved IPs for comparison -->
              <div v-if="savedComparisons.length > 0" class="saved-ips">
                <h4 class="saved-title">IPs Guardadas:</h4>
                <div class="saved-list">
                  <button 
                    v-for="saved in savedComparisons.slice(0, 5)" 
                    :key="saved.id"
                    @click="loadSavedIP(saved, 1)"
                    class="saved-btn"
                  >
                    {{ saved.query }}
                    <span class="saved-score">{{ saved.result.securityScore }}/100</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- VS Separator -->
            <div class="vs-separator">
              <div class="vs-circle">VS</div>
            </div>

            <!-- IP 2 -->
            <div class="search-column">
              <h3 class="search-title">Segunda IP/Dominio</h3>
              <SearchBar 
                @search="handleSearch2"
                :placeholder="'Ej: 1.1.1.1 o cloudflare.com'"
                :loading="loading2"
              />
              
              <!-- Saved IPs for comparison -->
              <div v-if="savedComparisons.length > 0" class="saved-ips">
                <h4 class="saved-title">IPs Guardadas:</h4>
                <div class="saved-list">
                  <button 
                    v-for="saved in savedComparisons.slice(0, 5)" 
                    :key="saved.id"
                    @click="loadSavedIP(saved, 2)"
                    class="saved-btn"
                  >
                    {{ saved.query }}
                    <span class="saved-score">{{ saved.result.securityScore }}/100</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Loading States -->
        <section v-if="loading1 || loading2" class="loading-section">
          <div class="loading-grid">
            <div class="loading-item" :class="{ active: loading1 }">
              <div v-if="loading1" class="loading-spinner"></div>
              <p>{{ loading1 ? 'Analizando primera IP...' : 'Esperando primera IP' }}</p>
            </div>
            <div class="loading-item" :class="{ active: loading2 }">
              <div v-if="loading2" class="loading-spinner"></div>
              <p>{{ loading2 ? 'Analizando segunda IP...' : 'Esperando segunda IP' }}</p>
            </div>
          </div>
        </section>

        <!-- Comparison Results -->
        <section v-if="hasAnyResult" class="comparison-section">
          <!-- Summary Comparison -->
          <div class="comparison-summary">
            <h2 class="comparison-title">Resumen de Comparación</h2>
            <div class="summary-grid">
              <!-- IP 1 Summary -->
              <ComparisonSummaryCard 
                :query="query1"
                :result="result1"
                :loading="loading1"
                :fromCache="result1FromCache"
                :isWinner="isWinner(1)"
                :winnerClass="getWinnerClass(1)"
              />

              <!-- IP 2 Summary -->
              <ComparisonSummaryCard 
                :query="query2"
                :result="result2"
                :loading="loading2"
                :fromCache="result2FromCache"
                :isWinner="isWinner(2)"
                :winnerClass="getWinnerClass(2)"
              />
            </div>
          </div>

          <!-- Detailed Comparison -->
          <div class="detailed-comparison">
            <h2 class="comparison-title">Comparación Detallada</h2>
            
            <!-- Services Comparison -->
            <ServicesComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
              :loading1="loading1"
              :loading2="loading2"
            />

            <!-- Reputation Comparison -->
            <ReputationComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
            />

            <!-- Maps Comparison -->
            <MapsComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
            />

            <!-- Network Information Comparison -->
            <NetworkInfoComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
            />

            <!-- Security Recommendations Comparison -->
            <RecommendationsComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
            />
          </div>

          <!-- Actions -->
          <div class="comparison-actions">
            <button @click="exportComparison" class="action-btn export-btn">
              Exportar Comparación
            </button>
            <button @click="saveComparison" class="action-btn save-btn">
              Guardar para Más Tarde
            </button>
            <button @click="clearComparison" class="action-btn clear-btn">
              Limpiar Comparación
            </button>
          </div>
        </section>

        <!-- Empty State -->
        <section v-if="!hasAnyResult && !hasLoading" class="empty-section">
          <div class="empty-content">
            <div class="empty-icon">⚖</div>
            <h3 class="empty-title">Comienza una Comparación</h3>
            <p class="empty-text">
              Ingresa dos direcciones IP o dominios para compararlos lado a lado y ver sus diferencias de seguridad.
            </p>
          </div>
        </section>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import SearchBar from '@/components/SearchBar.vue'
import ComparisonSummaryCard from '@/components/ComparisonSummaryCard.vue'
import ServicesComparison from '@/components/ServicesComparison.vue'
import ReputationComparison from '@/components/ReputationComparison.vue'
import MapsComparison from '@/components/MapsComparison.vue'
import NetworkInfoComparison from '@/components/NetworkInfoComparison.vue'
import RecommendationsComparison from '@/components/RecommendationsComparison.vue'
import { analysisService, type AnalysisResult } from '@/services/api'
import { cacheService } from '@/services/cache'
import { statisticsService, type IPComparisonData } from '@/services/statistics'

// Estado del componente
const loading1 = ref(false)
const loading2 = ref(false)
const query1 = ref('')
const query2 = ref('')
const result1 = ref<AnalysisResult | null>(null)
const result2 = ref<AnalysisResult | null>(null)
const result1FromCache = ref(false)
const result2FromCache = ref(false)
const savedComparisons = ref<IPComparisonData[]>([])

// Computed properties
const hasComparison = computed(() => result1.value && result2.value)
const hasAnyResult = computed(() => result1.value || result2.value)
const hasLoading = computed(() => loading1.value || loading2.value)

const isWinner = (ip: number) => {
  if (!hasComparison.value) return false
  const score1 = result1.value!.securityScore
  const score2 = result2.value!.securityScore
  
  if (score1 === score2) return false
  return ip === 1 ? score1 > score2 : score2 > score1
}

const getWinnerClass = (ip: number) => {
  if (isWinner(ip)) return 'winner'
  if (hasComparison.value && result1.value!.securityScore !== result2.value!.securityScore) {
    return 'loser'
  }
  return ''
}

const getScoreClass = (score: number) => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

// Methods
const handleSearch1 = async (searchQuery: string) => {
  await handleSearch(searchQuery, 1)
}

const handleSearch2 = async (searchQuery: string) => {
  await handleSearch(searchQuery, 2)
}

const handleSearch = async (searchQuery: string, ipNumber: number) => {
  if (!searchQuery.trim()) return

  const query = searchQuery.trim()
  
  try {
    // Determinar qué referencias usar según el número de IP
    if (ipNumber === 1) {
      query1.value = query
      loading1.value = true
      result1.value = null
      result1FromCache.value = false
    } else {
      query2.value = query
      loading2.value = true
      result2.value = null
      result2FromCache.value = false
    }

    // Verificar caché primero
    const cachedResult = await cacheService.get(query)
    
    if (cachedResult) {
      // Usar resultado del caché
      if (ipNumber === 1) {
        result1.value = cachedResult
        result1FromCache.value = true
        loading1.value = false
      } else {
        result2.value = cachedResult
        result2FromCache.value = true
        loading2.value = false
      }
      return
    }

    // Hacer análisis completo
    console.log(`Analizando ${query} para posición ${ipNumber}...`)
    const response = await analysisService.analyzeIP(query)
    console.log(`Respuesta para ${query}:`, response)
    
    if (response.success && response.data) {
      // Asignar resultado según la posición
      if (ipNumber === 1) {
        result1.value = response.data
        result1FromCache.value = false
        loading1.value = false
      } else {
        result2.value = response.data
        result2FromCache.value = false
        loading2.value = false
      }
      
      // Guardar en caché
      await cacheService.set(query, response.data, 30)
      
      // Registrar en estadísticas
      await statisticsService.recordAnalysis(query, response.data, false)
      
      console.log(`Análisis completado para ${query}`)
    } else {
      console.error('Error en análisis:', response.error)
      alert(`Error analizando ${query}: ${response.error || 'Error desconocido'}`)
    }
  } catch (error) {
    console.error('Error en búsqueda:', error)
    alert(`Error durante el análisis de ${query}: ${error}`)
  } finally {
    // Asegurar que loading se desactive
    if (ipNumber === 1) {
      loading1.value = false
    } else {
      loading2.value = false
    }
  }
}

const loadSavedIP = async (saved: IPComparisonData, ipNumber: number) => {
  const queryRef = ipNumber === 1 ? query1 : query2
  const resultRef = ipNumber === 1 ? result1 : result2
  const cacheRef = ipNumber === 1 ? result1FromCache : result2FromCache

  queryRef.value = saved.query
  resultRef.value = saved.result
  cacheRef.value = true
}

const loadSavedComparisons = async () => {
  try {
    savedComparisons.value = await statisticsService.getComparisonData()
  } catch (error) {
    console.error('Error cargando comparaciones guardadas:', error)
  }
}

const exportComparison = async () => {
  if (!hasComparison.value) return

  try {
    const comparisonData = {
      timestamp: new Date().toISOString(),
      comparison: {
        ip1: {
          query: query1.value,
          result: result1.value,
          fromCache: result1FromCache.value
        },
        ip2: {
          query: query2.value,
          result: result2.value,
          fromCache: result2FromCache.value
        }
      },
      summary: {
        winner: isWinner(1) ? query1.value : isWinner(2) ? query2.value : 'Empate',
        scoreDifference: Math.abs(result1.value!.securityScore - result2.value!.securityScore)
      }
    }

    const dataStr = JSON.stringify(comparisonData, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement('a')
    link.href = url
    link.download = `comparacion-${query1.value}-vs-${query2.value}-${Date.now()}.json`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Error exportando comparación:', error)
    alert('Error al exportar la comparación')
  }
}

const saveComparison = async () => {
  if (!hasComparison.value) return

  try {
    await statisticsService.saveForComparison(query1.value, result1.value!)
    await statisticsService.saveForComparison(query2.value, result2.value!)
    await loadSavedComparisons()
    alert('Comparación guardada exitosamente')
  } catch (error) {
    console.error('Error guardando comparación:', error)
    alert('Error al guardar la comparación')
  }
}

const clearComparison = () => {
  query1.value = ''
  query2.value = ''
  result1.value = null
  result2.value = null
  result1FromCache.value = false
  result2FromCache.value = false
}

// Lifecycle
onMounted(() => {
  loadSavedComparisons()
})
</script>

<style scoped>
.ip-comparator {
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
.comparator-header {
  text-align: center;
  margin-bottom: 3rem;
}

.comparator-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.comparator-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
}

/* Search Section */
.search-section {
  margin-bottom: 3rem;
}

.search-grid {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 2rem;
  align-items: start;
}

.search-column {
  background: var(--bg-primary);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.search-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.vs-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 4rem;
}

.vs-circle {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--color-primary), #3B82F6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1.2rem;
  box-shadow: var(--shadow-lg);
}

/* Saved IPs */
.saved-ips {
  margin-top: 1.5rem;
}

.saved-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.saved-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.saved-btn {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: monospace;
}

.saved-btn:hover {
  background: var(--color-primary);
  color: white;
  transform: translateY(-1px);
}

.saved-score {
  font-size: 0.8rem;
  font-weight: 600;
  opacity: 0.8;
}

/* Loading */
.loading-section {
  margin-bottom: 3rem;
}

.loading-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.loading-item {
  text-align: center;
  padding: 2rem;
  background: var(--bg-primary);
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
  opacity: 0.6;
  transition: opacity 0.3s ease;
}

.loading-item.active {
  opacity: 1;
  border-color: var(--color-primary);
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

/* Comparison Summary */
.comparison-summary {
  margin-bottom: 3rem;
}

.comparison-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.summary-card {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  min-height: 250px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.loading-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-color);
  border-top: 3px solid #3B82F6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

.empty-card {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-secondary);
  font-style: italic;
}

.empty-card p {
  margin: 0;
}

.summary-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-card.winner {
  border-color: #10B981;
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.2);
}

.summary-card.loser {
  opacity: 0.8;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.summary-ip {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  font-family: monospace;
}

.summary-badges {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.winner-badge {
  background: linear-gradient(135deg, #10B981, #059669);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.cache-badge {
  background: rgba(34, 197, 94, 0.1);
  color: #15803d;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.summary-score {
  text-align: center;
  margin-bottom: 1.5rem;
}

.score-number {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.score-number.score-high { color: #10B981; }
.score-number.score-medium { color: #F59E0B; }
.score-number.score-low { color: #EF4444; }

.score-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.summary-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-weight: 600;
  color: var(--text-secondary);
}

.detail-value {
  color: var(--text-primary);
  font-weight: 500;
}

/* Detailed Comparison */
.detailed-comparison {
  margin-bottom: 3rem;
}

.comparison-block {
  background: var(--bg-primary);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
  margin-bottom: 2rem;
}

.block-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.services-comparison,
.maps-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.services-column,
.map-column {
  border-right: 1px solid var(--border-primary);
  padding-right: 1rem;
}

.services-column:last-child,
.map-column:last-child {
  border-right: none;
  padding-right: 0;
  padding-left: 1rem;
}

.column-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
  font-family: monospace;
}

/* Services */
.services-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.service-item {
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  border-left: 4px solid var(--border-primary);
}

.service-item.low { border-left-color: #10B981; }
.service-item.medium { border-left-color: #F59E0B; }
.service-item.high { border-left-color: #EF4444; }

.service-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.service-port {
  font-family: monospace;
  font-weight: 700;
  color: var(--color-primary);
}

.service-name {
  font-weight: 600;
  color: var(--text-primary);
}

.service-details {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

/* Loading and Empty States */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  gap: 1rem;
  text-align: center;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.loading-state p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
  background: var(--bg-secondary);
  border-radius: 8px;
  color: var(--text-secondary);
  font-style: italic;
}

.empty-state p {
  margin: 0;
}

/* Actions */
.comparison-actions {
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
  display: flex;
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

.save-btn {
  background: #10B981;
  color: white;
}

.save-btn:hover {
  background: #059669;
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

/* Empty State */
.empty-section {
  text-align: center;
  padding: 4rem;
}

.empty-content {
  max-width: 500px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.empty-text {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Responsive */
@media (max-width: 1024px) {
  .search-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .vs-separator {
    padding: 1rem 0;
  }
  
  .network-comparison,
  .recommendations-comparison {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
}

/* Animations */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 1024px) {
  .search-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .vs-separator {
    padding: 1rem 0;
  }
  
  .network-comparison,
  .recommendations-comparison {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .vs-circle {
    transform: rotate(90deg);
  }
  
  .summary-grid,
  .services-comparison,
  .reputation-comparison,
  .maps-comparison {
    grid-template-columns: 1fr;
  }
  
  .services-column,
  .reputation-column,
  .map-column {
    border-right: none;
    padding-right: 0;
    padding-left: 0;
    border-bottom: 1px solid var(--border-primary);
    padding-bottom: 1rem;
    margin-bottom: 1rem;
  }
  
  .services-column:last-child,
  .reputation-column:last-child,
  .map-column:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
}

@media (max-width: 768px) {
  .comparator-title {
    font-size: 2rem;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
