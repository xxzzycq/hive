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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for NaturalLanguageParser.
 */
public class TestNaturalLanguageParser {

  private NaturalLanguageParser parser = new NaturalLanguageParser();

  @Test
  public void testShowTables() {
    String sql = parser.convertToSQL("show tables");
    assertEquals("SHOW TABLES", sql);
  }

  @Test
  public void testShowTablesInChinese() {
    String sql = parser.convertToSQL("显示表");
    assertEquals("SHOW TABLES", sql);
  }

  @Test
  public void testDescribeTable() {
    String sql = parser.convertToSQL("describe table users");
    assertEquals("DESC users", sql);
  }

  @Test
  public void testDescribeTableInChinese() {
    String sql = parser.convertToSQL("描述表 users");
    assertEquals("DESC users", sql);
  }

  @Test
  public void testSelectAll() {
    String sql = parser.convertToSQL("select all from users");
    assertEquals("SELECT * FROM users LIMIT 100", sql);
  }

  @Test
  public void testSelectAllInChinese() {
    String sql = parser.convertToSQL("查询表 users 的所有数据");
    assertEquals("SELECT * FROM users LIMIT 100", sql);
  }

  @Test
  public void testCountRecords() {
    String sql = parser.convertToSQL("count records in users");
    assertEquals("SELECT COUNT(*) FROM users", sql);
  }

  @Test
  public void testCountRecordsInChinese() {
    String sql = parser.convertToSQL("统计表 users 的记录数");
    assertEquals("SELECT COUNT(*) FROM users", sql);
  }

  @Test
  public void testShowFirstNRows() {
    String sql = parser.convertToSQL("show first 10 rows from users");
    assertEquals("SELECT * FROM users LIMIT 10", sql);
  }

  @Test
  public void testShowFirstNRowsInChinese() {
    String sql = parser.convertToSQL("显示表 users 的前 20 行");
    assertEquals("SELECT * FROM users LIMIT 20", sql);
  }

  @Test
  public void testSQLPassthrough() {
    String sql = parser.convertToSQL("SELECT * FROM users WHERE id = 1");
    assertEquals("SELECT * FROM users WHERE id = 1", sql);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyQuery() {
    parser.convertToSQL("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullQuery() {
    parser.convertToSQL(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnsupportedQuery() {
    parser.convertToSQL("this is not a valid query at all");
  }
}
