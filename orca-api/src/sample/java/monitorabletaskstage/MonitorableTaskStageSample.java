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
package monitorabletaskstage;

import com.netflix.spinnaker.kork.plugins.api.PluginSdks;
import com.netflix.spinnaker.orca.api.ClouddriverService;
import com.netflix.spinnaker.orca.api.OrcaSdk;
import com.netflix.spinnaker.orca.api.monitorabletaskstage.MonitorableTaskConfiguration;
import com.netflix.spinnaker.orca.api.monitorabletaskstage.MonitorableTaskStage;
import com.netflix.spinnaker.orca.api.monitorabletaskstage.MonitoringTask;
import com.netflix.spinnaker.orca.api.pipeline.RetryableTask;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import java.time.Duration;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.pf4j.Extension;

@Extension
public class MonitorableTaskStageSample extends MonitorableTaskStage {

  public MonitorableTaskStageSample(PluginSdks pluginSdks) {
    super(pluginSdks);
  }

  @Override
  protected <T extends RetryableTask> Class<T> getActionTask() {
    return null;
  }

  @Override
  protected <T extends MonitoringTask> Class<T> getMonitorTask() {
    return null;
  }

  @Extension
  public static class ActionTask implements RetryableTask {

    private final PluginSdks pluginSdks;

    public ActionTask(PluginSdks pluginSdks) {
      this.pluginSdks = pluginSdks;
    }

    @Override
    public long getBackoffPeriod() {
      return Duration.ofSeconds(5).toMillis();
    }

    @Override
    public long getTimeout() {
      return Duration.ofMinutes(10).toMillis();
    }

    @Nonnull
    @Override
    public TaskResult execute(@Nonnull StageExecution stage) {
      // Pretend there is actually an operation passed here.
      String taskId =
          pluginSdks
              .serviceSdk(OrcaSdk.class)
              .clouddriver()
              .requestOperations("aws", Collections.emptyList());

      return TaskResult.builder(ExecutionStatus.SUCCEEDED)
          .context("clouddriver.taskId", taskId)
          .build();
    }
  }

  @Extension
  public static class MonitorTask extends MonitoringTask {

    private final PluginSdks pluginSdks;

    public MonitorTask(MonitorableTaskConfiguration config, PluginSdks pluginSdks) {
      super(config);
      this.pluginSdks = pluginSdks;
    }

    @Override
    protected Function<StageExecution, TaskResult> getVerifyAction() {
      return stageExecution -> {
        String taskId = (String) stageExecution.getContext().get("clouddriver.taskId");
        if (taskId == null) {
          throw new IllegalStateException("There should be a task ID");
        }

        ClouddriverService clouddriverService = pluginSdks.serviceSdk(OrcaSdk.class).clouddriver();

        ClouddriverService.Task task = clouddriverService.getTask(taskId);

        if (task.getStatus().isCompleted()) {
          return TaskResult.ofStatus(ExecutionStatus.SUCCEEDED);
        } else if (task.getStatus().isFailed()) {
          if (task.getStatus().isRetryable()) {
            clouddriverService.resumeTask(taskId);
          } else {
            return TaskResult.ofStatus(ExecutionStatus.TERMINAL);
          }
        }

        return TaskResult.ofStatus(ExecutionStatus.RUNNING);
      };
    }

    @Override
    protected BiFunction<StageExecution, RuntimeException, TaskResult> getErrorHandler() {
      // This is pretty naive, and not very helpful for end-users.
      return (stageExecution, e) -> TaskResult.ofStatus(ExecutionStatus.TERMINAL);
    }
  }
}
