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
package org.apache.geode.serialization.registry;

import org.apache.geode.serialization.SerializationType;
import org.apache.geode.serialization.TypeCodec;
import org.apache.geode.serialization.registry.exception.CodecAlreadyRegisteredForTypeException;
import org.apache.geode.serialization.registry.exception.CodecNotRegisteredForTypeException;
import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class CodecRegistryJUnitTest {
  @Test
  public void testRegisterCodec() throws CodecAlreadyRegisteredForTypeException {
    SerializationCodecRegistry codecRegistry = new SerializationCodecRegistry();
    Assert.assertEquals(0, codecRegistry.getRegisteredCodecCount());
    codecRegistry.register(SerializationType.INT, new DummyTypeCodec());
    Assert.assertEquals(1, codecRegistry.getRegisteredCodecCount());
  }

  @Test
  public void testRegisteringCodecForRegisteredType_throwsException()
      throws CodecAlreadyRegisteredForTypeException {
    SerializationCodecRegistry codecRegistry = new SerializationCodecRegistry();
    codecRegistry.register(SerializationType.INT, new DummyTypeCodec());

    boolean caughtException = false;
    try {
      codecRegistry.register(SerializationType.INT, new DummyTypeCodec());
    } catch (CodecAlreadyRegisteredForTypeException e) {
      caughtException = true;
    }
    Assert.assertTrue("This was supposed to have thrown a CodecAlreadyRegisteredException",
        caughtException);
  }

  @Test
  public void testGetRegisteredCodec()
      throws CodecAlreadyRegisteredForTypeException, CodecNotRegisteredForTypeException {
    SerializationCodecRegistry codecRegistry = new SerializationCodecRegistry();
    TypeCodec expectedCodec = new DummyTypeCodec();
    codecRegistry.register(SerializationType.INT, expectedCodec);
    Assert.assertEquals(1, codecRegistry.getRegisteredCodecCount());
    TypeCodec codec = codecRegistry.getCodecForType(SerializationType.INT);
    Assert.assertSame(expectedCodec, codec);
  }

  @Test
  public void testGetCodecForUnregisteredType_throwsException() {
    SerializationCodecRegistry codecRegistry = new SerializationCodecRegistry();
    boolean caughtException = false;
    try {
      codecRegistry.getCodecForType(SerializationType.INT);
    } catch (CodecNotRegisteredForTypeException e) {
      caughtException = true;
    }
    Assert.assertTrue("This should have thrown a CodecNotRegisteredForTypeException",
        caughtException);
  }

  class DummyTypeCodec implements TypeCodec {
    @Override
    public Object decode(byte[] incoming) {
      return null;
    }

    @Override
    public byte[] encode(Object incoming) {
      return new byte[0];
    }
  }
}
