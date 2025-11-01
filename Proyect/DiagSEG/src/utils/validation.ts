// Validaciones frontend solo para UX (no reemplazan validación backend)
export interface ValidationResult {
  isValid: boolean
  message?: string
  type?: 'ip' | 'domain' | 'unknown'
}

// Validación básica de IPv4 (solo formato visual)
export const isValidIPv4Format = (ip: string): boolean => {
  const ipv4Regex = /^(\d{1,3}\.){3}\d{1,3}$/
  if (!ipv4Regex.test(ip)) return false
  
  const parts = ip.split('.')
  return parts.every(part => {
    const num = parseInt(part, 10)
    return num >= 0 && num <= 255 && part === num.toString()
  })
}

// Validación básica de IPv6 (solo formato visual)
export const isValidIPv6Format = (ip: string): boolean => {
  const ipv6Regex = /^([0-9a-fA-F]{0,4}:){1,7}[0-9a-fA-F]{0,4}$/
  return ipv6Regex.test(ip) || ip === '::1' || ip === '::'
}

// Validación básica de dominio (solo formato visual)
export const isValidDomainFormat = (domain: string): boolean => {
  const domainRegex = /^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/
  return domainRegex.test(domain) && domain.length <= 253
}

// Función principal de validación frontend
export const validateInput = (input: string): ValidationResult => {
  const trimmedInput = input.trim()
  
  if (!trimmedInput) {
    return {
      isValid: false,
      message: 'Por favor ingresa una IP o dominio'
    }
  }

  if (trimmedInput.length > 253) {
    return {
      isValid: false,
      message: 'El texto ingresado es demasiado largo'
    }
  }

  // Detectar tipo de input
  if (isValidIPv4Format(trimmedInput)) {
    return {
      isValid: true,
      type: 'ip',
      message: 'IPv4 válida'
    }
  }

  if (isValidIPv6Format(trimmedInput)) {
    return {
      isValid: true,
      type: 'ip',
      message: 'IPv6 válida'
    }
  }

  if (isValidDomainFormat(trimmedInput)) {
    return {
      isValid: true,
      type: 'domain',
      message: 'Dominio válido'
    }
  }

  return {
    isValid: false,
    message: 'Formato no válido. Ingresa una IP (ej: 8.8.8.8) o dominio (ej: google.com)'
  }
}

// Detectar tipo de input sin validar
export const detectInputType = (input: string): 'ip' | 'domain' | 'unknown' => {
  const trimmedInput = input.trim()
  
  if (isValidIPv4Format(trimmedInput) || isValidIPv6Format(trimmedInput)) {
    return 'ip'
  }
  
  if (isValidDomainFormat(trimmedInput)) {
    return 'domain'
  }
  
  return 'unknown'
}

// Formatear placeholder según el tipo detectado
export const getPlaceholderText = (inputType: 'ip' | 'domain' | 'unknown'): string => {
  switch (inputType) {
    case 'ip':
      return 'Ingresa una dirección IP (ej: 8.8.8.8)'
    case 'domain':
      return 'Ingresa un dominio (ej: google.com)'
    default:
      return 'Ingresa una IP o dominio (ej: 8.8.8.8, google.com)'
  }
}
