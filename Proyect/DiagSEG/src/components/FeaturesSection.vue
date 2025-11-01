<template>
  <section id="features" class="features-section">
    <div class="container">
      <div class="features-header">
        <h2 class="section-title">Características Principales</h2>
        <p class="section-subtitle">
          Análisis completo de seguridad IP con múltiples fuentes de datos integradas
        </p>
      </div>

      <div class="features-grid">
        <div 
          v-for="(feature, index) in features" 
          :key="feature.id"
          class="feature-card"
          :class="{ 'feature-card-featured': feature.featured }"
          :style="{ animationDelay: `${index * 100}ms` }"
        >
          <div class="feature-icon-wrapper">
            <div class="feature-icon">{{ feature.icon }}</div>
          </div>
          
          <div class="feature-content">
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
            
            <div class="feature-details">
              <ul class="feature-list">
                <li v-for="detail in feature.details" :key="detail" class="feature-item">
                  <span class="check-icon">✓</span>
                  {{ detail }}
                </li>
              </ul>
            </div>

            <div v-if="feature.badge" class="feature-badge">
              {{ feature.badge }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
interface Feature {
  id: string
  title: string
  description: string
  details: string[]
  badge?: string
  featured?: boolean
}

const features: Feature[] = [
  {
    id: 'analysis',
    title: 'Análisis Completo',
    description: 'Información consolidada de múltiples fuentes de datos gratuitas para un diagnóstico integral.',
    details: [
      'Datos de Censys vía BigQuery',
      'Geolocalización con GeoLite2',
      'Análisis de servicios activos',
      'Información de ISP/ASN'
    ]
  },
  {
    id: 'scoring',
    title: 'Score de Riesgo',
    description: 'Evaluación automática del nivel de amenaza con modelo ponderado basado en múltiples factores.',
    details: [
      'Reputación (40%)',
      'Puertos críticos (30%)',
      'Servicios vulnerables (20%)',
      'Contexto geográfico (10%)'
    ]
  },
  {
    id: 'geolocation',
    title: 'Geolocalización',
    description: 'Ubicación geográfica precisa usando base de datos GeoLite2 local sin límites de uso.',
    details: [
      'Base de datos local',
      'Sin límites de consultas',
      'Información de ISP/ASN',
      'Visualización en mapa'
    ]
  },
  {
    id: 'reports',
    title: 'Informes Detallados',
    description: 'Exporta resultados completos en múltiples formatos para análisis posterior.',
    details: [
      'Exportación PDF',
      'Formato JSON estructurado',
      'Recomendaciones contextuales',
      'Historial de consultas'
    ]
  },
  {
    id: 'performance',
    title: 'Alto Rendimiento',
    description: 'Respuesta rápida y eficiente con arquitectura optimizada para consultas concurrentes.',
    details: [
      'Tiempo de respuesta < 10s',
      'Consultas concurrentes',
      'Sistema de caché',
      'APIs optimizadas'
    ]
  },
  {
    id: 'opensource',
    title: 'Código Abierto',
    description: 'Proyecto completamente abierto con licencia MIT para garantizar transparencia y colaboración.',
    details: [
      'Licencia MIT',
      'Código en GitHub',
      'Documentación completa',
      'Comunidad activa'
    ],
    badge: 'Gratuito'
  }
]
</script>

<style scoped>
.features-section {
  padding: 5rem 0;
  background: var(--bg-secondary);
  position: relative;
  overflow: hidden;
}

.features-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23667eea' fill-opacity='0.03'%3E%3Cpath d='m36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E") repeat;
  opacity: 0.5;
  z-index: 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  position: relative;
  z-index: 1;
}

.features-header {
  text-align: center;
  margin-bottom: 4rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--text-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-subtitle {
  font-size: 1.2rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  margin-top: 3rem;
}

.feature-card {
  background: var(--bg-primary);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease forwards;
  opacity: 0;
  transform: translateY(30px);
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-card-featured {
  border: 2px solid var(--color-primary);
  background: var(--bg-primary);
}

.feature-card-featured::before {
  transform: scaleX(1);
}

.feature-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  margin-bottom: 1.5rem;
  position: relative;
  overflow: hidden;
}

.feature-icon-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.3) 0%, transparent 50%);
}

.feature-icon {
  font-size: 2.5rem;
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.feature-content {
  flex: 1;
}

.feature-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.75rem;
}

.feature-description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
}

.feature-details {
  margin-bottom: 1rem;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-item {
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.check-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: #48bb78;
  color: white;
  border-radius: 50%;
  margin-right: 0.75rem;
  font-size: 0.8rem;
  font-weight: bold;
  flex-shrink: 0;
}

.feature-badge {
  display: inline-block;
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Mobile styles */
@media (max-width: 768px) {
  .features-section {
    padding: 3rem 0;
  }

  .container {
    padding: 0 1rem;
  }

  .section-title {
    font-size: 2rem;
  }

  .section-subtitle {
    font-size: 1rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .feature-card {
    padding: 1.5rem;
  }

  .feature-icon-wrapper {
    width: 60px;
    height: 60px;
    border-radius: 15px;
  }

  .feature-icon {
    font-size: 2rem;
  }

  .feature-title {
    font-size: 1.2rem;
  }
}
</style>
