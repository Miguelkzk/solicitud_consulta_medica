package com.obra_social.solicitud_consulta_medica.rest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.camunda.client.CamundaClient;

@RestController
@RequestMapping("/")
public class StartProcesoValidacionController {
  private static final Logger LOG = LoggerFactory.getLogger(StartProcesoValidacionController.class);
   private static final String PROCESS_ID = "Process_0x09901"; // ID del proceso BPMN

  @Autowired private CamundaClient zeebe;

  @PostMapping("/start")
  public void startProcessInstance(@RequestBody Map<String, Object> variables) {

    LOG.info("Iniciando proceso de validacion de paciente " + variables);

    zeebe
        .newCreateInstanceCommand()
        .bpmnProcessId(PROCESS_ID)
        .latestVersion()
        .variables(variables)
        .send();
  }
}
