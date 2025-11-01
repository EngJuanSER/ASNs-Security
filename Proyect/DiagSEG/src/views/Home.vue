<template>
  <div class="home">
    <Navbar />
    
    <main>
      <!-- Hero Section -->
      <section class="hero">
        <div class="hero-background">
          <div class="floating-elements">
            <div class="floating-element" v-for="i in 6" :key="i"></div>
          </div>
        </div>
        
        <div class="hero-content">
          <div class="hero-badge">
            <span class="badge-text">Herramienta de Código Abierto</span>
          </div>
          
          <h1 class="hero-title">
            <span class="title-main">DiagSEG</span>
            <span class="title-gradient">Security</span>
          </h1>
          
          <p class="hero-subtitle">
            Diagnóstico Inteligente de Seguridad IP
          </p>
          
          <p class="hero-description">
            Análisis exhaustivo de seguridad para direcciones IP utilizando múltiples 
            fuentes de datos integradas. Obtén información detallada sobre geolocalización, 
            reputación, puertos abiertos y más en segundos.
          </p>
          
          <div class="search-container">
            <SearchBar 
              ref="searchBarRef"
              @search="handleSearch" 
              :loading="loading"
              :error="searchError"
              placeholder="Ingresa una IP o dominio para analizar..."
            />
            <div class="search-examples">
              <span class="examples-label">Ejemplos:</span>
              <button 
                v-for="example in searchExamples" 
                :key="example"
                @click="handleExampleClick(example)"
                class="example-btn"
              >
                {{ example }}
              </button>
            </div>
          </div>

          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">1M+</div>
              <div class="stat-label">IPs Analizadas</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number">98.7%</div>
              <div class="stat-label">Precisión</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number">&lt;5s</div>
              <div class="stat-label">Tiempo de Respuesta</div>
            </div>
          </div>

          <div class="trust-indicators">
            <div class="trust-item">
              <span class="trust-text">Fuentes Verificadas</span>
            </div>
            <div class="trust-item">
              <span class="trust-text">Datos Seguros</span>
            </div>
            <div class="trust-item">
              <span class="trust-text">Tiempo Real</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Features Section -->
      <FeaturesSection />

      <!-- Statistics Section -->
      <StatisticsSection />

      <!-- How It Works Section -->
      <section class="how-it-works">
        <div class="container">
          <div class="section-header">
            <h2 class="section-title">¿Cómo Funciona?</h2>
            <p class="section-subtitle">
              Proceso simple y automatizado para análisis completo de seguridad
            </p>
          </div>

          <div class="steps-container">
            <div class="step" v-for="(step, index) in workflowSteps" :key="step.id">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-content">
                <div class="step-icon">{{ step.icon }}</div>
                <h3 class="step-title">{{ step.title }}</h3>
                <p class="step-description">{{ step.description }}</p>
              </div>
              <div v-if="index < workflowSteps.length - 1" class="step-connector"></div>
            </div>
          </div>

          <div class="cta-section">
            <h3 class="cta-title">¿Listo para comenzar?</h3>
            <p class="cta-description">
              Comienza tu análisis de seguridad IP ahora mismo
            </p>
            <button @click="scrollToSearch" class="cta-button">
              Analizar Ahora
            </button>
          </div>
        </div>
      </section>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import SearchBar from '@/components/SearchBar.vue'
import FeaturesSection from '@/components/FeaturesSection.vue'
import StatisticsSection from '@/components/StatisticsSection.vue'

interface WorkflowStep {
  id: string
  icon: string
  title: string
  description: string
}

const loading = ref(false)
const searchError = ref('')
const searchExamples = ['8.8.8.8', 'google.com', '1.1.1.1']
const searchBarRef = ref()

const workflowSteps: WorkflowStep[] = [
  {
    id: 'input',
    icon: '',
    title: 'Ingresa la IP o Dominio',
    description: 'Introduce la dirección IP o nombre de dominio que deseas analizar en nuestra herramienta de búsqueda.'
  },
  {
    id: 'analysis',
    icon: '',
    title: 'Análisis Automático',
    description: 'Nuestro sistema consulta múltiples fuentes de datos para recopilar información completa sobre la dirección.'
  },
  {
    id: 'processing',
    icon: '',
    title: 'Procesamiento Inteligente',
    description: 'Los datos se procesan usando algoritmos avanzados para calcular el score de riesgo y generar recomendaciones.'
  },
  {
    id: 'results',
    icon: '',
    title: 'Resultados Detallados',
    description: 'Recibe un informe completo con geolocalización, reputación, puertos abiertos y recomendaciones de seguridad.'
  }
]

const handleSearch = async (query: string) => {
  loading.value = true
  searchError.value = ''
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    console.log('Searching for:', query)
    // Here you would make the actual API call
  } catch (error) {
    searchError.value = 'Error al realizar la búsqueda'
  } finally {
    loading.value = false
  }
}

const handleExampleClick = (example: string) => {
  if (searchBarRef.value && searchBarRef.value.setQuery) {
    searchBarRef.value.setQuery(example)
  }
}

