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
            <!-- Análisis de Seguridad Unificado -->
            <div class="result-card security-analysis-card">
              <div class="card-header">
                <h3 class="card-title">Análisis de Seguridad</h3>
              </div>
              <div class="card-content">
                <div class="security-visual-container">
                  <!-- Esfera de Líquido Animada (Componente) -->
                  <LiquidScoreSphere :score="results.securityScore" />

                  <!-- Detalles del Análisis -->
                  <div class="security-details">
                    <!-- Resumen usando componente -->
                    <SecuritySummary 
                      :servicesCount="results.services.length"
                      :vulnerabilitiesCount="results.vulnerabilities.length"
                      :sourcesCount="results.metadata?.sourcesUsed?.length || 0"
                      :scanDuration="results.metadata?.scanDuration"
                    />

                    <!-- Análisis de Reputación usando componente -->
                    <ReputationAnalysis :reputation="results.reputation" />

                    <!-- Vulnerabilidades Críticas usando componente -->
                    <VulnerabilitiesSummaryCard :vulnerabilities="results.vulnerabilities" />
                  </div>
                </div>
              </div>
            </div>

            <!-- Información General (sin score, ya está arriba) -->
            <!-- Información General -->
            <div class="result-card info-card">
              <div class="card-header">
                <h3 class="card-title">
                  <svg class="title-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Información General
                </h3>
              </div>
              <div class="card-content">
                <!-- Domain Preview con Favicon (si es un dominio) -->
                <div class="domain-preview" v-if="results.domain">
                  <div class="domain-favicon-container">
                    <img 
                      :src="`https://www.google.com/s2/favicons?domain=${results.domain}&sz=64`" 
                      :alt="`Favicon de ${results.domain}`"
                      class="domain-favicon"
                      @error="handleFaviconError"
                    />
                  </div>
                  <div class="domain-info-main">
                    <h4 class="domain-name">{{ results.domain }}</h4>
                    <a 
                      :href="`https://${results.domain}`" 
                      target="_blank" 
                      rel="noopener noreferrer"
                      class="domain-link"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                      </svg>
                      Visitar sitio web
                    </a>
                  </div>
                  <div class="domain-badge">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0121 12c0 6.627-5.373 12-12 12S-3 18.627-3 12 2.373 0 9 0c2.572 0 4.961.821 6.916 2.203" />
                    </svg>
                    Dominio Verificado
                  </div>
                </div>

                <div class="info-grid">
                  <div class="info-item" v-if="results.domain">
                    <span class="info-label">
                      <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
                      </svg>
                      Dominio
                    </span>
                    <span class="info-value">{{ results.domain }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">
                      <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      IP
                    </span>
                    <span class="info-value">{{ results.ip }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">
                      <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                      </svg>
                      Tipo
                    </span>
                    <span class="info-value info-type" :class="results.type.toLowerCase()">{{ results.type.toUpperCase() }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">
                      <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      Timestamp
                    </span>
                    <span class="info-value">{{ new Date(results.timestamp).toLocaleString() }}</span>
                  </div>
                  <div class="info-item" v-if="results.geolocation?.country">
                    <span class="info-label">
                      <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 21v-4m0 0V5a2 2 0 012-2h6.5l1 1H21l-3 6 3 6h-8.5l-1-1H5a2 2 0 00-2 2zm9-13.5V9" />
                      </svg>
                      País
                    </span>
                    <span class="info-value">{{ results.geolocation.country }}</span>
                  </div>
                  <div class="info-item" v-if="results.geolocation?.isp">
                    <span class="info-label">
                      <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" />
                      </svg>
                      ISP
                    </span>
                    <span class="info-value">{{ results.geolocation.isp }}</span>
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
          </div>

          <!-- Servicios Activos - Fila separada -->
          <div class="services-row">
            <ServicesSection 
              :services="results.services"
              :vulnerabilities="results.vulnerabilities"
            />
          </div>

          <!-- Vulnerabilidades y más contenido -->
          <div class="results-grid">
            <!-- Vulnerabilidades Detectadas -->
            <div class="result-card vulnerabilities-card" v-if="results.vulnerabilities && results.vulnerabilities.length > 0">
              <div class="card-content">
                <VulnerabilitiesSection :vulnerabilities="results.vulnerabilities" />
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

          <!-- Resumen Ejecutivo (antes de exportar) -->
          <ExecutiveSummary 
            v-if="results"
            :result="results"
            :query="query"
          />

          <!-- Export Actions -->
          <div class="export-section">
            <div class="export-header">
              <h3 class="export-title">Exportar Resultados</h3>
              
              <!-- Cache info y botón actualizar (en la misma línea del título) -->
              <div class="cache-info-container" v-if="fromCache">
                <div class="cache-badge">
                  <svg class="cache-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" />
                  </svg>
                  <span>Datos desde caché</span>
                </div>
                <button class="reload-btn" @click="forceReloadAnalysis" title="Forzar nuevo análisis">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                  <span>Actualizar</span>
                </button>
              </div>
            </div>
            
            <!-- Botones de exportación -->
            <div class="export-buttons">
              <button 
                class="export-btn pdf-btn" 
                @click="exportToPDF"
                :disabled="pdfExporting"
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                </svg>
                <span v-if="!pdfExporting">Exportar PDF</span>
                <span v-else>Generando PDF...</span>
              </button>
              <button class="export-btn json-btn" @click="exportToJSON">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <span>Exportar JSON</span>
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Loading State -->
      <section class="loading-section" v-if="loading">
        <div class="container">
          <div class="loading-content">
            <!-- Animación de radar interactiva -->
            <div class="radar-container" @click="toggleRadarSpeed">
              <svg class="radar-svg" viewBox="0 0 200 200">
                <!-- Círculos concéntricos -->
                <circle cx="100" cy="100" r="90" class="radar-ring ring-1" />
                <circle cx="100" cy="100" r="65" class="radar-ring ring-2" />
                <circle cx="100" cy="100" r="40" class="radar-ring ring-3" />
                
                <!-- Líneas de guía -->
                <line x1="100" y1="10" x2="100" y2="190" class="radar-line" />
                <line x1="10" y1="100" x2="190" y2="100" class="radar-line" />
                
                <!-- Haz del radar (rotating) -->
                <g class="radar-beam" :class="{ 'speed-fast': radarFast }">
                  <path d="M 100 100 L 100 10 A 90 90 0 0 1 190 100 Z" class="beam-fill" />
                  <line x1="100" y1="100" x2="100" y2="10" class="beam-line" />
                </g>
                
                <!-- Centro del radar -->
                <circle cx="100" cy="100" r="8" class="radar-center" />
                
                <!-- Puntos de actividad por paso -->
                <circle 
                  v-for="n in currentStep" 
                  :key="n"
                  :cx="100 + Math.cos((n * 72 - 90) * Math.PI / 180) * 70" 
                  :cy="100 + Math.sin((n * 72 - 90) * Math.PI / 180) * 70" 
                  r="6" 
                  class="activity-dot"
                />
              </svg>
            </div>
            
            <h3 class="loading-title">Analizando {{ query }}...</h3>
            <p class="loading-subtitle">
              Consultando múltiples fuentes de datos
            </p>
            
            <!-- Barra de progreso real -->
            <div class="progress-bar-container">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: realProgress + '%' }"></div>
              </div>
              <span class="progress-text">{{ realProgress }}%</span>
            </div>
            
            <!-- Pasos con tiempo estimado - INTERACTIVOS CON FÍSICA -->
            <div class="loading-steps" ref="stepsContainer">
              <div 
                v-for="(step, index) in loadingStepsData" 
                :key="index"
                class="loading-step" 
                :class="{ 
                  active: currentStep > index,
                  current: currentStep === index + 1,
                  pending: currentStep <= index,
                  launched: step.isLaunched
                }"
                :style="{
                  transform: step.isLaunched ? `translate(${step.x}px, ${step.y}px) rotate(${step.rotation}deg)` : '',
                  transition: step.isLaunched ? 'none' : 'all 0.3s ease',
                  cursor: 'pointer'
                }"
                @click="launchStep(index)"
              >
                <div class="step-icon">
                  <svg v-if="currentStep > index" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                  </svg>
                  <div v-else-if="currentStep === index + 1" class="spinner-small"></div>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div class="step-content">
                  <span class="step-text">{{ step.text }}</span>
                  <span class="step-time">{{ step.estimatedTime }}</span>
                </div>
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
import { useRoute, useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import SearchBar from '@/components/SearchBar.vue'
import GeolocationMap from '@/components/GeolocationMap.vue'
import VulnerabilitiesSection from '@/components/VulnerabilitiesSection.vue'
import LiquidScoreSphere from '@/components/LiquidScoreSphere.vue'
import SecuritySummary from '@/components/SecuritySummary.vue'
import ReputationAnalysis from '@/components/ReputationAnalysis.vue'
import VulnerabilitiesSummaryCard from '@/components/VulnerabilitiesSummaryCard.vue'
import ServicesSection from '@/components/ServicesSection.vue'
import ExecutiveSummary from '@/components/ExecutiveSummary.vue'
import { analysisService, type AnalysisResult } from '@/services/api'
import { cacheService } from '@/services/cache'
import { pdfExportService } from '@/services/pdf-export'
import { statisticsService } from '@/services/statistics'
import { toast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const searchError = ref('')
const query = ref('')
const results = ref<AnalysisResult | null>(null)
const currentStep = ref(0)
const mapLoading = ref(false)
const mapError = ref('')
const fromCache = ref(false)
const pdfExporting = ref(false)
const realProgress = ref(0)
const radarFast = ref(false)
const stepsContainer = ref<HTMLElement | null>(null)

// Tipo extendido para los pasos con propiedades de física
interface InteractiveStep {
  text: string
  estimatedTime: string
  isLaunched?: boolean
  x?: number
  y?: number
  vx?: number
  vy?: number
  rotation?: number
  angularVelocity?: number
}

const loadingStepsData = ref<InteractiveStep[]>([
  { text: 'Resolviendo dirección IP', estimatedTime: '~1s', isLaunched: false, x: 0, y: 0, vx: 0, vy: 0, rotation: 0, angularVelocity: 0 },
  { text: 'Consultando geolocalización', estimatedTime: '~2s', isLaunched: false, x: 0, y: 0, vx: 0, vy: 0, rotation: 0, angularVelocity: 0 },
  { text: 'Escaneando servicios (Nmap)', estimatedTime: '~60s', isLaunched: false, x: 0, y: 0, vx: 0, vy: 0, rotation: 0, angularVelocity: 0 },
  { text: 'Consultando vulnerabilidades (NVD)', estimatedTime: '~10s', isLaunched: false, x: 0, y: 0, vx: 0, vy: 0, rotation: 0, angularVelocity: 0 },
  { text: 'Generando reporte final', estimatedTime: '~2s', isLaunched: false, x: 0, y: 0, vx: 0, vy: 0, rotation: 0, angularVelocity: 0 }
])
const examples = ['8.8.8.8', 'google.com', '1.1.1.1', 'github.com']

const hasResults = computed(() => results.value !== null)

const toggleRadarSpeed = () => {
  radarFast.value = !radarFast.value
}

// Sistema de física mejorado - sin partículas, con colisiones
const launchStep = (index: number) => {
  const step = loadingStepsData.value[index]
  
  // Si ya está volando, resetear su posición
  if (step.isLaunched) {
    step.isLaunched = false
    step.x = 0
    step.y = 0
    step.vx = 0
    step.vy = 0
    step.rotation = 0
    return
  }
  
  // Lanzar el paso en una dirección aleatoria por TODA la pantalla
  const angle = Math.random() * Math.PI * 2
  const speed = 20 + Math.random() * 15  // Mayor velocidad para cubrir toda la pantalla
  
  step.isLaunched = true
  step.vx = Math.cos(angle) * speed
  step.vy = Math.sin(angle) * speed
  step.rotation = 0
  step.angularVelocity = (Math.random() - 0.5) * 10 // Rotación
  
  // Iniciar física si no está corriendo
  if (!physicsRunning.value) {
    startPhysics()
  }
}

// Sistema de física con colisiones RECTANGULARES (AABB)
const physicsRunning = ref(false)
const gravity = 0.6 // Gravedad más fuerte
const bounce = 0.6 // Rebote
const airFriction = 0.995 // Fricción del aire MUY baja (se mueven más)
const groundFriction = 0.92 // Fricción del suelo

// Dimensiones del paso (rectángulo)
const stepWidth = 300
const stepHeight = 70

const startPhysics = () => {
  if (physicsRunning.value) return
  physicsRunning.value = true
  
  const updatePhysics = () => {
    if (!loading.value) {
      physicsRunning.value = false
      return
    }
    
    let anyMoving = false
    const steps = loadingStepsData.value
    
    // Calcular posición del footer (límite inferior)
    const footer = document.querySelector('footer')
    let footerY = window.innerHeight - 100 // Por defecto
    
    if (footer) {
      const footerRect = footer.getBoundingClientRect()
      footerY = footerRect.top // Posición del footer en viewport
    }
    
    steps.forEach((step, index) => {
      if (!step.isLaunched) return
      
      // Aplicar gravedad (constante hacia abajo)
      step.vy += gravity
      
      // Actualizar posición
      step.x = (step.x || 0) + step.vx
      step.y = (step.y || 0) + step.vy
      
      // Aplicar fricción del aire (MUY baja para que no se detengan en el aire)
      step.vx *= airFriction
      step.vy *= airFriction
      
      // Rotación natural (física de rectángulo cayendo)
      step.rotation = (step.rotation || 0) + (step.angularVelocity || 0)
      step.angularVelocity *= 0.99 // Fricción angular baja
      
      // Obtener dimensiones de la pantalla
      const screenWidth = window.innerWidth
      const screenHeight = window.innerHeight
      
      // Límites laterales (izquierda y derecha)
      if (step.x < -screenWidth / 2 + stepWidth / 2) {
        step.x = -screenWidth / 2 + stepWidth / 2
        step.vx *= -bounce
        step.angularVelocity += step.vy * 0.08
      } else if (step.x > screenWidth / 2 - stepWidth / 2) {
        step.x = screenWidth / 2 - stepWidth / 2
        step.vx *= -bounce
        step.angularVelocity -= step.vy * 0.08
      }
      
      // Límite superior (techo)
      if (step.y < -screenHeight / 2 + stepHeight / 2) {
        step.y = -screenHeight / 2 + stepHeight / 2
        step.vy *= -bounce
        step.angularVelocity += step.vx * 0.08
      }
      
      // Límite inferior (FOOTER como suelo)
      const stepScreenY = screenHeight / 2 + step.y
      
      if (stepScreenY + stepHeight / 2 > footerY) {
        // Tocar el suelo (footer)
        step.y = footerY - stepHeight / 2 - screenHeight / 2
        step.vy *= -bounce
        
        // Fricción del suelo (solo cuando toca)
        if (Math.abs(step.vy) < 2) {
          step.vx *= groundFriction
          step.angularVelocity *= 0.95
        }
        
        // Transferir velocidad horizontal a rotación
        step.angularVelocity += step.vx * 0.03
      }
      
      // COLISIONES RECTANGULARES (AABB) entre pasos
      steps.forEach((otherStep, otherIndex) => {
        if (index >= otherIndex || !otherStep.isLaunched) return // Evitar duplicados
        
        // Calcular bordes de los rectángulos (AABB)
        const rect1 = {
          left: step.x - stepWidth / 2,
          right: step.x + stepWidth / 2,
          top: step.y - stepHeight / 2,
          bottom: step.y + stepHeight / 2
        }
        
        const rect2 = {
          left: otherStep.x - stepWidth / 2,
          right: otherStep.x + stepWidth / 2,
          top: otherStep.y - stepHeight / 2,
          bottom: otherStep.y + stepHeight / 2
        }
        
        // Detectar colisión rectangular (AABB)
        const colliding = rect1.left < rect2.right &&
                         rect1.right > rect2.left &&
                         rect1.top < rect2.bottom &&
                         rect1.bottom > rect2.top
        
        if (colliding) {
          // Calcular overlap en cada eje
          const overlapX = Math.min(rect1.right - rect2.left, rect2.right - rect1.left)
          const overlapY = Math.min(rect1.bottom - rect2.top, rect2.bottom - rect1.top)
          
          // Separar en el eje con menor overlap (más realista)
          if (overlapX < overlapY) {
            // Separar horizontalmente
            const direction = step.x < otherStep.x ? -1 : 1
            step.x += direction * overlapX / 2
            otherStep.x -= direction * overlapX / 2
            
            // Intercambiar velocidades horizontales con pérdida
            const tempVx = step.vx
            step.vx = otherStep.vx * 0.85
            otherStep.vx = tempVx * 0.85
            
            // Agregar spin por colisión
            step.angularVelocity += (otherStep.vy - step.vy) * 0.05
            otherStep.angularVelocity += (step.vy - otherStep.vy) * 0.05
          } else {
            // Separar verticalmente
            const direction = step.y < otherStep.y ? -1 : 1
            step.y += direction * overlapY / 2
            otherStep.y -= direction * overlapY / 2
            
            // Intercambiar velocidades verticales con pérdida
            const tempVy = step.vy
            step.vy = otherStep.vy * 0.85
            otherStep.vy = tempVy * 0.85
            
            // Agregar spin por colisión
            step.angularVelocity += (otherStep.vx - step.vx) * 0.05
            otherStep.angularVelocity += (step.vx - otherStep.vx) * 0.05
          }
        }
      })
      
      // Verificar si sigue en movimiento (umbral más bajo)
      if (Math.abs(step.vx) > 0.05 || Math.abs(step.vy) > 0.05 || Math.abs(step.angularVelocity) > 0.1) {
        anyMoving = true
      }
    })
    
    if (anyMoving) {
      requestAnimationFrame(updatePhysics)
    } else {
      physicsRunning.value = false
    }
  }
  
  updatePhysics()
}

const getScoreClass = (score: number) => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

const getReputationScoreClass = (score: number) => {
  if (score >= 80) return 'reputation-high'
  if (score >= 60) return 'reputation-medium'
  if (score >= 40) return 'reputation-low'
  return 'reputation-critical'
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

  const normalizedQuery = searchQuery.trim()
  
  // Verificar si se solicita explícitamente recargar (ignorar caché)
  const forceReload = route.query.reload === 'true'

  try {
    // VERIFICAR CACHÉ PRIMERO, ANTES DE CAMBIAR CUALQUIER ESTADO
    if (!forceReload) {
      const cachedResult = await cacheService.get(normalizedQuery)
      
      if (cachedResult) {
        // Si hay caché y es la misma query, no hacer nada
        if (query.value === normalizedQuery && results.value !== null) {
          console.log('[CACHE] Ya estamos mostrando estos resultados')
          return
        }
        
        // Actualizar con resultados del caché inmediatamente
        query.value = normalizedQuery
        results.value = cachedResult
        fromCache.value = true
        mapLoading.value = false
        mapError.value = ''
        loading.value = false
        searchError.value = ''
        
        // Registrar uso de caché en estadísticas
        await statisticsService.recordAnalysis(normalizedQuery, cachedResult, true)
        
        console.log('[CACHE] ✅ Cargado desde caché:', normalizedQuery)
        
        // Mostrar notificación de caché (opcional)
        toast.success(
          'Cargado desde Caché',
          'Mostrando resultados guardados previamente',
          'Los datos tienen menos de 30 minutos',
          3000
        )
        
        return
      }
    }

    // Si no hay caché o se fuerza recarga, ENTONCES preparar para análisis nuevo
    console.log('[BACKEND] No hay caché disponible, iniciando análisis...')
    
    query.value = normalizedQuery
    loading.value = true
    searchError.value = ''
    results.value = null  // Solo resetear si vamos a hacer análisis nuevo
    currentStep.value = 0
    realProgress.value = 0
    fromCache.value = false

    // Progreso real por pasos
    const progressSteps = [
      { step: 1, progress: 5, delay: 500 },     // Resolución DNS: 5%
      { step: 2, progress: 15, delay: 2000 },   // Geolocalización: 15%
      { step: 3, progress: 75, delay: 60000 },  // Nmap (el más largo): 75%
      { step: 4, progress: 90, delay: 10000 },  // NVD vulnerabilidades: 90%
      { step: 5, progress: 100, delay: 3000 }   // Generando reporte: 100%
    ]
    
    // Intervalos de progreso suave
    const progressIntervals: number[] = []
    
    progressSteps.forEach((stepData, index) => {
      setTimeout(() => {
        if (!loading.value) return
        
        currentStep.value = stepData.step
        
        // Progreso suave hasta el siguiente paso
        const currentProgress = index > 0 ? progressSteps[index - 1].progress : 0
        const targetProgress = stepData.progress
        const duration = stepData.delay
        const steps = 20 // 20 actualizaciones por paso
        const increment = (targetProgress - currentProgress) / steps
        const intervalTime = duration / steps
        
        let tempProgress = currentProgress
        const intervalId = setInterval(() => {
          if (!loading.value || tempProgress >= targetProgress) {
            clearInterval(intervalId)
            return
          }
          tempProgress += increment
          realProgress.value = Math.min(Math.round(tempProgress), targetProgress)
        }, intervalTime)
        
        progressIntervals.push(intervalId)
      }, progressSteps.slice(0, index).reduce((sum, s) => sum + s.delay, 0))
    })

    // Intentar análisis con el backend (puede fallar si está offline)
    console.log('[BACKEND] No hay caché, consultando backend...')
    const response = await analysisService.analyzeIP(query.value)
    
    // Limpiar intervalos
    progressIntervals.forEach(id => clearInterval(id))
    
    // Asegurar que llegamos al 100%
    currentStep.value = 5
    realProgress.value = 100
    
    if (response.success && response.data) {
      results.value = response.data
      mapLoading.value = false
      mapError.value = ''
      
      // Mostrar warnings si existen
      if (response.data.metadata?.warnings && response.data.metadata.warnings.length > 0) {
        response.data.metadata.warnings.forEach((warning: string) => {
          toast.warning(
            'Advertencia del Sistema',
            warning,
            'El análisis continuó pero algunos datos pueden estar incompletos',
            6000
          )
        })
      }
      
      // Guardar en caché para futuros usos
      await cacheService.set(query.value, response.data)
      
      // Siempre registrar en estadísticas (solo si es análisis nuevo, no caché)
      await statisticsService.recordAnalysis(query.value, response.data, false)
      
      console.log('[SUCCESS] Análisis completado y guardado en caché')
    } else {
      searchError.value = response.error || 'Error en el análisis'
      mapError.value = response.error || 'Error cargando mapa'
      
      // Mostrar toast de error con información detallada
      toast.error(
        'Error en el Análisis',
        response.error || 'No se pudo completar el análisis',
        'Verifica la dirección IP o dominio e intenta nuevamente',
        8000
      )
    }
  } catch (error: any) {
    console.error('Analysis error:', error)
    
    // Si hay error de conexión, intentar cargar del caché como fallback
    if (error.message && error.message.includes('fetch')) {
      console.log('[OFFLINE] Backend no disponible, verificando caché como fallback...')
      
      const cachedResult = await cacheService.get(query.value)
      if (cachedResult) {
        results.value = cachedResult
        fromCache.value = true
        mapLoading.value = false
        mapError.value = ''
        loading.value = false
        
        toast.warning(
          'Modo Offline',
          'Se cargó el último análisis guardado en caché',
          'El backend no está disponible en este momento',
          6000
        )
        
        console.log('[FALLBACK] Cargado desde caché')
        return
      }
    }
    
    // Intentar extraer información del error
    let errorMessage = 'Error de conexión con el servidor'
    let errorAction = 'Verifica tu conexión a internet e intenta nuevamente'
    
    if (error.response) {
      // Error del backend con respuesta
      try {
        const errorData = await error.response.json()
        toast.backendError(errorData)
      } catch {
        toast.error(
          'Error del Servidor',
          `El servidor respondió con código ${error.response.status}`,
          'Intenta nuevamente o contacta al administrador',
          8000
        )
      }
    } else if (error.message) {
      // Error de red u otro
      if (error.message.includes('fetch')) {
        errorMessage = 'No se pudo conectar con el servidor'
        errorAction = 'Verifica que el backend esté ejecutándose en localhost:8080'
      } else {
        errorMessage = error.message
      }
      toast.error('Error de Conexión', errorMessage, errorAction, 8000)
    } else {
      // Error genérico
      toast.error('Error Inesperado', errorMessage, errorAction, 8000)
    }
    
    searchError.value = errorMessage
    mapError.value = errorMessage
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

const forceReloadAnalysis = async () => {
  if (!query.value) return
  
  // Limpiar caché para esta query específica
  await cacheService.remove(query.value)
  
  // Ejecutar nuevo análisis inmediatamente
  await handleSearch(query.value)
}

const handleFaviconError = (event: Event) => {
  const img = event.target as HTMLImageElement
  // Fallback a un ícono genérico de dominio
  img.src = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI2NCIgaGVpZ2h0PSI2NCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiM5M0M1RkQiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48Y2lyY2xlIGN4PSIxMiIgY3k9IjEyIiByPSIxMCIvPjxsaW5lIHgxPSIyIiB5MT0iMTIiIHgyPSIyMiIgeTI9IjEyIi8+PHBhdGggZD0iTTEyIDJhMTUuMyAxNS4zIDAgMCAxIDQgMTAgMTUuMyAxNS4zIDAgMCAxLTQgMTAgMTUuMyAxNS4zIDAgMCAxLTQtMTAgMTUuMyAxNS4zIDAgMCAxIDQtMTB6Ii8+PC9zdmc+'
}

onMounted(async () => {
  // Si hay query en la URL, verificar caché primero
  if (route.query.q) {
    const queryParam = route.query.q as string
    
    // Verificar caché inmediatamente al montar
    const cachedResult = await cacheService.get(queryParam)
    
    if (cachedResult && route.query.reload !== 'true') {
      // Cargar desde caché sin mostrar loading
      query.value = queryParam
      results.value = cachedResult
      fromCache.value = true
      mapLoading.value = false
      mapError.value = ''
      loading.value = false
      
      await statisticsService.recordAnalysis(queryParam, cachedResult, true)
      
      console.log('[MOUNT] ✅ Cargado desde caché al montar:', queryParam)
      
      toast.success(
        'Resultados Guardados',
        'Se cargaron los resultados previos',
        'Usa el botón "Actualizar Análisis" para obtener datos nuevos',
        4000
      )
    } else {
      // No hay caché, ejecutar análisis normal
      console.log('[MOUNT] No hay caché, ejecutando análisis...')
      handleSearch(queryParam)
    }
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
  grid-template-columns: repeat(2, 1fr);
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

/* Hacer que vulnerabilidades ocupe toda la fila */
.vulnerabilities-card {
  grid-column: 1 / -1; /* Ocupa todas las columnas */
}

.result-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

/* Análisis de Seguridad */
.security-analysis-card {
  grid-column: 1 / -1;
}

.services-row {
  margin-bottom: 2rem;
}

.security-visual-container {
  display: flex;
  align-items: flex-start;
  gap: 3rem;
}

.security-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* Card Headers */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-secondary);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.title-icon {
  width: 28px;
  height: 28px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.card-content {
  color: var(--text-secondary);
}

/* Overview Card */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.25rem;
  background: var(--bg-secondary);
  border-radius: 10px;
  border: 1px solid var(--border-secondary);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.info-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, var(--color-primary), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.info-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  border-color: var(--color-primary);
}

.info-item:hover::before {
  opacity: 1;
}

/* Domain Preview Section */
.domain-preview {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(139, 92, 246, 0.08) 100%);
  border-radius: 12px;
  margin-bottom: 2rem;
  border: 1px solid rgba(59, 130, 246, 0.2);
  transition: all 0.3s ease;
}

[data-theme="dark"] .domain-preview {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12) 0%, rgba(139, 92, 246, 0.12) 100%);
  border-color: rgba(59, 130, 246, 0.3);
}

.domain-preview:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.4);
}

.domain-favicon-container {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px;
}

[data-theme="dark"] .domain-favicon-container {
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.domain-favicon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 4px;
}

.domain-info-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.domain-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e3a8a;
  margin: 0;
  letter-spacing: -0.02em;
}

