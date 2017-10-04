/*
 * Copyright 2017 Netflix, Inc.
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
package com.netflix.spinnaker.orca.declarative.intents.processors

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.orca.declarative.Intent
import com.netflix.spinnaker.orca.declarative.IntentMetadata
import com.netflix.spinnaker.orca.declarative.IntentPlan
import com.netflix.spinnaker.orca.declarative.IntentProcessor
import com.netflix.spinnaker.orca.declarative.intents.ApplicationIntent
import com.netflix.spinnaker.orca.front50.Front50Service
import com.netflix.spinnaker.orca.pipeline.StageDefinitionBuilder
import com.netflix.spinnaker.orca.pipeline.model.Orchestration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ApplicationIntentProcessor
@Autowired constructor(
  private val mapper: ObjectMapper,
  private val front50Service: Front50Service
): IntentProcessor<ApplicationIntent> {

  override fun supports(intent: Intent<*>) = intent is ApplicationIntent

  override fun plan(intent: ApplicationIntent, metadata: IntentMetadata): IntentPlan<ApplicationIntent> {
    return IntentPlan(
      intent,
      listOf(Orchestration(metadata.application).apply {
        name = "Update application"
        description = "Converging on external state intent"
        isLimitConcurrent = true
        isKeepWaitingPipelines = true
        origin = metadata.origin
        stages.add(StageDefinitionBuilder.newStage(
          this,
          "upsertApplication",
          null,
          mapper.convertValue(intent.spec, Map::class.java) as Map<String, Any>,
          null,
          null))
      })
    )
  }

  override fun apply(plan: IntentPlan<ApplicationIntent>, metadata: IntentMetadata)
    // TODO rz - perform a plan and verify that state has not changed since plan was generated
    = plan.orchestrations
}
