<template>
  <div class="map-container">
    <div class="map-header">
      <h4 class="map-title">Ubicación Geográfica</h4>
      <div class="map-info" v-if="geolocation">
        <span class="location-text">
          {{ geolocation.city }}, {{ geolocation.region }}, {{ geolocation.country }}
        </span>
      </div>
    </div>
    
    <div ref="mapContainer" class="map" :class="{ 'map-loading': loading }">
      <div v-if="loading" class="map-loading-overlay">
        <div class="loading-spinner"></div>
        <p>Cargando mapa...</p>
      </div>
      
      <div v-else-if="error" class="map-error">
        <div class="error-icon">!</div>
        <p>{{ error }}</p>
      </div>
    </div>
    
    <div class="map-details" v-if="geolocation && !loading">
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">ISP:</span>
          <span class="detail-value">{{ geolocation.isp }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">ASN:</span>
          <span class="detail-value">{{ geolocation.asn }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Zona Horaria:</span>
          <span class="detail-value">{{ geolocation.timezone }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Coordenadas:</span>
          <span class="detail-value">{{ geolocation.latitude.toFixed(4) }}, {{ geolocation.longitude.toFixed(4) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import type { GeolocationData } from '@/services/api'

// Leaflet import
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Fix for default markers in Leaflet with Vite
delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
})

interface Props {
  geolocation?: GeolocationData
  loading?: boolean
  error?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  error: ''
})

const mapContainer = ref<HTMLElement>()
let map: L.Map | null = null
let marker: L.Marker | null = null

const initializeMap = () => {
  if (!mapContainer.value || !props.geolocation) return

  try {
    // Create map
    map = L.map(mapContainer.value).setView(
      [props.geolocation.latitude, props.geolocation.longitude], 
      10
    )

    // Add tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(map)

    // Create custom marker content
    const markerContent = `
      <div class="custom-marker">
        <div class="marker-content">
          <h5>${props.geolocation.city}</h5>
          <p>${props.geolocation.region}, ${props.geolocation.country}</p>
          <small>ISP: ${props.geolocation.isp}</small>
        </div>
      </div>
    `

    // Add marker
    marker = L.marker([props.geolocation.latitude, props.geolocation.longitude])
      .addTo(map)
      .bindPopup(markerContent)
      .openPopup()

    // Add circle to show approximate area
    L.circle([props.geolocation.latitude, props.geolocation.longitude], {
      color: '#667eea',
      fillColor: '#667eea',
      fillOpacity: 0.1,
      radius: 50000 // 50km radius
    }).addTo(map)

  } catch (error) {
    console.error('Error initializing map:', error)
  }
}

const updateMap = () => {
  if (!map || !props.geolocation) return

  // Update map view
  map.setView([props.geolocation.latitude, props.geolocation.longitude], 10)

  // Remove existing marker
  if (marker) {
    map.removeLayer(marker)
  }

  // Add new marker
  const markerContent = `
    <div class="custom-marker">
      <div class="marker-content">
        <h5>${props.geolocation.city}</h5>
        <p>${props.geolocation.region}, ${props.geolocation.country}</p>
        <small>ISP: ${props.geolocation.isp}</small>
      </div>
    </div>
  `

  marker = L.marker([props.geolocation.latitude, props.geolocation.longitude])
    .addTo(map)
    .bindPopup(markerContent)
    .openPopup()
}

// Watch for changes in geolocation
watch(() => props.geolocation, (newLocation) => {
  if (newLocation && !props.loading) {
    if (map) {
      updateMap()
    } else {
      // Small delay to ensure DOM is ready
      setTimeout(initializeMap, 100)
    }
  }
}, { immediate: true })

onMounted(() => {
  if (props.geolocation && !props.loading) {
    setTimeout(initializeMap, 100)
  }
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})
</script>

<style scoped>
.map-container {
  background: var(--bg-primary);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-primary);
}

.map-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-primary);
  background: var(--bg-secondary);
}

.map-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
}

.map-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.location-text {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.map {
  height: 300px;
  position: relative;
  background: var(--bg-secondary);
}

.map-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-primary);
  border-top: 3px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.map-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
}

.error-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.map-details {
  padding: 1rem 1.5rem;
  background: var(--bg-secondary);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.75rem;
  background: var(--bg-primary);
  border-radius: 6px;
  border: 1px solid var(--border-primary);
}

.detail-label {
  font-weight: 500;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.detail-value {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.85rem;
  text-align: right;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Mobile responsive */
@media (max-width: 768px) {
  .map {
    height: 250px;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .map-header {
    padding: 0.75rem 1rem;
  }
  
  .map-details {
    padding: 0.75rem 1rem;
  }
}
</style>

<style>
/* Global styles for Leaflet popup */
.custom-marker .marker-content h5 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
}

.custom-marker .marker-content p {
  margin: 0 0 0.25rem 0;
  font-size: 0.9rem;
}

.custom-marker .marker-content small {
  font-size: 0.8rem;
  color: #666;
}

.leaflet-popup-content-wrapper {
  border-radius: 8px;
}

.leaflet-popup-content {
  margin: 12px 16px;
}
</style>
