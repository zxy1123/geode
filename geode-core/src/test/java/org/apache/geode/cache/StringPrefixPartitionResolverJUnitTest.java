/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.cache;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.apache.geode.internal.cache.EntryOperationImpl;
import org.apache.geode.test.junit.categories.UnitTest;

@Category(UnitTest.class)
public class StringPrefixPartitionResolverJUnitTest {
  static final String DELIMITER = StringPrefixPartitionResolver.DEFAULT_DELIMITER;

  @Test
  public void testGetName() {
    assertEquals("org.apache.geode.cache.StringPrefixPartitionResolver" + DELIMITER,
        (new StringPrefixPartitionResolver()).getName());
  }

  @Test
  public void testEquals() {
    StringPrefixPartitionResolver pr1 = new StringPrefixPartitionResolver();
    assertEquals(true, pr1.equals(pr1));
    StringPrefixPartitionResolver pr2 = new StringPrefixPartitionResolver();
    assertEquals(true, pr1.equals(pr2));
    assertEquals(false, pr1.equals(new Object()));
  }

  @Test
  public void testNonStringKey() {
    @SuppressWarnings("unchecked")
    EntryOperation<String, Object> eo =
        new EntryOperationImpl(null, null, new Object(), null, null);
    StringPrefixPartitionResolver pr = new StringPrefixPartitionResolver();
    assertThatThrownBy(() -> pr.getRoutingObject(eo)).isInstanceOf(ClassCastException.class);
  }

  @Test
  public void testNoDelimiterKey() {
    @SuppressWarnings("unchecked")
    EntryOperation<String, Object> eo = new EntryOperationImpl(null, null, "foobar", null, null);
    StringPrefixPartitionResolver pr = new StringPrefixPartitionResolver();
    assertThatThrownBy(() -> pr.getRoutingObject(eo)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("The key \"foobar\" does not contains the \"" + DELIMITER + "\" delimiter.");
  }

  @Test
  public void testEmptyPrefix() {
    @SuppressWarnings("unchecked")
    EntryOperation<String, Object> eo =
        new EntryOperationImpl(null, null, DELIMITER + "foobar", null, null);
    StringPrefixPartitionResolver pr = new StringPrefixPartitionResolver();
    assertEquals("", pr.getRoutingObject(eo));
  }

  @Test
  public void testAllPrefix() {
    @SuppressWarnings("unchecked")
    EntryOperation<String, Object> eo =
        new EntryOperationImpl(null, null, "foobar" + DELIMITER, null, null);
    StringPrefixPartitionResolver pr = new StringPrefixPartitionResolver();
    assertEquals("foobar", pr.getRoutingObject(eo));
  }

  @Test
  public void testSimpleKey() {
    @SuppressWarnings("unchecked")
    EntryOperation<String, Object> eo =
        new EntryOperationImpl(null, null, "1" + DELIMITER + "2", null, null);
    StringPrefixPartitionResolver pr = new StringPrefixPartitionResolver();
    assertEquals("1", pr.getRoutingObject(eo));
  }

  @Test
  public void testMulitPrefix() {
    @SuppressWarnings("unchecked")
    EntryOperation<String, Object> eo = new EntryOperationImpl(null, null,
        "one" + DELIMITER + "two" + DELIMITER + "three", null, null);
    StringPrefixPartitionResolver pr = new StringPrefixPartitionResolver();
    assertEquals("one", pr.getRoutingObject(eo));
  }

}
