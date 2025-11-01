<template>
  <section class="stats-summary">
    <div class="stat-card">
      <div class="stat-content">
        <div class="stat-number">{{ stats.totalAnalyses }}</div>
        <div class="stat-label">Análisis Realizados</div>
      </div>
    </div>
    
    <div class="stat-card">
      <div class="stat-content">
        <div class="stat-number">{{ stats.uniqueIPs }}</div>
        <div class="stat-label">IPs Únicas</div>
      </div>
    </div>
    
    <div class="stat-card">
      <div class="stat-content">
        <div class="stat-number">{{ stats.averageScore }}</div>
        <div class="stat-label">Score Promedio</div>
      </div>
    </div>
    
    <div class="stat-card risk-card">
      <div class="stat-content">
        <div class="risk-distribution">
          <div class="risk-item safe">
            <span class="risk-count">{{ stats.riskDistribution.safe }}</span>
            <span class="risk-label">Seguras</span>
          </div>
          <div class="risk-item warning">
            <span class="risk-count">{{ stats.riskDistribution.warning }}</span>
            <span class="risk-label">Advertencia</span>
          </div>
          <div class="risk-item danger">
            <span class="risk-count">{{ stats.riskDistribution.danger }}</span>
            <span class="risk-label">Peligrosas</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { AnalysisStatistics } from '@/services/statistics'

interface Props {
  stats: AnalysisStatistics
}

defineProps<Props>()
</script>

<style scoped>
.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 1.5rem;
  border: 2px solid var(--border-primary);
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  border-color: var(--color-primary);
}

.stat-content {
  text-align: center;
}

.stat-number {
  font-size: 2.75rem;
  font-weight: 800;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  font-weight: 700;
}

.risk-card .stat-content {
  text-align: left;
}

.risk-distribution {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.risk-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.risk-item:hover {
  transform: translateX(4px);
  border-color: currentColor;
}

.risk-item.safe {
  background: #10B981;
}

.risk-item.warning {
  background: #F59E0B;
}

.risk-item.danger {
  background: #EF4444;
}

.risk-count {
  font-weight: 800;
  font-size: 1.5rem;
  color: white;
}

.risk-label {
  font-size: 0.9rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: white;
}

@media (max-width: 768px) {
  .stats-summary {
    grid-template-columns: 1fr;
  }
  
  .stat-number {
    font-size: 2.25rem;
  }
}
</style>
