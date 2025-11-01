<template>
  <div class="comparison-block">
    <h3 class="block-title">Reputación</h3>
    <div class="reputation-comparison">
      <div class="reputation-column">
        <h4 class="column-title">{{ query1 }}</h4>
        <div class="reputation-list">
          <div 
            v-for="rep in result1?.reputation" 
            :key="`1-${rep.name}`"
            class="reputation-item"
          >
            <div class="reputation-header">
              <span class="reputation-source">{{ rep.name }}</span>
              <span class="reputation-status" :class="rep.status">
                {{ rep.statusText }}
              </span>
            </div>
            <p class="reputation-details">{{ rep.details }}</p>
          </div>
        </div>
      </div>
      
      <div class="reputation-column">
        <h4 class="column-title">{{ query2 }}</h4>
        <div class="reputation-list">
          <div 
            v-for="rep in result2?.reputation" 
            :key="`2-${rep.name}`"
            class="reputation-item"
          >
            <div class="reputation-header">
              <span class="reputation-source">{{ rep.name }}</span>
              <span class="reputation-status" :class="rep.status">
                {{ rep.statusText }}
              </span>
            </div>
            <p class="reputation-details">{{ rep.details }}</p>
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

.reputation-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.reputation-column {
  min-height: 200px;
}

.column-title {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-primary);
}

.reputation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reputation-item {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 12px;
  border-left: 3px solid var(--border-primary);
  transition: all 0.2s;
  border: 1px solid var(--border-primary);
}

.reputation-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.reputation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.reputation-source {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.reputation-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.reputation-status.clean,
.reputation-status.safe {
  background: #d1fae5;
  color: #065f46;
}

.reputation-status.suspicious {
  background: #fef3c7;
  color: #92400e;
}

.reputation-status.malicious,
.reputation-status.dangerous {
  background: #fee2e2;
  color: #991b1b;
}

.reputation-status.unknown {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.reputation-details {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
}

@media (max-width: 768px) {
  .reputation-comparison {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