[data-theme="dark"] .domain-name {
  color: #93c5fd;
}

.domain-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #3b82f6;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.2s ease;
  width: fit-content;
}

.domain-link:hover {
  color: #2563eb;
  gap: 0.75rem;
}

[data-theme="dark"] .domain-link {
  color: #60a5fa;
}

[data-theme="dark"] .domain-link:hover {
  color: #93c5fd;
}

.domain-link svg {
  width: 16px;
  height: 16px;
  transition: transform 0.2s ease;
}

.domain-link:hover svg {
  transform: translate(2px, -2px);
}

.domain-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(16, 185, 129, 0.15);
  color: #065f46;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 700;
  border: 1px solid rgba(16, 185, 129, 0.3);
  flex-shrink: 1;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

[data-theme="dark"] .domain-badge {
  background: rgba(16, 185, 129, 0.2);
  color: #6ee7b7;
  border-color: rgba(16, 185, 129, 0.4);
}

.domain-badge svg {
  width: 18px;
  height: 18px;
}

.info-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.info-icon {
  width: 18px;
  height: 18px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.info-value {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-primary);
  word-break: break-word;
  line-height: 1.3;
}

.info-type {
  display: inline-flex;
  padding: 0.375rem 0.875rem;
  border-radius: 6px;
  font-size: 0.95rem;
  width: fit-content;
  font-weight: 700;
}

