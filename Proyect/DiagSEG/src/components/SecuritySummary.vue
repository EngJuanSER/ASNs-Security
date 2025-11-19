<template>
  <div class="security-summary">
    <h4 class="summary-title">Resumen del Análisis</h4>
    <div class="summary-grid">
      <div class="summary-item">
        <span class="summary-label">Servicios Detectados:</span>
        <span class="summary-value">{{ servicesCount }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">Vulnerabilidades:</span>
        <span class="summary-value" :class="getVulnClass(vulnerabilitiesCount)">
          {{ vulnerabilitiesCount }}
        </span>
      </div>
      <div class="summary-item">
        <span class="summary-label">Fuentes Consultadas:</span>
        <span class="summary-value">{{ sourcesCount }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">Tiempo de Escaneo:</span>
        <span class="summary-value">{{ formatDuration(scanDuration) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  servicesCount: number
  vulnerabilitiesCount: number
  sourcesCount: number
  scanDuration?: number
}

const props = defineProps<Props>()

const getVulnClass = (count: number) => {
  if (count === 0) return 'text-success'
  if (count <= 3) return 'text-warning'
  return 'text-danger'
}

const formatDuration = (duration?: number) => {
  if (!duration) return 'N/A'
  if (duration < 1000) return `${duration}ms`
  const seconds = Math.floor(duration / 1000)
  if (seconds < 60) return `${seconds}s`
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}m ${remainingSeconds}s`
}
</script>

<style scoped>
.security-summary {
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-primary) 100%);
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid var(--border-secondary);
}

.summary-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.summary-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.summary-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.summary-value.text-success { color: #10B981; }
.summary-value.text-warning { color: #F59E0B; }
.summary-value.text-danger { color: #EF4444; }

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
