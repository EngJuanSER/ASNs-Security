import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import type { AnalysisResult } from './api'

/**
 * Servicio para exportación de resultados de análisis a PDF
 */
class PDFExportService {
  /**
   * Exportar análisis a PDF usando el DOM actual
   */
  async exportAnalysisToPDF(query: string, results: AnalysisResult): Promise<void> {
    try {
      // Crear elemento temporal con el contenido del análisis
      const exportElement = this.createExportElement(query, results)
      document.body.appendChild(exportElement)

      // Configurar opciones para html2canvas
      const canvas = await html2canvas(exportElement, {
        scale: 2, // Mayor resolución
        useCORS: true,
        backgroundColor: '#ffffff',
        width: 800,
        height: exportElement.scrollHeight
      })

      // Crear PDF
      const pdf = new jsPDF('p', 'mm', 'a4')
      const imgWidth = 210 // A4 width in mm
      const pageHeight = 295 // A4 height in mm
      const imgHeight = (canvas.height * imgWidth) / canvas.width
      let heightLeft = imgHeight

      let position = 0

      // Añadir la primera página
      pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight

      // Añadir páginas adicionales si es necesario
      while (heightLeft >= 0) {
        position = heightLeft - imgHeight
        pdf.addPage()
        pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
        heightLeft -= pageHeight
      }

      // Descargar el PDF
      const filename = `analisis-seguridad-${query}-${new Date().toISOString().split('T')[0]}.pdf`
      pdf.save(filename)

      // Limpiar elemento temporal
      document.body.removeChild(exportElement)

    } catch (error) {
      console.error('Error al exportar PDF:', error)
      throw new Error('Error al generar el archivo PDF')
    }
  }

