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
package com.netflix.spinnaker.orca.declarative.summary

import com.netflix.spinnaker.orca.declarative.IntentPlan
import com.netflix.spinnaker.orca.pipeline.model.Orchestration

class DefaultSummaryBuilder : SummaryBuilder {

  override fun build(plan: IntentPlan<*>): Summary {
    return Summary(
      // TODO rz - might be worthwhile to give a plan a name & description
      name = plan.intent.kind,
      description = null,
      operations = collectOperations(plan.orchestrations),
      children = collectChildren(plan.children)
    )
  }

  private fun collectOperations(orchestrations: List<Orchestration>)
    = orchestrations.map { o -> o.stages.map { s -> "${o.name}: ${s.name}" } }.flatten()

  private fun collectChildren(children: List<IntentPlan<*>>)
    = children.map { DefaultSummaryBuilder().build(it) }
}
