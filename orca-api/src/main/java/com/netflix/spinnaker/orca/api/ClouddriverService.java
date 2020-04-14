/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.orca.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Data;

public interface ClouddriverService {

  @Nonnull
  String requestOperations(
      @Nonnull String cloudProvider,
      @Nonnull Collection<? extends Map<String, Map<String, Object>>> operations);

  Task getTask(@Nonnull String id);

  String resumeTask(@Nonnull String id);

  @Data
  class Task {
    @Nonnull private final String id;

    @Nonnull private final Status status;

    @Nonnull private final List<Map<String, Object>> resultObjects;

    @Nonnull private final List<StatusLine> history;

    @Data
    public static class Status {
      private final boolean completed;
      private final boolean failed;
      private final boolean retryable;
    }

    @Data
    public static class StatusLine {
      private final String phase;
      private final String status;
    }
  }
}
