<template>
  <div class="data-visualization">
    <!-- Risk Score Radar Chart -->
    <div v-if="type === 'risk-radar'" class="chart-container">
      <h3 class="chart-title">{{ title }}</h3>
      <div class="chart-wrapper">
        <Radar 
          v-if="radarData.datasets[0].data.length > 0"
          :data="radarData" 
          :options="radarOptions" 
        />
        <div v-else class="empty-chart">
          <p>No hay datos suficientes para mostrar</p>
        </div>
      </div>
    </div>

    <!-- Security Score Timeline -->
    <div v-else-if="type === 'score-timeline'" class="chart-container">
      <h3 class="chart-title">{{ title }}</h3>
      <div class="chart-wrapper">
        <Line 
          v-if="timelineData.datasets[0].data.length > 0"
          :data="timelineData" 
          :options="timelineOptions" 
        />
        <div v-else class="empty-chart">
          <p>No hay historial disponible</p>
        </div>
      </div>
    </div>

    <!-- Risk Distribution Doughnut -->
    <div v-else-if="type === 'risk-distribution'" class="chart-container">
      <h3 class="chart-title">{{ title }}</h3>
      <div class="chart-wrapper">
        <Doughnut 
          v-if="doughnutData.datasets[0].data.some(d => d > 0)"
          :data="doughnutData" 
          :options="doughnutOptions" 
        />
        <div v-else class="empty-chart">
          <p>No hay datos de distribución</p>
        </div>
      </div>
    </div>

    <!-- Geographic Heat Map -->
    <div v-else-if="type === 'geo-heatmap'" class="chart-container">
      <h3 class="chart-title">{{ title }}</h3>
      <div class="heatmap-wrapper">
        <div class="heatmap-container" ref="heatmapContainer">
          <div 
            v-for="country in geoData" 
            :key="country.country"
            class="country-bar"
            :style="{ 
              '--intensity': country.percentage / 100,
              width: `${Math.max(country.percentage, 5)}%`
            }"
          >
            <div class="country-info">
              <span class="country-name">{{ country.country }}</span>
              <span class="country-stats">{{ country.count }} ({{ Math.round(country.percentage) }}%)</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Port Activity Chart -->
    <div v-else-if="type === 'port-activity'" class="chart-container">
      <h3 class="chart-title">{{ title }}</h3>
      <div class="chart-wrapper">
        <Bar 
          v-if="barData.datasets[0].data.length > 0"
          :data="barData" 
          :options="barOptions" 
        />
        <div v-else class="empty-chart">
          <p>No hay datos de puertos</p>
        </div>
      </div>
    </div>

    <!-- Threat Level Gauge -->
    <div v-else-if="type === 'threat-gauge'" class="chart-container">
      <h3 class="chart-title">{{ title }}</h3>
      <div class="gauge-wrapper">
        <div class="gauge-container">
          <svg class="gauge-svg" viewBox="0 0 200 120">
            <!-- Background arc -->
            <path
              class="gauge-bg"
              d="M 20 100 A 80 80 0 0 1 180 100"
              fill="none"
              stroke="#e5e7eb"
              stroke-width="8"
            />
            <!-- Progress arc -->
            <path
              class="gauge-progress"
              :d="getGaugeArc()"
              fill="none"
              :stroke="getGaugeColor()"
              stroke-width="8"
              stroke-linecap="round"
              :style="{ strokeDasharray: getGaugeDasharray() }"
            />
            <!-- Score text -->
            <text x="100" y="90" text-anchor="middle" class="gauge-score">
              {{ threatScore }}
            </text>
            <text x="100" y="105" text-anchor="middle" class="gauge-label">
              Score
            </text>
          </svg>
          <div class="gauge-description">
            <span :class="['threat-level', getThreatLevel()]">
              {{ getThreatText() }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Comparison Matrix -->
    <div v-else-if="type === 'comparison-matrix'" class="chart-container">
      <h3 class="chart-title">{{ title }}</h3>
      <div class="matrix-wrapper">
        <div class="comparison-matrix">
          <div class="matrix-header">
            <div class="matrix-cell header-cell"></div>
            <div v-for="metric in comparisonMetrics" :key="metric" class="matrix-cell header-cell">
              {{ formatMetricName(metric) }}
            </div>
          </div>
          <div v-for="ip in comparisonData" :key="ip.name" class="matrix-row">
            <div class="matrix-cell ip-cell">{{ ip.name }}</div>
            <div 
              v-for="metric in comparisonMetrics" 
              :key="`${ip.name}-${metric}`"
              class="matrix-cell data-cell"
              :class="getMatrixCellClass(ip.data[metric], metric)"
            >
              {{ formatMetricValue(ip.data[metric], metric) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Line, Doughnut, Bar, Radar } from 'vue-chartjs'
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
  BarElement,
  RadialLinearScale,
  Filler
} from 'chart.js'

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
  BarElement,
  RadialLinearScale,
  Filler
)

