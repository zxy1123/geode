/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.client.protocol;

import org.apache.geode.client.protocol.exception.InvalidProtocolMessageException;
import org.apache.geode.client.protocol.handler.ProtocolHandler;
import org.apache.geode.client.protocol.handler.protobuf.ProtobufProtocolHandler;
import org.apache.geode.protocol.protobuf.ClientProtocol;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

public class ProtobufProtocolHandlerTest {
  private ProtocolHandler protocolHandler;

  @Before
  public void startup() {
    ServiceLoader<ProtocolHandler> serviceLoader = ServiceLoader.load(ProtocolHandler.class);
    for (ProtocolHandler protocolHandler : serviceLoader) {
      if (protocolHandler instanceof ProtobufProtocolHandler) {
        this.protocolHandler = protocolHandler;
      }
    }
  }

  @Test
  public void testDeserializeByteArrayToMessage()
      throws IOException, InvalidProtocolMessageException {
    ClientProtocol.Message expectedRequestMessage = MessageUtil.createGetRequestMessage();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    expectedRequestMessage.writeDelimitedTo(byteArrayOutputStream);
    InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

    ClientProtocol.Message actualMessage = (ClientProtocol.Message) protocolHandler.deserialize(inputStream);
    Assert.assertEquals(expectedRequestMessage, actualMessage);
  }

  @Test
  public void testDeserializeInvalidByteThrowsException() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write("Some incorrect byte array".getBytes());
    InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

    boolean caughtException = false;
    try {
      protocolHandler.deserialize(inputStream);
    } catch (InvalidProtocolMessageException e) {
      caughtException = true;
    }
    Assert.assertTrue(caughtException);
  }

  @Test
  public void testSerializeMessageToByteArray() throws IOException {
    ClientProtocol.Message message = MessageUtil.createGetRequestMessage();
    ByteArrayOutputStream expectedByteArrayOutputStream = new ByteArrayOutputStream();
    message.writeDelimitedTo(expectedByteArrayOutputStream);
    byte[] expectedByteArray = expectedByteArrayOutputStream.toByteArray();

    ByteArrayOutputStream actualByteArrayOutputStream = new ByteArrayOutputStream();
    protocolHandler.serialize(message, actualByteArrayOutputStream);
    Assert.assertArrayEquals(expectedByteArray,actualByteArrayOutputStream.toByteArray());
  }
}
