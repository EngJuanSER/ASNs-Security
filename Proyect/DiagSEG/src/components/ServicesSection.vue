<template>
  <div class="result-card services-card">
    <div class="card-header">
      <div class="header-title-section">
        <svg class="card-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" />
        </svg>
        <h3 class="card-title">Servicios Activos</h3>
      </div>
      <span class="service-count-badge">{{ services.length }} detectados</span>
    </div>
    <div class="card-content">
      <p class="services-intro">
        Puertos y servicios detectados mediante <strong>Nmap</strong>. El nivel de riesgo se basa en la exposición y versiones conocidas.
      </p>
      <div class="services-grid-improved">
        <div 
          v-for="service in services" 
          :key="service.port"
          class="service-card-improved"
          :class="`risk-${service.riskLevel}`"
        >
          <div class="service-card-header">
            <div class="service-port-badge" :class="`risk-${service.riskLevel}`">
              Puerto {{ service.port }}
            </div>
            <div class="service-risk-badge" :class="`risk-${service.riskLevel}`">
              {{ service.riskText }}
            </div>
          </div>
          <div class="service-card-body">
            <div class="service-name-improved">{{ service.name }}</div>
            <div class="service-version-improved" v-if="service.version && service.version !== 'Unknown'">
              <svg class="version-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
              {{ service.version }}
            </div>
            <div class="service-version-improved unknown" v-else>
              <svg class="version-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Versión desconocida
            </div>
            <div class="service-tech-info" v-if="getServiceTechnology(service)">
              <div class="tech-description">
                <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {{ getServiceTechnology(service)?.description }}
              </div>
              <div class="tech-recommendation" :class="`risk-${service.riskLevel}`">
                <svg class="shield-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
                {{ getServiceTechnology(service)?.recommendation }}
              </div>
            </div>
            <!-- Mensaje por defecto cuando no hay información del servicio -->
            <div class="service-tech-info no-info" v-else>
              <div class="tech-description default">
                <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
                No hay información detallada disponible para este servicio en nuestra base de datos.
              </div>
              <div class="tech-recommendation default">
                <svg class="shield-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                Se recomienda buscar información sobre "{{ service.name }}" en internet para entender sus riesgos y mejores prácticas de seguridad.
              </div>
            </div>
          </div>
          <div class="service-card-footer" v-if="hasVulnerabilities(service)">
            <svg class="warning-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <span>Vulnerabilidades detectadas</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ServiceInfo, Vulnerability } from '@/services/api'

interface Props {
  services: ServiceInfo[]
  vulnerabilities: Vulnerability[]
}

const props = defineProps<Props>()

interface TechnologyInfo {
  description: string
  recommendation: string
}

