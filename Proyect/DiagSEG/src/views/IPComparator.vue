<template>
  <div class="ip-comparator">
    <Navbar />
    
    <main class="main-content">
      <div class="container">
        <!-- Header -->
        <section class="comparator-header">
          <h1 class="comparator-title">Comparador de IPs</h1>
          <p class="comparator-subtitle">
            Compara hasta dos direcciones IP o dominios lado a lado
          </p>
        </section>

        <!-- Search Section -->
        <section class="search-section">
          <div class="search-grid">
            <!-- IP 1 -->
            <div class="search-column">
              <h3 class="search-title">Primera IP/Dominio</h3>
              <SearchBar 
                @search="handleSearch1"
                :placeholder="'Ej: 8.8.8.8 o google.com'"
                :loading="loading1"
              />
              
              <!-- Saved IPs for comparison -->
              <div v-if="savedComparisons.length > 0" class="saved-ips">
                <h4 class="saved-title">IPs Guardadas:</h4>
                <div class="saved-list">
                  <button 
                    v-for="saved in savedComparisons.slice(0, 5)" 
                    :key="saved.id"
                    @click="loadSavedIP(saved, 1)"
                    class="saved-btn"
                  >
                    {{ saved.query }}
                    <span class="saved-score">{{ saved.result.securityScore }}/100</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- VS Separator -->
            <div class="vs-separator">
              <div class="vs-circle">VS</div>
            </div>

            <!-- IP 2 -->
            <div class="search-column">
              <h3 class="search-title">Segunda IP/Dominio</h3>
              <SearchBar 
                @search="handleSearch2"
                :placeholder="'Ej: 1.1.1.1 o cloudflare.com'"
                :loading="loading2"
              />
              
              <!-- Saved IPs for comparison -->
              <div v-if="savedComparisons.length > 0" class="saved-ips">
                <h4 class="saved-title">IPs Guardadas:</h4>
                <div class="saved-list">
                  <button 
                    v-for="saved in savedComparisons.slice(0, 5)" 
                    :key="saved.id"
                    @click="loadSavedIP(saved, 2)"
                    class="saved-btn"
                  >
                    {{ saved.query }}
                    <span class="saved-score">{{ saved.result.securityScore }}/100</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Loading States - Versión Simple -->
        <section v-if="loading1 || loading2" class="loading-section">
          <h2 class="loading-main-title">Analizando IPs...</h2>
          
          <div class="loading-grid">
            <!-- Loading para IP 1 -->
            <div class="loading-column" :class="{ active: loading1, completed: !loading1 && result1 }">
              <h3 class="loading-column-title">{{ query1 || 'Primera IP' }}</h3>
              
              <div v-if="loading1" class="loading-spinner-wrapper">
                <div class="loading-spinner"></div>
                <p class="loading-text">Analizando...</p>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: realProgress1 + '%' }"></div>
                </div>
                <span class="progress-percentage">{{ realProgress1 }}%</span>
              </div>
              
              <div v-else-if="result1" class="loading-completed">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <p class="completed-text">✓ Análisis Completado</p>
                <p class="score-preview">Score: {{ result1.securityScore }}/100</p>
              </div>
              
              <div v-else class="loading-waiting">
                <p>En espera...</p>
              </div>
            </div>

            <!-- Loading para IP 2 -->
            <div class="loading-column" :class="{ active: loading2, completed: !loading2 && result2 }">
              <h3 class="loading-column-title">{{ query2 || 'Segunda IP' }}</h3>
              
              <div v-if="loading2" class="loading-spinner-wrapper">
                <div class="loading-spinner"></div>
                <p class="loading-text">Analizando...</p>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: realProgress2 + '%' }"></div>
                </div>
                <span class="progress-percentage">{{ realProgress2 }}%</span>
              </div>
              
              <div v-else-if="result2" class="loading-completed">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <p class="completed-text">✓ Análisis Completado</p>
                <p class="score-preview">Score: {{ result2.securityScore }}/100</p>
              </div>
              
              <div v-else class="loading-waiting">
                <p>En espera...</p>
              </div>
            </div>
          </div>
        </section>

        <!-- Comparison Results -->
        <section v-if="hasAnyResult" class="comparison-section">
          <!-- Info General Comparison -->
          <div class="info-general-comparison">
            <h2 class="comparison-title">Información General</h2>
            <div class="info-comparison-grid">
              <!-- IP 1 Info General -->
              <div class="info-card-compare">
                <div class="info-card-header">
                  <h3 class="card-subtitle">{{ query1 }}</h3>
                  <span v-if="result1FromCache" class="cache-badge">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
                    </svg>
                    Cache
                  </span>
                </div>

                <div v-if="loading1" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Cargando información...</p>
                </div>

                <template v-else-if="result1">
                  <!-- Domain Preview con Favicon (si es dominio) -->
                  <div class="domain-preview" v-if="result1.domain">
                    <div class="domain-favicon-container">
                      <img 
                        :src="`https://www.google.com/s2/favicons?domain=${result1.domain}&sz=64`" 
                        :alt="`Favicon de ${result1.domain}`"
                        class="domain-favicon"
                        @error="(e) => (e.target as HTMLImageElement).src = 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 24 24%22 fill=%22%23667eea%22%3E%3Cpath d=%22M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z%22/%3E%3C/svg%3E'"
                      />
                    </div>
                    <div class="domain-info-main">
                      <h4 class="domain-name">{{ result1.domain }}</h4>
                      <a 
                        :href="`https://${result1.domain}`" 
                        target="_blank" 
                        rel="noopener noreferrer"
                        class="domain-link"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                        </svg>
                        Visitar sitio
                      </a>
                    </div>
                  </div>

                  <!-- Info Grid -->
                  <div class="info-grid-compare">
                    <div class="info-item-compare" v-if="result1.domain">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
                        </svg>
                        Dominio
                      </span>
                      <span class="info-value-compare">{{ result1.domain }}</span>
                    </div>
                    <div class="info-item-compare">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        IP
                      </span>
                      <span class="info-value-compare" :class="{'highlight-different': result2 && result1.ip !== result2.ip}">{{ result1.ip }}</span>
                    </div>
                    <div class="info-item-compare">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                        </svg>
                        Tipo
                      </span>
                      <span class="info-value-compare info-type-badge" :class="result1.type.toLowerCase()">{{ result1.type.toUpperCase() }}</span>
                    </div>
                    <div class="info-item-compare">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        Timestamp
                      </span>
                      <span class="info-value-compare">{{ new Date(result1.timestamp).toLocaleString() }}</span>
                    </div>
                    <div class="info-item-compare" v-if="result1.geolocation?.country">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 21v-4m0 0V5a2 2 0 012-2h6.5l1 1H21l-3 6 3 6h-8.5l-1-1H5a2 2 0 00-2 2zm9-13.5V9" />
                        </svg>
                        País
                      </span>
                      <span class="info-value-compare" :class="{'highlight-different': result2?.geolocation?.country && result1.geolocation.country !== result2.geolocation.country}">
                        {{ result1.geolocation.country }}
                      </span>
                    </div>
                    <div class="info-item-compare" v-if="result1.geolocation?.isp">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" />
                        </svg>
                        ISP
                      </span>
                      <span class="info-value-compare" :class="{'highlight-different': result2?.geolocation?.isp && result1.geolocation.isp !== result2.geolocation.isp}">
                        {{ result1.geolocation.isp }}
                      </span>
                    </div>
                  </div>
                </template>

                <div v-else class="empty-state">
                  <p>No hay resultados disponibles</p>
                </div>
              </div>

              <!-- IP 2 Info General -->
              <div class="info-card-compare">
                <div class="info-card-header">
                  <h3 class="card-subtitle">{{ query2 }}</h3>
                  <span v-if="result2FromCache" class="cache-badge">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
                    </svg>
                    Cache
                  </span>
                </div>

                <div v-if="loading2" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Cargando información...</p>
                </div>

                <template v-else-if="result2">
                  <!-- Domain Preview con Favicon (si es dominio) -->
                  <div class="domain-preview" v-if="result2.domain">
                    <div class="domain-favicon-container">
                      <img 
                        :src="`https://www.google.com/s2/favicons?domain=${result2.domain}&sz=64`" 
                        :alt="`Favicon de ${result2.domain}`"
                        class="domain-favicon"
                        @error="(e) => (e.target as HTMLImageElement).src = 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 24 24%22 fill=%22%23667eea%22%3E%3Cpath d=%22M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z%22/%3E%3C/svg%3E'"
                      />
                    </div>
                    <div class="domain-info-main">
                      <h4 class="domain-name">{{ result2.domain }}</h4>
                      <a 
                        :href="`https://${result2.domain}`" 
                        target="_blank" 
                        rel="noopener noreferrer"
                        class="domain-link"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                        </svg>
                        Visitar sitio
                      </a>
                    </div>
                  </div>

                  <!-- Info Grid -->
                  <div class="info-grid-compare">
                    <div class="info-item-compare" v-if="result2.domain">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
                        </svg>
                        Dominio
                      </span>
                      <span class="info-value-compare">{{ result2.domain }}</span>
                    </div>
                    <div class="info-item-compare">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        IP
                      </span>
                      <span class="info-value-compare" :class="{'highlight-different': result1 && result2.ip !== result1.ip}">{{ result2.ip }}</span>
                    </div>
                    <div class="info-item-compare">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                        </svg>
                        Tipo
                      </span>
                      <span class="info-value-compare info-type-badge" :class="result2.type.toLowerCase()">{{ result2.type.toUpperCase() }}</span>
                    </div>
                    <div class="info-item-compare">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        Timestamp
                      </span>
                      <span class="info-value-compare">{{ new Date(result2.timestamp).toLocaleString() }}</span>
                    </div>
                    <div class="info-item-compare" v-if="result2.geolocation?.country">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 21v-4m0 0V5a2 2 0 012-2h6.5l1 1H21l-3 6 3 6h-8.5l-1-1H5a2 2 0 00-2 2zm9-13.5V9" />
                        </svg>
                        País
                      </span>
                      <span class="info-value-compare" :class="{'highlight-different': result1?.geolocation?.country && result2.geolocation.country !== result1.geolocation.country}">
                        {{ result2.geolocation.country }}
                      </span>
                    </div>
                    <div class="info-item-compare" v-if="result2.geolocation?.isp">
                      <span class="info-label-compare">
                        <svg class="info-icon-compare" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" />
                        </svg>
                        ISP
                      </span>
                      <span class="info-value-compare" :class="{'highlight-different': result1?.geolocation?.isp && result2.geolocation.isp !== result1.geolocation.isp}">
                        {{ result2.geolocation.isp }}
                      </span>
                    </div>
                  </div>
                </template>

                <div v-else class="empty-state">
                  <p>No hay resultados disponibles</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Summary Comparison -->
          <div class="comparison-summary">
            <h2 class="comparison-title">Resumen de Comparación</h2>
            <div class="summary-grid">
              <!-- IP 1 Summary -->
              <ComparisonSummaryCard 
                :query="query1"
                :result="result1"
                :loading="loading1"
                :fromCache="result1FromCache"
                :isWinner="isWinner(1)"
                :winnerClass="getWinnerClass(1)"
              />

              <!-- IP 2 Summary -->
              <ComparisonSummaryCard 
                :query="query2"
                :result="result2"
                :loading="loading2"
                :fromCache="result2FromCache"
                :isWinner="isWinner(2)"
                :winnerClass="getWinnerClass(2)"
              />
            </div>
          </div>

          <!-- Detailed Comparison -->
          <div class="detailed-comparison">
            <h2 class="comparison-title">Comparación Detallada</h2>
            
            <!-- Services Comparison -->
            <div class="comparison-block">
              <h3 class="block-title">Comparación de Servicios Activos</h3>
              <div class="services-comparison-grid">
                <!-- IP 1 Services -->
                <div class="services-column">
                  <h4 class="column-title">
                    {{ query1 }}
                    <span v-if="result1?.services?.length" class="services-badge">
                      {{ result1.services.length }} servicios
                    </span>
                  </h4>
                  <div v-if="loading1" class="loading-state">
                    <div class="loading-spinner"></div>
                    <p>Analizando servicios...</p>
                  </div>
                  <div v-else-if="!result1" class="empty-state">
                    <p>No hay resultados disponibles</p>
                  </div>
                  <div v-else-if="result1.services && result1.services.length > 0">
                    <ServicesSection 
                      :services="result1.services" 
                      :vulnerabilities="result1.vulnerabilities || []"
                    />
                  </div>
                  <div v-else class="empty-state">
                    <p>No se detectaron servicios activos</p>
                  </div>
                </div>

                <!-- IP 2 Services -->
                <div class="services-column">
                  <h4 class="column-title">
                    {{ query2 }}
                    <span v-if="result2?.services?.length" class="services-badge">
                      {{ result2.services.length }} servicios
                    </span>
                  </h4>
                  <div v-if="loading2" class="loading-state">
                    <div class="loading-spinner"></div>
                    <p>Analizando servicios...</p>
                  </div>
                  <div v-else-if="!result2" class="empty-state">
                    <p>No hay resultados disponibles</p>
                  </div>
                  <div v-else-if="result2.services && result2.services.length > 0">
                    <ServicesSection 
                      :services="result2.services"
                      :vulnerabilities="result2.vulnerabilities || []"
                    />
                  </div>
                  <div v-else class="empty-state">
                    <p>No se detectaron servicios activos</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Reputation Comparison -->
            <ReputationComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
            />

            <!-- Maps Comparison -->
            <MapsComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
            />

            <!-- Network Information Comparison -->
            <NetworkInfoComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
            />

            <!-- Security Recommendations Comparison -->
            <RecommendationsComparison 
              :query1="query1"
              :query2="query2"
              :result1="result1"
              :result2="result2"
            />

            <!-- Vulnerabilities Comparison -->
            <div class="comparison-block" v-if="(result1?.vulnerabilities?.length || 0) > 0 || (result2?.vulnerabilities?.length || 0) > 0">
              <h3 class="block-title">Comparación de Vulnerabilidades</h3>
              <div class="vulnerabilities-comparison">
                <!-- IP 1 Vulnerabilities -->
                <div class="vuln-column">
                  <div v-if="loading1" class="loading-state">
                    <div class="loading-spinner"></div>
                    <p>Cargando vulnerabilidades...</p>
                  </div>
                  <div v-else-if="!result1" class="empty-state">
                    <p>No hay resultados disponibles</p>
                  </div>
                  <div v-else-if="result1.vulnerabilities && result1.vulnerabilities.length > 0">
                    <VulnerabilitiesSection :vulnerabilities="result1.vulnerabilities" />
                  </div>
                  <div v-else class="empty-state">
                    <p>No se detectaron vulnerabilidades</p>
                  </div>
                </div>

                <!-- IP 2 Vulnerabilities -->
                <div class="vuln-column">
                  <h4 class="column-title">
                    {{ query2 }}
                    <span v-if="result2?.vulnerabilities?.length" class="vuln-badge">
                      {{ result2.vulnerabilities.length }} CVEs
                    </span>
                  </h4>
                  <div v-if="loading2" class="loading-state">
                    <div class="loading-spinner"></div>
                    <p>Cargando vulnerabilidades...</p>
                  </div>
                  <div v-else-if="!result2" class="empty-state">
                    <p>No hay resultados disponibles</p>
                  </div>
                  <div v-else-if="result2.vulnerabilities && result2.vulnerabilities.length > 0">
                    <VulnerabilitiesSection :vulnerabilities="result2.vulnerabilities" />
                  </div>
                  <div v-else class="empty-state">
                    <p>No se detectaron vulnerabilidades</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Executive Summary Comparison -->
          <div class="executive-summary-comparison">
            <h2 class="comparison-title">Resumen Ejecutivo</h2>
            <div class="executive-comparison-grid">
              <!-- IP 1 Executive Summary -->
              <div class="executive-column">
                <h4 class="column-title">{{ query1 }}</h4>
                <div v-if="loading1" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Generando resumen ejecutivo...</p>
                </div>
                <div v-else-if="!result1" class="empty-state">
                  <p>No hay resultados disponibles</p>
                </div>
                <div v-else>
                  <ExecutiveSummary 
                    :result="result1"
                    :query="query1"
                  />
                </div>
              </div>

              <!-- IP 2 Executive Summary -->
              <div class="executive-column">
                <h4 class="column-title">{{ query2 }}</h4>
                <div v-if="loading2" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Generando resumen ejecutivo...</p>
                </div>
                <div v-else-if="!result2" class="empty-state">
                  <p>No hay resultados disponibles</p>
                </div>
                <div v-else>
                  <ExecutiveSummary 
                    :result="result2"
                    :query="query2"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="comparison-actions">
            <div class="export-options">
              <button @click="exportAsJSON" class="action-btn export-json-btn">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10" />
                </svg>
                Exportar JSON
              </button>
              <button @click="exportAsPDF" class="action-btn export-pdf-btn" :disabled="pdfExporting">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                </svg>
                <span v-if="!pdfExporting">Exportar PDF</span>
                <span v-else>Generando...</span>
              </button>
            </div>
            <button @click="clearComparison" class="action-btn clear-btn">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Limpiar Comparación
            </button>
          </div>
        </section>

        <!-- Empty State -->
        <section v-if="!hasAnyResult && !hasLoading" class="empty-section">
          <div class="empty-content">
            <div class="empty-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="currentColor">
                <path d="M11 17h2v-6h-2v6zm1-8c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1zm0-7C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
              </svg>
            </div>
            <h3 class="empty-title">Comienza una Comparación</h3>
            <p class="empty-text">
              Ingresa dos direcciones IP o dominios para compararlos lado a lado y ver sus diferencias de seguridad.
            </p>
          </div>
        </section>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import SearchBar from '@/components/SearchBar.vue'
