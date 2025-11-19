<template>
  <div class="vulnerabilities-summary" v-if="vulnerabilities.length > 0">
    <h4 class="section-subtitle">
      <svg class="section-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
      Vulnerabilidades Detectadas (NVD)
    </h4>
    <div class="vuln-stats">
      <div class="vuln-stat severity-critical">
        <span class="vuln-count">{{ countBySeverity('critical') }}</span>
        <span class="vuln-label">Críticas</span>
      </div>
      <div class="vuln-stat severity-high">
        <span class="vuln-count">{{ countBySeverity('high') }}</span>
        <span class="vuln-label">Altas</span>
      </div>
      <div class="vuln-stat severity-medium">
        <span class="vuln-count">{{ countBySeverity('medium') }}</span>
        <span class="vuln-label">Medias</span>
      </div>
      <div class="vuln-stat severity-low">
        <span class="vuln-count">{{ countBySeverity('low') }}</span>
        <span class="vuln-label">Bajas</span>
      </div>
    </div>
    <p class="vuln-note">
      Ver la sección completa de "Vulnerabilidades (CVEs)" más abajo para detalles y soluciones.
    </p>
  </div>
</template>

<script setup lang="ts">
import type { Vulnerability } from '@/services/api'

interface Props {
  vulnerabilities: Vulnerability[]
}

const props = defineProps<Props>()

const countBySeverity = (severity: string) => {
  return props.vulnerabilities.filter(v => v.severity === severity).length
}
</script>

<style scoped>
.vulnerabilities-summary {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid var(--border-secondary);
}

.section-subtitle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.section-icon {
  width: 20px;
  height: 20px;
  color: var(--color-primary);
}

.vuln-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.vuln-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem;
  border-radius: 8px;
  border: 2px solid transparent;
}

.vuln-stat.severity-critical {
  background: #FEE2E2;
  border-color: #991B1B;
}

.vuln-stat.severity-high {
  background: #FED7AA;
  border-color: #C2410C;
}

.vuln-stat.severity-medium {
  background: #FEF3C7;
  border-color: #92400E;
}

.vuln-stat.severity-low {
  background: #DBEAFE;
  border-color: #1E40AF;
}

.vuln-count {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
}

.vuln-label {
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.vuln-note {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-style: italic;
  margin: 0;
  padding: 0.75rem;
  background: var(--bg-primary);
  border-radius: 6px;
}

/* Estilos para modo oscuro */
[data-theme="dark"] .vuln-stat.severity-critical {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.6);
}

[data-theme="dark"] .vuln-stat.severity-high {
  background: rgba(234, 88, 12, 0.2);
  border-color: rgba(234, 88, 12, 0.6);
}

[data-theme="dark"] .vuln-stat.severity-medium {
  background: rgba(245, 158, 11, 0.2);
  border-color: rgba(245, 158, 11, 0.6);
}

[data-theme="dark"] .vuln-stat.severity-low {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.6);
}

[data-theme="dark"] .vuln-count {
  color: #f1f5f9;
}

[data-theme="dark"] .vuln-label {
  color: #cbd5e1;
}

[data-theme="dark"] .section-subtitle {
  color: #f3f4f6;
}

[data-theme="dark"] .section-icon {
  color: #fca5a5;
}

@media (max-width: 768px) {
  .vuln-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