const serviceTechnologies: Record<string, TechnologyInfo> = {
  'http': {
    description: 'Protocolo de transferencia de hipertexto. Usado para sitios web y aplicaciones web. Principal punto de entrada en servidores.',
    recommendation: 'Usar HTTPS en su lugar. Implementar certificado SSL/TLS válido y actualizado.'
  },
  'https': {
    description: 'HTTP seguro con cifrado SSL/TLS. Protege la comunicación entre cliente y servidor cifrando los datos transmitidos.',
    recommendation: 'Mantener certificados actualizados. Usar TLS 1.2+ y configurar cabeceras de seguridad.'
  },
  'ssh': {
    description: 'Shell seguro para acceso remoto. Permite administración del servidor de forma cifrada y autenticada.',
    recommendation: 'Usar autenticación por claves SSH en lugar de contraseñas. Deshabilitar acceso root directo.'
  },
  'ftp': {
    description: 'Protocolo de transferencia de archivos. Transmite datos sin cifrar, incluyendo credenciales de acceso.',
    recommendation: 'Reemplazar por SFTP o FTPS. FTP es obsoleto y transmite credenciales en texto plano.'
  },
  'smtp': {
    description: 'Protocolo de envío de correo electrónico. Usado por servidores para enviar y recibir emails.',
    recommendation: 'Configurar autenticación SMTP. Implementar SPF, DKIM y DMARC para prevenir spoofing.'
  },
  'mysql': {
    description: 'Sistema de gestión de bases de datos relacionales. Almacena datos de aplicaciones web y sistemas.',
    recommendation: 'No exponer públicamente. Usar solo en red privada con contraseñas fuertes.'
  },
  'postgresql': {
    description: 'Sistema de gestión de bases de datos avanzado. Usado en aplicaciones empresariales para almacenar datos críticos.',
    recommendation: 'Restringir acceso solo a IPs autorizadas. Cifrar conexiones con SSL/TLS.'
  },
  'redis': {
    description: 'Almacenamiento de datos en memoria. Usado como caché, sesiones o broker de mensajes en aplicaciones web.',
    recommendation: 'Nunca exponer a internet. Requiere autenticación con contraseña fuerte.'
  },
  'mongodb': {
    description: 'Base de datos NoSQL orientada a documentos. Almacena datos en formato JSON para aplicaciones modernas.',
    recommendation: 'Habilitar autenticación obligatoria. Usar firewall para restringir acceso.'
  },
  'dns': {
    description: 'Sistema de nombres de dominio. Traduce nombres de dominio a direcciones IP para acceso a servicios.',
    recommendation: 'Implementar DNSSEC. Proteger contra amplificación DDoS limitando consultas recursivas.'
  },
  'telnet': {
    description: 'Protocolo de acceso remoto obsoleto. Transmite todo en texto plano sin cifrado, incluyendo contraseñas.',
    recommendation: 'Desactivar inmediatamente. Reemplazar con SSH para acceso remoto seguro.'
  },
  'rdp': {
    description: 'Protocolo de escritorio remoto de Microsoft. Permite acceso gráfico completo al sistema Windows.',
    recommendation: 'Usar VPN o túnel SSH. Cambiar puerto por defecto y habilitar autenticación de dos factores.'
  },
  'smb': {
    description: 'Protocolo de compartición de archivos de Windows. Permite acceso a carpetas y recursos compartidos en red.',
    recommendation: 'Deshabilitar SMBv1. No exponer a internet. Usar solo en redes privadas seguras.'
  },
  'ntp': {
    description: 'Protocolo de sincronización de tiempo. Mantiene la hora exacta del servidor sincronizada con servidores de tiempo.',
    recommendation: 'Limitar respuestas NTP para prevenir amplificación DDoS. Usar NTS para cifrado.'
  },
  'vnc': {
    description: 'Protocolo de escritorio remoto multiplataforma. Permite control gráfico del servidor desde otro equipo.',
    recommendation: 'Usar túnel SSH o VPN. Implementar autenticación fuerte y cifrado de conexión.'
  },
  'ldap': {
    description: 'Protocolo de acceso a directorios. Usado para autenticación centralizada y gestión de usuarios en redes corporativas.',
    recommendation: 'Usar LDAPS (LDAP sobre SSL/TLS). Implementar controles de acceso estrictos.'
  },
  'pop3': {
    description: 'Protocolo de recepción de correo electrónico. Descarga emails del servidor al cliente eliminándolos del servidor.',
    recommendation: 'Usar POP3S (con SSL/TLS). Considerar IMAP como alternativa más segura.'
  },
  'imap': {
    description: 'Protocolo de acceso a correo electrónico avanzado. Mantiene emails en el servidor para acceso desde múltiples dispositivos.',
    recommendation: 'Usar IMAPS (con SSL/TLS). Implementar autenticación de dos factores.'
  },
  'docker': {
    description: 'API de Docker para gestión de contenedores. Permite control y despliegue de aplicaciones containerizadas.',
    recommendation: 'Nunca exponer sin autenticación. Usar TLS y certificados para conexiones remotas.'
  },
  'elasticsearch': {
    description: 'Motor de búsqueda y análisis de datos. Usado para logging, análisis de logs y búsquedas en tiempo real.',
    recommendation: 'No exponer públicamente. Habilitar autenticación X-Pack o Security plugin.'
  },
  'mssql': {
    description: 'Microsoft SQL Server. Sistema de gestión de bases de datos relacionales de Microsoft usado en aplicaciones empresariales.',
    recommendation: 'No exponer públicamente. Usar autenticación Windows y cifrar conexiones con SSL/TLS.'
  },
  'oracle': {
    description: 'Oracle Database. Base de datos empresarial de alto rendimiento para aplicaciones críticas y transaccionales.',
    recommendation: 'Restringir acceso con firewall. Aplicar todos los parches de seguridad de Oracle.'
  },
  'snmp': {
    description: 'Protocolo de gestión de red. Permite monitoreo y administración de dispositivos de red como routers y switches.',
    recommendation: 'Usar SNMPv3 con autenticación. Cambiar comunidades por defecto (public/private).'
  },
  'sip': {
    description: 'Protocolo de inicio de sesión. Usado para VoIP y comunicaciones multimedia en tiempo real.',
    recommendation: 'Usar SIPS (SIP seguro con TLS). Implementar autenticación fuerte para prevenir fraude.'
  },
  'rtsp': {
    description: 'Protocolo de streaming en tiempo real. Usado para transmisión de audio y video en cámaras IP y servidores multimedia.',
    recommendation: 'Usar RTSPS (con TLS). Implementar autenticación para evitar acceso no autorizado.'
  },
  'mqtt': {
    description: 'Protocolo de mensajería IoT. Usado en dispositivos Internet de las Cosas para comunicación ligera y eficiente.',
    recommendation: 'Usar autenticación y TLS. Validar y filtrar todos los mensajes del broker.'
  },
  'amqp': {
    description: 'Protocolo avanzado de colas de mensajes. Usado en sistemas de mensajería como RabbitMQ para comunicación entre servicios.',
    recommendation: 'Habilitar autenticación. Usar TLS para cifrar mensajes en tránsito.'
  },
  'cassandra': {
    description: 'Base de datos NoSQL distribuida. Diseñada para alta disponibilidad y escalabilidad en aplicaciones críticas.',
    recommendation: 'Habilitar autenticación. Cifrar comunicación entre nodos con SSL/TLS.'
  },
  'couchdb': {
    description: 'Base de datos NoSQL orientada a documentos. Almacena datos JSON con replicación y API HTTP.',
    recommendation: 'Habilitar autenticación obligatoria. No exponer puerto de administración públicamente.'
  },
  'memcached': {
    description: 'Sistema de caché distribuido en memoria. Acelera aplicaciones web almacenando datos frecuentemente accedidos.',
    recommendation: 'Nunca exponer a internet. Usar solo en red privada sin acceso público.'
  },
  'jenkins': {
    description: 'Servidor de automatización y CI/CD. Usado para compilar, probar y desplegar aplicaciones automáticamente.',
    recommendation: 'Habilitar autenticación. Usar HTTPS y mantener actualizado para evitar ejecución remota de código.'
  },
  'kubernetes': {
    description: 'API de Kubernetes para orquestación de contenedores. Gestiona despliegue, escalado y operaciones de aplicaciones containerizadas.',
    recommendation: 'Usar autenticación basada en certificados. Implementar RBAC y políticas de red estrictas.'
  },
  'grafana': {
    description: 'Plataforma de visualización y monitoreo. Crea dashboards para análisis de métricas y logs en tiempo real.',
    recommendation: 'Habilitar autenticación. Usar HTTPS y proteger credenciales de fuentes de datos.'
  },
  'prometheus': {
    description: 'Sistema de monitoreo y alertas. Recopila métricas de sistemas y aplicaciones para análisis y alertas.',
    recommendation: 'Usar autenticación básica o proxy reverso. No exponer métricas sensibles públicamente.'
  },
  'etcd': {
    description: 'Almacenamiento clave-valor distribuido. Usado por Kubernetes y otros sistemas distribuidos para configuración.',
    recommendation: 'Habilitar autenticación con certificados. Cifrar comunicación entre nodos y clientes.'
  },
  'rsync': {
    description: 'Protocolo de sincronización de archivos. Usado para backups y replicación eficiente de datos entre servidores.',
    recommendation: 'Usar sobre SSH (rsync+ssh). Restringir acceso con autenticación fuerte.'
  },
  'nfs': {
    description: 'Sistema de archivos de red. Permite compartir directorios entre sistemas Unix/Linux en red.',
    recommendation: 'Usar NFSv4 con Kerberos. Restringir montajes solo a IPs autorizadas.'
  },
  'samba': {
    description: 'Implementación libre de SMB/CIFS. Permite interoperabilidad entre Linux/Unix y Windows para compartir archivos.',
    recommendation: 'Deshabilitar SMBv1. Usar autenticación fuerte y no exponer a internet.'
  },
  'openvpn': {
    description: 'Servidor VPN de código abierto. Crea túneles seguros para acceso remoto a redes privadas.',
    recommendation: 'Usar autenticación de dos factores. Mantener actualizado y usar cifrado fuerte (AES-256).'
  },
  'ipsec': {
    description: 'Protocolo de seguridad IP. Proporciona cifrado y autenticación para comunicaciones VPN de nivel IP.',
    recommendation: 'Usar IKEv2 con certificados. Evitar algoritmos débiles como 3DES o MD5.'
  },
  'pptp': {
    description: 'Protocolo VPN obsoleto de Microsoft. Conocido por vulnerabilidades de seguridad y cifrado débil.',
    recommendation: 'Desactivar inmediatamente. Reemplazar con OpenVPN, WireGuard o IKEv2.'
  },
  'wireguard': {
    description: 'VPN moderna y rápida. Ofrece mejor rendimiento y seguridad que VPN tradicionales.',
    recommendation: 'Mantener configuración de claves segura. Rotar claves periódicamente.'
  },
  'apache': {
    description: 'Servidor web Apache HTTP Server. Uno de los servidores web más populares para alojar sitios y aplicaciones.',
    recommendation: 'Mantener actualizado. Configurar HTTPS, deshabilitar módulos innecesarios y ocultar versión.'
  },
  'nginx': {
    description: 'Servidor web y proxy inverso de alto rendimiento. Usado para servir contenido web y balanceo de carga.',
    recommendation: 'Mantener actualizado. Configurar rate limiting, HTTPS y cabeceras de seguridad.'
  },
  'tomcat': {
    description: 'Servidor de aplicaciones Java. Ejecuta aplicaciones web Java y servlets en entornos empresariales.',
    recommendation: 'Desactivar manager y host-manager si no se usan. Usar contraseñas fuertes y HTTPS.'
  },
  'weblogic': {
    description: 'Servidor de aplicaciones Java EE de Oracle. Usado en entornos empresariales para aplicaciones críticas.',
    recommendation: 'Aplicar todos los parches de seguridad. Frecuentemente objetivo de vulnerabilidades críticas.'
  },
  'jboss': {
    description: 'Servidor de aplicaciones Java EE de código abierto (ahora WildFly). Ejecuta aplicaciones empresariales Java.',
    recommendation: 'Mantener actualizado. Proteger consola de administración con autenticación fuerte.'
  },
  'iis': {
    description: 'Internet Information Services de Microsoft. Servidor web para Windows que ejecuta aplicaciones ASP.NET.',
    recommendation: 'Mantener Windows actualizado. Configurar HTTPS, deshabilitar métodos HTTP innecesarios.'
  },
  'cups': {
    description: 'Sistema de impresión Unix. Gestiona trabajos de impresión y comparte impresoras en red.',
    recommendation: 'No exponer a internet. Restringir acceso solo a red local confiable.'
  },
  'printer': {
    description: 'Servicio de impresora de red. Permite impresión directa a través de protocolos como IPP, LPD o JetDirect.',
    recommendation: 'Segmentar en VLAN separada. Actualizar firmware y deshabilitar protocolos innecesarios.'
  },
  'iscsi': {
    description: 'Protocolo de almacenamiento en red basado en IP. Permite acceso a dispositivos de almacenamiento por red.',
    recommendation: 'Usar autenticación CHAP. Segmentar en VLAN dedicada sin acceso a internet.'
  },
  'tftp': {
    description: 'Protocolo trivial de transferencia de archivos. Versión simplificada de FTP sin autenticación ni cifrado.',
    recommendation: 'Desactivar si no es necesario. Solo usar en redes aisladas para arranque PXE.'
  },
  'socks': {
    description: 'Protocolo de proxy. Permite enrutamiento de tráfico de red a través de un servidor intermediario.',
    recommendation: 'Usar SOCKS5 con autenticación. Restringir acceso solo a usuarios autorizados.'
  },
  'squid': {
    description: 'Servidor proxy y caché HTTP. Acelera navegación web y controla acceso a internet en redes corporativas.',
    recommendation: 'Configurar listas de control de acceso (ACL). Usar autenticación y logs de auditoría.'
  },
  'haproxy': {
    description: 'Balanceador de carga y proxy reverso. Distribuye tráfico entre múltiples servidores backend.',
    recommendation: 'Proteger stats interface. Configurar rate limiting y timeouts apropiados.'
  },
  'zookeeper': {
    description: 'Servicio de coordinación distribuida. Usado en sistemas distribuidos para sincronización y configuración.',
    recommendation: 'Habilitar autenticación SASL. Usar ACLs para controlar acceso a znodes.'
  },
  'kafka': {
    description: 'Plataforma de streaming distribuido. Procesa y almacena streams de datos en tiempo real a gran escala.',
    recommendation: 'Habilitar autenticación SASL y cifrado TLS. Configurar ACLs para topics.'
  },
  'activemq': {
    description: 'Broker de mensajes Java. Implementa JMS para comunicación entre aplicaciones empresariales.',
    recommendation: 'Habilitar autenticación. Proteger consola web y mantener actualizado.'
  },
  'rabbitmq': {
    description: 'Broker de mensajes robusto. Implementa AMQP para comunicación confiable entre microservicios.',
    recommendation: 'Habilitar autenticación. Usar TLS para conexiones y proteger management plugin.'
  },
  'solr': {
    description: 'Plataforma de búsqueda empresarial. Basada en Lucene para búsquedas de texto completo.',
    recommendation: 'Habilitar autenticación. No exponer admin UI públicamente.'
  },
  'couchbase': {
    description: 'Base de datos NoSQL distribuida. Combina características de documentos y caché para aplicaciones de alto rendimiento.',
    recommendation: 'Habilitar autenticación RBAC. Cifrar comunicación con TLS.'
  },
  'riak': {
    description: 'Base de datos NoSQL distribuida de clave-valor. Diseñada para alta disponibilidad y tolerancia a fallos.',
    recommendation: 'Configurar autenticación. Usar solo en redes privadas.'
  },
  'influxdb': {
    description: 'Base de datos de series temporales. Optimizada para almacenar métricas, eventos y análisis en tiempo real.',
    recommendation: 'Habilitar autenticación. Proteger API HTTP con HTTPS.'
  },
  'timescaledb': {
    description: 'Extensión de PostgreSQL para series temporales. Combina SQL relacional con escalabilidad para datos temporales.',
    recommendation: 'Seguir mejores prácticas de PostgreSQL. Restringir acceso y usar SSL/TLS.'
  },
  'cockroachdb': {
    description: 'Base de datos SQL distribuida. Proporciona consistencia fuerte y alta disponibilidad global.',
    recommendation: 'Usar autenticación con certificados. Cifrar comunicación entre nodos.'
  },
  'neo4j': {
    description: 'Base de datos de grafos. Optimizada para almacenar y consultar relaciones complejas entre datos.',
    recommendation: 'Habilitar autenticación. Usar HTTPS para web UI y Bolt con TLS.'
  },
  'tcpwrapped': {
    description: 'Servicio protegido por TCP Wrapper. El servidor usa tcp_wrappers para controlar acceso basado en reglas.',
    recommendation: 'Revisar configuración en /etc/hosts.allow y /etc/hosts.deny. Asegurar que solo IPs autorizadas tengan acceso.'
  },
  'unknown': {
    description: 'Servicio no identificado. El puerto está abierto pero no se pudo determinar el servicio específico.',
    recommendation: 'Investigar manualmente qué servicio se ejecuta en este puerto. Considerar cerrar si no es necesario.'
  },
  'filtered': {
    description: 'Puerto filtrado. Un firewall o sistema de filtrado está bloqueando el acceso al puerto.',
    recommendation: 'Verificar reglas de firewall. Asegurar que el filtrado sea intencional y necesario.'
  },
  'closed': {
    description: 'Puerto cerrado. No hay ningún servicio escuchando en este puerto.',
    recommendation: 'Estado seguro. No se requiere acción a menos que se esperara un servicio en este puerto.'
  },
  'msrpc': {
    description: 'Microsoft Remote Procedure Call. Protocolo usado por Windows para comunicación entre procesos.',
    recommendation: 'Restringir acceso solo a red interna. Deshabilitar si no se usa en entornos Windows.'
  },
  'netbios-ssn': {
    description: 'Sesión NetBIOS. Protocolo heredado de Windows para compartir archivos y servicios de red.',
    recommendation: 'Deshabilitar NetBIOS sobre TCP/IP si no es necesario. Usar SMB3 en su lugar.'
  },
  'microsoft-ds': {
    description: 'Microsoft Directory Services (Active Directory). Puerto usado por AD para servicios de directorio.',
    recommendation: 'Proteger con firewall. Solo permitir acceso desde red corporativa interna.'
  },
  'domain': {
    description: 'Servicio DNS en puerto estándar. Resuelve nombres de dominio a direcciones IP.',
    recommendation: 'Implementar DNSSEC. Proteger contra ataques de amplificación DNS.'
  },
  'kerberos': {
    description: 'Protocolo de autenticación de red. Usado por Active Directory y otros sistemas para autenticación segura.',
    recommendation: 'Mantener sincronización de tiempo NTP. Proteger de ataques de replay con tickets.'
  },
  'ldaps': {
    description: 'LDAP seguro con SSL/TLS. Versión cifrada de LDAP para autenticación y directorios.',
    recommendation: 'Usar certificados válidos. Implementar controles de acceso estrictos.'
  },
  'ms-sql-s': {
    description: 'Microsoft SQL Server. Sistema de gestión de bases de datos de Microsoft.',
    recommendation: 'No exponer públicamente. Usar autenticación Windows y cifrar conexiones.'
  },
  'submission': {
    description: 'Puerto de envío SMTP (587). Usado para envío autenticado de correo electrónico.',
    recommendation: 'Requerir autenticación y STARTTLS. Implementar rate limiting contra spam.'
  },
  'imaps': {
    description: 'IMAP seguro con SSL/TLS. Acceso cifrado a correo electrónico en servidor.',
    recommendation: 'Usar certificados válidos. Implementar autenticación de dos factores.'
  },
  'pop3s': {
    description: 'POP3 seguro con SSL/TLS. Descarga cifrada de correo electrónico del servidor.',
    recommendation: 'Preferir IMAPS sobre POP3S. Usar certificados SSL/TLS válidos.'
  },
  'smtps': {
    description: 'SMTP seguro con SSL/TLS implícito. Envío de correo con cifrado desde el inicio.',
    recommendation: 'Configurar certificados válidos. Implementar SPF, DKIM y DMARC.'
  },
  'mysql-proxy': {
    description: 'Proxy para MySQL. Intermediario entre clientes y servidores MySQL para balanceo o monitoreo.',
    recommendation: 'Proteger con autenticación. Monitorear conexiones para detectar anomalías.'
  },
  'distcc': {
    description: 'Compilación distribuida en C/C++. Permite compilar código en múltiples máquinas.',
    recommendation: 'No exponer públicamente. Conocido por vulnerabilidades de ejecución remota.'
  },
  'svn': {
    description: 'Subversion. Sistema de control de versiones centralizado para gestión de código fuente.',
    recommendation: 'Usar autenticación. Preferir HTTPS sobre svn://. Considerar migrar a Git.'
  },
  'git': {
    description: 'Protocolo Git. Permite clonar y sincronizar repositorios Git remotos.',
    recommendation: 'Usar autenticación SSH o HTTPS. No exponer .git en servidores web.'
  },
  'ajp13': {
    description: 'Apache JServ Protocol. Protocolo binario entre servidor web y Tomcat/aplicaciones Java.',
    recommendation: 'No exponer públicamente. Solo usar en red interna detrás de Apache/Nginx.'
  },
  'afs3-fileserver': {
    description: 'Andrew File System. Sistema de archivos distribuido para compartir archivos en red.',
    recommendation: 'Usar autenticación Kerberos. Restringir acceso solo a red interna.'
  },
  'x11': {
    description: 'X Window System. Protocolo para interfaces gráficas en Unix/Linux.',
    recommendation: 'Nunca exponer X11 a internet. Usar túneles SSH para acceso remoto seguro.'
  },
  'vnc-http': {
    description: 'VNC sobre HTTP. Acceso web a escritorio remoto VNC.',
    recommendation: 'Usar contraseña fuerte. Implementar autenticación adicional y túnel SSH/VPN.'
  },
  'ms-wbt-server': {
    description: 'Microsoft Windows Terminal Server (RDP). Escritorio remoto de Windows.',
    recommendation: 'Cambiar puerto por defecto. Usar NLA y autenticación de dos factores.'
  },
  'ipp': {
    description: 'Internet Printing Protocol. Protocolo para impresión remota sobre HTTP.',
    recommendation: 'Restringir acceso solo a red local. Actualizar firmware de impresoras.'
  },
  'rpcbind': {
    description: 'RPC Bind. Mapea servicios RPC a puertos en sistemas Unix/Linux.',
    recommendation: 'Restringir acceso con firewall. Puede exponer información sobre servicios RPC.'
  },
  'mountd': {
    description: 'NFS Mount Daemon. Maneja solicitudes de montaje de sistemas de archivos NFS.',
    recommendation: 'Usar junto con NFSv4 y Kerberos. Restringir a IPs específicas.'
  },
  'java-rmi': {
    description: 'Java Remote Method Invocation. Permite ejecutar métodos en JVM remotas.',
    recommendation: 'No exponer públicamente. Vulnerable a ataques de deserialización. Usar autenticación.'
  },
  'upnp': {
    description: 'Universal Plug and Play. Permite descubrimiento automático de dispositivos en red.',
    recommendation: 'Deshabilitar en routers expuestos a internet. Conocido por vulnerabilidades.'
  },
  'ssdp': {
    description: 'Simple Service Discovery Protocol. Parte de UPnP para descubrir servicios.',
    recommendation: 'Deshabilitar si no es necesario. Vulnerable a amplificación DDoS.'
  },
  'mdns': {
    description: 'Multicast DNS (Bonjour/Zeroconf). Resolución de nombres sin servidor DNS en redes locales.',
    recommendation: 'Solo para redes locales confiables. No debe ser accesible desde internet.'
  }
}

