<template>
  <div class="liquid-sphere-wrapper" @click="onSphereClick" :class="{ 'is-wobbling': isWobbling }">
    <svg class="liquid-sphere" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <clipPath id="sphereClip">
          <circle cx="100" cy="100" r="90" />
        </clipPath>
        <linearGradient id="liquidGradient" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop :offset="'0%'" :stop-color="getLiquidColor(score, 0.8)" />
          <stop :offset="'100%'" :stop-color="getLiquidColor(score, 1)" />
        </linearGradient>
      </defs>
      
      <!-- Círculo exterior (contenedor) -->
      <circle 
        cx="100" 
        cy="100" 
        r="90" 
        fill="none" 
        :stroke="getLiquidColor(score, 0.3)"
        stroke-width="3"
      />
      
      <!-- Líquido animado -->
      <g clip-path="url(#sphereClip)">
        <rect 
          :key="score"
          x="0" 
          :y="liquidY"
          width="200" 
          height="200" 
          fill="url(#liquidGradient)"
          class="liquid-fill"
          :class="{ 'wobbling': isWobbling }"
        >
          <animate 
            attributeName="y" 
            :from="200"
            :to="liquidY"
            dur="2s" 
            fill="freeze"
            calcMode="spline"
            keySplines="0.4 0.0 0.2 1"
          />
        </rect>
        
        <!-- Ondas del líquido con física mejorada -->
        <path 
          :d="`M 0 ${liquidY} Q 50 ${liquidY - 8} 100 ${liquidY} T 200 ${liquidY} V 200 H 0 Z`"
          fill="url(#liquidGradient)"
          opacity="0.5"
          class="liquid-wave wave-1"
          :class="{ 'wobbling': isWobbling }"
        />
        <path 
          :d="`M 0 ${liquidY + 3} Q 50 ${liquidY - 5} 100 ${liquidY + 3} T 200 ${liquidY + 3} V 200 H 0 Z`"
          fill="url(#liquidGradient)"
          opacity="0.3"
          class="liquid-wave wave-2"
          :class="{ 'wobbling': isWobbling }"
        />
        <path 
          :d="`M 0 ${liquidY + 6} Q 50 ${liquidY + 2} 100 ${liquidY + 6} T 200 ${liquidY + 6} V 200 H 0 Z`"
          fill="url(#liquidGradient)"
          opacity="0.2"
          class="liquid-wave wave-3"
          :class="{ 'wobbling': isWobbling }"
        />
      </g>
      
      <!-- Texto del score -->
      <text 
        :key="'text-' + score"
        x="100" 
        y="95" 
        text-anchor="middle" 
        font-size="42" 
        font-weight="bold"
        class="score-text"
      >
        {{ score }}
      </text>
      <text 
        x="100" 
        y="115" 
        text-anchor="middle" 
        font-size="16" 
        font-weight="500"
        opacity="0.8"
        class="score-text-small"
      >
        / 100
      </text>
    </svg>
    
    <div class="security-status-label" :class="getScoreClass(score)">
      {{ getSecurityStatusText(score) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  score: number
}

const props = defineProps<Props>()
const isWobbling = ref(false)

// Cálculo preciso del nivel de líquido
// score 0 = y:200 (vacío), score 100 = y:10 (lleno casi completo)
const liquidY = computed(() => {
  // Mapeo lineal: 0 -> 200, 100 -> 10
  // Fórmula: y = 200 - (score * 1.9)
  // Esto deja un margen superior de 10px incluso al 100%
  return 200 - (props.score * 1.9)
})

const onSphereClick = () => {
  isWobbling.value = true
  setTimeout(() => {
    isWobbling.value = false
  }, 1000)
}

const getLiquidColor = (score: number, opacity: number = 1) => {
  let r, g, b
  
  if (score >= 80) {
    r = 16; g = 185; b = 129 // Verde #10B981
  } else if (score >= 60) {
    r = 245; g = 158; b = 11 // Amarillo #F59E0B
  } else if (score >= 40) {
    r = 239; g = 68; b = 68 // Naranja/Rojo #EF4444
  } else {
    r = 153; g = 27; b = 27 // Rojo oscuro #991B1B
  }
  
  return `rgba(${r}, ${g}, ${b}, ${opacity})`
}

const getScoreClass = (score: number) => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

const getSecurityStatusText = (score: number) => {
  if (score >= 90) return 'Excelente Seguridad'
  if (score >= 80) return 'Buena Seguridad'
  if (score >= 70) return 'Seguridad Aceptable'
  if (score >= 60) return 'Seguridad Media'
  if (score >= 40) return 'Seguridad Baja'
  return 'Seguridad Crítica'
}
</script>

<style scoped>
.liquid-sphere-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.liquid-sphere-wrapper:hover {
  transform: scale(1.05);
}

.liquid-sphere-wrapper:active {
  transform: scale(0.98);
}

.liquid-sphere-wrapper.is-wobbling {
  animation: wobble 1s ease-in-out;
}

@keyframes wobble {
  0%, 100% { transform: translateX(0) rotate(0deg); }
  15% { transform: translateX(-10px) rotate(-5deg); }
  30% { transform: translateX(10px) rotate(5deg); }
  45% { transform: translateX(-8px) rotate(-3deg); }
  60% { transform: translateX(8px) rotate(3deg); }
  75% { transform: translateX(-4px) rotate(-1deg); }
  90% { transform: translateX(4px) rotate(1deg); }
}

.liquid-sphere {
  width: 250px;
  height: 250px;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
}

.liquid-fill {
  transition: y 0.3s ease-out;
}

.liquid-fill.wobbling {
  animation: liquidShake 1s ease-in-out;
}

@keyframes liquidShake {
  0%, 100% { transform: translateY(0) scaleY(1); }
  10% { transform: translateY(-8px) scaleY(0.95); }
  20% { transform: translateY(6px) scaleY(1.05); }
  30% { transform: translateY(-5px) scaleY(0.97); }
  40% { transform: translateY(4px) scaleY(1.03); }
  50% { transform: translateY(-3px) scaleY(0.98); }
  60% { transform: translateY(2px) scaleY(1.02); }
  70% { transform: translateY(-2px) scaleY(0.99); }
  80% { transform: translateY(1px) scaleY(1.01); }
  90% { transform: translateY(-1px) scaleY(1); }
}

.liquid-wave {
  animation: wave 3s ease-in-out infinite;
}

.wave-1 {
  animation: wave 3s ease-in-out infinite;
}

.wave-2 {
  animation: wave 4s ease-in-out infinite reverse;
}

.wave-3 {
  animation: wave 3.5s ease-in-out infinite;
  animation-delay: 0.5s;
}

.liquid-wave.wobbling {
  animation: waveWobble 1s ease-in-out, wave 3s ease-in-out infinite;
}

.wave-2.wobbling {
  animation: waveWobble 1s ease-in-out, wave 4s ease-in-out infinite reverse;
}

.wave-3.wobbling {
  animation: waveWobble 1s ease-in-out 0.2s, wave 3.5s ease-in-out infinite;
}

@keyframes wave {
  0%, 100% { 
    transform: translateX(0) translateY(0);
  }
  25% { 
    transform: translateX(-5px) translateY(-2px);
  }
  50% { 
    transform: translateX(-10px) translateY(0);
  }
  75% { 
    transform: translateX(-5px) translateY(2px);
  }
}

@keyframes waveWobble {
  0%, 100% { 
    transform: translateX(0) translateY(0) scaleX(1);
  }
  10% { 
    transform: translateX(-15px) translateY(-10px) scaleX(1.1);
  }
  20% { 
    transform: translateX(12px) translateY(8px) scaleX(0.95);
  }
  30% { 
    transform: translateX(-10px) translateY(-6px) scaleX(1.05);
  }
  40% { 
    transform: translateX(8px) translateY(5px) scaleX(0.98);
  }
  50% { 
    transform: translateX(-6px) translateY(-4px) scaleX(1.02);
  }
  60% { 
    transform: translateX(5px) translateY(3px) scaleX(0.99);
  }
  70% { 
    transform: translateX(-4px) translateY(-2px) scaleX(1.01);
  }
  80% { 
    transform: translateX(3px) translateY(2px) scaleX(1);
  }
  90% { 
    transform: translateX(-2px) translateY(-1px) scaleX(1);
  }
}

.score-text {
  animation: fadeIn 0.8s ease-in-out;
  pointer-events: none;
  /* Texto oscuro en modo claro para buen contraste */
  fill: #1f2937;
}

.score-text-small {
  /* Texto oscuro en modo claro */
  fill: #374151;
}

/* Modo oscuro - texto claro para contraste */
[data-theme="dark"] .score-text {
  fill: #f9fafb;
}

[data-theme="dark"] .score-text-small {
  fill: #f3f4f6;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.8); }
  to { opacity: 1; transform: scale(1); }
}

.security-status-label {
  padding: 0.75rem 1.5rem;
  border-radius: 24px;
  font-weight: 700;
  font-size: 1.1rem;
  text-align: center;
  min-width: 200px;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.liquid-sphere-wrapper:hover .security-status-label {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.security-status-label.score-high {
  background: linear-gradient(135deg, #D1FAE5, #A7F3D0);
  color: #065F46;
}

.security-status-label.score-medium {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  color: #92400E;
}

.security-status-label.score-low {
  background: linear-gradient(135deg, #FEE2E2, #FECACA);
  color: #991B1B;
}

@media (max-width: 768px) {
  .liquid-sphere {
    width: 200px;
    height: 200px;
  }
}
</style>