import ComparisonSummaryCard from '@/components/ComparisonSummaryCard.vue'
import ServicesSection from '@/components/ServicesSection.vue'
import ReputationComparison from '@/components/ReputationComparison.vue'
import MapsComparison from '@/components/MapsComparison.vue'
import NetworkInfoComparison from '@/components/NetworkInfoComparison.vue'
import RecommendationsComparison from '@/components/RecommendationsComparison.vue'
import VulnerabilitiesSection from '@/components/VulnerabilitiesSection.vue'
import LiquidScoreSphere from '@/components/LiquidScoreSphere.vue'
import SecuritySummary from '@/components/SecuritySummary.vue'
import ReputationAnalysis from '@/components/ReputationAnalysis.vue'
import VulnerabilitiesSummaryCard from '@/components/VulnerabilitiesSummaryCard.vue'
import ExecutiveSummary from '@/components/ExecutiveSummary.vue'
import { analysisService, type AnalysisResult } from '@/services/api'
import { cacheService } from '@/services/cache'
import { statisticsService, type IPComparisonData } from '@/services/statistics'

// Estado del componente
const loading1 = ref(false)
const loading2 = ref(false)
const query1 = ref('')
const query2 = ref('')
const result1 = ref<AnalysisResult | null>(null)
const result2 = ref<AnalysisResult | null>(null)
const result1FromCache = ref(false)
const result2FromCache = ref(false)
const savedComparisons = ref<IPComparisonData[]>([])

