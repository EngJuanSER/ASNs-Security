<template>
  <div class="executive-summary">
    <div class="summary-header">
      <h2 class="summary-title">Resumen Ejecutivo</h2>
      <p class="summary-subtitle">Análisis simplificado para {{ query }}</p>
    </div>

    <div class="summary-content">
      <!-- Score General -->
      <div class="summary-card score-card">
        <div class="card-icon" :class="getScoreClass(result.securityScore)">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="card-content">
          <h3 class="card-title">Estado General</h3>
          <p class="card-value" :class="getScoreClass(result.securityScore)">
            {{ getScoreText(result.securityScore) }}
          </p>
          <p class="card-description">{{ getScoreDescription(result.securityScore) }}</p>
        </div>
      </div>

      <!-- Ubicación -->
      <div class="summary-card location-card">
        <div class="card-icon location-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
        </div>
        <div class="card-content">
          <h3 class="card-title">Ubicación</h3>
          <p class="card-value">{{ result.geolocation.city }}, {{ result.geolocation.country }}</p>
          <p class="card-description">Proveedor: {{ result.geolocation.isp || 'Desconocido' }}</p>
        </div>
      </div>

      <!-- Servicios Detectados -->
      <div class="summary-card services-card">
        <div class="card-icon services-icon">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" />
          </svg>
        </div>
        <div class="card-content">
          <h3 class="card-title">Servicios Activos</h3>
          <p class="card-value">{{ result.services.length }} puertos abiertos</p>
          <p class="card-description">{{ getServicesDescription() }}</p>
        </div>
      </div>

      <!-- Vulnerabilidades -->
      <div class="summary-card vulnerabilities-card">
        <div class="card-icon" :class="getVulnClass()">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <div class="card-content">
          <h3 class="card-title">Seguridad</h3>
          <p class="card-value" :class="getVulnClass()">
            {{ result.vulnerabilities.length }} vulnerabilidades
          </p>
          <p class="card-description">{{ getVulnDescription() }}</p>
        </div>
      </div>
    </div>

    <!-- Explicación Simple -->
    <div class="summary-explanation">
      <h3 class="explanation-title">¿Qué significa esto?</h3>
      <div class="explanation-content">
        <p v-html="getFullExplanation()"></p>
      </div>
    </div>

    <!-- Acciones Recomendadas -->
    <div class="summary-actions" v-if="result.recommendations.length > 0">
      <h3 class="actions-title">Acciones Recomendadas</h3>
      <ul class="actions-list">
        <li v-for="(rec, index) in result.recommendations.slice(0, 3)" :key="index" class="action-item">
          <span class="action-icon" :class="rec.priority">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
          </span>
          <span class="action-text">{{ rec.description }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AnalysisResult } from '@/services/api'

interface Props {
  result: AnalysisResult
  query: string
}

const props = defineProps<Props>()

const getScoreClass = (score: number) => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

const getScoreText = (score: number) => {
  if (score >= 90) return 'Excelente'
  if (score >= 80) return 'Bueno'
  if (score >= 70) return 'Aceptable'
  if (score >= 60) return 'Regular'
  if (score >= 40) return 'Bajo'
  return 'Crítico'
}

const getScoreDescription = (score: number) => {
  if (score >= 80) return 'La dirección IP no presenta riesgos significativos de seguridad.'
  if (score >= 60) return 'Existen algunos aspectos de seguridad que requieren atención.'
  return 'Se detectaron problemas de seguridad importantes que deben ser atendidos.'
}

const getVulnClass = () => {
  const count = props.result.vulnerabilities.length
  if (count === 0) return 'vuln-none'
  if (count <= 3) return 'vuln-low'
  if (count <= 10) return 'vuln-medium'
  return 'vuln-high'
}

const getVulnDescription = () => {
  const count = props.result.vulnerabilities.length
  if (count === 0) return 'No se encontraron vulnerabilidades conocidas'
  if (count === 1) return 'Se encontró 1 vulnerabilidad conocida'
  return `Se encontraron ${count} vulnerabilidades conocidas en los servicios`
}