.info-type.domain {
  background: rgba(59, 130, 246, 0.15);
  color: #1e3a8a;
  border: 1px solid rgba(59, 130, 246, 0.4);
}

[data-theme="dark"] .info-type.domain {
  background: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
  border-color: rgba(59, 130, 246, 0.5);
}

.info-type.ipv4,
.info-type.ipv6 {
  background: rgba(139, 92, 246, 0.15);
  color: #581c87;
  border: 1px solid rgba(139, 92, 246, 0.4);
}

[data-theme="dark"] .info-type.ipv4,
[data-theme="dark"] .info-type.ipv6 {
  background: rgba(139, 92, 246, 0.2);
  color: #c4b5fd;
  border-color: rgba(139, 92, 246, 0.5);
}

/* Vulnerabilities Card */
.vuln-count {
  padding: 0.5rem 1rem;
  background: var(--color-danger);
  color: white;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 700;
}

/* Recommendations Section */
.recommendations-section {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.recommendations-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.recommendation-item {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 20px 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid var(--border-primary);
  position: relative;
  overflow: hidden;
}

.recommendation-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  transition: width 0.3s ease;
}

.recommendation-item.high {
  border-color: rgba(239, 68, 68, 0.3);
}

.recommendation-item.high::before {
  background: linear-gradient(180deg, #ef4444 0%, #dc2626 100%);
}

.recommendation-item.medium {
  border-color: rgba(245, 158, 11, 0.3);
}

.recommendation-item.medium::before {
  background: linear-gradient(180deg, #f59e0b 0%, #d97706 100%);
}

.recommendation-item.low {
  border-color: rgba(59, 130, 246, 0.3);
}

.recommendation-item.low::before {
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
}

.recommendation-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  border-color: currentColor;
}

.recommendation-item:hover::before {
  width: 8px;
}

.rec-priority {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  border: 1px solid transparent;
}

.recommendation-item.high .rec-priority {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #991b1b;
  border-color: #fca5a5;
}

.recommendation-item.medium .rec-priority {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  border-color: #fcd34d;
}

.recommendation-item.low .rec-priority {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
  border-color: #93c5fd;
}

.rec-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  line-height: 1.4;
}

