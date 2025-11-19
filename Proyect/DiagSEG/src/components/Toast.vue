<template>
  <Transition name="toast">
    <div v-if="visible" class="toast-container" :class="[typeClass, positionClass]">
      <div class="toast-content">
        <div class="toast-icon">
          <component :is="iconComponent" />
        </div>
        <div class="toast-body">
          <h4 class="toast-title">{{ title }}</h4>
          <p class="toast-message">{{ message }}</p>
          <div v-if="action" class="toast-action">
            <svg class="action-icon" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
            </svg>
            <span>{{ action }}</span>
          </div>
        </div>
        <button class="toast-close" @click="close" aria-label="Cerrar">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
          </svg>
        </button>
      </div>
      <div class="toast-progress" :style="{ width: progressWidth + '%' }"></div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, h } from 'vue'

export interface ToastProps {
  title: string
  message: string
  action?: string
  type?: 'success' | 'error' | 'warning' | 'info'
  duration?: number
  position?: 'top-right' | 'top-center' | 'bottom-right' | 'bottom-center'
}

const props = withDefaults(defineProps<ToastProps>(), {
  type: 'info',
  duration: 5000,
  position: 'top-right'
})

const emit = defineEmits<{
  close: []
}>()

const visible = ref(false)
const progressWidth = ref(100)
let timer: number | undefined
let progressTimer: number | undefined

const typeClass = computed(() => `toast-${props.type}`)
const positionClass = computed(() => `toast-${props.position}`)

const iconComponent = computed(() => {
  const icons = {
    success: () => h('svg', { width: 24, height: 24, viewBox: '0 0 24 24', fill: 'currentColor' }, [
      h('path', { d: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z' })
    ]),
    error: () => h('svg', { width: 24, height: 24, viewBox: '0 0 24 24', fill: 'currentColor' }, [
      h('path', { d: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z' })
    ]),
    warning: () => h('svg', { width: 24, height: 24, viewBox: '0 0 24 24', fill: 'currentColor' }, [
      h('path', { d: 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z' })
    ]),
    info: () => h('svg', { width: 24, height: 24, viewBox: '0 0 24 24', fill: 'currentColor' }, [
      h('path', { d: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z' })
    ])
  }
  return icons[props.type]
})

const close = () => {
  visible.value = false
  if (timer) clearTimeout(timer)
  if (progressTimer) clearInterval(progressTimer)
  setTimeout(() => emit('close'), 300)
}

const startProgress = () => {
  const interval = 50
  const steps = props.duration / interval
  const decrement = 100 / steps

  progressTimer = window.setInterval(() => {
    progressWidth.value -= decrement
    if (progressWidth.value <= 0) {
      if (progressTimer) clearInterval(progressTimer)
    }
  }, interval)
}

onMounted(() => {
  visible.value = true
  startProgress()
  
  if (props.duration > 0) {
    timer = window.setTimeout(() => {
      close()
    }, props.duration)
  }
})

onUnmounted(() => {
  if (timer) clearTimeout(timer)
  if (progressTimer) clearInterval(progressTimer)
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  z-index: 10000;
  min-width: 320px;
  max-width: 500px;
  background: var(--bg-primary);
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  border-left: 4px solid;
}

/* Positions */
.toast-top-right {
  top: 20px;
  right: 20px;
}

.toast-top-center {
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
}

.toast-bottom-right {
  bottom: 20px;
  right: 20px;
}

.toast-bottom-center {
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
}

/* Types */
.toast-success {
  border-left-color: #10b981;
}

.toast-error {
  border-left-color: #ef4444;
}

.toast-warning {
  border-left-color: #f59e0b;
}

.toast-info {
  border-left-color: #3b82f6;
}

.toast-content {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.25rem;
  align-items: flex-start;
}

.toast-icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
}

.toast-success .toast-icon {
  color: #10b981;
}

.toast-error .toast-icon {
  color: #ef4444;
}

.toast-warning .toast-icon {
  color: #f59e0b;
}

.toast-info .toast-icon {
  color: #3b82f6;
}

.toast-body {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.25rem 0;
}

.toast-message {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
}

.toast-action {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-muted);
  margin: 0.5rem 0 0 0;
  line-height: 1.4;
  padding: 0.5rem;
  background: var(--bg-secondary);
  border-radius: 6px;
}

.toast-action .action-icon {
  flex-shrink: 0;
  opacity: 0.7;
}

.toast-action span {
  flex: 1;
}

.toast-close {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.toast-close:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.toast-progress {
  height: 3px;
  background: currentColor;
  transition: width 50ms linear;
}

.toast-success .toast-progress {
  color: #10b981;
}

.toast-error .toast-progress {
  color: #ef4444;
}

.toast-warning .toast-progress {
  color: #f59e0b;
}

.toast-info .toast-progress {
  color: #3b82f6;
}

/* Animations */
.toast-enter-active {
  animation: slideIn 0.3s ease-out;
}

.toast-leave-active {
  animation: slideOut 0.3s ease-in;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}

.toast-top-center.toast-enter-active,
.toast-bottom-center.toast-enter-active {
  animation: slideInCenter 0.3s ease-out;
}

.toast-top-center.toast-leave-active,
.toast-bottom-center.toast-leave-active {
  animation: slideOutCenter 0.3s ease-in;
}

@keyframes slideInCenter {
  from {
    transform: translate(-50%, -20px);
    opacity: 0;
  }
  to {
    transform: translate(-50%, 0);
    opacity: 1;
  }
}

@keyframes slideOutCenter {
  from {
    transform: translate(-50%, 0);
    opacity: 1;
  }
  to {
    transform: translate(-50%, -20px);
    opacity: 0;
  }
}

/* Responsive */
@media (max-width: 640px) {
  .toast-container {
    min-width: 280px;
    max-width: calc(100vw - 40px);
    left: 20px !important;
    right: 20px !important;
    transform: none !important;
  }
  
  .toast-top-right, .toast-top-center {
    top: 10px;
  }
  
  .toast-bottom-right, .toast-bottom-center {
    bottom: 10px;
  }
}
</style>
