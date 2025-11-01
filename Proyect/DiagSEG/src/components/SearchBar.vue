<template>
  <div class="search-bar">
    <div class="search-container">
      <div class="search-input-wrapper">
        <input
          v-model="searchQuery"
          type="text"
          class="search-input"
          :class="{ 'input-valid': validationState.isValid && searchQuery.length > 0, 'input-invalid': !validationState.isValid && searchQuery.length > 0 }"
          :placeholder="dynamicPlaceholder"
          @keyup.enter="handleSearch"
          @input="validateInput"
          :disabled="loading"
        />
        <button
          class="search-button"
          @click="handleSearch"
          :disabled="loading || !validationState.isValid || !searchQuery.trim()"
          :class="{ 'button-valid': validationState.isValid && searchQuery.length > 0 }"
        >
          {{ loading ? 'Analizando...' : 'Analizar' }}
        </button>
      </div>
      
      <!-- Validation feedback -->
      <div v-if="searchQuery.length > 0 && validationState.message" class="validation-message" :class="{ 'validation-success': validationState.isValid, 'validation-error': !validationState.isValid }">
        {{ validationState.message }}
      </div>
      
      <!-- Error message -->
      <div v-if="errorProp || error" class="error-message">
        {{ errorProp || error }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { validateInput as validateInputUtil, detectInputType, getPlaceholderText } from '@/utils/validation'

interface Props {
  loading?: boolean
  error?: string
  value?: string
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: 'Ingresa una IP o dominio (ej: 8.8.8.8)'
})

const emit = defineEmits<{
  search: [query: string]
}>()

// Definir función expuesta para componentes padre
defineExpose({
  setQuery: (query: string) => {
    searchQuery.value = query
    validateInput()
  }
})

const router = useRouter()
const searchQuery = ref(props.value || '')
const error = ref('')
const errorProp = ref(props.error || '')
const validationState = ref({ isValid: true, message: '', type: 'unknown' as 'ip' | 'domain' | 'unknown' })

// Placeholder dinámico basado en el tipo de input detectado
const dynamicPlaceholder = computed(() => {
  if (props.placeholder !== 'Ingresa una IP o dominio (ej: 8.8.8.8)') {
    return props.placeholder
  }
  
  if (searchQuery.value.length === 0) {
    return props.placeholder
  }
  
  const inputType = detectInputType(searchQuery.value)
  return getPlaceholderText(inputType)
})

// Watch for changes in props
watch(() => props.value, (newValue) => {
  if (newValue !== undefined) {
    searchQuery.value = newValue
    validateInput()
  }
})

watch(() => props.error, (newError) => {
  errorProp.value = newError || ''
})

const validateInput = () => {
  if (searchQuery.value.length === 0) {
    validationState.value = { isValid: true, message: '', type: 'unknown' }
    return
  }
  
  const result = validateInputUtil(searchQuery.value)
  validationState.value = {
    isValid: result.isValid,
    message: result.message || '',
    type: result.type || 'unknown'
  }
}

const handleSearch = () => {
  error.value = ''
  const query = searchQuery.value.trim()
  
  if (!query) {
    error.value = 'Por favor ingresa una IP o dominio'
    return
  }
  
  const validation = validateInputUtil(query)
  if (!validation.isValid) {
    error.value = validation.message || 'Formato no válido'
    return
  }
  
  emit('search', query)
  
  // Si estamos en la página de inicio, navegar a análisis
  if (router.currentRoute.value.name === 'Home') {
    router.push({ name: 'Analysis', query: { q: query } })
  }
}

// Inicializar validación si hay valor inicial
if (searchQuery.value) {
  validateInput()
}
</script>

<style scoped>
.search-bar {
  margin: 2rem 0;
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
}

.search-input-wrapper {
  display: flex;
  gap: 0.5rem;
  position: relative;
}

.search-input {
  flex: 1;
  padding: 1rem 1.5rem;
  font-size: 1rem;
  border: 2px solid var(--color-gray-200, #e2e8f0);
  border-radius: 8px;
  outline: none;
  background: var(--color-white, white);
  color: var(--color-text, #1a202c);
  transition: all 0.2s ease;
}

[data-theme="dark"] .search-input {
  background: var(--color-dark-card);
  border-color: var(--color-dark-border);
  color: var(--color-gray-100);
}

.search-input:focus {
  border-color: var(--color-primary, #667eea);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-input.input-valid {
  border-color: var(--color-success, #48bb78);
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

.search-input.input-invalid {
  border-color: var(--color-error, #e53e3e);
  box-shadow: 0 0 0 3px rgba(229, 62, 62, 0.1);
}

.search-input:disabled {
  background: var(--color-gray-50, #f7fafc);
  opacity: 0.6;
}

.search-button {
  padding: 1rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  color: white;
  background: var(--color-primary, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.search-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.search-button.button-valid {
  background: var(--color-success, linear-gradient(135deg, #48bb78 0%, #38a169 100%));
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.3);
}

.validation-message {
  margin-top: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.validation-success {
  background: var(--color-success-light, #f0fff4);
  border: 1px solid var(--color-success-border, #9ae6b4);
  color: var(--color-success-dark, #276749);
}

.validation-error {
  background: var(--color-error-light, #fff5f5);
  border: 1px solid var(--color-error-border, #fc8181);
  color: var(--color-error-dark, #c53030);
}

[data-theme="dark"] .validation-success {
  background: rgba(72, 187, 120, 0.1);
  border-color: rgba(72, 187, 120, 0.3);
  color: var(--color-success);
}

[data-theme="dark"] .validation-error {
  background: rgba(229, 62, 62, 0.1);
  border-color: rgba(229, 62, 62, 0.3);
  color: var(--color-error);
}

.validation-success::before {
  content: '✓';
  font-weight: bold;
}

.validation-error::before {
  content: '⚠';
  font-weight: bold;
}

.error-message {
  margin-top: 0.5rem;
  padding: 0.75rem;
  background: var(--color-error-light, #fff5f5);
  border: 1px solid var(--color-error-border, #fc8181);
  border-radius: 6px;
  color: var(--color-error-dark, #c53030);
  font-size: 0.9rem;
}

[data-theme="dark"] .error-message {
  background: rgba(229, 62, 62, 0.1);
  border-color: rgba(229, 62, 62, 0.3);
  color: var(--color-error);
}
</style>
