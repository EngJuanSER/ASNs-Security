<template>
  <div 
    v-if="result1?.recommendations?.length || result2?.recommendations?.length"
    class="comparison-block"
  >
    <h3 class="block-title">Recomendaciones de Seguridad</h3>
    <div class="recommendations-comparison">
      <div class="recommendations-column">
        <h4 class="column-title">{{ query1 }}</h4>
        <div class="recommendations-list" v-if="result1?.recommendations?.length">
          <div 
            v-for="(rec, index) in result1.recommendations" 
            :key="index"
            class="recommendation-item"
            :class="rec.priority"
          >
            <div class="recommendation-header">
              <span class="recommendation-type">{{ rec.title }}</span>
              <span class="recommendation-severity" :class="rec.priority">
                {{ rec.priority?.toUpperCase() || 'INFO' }}
              </span>
            </div>
            <p class="recommendation-description">{{ rec.description }}</p>
          </div>
        </div>
        <div v-else class="no-recommendations">
          <p>No hay recomendaciones específicas</p>
        </div>
      </div>
      
      <div class="recommendations-column">
        <h4 class="column-title">{{ query2 }}</h4>
        <div class="recommendations-list" v-if="result2?.recommendations?.length">
          <div 
            v-for="(rec, index) in result2.recommendations" 
            :key="index"
            class="recommendation-item"
            :class="rec.priority"
          >
            <div class="recommendation-header">
              <span class="recommendation-type">{{ rec.title }}</span>
              <span class="recommendation-severity" :class="rec.priority">
                {{ rec.priority?.toUpperCase() || 'INFO' }}
              </span>
            </div>
            <p class="recommendation-description">{{ rec.description }}</p>
          </div>
        </div>
        <div v-else class="no-recommendations">
          <p>No hay recomendaciones específicas</p>
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

.recommendations-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.recommendations-column {
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

.recommendations-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommendation-item {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid var(--border-primary);
  transition: all 0.2s;
  border: 1px solid var(--border-primary);
}

.recommendation-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.recommendation-item.high {
  border-left-color: #ef4444;
}

.recommendation-item.medium {
  border-left-color: #f59e0b;
}

.recommendation-item.low {
  border-left-color: #3b82f6;
}

.recommendation-item.info {
  border-left-color: var(--text-secondary);
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 12px;
}

.recommendation-type {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
  flex: 1;
}

.recommendation-severity {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.recommendation-severity.high {
  background: #fee2e2;
  color: #991b1b;
}

.recommendation-severity.medium {
  background: #fef3c7;
  color: #92400e;
}

.recommendation-severity.low {
  background: #dbeafe;
  color: #1e40af;
}

.recommendation-severity.info {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.recommendation-description {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.no-recommendations {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  border: 1px dashed var(--border-primary);
}

.no-recommendations p {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

@media (max-width: 768px) {
  .recommendations-comparison {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .recommendation-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
