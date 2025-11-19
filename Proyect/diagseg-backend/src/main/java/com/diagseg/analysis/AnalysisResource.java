package com.diagseg.analysis;

import com.diagseg.analysis.dto.*;
import com.diagseg.analysis.service.AnalysisService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.validation.Valid;
import org.jboss.logging.Logger;

@Path("/api/analysis")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnalysisResource {

    private static final Logger LOG = Logger.getLogger(AnalysisResource.class);

    @Inject
    AnalysisService analysisService;

    @POST
    @Path("/analyze")
    public AnalysisResult analyze(@Valid AnalysisRequest request) {
        LOG.infof("Recibida solicitud de análisis para: %s (tipo: %s)", request.query, request.type);
        
        try {
            AnalysisResult result = analysisService.analyze(request);
            LOG.infof("Análisis completado exitosamente para: %s", request.query);
            return result;
        } catch (Exception e) {
            LOG.errorf(e, "Error procesando análisis para: %s", request.query);
            throw e;
        }
    }
}
