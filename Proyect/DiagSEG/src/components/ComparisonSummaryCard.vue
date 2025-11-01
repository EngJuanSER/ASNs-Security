<template>
  <div class="summary-card" :class="winnerClass">
    <div v-if="loading" class="loading-card">
      <div class="loading-spinner"></div>
      <p class="loading-text">Analizando {{ query }}...</p>
    </div>
    <div v-else-if="result" class="summary-content">
      <div class="summary-header">
        <h3 class="summary-ip">{{ result.ip }}</h3>
        <div class="summary-badges">
          <span v-if="isWinner" class="winner-badge">Mejor Score</span>
          <span v-if="fromCache" class="cache-badge">Caché</span>
        </div>
      </div>
      <div class="summary-score">
        <div class="score-number" :class="getScoreClass(result.securityScore)">
          {{ result.securityScore }}/100
        </div>
        <div class="score-label">Score de Seguridad</div>
      </div>
      <div class="summary-details">
        <div class="detail-item">
          <span class="detail-label">País:</span>
          <span class="detail-value">{{ result.geolocation.country }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">ISP:</span>
          <span class="detail-value">{{ result.geolocation.isp }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Servicios:</span>
          <span class="detail-value">{{ result.services.length }} detectados</span>
        </div>
      </div>
    </div>
    <div v-else class="empty-card">
      <p>Esperando análisis...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AnalysisResult } from '@/services/api'

interface Props {
  query: string
  result: AnalysisResult | null
  loading: boolean
  fromCache: boolean
  isWinner: boolean
  winnerClass: string
}

defineProps<Props>()

const getScoreClass = (score: number): string => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}
</script>

<style scoped>
.summary-card {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 1.5rem;
  border: 2px solid var(--border-primary);
  transition: all 0.3s ease;
  min-height: 250px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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
  border: 3px solid var(--border-primary);
  border-top: 3px solid #3B82F6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: var(--text-primary);
  font-size: 0.95rem;
  margin: 0;
  font-weight: 500;
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
  font-size: 0.95rem;
}

.summary-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-card.winner {
  border-color: #10B981;
  border-width: 3px;
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.2);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.08) 0%, rgba(16, 185, 129, 0.03) 100%);
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.summary-ip {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  word-break: break-all;
  font-family: 'Courier New', monospace;
}

.summary-badges {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.winner-badge {
  background: #10B981;
  color: white;
  padding: 0.35rem 0.85rem;
  border-radius: 14px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3);
}

.cache-badge {
  background: #3B82F6;
  color: white;
  padding: 0.35rem 0.85rem;
  border-radius: 14px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.summary-score {
  text-align: center;
  padding: 1rem 0;
}

.score-number {
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 0.25rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.score-number.score-high { color: #10B981; }
.score-number.score-medium { color: #F59E0B; }
.score-number.score-low { color: #EF4444; }

.score-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  font-weight: 600;
}

.summary-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 0.65rem 0.75rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-primary);
}

.detail-label {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.detail-value {
  font-weight: 700;
  color: var(--text-primary);
  text-align: right;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .summary-header {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .summary-badges {
    justify-content: flex-start;
  }
  
  .score-number {
    font-size: 2rem;
  }
}
</style>