.rec-description {
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 10px;
  font-size: 14px;
}

.rec-action {
  color: var(--color-primary);
  font-weight: 600;
  font-size: 0.9rem;
  padding: 8px 12px;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 6px;
  display: inline-block;
  margin-top: 8px;
}

/* Estilos para modo oscuro */
[data-theme="dark"] .recommendation-item {
  background: rgba(30, 41, 59, 0.5);
  border-color: rgba(71, 85, 105, 0.3);
}

[data-theme="dark"] .recommendation-item:hover {
  background: rgba(30, 41, 59, 0.7);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
}

[data-theme="dark"] .recommendation-item.high {
  border-color: rgba(239, 68, 68, 0.4);
}

[data-theme="dark"] .recommendation-item.medium {
  border-color: rgba(245, 158, 11, 0.4);
}

[data-theme="dark"] .recommendation-item.low {
  border-color: rgba(59, 130, 246, 0.4);
}

[data-theme="dark"] .recommendation-item.high .rec-priority {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border-color: rgba(239, 68, 68, 0.4);
}

[data-theme="dark"] .recommendation-item.medium .rec-priority {
  background: rgba(245, 158, 11, 0.2);
  color: #fcd34d;
  border-color: rgba(245, 158, 11, 0.4);
}

[data-theme="dark"] .recommendation-item.low .rec-priority {
  background: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
  border-color: rgba(59, 130, 246, 0.4);
}