// Props
interface Props {
  type: 'risk-radar' | 'score-timeline' | 'risk-distribution' | 'geo-heatmap' | 'port-activity' | 'threat-gauge' | 'comparison-matrix'
  title: string
  data: any
  options?: any
}

const props = withDefaults(defineProps<Props>(), {
  options: () => ({})
})

// Refs
const heatmapContainer = ref<HTMLElement>()

// Computed properties para diferentes tipos de datos
const radarData = computed(() => {
  if (props.type !== 'risk-radar' || !props.data) {
    return { labels: [], datasets: [{ data: [] }] }
  }

  return {
    labels: ['Reputación', 'Servicios', 'Vulnerabilidades', 'Geografía', 'SSL', 'DNS'],
    datasets: [{
      label: 'Score de Seguridad',
      data: [
        props.data.reputation || 0,
        props.data.services || 0,
        props.data.vulnerabilities || 0,
        props.data.geography || 0,
        props.data.ssl || 0,
        props.data.dns || 0
      ],
      backgroundColor: 'rgba(59, 130, 246, 0.2)',
      borderColor: '#3B82F6',
      borderWidth: 2,
      pointBackgroundColor: '#3B82F6',
      pointBorderColor: '#fff',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: '#3B82F6'
    }]
  }
})

const timelineData = computed(() => {
  if (props.type !== 'score-timeline' || !props.data) {
    return { labels: [], datasets: [{ data: [] }] }
  }

  return {
    labels: props.data.labels || [],
    datasets: [{
      label: 'Score de Seguridad',
      data: props.data.scores || [],
      borderColor: '#10B981',
      backgroundColor: 'rgba(16, 185, 129, 0.1)',
      fill: true,
      tension: 0.4
    }]
  }
})

const doughnutData = computed(() => {
  if (props.type !== 'risk-distribution' || !props.data) {
    return { labels: [], datasets: [{ data: [] }] }
  }

  return {
    labels: ['Seguras', 'Advertencia', 'Peligrosas'],
    datasets: [{
      data: [
        props.data.safe || 0,
        props.data.warning || 0,
        props.data.danger || 0
      ],
      backgroundColor: ['#10B981', '#F59E0B', '#EF4444'],
      borderWidth: 0,
      hoverOffset: 4
    }]
  }
})

const barData = computed(() => {
  if (props.type !== 'port-activity' || !props.data) {
    return { labels: [], datasets: [{ data: [] }] }
  }

  return {
    labels: props.data.ports || [],
    datasets: [{
      label: 'Frecuencia',
      data: props.data.counts || [],
      backgroundColor: props.data.counts?.map((count: number, index: number) => {
        const colors = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6']
        return colors[index % colors.length]
      }) || [],
      borderWidth: 0
    }]
  }
})

const geoData = computed(() => {
  if (props.type !== 'geo-heatmap' || !props.data) {
    return []
  }
  return props.data.countries || []
})

const threatScore = computed(() => {
  if (props.type !== 'threat-gauge' || !props.data) {
    return 0
  }
  return props.data.score || 0
})

const comparisonData = computed(() => {
  if (props.type !== 'comparison-matrix' || !props.data) {
    return []
  }
  return props.data.items || []
})

const comparisonMetrics = computed(() => {
  if (props.type !== 'comparison-matrix' || !props.data) {
    return []
  }
  return props.data.metrics || []
})

// Chart options
const radarOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    r: {
      angleLines: {
        display: true,
        color: 'rgba(0, 0, 0, 0.1)'
      },
      grid: {
        color: 'rgba(0, 0, 0, 0.1)'
      },
      pointLabels: {
        color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary') || '#374151'
      },
      ticks: {
        color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary') || '#6B7280',
        backdropColor: 'transparent',
        beginAtZero: true,
        max: 100
      }
    }
  },
  plugins: {
    legend: {
      labels: {
        color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary') || '#374151'
      }
    }
  },
  ...props.options
}))

const timelineOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    x: {
      grid: { color: 'rgba(0, 0, 0, 0.05)' },
      ticks: { color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary') || '#6B7280' }
    },
    y: {
      grid: { color: 'rgba(0, 0, 0, 0.05)' },
      ticks: { color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary') || '#6B7280' },
      beginAtZero: true,
      max: 100
    }
  },
  plugins: {
    legend: {
      labels: { color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary') || '#374151' }
    }
  },
  ...props.options
}))

const doughnutOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom' as const,
      labels: {
        padding: 20,
        usePointStyle: true,
        color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary') || '#374151'
      }
    }
  },
  ...props.options
}))

const barOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    x: {
      grid: { color: 'rgba(0, 0, 0, 0.05)' },
      ticks: { color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary') || '#6B7280' }
    },
    y: {
      grid: { color: 'rgba(0, 0, 0, 0.05)' },
      ticks: { color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary') || '#6B7280' },
      beginAtZero: true
    }
  },
  plugins: {
    legend: {
      labels: { color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary') || '#374151' }
    }
  },
  ...props.options
}))

// Gauge methods
const getGaugeArc = () => {
  return "M 20 100 A 80 80 0 0 1 180 100"
}

const getGaugeDasharray = () => {
  const circumference = Math.PI * 80 // Half circle
  const progress = (threatScore.value / 100) * circumference
  return `${progress} ${circumference}`
}

const getGaugeColor = () => {
  if (threatScore.value >= 80) return '#10B981'
  if (threatScore.value >= 60) return '#F59E0B'
  return '#EF4444'
}

const getThreatLevel = () => {
  if (threatScore.value >= 80) return 'safe'
  if (threatScore.value >= 60) return 'warning'
  return 'danger'
}

const getThreatText = () => {
  if (threatScore.value >= 80) return 'Seguro'
  if (threatScore.value >= 60) return 'Advertencia'
  return 'Peligroso'
}

// Matrix methods
const formatMetricName = (metric: string) => {
  const names: { [key: string]: string } = {
    score: 'Score',
    services: 'Servicios',
    reputation: 'Reputación',
    country: 'País',
    risk: 'Riesgo'
  }
  return names[metric] || metric
}

const formatMetricValue = (value: any, metric: string) => {
  if (metric === 'score') return `${value}/100`
  if (metric === 'services') return value
  if (typeof value === 'number') return Math.round(value * 100) / 100
  return value
}

const getMatrixCellClass = (value: any, metric: string) => {
  if (metric === 'score') {
    if (value >= 80) return 'high-value'
    if (value >= 60) return 'medium-value'
    return 'low-value'
  }
  return ''
}
</script>

<style scoped>
.data-visualization {
  width: 100%;
}

.chart-container {
  background: var(--bg-primary);
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.chart-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
  text-align: center;
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
  font-style: italic;
}

/* Heatmap */
.heatmap-wrapper {
  max-height: 300px;
  overflow-y: auto;
}

.heatmap-container {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.country-bar {
  background: linear-gradient(
    90deg,
    rgba(59, 130, 246, var(--intensity, 0.1)),
    rgba(59, 130, 246, calc(var(--intensity, 0.1) * 0.5))
  );
  border-radius: 6px;
  padding: 0.75rem;
  border: 1px solid rgba(59, 130, 246, 0.2);
  transition: all 0.3s ease;
}

.country-bar:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.country-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.country-name {
  font-weight: 600;
  color: var(--text-primary);
}

.country-stats {
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Gauge */
.gauge-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.gauge-container {
  text-align: center;
}

.gauge-svg {
  width: 200px;
  height: 120px;
}

.gauge-score {
  font-size: 24px;
  font-weight: 700;
  fill: var(--text-primary);
}

.gauge-label {
  font-size: 12px;
  fill: var(--text-secondary);
}

.gauge-description {
  margin-top: 1rem;
}

.threat-level {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.9rem;
}

.threat-level.safe {
  background: #D1FAE5;
  color: #065F46;
}

.threat-level.warning {
  background: #FEF3C7;
  color: #92400E;
}

.threat-level.danger {
  background: #FEE2E2;
  color: #991B1B;
}

/* Matrix */
.matrix-wrapper {
  overflow-x: auto;
}

.comparison-matrix {
  min-width: 100%;
  border-collapse: collapse;
  display: table;
}

.matrix-header,
.matrix-row {
  display: table-row;
}

.matrix-cell {
  display: table-cell;
  padding: 0.75rem;
  border: 1px solid var(--border-primary);
  text-align: center;
  vertical-align: middle;
}

.header-cell {
  background: var(--bg-secondary);
  font-weight: 600;
  color: var(--text-primary);
}

.ip-cell {
  background: var(--bg-secondary);
  font-weight: 600;
  font-family: monospace;
  text-align: left;
}

.data-cell {
  background: var(--bg-primary);
  color: var(--text-secondary);
}

.high-value {
  background: #D1FAE5 !important;
  color: #065F46 !important;
  font-weight: 600;
}

.medium-value {
  background: #FEF3C7 !important;
  color: #92400E !important;
  font-weight: 600;
}

.low-value {
  background: #FEE2E2 !important;
  color: #991B1B !important;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 768px) {
  .chart-wrapper {
    height: 250px;
  }
  
  .gauge-svg {
    width: 150px;
    height: 90px;
  }
  
  .matrix-cell {
    padding: 0.5rem;
    font-size: 0.9rem;
  }
}
</style>
