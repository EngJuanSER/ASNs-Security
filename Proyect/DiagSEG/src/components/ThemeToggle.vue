<template>
  <div class="theme-toggle">
    <button 
      @click="cycleTheme"
      class="theme-button"
      :title="`Cambiar a ${getThemeLabel(getNextTheme())}`"
    >
      <span class="theme-icon">{{ getThemeIcon(currentTheme) }}</span>
      <span v-if="showLabel" class="theme-label">{{ getThemeLabel(currentTheme) }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

interface Props {
  showLabel?: boolean
}

withDefaults(defineProps<Props>(), {
  showLabel: false
})

const { 
  currentTheme, 
  cycleTheme, 
  getThemeLabel, 
  getThemeIcon, 
  getNextTheme 
} = useTheme()
</script>

<style scoped>
.theme-toggle {
  display: inline-block;
}

.theme-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--theme-toggle-bg, rgba(255, 255, 255, 0.1));
  border: 1px solid var(--theme-toggle-border, rgba(255, 255, 255, 0.2));
  color: var(--theme-toggle-color, inherit);
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.theme-button:hover {
  background: var(--theme-toggle-hover-bg, rgba(255, 255, 255, 0.2));
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.theme-icon {
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 1rem;
}

.theme-label {
  font-weight: 500;
  white-space: nowrap;
}

/* Dark theme styles */
:global(.dark) .theme-button {
  --theme-toggle-bg: rgba(0, 0, 0, 0.3);
  --theme-toggle-border: rgba(255, 255, 255, 0.1);
  --theme-toggle-hover-bg: rgba(0, 0, 0, 0.5);
}

/* Light theme in dark contexts */
.theme-button {
  --theme-toggle-bg: rgba(255, 255, 255, 0.1);
  --theme-toggle-border: rgba(255, 255, 255, 0.2);
  --theme-toggle-hover-bg: rgba(255, 255, 255, 0.2);
}

/* Responsive */
@media (max-width: 768px) {
  .theme-button {
    padding: 0.4rem 0.6rem;
    font-size: 0.85rem;
  }
  
  .theme-icon {
    font-size: 0.9rem;
  }
}
</style>
