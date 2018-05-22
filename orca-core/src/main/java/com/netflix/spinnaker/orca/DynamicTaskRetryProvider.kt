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
package com.netflix.spinnaker.orca.pipeline

import com.netflix.spinnaker.kork.dynamicconfig.DynamicConfigSerivce
import com.netflix.spinnaker.orca.Task
import com.netflix.spinnaker.orca.pipeline.model.Stage
import org.springframework.stereotype.Component

interface DynamicTaskRetryProvider {

  fun timeout(task: Task, defaultMs: Long, stage: Stage? = null): Long

  fun backoff(task: Task, defaultMs: Long, stage: Stage? = null): Long
}

@Component
class DefaultDynamicTaskRetryProvider(
  private val dynamicConfigSerivce: DynamicConfigSerivce
) : DynamicTaskRetryProvider {

  override fun timeout(task: Task, defaultMs: Long, stage: Stage?) = configChain("timeout", task, stage, defaultMs)
  override fun backoff(task: Task, defaultMs: Long, stage: Stage?) = configChain("backoff", task, stage, defaultMs)

  private fun configChain(configName: String, task: Task, stage: Stage?, defaultMs: Long): Long {
    if (!dynamicConfigSerivce.isEnabled("dynamicTasks", false)) {
      return defaultMs
    }

    val taskOverride = timeoutConfig("dynamicTasks.$configName.task.${task.javaClass.simpleName}", defaultMs)
    if (taskOverride != null) {
      return taskOverride
    }

    if (stage != null) {
      val stageOverride = timeoutConfig("dynamicTasks.$configName.stage.${stage.type}", defaultMs)
      if (stageOverride != null) {
        return stageOverride
      }
    }

    return timeoutConfig("dynamicTasks.$configName.default", defaultMs)

  }

  private fun timeoutConfig(configName: String, defaultMs: Long) =
    defaultMs.let {
      val timeout = dynamicConfigSerivce.getConfig(Long::class.java, configName, it)
      if (timeout == it) {
        return null
      }
      timeout
    }
}
