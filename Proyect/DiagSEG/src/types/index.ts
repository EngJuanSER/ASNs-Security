// Tipos básicos para la aplicación DiagSEG

export interface SearchQuery {
  query: string
  type: 'ipv4' | 'ipv6' | 'domain'
}