const scrollToSearch = () => {
  const searchElement = document.querySelector('.search-container')
  if (searchElement) {
    searchElement.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

main {
  flex: 1;
}

/* Hero Section */
.hero {
  background: var(--hero-gradient, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  color: var(--hero-text, white);
  padding: 2rem 0 4rem;
  text-align: center;
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  display: flex;
  align-items: center;
}

[data-theme="dark"] .hero {
  --hero-gradient: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  --hero-text: var(--color-gray-100);
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
}

.floating-element {
  position: absolute;
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float 3s ease-in-out infinite;
}

.floating-element:nth-child(1) { top: 10%; left: 10%; animation-delay: 0s; }
.floating-element:nth-child(2) { top: 20%; right: 10%; animation-delay: 1s; }
.floating-element:nth-child(3) { top: 60%; left: 20%; animation-delay: 2s; }
.floating-element:nth-child(4) { bottom: 30%; right: 20%; animation-delay: 3s; }
.floating-element:nth-child(5) { bottom: 10%; left: 30%; animation-delay: 4s; }
.floating-element:nth-child(6) { top: 50%; right: 30%; animation-delay: 5s; }

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-20px) rotate(120deg); }
  66% { transform: translateY(10px) rotate(240deg); }
}

.hero-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 2rem;
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 0.5rem 1rem;
  border-radius: 50px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 2rem;
  animation: slideInDown 0.8s ease;
}

.badge-text {
  font-size: 0.9rem;
  font-weight: 500;
}

.hero-title {
  font-size: 4.5rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  line-height: 1.1;
  animation: slideInUp 0.8s ease 0.2s both;
}

.title-main {
  display: block;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.title-gradient {
  background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: block;
  font-size: 0.7em;
  margin-top: -0.2rem;
}

.hero-subtitle {
  font-size: 1.6rem;
  margin-bottom: 1.5rem;
  opacity: 0.95;
  font-weight: 300;
  animation: slideInUp 0.8s ease 0.4s both;
}

.hero-description {
  font-size: 1.2rem;
  margin-bottom: 3rem;
  opacity: 0.9;
  line-height: 1.7;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
  animation: slideInUp 0.8s ease 0.6s both;
}

.search-container {
  margin-bottom: 3rem;
  animation: slideInUp 0.8s ease 0.8s both;
}

.search-examples {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.examples-label {
  font-size: 0.9rem;
  opacity: 0.8;
  margin-right: 0.5rem;
}

.example-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.example-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.hero-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2rem;
  margin-bottom: 3rem;
  animation: slideInUp 0.8s ease 1s both;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #ffd89b;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.8;
  margin-top: 0.25rem;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

.trust-indicators {
  display: flex;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
  animation: slideInUp 0.8s ease 1.2s both;
}

.trust-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: transform 0.3s ease;
}

.trust-item:hover {
  transform: translateY(-2px);
}

.trust-text {
  font-size: 0.9rem;
  font-weight: 500;
}

/* How It Works Section */
.how-it-works {
  padding: 5rem 0;
  background: var(--section-bg, #f8fafc);
}

[data-theme="dark"] .how-it-works {
  --section-bg: var(--color-dark-surface);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.section-header {
  text-align: center;
  margin-bottom: 4rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--section-title, #2d3748);
  margin-bottom: 1rem;
}

.section-subtitle {
  font-size: 1.2rem;
  color: var(--section-subtitle, #4a5568);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

[data-theme="dark"] .section-title {
  --section-title: var(--color-gray-100);
}

[data-theme="dark"] .section-subtitle {
  --section-subtitle: var(--color-gray-300);
}

.steps-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 4rem;
  position: relative;
}

.step {
  position: relative;
  text-align: center;
  padding: 2rem;
}

.step-number {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.2rem;
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.step-content {
  margin-top: 2rem;
}

.step-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.step-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.step-description {
  color: var(--text-secondary);
  line-height: 1.6;
  font-size: 0.95rem;
}

.step-connector {
  position: absolute;
  top: 20px;
  left: calc(100% - 1rem);
  width: 2rem;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1;
}

.cta-section {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--bg-primary);
  border-radius: 20px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-primary);
}

.cta-title {
  font-size: 2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.cta-description {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.cta-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 2rem;
  border: none;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6);
}

/* Animations */
@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Mobile styles */
@media (max-width: 768px) {
  .hero {
    padding: 4rem 0 3rem;
    min-height: auto;
  }

  .hero-content {
    padding: 0 1rem;
  }

  .hero-title {
    font-size: 3rem;
  }

  .hero-subtitle {
    font-size: 1.3rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .hero-stats {
    flex-direction: column;
    gap: 1rem;
  }

  .stat-divider {
    display: none;
  }

  .trust-indicators {
    gap: 1rem;
  }

  .trust-item {
    padding: 0.5rem 1rem;
    font-size: 0.85rem;
  }

  .search-examples {
    flex-direction: column;
    align-items: center;
  }

  .how-it-works {
    padding: 3rem 0;
  }

  .container {
    padding: 0 1rem;
  }

  .section-title {
    font-size: 2rem;
  }

  .steps-container {
    grid-template-columns: 1fr;
  }

  .step-connector {
    display: none;
  }

  .cta-section {
    padding: 2rem 1rem;
  }

  .cta-title {
    font-size: 1.5rem;
  }
}
</style>
