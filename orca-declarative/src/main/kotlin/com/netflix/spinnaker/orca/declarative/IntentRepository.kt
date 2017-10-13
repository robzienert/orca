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

import com.netflix.spinnaker.orca.pipeline.model.Orchestration

interface IntentRepository {

  fun store(intent: Intent<*>)
  fun updateStatus(id: String, status: IntentStatus)
  fun retrieve(id: String): Intent<*>?
  fun retrieveAll(): List<Intent<*>>
  fun retrieveAll(criteria: Criteria): List<Intent<*>>
  fun cancel(id: String)
  fun cancel(id: String, user: String, reason: String)
  fun addOrchestration(id: String, orchestration: Orchestration)

  data class Criteria(
    val limit: Int,
    val statuses: List<IntentStatus>
  )
}
