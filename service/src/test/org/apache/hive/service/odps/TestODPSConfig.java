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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ODPSConfig.
 */
public class TestODPSConfig {

  @Test
  public void testDefaultConfiguration() {
    Configuration conf = new Configuration();
    ODPSConfig config = new ODPSConfig(conf);
    
    assertFalse("ODPS should be disabled by default", config.isEnabled());
    assertFalse("Config should not be valid with defaults", config.isValid());
    assertEquals("http://service.odps.aliyun.com/api", config.getEndpoint());
  }

  @Test
  public void testEnabledButIncompleteConfiguration() {
    Configuration conf = new Configuration();
    conf.setBoolean("odps.enabled", true);
    
    ODPSConfig config = new ODPSConfig(conf);
    
    assertTrue("ODPS should be enabled", config.isEnabled());
    assertFalse("Config should not be valid without credentials", config.isValid());
  }

  @Test
  public void testCompleteConfiguration() {
    Configuration conf = new Configuration();
    conf.setBoolean("odps.enabled", true);
    conf.set("odps.access.id", "test-id");
    conf.set("odps.access.key", "test-key");
    conf.set("odps.endpoint", "http://test.endpoint.com");
    conf.set("odps.project", "test-project");
    
    ODPSConfig config = new ODPSConfig(conf);
    
    assertTrue("ODPS should be enabled", config.isEnabled());
    assertTrue("Config should be valid", config.isValid());
    assertEquals("test-id", config.getAccessId());
    assertEquals("test-key", config.getAccessKey());
    assertEquals("http://test.endpoint.com", config.getEndpoint());
    assertEquals("test-project", config.getProject());
  }

  @Test
  public void testDisabledConfiguration() {
    Configuration conf = new Configuration();
    conf.setBoolean("odps.enabled", false);
    conf.set("odps.access.id", "test-id");
    conf.set("odps.access.key", "test-key");
    conf.set("odps.project", "test-project");
    
    ODPSConfig config = new ODPSConfig(conf);
    
    assertFalse("ODPS should be disabled", config.isEnabled());
    assertFalse("Config should not be valid when disabled", config.isValid());
  }

  @Test
  public void testMissingAccessKey() {
    Configuration conf = new Configuration();
    conf.setBoolean("odps.enabled", true);
    conf.set("odps.access.id", "test-id");
    conf.set("odps.project", "test-project");
    
    ODPSConfig config = new ODPSConfig(conf);
    
    assertFalse("Config should not be valid without access key", config.isValid());
  }

  @Test
  public void testMissingProject() {
    Configuration conf = new Configuration();
    conf.setBoolean("odps.enabled", true);
    conf.set("odps.access.id", "test-id");
    conf.set("odps.access.key", "test-key");
    
    ODPSConfig config = new ODPSConfig(conf);
    
    assertFalse("Config should not be valid without project", config.isValid());
  }
}
