import { createApp, h } from 'vue'
import Toast, { type ToastProps } from '@/components/Toast.vue'

interface ToastOptions extends Partial<ToastProps> {
  title: string
  message: string
}

class ToastService {
  private container: HTMLDivElement | null = null

  private ensureContainer() {
    if (!this.container) {
      this.container = document.createElement('div')
      this.container.id = 'toast-container'
      document.body.appendChild(this.container)
    }
    return this.container
  }

  private show(options: ToastOptions) {
    const container = this.ensureContainer()
    const toastWrapper = document.createElement('div')
    container.appendChild(toastWrapper)

    const app = createApp({
      render() {
        return h(Toast, {
          ...options,
          onClose: () => {
            app.unmount()
            container.removeChild(toastWrapper)
          }
        })
      }
    })

    app.mount(toastWrapper)
  }

  success(title: string, message: string, action?: string, duration?: number) {
    this.show({ title, message, action, type: 'success', duration })
  }

  error(title: string, message: string, action?: string, duration?: number) {
    this.show({ title, message, action, type: 'error', duration: duration || 8000 })
  }

  warning(title: string, message: string, action?: string, duration?: number) {
    this.show({ title, message, action, type: 'warning', duration: duration || 6000 })
  }

  info(title: string, message: string, action?: string, duration?: number) {
    this.show({ title, message, action, type: 'info', duration })
  }

  /**
   * Muestra un toast específico para errores de backend
   */
  backendError(errorResponse: any) {
    const defaultTitle = 'Error en el Servidor'
    const defaultMessage = 'Ocurrió un error inesperado. Por favor, intenta nuevamente.'
    const defaultAction = 'Si el problema persiste, contacta al administrador.'

    // Si es una respuesta estructurada del backend
    if (errorResponse && typeof errorResponse === 'object') {
      const title = this.getErrorTitle(errorResponse.errorCode) || defaultTitle
      const message = errorResponse.error || errorResponse.message || defaultMessage
      const action = errorResponse.suggestedAction || defaultAction

      this.error(title, message, action, 10000)
    } else if (typeof errorResponse === 'string') {
      this.error(defaultTitle, errorResponse, defaultAction)
    } else {
      this.error(defaultTitle, defaultMessage, defaultAction)
    }
  }

  private getErrorTitle(errorCode?: string): string {
    const titles: Record<string, string> = {
      'NMAP_NOT_FOUND': 'Nmap No Instalado',
      'NMAP_TIMEOUT': 'Timeout del Escaneo',
      'NMAP_PERMISSION_DENIED': 'Permisos Insuficientes',
      'NMAP_EXECUTION_ERROR': 'Error en el Escaneo',
      'NVD_RATE_LIMIT': 'Límite de Consultas Excedido',
      'NVD_CONNECTION_ERROR': 'Error de Conexión',
      'NVD_TIMEOUT': 'Timeout de Vulnerabilidades',
      'ASN_SERVICE_UNAVAILABLE': 'Servicio ASN No Disponible',
      'GEOLOCATION_DB_ERROR': 'Error de Geolocalización',
      'INVALID_INPUT': 'Entrada Inválida'
    }
    return errorCode ? titles[errorCode] || 'Error del Sistema' : 'Error del Sistema'
  }
}

export const toast = new ToastService()
