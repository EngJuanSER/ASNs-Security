<template>
  <div class="vulnerabilities-section">
    <div class="section-header">
      <h3 class="section-title">
        <svg class="icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        Vulnerabilidades Detectadas
      </h3>
      <span v-if="vulnerabilities.length > 0" class="vuln-count" :class="getSeverityClass(highestSeverity)">
        {{ vulnerabilities.length }} CVE{{ vulnerabilities.length !== 1 ? 's' : '' }}
      </span>
    </div>

    <div v-if="vulnerabilities.length === 0" class="empty-state">
      <svg class="empty-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <p class="empty-text">No se detectaron vulnerabilidades conocidas</p>
      <p class="empty-subtext">Los servicios analizados no tienen CVEs registrados en NVD</p>
    </div>

    <div v-else class="vulnerabilities-list">
      <div 
        v-for="vuln in sortedVulnerabilities" 
        :key="vuln.id"
        class="vulnerability-card"
        :class="getSeverityClass(vuln.severity)"
      >
        <div class="vuln-header">
          <div class="vuln-id-section">
            <span class="vuln-id">{{ vuln.id }}</span>
            <span class="vuln-severity" :class="getSeverityClass(vuln.severity)">
              {{ vuln.severity.toUpperCase() }}
            </span>
            <span class="vuln-cvss">
              CVSS: {{ vuln.cvss.toFixed(1) }}
            </span>
          </div>
          <button 
            class="expand-btn" 
            @click="toggleExpand(vuln.id)"
            :aria-label="expandedVulns.has(vuln.id) ? 'Contraer' : 'Expandir'"
          >
            <svg 
              class="expand-icon" 
              :class="{ 'expanded': expandedVulns.has(vuln.id) }"
              xmlns="http://www.w3.org/2000/svg" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>
        </div>

        <div class="vuln-title">
          {{ vuln.title }}
        </div>

        <transition name="expand">
          <div v-if="expandedVulns.has(vuln.id)" class="vuln-details">
            <div class="detail-section impact-section">
              <h4 class="detail-label">
                <svg class="section-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                ¿Qué significa esto?
              </h4>
              <p class="detail-text-simple" v-html="getSimpleExplanation(vuln)"></p>
            </div>

            <div class="detail-section">
              <h4 class="detail-label">
                <svg class="section-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Descripción Técnica
              </h4>
              <p class="detail-text">{{ vuln.description }}</p>
            </div>

            <div class="detail-section solution-section">
              <h4 class="detail-label">
                <svg class="section-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
                Cómo Solucionarlo
              </h4>
              <p class="detail-text-solution">{{ vuln.solution }}</p>
            </div>

            <div v-if="vuln.references && vuln.references.length > 0" class="detail-section">
              <h4 class="detail-label">
                <svg class="section-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                </svg>
                Referencias y Más Información
              </h4>
              <ul class="references-list">
                <li v-for="(ref, index) in vuln.references.slice(0, 3)" :key="index">
                  <a :href="ref" target="_blank" rel="noopener noreferrer" class="reference-link">
                    {{ ref }}
                    <svg class="external-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface Vulnerability {
  id: string
  title: string
  severity: 'low' | 'medium' | 'high' | 'critical'
  cvss: number
  description: string
  solution: string
  references: string[]
}

interface Props {
  vulnerabilities: Vulnerability[]
}

const props = defineProps<Props>()

const expandedVulns = ref<Set<string>>(new Set())

const sortedVulnerabilities = computed(() => {
  const severityOrder = { critical: 4, high: 3, medium: 2, low: 1 }
  return [...props.vulnerabilities].sort((a, b) => {
    return severityOrder[b.severity] - severityOrder[a.severity] || b.cvss - a.cvss
  })
})

const highestSeverity = computed(() => {
  if (props.vulnerabilities.length === 0) return 'low'
  return sortedVulnerabilities.value[0].severity
})

const toggleExpand = (id: string) => {
  if (expandedVulns.value.has(id)) {
    expandedVulns.value.delete(id)
  } else {
    expandedVulns.value.add(id)
  }
}

const getSeverityClass = (severity: string) => {
  return `severity-${severity.toLowerCase()}`
}

