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
package com.netflix.spinnaker.orca.declarative

import com.netflix.spinnaker.orca.declarative.exceptions.DeclarativeException
import com.netflix.spinnaker.orca.declarative.intents.processors.IntentProcessor
import com.netflix.spinnaker.orca.pipeline.OrchestrationLauncher
import com.netflix.spinnaker.orca.pipeline.model.Execution
import com.netflix.spinnaker.orca.pipeline.model.Orchestration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class IntentLauncher
@Autowired constructor(
  private val intentProcessors: Set<IntentProcessor<*>>,
  private val orchestrationLauncher: OrchestrationLauncher,
  private val clock: Clock
) {

  private val log = LoggerFactory.getLogger(javaClass)

  fun launch(intent: Intent<IntentSpec>, request: IntentInvocationWrapper): List<Orchestration> {
    val processor = intentProcessor(intent)

    val plan = processor.plan(intent, request.metadata)
    // TODO rz - persist intent, plan
    // TODO rz - generate plan summary

    return mutableListOf<Orchestration>().apply {
      plan.orchestrations.forEach {
        log.info("Intent launching orchestration (kind: ${intent.kind}, intent: ${request.metadata.id}, orchestration: ${it.id}")
        configureOrchestration(it, request.metadata.origin)

        // TODO rz - track orchestration in intentrepository
        if (!request.dryRun) {
          add(orchestrationLauncher.persistAndStart(it))
        }
      }
      // TODO rz - trigger monitor intent orchestration if intent has any children
    }
  }

  private fun configureOrchestration(orchestration: Orchestration, o: String) {
    orchestration.apply {
      buildTime = clock.millis()
      authentication = Execution.AuthenticationDetails.build().orElse(Execution.AuthenticationDetails())
      origin = o
    }
  }

  private fun <I : Intent<IntentSpec>> intentProcessor(intent: I)
    = intentProcessors.find { it.supports(intent) }.let {
        if (it == null) {
          throw DeclarativeException("Could not find processor for intent ${intent.javaClass.simpleName}")
        }
        // TODO rz - GROSS AND WRONG
        return@let it as IntentProcessor<I>
      }
}
