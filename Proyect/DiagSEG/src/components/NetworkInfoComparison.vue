<template>
  <div class="comparison-block">
    <h3 class="block-title">Información de Red</h3>
    <div class="network-comparison">
      <div class="network-column">
        <h4 class="column-title">{{ query1 }}</h4>
        <div class="network-details" v-if="result1">
          <div class="network-item">
            <span class="network-label">ASN:</span>
            <span class="network-value">{{ result1.geolocation.asn }}</span>
          </div>
          <div class="network-item">
            <span class="network-label">ISP:</span>
            <span class="network-value">{{ result1.geolocation.isp }}</span>
          </div>
          <div class="network-item">
            <span class="network-label">Organización:</span>
            <span class="network-value">{{ result1.geolocation.org || 'No disponible' }}</span>
          </div>
          <div class="network-item">
            <span class="network-label">Zona Horaria:</span>
            <span class="network-value">{{ result1.geolocation.timezone || 'No disponible' }}</span>
          </div>
        </div>
      </div>
      
      <div class="network-column">
        <h4 class="column-title">{{ query2 }}</h4>
        <div class="network-details" v-if="result2">
          <div class="network-item">
            <span class="network-label">ASN:</span>
            <span class="network-value">{{ result2.geolocation.asn }}</span>
          </div>
          <div class="network-item">
            <span class="network-label">ISP:</span>
            <span class="network-value">{{ result2.geolocation.isp }}</span>
          </div>
          <div class="network-item">
            <span class="network-label">Organización:</span>
            <span class="network-value">{{ result2.geolocation.org || 'No disponible' }}</span>
          </div>
          <div class="network-item">
            <span class="network-label">Zona Horaria:</span>
            <span class="network-value">{{ result2.geolocation.timezone || 'No disponible' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type AnalysisResult } from '@/services/api'

interface Props {
  query1: string
  query2: string
  result1: AnalysisResult | null
  result2: AnalysisResult | null
}

defineProps<Props>()
</script>

<style scoped>
.comparison-block {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-primary);
}

.block-title {
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border-primary);
}

.network-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.network-column {
  min-height: 150px;
}

.column-title {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-primary);
}

.network-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.network-item {
  background: var(--bg-secondary);
  padding: 12px 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s;
  border: 1px solid var(--border-primary);
}

.network-item:hover {
  background: var(--bg-tertiary);
  transform: translateX(4px);
}

.network-label {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 14px;
}

.network-value {
  color: var(--text-primary);
  font-size: 14px;
  font-family: 'Courier New', monospace;
  text-align: right;
  font-weight: 600;
}

@media (max-width: 768px) {
  .network-comparison {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .network-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .network-value {
    text-align: left;
  }
}
</style>
