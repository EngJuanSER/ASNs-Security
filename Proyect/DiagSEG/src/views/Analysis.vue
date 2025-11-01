<template>
  <div class="analysis">
    <Navbar />
    
    <main>
      <!-- Header Section -->
      <section class="analysis-header">
        <div class="container">
          <div class="header-content">
            <h1 class="page-title">Análisis de Seguridad IP</h1>
            <p class="page-subtitle">
              Diagnóstico completo para {{ query || 'dirección IP' }}
            </p>
            
            <!-- Search Bar -->
            <div class="search-section">
              <SearchBar 
                @search="handleSearch" 
                :loading="loading"
                :error="searchError"
                :value="query"
                placeholder="Ingresa una IP o dominio para analizar..."
              />
            </div>
          </div>
        </div>
      </section>

      <!-- Results Section -->
      <section class="results-section" v-if="hasResults">
        <div class="container">
          <div class="results-grid">
            <!-- Información General -->
            <div class="result-card overview-card">
              <div class="card-header">
                <h3 class="card-title">Información General</h3>
                <div class="security-score" :class="getScoreClass(results.securityScore)">
                  <div class="score-value">{{ results.securityScore }}/100</div>
                  <div class="score-label">Score de Seguridad</div>
                </div>
              </div>
              <div class="card-content">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-label">IP:</span>
                    <span class="info-value">{{ results.ip }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">Tipo:</span>
                    <span class="info-value">{{ results.type.toUpperCase() }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">Timestamp:</span>
                    <span class="info-value">{{ new Date(results.timestamp).toLocaleString() }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Geolocalización -->
            <div class="result-card geo-card">
              <div class="card-header">
                <h3 class="card-title">Geolocalización</h3>
              </div>
              <div class="card-content">
                <GeolocationMap 
                  :geolocation="results.geolocation"
                  :loading="mapLoading"
                  :error="mapError"
                />
              </div>
            </div>

            <!-- Servicios Detectados -->
            <div class="result-card services-card">
              <div class="card-header">
                <h3 class="card-title">Servicios Activos</h3>
                <span class="service-count">{{ results.services.length }} detectados</span>
              </div>
              <div class="card-content">
                <div class="services-list">
                  <div 
                    v-for="service in results.services" 
                    :key="service.port"
                    class="service-item"
                    :class="service.riskLevel"
                  >
                    <div class="service-port">{{ service.port }}</div>
                    <div class="service-details">
                      <div class="service-name">{{ service.name }}</div>
                      <div class="service-version">{{ service.version }}</div>
                    </div>
                    <div class="service-risk" :class="service.riskLevel">
                      {{ service.riskText }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Análisis de Reputación -->
            <div class="result-card reputation-card">
              <div class="card-header">
                <h3 class="card-title">Análisis de Reputación</h3>
              </div>
              <div class="card-content">
                <div class="reputation-sources">
                  <div 
                    v-for="source in results.reputation" 
                    :key="source.name"
                    class="reputation-item"
                  >
                    <div class="source-name">{{ source.name }}</div>
                    <div class="source-status" :class="source.status">
                      {{ source.statusText }}
                    </div>
                    <div class="source-details" v-if="source.details">
                      {{ source.details }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Recomendaciones -->
          <div class="recommendations-section" v-if="results.recommendations.length">
            <h3 class="recommendations-title">Recomendaciones de Seguridad</h3>
            <div class="recommendations-grid">
              <div 
                v-for="(rec, index) in results.recommendations" 
                :key="index"
                class="recommendation-item"
                :class="rec.priority"
              >
                <div class="rec-priority">{{ rec.priority.toUpperCase() }}</div>
                <div class="rec-content">
                  <h4 class="rec-title">{{ rec.title }}</h4>
                  <p class="rec-description">{{ rec.description }}</p>
                  <p v-if="rec.action" class="rec-action">Acción: {{ rec.action }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Export Actions -->
          <div class="export-section">
            <div class="export-header">
              <h3 class="export-title">Exportar Resultados</h3>
              <div class="cache-indicator" v-if="fromCache">
                <span class="cache-badge">Desde caché</span>
              </div>
            </div>
            <div class="export-buttons">
              <button 
                class="export-btn pdf-btn" 
                @click="exportToPDF"
                :disabled="pdfExporting"
              >
                <span v-if="!pdfExporting">Exportar PDF</span>
                <span v-else>⏳ Generando PDF...</span>
              </button>
              <button class="export-btn json-btn" @click="exportToJSON">
                Exportar JSON
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Loading State -->
      <section class="loading-section" v-if="loading">
        <div class="container">
          <div class="loading-content">
            <div class="loading-spinner"></div>
            <h3 class="loading-title">Analizando {{ query }}...</h3>
            <p class="loading-subtitle">
              Consultando múltiples fuentes de datos para obtener información completa
            </p>
            <div class="loading-steps">
              <div class="loading-step" :class="{ active: currentStep >= 1 }">
                Resolviendo dirección IP
              </div>
              <div class="loading-step" :class="{ active: currentStep >= 2 }">
                Consultando geolocalización
              </div>
              <div class="loading-step" :class="{ active: currentStep >= 3 }">
                Escaneando servicios
              </div>
              <div class="loading-step" :class="{ active: currentStep >= 4 }">
                Analizando reputación
              </div>
              <div class="loading-step" :class="{ active: currentStep >= 5 }">
                Generando reporte
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Empty State -->
      <section class="empty-section" v-if="!hasResults && !loading">
        <div class="container">
          <div class="empty-content">
            <div class="empty-icon">
              <svg width="48" height="48" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
              </svg>
            </div>
            <h3 class="empty-title">¿Listo para comenzar?</h3>
            <p class="empty-subtitle">
              Ingresa una dirección IP o dominio en el buscador para comenzar el análisis de seguridad.
            </p>
            <div class="empty-examples">
              <p>Ejemplos que puedes probar:</p>
              <div class="example-buttons">
                <button 
                  v-for="example in examples" 
                  :key="example"
                  @click="handleSearch(example)"
                  class="example-btn"
                >
                  {{ example }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <Footer/>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import SearchBar from '@/components/SearchBar.vue'
import GeolocationMap from '@/components/GeolocationMap.vue'
import { analysisService, type AnalysisResult } from '@/services/api'
import { cacheService } from '@/services/cache'
import { pdfExportService } from '@/services/pdf-export'
import { statisticsService } from '@/services/statistics'

const route = useRoute()
const loading = ref(false)
const searchError = ref('')
const query = ref('')
const results = ref<AnalysisResult | null>(null)
const currentStep = ref(0)
const mapLoading = ref(false)
const mapError = ref('')
const fromCache = ref(false)
const pdfExporting = ref(false)

const examples = ['8.8.8.8', 'google.com', '1.1.1.1', 'github.com']

const hasResults = computed(() => results.value !== null)

const getScoreClass = (score: number) => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

const loadingSteps = [
  'Validando entrada...',
  'Consultando geolocalización...',
  'Analizando servicios...',
  'Verificando reputación...',
  'Generando recomendaciones...',
  'Calculando score de seguridad...'
]

const handleSearch = async (searchQuery: string) => {
  if (!searchQuery.trim()) return

  query.value = searchQuery.trim()
  loading.value = true
  searchError.value = ''
  results.value = null
  currentStep.value = 0
  fromCache.value = false

  // Verificar si viene del historial (parámetro reload)
  const isFromHistory = route.query.reload === 'true'

  try {
    // Verificar caché primero, solo si no viene del historial
    const cachedResult = !isFromHistory ? await cacheService.get(query.value) : null
    
    if (cachedResult && !isFromHistory) {
      // Usar resultado del caché
      results.value = cachedResult
      fromCache.value = true
      mapLoading.value = false
      mapError.value = ''
      loading.value = false
      
      // Registrar uso de caché en estadísticas
      await statisticsService.recordAnalysis(query.value, cachedResult, true)
      return
    }

    // Si no hay caché o viene del historial, hacer análisis completo con pasos simulados
    const stepInterval = setInterval(() => {
      if (currentStep.value < loadingSteps.length - 1) {
        currentStep.value++
      }
    }, 800)

    const response = await analysisService.analyzeIP(query.value)
    
    if (response.success && response.data) {
      results.value = response.data
      mapLoading.value = false
      mapError.value = ''
      
      // Guardar en caché para futuros usos (30 minutos de TTL)
      await cacheService.set(query.value, response.data, 30)
      
      // Solo registrar en estadísticas si NO viene del historial
      if (!isFromHistory) {
        await statisticsService.recordAnalysis(query.value, response.data, false)
      }
    } else {
      searchError.value = response.error || 'Error en el análisis'
      mapError.value = response.error || 'Error cargando mapa'
    }
    
    clearInterval(stepInterval)
  } catch (error) {
    console.error('Analysis error:', error)
    searchError.value = 'Error de conexión'
    mapError.value = 'Error de conexión'
  } finally {
    loading.value = false
    currentStep.value = 0
  }
}

const handleExampleClick = (example: string) => {
  handleSearch(example)
}

const exportToPDF = async () => {
  if (!results.value) return
  
  pdfExporting.value = true
  
  try {
    await pdfExportService.exportAnalysisToPDF(query.value, results.value)
  } catch (error) {
    console.error('Error exportando PDF:', error)
    alert('Error al generar el archivo PDF. Por favor, inténtelo de nuevo.')
  } finally {
    pdfExporting.value = false
  }
}

const exportToJSON = () => {
  if (!results.value) return
  
  const dataStr = JSON.stringify(results.value, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  
  const link = document.createElement('a')
  link.href = url
  link.download = `analysis-${results.value.ip}-${Date.now()}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  URL.revokeObjectURL(url)
}

onMounted(() => {
  // Si hay query en la URL, comenzar análisis automáticamente
  if (route.query.q) {
    handleSearch(route.query.q as string)
  }
})
</script>

<style scoped>
.analysis {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

main {
  flex: 1;
}

/* Header Section */
.analysis-header {
  background: var(--header-gradient, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  color: var(--header-text, white);
  padding: 2rem 0 3rem;
}

[data-theme="dark"] .analysis-header {
  --header-gradient: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  --header-text: var(--color-gray-100);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.header-content {
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.page-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 2rem;
}

.search-section {
  margin-bottom: 1rem;
}

/* Results Section */
.results-section {
  padding: 3rem 0;
  background: var(--bg-secondary);
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.result-card {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
  transition: all 0.3s ease;
}

.result-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-primary);
}

.card-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
}

.security-score {
  text-align: center;
  padding: 0.75rem;
  border-radius: 8px;
  min-width: 80px;
}

.score-high { background: #c6f6d5; color: #22543d; }
.score-medium { background: #fefcbf; color: #744210; }
.score-low { background: #fed7d7; color: #742a2a; }

.score-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.score-label {
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

/* Info Grid */
.info-grid {
  display: grid;
  gap: 1rem;
}

.info-item, .geo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: var(--bg-secondary);
  border-radius: 6px;
  border: 1px solid var(--border-primary);
}

.info-label, .geo-label {
  font-weight: 500;
  color: var(--text-secondary);
}

.info-value, .geo-value {
  font-weight: 600;
  color: var(--text-primary);
}

.status.safe { color: #22543d; }
.status.warning { color: #744210; }
.status.danger { color: #742a2a; }

/* Map Placeholder */
.map-placeholder {
  margin-top: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  border: 2px dashed var(--border-secondary);
  color: var(--text-muted);
}

.map-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

/* Services */
.service-count {
  background: #667eea;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.services-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  border-left: 4px solid var(--border-primary);
  border: 1px solid var(--border-primary);
}

.service-item.low { border-left-color: var(--color-success); }
.service-item.medium { border-left-color: var(--color-warning); }
.service-item.high { border-left-color: var(--color-error); }

.service-port {
  font-weight: 700;
  color: var(--text-primary);
  min-width: 50px;
}

.service-details {
  flex: 1;
}

.service-name {
  font-weight: 600;
  color: var(--text-primary);
}

.service-version {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.service-risk {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.service-risk.low { background: #c6f6d5; color: #22543d; }
.service-risk.medium { background: #fefcbf; color: #744210; }
.service-risk.high { background: #fed7d7; color: #742a2a; }

/* Reputation */
.reputation-sources {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.reputation-item {
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-primary);
}

.source-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.source-status {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.source-status.clean { background: #c6f6d5; color: #22543d; }
.source-status.suspicious { background: #fefcbf; color: #744210; }
.source-status.malicious { background: #fed7d7; color: #742a2a; }

.source-details {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

/* Recommendations */
.recommendations-section {
  margin-bottom: 3rem;
}

.recommendations-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.recommendations-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.recommendation-item {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  background: var(--bg-primary);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  border-left: 4px solid var(--border-primary);
  border: 1px solid var(--border-primary);
}

.recommendation-item.high { border-left-color: var(--color-error); }
.recommendation-item.medium { border-left-color: var(--color-warning); }
.recommendation-item.low { border-left-color: var(--color-success); }
.recommendation-item.info { border-left-color: var(--color-info); }

.rec-priority {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  height: fit-content;
}

.recommendation-item.high .rec-priority { background: #fed7d7; color: #742a2a; }
.recommendation-item.medium .rec-priority { background: #fefcbf; color: #744210; }
.recommendation-item.low .rec-priority { background: #c6f6d5; color: #22543d; }
.recommendation-item.info .rec-priority { background: #bee3f8; color: #2c5282; }

.rec-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.rec-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 0.5rem;
}

.rec-action {
  color: var(--text-muted);
  font-size: 0.85rem;
  font-style: italic;
  margin: 0;
}

/* Export Section */
.export-section {
  text-align: center;
  padding: 2rem;
  background: var(--bg-primary);
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.export-header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.export-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.cache-indicator {
  display: flex;
  align-items: center;
}

.cache-badge {
  background: rgba(34, 197, 94, 0.1);
  color: #15803d;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.export-buttons {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.export-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 140px;
}

.export-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.pdf-btn {
  background: #e53e3e;
  color: white;
}

.pdf-btn:hover {
  background: #c53030;
  transform: translateY(-2px);
}

.json-btn {
  background: #667eea;
  color: white;
}

.json-btn:hover {
  background: #5a67d8;
  transform: translateY(-2px);
}

/* Loading Section */
.loading-section {
  padding: 5rem 0;
  background: var(--bg-secondary);
}

.loading-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid var(--border-primary);
  border-top: 4px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 2rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.loading-subtitle {
  color: var(--text-secondary);
  margin-bottom: 2rem;
}

.loading-steps {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  text-align: left;
  max-width: 300px;
  margin: 0 auto;
}

.loading-step {
  padding: 0.75rem 1rem;
  background: var(--bg-tertiary);
  border-radius: 6px;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.loading-step.active {
  background: var(--color-primary);
  color: white;
  transform: translateX(10px);
}

/* Empty Section */
.empty-section {
  padding: 5rem 0;
  background: var(--bg-secondary);
}

.empty-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
}

.empty-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.empty-subtitle {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.empty-examples {
  background: var(--bg-primary);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.empty-examples p {
  margin-bottom: 1rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.example-buttons {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.example-btn {
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: var(--color-primary);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.example-btn:hover {
  background: var(--color-primary);
  color: white;
  transform: translateY(-2px);
}

.example-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

/* Mobile styles */
@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  .page-title {
    font-size: 2rem;
  }

  .results-grid {
    grid-template-columns: 1fr;
  }

  .export-buttons {
    flex-direction: column;
    align-items: center;
  }

  .export-btn {
    width: 200px;
  }

  .loading-steps {
    max-width: 100%;
  }

  .example-buttons {
    flex-direction: column;
    align-items: center;
  }

  .example-btn {
    width: 200px;
  }
}
</style>
