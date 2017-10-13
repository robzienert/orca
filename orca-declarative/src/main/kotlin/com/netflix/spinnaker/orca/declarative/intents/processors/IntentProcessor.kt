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

import com.netflix.spinnaker.orca.declarative.Intent
import com.netflix.spinnaker.orca.declarative.IntentMetadata
import com.netflix.spinnaker.orca.declarative.IntentPlan
import com.netflix.spinnaker.orca.declarative.IntentSpec

/**
 * The IntentProcessor is responsible for performing plan and apply operations
 * on a specific Intent.
 *
 * TODO rz - should create CRUD methods instead of plan/apply. Plan and apply
 * feel like higher-level constructs.
 */
interface IntentProcessor<I : Intent<IntentSpec>> {

  fun supports(intent: Intent<IntentSpec>): Boolean

  /**
   * Performs a plan for its configured state. The function should be responsible
   * for both collecting the state, as well as creating the diff of state.
   *
   * TODO rz - Should the plan also take a list of orchestrations so plans can
   * reorder dependencies as necessary?
   */
  fun plan(intent: I, metadata: IntentMetadata): IntentPlan<I>

  /**
   * Applies an IntentPlan. Prior to applying this plan, the current state must
   * be read and the IntentPlan calculated again. If the two plans differ in any
   * way, the function must fail.
   *
   * TODO rz - Thinking that this should be removed entirely. We already have the
   * orchestrations that we'll use to make changes, we should just use those
   * instead of trying to recalc.
   */
//  fun apply(plan: IntentPlan<I>, metadata: IntentMetadata): List<Orchestration>
}
