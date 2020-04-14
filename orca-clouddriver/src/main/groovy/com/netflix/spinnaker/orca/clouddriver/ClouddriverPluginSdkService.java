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
package com.netflix.spinnaker.orca.clouddriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.orca.api.ClouddriverService;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;

public class ClouddriverPluginSdkService implements ClouddriverService {

  private final KatoService katoService;
  private final ObjectMapper objectMapper;

  public ClouddriverPluginSdkService(KatoService katoService, ObjectMapper objectMapper) {
    this.katoService = katoService;
    this.objectMapper = objectMapper;
  }

  @Nonnull
  @Override
  public String requestOperations(
      @Nonnull String cloudProvider,
      @Nonnull Collection<? extends Map<String, Map<String, Object>>> operations) {
    return katoService
        .requestOperations(cloudProvider, (Collection<? extends Map<String, Map>>) operations)
        .toBlocking()
        .first()
        .getId();
  }

  @Override
  public Task getTask(@Nonnull String id) {
    return objectMapper.convertValue(katoService.lookupTask(id), Task.class);
  }

  @Override
  public String resumeTask(@Nonnull String id) {
    return katoService.resumeTask(id).getId();
  }
}
