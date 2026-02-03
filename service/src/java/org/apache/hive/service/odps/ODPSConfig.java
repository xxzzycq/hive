/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hive.service.odps;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for ODPS connection settings.
 */
public class ODPSConfig {
  private static final Logger LOG = LoggerFactory.getLogger(ODPSConfig.class);

  private static final String ODPS_ACCESS_ID_KEY = "odps.access.id";
  private static final String ODPS_ACCESS_KEY_KEY = "odps.access.key";
  private static final String ODPS_ENDPOINT_KEY = "odps.endpoint";
  private static final String ODPS_PROJECT_KEY = "odps.project";
  private static final String ODPS_ENABLED_KEY = "odps.enabled";

  private String accessId;
  private String accessKey;
  private String endpoint;
  private String project;
  private boolean enabled;

  public ODPSConfig(Configuration conf) {
    this.accessId = conf.get(ODPS_ACCESS_ID_KEY, "");
    this.accessKey = conf.get(ODPS_ACCESS_KEY_KEY, "");
    this.endpoint = conf.get(ODPS_ENDPOINT_KEY, "http://service.odps.aliyun.com/api");
    this.project = conf.get(ODPS_PROJECT_KEY, "");
    this.enabled = conf.getBoolean(ODPS_ENABLED_KEY, false);

    if (enabled) {
      validateConfig();
    }
  }

  private void validateConfig() {
    if (accessId == null || accessId.isEmpty()) {
      LOG.warn("ODPS access ID is not configured");
    }
    if (accessKey == null || accessKey.isEmpty()) {
      LOG.warn("ODPS access key is not configured");
    }
    if (project == null || project.isEmpty()) {
      LOG.warn("ODPS project is not configured");
    }
  }

  public String getAccessId() {
    return accessId;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getProject() {
    return project;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isValid() {
    return enabled && 
           accessId != null && !accessId.isEmpty() &&
           accessKey != null && !accessKey.isEmpty() &&
           endpoint != null && !endpoint.isEmpty() &&
           project != null && !project.isEmpty();
  }
}
