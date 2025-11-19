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
  border-radius: 12px;
  padding: 18px 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid var(--border-primary);
  position: relative;
  overflow: hidden;
}

.recommendation-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  transition: width 0.3s ease;
}

.recommendation-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: currentColor;
}

.recommendation-item:hover::before {
  width: 8px;
}

.recommendation-item.high {
  border-color: rgba(239, 68, 68, 0.3);
}

.recommendation-item.high::before {
  background: linear-gradient(180deg, #ef4444 0%, #dc2626 100%);
}

.recommendation-item.medium {
  border-color: rgba(245, 158, 11, 0.3);
}

.recommendation-item.medium::before {
  background: linear-gradient(180deg, #f59e0b 0%, #d97706 100%);
}

.recommendation-item.low {
  border-color: rgba(59, 130, 246, 0.3);
}

.recommendation-item.low::before {
  background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
}

.recommendation-item.info {
  border-color: var(--border-primary);
}

.recommendation-item.info::before {
  background: linear-gradient(180deg, #6b7280 0%, #4b5563 100%);
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
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.8px;
  white-space: nowrap;
  text-transform: uppercase;
  border: 1px solid transparent;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.recommendation-severity.high {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #991b1b;
  border-color: #fca5a5;
}

.recommendation-severity.medium {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  border-color: #fcd34d;
}

.recommendation-severity.low {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
  border-color: #93c5fd;
}

.recommendation-severity.info {
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-secondary) 100%);
  color: var(--text-secondary);
  border-color: var(--border-primary);
}

.recommendation-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0;
  padding-left: 4px;
}

/* Estilos para modo oscuro */
[data-theme="dark"] .recommendation-item {
  background: rgba(30, 41, 59, 0.5);
  border-color: rgba(71, 85, 105, 0.3);
}

[data-theme="dark"] .recommendation-item:hover {
  background: rgba(30, 41, 59, 0.7);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .recommendation-item.high {
  border-color: rgba(239, 68, 68, 0.4);
}

[data-theme="dark"] .recommendation-item.medium {
  border-color: rgba(245, 158, 11, 0.4);
}

[data-theme="dark"] .recommendation-item.low {
  border-color: rgba(59, 130, 246, 0.4);
}

[data-theme="dark"] .recommendation-severity.high {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border-color: rgba(239, 68, 68, 0.4);
}

[data-theme="dark"] .recommendation-severity.medium {
  background: rgba(245, 158, 11, 0.2);
  color: #fcd34d;
  border-color: rgba(245, 158, 11, 0.4);
}

[data-theme="dark"] .recommendation-severity.low {
  background: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
  border-color: rgba(59, 130, 246, 0.4);
}

[data-theme="dark"] .recommendation-severity.info {
  background: rgba(107, 114, 128, 0.2);
  color: #9ca3af;
  border-color: rgba(107, 114, 128, 0.4);
}

[data-theme="dark"] .recommendation-description {
  color: #cbd5e1;
}

[data-theme="dark"] .recommendation-type {
  color: #f1f5f9;
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