// Variables para progreso simple
const realProgress1 = ref(0)
const realProgress2 = ref(0)

// Computed properties
const hasComparison = computed(() => result1.value && result2.value)
const hasAnyResult = computed(() => result1.value || result2.value)
const hasLoading = computed(() => loading1.value || loading2.value)

const isWinner = (ip: number) => {
  if (!hasComparison.value) return false
  const score1 = result1.value!.securityScore
  const score2 = result2.value!.securityScore
  
  if (score1 === score2) return false
  return ip === 1 ? score1 > score2 : score2 > score1
}

const getWinnerClass = (ip: number) => {
  if (isWinner(ip)) return 'winner'
  if (hasComparison.value && result1.value!.securityScore !== result2.value!.securityScore) {
    return 'loser'
  }
  return ''
}

const getScoreClass = (score: number) => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

// Methods
const handleSearch1 = async (searchQuery: string) => {
  await handleSearch(searchQuery, 1)
}

const handleSearch2 = async (searchQuery: string) => {
  await handleSearch(searchQuery, 2)
}

const handleSearch = async (searchQuery: string, ipNumber: number) => {
  if (!searchQuery.trim()) return

  const query = searchQuery.trim()
  
  try {
    // PRIMERO verificar caché ANTES de cambiar el estado de loading
    const cachedResult = await cacheService.get(query)
    
    if (cachedResult) {
      // Usar resultado del caché SIN mostrar loading
      if (ipNumber === 1) {
        query1.value = query
        result1.value = cachedResult
        result1FromCache.value = true
        loading1.value = false
        realProgress1.value = 100
      } else {
        query2.value = query
        result2.value = cachedResult
        result2FromCache.value = true
        loading2.value = false
        realProgress2.value = 100
      }
      
      return // SALIR TEMPRANO si hay caché
    }

    // Si NO hay caché, ENTONCES mostrar loading y hacer análisis
    // Ahora sí, resetear estado y mostrar loading
    if (ipNumber === 1) {
      query1.value = query
      loading1.value = true
      result1.value = null
      result1FromCache.value = false
      realProgress1.value = 0
    } else {
      query2.value = query
      loading2.value = true
      result2.value = null
      result2FromCache.value = false
      realProgress2.value = 0
    }

    // Progreso simple por pasos
    const progressSteps = [
      { progress: 5, delay: 500 },
      { progress: 15, delay: 2000 },
      { progress: 75, delay: 60000 },
      { progress: 90, delay: 10000 },
      { progress: 100, delay: 3000 }
    ]
    
    const progressIntervals: number[] = []
    
    progressSteps.forEach((stepData, index) => {
      setTimeout(() => {
        const isLoading = ipNumber === 1 ? loading1.value : loading2.value
        if (!isLoading) return
        
        const currentProgress = index > 0 ? progressSteps[index - 1].progress : 0
        const targetProgress = stepData.progress
        const duration = stepData.delay
        const steps = 20
        const increment = (targetProgress - currentProgress) / steps
        const intervalTime = duration / steps
        
        let tempProgress = currentProgress
        const intervalId = setInterval(() => {
          const stillLoading = ipNumber === 1 ? loading1.value : loading2.value
          if (!stillLoading || tempProgress >= targetProgress) {
            clearInterval(intervalId)
            return
          }
          tempProgress += increment
          if (ipNumber === 1) {
            realProgress1.value = Math.min(Math.round(tempProgress), targetProgress)
          } else {
            realProgress2.value = Math.min(Math.round(tempProgress), targetProgress)
          }
        }, intervalTime)
        
        progressIntervals.push(intervalId)
      }, progressSteps.slice(0, index).reduce((sum, s) => sum + s.delay, 0))
    })

    // Hacer análisis completo (solo si no hay caché)
    const response = await analysisService.analyzeIP(query)
    
    // Limpiar intervalos
    progressIntervals.forEach(id => clearInterval(id))
    
    // Asegurar que llegamos al 100%
    if (ipNumber === 1) {
      realProgress1.value = 100
    } else {
      realProgress2.value = 100
    }
    
    if (response.success && response.data) {
      // Asignar resultado según la posición
      if (ipNumber === 1) {
        result1.value = response.data
        result1FromCache.value = false
        loading1.value = false
      } else {
        result2.value = response.data
        result2FromCache.value = false
        loading2.value = false
      }
      
      // Guardar en caché
      await cacheService.set(query, response.data)
      
      // Registrar en estadísticas
      await statisticsService.recordAnalysis(query, response.data, false)
      
      console.log(`[SUCCESS] IP${ipNumber} - Análisis completado:`, query)
    } else {
      console.error(`Error en análisis IP${ipNumber}:`, response.error)
      alert(`Error analizando ${query}: ${response.error || 'Error desconocido'}`)
    }
  } catch (error: any) {
    console.error(`Error en búsqueda IP${ipNumber}:`, error)
    
    // Si hay error de conexión, intentar cargar del caché como fallback
    if (error.message && error.message.includes('fetch')) {
      console.log(`[OFFLINE] IP${ipNumber} - Backend no disponible, verificando caché como fallback...`)
      
      const cachedResult = await cacheService.get(query)
      if (cachedResult) {
        if (ipNumber === 1) {
          result1.value = cachedResult
          result1FromCache.value = true
          loading1.value = false
        } else {
          result2.value = cachedResult
          result2FromCache.value = true
          loading2.value = false
        }
        
        alert(`Modo Offline: Se cargó el último análisis de ${query} desde caché.\nEl backend no está disponible.`)
        console.log(`[FALLBACK] IP${ipNumber} - Cargado desde caché`)
        return
      }
    }
    
    alert(`Error durante el análisis de ${query}: ${error.message || error}`)
  } finally {
    // Asegurar que loading se desactive
    if (ipNumber === 1) {
      loading1.value = false
    } else {
      loading2.value = false
    }
  }
}