const getServicesDescription = () => {
  const count = props.result.services.length
  if (count === 0) return 'No hay servicios activos detectados'
  if (count === 1) return 'Se detectó 1 servicio activo'
  
  const commonServices = props.result.services.slice(0, 2).map(s => s.name).join(' y ')
  return `Servicios principales: ${commonServices}`
}

const getFullExplanation = () => {
  const score = props.result.securityScore
  const vulnCount = props.result.vulnerabilities.length
  const serviceCount = props.result.services.length
  
  let explanation = `Este análisis muestra que <strong>${props.query}</strong> tiene un nivel de seguridad <strong>${getScoreText(score).toLowerCase()}</strong>. `
  
  if (serviceCount > 0) {
    explanation += `Se detectaron <strong>${serviceCount} servicios activos</strong> (puertos abiertos), `
    explanation += 'lo que significa que hay programas esperando conexiones desde internet. '
  } else {
    explanation += 'No se detectaron servicios activos (todos los puertos están cerrados). '
  }
  
  if (vulnCount > 0) {
    explanation += `Se identificaron <strong>${vulnCount} vulnerabilidades conocidas</strong> en estos servicios, `
    explanation += 'que son fallos de seguridad documentados públicamente. '
  } else {
    explanation += 'No se encontraron vulnerabilidades conocidas en los servicios activos. '
  }
  
  if (score < 60) {
    explanation += '<br><br><strong>Recomendación:</strong> Es importante tomar medidas correctivas para mejorar la seguridad.'
  } else if (score < 80) {
    explanation += '<br><br><strong>Recomendación:</strong> Se sugiere revisar y aplicar las recomendaciones de seguridad.'
  } else {
    explanation += '<br><br><strong>Estado:</strong> La configuración actual es segura, pero siempre es bueno mantenerse actualizado.'
  }
  
  return explanation
}
</script>

<style scoped>
.executive-summary {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
  margin-bottom: 2rem;
}

.summary-header {
  text-align: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid var(--border-secondary);
}

.summary-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.summary-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.summary-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.summary-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border-secondary);
  transition: all 0.3s ease;
}

.summary-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon svg {
  width: 28px;
  height: 28px;
}

.card-icon.score-high {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.card-icon.score-medium {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.card-icon.score-low {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.card-icon.location-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.card-icon.services-icon {
  background: rgba(139, 92, 246, 0.1);
  color: #8B5CF6;
}

.card-icon.vuln-none {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.card-icon.vuln-low {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.card-icon.vuln-medium,
.card-icon.vuln-high {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
}

.card-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.card-value.score-high {
  color: #10B981;
}

.card-value.score-medium {
  color: #F59E0B;
}

.card-value.score-low {
  color: #EF4444;
}

.card-value.vuln-none {
  color: #10B981;
}

.card-value.vuln-low {
  color: #F59E0B;
}

.card-value.vuln-medium,
.card-value.vuln-high {
  color: #EF4444;
}

.card-description {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.4;
}

.summary-explanation {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(139, 92, 246, 0.05));
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.explanation-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.explanation-content {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.8;
}

.explanation-content strong {
  color: var(--text-primary);
  font-weight: 600;
}

.summary-actions {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
}

.actions-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.actions-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.action-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  background: var(--bg-primary);
  border-radius: 8px;
  border: 1px solid var(--border-secondary);
  transition: all 0.3s ease;
}

.action-item:hover {
  transform: translateX(4px);
}

.action-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-icon svg {
  width: 16px;
  height: 16px;
}

.action-icon.high {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.action-icon.medium {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.action-icon.low,
.action-icon.info {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.action-text {
  flex: 1;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .executive-summary {
    padding: 1.5rem;
  }
  
  .summary-content {
    grid-template-columns: 1fr;
  }
  
  .summary-title {
    font-size: 1.5rem;
  }
}
</style>
