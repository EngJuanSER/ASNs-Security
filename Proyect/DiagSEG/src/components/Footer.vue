<template>
  <footer class="footer">
    <div class="footer-container">
      <!-- Sección principal del footer -->
      <div class="footer-main">
        <div class="footer-brand">
          <div class="footer-logo">
            <span class="brand-text">DiagSEG</span>
          </div>
          <p class="footer-description">
            Herramienta de código abierto para diagnóstico unificado de seguridad IP.
            Democratizando el acceso a herramientas de ciberseguridad en Colombia.
          </p>
          <div class="footer-links">
            <a 
              href="https://github.com/EngJuanSER/ASNs-Security" 
              target="_blank" 
              class="footer-link"
              title="Ver código fuente"
            >
              GitHub
            </a>
            <a 
              href="mailto:jumserranor@udistrital.edu.co" 
              class="footer-link"
              title="Contactar"
            >
              Contacto
            </a>
          </div>
        </div>

        <div class="footer-grid">
          <!-- Fuentes de datos -->
          <div class="footer-section">
            <h3 class="section-title">Fuentes de Datos</h3>
            <ul class="footer-list">
              <li><a href="https://nmap.org/" target="_blank" class="list-link">Nmap</a></li>
              <li><a href="https://nvd.nist.gov/" target="_blank" class="list-link">NVD (NIST)</a></li>
              <li><a href="https://dev.maxmind.com/geoip/geolite2-free-geolocation-data" target="_blank" class="list-link">GeoLite2</a></li>
              <li><a href="https://ip-api.com/" target="_blank" class="list-link">IP-API</a></li>
            </ul>
          </div>

          <!-- Tecnologías -->
          <div class="footer-section">
            <h3 class="section-title">Tecnologías</h3>
            <ul class="footer-list">
              <li><span class="list-text">Frontend: Vue 3 + TypeScript</span></li>
              <li><span class="list-text">Backend: Java 21 + Quarkus</span></li>
              <li><span class="list-text">Cache: LocalForage (IndexedDB)</span></li>
              <li><span class="list-text">Build: Vite + Gradle</span></li>
            </ul>
          </div>

          <!-- Información del proyecto -->
          <div class="footer-section">
            <h3 class="section-title">Proyecto</h3>
            <ul class="footer-list">
              <li><span class="list-text">Versión: 1.0.0</span></li>
              <li><span class="list-text">Licencia: MIT</span></li>
              <li><span class="list-text">Universidad Distrital</span></li>
              <li><span class="list-text">Open Source</span></li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Copyright y información legal -->
      <div class="footer-bottom">
        <div class="footer-copyright">
          <p>&copy; {{ currentYear }} DiagSEG. Proyecto de código abierto bajo licencia MIT.</p>
          <p class="footer-disclaimer">
            Herramienta educativa para diagnóstico de seguridad IP. Los datos se consultan desde Nmap, NVD, GeoLite2 e IP-API.
          </p>
        </div>
        
        <div class="footer-meta">
          <span class="meta-item">Universidad Distrital</span>
          <span class="meta-separator">•</span>
          <span class="meta-item">Hecho en Colombia</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  background: var(--footer-bg, linear-gradient(135deg, #2d3748 0%, #1a202c 100%));
  color: var(--footer-text, #e2e8f0);
  margin-top: auto;
}

[data-theme="dark"] .footer {
  --footer-bg: var(--color-dark-surface);
  --footer-text: var(--color-gray-300);
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 2rem 1rem;
}

.footer-main {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 3rem;
  margin-bottom: 2rem;
}

.footer-brand {
  max-width: 400px;
}

.footer-logo {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.logo-icon {
  font-size: 2rem;
  margin-right: 0.5rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.brand-text {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-description {
  color: var(--footer-desc, #cbd5e0);
  line-height: 1.6;
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
}

[data-theme="dark"] .footer-description {
  --footer-desc: var(--color-gray-400);
}

.footer-links {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.footer-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #a0aec0;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.footer-link:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
}

.link-icon {
  font-size: 1.1rem;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 2rem;
}

.footer-section {
  min-width: 0;
}

.section-title {
  color: #f7fafc;
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #4a5568;
}

.footer-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-list li {
  margin-bottom: 0.5rem;
}

.list-link {
  color: #a0aec0;
  text-decoration: none;
  transition: color 0.3s ease;
  font-size: 0.9rem;
}

.list-link:hover {
  color: #667eea;
}

.list-text {
  color: #a0aec0;
  font-size: 0.9rem;
}

.footer-bottom {
  border-top: 1px solid #4a5568;
  padding-top: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.footer-copyright {
  flex: 1;
}

.footer-copyright p {
  margin: 0 0 0.5rem 0;
  color: #a0aec0;
  font-size: 0.9rem;
}

.footer-disclaimer {
  color: #718096 !important;
  font-size: 0.8rem !important;
}

.footer-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #718096;
  font-size: 0.9rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.meta-separator {
  opacity: 0.5;
}

/* Mobile styles */
@media (max-width: 768px) {
  .footer-container {
    padding: 2rem 1rem 1rem;
  }

  .footer-main {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .footer-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }

  .footer-links {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .footer-container {
    padding: 1.5rem 1rem 1rem;
  }

  .footer-links {
    flex-direction: column;
    align-items: stretch;
  }

  .footer-link {
    justify-content: center;
  }
}
</style>
