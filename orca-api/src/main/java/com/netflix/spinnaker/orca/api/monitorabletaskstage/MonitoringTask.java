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
package com.netflix.spinnaker.orca.api.monitorabletaskstage;

import com.netflix.spinnaker.orca.api.pipeline.RetryableTask;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import java.util.function.*;
import javax.annotation.Nonnull;

public abstract class MonitoringTask implements RetryableTask {

  private MonitorableTaskConfiguration config;

  public MonitoringTask(MonitorableTaskConfiguration config) {
    this.config = config;
  }

  protected abstract Function<StageExecution, TaskResult> getVerifyAction();

  protected abstract BiFunction<StageExecution, RuntimeException, TaskResult> getErrorHandler();

  @Nonnull
  @Override
  public TaskResult execute(@Nonnull StageExecution stage) {
    try {
      return getVerifyAction().apply(stage);
    } catch (RuntimeException e) {
      return getErrorHandler().apply(stage, e);
    }
  }

  @Override
  public long getBackoffPeriod() {
    return config.backoffPeriod.toMillis();
  }

  @Override
  public long getTimeout() {
    return config.timeout.toMillis();
  }
}
