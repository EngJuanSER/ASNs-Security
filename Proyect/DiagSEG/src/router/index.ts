import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Analysis from '../views/Analysis.vue'
import History from '../views/History.vue'
import IPComparator from '../views/IPComparator.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: Analysis
  },
  {
    path: '/history',
    name: 'History',
    component: History,
    meta: {
      title: 'Historial de Análisis',
      description: 'Visualiza estadísticas y revisa análisis anteriores de seguridad'
    }
  },
  {
    path: '/compare',
    name: 'IPComparator',
    component: IPComparator,
    meta: {
      title: 'Comparador de IPs',
      description: 'Compara dos direcciones IP o dominios lado a lado'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
