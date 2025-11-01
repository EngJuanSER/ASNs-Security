<template>
  <section class="statistics-section">
    <div class="container">
      <div class="stats-header">
        <h2 class="section-title">Impacto y Alcance</h2>
        <p class="section-subtitle">
          Datos en tiempo real sobre el análisis de seguridad IP
        </p>
      </div>

      <div class="stats-grid">
        <div 
          v-for="(stat, index) in statistics" 
          :key="stat.id"
          class="stat-card"
          :style="{ animationDelay: `${index * 150}ms` }"
        >
          <div class="stat-icon">{{ stat.icon }}</div>
          <div class="stat-number" ref="statNumbers">
            <span class="stat-value">{{ formatNumber(stat.value) }}</span>
            <span class="stat-unit">{{ stat.unit }}</span>
          </div>
          <h3 class="stat-title">{{ stat.title }}</h3>
          <p class="stat-description">{{ stat.description }}</p>
          
          <div class="stat-progress">
            <div 
              class="stat-progress-bar" 
              :style="{ width: `${stat.progress}%` }"
            ></div>
          </div>
        </div>
      </div>

      <div class="data-sources">
        <h3 class="sources-title">Fuentes de Datos Integradas</h3>
        <div class="sources-grid">
          <div 
            v-for="source in dataSources" 
            :key="source.name"
            class="source-item"
          >
            <div class="source-logo">{{ source.icon }}</div>
            <div class="source-info">
              <h4 class="source-name">{{ source.name }}</h4>
              <p class="source-description">{{ source.description }}</p>
              <div class="source-metrics">
                <span class="source-metric">
                  <strong>{{ source.records }}</strong> registros
                </span>
                <span class="source-metric">
                  <strong>{{ source.coverage }}</strong> cobertura
                </span>
              </div>
            </div>
            <div class="source-status" :class="source.status">
              {{ source.status === 'active' ? 'Activo' : 'Inactivo' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Statistic {
  id: string
  icon: string
  value: number
  unit: string
  title: string
  description: string
  progress: number
}

interface DataSource {
  name: string
  icon: string
  description: string
  records: string
  coverage: string
  status: 'active' | 'inactive'
}

const statistics: Statistic[] = [
  {
    id: 'scanned-ips',
    icon: '',
    value: 1250000,
    unit: '+',
    title: 'IPs Analizadas',
    description: 'Direcciones IP escaneadas y analizadas en nuestra base de datos',
    progress: 85
  },
  {
    id: 'threat-detection',
    icon: '',
    value: 98.7,
    unit: '%',
    title: 'Precisión',
    description: 'Tasa de precisión en la detección de amenazas conocidas',
    progress: 99
  },
  {
    id: 'response-time',
    icon: '',
    value: 3.2,
    unit: 's',
    title: 'Tiempo de Respuesta',
    description: 'Tiempo promedio de análisis completo por consulta',
    progress: 78
  },
  {
    id: 'data-sources',
    icon: '',
    value: 2,
    unit: '',
    title: 'Fuentes Integradas',
    description: 'APIs y bases de datos conectadas para análisis completo',
    progress: 100
  }
]

const dataSources: DataSource[] = [
  {
    name: 'Censys (BigQuery)',
    icon: '',
    description: 'Datos de escaneo masivo de Internet',
    records: '4.2B',
    coverage: 'Global',
    status: 'active'
  },
  {
    name: 'GeoLite2',
    icon: '',
    description: 'Base de datos de geolocalización',
    records: '180M',
    coverage: '99.8%',
    status: 'active'
  }
]

const statNumbers = ref<HTMLElement[]>([])

const formatNumber = (value: number): string => {
  if (value >= 1000000) {
    return (value / 1000000).toFixed(1) + 'M'
  } else if (value >= 1000) {
    return (value / 1000).toFixed(0) + 'K'
  }
  return value.toString()
}

const animateNumber = (element: HTMLElement, finalValue: number, duration: number = 2000) => {
  const startValue = 0
  const startTime = performance.now()
  
  const updateNumber = (currentTime: number) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // Easing function
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    const currentValue = startValue + (finalValue - startValue) * easeOutQuart
    
    if (element.querySelector('.stat-value')) {
      const valueElement = element.querySelector('.stat-value') as HTMLElement
      if (finalValue >= 1000000) {
        valueElement.textContent = (currentValue / 1000000).toFixed(1) + 'M'
      } else if (finalValue >= 1000) {
        valueElement.textContent = (currentValue / 1000).toFixed(0) + 'K'
      } else {
        valueElement.textContent = currentValue.toFixed(1)
      }
    }
    
    if (progress < 1) {
      requestAnimationFrame(updateNumber)
    }
  }
  
  requestAnimationFrame(updateNumber)
}

onMounted(() => {
  // Animate numbers when component mounts
  setTimeout(() => {
    statNumbers.value.forEach((element, index) => {
      if (element) {
        animateNumber(element, statistics[index].value, 2000 + index * 200)
      }
    })
  }, 500)
})
</script>

<style scoped>
.statistics-section {
  padding: 5rem 0;
  background: var(--stats-bg, linear-gradient(135deg, #1a202c 0%, #2d3748 100%));
  color: var(--stats-text, white);
  position: relative;
  overflow: hidden;
}

[data-theme="light"] .statistics-section {
  --stats-bg: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --stats-text: white;
}

[data-theme="dark"] .statistics-section {
  --stats-bg: linear-gradient(135deg, #1a202c 0%, #2d3748 100%);
  --stats-text: white;
}

.statistics-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 80%, rgba(102, 126, 234, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(118, 75, 162, 0.1) 0%, transparent 50%);
  z-index: 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  position: relative;
  z-index: 1;
}

.stats-header {
  text-align: center;
  margin-bottom: 4rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  background: linear-gradient(135deg, var(--stats-title-start, #ffffff) 0%, var(--stats-title-end, #e2e8f0) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-subtitle {
  font-size: 1.2rem;
  color: var(--stats-subtitle, #cbd5e0);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

[data-theme="light"] .section-title {
  --stats-title-start: #ffffff;
  --stats-title-end: #f7fafc;
}

[data-theme="light"] .section-subtitle {
  --stats-subtitle: rgba(255, 255, 255, 0.9);
}

[data-theme="dark"] .section-title {
  --stats-title-start: #ffffff;
  --stats-title-end: #e2e8f0;
}

[data-theme="dark"] .section-subtitle {
  --stats-subtitle: #cbd5e0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 5rem;
}

.stat-card {
  background: var(--stat-card-bg, rgba(255, 255, 255, 0.05));
  backdrop-filter: blur(10px);
  border: 1px solid var(--stat-card-border, rgba(255, 255, 255, 0.1));
  border-radius: 20px;
  padding: 2rem;
  text-align: center;
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease forwards;
  opacity: 0;
  transform: translateY(30px);
  position: relative;
  overflow: hidden;
}

[data-theme="light"] .stat-card {
  --stat-card-bg: rgba(255, 255, 255, 0.2);
  --stat-card-border: rgba(255, 255, 255, 0.3);
}

[data-theme="dark"] .stat-card {
  --stat-card-bg: rgba(255, 255, 255, 0.05);
  --stat-card-border: rgba(255, 255, 255, 0.1);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
}

.stat-number {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 1rem;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 3rem;
  font-weight: 700;
  color: var(--stat-value-color, #667eea);
  text-shadow: var(--stat-value-shadow, 0 2px 4px rgba(0, 0, 0, 0.3));
}

.stat-unit {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--stat-unit-color, #a0aec0);
  margin-left: 0.25rem;
}

.stat-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--stat-title-color, #f7fafc);
  position: relative;
  z-index: 1;
}

.stat-description {
  color: var(--stat-desc-color, #cbd5e0);
  font-size: 0.9rem;
  line-height: 1.4;
  margin-bottom: 1.5rem;
  position: relative;
  z-index: 1;
}

[data-theme="light"] .stat-value {
  --stat-value-color: #ffffff;
  --stat-value-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

[data-theme="light"] .stat-unit {
  --stat-unit-color: rgba(255, 255, 255, 0.8);
}

[data-theme="light"] .stat-title {
  --stat-title-color: #ffffff;
}

[data-theme="light"] .stat-description {
  --stat-desc-color: rgba(255, 255, 255, 0.9);
}

[data-theme="dark"] .stat-value {
  --stat-value-color: #667eea;
  --stat-value-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .stat-unit {
  --stat-unit-color: #a0aec0;
}

[data-theme="dark"] .stat-title {
  --stat-title-color: #f7fafc;
}

[data-theme="dark"] .stat-description {
  --stat-desc-color: #cbd5e0;
}

.stat-progress {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.stat-progress-bar {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
  transition: width 1s ease;
  animation: progressAnimation 2s ease-in-out;
}

.data-sources {
  margin-top: 4rem;
}

.sources-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 2rem;
  color: var(--sources-title-color, #f7fafc);
}

[data-theme="light"] .sources-title {
  --sources-title-color: #ffffff;
}

[data-theme="dark"] .sources-title {
  --sources-title-color: #f7fafc;
}

.sources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.source-item {
  display: flex;
  align-items: center;
  background: var(--source-item-bg, rgba(255, 255, 255, 0.05));
  backdrop-filter: blur(10px);
  border: 1px solid var(--source-item-border, rgba(255, 255, 255, 0.1));
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.source-item:hover {
  background: var(--source-item-hover-bg, rgba(255, 255, 255, 0.1));
  transform: translateY(-2px);
}

[data-theme="light"] .source-item {
  --source-item-bg: rgba(255, 255, 255, 0.15);
  --source-item-border: rgba(255, 255, 255, 0.25);
}

[data-theme="light"] .source-item:hover {
  --source-item-hover-bg: rgba(255, 255, 255, 0.25);
}

[data-theme="dark"] .source-item {
  --source-item-bg: rgba(255, 255, 255, 0.05);
  --source-item-border: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .source-item:hover {
  --source-item-hover-bg: rgba(255, 255, 255, 0.1);
}

.source-logo {
  font-size: 2.5rem;
  margin-right: 1rem;
  flex-shrink: 0;
}

.source-info {
  flex: 1;
}

.source-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--source-name-color, #f7fafc);
  margin-bottom: 0.25rem;
}

.source-description {
  color: var(--source-desc-color, #cbd5e0);
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.source-metrics {
  display: flex;
  gap: 1rem;
}

.source-metric {
  color: var(--source-metric-color, #a0aec0);
  font-size: 0.8rem;
}

[data-theme="light"] .source-name {
  --source-name-color: #ffffff;
}

[data-theme="light"] .source-description {
  --source-desc-color: rgba(255, 255, 255, 0.9);
}

[data-theme="light"] .source-metric {
  --source-metric-color: rgba(255, 255, 255, 0.8);
}

[data-theme="dark"] .source-name {
  --source-name-color: #f7fafc;
}

[data-theme="dark"] .source-description {
  --source-desc-color: #cbd5e0;
}

[data-theme="dark"] .source-metric {
  --source-metric-color: #a0aec0;
}

.source-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

.source-status.active {
  background: rgba(72, 187, 120, 0.2);
  color: #68d391;
  border: 1px solid rgba(72, 187, 120, 0.3);
}

.source-status.inactive {
  background: rgba(245, 101, 101, 0.2);
  color: #fc8181;
  border: 1px solid rgba(245, 101, 101, 0.3);
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes progressAnimation {
  0% { width: 0; }
  100% { width: var(--progress-width); }
}

/* Mobile styles */
@media (max-width: 768px) {
  .statistics-section {
    padding: 3rem 0;
  }

  .container {
    padding: 0 1rem;
  }

  .section-title {
    font-size: 2rem;
  }

  .section-subtitle {
    font-size: 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .stat-card {
    padding: 1.5rem;
  }

  .stat-value {
    font-size: 2.5rem;
  }

  .sources-grid {
    grid-template-columns: 1fr;
  }

  .source-item {
    flex-direction: column;
    text-align: center;
  }

  .source-logo {
    margin-right: 0;
    margin-bottom: 1rem;
  }

  .source-metrics {
    justify-content: center;
  }
}
</style>