const getSimpleExplanation = (vuln: Vulnerability): string => {
  const severityImpact = {
    critical: 'Esta es una vulnerabilidad <strong>crítica</strong> que debe ser solucionada inmediatamente. Un atacante podría comprometer completamente el sistema o robar información sensible.',
    high: 'Esta es una vulnerabilidad <strong>de alta gravedad</strong>. Permite a un atacante realizar acciones maliciosas significativas como ejecutar código, acceder a datos confidenciales o tomar control del servicio.',
    medium: 'Esta es una vulnerabilidad <strong>de gravedad media</strong>. Podría permitir a un atacante obtener información no autorizada o degradar el servicio. Debe ser corregida en el corto plazo.',
    low: 'Esta es una vulnerabilidad <strong>de baja gravedad</strong>. El riesgo es limitado pero debe ser corregida para mejorar la seguridad general del sistema.'
  }
  
  let explanation = severityImpact[vuln.severity] || severityImpact.low
  
  // Agregar contexto basado en palabras clave comunes
  const title = vuln.title.toLowerCase()
  const desc = vuln.description.toLowerCase()
  
  if (title.includes('remote code execution') || title.includes('rce') || desc.includes('execute code')) {
    explanation += ' <strong>Permite ejecutar código arbitrario</strong> en el servidor, lo que significa que un atacante puede instalar malware, robar datos o usar el servidor para atacar otros sistemas.'
  } else if (title.includes('sql injection') || desc.includes('sql injection')) {
    explanation += ' <strong>Permite inyección SQL</strong>, lo que significa que un atacante puede acceder, modificar o eliminar información de la base de datos sin autorización.'
  } else if (title.includes('xss') || title.includes('cross-site scripting') || desc.includes('xss')) {
    explanation += ' <strong>Permite inyección de scripts maliciosos</strong> que pueden robar cookies de sesión, credenciales o redirigir usuarios a sitios fraudulentos.'
  } else if (title.includes('denial of service') || title.includes('dos') || desc.includes('denial of service')) {
    explanation += ' <strong>Permite ataques de denegación de servicio</strong>, lo que significa que un atacante puede hacer que el servicio deje de funcionar para usuarios legítimos.'
  } else if (title.includes('authentication bypass') || desc.includes('bypass authentication')) {
    explanation += ' <strong>Permite saltarse la autenticación</strong>, lo que significa que un atacante puede acceder al sistema sin necesidad de credenciales válidas.'
  } else if (title.includes('privilege escalation') || desc.includes('privilege escalation')) {
    explanation += ' <strong>Permite escalada de privilegios</strong>, lo que significa que un usuario sin privilegios puede obtener acceso de administrador.'
  } else if (title.includes('information disclosure') || desc.includes('information disclosure')) {
    explanation += ' <strong>Expone información sensible</strong> como configuraciones, rutas de archivos, versiones de software o datos de usuarios.'
  } else if (title.includes('buffer overflow') || desc.includes('buffer overflow')) {
    explanation += ' <strong>Permite desbordamiento de buffer</strong>, lo que puede llevar a la ejecución de código malicioso o caídas del sistema.'
  } else if (title.includes('csrf') || title.includes('cross-site request forgery')) {
    explanation += ' <strong>Permite falsificación de peticiones</strong>, lo que significa que un atacante puede hacer que usuarios legítimos realicen acciones no deseadas sin su conocimiento.'
  } else if (title.includes('directory traversal') || title.includes('path traversal')) {
    explanation += ' <strong>Permite acceso a archivos del sistema</strong>, lo que significa que un atacante puede leer archivos confidenciales fuera del directorio web.'
  }
  
  return explanation
}
</script>

<style scoped>
.vulnerabilities-section {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-primary);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border-primary);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}

.icon {
  width: 24px;
  height: 24px;
  color: #EF4444;
}

