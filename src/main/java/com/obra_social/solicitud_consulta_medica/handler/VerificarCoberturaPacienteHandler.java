package com.obra_social.solicitud_consulta_medica.handler;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class VerificarCoberturaPacienteHandler {
    private static final Logger logger = LoggerFactory.getLogger(VerificarCoberturaPacienteHandler.class);

    @JobWorker(type = "verificarCoberturaPaciente")
    public void handleVerificarCoberturaPaciente(final JobClient client,
                                                 final ActivatedJob job,
                                                 @Variable String num_socio,
                                                 @Variable String especialidad) throws InterruptedException {
        try {
            logger.info("Verificando cobertura para num_socio={}, especialidad={}", num_socio, especialidad);

            boolean apto = true;
            String razonRechazo = null;

            // Simulación de reglas de negocio
            if ("Oncología".equalsIgnoreCase(especialidad)) {
                apto = false;
                razonRechazo = "Especialidad no cubierta por el plan";
            }

            if ("999".equals(num_socio)) {
                apto = false;
                razonRechazo = "Socio con deuda pendiente";
            }

            // Crear mapa de variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("apto", apto);
            if (razonRechazo != null) {
                variables.put("razonRechazo", razonRechazo);
            }

            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();

            logger.info("Resultado de cobertura: apto={}, razonRechazo={}", apto, razonRechazo);

        } catch (Exception e) {
            logger.error("Error técnico al verificar cobertura del paciente", e);
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Error técnico: " + e.getMessage())
                    .send()
                    .join();

            throw new InterruptedException("Error técnico: " + e.getMessage());
        }
    }
}
