/*
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.orca.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.netflix.spinnaker.kork.web.exceptions.InvalidRequestException;
import com.netflix.spinnaker.orca.model.EvaluateExpressionRequest;
import com.netflix.spinnaker.orca.pipeline.expressions.ExpressionEvaluationSummary;
import com.netflix.spinnaker.orca.pipeline.model.Execution;
import com.netflix.spinnaker.orca.pipeline.persistence.ExecutionRepository;
import com.netflix.spinnaker.orca.pipeline.util.ContextParameterProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/debug")
public class DebugController {

  private static final TypeReference MAP_OF_MAPS = new TypeReference<Map<String, Object>>() {};
  private static final Logger log = LoggerFactory.getLogger(DebugController.class);

  ExecutionRepository executionRepository;
  ContextParameterProcessor contextParameterProcessor;
  ObjectMapper objectMapper;

  /**
   * Allows testing expressions against executions.
   */
  @RequestMapping(value = "/expression", method = RequestMethod.POST)
  ExpressionEvaluationSummary evaluateExpression(@Valid @RequestBody EvaluateExpressionRequest request) {
    Execution execution = getExecution(request)
      .orElseThrow(() -> new InvalidRequestException("could not get execution"));

    Map<String, Object> result = contextParameterProcessor.process(
      Collections.singletonMap("expression", request.expression),
      objectMapper.convertValue(execution, MAP_OF_MAPS),
      true
    );

    if (!result.containsKey(ContextParameterProcessor.SUMMARY_RESULT_KEY)) {
      log.error("Expression result does not contain a summary: {}", result);
      throw new RuntimeException("expression result does not contain a summary");
    }

    return (ExpressionEvaluationSummary) result.get(ContextParameterProcessor.SUMMARY_RESULT_KEY);
  }

  private Optional<Execution> getExecution(EvaluateExpressionRequest request) {
    if (!Strings.isNullOrEmpty(request.execution)) {
      try {
        return Optional.of(objectMapper.readValue(request.execution, Execution.class));
      } catch (IOException e) {
        throw new InvalidRequestException("execution JSON is invalid", e);
      }
    }
    if (request.executionType != null) {
      return Optional.of(executionRepository.retrieve(request.executionType, request.executionId));
    }
    return Optional.empty();
  }
}