.vuln-count {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.vuln-count.severity-low {
  background: #D1FAE5;
  color: #065F46;
}

.vuln-count.severity-medium {
  background: #FEF3C7;
  color: #92400E;
}

.vuln-count.severity-high {
  background: #FEE2E2;
  color: #991B1B;
}

.vuln-count.severity-critical {
  background: #7C2D12;
  color: #FEE2E2;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px dashed var(--border-primary);
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #10B981;
  margin: 0 auto 16px;
}

.empty-text {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
}

.empty-subtext {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.vulnerabilities-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.vulnerability-card {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid var(--border-primary);
  transition: all 0.2s;
  border: 1px solid var(--border-primary);
}

.vulnerability-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.vulnerability-card.severity-low {
  border-left-color: #10B981;
}

.vulnerability-card.severity-medium {
  border-left-color: #F59E0B;
}

.vulnerability-card.severity-high {
  border-left-color: #EF4444;
}

.vulnerability-card.severity-critical {
  border-left-color: #7C2D12;
  background: linear-gradient(90deg, rgba(124, 45, 18, 0.05) 0%, transparent 100%);
}

.vuln-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.vuln-id-section {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.vuln-id {
  font-family: 'Courier New', monospace;
  font-weight: 800;
  color: #3B82F6;
  font-size: 15px;
}

.vuln-severity {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.vuln-severity.severity-low {
  background: #D1FAE5;
  color: #065F46;
}

.vuln-severity.severity-medium {
  background: #FEF3C7;
  color: #92400E;
}

.vuln-severity.severity-high {
  background: #FEE2E2;
  color: #991B1B;
}

.vuln-severity.severity-critical {
  background: #7C2D12;
  color: #FEE2E2;
}

.vuln-cvss {
  font-family: 'Courier New', monospace;
  font-weight: 700;
  color: var(--text-primary);
  font-size: 13px;
  padding: 4px 10px;
  background: var(--bg-primary);
  border-radius: 8px;
  border: 1px solid var(--border-primary);
}

.expand-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: background 0.2s;
}

.expand-btn:hover {
  background: var(--bg-primary);
}

.expand-icon {
  width: 20px;
  height: 20px;
  color: var(--text-secondary);
  transition: transform 0.3s;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.vuln-title {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  margin-bottom: 12px;
}

.vuln-details {
  padding-top: 12px;
  border-top: 1px solid var(--border-primary);
}

.detail-section {
  margin-bottom: 16px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 700;
  margin: 0 0 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-icon {
  width: 18px;
  height: 18px;
  color: var(--color-primary);
}

.impact-section .detail-label {
  color: #7C2D12;
}

.impact-section .section-icon {
  color: #EF4444;
}

.solution-section .detail-label {
  color: #065F46;
}

.solution-section .section-icon {
  color: #10B981;
}

.detail-text {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.detail-text-simple {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
  padding: 1rem;
  background: rgba(239, 68, 68, 0.05);
  border-radius: 8px;
}

.detail-text-simple :deep(strong) {
  color: #991B1B;
  font-weight: 700;
}

.detail-text-solution {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
  padding: 1rem;
  background: rgba(16, 185, 129, 0.05);
  border-radius: 8px;
  font-weight: 500;
}

.references-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reference-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #3B82F6;
  text-decoration: none;
  font-size: 13px;
  font-family: 'Courier New', monospace;
  word-break: break-all;
  transition: color 0.2s;
}

.reference-link:hover {
  color: #2563EB;
  text-decoration: underline;
}

.external-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 1000px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

/* Estilos para modo oscuro - Mejorar contraste */
[data-theme="dark"] .vuln-count.severity-low {
  background: rgba(16, 185, 129, 0.2);
  color: #6EE7B7;
  border: 1px solid rgba(16, 185, 129, 0.4);
}

[data-theme="dark"] .vuln-count.severity-medium {
  background: rgba(245, 158, 11, 0.2);
  color: #FCD34D;
  border: 1px solid rgba(245, 158, 11, 0.4);
}

[data-theme="dark"] .vuln-count.severity-high {
  background: rgba(239, 68, 68, 0.2);
  color: #FCA5A5;
  border: 1px solid rgba(239, 68, 68, 0.4);
}

[data-theme="dark"] .section-title {
  color: #f3f4f6;
}

[data-theme="dark"] .icon {
  color: #FCA5A5;
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .vuln-id-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .vuln-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .expand-btn {
    align-self: flex-end;
  }
}
</style>
