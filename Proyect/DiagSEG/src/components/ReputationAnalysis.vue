<template>
  <div class="reputation-analysis">
    <h4 class="section-subtitle">
      <svg class="section-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0121 12c0 6.627-5.373 12-12 12S-3 18.627-3 12 2.373 0 9 0c2.572 0 4.961.821 6.916 2.203" />
      </svg>
      Análisis de Reputación
    </h4>
    
    <p class="reputation-intro">
      Este análisis evalúa la reputación de la IP consultando múltiples fuentes de seguridad.
      El nivel de <strong>confianza</strong> indica qué tan segura está cada fuente de su evaluación.
    </p>

    <div 
      v-for="source in reputation" 
      :key="source.name"
      class="reputation-card"
      :class="`status-${source.status}`"
    >
      <div class="reputation-header">
        <div class="reputation-source">
          <span class="source-name">{{ source.name }}</span>
          <span class="status-badge" :class="`status-${source.status}`">
            <svg v-if="source.status === 'clean'" class="status-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <svg v-else-if="source.status === 'suspicious'" class="status-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <svg v-else class="status-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            {{ source.statusText }}
          </span>
        </div>
        <div class="confidence-badge" v-if="source.confidence" :title="`Esta fuente tiene un ${source.confidence}% de confianza en su evaluación`">
          <svg class="confidence-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Confianza: {{ source.confidence }}%
        </div>
      </div>
      <p class="reputation-details" v-if="source.details">
        {{ source.details }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ReputationData } from '@/services/api'

interface Props {
  reputation: ReputationData[]
}

defineProps<Props>()
</script>

<style scoped>
.reputation-analysis {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid var(--border-secondary);
}

.section-subtitle {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 0.75rem 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.section-icon {
  width: 22px;
  height: 22px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.reputation-intro {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0 0 1.25rem 0;
  padding: 0.75rem;
  background: var(--bg-primary);
  border-radius: 8px;
}

.reputation-intro strong {
  color: var(--text-primary);
}

.reputation-card {
  background: var(--bg-primary);
  border-radius: 8px;
  padding: 1.25rem;
  margin-bottom: 0.75rem;
  border: 1px solid var(--border-secondary);
  transition: all 0.3s ease;
}

.reputation-card:last-child {
  margin-bottom: 0;
}

.reputation-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.reputation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.reputation-source {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.source-name {
  font-weight: 700;
  color: var(--text-primary);
  font-size: 1rem;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.status-badge.status-clean {
  background: #D1FAE5;
  color: #065F46;
}

.status-badge.status-suspicious {
  background: #FEF3C7;
  color: #92400E;
}

.status-badge.status-malicious {
  background: #FEE2E2;
  color: #991B1B;
}

.confidence-badge {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 0.875rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-primary);
  cursor: help;
  transition: all 0.2s ease;
}

.confidence-badge:hover {
  background: var(--color-primary);
  color: white;
}

.confidence-icon {
  width: 16px;
  height: 16px;
}

.reputation-details {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .reputation-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