const loadSavedIP = async (saved: IPComparisonData, ipNumber: number) => {
  const queryRef = ipNumber === 1 ? query1 : query2
  const resultRef = ipNumber === 1 ? result1 : result2
  const cacheRef = ipNumber === 1 ? result1FromCache : result2FromCache

  queryRef.value = saved.query
  resultRef.value = saved.result
  cacheRef.value = true
}

const loadSavedComparisons = async () => {
  try {
    savedComparisons.value = await statisticsService.getComparisonData()
  } catch (error) {
    console.error('Error cargando comparaciones guardadas:', error)
  }
}

const pdfExporting = ref(false)

const exportAsJSON = async () => {
  if (!hasComparison.value) return

  try {
    const comparisonData = {
      timestamp: new Date().toISOString(),
      comparison: {
        ip1: {
          query: query1.value,
          result: result1.value,
          fromCache: result1FromCache.value
        },
        ip2: {
          query: query2.value,
          result: result2.value,
          fromCache: result2FromCache.value
        }
      },
      summary: {
        winner: isWinner(1) ? query1.value : isWinner(2) ? query2.value : 'Empate',
        scoreDifference: Math.abs(result1.value!.securityScore - result2.value!.securityScore)
      }
    }

    const dataStr = JSON.stringify(comparisonData, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement('a')
    link.href = url
    link.download = `comparacion-${query1.value}-vs-${query2.value}-${Date.now()}.json`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Error exportando comparación:', error)
    alert('Error al exportar la comparación')
  }
}

const exportAsPDF = async () => {
  if (!hasComparison.value) return
  
  pdfExporting.value = true
  try {
    // Crear datos para PDF
    const pdfData = {
      title: `Comparación: ${query1.value} vs ${query2.value}`,
      timestamp: new Date().toISOString(),
      ip1: { query: query1.value, result: result1.value },
      ip2: { query: query2.value, result: result2.value },
      summary: {
        winner: isWinner(1) ? query1.value : isWinner(2) ? query2.value : 'Empate',
        scoreDifference: Math.abs(result1.value!.securityScore - result2.value!.securityScore)
      }
    }
    
    // Aquí puedes implementar generación PDF con jsPDF o similar
    alert('Funcionalidad de exportación PDF en desarrollo')
  } catch (error) {
    console.error('Error exportando PDF:', error)
    alert('Error al exportar PDF')
  } finally {
    pdfExporting.value = false
  }
}

const exportComparison = exportAsJSON // Backward compatibility

const saveComparison = async () => {
  if (!hasComparison.value) return

  try {
    await statisticsService.saveForComparison(query1.value, result1.value!)
    await statisticsService.saveForComparison(query2.value, result2.value!)
    await loadSavedComparisons()
    alert('Comparación guardada exitosamente')
  } catch (error) {
    console.error('Error guardando comparación:', error)
    alert('Error al guardar la comparación')
  }
}

const clearComparison = () => {
  query1.value = ''
  query2.value = ''
  result1.value = null
  result2.value = null
  result1FromCache.value = false
  result2FromCache.value = false
}

// Lifecycle
onMounted(() => {
  loadSavedComparisons()
})
</script>

<style scoped>
.ip-comparator {
  min-height: 100vh;
  background: var(--bg-body);
}

.main-content {
  flex: 1;
  padding: 2rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Header */
.comparator-header {
  text-align: center;
  margin-bottom: 3rem;
}

.comparator-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.comparator-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
}

/* Search Section */
.search-section {
  margin-bottom: 3rem;
}

.search-grid {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 2rem;
  align-items: start;
}

.search-column {
  background: var(--bg-primary);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
}

.search-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.vs-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 4rem;
}

