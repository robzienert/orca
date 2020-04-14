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
package com.netflix.spinnaker.orca.api;

import com.netflix.spinnaker.kork.plugins.api.servicesdk.ServiceSdk;
import javax.annotation.Nonnull;

/** Provides plugin SDKs specific to Orca. */
public interface OrcaSdk extends ServiceSdk {

  /** A client for interacting with Clouddriver's cloud operations APIs. */
  @Nonnull
  ClouddriverService clouddriver();
}
