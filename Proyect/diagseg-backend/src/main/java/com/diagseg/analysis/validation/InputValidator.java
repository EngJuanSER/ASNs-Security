package com.diagseg.analysis.validation;

import com.diagseg.analysis.dto.AnalysisRequest;
import com.diagseg.analysis.dto.TargetType;
import com.diagseg.analysis.exception.InvalidInputException;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.regex.Pattern;

@ApplicationScoped
public class InputValidator {

    // Regex IPv4 (la del enunciado)
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    // Regex IPv6 (un poco más completa para soportar ::, ::1, fe80::1, etc.)
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^(" +
          "([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +                // 1:2:3:4:5:6:7:8
          "([0-9a-fA-F]{1,4}:){1,7}:|" +                             // 1:: 1:2:3:4:5:6:7::
          "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +             // 1::8, 1:2:3:4:5:6::8
          "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
          "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
          "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
          "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
          "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
          ":((:[0-9a-fA-F]{1,4}){1,7}|:)" +                          // ::, ::1, etc.
        ")$"
    );

    // Regex ASN (la del enunciado)
    private static final Pattern ASN_PATTERN = Pattern.compile(
        "^AS[0-9]{1,10}$"
    );

    public void validate(AnalysisRequest request) {
        if (request == null) {
            throw new InvalidInputException("La solicitud no puede ser nula");
        }

        if (request.query == null || request.query.isBlank()) {
            throw new InvalidInputException("La consulta (query) no puede estar vacía");
        }

        if (request.type == null) {
            throw new InvalidInputException("El tipo de objetivo (type) es obligatorio");
        }

        String q = request.query.trim();

        if (request.type == TargetType.IPV4) {
            if (!IPV4_PATTERN.matcher(q).matches()) {
                throw new InvalidInputException(
                    "La dirección IPv4 '" + q + "' no es válida"
                );
            }
        } else if (request.type == TargetType.IPV6) {
            if (!IPV6_PATTERN.matcher(q).matches()) {
                throw new InvalidInputException(
                    "La dirección IPv6 '" + q + "' no es válida"
                );
            }
        } else if (request.type == TargetType.ASN) {
            if (!ASN_PATTERN.matcher(q).matches()) {
                throw new InvalidInputException(
                    "El ASN '" + q + "' no es válido. Formato esperado: AS#####"
                );
            }
        }
    }
}
