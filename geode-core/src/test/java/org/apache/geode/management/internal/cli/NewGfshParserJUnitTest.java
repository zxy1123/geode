/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.management.internal.cli;

import static org.junit.Assert.assertEquals;

import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

/**
 * GfshParserJUnitTest - Includes tests to check the parsing and auto-completion capabilities of
 * {@link GfshParser}
 */
@Category(UnitTest.class)
public class NewGfshParserJUnitTest {
  @Test
  public void testSplitUserInputDoubleQuotes() {
    String str = "query --query=\"select * from /region\"";
    List<String> tokens = GfshParser.splitUserInput(str);
    assertEquals(3, tokens.size());
    assertEquals("query", tokens.get(0));
    assertEquals("--query", tokens.get(1));
    assertEquals("\"select * from /region\"", tokens.get(2));
  }

  @Test
  public void testSplitUserInputSingleQuotes() {
    String str = "query --query='select * from /region'";
    List<String> tokens = GfshParser.splitUserInput(str);
    assertEquals(3, tokens.size());
    assertEquals("query", tokens.get(0));
    assertEquals("--query", tokens.get(1));
    assertEquals("'select * from /region'", tokens.get(2));
  }

  @Test
  public void testSplitUserInputWithJ() {
    String
        str =
        "start server --name=server1  --J=\"-Dgemfire.start-dev-rest-api=true\" --J='-Dgemfire.http-service-port=8080' --J='-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=30000'";
    List<String> tokens = GfshParser.splitUserInput(str);
    assertEquals(10, tokens.size());
    assertEquals("\"-Dgemfire.start-dev-rest-api=true\"", tokens.get(5));
    assertEquals("'-Dgemfire.http-service-port=8080'", tokens.get(7));
    assertEquals("'-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=30000'",
        tokens.get(9));
  }

  @Test
  public void testSplitUserInputWithJNoQuotes() {
    String
        str =
        "start server --name=server1  --J=-Dgemfire.start-dev-rest-api=true --J=-Dgemfire.http-service-port=8080";
    List<String> tokens = GfshParser.splitUserInput(str);
    assertEquals(8, tokens.size());
    assertEquals("-Dgemfire.start-dev-rest-api=true", tokens.get(5));
    assertEquals("-Dgemfire.http-service-port=8080", tokens.get(7));
  }
}