const hasVulnerabilities = (service: ServiceInfo) => {
  return props.vulnerabilities.some(vuln => 
    vuln.description.toLowerCase().includes(service.name.toLowerCase()) ||
    vuln.title.toLowerCase().includes(service.name.toLowerCase())
  )
}

const getServiceTechnology = (service: ServiceInfo): TechnologyInfo | null => {
  const serviceName = service.name.toLowerCase()
  
  // Buscar coincidencia exacta
  if (serviceTechnologies[serviceName]) {
    return serviceTechnologies[serviceName]
  }
  
  // Buscar coincidencia parcial
  for (const [key, value] of Object.entries(serviceTechnologies)) {
    if (serviceName.includes(key) || key.includes(serviceName)) {
      return value
    }
  }
  
  return null
}
</script>

<style scoped>
.result-card {
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--shadow-md);
  transition: all 0.3s ease;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-title-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.card-icon {
  width: 24px;
  height: 24px;
  color: var(--color-primary);
}

.card-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.service-count-badge {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: white;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.card-content {
  color: var(--text-secondary);
}

.services-intro {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0 0 1.5rem 0;
  padding: 0.875rem 1rem;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-primary) 100%);
  border-radius: 8px;
}

.services-intro strong {
  color: var(--text-primary);
  font-weight: 700;
}

