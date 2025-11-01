import { ref, watch } from 'vue'

export type Theme = 'light' | 'dark' | 'system'

const STORAGE_KEY = 'diagseg_theme'

// Estado global del tema
const currentTheme = ref<Theme>('system')
const isDark = ref(false)

// Detectar preferencia del sistema
const getSystemPreference = (): boolean => {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

// Cargar tema desde localStorage
const loadTheme = (): Theme => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY) as Theme
    return stored || 'system'
  } catch {
    return 'system'
  }
}

// Guardar tema en localStorage
const saveTheme = (theme: Theme) => {
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch {
    // Silently fail if localStorage is not available
  }
}

// Aplicar tema al documento
const applyTheme = (theme: Theme) => {
  const root = document.documentElement
  
  if (theme === 'system') {
    const systemIsDark = getSystemPreference()
    isDark.value = systemIsDark
    root.setAttribute('data-theme', systemIsDark ? 'dark' : 'light')
  } else {
    isDark.value = theme === 'dark'
    root.setAttribute('data-theme', theme)
  }
}

// Escuchar cambios en la preferencia del sistema
const systemMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
systemMediaQuery.addEventListener('change', () => {
  if (currentTheme.value === 'system') {
    applyTheme('system')
  }
})

// Inicializar tema
currentTheme.value = loadTheme()
applyTheme(currentTheme.value)

export const useTheme = () => {
  // Cambiar tema
  const setTheme = (theme: Theme) => {
    currentTheme.value = theme
    applyTheme(theme)
    saveTheme(theme)
  }

  // Alternar entre claro y oscuro
  const toggleTheme = () => {
    if (currentTheme.value === 'system') {
      setTheme(isDark.value ? 'light' : 'dark')
    } else {
      setTheme(currentTheme.value === 'light' ? 'dark' : 'light')
    }
  }

  // Obtener el siguiente tema en el ciclo
  const getNextTheme = (): Theme => {
    switch (currentTheme.value) {
      case 'light': return 'dark'
      case 'dark': return 'system'
      case 'system': return 'light'
    }
  }

  // Ciclar entre todos los temas
  const cycleTheme = () => {
    setTheme(getNextTheme())
  }

  // Obtener etiqueta del tema
  const getThemeLabel = (theme: Theme): string => {
    switch (theme) {
      case 'light': return 'Claro'
      case 'dark': return 'Oscuro'
      case 'system': return 'Sistema'
    }
  }

  // Obtener icono del tema
  const getThemeIcon = (theme: Theme): string => {
    switch (theme) {
      case 'light': return 'Light'
      case 'dark': return 'Dark'
      case 'system': return 'Auto'
    }
  }

  return {
    currentTheme: currentTheme,
    isDark: isDark,
    setTheme,
    toggleTheme,
    cycleTheme,
    getThemeLabel,
    getThemeIcon,
    getNextTheme
  }
}