  /**
   * Crear elemento HTML con el contenido del análisis para exportar
   */
  private createExportElement(query: string, results: AnalysisResult): HTMLElement {
    const element = document.createElement('div')
    element.style.cssText = `
      width: 800px;
      padding: 20px;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      background: white;
      color: #333;
      position: absolute;
      top: -9999px;
      left: -9999px;
    `

    const currentDate = new Date().toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })

    element.innerHTML = `
      <div style="text-align: center; margin-bottom: 30px; border-bottom: 2px solid #e0e0e0; padding-bottom: 20px;">
        <h1 style="color: #2c3e50; margin: 0; font-size: 24px;">DiagSEG - Análisis de Seguridad</h1>
        <p style="color: #7f8c8d; margin: 5px 0 0 0; font-size: 14px;">Informe generado el ${currentDate}</p>
      </div>

      <div style="margin-bottom: 25px;">
        <h2 style="color: #34495e; border-left: 4px solid #3498db; padding-left: 15px; margin: 0 0 15px 0;">Información General</h2>
        <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 15px;">
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
            <div>
              <strong>Consulta:</strong> ${query}<br>
              <strong>IP:</strong> ${results.ip}<br>
              <strong>Tipo:</strong> ${results.type}
            </div>
            <div>
              <strong>Puntuación:</strong> <span style="color: ${this.getScoreColor(results.securityScore)}">${results.securityScore}/100</span><br>
              <strong>Análisis:</strong> ${new Date(results.timestamp).toLocaleString('es-ES')}
            </div>
          </div>
        </div>
      </div>

      <div style="margin-bottom: 25px;">
        <h2 style="color: #34495e; border-left: 4px solid #27ae60; padding-left: 15px; margin: 0 0 15px 0;">Geolocalización</h2>
        <div style="background: #f8f9fa; padding: 15px; border-radius: 8px;">
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
            <div>
              <strong>País:</strong> ${results.geolocation.country}<br>
              <strong>Ciudad:</strong> ${results.geolocation.city}<br>
              <strong>ISP:</strong> ${results.geolocation.isp}
            </div>
            <div>
              <strong>ASN:</strong> ${results.geolocation.asn}<br>
              <strong>Coordenadas:</strong> ${results.geolocation.latitude}, ${results.geolocation.longitude}
            </div>
          </div>
        </div>
      </div>

      <div style="margin-bottom: 25px;">
        <h2 style="color: #34495e; border-left: 4px solid #e74c3c; padding-left: 15px; margin: 0 0 15px 0;">Servicios Detectados</h2>
        ${results.services.map(service => `
          <div style="background: #f8f9fa; padding: 12px; border-radius: 8px; margin-bottom: 10px; border-left: 3px solid ${this.getRiskColor(service.riskLevel)};">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <strong>Puerto ${service.port}</strong> - ${service.name}<br>
                <small style="color: #666;">Versión: ${service.version}</small>
              </div>
              <div style="text-align: right;">
                <span style="color: ${this.getRiskColor(service.riskLevel)}; font-weight: bold;">
                  ${service.riskText}
                </span>
              </div>
            </div>
          </div>
        `).join('')}
      </div>

      <div style="margin-bottom: 25px;">
        <h2 style="color: #34495e; border-left: 4px solid #f39c12; padding-left: 15px; margin: 0 0 15px 0;">Reputación</h2>
        ${results.reputation.map(rep => `
          <div style="background: #f8f9fa; padding: 12px; border-radius: 8px; margin-bottom: 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
              <strong>${rep.name}</strong>
              <span style="color: ${this.getReputationColor(rep.status)}; font-weight: bold;">
                ${rep.statusText}
              </span>
            </div>
            <p style="margin: 0; color: #666; font-size: 14px;">${rep.details}</p>
          </div>
        `).join('')}
      </div>

      <div style="margin-bottom: 25px;">
        <h2 style="color: #34495e; border-left: 4px solid #9b59b6; padding-left: 15px; margin: 0 0 15px 0;">Recomendaciones</h2>
        ${results.recommendations.map(rec => `
          <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 10px; border-left: 3px solid ${this.getPriorityColor(rec.priority)};">
            <div style="display: flex; align-items: center; margin-bottom: 8px;">
              <span style="background: ${this.getPriorityColor(rec.priority)}; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-right: 10px;">
                ${rec.priority.toUpperCase()}
              </span>
              <strong>${rec.title}</strong>
            </div>
            <p style="margin: 0; color: #666;">${rec.description}</p>
          </div>
        `).join('')}
      </div>

      <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; text-align: center; color: #7f8c8d; font-size: 12px;">
        <p>Este informe fue generado automáticamente por DiagSEG</p>
        <p>Para más información visite: https://github.com/EngJuanSER/ASNs-Security</p>
      </div>
    `

    return element
  }

  /**
   * Obtener color según el estado
   */
  private getStatusColor(status: string): string {
    switch (status) {
      case 'safe': return '#27ae60'
      case 'warning': return '#f39c12'
      case 'danger': return '#e74c3c'
      default: return '#7f8c8d'
    }
  }

  /**
   * Obtener color según la puntuación
   */
  private getScoreColor(score: number): string {
    if (score >= 80) return '#27ae60'
    if (score >= 60) return '#f39c12'
    return '#e74c3c'
  }

  /**
   * Obtener color según el nivel de riesgo
   */
  private getRiskColor(riskLevel: string): string {
    switch (riskLevel) {
      case 'low': return '#27ae60'
      case 'medium': return '#f39c12'
      case 'high': return '#e74c3c'
      default: return '#7f8c8d'
    }
  }

  /**
   * Obtener color según el estado de reputación
   */
  private getReputationColor(status: string): string {
    switch (status) {
      case 'clean': return '#27ae60'
      case 'suspicious': return '#f39c12'
      case 'malicious': return '#e74c3c'
      default: return '#7f8c8d'
    }
  }

  /**
   * Obtener color según la prioridad
   */
  private getPriorityColor(priority: string): string {
    switch (priority) {
      case 'info': return '#3498db'
      case 'low': return '#27ae60'
      case 'medium': return '#f39c12'
      case 'high': return '#e74c3c'
      case 'critical': return '#8e44ad'
      default: return '#7f8c8d'
    }
  }
}

// Instancia singleton del servicio de exportación PDF
export const pdfExportService = new PDFExportService()

export default pdfExportService
