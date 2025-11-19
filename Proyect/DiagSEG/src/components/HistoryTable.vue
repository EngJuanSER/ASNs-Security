<template>
  <div class="history-table-container">
    <!-- Empty State -->
    <div v-if="analyses.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="currentColor">
          <path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/>
        </svg>
      </div>
      <h3 class="empty-title">No hay análisis en el historial</h3>
      <p class="empty-text">
        Realiza tu primer análisis para comenzar a ver estadísticas aquí.
      </p>
      <router-link to="/analysis" class="empty-cta">
        Hacer Análisis
      </router-link>
    </div>

    <!-- Table with data -->
    <div v-else>
      <div class="history-table-wrapper">
        <table class="history-table">
          <thead>
            <tr>
              <th>IP/Dominio</th>
              <th>Tipo</th>
              <th>Score</th>
              <th>País</th>
              <th>Estado</th>
              <th>Fecha</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="analysis in paginatedAnalyses" 
              :key="analysis.id"
              class="history-row"
              @click="$emit('load-analysis', analysis)"
            >
            <td class="analysis-query">{{ analysis.query }}</td>
            <td>
              <span class="type-badge">{{ getTypeText(analysis.type) }}</span>
            </td>
            <td>
              <span class="score-badge" :class="getScoreClass(analysis.securityScore)">
                {{ analysis.securityScore }}
              </span>
            </td>
            <td>{{ analysis.country }}</td>
            <td>
              <span class="status-badge" :class="analysis.status">
                {{ getStatusText(analysis.status) }}
              </span>
            </td>
            <td class="analysis-date">
              {{ formatDate(analysis.timestamp) }}
              <span class="time-ago">{{ formatTimeAgo(analysis.timestamp) }}</span>
            </td>
            <td class="actions-cell">
              <button 
                class="delete-btn"
                @click.stop="$emit('delete-analysis', analysis.id)"
                title="Eliminar"
              >
                ×
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="totalPages > 1">
      <button 
        class="pagination-btn"
        :disabled="currentPage === 1"
        @click="$emit('update:currentPage', currentPage - 1)"
      >
        Anterior
      </button>
      <span class="pagination-info">
        Página {{ currentPage }} de {{ totalPages }}
      </span>
      <button 
        class="pagination-btn"
        :disabled="currentPage === totalPages"
        @click="$emit('update:currentPage', currentPage + 1)"
      >
        Siguiente
      </button>
    </div>
    </div><!-- end v-else -->
  </div><!-- end history-table-container -->
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AnalysisHistoryEntry } from '@/services/statistics'

interface Props {
  analyses: AnalysisHistoryEntry[]
  currentPage: number
  itemsPerPage: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'load-analysis': [analysis: AnalysisHistoryEntry]
  'delete-analysis': [id: string]
  'update:currentPage': [page: number]
}>()

const paginatedAnalyses = computed(() => {
  const start = (props.currentPage - 1) * props.itemsPerPage
  const end = start + props.itemsPerPage
  return props.analyses.slice(start, end)
})

const totalPages = computed(() => Math.ceil(props.analyses.length / props.itemsPerPage))

const getTypeText = (type: string): string => {
  const typeMap = {
    ipv4: 'IPv4',
    ipv6: 'IPv6',
    domain: 'Dominio'
  }
  return typeMap[type as keyof typeof typeMap] || type
}

const getStatusText = (status: string): string => {
  const statusMap = {
    safe: 'Segura',
    warning: 'Advertencia', 
    danger: 'Peligrosa'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const getScoreClass = (score: number): string => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

const formatDate = (timestamp: number): string => {
  return new Date(timestamp).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatTimeAgo = (timestamp: number): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) return `hace ${minutes}m`
  if (hours < 24) return `hace ${hours}h`
  return `hace ${days}d`
}
</script>

<style scoped>
.history-table-container {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid var(--border-primary);
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  margin-bottom: 1rem;
  color: var(--text-tertiary);
  opacity: 0.5;
}

.empty-title {
  color: var(--text-primary);
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.empty-text {
  color: var(--text-secondary);
  font-size: 1rem;
  margin-bottom: 2rem;
}

.empty-cta {
  display: inline-block;
  padding: 0.75rem 2rem;
  background: var(--color-primary);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.empty-cta:hover {
  background: #2563EB;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.history-table-wrapper {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
}

.history-table thead {
  background: var(--bg-secondary);
}

.history-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 700;
  color: var(--text-primary);
  border-bottom: 2px solid var(--border-primary);
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.history-row {
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid var(--border-primary);
}

.history-row:hover {
  background: var(--bg-secondary);
}

.history-table td {
  padding: 1rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.analysis-query {
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'Courier New', monospace;
}

.type-badge,
.status-badge,
.score-badge {
  padding: 0.35rem 0.85rem;
  border-radius: 14px;
  font-size: 0.75rem;
  font-weight: 700;
  display: inline-block;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.type-badge {
  background: #3B82F6;
  color: white;
}

.status-badge.safe { background: #10B981; color: white; }
.status-badge.warning { background: #F59E0B; color: white; }
.status-badge.danger { background: #EF4444; color: white; }

.score-badge.score-high { background: #10B981; color: white; }
.score-badge.score-medium { background: #F59E0B; color: white; }
.score-badge.score-low { background: #EF4444; color: white; }

.analysis-date {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.time-ago {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

.actions-cell {
  text-align: center;
}

.delete-btn {
  background: #EF4444;
  color: white;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.5rem;
  line-height: 1;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.delete-btn:hover {
  background: #DC2626;
  transform: scale(1.15);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-primary);
}

.pagination-btn {
  padding: 0.65rem 1.25rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.pagination-btn:hover:not(:disabled) {
  background: #2563EB;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.pagination-btn:disabled {
  background: var(--bg-secondary);
  color: var(--text-tertiary);
  cursor: not-allowed;
  opacity: 0.6;
  border: 1px solid var(--border-primary);
}

.pagination-info {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .history-table {
    font-size: 0.85rem;
  }
  
  .history-table th,
  .history-table td {
    padding: 0.75rem 0.5rem;
  }
  
  .empty-state {
    padding: 3rem 1rem;
  }
  
  .empty-icon svg {
    width: 48px;
    height: 48px;
  }
  
  .empty-title {
    font-size: 1.25rem;
  }
}
</style>
