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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.jonpeterson.jackson.module.versioning.JsonSerializeToVersion

/**
 * An Intent represents a new, discrete desired state. An Intent can contain
 * other Intents to be updated at the same time. They are responsible for taking
 * user (or system) inputs via their Specification and generating Orchestrations
 * that will converge the managed system to its desired state.
 *
 * Intents first go through a plan phase which will verify preconditions and
 * validate that the state can even be applied. Once the plan is generated it can
 * optionally be gated for user action, or immediately applied. In both cases, a
 * plan will be re-generated at apply time to verify that the actions have not
 * changed; which can optionally be used to fail the operation if the plans differ,
 * or continue anyway.
 *
 * Intent apply operations must be idempotent.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
abstract class Intent<out S : IntentSpec>
@JsonCreator constructor(
  @JsonSerializeToVersion(defaultToSource = true) val schema: String,
  val kind: String,
  val spec: S
) {

  /**
   * Returns a collection of Intent classes that must be processed before this
   * one during a multi-Intent operation. Dependencies must not be cyclic.
   */
  fun dependsOn(): Set<Class<Intent<*>>> = setOf()
}

/**
 * A typed model of an Intent's configuration.
 */
interface IntentSpec