.services-grid-improved {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.25rem;
}

.service-card-improved {
  background: var(--bg-primary);
  border-radius: 10px;
  padding: 0;
  border: 2px solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.service-card-improved.risk-low {
  border-color: #10B981;
}

.service-card-improved.risk-medium {
  border-color: #F59E0B;
}

.service-card-improved.risk-high {
  border-color: #EF4444;
}

.service-card-improved:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.service-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  background: var(--bg-secondary);
  gap: 0.75rem;
  flex-wrap: wrap;
}

.service-port-badge {
  padding: 0.5rem 0.875rem;
  border-radius: 6px;
  font-weight: 700;
  font-size: 0.95rem;
  color: white;
  letter-spacing: 0.3px;
}

.service-port-badge.risk-low {
  background: #10B981;
}

.service-port-badge.risk-medium {
  background: #F59E0B;
}

.service-port-badge.risk-high {
  background: #EF4444;
}

.service-risk-badge {
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Mejor contraste en modo oscuro */
.service-risk-badge.risk-low {
  background: #D1FAE5;
  color: #065F46;
}

@media (prefers-color-scheme: dark) {
  .service-risk-badge.risk-low {
    background: #065F46;
    color: #D1FAE5;
  }
}

.service-risk-badge.risk-medium {
  background: #FEF3C7;
  color: #92400E;
}

@media (prefers-color-scheme: dark) {
  .service-risk-badge.risk-medium {
    background: #92400E;
    color: #FEF3C7;
  }
}

.service-risk-badge.risk-high {
  background: #FEE2E2;
  color: #991B1B;
}

@media (prefers-color-scheme: dark) {
  .service-risk-badge.risk-high {
    background: #991B1B;
    color: #FEE2E2;
  }
}

.service-card-body {
  padding: 1.25rem;
}

.service-name-improved {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.75rem;
  letter-spacing: 0.3px;
}

.service-version-improved {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
  padding: 0.5rem 0.75rem;
  background: var(--bg-secondary);
  border-radius: 6px;
  margin-top: 0.5rem;
}

.service-version-improved.unknown {
  color: #9CA3AF;
  font-style: italic;
}

.version-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.service-tech-info {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-secondary);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.tech-description {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
  padding: 0.75rem;
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 8px;
  font-size: 0.875rem;
  line-height: 1.5;
  color: #2e4375;
  font-weight: 700;
}

[data-theme="dark"] .tech-description {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.4);
  color: #93C5FD;
  font-weight: 500;
}