.vs-circle {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--color-primary), #3B82F6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1.2rem;
  box-shadow: var(--shadow-lg);
}

/* Saved IPs */
.saved-ips {
  margin-top: 1.5rem;
}

.saved-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.saved-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.saved-btn {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: monospace;
}

.saved-btn:hover {
  background: var(--color-primary);
  color: white;
  transform: translateY(-1px);
}

.saved-score {
  font-size: 0.8rem;
  font-weight: 600;
  opacity: 0.8;
}

/* Loading - Versión Interactiva */
.loading-section {
  margin-bottom: 3rem;
  background: var(--bg-primary);
  padding: 3rem 2rem;
  border-radius: 16px;
  box-shadow: var(--shadow-lg);
}

.loading-main-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: 2rem;
}

.loading-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
}

.loading-column {
  opacity: 0.5;
  transition: opacity 0.3s ease;
}

.loading-column.active {
  opacity: 1;
}

.loading-column-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
}

.waiting-text {
  text-align: center;
  font-size: 1.2rem;
  color: #10B981;
  font-weight: 600;
  padding: 2rem;
}

/* Radar SVG */
.radar-container {
  width: 200px;
  margin: 0 auto 2rem;
  cursor: pointer;
  text-align: center;
}

.radar-svg {
  width: 100%;
  height: auto;
  filter: drop-shadow(0 4px 12px rgba(59, 130, 246, 0.3));
}

