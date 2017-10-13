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
package com.netflix.spinnaker.orca.declarative.policies

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeName
import com.netflix.spinnaker.orca.declarative.Policy
import com.netflix.spinnaker.orca.declarative.PolicySpec

@JsonTypeName("MaxConsecutiveFailures")
data class MaxConsecutiveFailuresPolicy
@JsonCreator constructor(private val s: MaxConsecutiveFailuresPolicySpec) : Policy<MaxConsecutiveFailuresPolicySpec>(
  kind = "MaxConsecutiveFailures",
  spec = s
)

data class MaxConsecutiveFailuresPolicySpec(
  val failures: Int
) : PolicySpec