[data-theme="dark"] .rec-title {
  color: #f1f5f9;
}

[data-theme="dark"] .rec-description {
  color: #cbd5e1;
}

[data-theme="dark"] .rec-action {
  background: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
}

/* Export Section */
.export-section {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.export-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.export-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.cache-info-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.cache-badge {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 1.25rem;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 8px;
  font-size: 0.95rem;
  color: var(--text-primary);
  font-weight: 500;
}

.cache-icon {
  width: 20px;
  height: 20px;
  color: #3B82F6;
}

.reload-btn {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.75rem 1.5rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reload-btn:hover {
  background: var(--color-primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.reload-btn svg {
  width: 20px;
  height: 20px;
}

.export-buttons {
  display: flex;
  gap: 1rem;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.export-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 200px;
  justify-content: center;
}

.export-btn svg {
  width: 22px;
  height: 22px;
}

.pdf-btn {
  background: #EF4444;
  color: white;
}

.pdf-btn:hover:not(:disabled) {
  background: #DC2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.pdf-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.json-btn {
  background: #3B82F6;
  color: white;
}

.json-btn:hover {
  background: #2563EB;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* Loading Section */
.loading-section {
  padding: 5rem 0;
  background: var(--bg-secondary);
  min-height: 600px;
  display: flex;
  align-items: center;
  overflow: visible; /* Permite que los pasos salgan del contenedor */
}

.loading-content {
  text-align: center;
  max-width: 700px;
  margin: 0 auto;
  overflow: visible; /* Permite que los pasos salgan del contenedor */
}

/* Radar Animation */
.radar-container {
  width: 200px;
  height: 200px;
  margin: 0 auto 2rem;
  cursor: pointer;
  position: relative;
}

.radar-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 12px rgba(59, 130, 246, 0.3));
}

.radar-ring {
  fill: none;
  stroke: rgba(59, 130, 246, 0.2);
  stroke-width: 1;
}

.radar-line {
  stroke: rgba(59, 130, 246, 0.1);
  stroke-width: 1;
}

.radar-beam {
  transform-origin: 100px 100px;
  animation: radar-rotate 4s linear infinite;
}

.radar-beam.speed-fast {
  animation-duration: 2s;
}

@keyframes radar-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.beam-fill {
  fill: rgba(59, 130, 246, 0.15);
}

.beam-line {
  stroke: #3B82F6;
  stroke-width: 2;
}

.radar-center {
  fill: #3B82F6;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.activity-dot {
  fill: #10B981;
  animation: dot-appear 0.5s ease-out;
}

@keyframes dot-appear {
  from {
    opacity: 0;
    transform: scale(0);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.loading-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.loading-subtitle {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1rem;
}

/* Progress Bar */
.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 2rem auto;
  max-width: 500px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: var(--bg-primary);
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #10B981);
  border-radius: 10px;
  transition: width 0.3s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  animation: progress-shimmer 2s infinite;
}

@keyframes progress-shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.progress-text {
  font-weight: 700;
  color: var(--text-primary);
  min-width: 45px;
  text-align: right;
}

/* Loading Steps */
.loading-steps {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: stretch;
  max-width: 500px;
  margin: 2rem auto 0;
  position: relative;
  min-height: 400px;
  overflow: visible; /* Permite que los pasos se muevan por toda la pantalla */
}

.loading-step {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background: var(--bg-primary);
  border-radius: 10px;
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
  cursor: pointer;
  user-select: none;
  position: relative;
  will-change: transform;
  z-index: 1000; /* Asegura que los pasos lanzados estén por encima de todo */
}

.loading-step:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.loading-step.pending {
  opacity: 0.5;
  border-left-color: var(--border-secondary);
}

.loading-step.current {
  opacity: 1;
  border-left-color: #3B82F6;
  background: rgba(59, 130, 246, 0.05);
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.1);
}

.loading-step.active {
  opacity: 1;
  border-left-color: #10B981;
  background: rgba(16, 185, 129, 0.05);
}

.loading-step.floating {
  position: absolute;
  z-index: 100;
  cursor: move;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.25);
  animation: float-wobble 3s ease-in-out infinite;
}

@keyframes float-wobble {
  0%, 100% {
    filter: drop-shadow(0 10px 20px rgba(59, 130, 246, 0.3));
  }
  50% {
    filter: drop-shadow(0 20px 40px rgba(139, 92, 246, 0.4));
  }
}

/* Estilos para pasos lanzados con física */
.loading-step.launched {
  z-index: 100;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border-color: rgba(59, 130, 246, 0.5);
}

[data-theme="dark"] .loading-step.launched {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.loading-step {
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.loading-step:active {
  cursor: grabbing;
}

.step-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--bg-secondary);
}

.loading-step.current .step-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.loading-step.active .step-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.loading-step.pending .step-icon {
  opacity: 0.5;
}

.step-icon svg {
  width: 20px;
  height: 20px;
}

.spinner-small {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(59, 130, 246, 0.2);
  border-top-color: #3B82F6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.step-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-text {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 0.95rem;
}

.step-time {
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-weight: 400;
}

/* Empty State */
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
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  color: var(--text-secondary);
  opacity: 0.5;
}

.empty-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.75rem;
}

.empty-subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
  margin-bottom: 2rem;
}

.empty-examples {
  margin-top: 2rem;
}

.empty-examples p {
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.example-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.example-btn {
  padding: 0.75rem 1.5rem;
  background: var(--bg-primary);
  border: 2px solid var(--border-primary);
  border-radius: 8px;
  color: var(--text-primary);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.example-btn:hover {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 968px) {
  .security-visual-container {
    flex-direction: column;
    gap: 2rem;
    align-items: center;
  }
  
  .security-details {
    width: 100%;
  }
  
  .results-grid {
    grid-template-columns: 1fr;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .recommendations-grid {
    grid-template-columns: 1fr;
  }
  
  .cache-info-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .export-buttons {
    flex-direction: column;
  }
  
  .export-btn {
    width: 100%;
    min-width: auto;
  }
}

@media (max-width: 640px) {
  .container {
    padding: 0 1rem;
  }
  
  .page-title {
    font-size: 1.75rem;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .cache-badge {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .export-btn {
    padding: 0.875rem 1.5rem;
    font-size: 0.95rem;
  }
}
</style>