.radar-ring {
  fill: none;
  stroke: rgba(59, 130, 246, 0.2);
  stroke-width: 1;
}

.radar-line {
  stroke: rgba(59, 130, 246, 0.1);
  stroke-width: 1;
}

.radar-beam {
  transform-origin: 100px 100px;
  animation: radar-rotate 4s linear infinite;
}

.radar-beam.speed-fast {
  animation-duration: 2s;
}

@keyframes radar-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.beam-fill {
  fill: rgba(59, 130, 246, 0.15);
}

.beam-line {
  stroke: #3B82F6;
  stroke-width: 2;
}

.radar-center {
  fill: #3B82F6;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.activity-dot {
  fill: #10B981;
  animation: dot-appear 0.5s ease-out;
}

@keyframes dot-appear {
  from {
    opacity: 0;
    transform: scale(0);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.radar-hint {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 0.5rem;
  opacity: 0.7;
}

/* Progress Bar */
.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 1.5rem auto;
  max-width: 400px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: var(--bg-secondary);
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #8B5CF6);
  border-radius: 10px;
  transition: width 0.3s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: progress-shimmer 2s infinite;
}

@keyframes progress-shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.progress-text {
  font-weight: 700;
  color: var(--text-primary);
  min-width: 45px;
  text-align: right;
}

/* Loading Steps */
.loading-steps {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-width: 450px;
  margin: 0 auto;
  position: relative;
  min-height: 300px;
}

.loading-step {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  border-left: 3px solid transparent;
  transition: all 0.3s ease;
  cursor: pointer;
  user-select: none;
  position: relative;
  will-change: transform;
  font-size: 0.9rem;
}

.loading-step:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.loading-step.pending {
  opacity: 0.5;
  border-left-color: var(--border-secondary);
}

.loading-step.current {
  opacity: 1;
  border-left-color: #3B82F6;
  background: rgba(59, 130, 246, 0.05);
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.1);
}

.loading-step.active {
  opacity: 1;
  border-left-color: #10B981;
  background: rgba(16, 185, 129, 0.05);
}

.loading-step.floating {
  position: absolute;
  z-index: 100;
  cursor: move;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
  animation: float-wobble 3s ease-in-out infinite;
}

@keyframes float-wobble {
  0%, 100% {
    filter: drop-shadow(0 8px 16px rgba(59, 130, 246, 0.3));
  }
  50% {
    filter: drop-shadow(0 16px 32px rgba(139, 92, 246, 0.4));
  }
}

.loading-step.exploded {
  animation: explode-shake 0.3s ease-out;
}

@keyframes explode-shake {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(-4px, -4px) rotate(-4deg); }
  50% { transform: translate(4px, 4px) rotate(4deg); }
  75% { transform: translate(-4px, 4px) rotate(-4deg); }
}

.particles-container {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  pointer-events: none;
  z-index: 200;
}

.particle {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  box-shadow: 0 0 8px currentColor;
}

.step-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--bg-primary);
}

.loading-step.current .step-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.loading-step.active .step-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.step-icon svg {
  width: 18px;
  height: 18px;
}

.spinner-small {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(59, 130, 246, 0.2);
  border-top-color: #3B82F6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.step-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-text {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 0.85rem;
}

.step-time {
  color: var(--text-secondary);
  font-size: 0.75rem;
  font-weight: 400;
}

.interactive-hint {
  text-align: center;
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-top: 2rem;
  padding: 1rem;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 8px;
  border: 1px dashed rgba(59, 130, 246, 0.3);
  animation: hint-pulse 2s ease-in-out infinite;
}

[data-theme="dark"] .interactive-hint {
  background: rgba(59, 130, 246, 0.08);
  border-color: rgba(59, 130, 246, 0.4);
}

@keyframes hint-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.01);
  }
}

/* Info General Comparison */
.info-general-comparison {
  margin-bottom: 3rem;
}

.info-comparison-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.info-card-compare {
  background: var(--bg-secondary);
  border-radius: 16px;
  padding: 2rem;
  border: 2px solid var(--border-color);
  transition: all 0.3s ease;
}

.info-card-compare:hover {
  border-color: rgba(59, 130, 246, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

[data-theme="dark"] .info-card-compare:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.info-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
}

.card-subtitle {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.cache-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.8rem;
  background: linear-gradient(135deg, #10B981, #059669);
  color: white;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.cache-badge svg {
  width: 14px;
  height: 14px;
}

/* Domain Preview */
.domain-preview {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-radius: 12px;
  margin-bottom: 1.5rem;
  border: 1px solid rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.domain-preview:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15), rgba(118, 75, 162, 0.15));
  border-color: rgba(102, 126, 234, 0.4);
}

.domain-favicon-container {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

[data-theme="dark"] .domain-favicon-container {
  background: var(--bg-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.domain-favicon {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.domain-info-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.domain-name {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  word-break: break-word;
}

.domain-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #3B82F6;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.domain-link:hover {
  color: #2563EB;
  gap: 0.7rem;
}

.domain-link svg {
  width: 16px;
  height: 16px;
}

/* Info Grid Compare */
.info-grid-compare {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

.info-item-compare {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem;
  background: var(--bg-primary);
  border-radius: 8px;
  transition: all 0.2s ease;
}

.info-item-compare:hover {
  background: rgba(59, 130, 246, 0.05);
  transform: translateX(4px);
}

[data-theme="dark"] .info-item-compare:hover {
  background: rgba(59, 130, 246, 0.1);
}

.info-label-compare {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

.info-icon-compare {
  width: 18px;
  height: 18px;
  color: #3B82F6;
}

.info-value-compare {
  color: var(--text-primary);
  font-size: 0.95rem;
  font-weight: 600;
  text-align: right;
  max-width: 60%;
  word-break: break-word;
}

/* Highlight different values */
.highlight-different {
  position: relative;
  padding: 0.25rem 0.75rem;
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.15), rgba(249, 115, 22, 0.15));
  border-radius: 6px;
  color: #F97316;
  font-weight: 700;
  animation: highlight-pulse 2s ease-in-out infinite;
}

[data-theme="dark"] .highlight-different {
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.2), rgba(249, 115, 22, 0.2));
  color: #FB923C;
}

@keyframes highlight-pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(249, 115, 22, 0.4);
  }
  50% {
    box-shadow: 0 0 0 6px rgba(249, 115, 22, 0);
  }
}

