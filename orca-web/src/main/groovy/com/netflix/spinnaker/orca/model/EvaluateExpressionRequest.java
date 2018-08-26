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
package com.netflix.spinnaker.orca.model;

import com.google.common.base.Strings;
import com.netflix.spinnaker.orca.pipeline.model.Execution;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class EvaluateExpressionRequest {
  /**
   * The expression to be evaluated
   */
  public String expression;

  /**
   * The type of execution retrieved from {@code executionId}. Must be defined with
   * {@code executionId}.
   */
  public Execution.ExecutionType executionType;

  /**
   * A previous execution ID to evaluate the expression against.
   *
   * Cannot be defined with {@code execution}. Must be defined with {@code executionType}.
   */
  public String executionId;

  /**
   * Execution JSON to evaluate the expression against.
   *
   * Cannot be defined with {@code executionId}. Must be valid Execution JSON.
   */
  public String execution;

  public static class RequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
      return EvaluateExpressionRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
      EvaluateExpressionRequest request = (EvaluateExpressionRequest) target;

      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expression", "field.required");
      if (Strings.isNullOrEmpty(request.execution) && Strings.isNullOrEmpty(request.executionId)) {
        errors.reject(
          "executionAndExecutionIdUndefined",
          "one of execution or executionId must be defined"
        );
      }
      if (!Strings.isNullOrEmpty(request.expression) && !Strings.isNullOrEmpty(request.executionId)) {
        errors.reject(
          "executionAndExecutionIdDefined",
          "only one of execution or executionId can be defined"
        );
      }
      if (!Strings.isNullOrEmpty(request.executionId) && request.executionType == null) {
        errors.reject(
          "executionTypeUndefined",
          "execution type must be defined with executionId"
        );
      }
    }
  }
}
