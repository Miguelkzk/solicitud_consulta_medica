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
public class RevisarAgendaHandler {
    private static final Logger logger = LoggerFactory.getLogger(RevisarAgendaHandler.class);

    @JobWorker(type = "revisar_agenda")
    public void handleRevisarAgenda(final JobClient client,
                                    final ActivatedJob job,
                                    @Variable String especialidad,
                                    @Variable String motivo) throws InterruptedException {
        try {
            logger.info("Revisando agenda para especialidad={}, motivo={}", especialidad, motivo);

            // Simulación de variables de salida
            boolean horarioDisp = true;
            String fechaTurno = "2025-06-10T10:00:00";

            // Simulación de error técnico
            if ("error-tecnico".equals(motivo)) {
                throw new RuntimeException("Simulación de error técnico en revisar agenda");
            }

            // Simulación de error de negocio
            if ("SinTurno".equals(motivo)) {
                horarioDisp = false;
            }

            // Simulación de error técnico: error al parsear la fecha
            try {
                java.time.LocalDateTime.parse(fechaTurno); // Si la fecha es inválida, lanza excepción
            } catch (Exception ex) {
                logger.error("Error técnico al parsear la fecha_turno: {}", fechaTurno);
                client.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage("Error técnico: fecha_turno inválida")
                        .send()
                        .join();
                throw new InterruptedException("Error técnico: fecha_turno inválida");
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("horario_disp", horarioDisp);
            if (fechaTurno != null) {
                variables.put("fecha_turno", fechaTurno);
            }

            if (!horarioDisp) {
                 client.newCompleteCommand(job.getKey())
                        .variables(variables)
                        .send()
                        .join();
                logger.info("No hay turnos disponibles para la especialidad: {}", especialidad);
                return;
            }

            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();

            logger.info("Resultado de agenda: horario_disp={}, fecha_turno={}", horarioDisp, fechaTurno);
        } catch (Exception e) {
            logger.error("Error técnico al revisar agenda", e);
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Error técnico: " + e.getMessage())
                    .send()
                    .join();
            throw new InterruptedException("Error técnico: " + e.getMessage());
        }
    }
}
