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
import java.util.Map;
import static org.junit.Assert.*;

/**
 * Unit tests for ODPSClient.
 */
public class TestODPSClient {

  private ODPSConfig getValidConfig() {
    Configuration conf = new Configuration();
    conf.setBoolean("odps.enabled", true);
    conf.set("odps.access.id", "test-id");
    conf.set("odps.access.key", "test-key");
    conf.set("odps.endpoint", "http://test.endpoint.com");
    conf.set("odps.project", "test-project");
    return new ODPSConfig(conf);
  }

  @Test
  public void testConnect() throws Exception {
    ODPSConfig config = getValidConfig();
    ODPSClient client = new ODPSClient(config);
    
    assertFalse("Client should not be connected initially", client.isConnected());
    
    client.connect();
    
    assertTrue("Client should be connected after connect()", client.isConnected());
    
    client.close();
    
    assertFalse("Client should not be connected after close()", client.isConnected());
  }

  @Test(expected = Exception.class)
  public void testConnectWithInvalidConfig() throws Exception {
    Configuration conf = new Configuration();
    ODPSConfig config = new ODPSConfig(conf);
    ODPSClient client = new ODPSClient(config);
    
    client.connect();
  }

  @Test(expected = Exception.class)
  public void testExecuteQueryWithoutConnect() throws Exception {
    ODPSConfig config = getValidConfig();
    ODPSClient client = new ODPSClient(config);
    
    client.executeQuery("SHOW TABLES");
  }

  @Test
  public void testExecuteShowTablesQuery() throws Exception {
    ODPSConfig config = getValidConfig();
    ODPSClient client = new ODPSClient(config);
    
    client.connect();
    
    Map<String, Object> result = client.executeQuery("SHOW TABLES");
    
    assertNotNull("Result should not be null", result);
    assertEquals("success", result.get("status"));
    assertEquals("SHOW TABLES", result.get("sql"));
    assertNotNull("Result should have rows", result.get("rows"));
    
    client.close();
  }

  @Test
  public void testExecuteSelectQuery() throws Exception {
    ODPSConfig config = getValidConfig();
    ODPSClient client = new ODPSClient(config);
    
    client.connect();
    
    Map<String, Object> result = client.executeQuery("SELECT * FROM users");
    
    assertNotNull("Result should not be null", result);
    assertEquals("success", result.get("status"));
    assertEquals("SELECT * FROM users", result.get("sql"));
    
    client.close();
  }

  @Test
  public void testExecuteCountQuery() throws Exception {
    ODPSConfig config = getValidConfig();
    ODPSClient client = new ODPSClient(config);
    
    client.connect();
    
    Map<String, Object> result = client.executeQuery("SELECT COUNT(*) FROM users");
    
    assertNotNull("Result should not be null", result);
    assertEquals("success", result.get("status"));
    
    client.close();
  }

  @Test
  public void testMultipleQueries() throws Exception {
    ODPSConfig config = getValidConfig();
    ODPSClient client = new ODPSClient(config);
    
    client.connect();
    
    Map<String, Object> result1 = client.executeQuery("SHOW TABLES");
    Map<String, Object> result2 = client.executeQuery("SELECT * FROM users");
    
    assertNotNull("First result should not be null", result1);
    assertNotNull("Second result should not be null", result2);
    
    client.close();
  }
}
