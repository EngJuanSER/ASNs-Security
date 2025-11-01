<template>
  <div class="comparison-block">
    <h3 class="block-title">Geolocalización</h3>
    <div class="maps-comparison">
      <div class="map-column">
        <h4 class="column-title">{{ query1 }}</h4>
        <div class="map-container">
          <GeolocationMap 
            v-if="result1"
            :geolocation="result1.geolocation"
            :loading="false"
            :error="''"
          />
        </div>
      </div>
      
      <div class="map-column">
        <h4 class="column-title">{{ query2 }}</h4>
        <div class="map-container">
          <GeolocationMap 
            v-if="result2"
            :geolocation="result2.geolocation"
            :loading="false"
            :error="''"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import GeolocationMap from './GeolocationMap.vue'
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

.maps-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.map-column {
  min-height: 400px;
}

.column-title {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-primary);
  font-family: 'Courier New', monospace;
}

.map-container {
  width: 100%;
  height: 350px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border-primary);
}

@media (max-width: 968px) {
  .maps-comparison {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .map-column {
    min-height: 350px;
  }
}
</style>
