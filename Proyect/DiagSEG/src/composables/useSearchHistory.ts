import { ref, computed } from 'vue'

export interface SearchHistoryItem {
  query: string
  timestamp: number
  type: 'ip' | 'domain' | 'unknown'
}

const STORAGE_KEY = 'diagseg_search_history'
const MAX_HISTORY_ITEMS = 10

// Estado global del historial
const searchHistory = ref<SearchHistoryItem[]>([])

// Cargar historial desde localStorage
const loadHistory = (): SearchHistoryItem[] => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored ? JSON.parse(stored) : []
  } catch {
    return []
  }
}

// Guardar historial en localStorage
const saveHistory = (history: SearchHistoryItem[]) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history))
  } catch {
    // Silently fail if localStorage is not available
  }
}

// Inicializar historial
searchHistory.value = loadHistory()

export const useSearchHistory = () => {
  // Agregar nueva búsqueda al historial
  const addToHistory = (query: string, type: 'ip' | 'domain' | 'unknown' = 'unknown') => {
    const newItem: SearchHistoryItem = {
      query: query.trim(),
      timestamp: Date.now(),
      type
    }

    // Remover duplicados existentes
    searchHistory.value = searchHistory.value.filter(item => item.query !== newItem.query)
    
    // Agregar al inicio
    searchHistory.value.unshift(newItem)
    
    // Limitar el número de elementos
    if (searchHistory.value.length > MAX_HISTORY_ITEMS) {
      searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY_ITEMS)
    }
    
    // Guardar en localStorage
    saveHistory(searchHistory.value)
  }

  // Eliminar un elemento del historial
  const removeFromHistory = (query: string) => {
    searchHistory.value = searchHistory.value.filter(item => item.query !== query)
    saveHistory(searchHistory.value)
  }

  // Limpiar todo el historial
  const clearHistory = () => {
    searchHistory.value = []
    saveHistory([])
  }

  // Obtener historial reciente (últimos N elementos)
  const getRecentHistory = (limit: number = 5) => {
    return searchHistory.value.slice(0, limit)
  }

  // Buscar en el historial
  const searchInHistory = (searchTerm: string) => {
    return searchHistory.value.filter(item => 
      item.query.toLowerCase().includes(searchTerm.toLowerCase())
    )
  }

  // Verificar si una query está en el historial
  const isInHistory = (query: string) => {
    return searchHistory.value.some(item => item.query === query.trim())
  }

  // Formatear timestamp para mostrar
  const formatTimestamp = (timestamp: number) => {
    const now = Date.now()
    const diff = now - timestamp
    
    const minutes = Math.floor(diff / (1000 * 60))
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (minutes < 1) return 'Ahora'
    if (minutes < 60) return `Hace ${minutes} min`
    if (hours < 24) return `Hace ${hours} h`
    if (days < 7) return `Hace ${days} días`
    
    return new Date(timestamp).toLocaleDateString()
  }

  // Obtener estadísticas del historial
  const getHistoryStats = computed(() => {
    const total = searchHistory.value.length
    const ipCount = searchHistory.value.filter(item => item.type === 'ip').length
    const domainCount = searchHistory.value.filter(item => item.type === 'domain').length
    
    return {
      total,
      ips: ipCount,
      domains: domainCount,
      others: total - ipCount - domainCount
    }
  })

  return {
    searchHistory: computed(() => searchHistory.value),
    addToHistory,
    removeFromHistory,
    clearHistory,
    getRecentHistory,
    searchInHistory,
    isInHistory,
    formatTimestamp,
    historyStats: getHistoryStats
  }
}