.info-type-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-type-badge.ipv4 {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  color: white;
}

.info-type-badge.ipv6 {
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  color: white;
}

.info-type-badge.domain {
  background: linear-gradient(135deg, #10B981, #059669);
  color: white;
}

/* Responsive */
@media (max-width: 1024px) {
  .info-comparison-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
}

/* Security Analysis Comparison */
.security-analysis-comparison {
  margin-bottom: 3rem;
}

.security-comparison-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.security-card-compare {
  background: var(--bg-secondary);
  border-radius: 16px;
  padding: 2rem;
  border: 2px solid var(--border-color);
  transition: all 0.3s ease;
}

.security-card-compare:hover {
  border-color: rgba(59, 130, 246, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

[data-theme="dark"] .security-card-compare:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.security-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
}

.security-visual-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  align-items: center;
}

.security-details-compare {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Winner Indicator */
.winner-indicator {
  margin-top: 2rem;
  padding: 1.5rem;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(5, 150, 105, 0.1));
  border-radius: 12px;
  border: 2px solid rgba(16, 185, 129, 0.3);
  animation: winner-glow 2s ease-in-out infinite;
}

[data-theme="dark"] .winner-indicator {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15), rgba(5, 150, 105, 0.15));
  border-color: rgba(16, 185, 129, 0.4);
}

@keyframes winner-glow {
  0%, 100% {
    box-shadow: 0 0 20px rgba(16, 185, 129, 0.3);
  }
  50% {
    box-shadow: 0 0 30px rgba(16, 185, 129, 0.5);
  }
}

.winner-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.winner-icon {
  width: 32px;
  height: 32px;
  color: #10B981;
  flex-shrink: 0;
}

.equal-icon {
  width: 32px;
  height: 32px;
  color: #6B7280;
  flex-shrink: 0;
}

.winner-text {
  font-size: 1.1rem;
  color: var(--text-primary);
  font-weight: 500;
}

.winner-text strong {
  color: #10B981;
  font-weight: 700;
}

.equal-text {
  font-size: 1.1rem;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Responsive */
@media (max-width: 1024px) {
  .security-comparison-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .winner-content {
    flex-direction: column;
    text-align: center;
  }
}

/* Executive Summary Comparison */
.executive-summary-comparison {
  margin-bottom: 3rem;
  margin-top: 3rem;
}

.executive-comparison-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2.5rem;
}

.executive-card-compare {
  background: var(--bg-secondary);
  border-radius: 16px;
  padding: 2rem;
  border: 2px solid var(--border-color);
  transition: all 0.3s ease;
}

