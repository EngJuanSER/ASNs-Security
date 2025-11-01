<template>
  <nav class="navbar">
    <div class="nav-container">
      <div class="nav-brand">
        <router-link to="/" class="brand-link">
          <span class="brand-text">DiagSEG</span>
        </router-link>
      </div>

      <!-- Menú de navegación -->
      <div class="nav-menu" :class="{ 'nav-menu-active': isMenuOpen }">
        <router-link to="/" class="nav-link" @click="closeMenu">
          Inicio
        </router-link>
        <router-link to="/analysis" class="nav-link" @click="closeMenu">
          Análisis
        </router-link>
        <router-link to="/history" class="nav-link" @click="closeMenu">
          Historial
        </router-link>
        <router-link to="/compare" class="nav-link" @click="closeMenu">
          Comparar
        </router-link>
        <a href="https://github.com/EngJuanSER/ASNs-Security" target="_blank" class="nav-link" @click="closeMenu">
          GitHub
        </a>
        <ThemeToggle />
      </div>

      <!-- Botón hamburguesa para móvil -->
      <button 
        class="nav-toggle" 
        @click="toggleMenu"
        :class="{ 'nav-toggle-active': isMenuOpen }"
      >
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
      </button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ThemeToggle from './ThemeToggle.vue'

const isMenuOpen = ref(false)

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  closeMenu()
}
</script>

<style scoped>
.navbar {
  background: var(--bg-primary);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-primary);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.nav-brand {
  display: flex;
  align-items: center;
}

.brand-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 700;
  font-size: 1.5rem;
  transition: color 0.3s ease;
}

.brand-link:hover {
  color: #667eea;
}

.logo-icon {
  font-size: 2rem;
  margin-right: 0.5rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.brand-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: var(--text-secondary);
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.nav-link.router-link-active {
  color: #667eea;
  background: rgba(102, 126, 234, 0.15);
  font-weight: 600;
}

.nav-link.router-link-exact-active {
  color: #667eea;
  background: rgba(102, 126, 234, 0.2);
  font-weight: 700;
}

.nav-icon {
  font-size: 1.1rem;
}

.nav-toggle {
  display: none;
  flex-direction: column;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.hamburger-line {
  width: 25px;
  height: 3px;
  background: var(--text-secondary);
  margin: 2px 0;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.nav-toggle-active .hamburger-line:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.nav-toggle-active .hamburger-line:nth-child(2) {
  opacity: 0;
}

.nav-toggle-active .hamburger-line:nth-child(3) {
  transform: rotate(-45deg) translate(7px, -6px);
}

/* Mobile styles */
@media (max-width: 768px) {
  .nav-toggle {
    display: flex;
  }

  .nav-menu {
    position: fixed;
    top: 70px;
    left: 0;
    width: 100%;
    height: calc(100vh - 70px);
    background: var(--bg-primary);
    backdrop-filter: blur(10px);
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
    padding-top: 2rem;
    gap: 1rem;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .nav-menu-active {
    transform: translateX(0);
  }

  .nav-link {
    width: 80%;
    justify-content: center;
    padding: 1rem;
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .nav-container {
    padding: 0 1rem;
  }
  
  .brand-text {
    font-size: 1.3rem;
  }
  
  .logo-icon {
    font-size: 1.8rem;
  }
}
</style>
