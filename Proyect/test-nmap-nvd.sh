#!/bin/bash

# Script de prueba: Nmap + NVD API
# Simula el flujo completo de DiagSEG

TARGET=${1:-scanme.nmap.org}

echo "=========================================="
echo "DiagSEG - Prueba de Flujo Completo"
echo "=========================================="
echo ""
echo "Target: $TARGET"
echo ""

# PASO 1: Escaneo con Nmap
echo "📡 PASO 1: Escaneando con Nmap..."
echo "------------------------------------------"
nmap -sV -p 22,80,443 --open $TARGET -oX /tmp/nmap_scan.xml > /dev/null 2>&1

# Extraer servicios detectados
echo ""
echo "Servicios detectados:"
echo ""

# Parsear el XML línea por línea
grep '<port protocol=' /tmp/nmap_scan.xml | while read -r line; do
    PORT=$(echo "$line" | grep -oP 'portid="\K[^"]*')
    PRODUCT=$(echo "$line" | grep -oP 'product="\K[^"]*' || echo "Unknown")
    VERSION=$(echo "$line" | grep -oP 'version="\K[^"]*' || echo "Unknown")
    CPE=$(echo "$line" | grep -oP '<cpe>\K[^<]*' | head -1)
    
    echo "  Puerto: $PORT"
    echo "    Servicio: $PRODUCT"
    echo "    Versión: $VERSION"
    [ -n "$CPE" ] && echo "    CPE: $CPE"
    echo ""
done

echo "✅ Escaneo completado"
echo ""

# PASO 2: Buscar vulnerabilidades en NVD
echo "🔍 PASO 2: Consultando NVD API..."
echo "------------------------------------------"
echo ""

# Extraer CPE de OpenSSH
OPENSSH_CPE=$(grep -oP '<cpe>cpe:/a:openbsd:openssh:\K[^<]*' /tmp/nmap_scan.xml | head -1)

if [ -n "$OPENSSH_CPE" ]; then
    # Convertir CPE de formato 2.2 a 2.3
    CPE_23="cpe:2.3:a:openbsd:openssh:${OPENSSH_CPE}:*:*:*:*:*:*:*"
    
    echo "Buscando vulnerabilidades para: OpenSSH $OPENSSH_CPE"
    echo "CPE: $CPE_23"
    echo ""
    
    # Consultar NVD API
    RESPONSE=$(curl -s "https://services.nvd.nist.gov/rest/json/cves/2.0?cpeName=${CPE_23}&resultsPerPage=5")
    
    TOTAL=$(echo "$RESPONSE" | jq -r '.totalResults // 0')
    echo "Total de vulnerabilidades encontradas: $TOTAL"
    echo ""
    
    if [ "$TOTAL" -gt 0 ]; then
        echo "Top 5 CVEs:"
        echo "$RESPONSE" | jq -r '.vulnerabilities[]? | "  - \(.cve.id) | Published: \(.cve.published[0:10]) | CVSS: \((.cve.metrics.cvssMetricV31[0].cvssData.baseScore // .cve.metrics.cvssMetricV2[0].cvssData.baseScore // "N/A")) | Severity: \((.cve.metrics.cvssMetricV31[0].cvssData.baseSeverity // .cve.metrics.cvssMetricV2[0].baseSeverity // "N/A"))"'
        echo ""
        
        # Mostrar detalle de la primera vulnerabilidad
        echo "Detalle del CVE más reciente:"
        echo "$RESPONSE" | jq -r '.vulnerabilities[0]? | "  ID: \(.cve.id)\n  Descripción: \(.cve.descriptions[0].value[0:200])..."'
    else
        echo "✅ No se encontraron vulnerabilidades registradas en NVD"
    fi
else
    echo "⚠️  No se encontró OpenSSH en el escaneo"
fi

echo ""

# PASO 3: Buscar vulnerabilidades de Apache
echo "🔍 PASO 3: Consultando vulnerabilidades de Apache..."
echo "------------------------------------------"
echo ""

APACHE_CPE=$(grep -oP '<cpe>cpe:/a:apache:http_server:\K[^<]*' /tmp/nmap_scan.xml | head -1)

if [ -n "$APACHE_CPE" ]; then
    CPE_23="cpe:2.3:a:apache:http_server:${APACHE_CPE}:*:*:*:*:*:*:*"
    
    echo "Buscando vulnerabilidades para: Apache HTTP Server $APACHE_CPE"
    echo "CPE: $CPE_23"
    echo ""
    
    RESPONSE=$(curl -s "https://services.nvd.nist.gov/rest/json/cves/2.0?cpeName=${CPE_23}&resultsPerPage=5")
    
    TOTAL=$(echo "$RESPONSE" | jq -r '.totalResults // 0')
    echo "Total de vulnerabilidades encontradas: $TOTAL"
    echo ""
    
    if [ "$TOTAL" -gt 0 ]; then
        echo "Top 5 CVEs:"
        echo "$RESPONSE" | jq -r '.vulnerabilities[]? | "  - \(.cve.id) | Published: \(.cve.published[0:10]) | CVSS: \((.cve.metrics.cvssMetricV31[0].cvssData.baseScore // .cve.metrics.cvssMetricV2[0].cvssData.baseScore // "N/A")) | Severity: \((.cve.metrics.cvssMetricV31[0].cvssData.baseSeverity // .cve.metrics.cvssMetricV2[0].baseSeverity // "N/A"))"'
    else
        echo "✅ No se encontraron vulnerabilidades registradas en NVD"
    fi
else
    echo "⚠️  No se encontró Apache en el escaneo"
fi

echo ""
echo "=========================================="
echo "✅ Prueba completada exitosamente"
echo "=========================================="
echo ""
echo "Archivos generados:"
echo "  - /tmp/nmap_scan.xml (resultado Nmap completo)"
echo ""
echo "Para ver el XML completo: cat /tmp/nmap_scan.xml"
echo ""