.executive-card-compare:hover {
  border-color: rgba(59, 130, 246, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

[data-theme="dark"] .executive-card-compare:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.executive-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
}

/* Ajustes para ExecutiveSummary dentro del comparador */
.executive-card-compare :deep(.executive-summary) {
  background: transparent;
  padding: 0;
  border: none;
  box-shadow: none;
  margin: 0;
}

.executive-card-compare :deep(.executive-header) {
  padding: 1.5rem;
  background: var(--bg-primary);
  border-radius: 12px;
  margin-bottom: 1.5rem;
  border: 1px solid var(--border-color);
}

.executive-card-compare :deep(.executive-grid) {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  width: 100%;
}

.executive-card-compare :deep(.executive-card) {
  padding: 1.5rem;
  background: var(--bg-primary);
  border-radius: 12px;
  border: 2px solid var(--border-color);
  transition: all 0.3s ease;
  width: 100%;
  box-sizing: border-box;
  overflow: visible;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.executive-card-compare :deep(.executive-card:hover) {
  border-color: rgba(59, 130, 246, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

[data-theme="dark"] .executive-card-compare :deep(.executive-card:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.executive-card-compare :deep(.card-icon) {
  width: 42px;
  height: 42px;
  margin-bottom: 0.5rem;
  flex-shrink: 0;
}

.executive-card-compare :deep(.card-label) {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.executive-card-compare :deep(.card-value) {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  word-wrap: break-word;
  overflow-wrap: break-word;
  hyphens: auto;
  line-height: 1.3;
  width: 100%;
}

.executive-card-compare :deep(.card-description) {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.6;
  word-wrap: break-word;
  overflow-wrap: break-word;
  width: 100%;
}

.executive-card-compare :deep(.services-list) {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
  width: 100%;
}

.executive-card-compare :deep(.service-badge) {
  padding: 0.35rem 0.7rem;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(139, 92, 246, 0.15));
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.executive-card-compare :deep(.status-badge) {
  display: inline-block;
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.executive-column :deep(.status-badge.excelente),
.executive-column :deep(.status-badge.bueno) {
  background: linear-gradient(135deg, #10B981, #059669);
  color: white;
}

.executive-column :deep(.status-badge.aceptable) {
  background: linear-gradient(135deg, #F59E0B, #D97706);
  color: white;
}

.executive-column :deep(.status-badge.critico),
.executive-column :deep(.status-badge.alto) {
  background: linear-gradient(135deg, #EF4444, #DC2626);
  color: white;
}

/* Servicios Comparison Styles */
.services-comparison-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.services-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* Ocultar el encabezado duplicado del ServicesSection en comparador */
.services-column :deep(.services-card .card-header) {
  display: none;
}

.services-column :deep(.services-card) {
  padding-top: 0;
}

.services-column :deep(.card-content) {
  padding-top: 0;
}

.services-column .column-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid var(--border-color);
}

.services-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.4rem 0.9rem;
  background: linear-gradient(135deg, #3B82F6, #8B5CF6);
  color: white;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 1024px) {
  .executive-comparison-grid,
  .services-comparison-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
}

/* Comparison Summary */
.comparison-summary {
  margin-bottom: 3rem;
}

.comparison-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.summary-card {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  min-height: 250px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.loading-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-color);
  border-top: 3px solid #3B82F6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

.empty-card {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-secondary);
  font-style: italic;
}

.empty-card p {
  margin: 0;
}

.summary-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-card.winner {
  border-color: #10B981;
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.2);
}

.summary-card.loser {
  opacity: 0.8;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.summary-ip {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  font-family: monospace;
}

.summary-badges {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.winner-badge {
  background: linear-gradient(135deg, #10B981, #059669);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.cache-badge {
  background: rgba(34, 197, 94, 0.1);
  color: #15803d;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.summary-score {
  text-align: center;
  margin-bottom: 1.5rem;
}

.score-number {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.score-number.score-high { color: #10B981; }
.score-number.score-medium { color: #F59E0B; }
.score-number.score-low { color: #EF4444; }

.score-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.summary-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-weight: 600;
  color: var(--text-secondary);
}

.detail-value {
  color: var(--text-primary);
  font-weight: 500;
}

/* Detailed Comparison */
.detailed-comparison {
  margin-bottom: 3rem;
}

.comparison-block {
  background: var(--bg-primary);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-primary);
  margin-bottom: 2rem;
}

.block-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.services-comparison,
.maps-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.services-column,
.map-column {
  border-right: 1px solid var(--border-primary);
  padding-right: 1rem;
}

.services-column:last-child,
.map-column:last-child {
  border-right: none;
  padding-right: 0;
  padding-left: 1rem;
}

.column-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
  font-family: monospace;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.vuln-badge {
  background: #dc2626;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Vulnerabilities Comparison */
.vulnerabilities-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.vuln-column {
  border-right: 1px solid var(--border-primary);
  padding-right: 1rem;
}

/* Ocultar el encabezado duplicado del VulnerabilitiesSection en comparador */
.vuln-column :deep(.result-card .card-header) {
  display: none;
}

.vuln-column :deep(.result-card) {
  padding-top: 0;
  background: transparent;
  border: none;
  box-shadow: none;
}

.vuln-column :deep(.card-content) {
  padding: 0;
}

.vuln-column:last-child {
  border-right: none;
  padding-right: 0;
  padding-left: 1rem;
}

/* Services */
.services-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.service-item {
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 8px;
  border-left: 4px solid var(--border-primary);
}

.service-item.low { border-left-color: #10B981; }
.service-item.medium { border-left-color: #F59E0B; }
.service-item.high { border-left-color: #EF4444; }

.service-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.service-port {
  font-family: monospace;
  font-weight: 700;
  color: var(--color-primary);
}

.service-name {
  font-weight: 600;
  color: var(--text-primary);
}

.service-details {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

/* Loading and Empty States */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  gap: 1rem;
  text-align: center;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.loading-state p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
  background: var(--bg-secondary);
  border-radius: 8px;
  color: var(--text-secondary);
  font-style: italic;
}

.empty-state p {
  margin: 0;
}

/* Actions */
.comparison-actions {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-top: 2rem;
}

.export-options {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  min-width: 160px;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-btn svg {
  width: 20px;
  height: 20px;
}

.export-json-btn {
  background: #3B82F6;
  color: white;
}

.export-json-btn:hover:not(:disabled) {
  background: #2563EB;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.export-pdf-btn {
  background: #EF4444;
  color: white;
}

.export-pdf-btn:hover:not(:disabled) {
  background: #DC2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

.clear-btn {
  background: #6B7280;
  color: white;
  align-self: center;
}

.clear-btn:hover {
  background: #4B5563;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.4);
}

.save-btn {
  background: #10B981;
  color: white;
}

.save-btn:hover {
  background: #059669;
  transform: translateY(-2px);
}

.clear-btn {
  background: #EF4444;
  color: white;
}

.clear-btn:hover {
  background: #DC2626;
  transform: translateY(-2px);
}

/* Empty State */
.empty-section {
  text-align: center;
  padding: 4rem;
}

.empty-content {
  max-width: 500px;
  margin: 0 auto;
}

.empty-icon {
  margin-bottom: 1rem;
  color: var(--text-tertiary);
  opacity: 0.5;
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.empty-text {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Responsive */
@media (max-width: 1024px) {
  .search-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .vs-separator {
    padding: 1rem 0;
  }
  
  .network-comparison,
  .recommendations-comparison {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
}

/* Animations */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 1024px) {
  .search-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .vs-separator {
    padding: 1rem 0;
  }
  
  .network-comparison,
  .recommendations-comparison {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .vs-circle {
    transform: rotate(90deg);
  }
  
  .summary-grid,
  .services-comparison,
  .reputation-comparison,
  .maps-comparison,
  .vulnerabilities-comparison {
    grid-template-columns: 1fr;
  }
  
  .services-column,
  .reputation-column,
  .map-column,
  .vuln-column {
    border-right: none;
    padding-right: 0;
    padding-left: 0;
    border-bottom: 1px solid var(--border-primary);
    padding-bottom: 1rem;
    margin-bottom: 1rem;
  }
  
  .services-column:last-child,
  .reputation-column:last-child,
  .map-column:last-child,
  .vuln-column:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
}

@media (max-width: 768px) {
  .comparator-title {
    font-size: 2rem;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