.info-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  color: #1e40af;
  margin-top: 2px;
}

[data-theme="dark"] .info-icon {
  color: #60a5fa;
}

.tech-recommendation {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
  padding: 0.75rem;
  border-radius: 8px;
  font-size: 0.875rem;
  line-height: 1.5;
  font-weight: 700;
  border: 1px solid;
}

.tech-recommendation.risk-low {
  background: rgba(16, 185, 129, 0.15);
  border-color: rgba(16, 185, 129, 0.4);
  color: #347951;
}

[data-theme="dark"] .tech-recommendation.risk-low {
  background: rgba(16, 185, 129, 0.2);
  border-color: rgba(16, 185, 129, 0.4);
  color: #6EE7B7;
  font-weight: 600;
}

.tech-recommendation.risk-medium {
  background: rgba(245, 158, 11, 0.15);
  border-color: rgba(245, 158, 11, 0.4);
  color: #754932;
}

[data-theme="dark"] .tech-recommendation.risk-medium {
  background: rgba(245, 158, 11, 0.2);
  border-color: rgba(245, 158, 11, 0.4);
  color: #FCD34D;
  font-weight: 600;
}

.tech-recommendation.risk-high {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.4);
  color: #450a0a;
}

[data-theme="dark"] .tech-recommendation.risk-high {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
  color: #FCA5A5;
  font-weight: 600;
}

/* Estilos para mensaje por defecto cuando no hay información */
.service-tech-info.no-info {
  border: 2px dashed rgba(156, 163, 175, 0.3);
  background: rgba(156, 163, 175, 0.05);
}

.tech-description.default,
.tech-recommendation.default {
  background: rgba(156, 163, 175, 0.1);
  border-color: rgba(156, 163, 175, 0.3);
  color: #6b7280;
  font-style: italic;
}

[data-theme="dark"] .tech-description.default,
[data-theme="dark"] .tech-recommendation.default {
  background: rgba(156, 163, 175, 0.15);
  border-color: rgba(156, 163, 175, 0.4);
  color: #9ca3af;
}

.shield-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  margin-top: 2px;
}

.service-card-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1.25rem;
  background: #FEF3C7;
  color: #92400E;
  font-size: 0.85rem;
  font-weight: 600;
  border-top: 1px solid #FDE68A;
}

.warning-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .services-grid-improved {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
