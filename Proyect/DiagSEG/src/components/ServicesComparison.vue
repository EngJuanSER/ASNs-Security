<template>
  <div class="comparison-block">
    <h3 class="block-title">Servicios Detectados</h3>
    <div class="services-comparison">
      <!-- Columna 1 -->
      <div class="services-column">
        <h4 class="column-title">{{ query1 || 'IP/Dominio 1' }}</h4>
        <div v-if="loading1" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Analizando servicios...</p>
        </div>
        <div v-else-if="result1" class="services-list">
          <div 
            v-for="service in result1.services" 
            :key="`1-${service.port}`"
            class="service-item"
            :class="service.riskLevel"
          >
            <div class="service-header">
              <span class="service-port">:{{ service.port }}</span>
              <span class="service-name">{{ service.name }}</span>
            </div>
            <div class="service-details">
              <span class="service-version">{{ service.version }}</span>
              <span class="service-risk">{{ service.riskText }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>Sin datos disponibles</p>
        </div>
      </div>
      
      <!-- Columna 2 -->
      <div class="services-column">
        <h4 class="column-title">{{ query2 || 'IP/Dominio 2' }}</h4>
        <div v-if="loading2" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Analizando servicios...</p>
        </div>
        <div v-else-if="result2" class="services-list">
          <div 
            v-for="service in result2.services" 
            :key="`2-${service.port}`"
            class="service-item"
            :class="service.riskLevel"
          >
            <div class="service-header">
              <span class="service-port">:{{ service.port }}</span>
              <span class="service-name">{{ service.name }}</span>
            </div>
            <div class="service-details">
              <span class="service-version">{{ service.version }}</span>
              <span class="service-risk">{{ service.riskText }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>Sin datos disponibles</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AnalysisResult } from '@/services/api'

interface Props {
  query1: string
  query2: string
  result1: AnalysisResult | null
  result2: AnalysisResult | null
  loading1: boolean
  loading2: boolean
}

defineProps<Props>()
</script>

<style scoped>
.comparison-block {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-primary);
}

.block-title {
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border-primary);
}

.services-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.services-column {
  min-height: 200px;
}

.column-title {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-primary);
  font-family: 'Courier New', monospace;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-primary);
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--border-primary);
  border-top: 3px solid #3B82F6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.services-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.service-item {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 14px;
  border-left: 4px solid var(--border-primary);
  transition: all 0.2s;
  border: 1px solid var(--border-primary);
}

.service-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.service-item.low {
  border-left-color: #10B981;
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.05) 0%, transparent 100%);
}

.service-item.medium {
  border-left-color: #F59E0B;
  background: linear-gradient(90deg, rgba(245, 158, 11, 0.05) 0%, transparent 100%);
}

.service-item.high {
  border-left-color: #EF4444;
  background: linear-gradient(90deg, rgba(239, 68, 68, 0.05) 0%, transparent 100%);
}

.service-header {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.service-port {
  font-family: 'Courier New', monospace;
  font-weight: 800;
  color: #3B82F6;
  font-size: 15px;
  min-width: 50px;
}

.service-name {
  font-weight: 700;
  color: var(--text-primary);
  font-size: 14px;
}

.service-details {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
  gap: 12px;
  flex-wrap: wrap;
}

.service-version {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: var(--text-primary);
}

.service-risk {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.service-risk.low {
  background: #D1FAE5;
  color: #065F46;
}

.service-risk.medium {
  background: #FEF3C7;
  color: #92400E;
}

.service-risk.high {
  background: #FEE2E2;
  color: #991B1B;
}

.no-services {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 32px 20px;
  text-align: center;
  border: 1px dashed var(--border-primary);
}

.no-services p {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
  font-weight: 500;
}

@media (max-width: 768px) {
  .services-comparison {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .service-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  
  .service-details {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
