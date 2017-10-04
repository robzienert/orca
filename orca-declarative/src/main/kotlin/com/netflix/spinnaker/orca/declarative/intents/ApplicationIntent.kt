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
package com.netflix.spinnaker.orca.declarative.intents

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeName
import com.github.jonpeterson.jackson.module.versioning.JsonVersionedModel
import com.netflix.spinnaker.orca.declarative.Intent
import com.netflix.spinnaker.orca.declarative.IntentSpec
import com.netflix.spinnaker.orca.declarative.converter.ApplicationToCurrentConverter

/**
 * The ApplicationIntent is responsible for updating all application configuration preferences.
 */
@JsonTypeName("Application")
@JsonVersionedModel(currentVersion = "1", toCurrentConverterClass = ApplicationToCurrentConverter::class, propertyName = "schema")
class ApplicationIntent
@JsonCreator constructor(spec: ApplicationSpec) : Intent<ApplicationSpec>(
  kind = "Application",
  schema = "1",
  spec = spec
)

data class ApplicationSpec(
  val name: String,
  val description: String,
  val type: String,
  val email: String,
  val repoType: String,
  val repoSlug: String,
  val repoProjectKey: String,
  val owner: String,
  val enableRestartRunningExecutions: Boolean,
  val accounts: Set<String>,
  val appGroup: String,
  val group: String,
  val cloudProviders: Set<String>,
  val requiredGroupMembership: Set<String>,
  val pdApiKey: String,
  val dataSources: ApplicationFeatures,
  val chaosMonkey: ChaosMonkey
) : IntentSpec

data class ApplicationFeatures(
  val enabled: Set<String>,
  val disabled: Set<String>
)

data class ChaosMonkey(
  val enabled: Boolean,
  val meanTimeBetweenKillsInWorkDays: Int,
  val minTimeBetweenKillsInWorkDays: Int,
  val grouping: String,
  val regionsAreIndependent: Boolean,
  val exceptions: List<ChaosMonkeyExceptionRule>
)

data class ChaosMonkeyExceptionRule(
  val region: String,
  val account: String,
  val detail: String,
  val stack: String
)
