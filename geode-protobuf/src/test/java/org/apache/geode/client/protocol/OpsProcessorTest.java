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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.ByteString;

import org.apache.geode.protocol.protobuf.BasicTypes;
import org.apache.geode.protocol.protobuf.ClientProtocol;
import org.apache.geode.protocol.protobuf.RegionAPI;
import org.junit.Assert;
import org.junit.Test;

public class OpsProcessorTest {
  @Test
  public void testOpsProcessor() {
    OpsHandlerRegistry registryStub = mock(OpsHandlerRegistry.class);
    OpsHandler operationHandlerStub = mock(OpsHandler.class);
    EncodingHandlerRegistry encodingHandlerRegistry = mock(EncodingHandlerRegistry.class);
    EncodingHandler encodingHandler = mock(EncodingHandler.class);


    ClientProtocol.Request messageRequest = ClientProtocol.Request.newBuilder().build();
    String expectedResponse = "10";
    when(registryStub.getOpsHandler(2)).thenReturn(operationHandlerStub);
    when(operationHandlerStub.process(messageRequest)).thenReturn(expectedResponse);

    OpsProcessor processor = new OpsProcessor(registryStub);
    ClientProtocol.Response response = processor.process(messageRequest);
    Assert.assertEquals(expectedResponse, response.getGetResponse());
  }

  private class OpsProcessor {
    private OpsHandlerRegistry opsHandlerRegistry;
    private EncodingHandler encodingHandler;

    public OpsProcessor(OpsHandlerRegistry opsHandlerRegistry,EncodingHandler encodingHandler) {
      this.opsHandlerRegistry = opsHandlerRegistry;
    }

    public ClientProtocol.Response process(ClientProtocol.Request request) {
      OpsHandler<Object, Object> opsHandler = opsHandlerRegistry.getOpsHandler(2);
      ClientProtocol.Response.Builder responseBuilder = ClientProtocol.Response.newBuilder();
      Object rawResponse = opsHandler.process(request);
      BasicTypes.EncodedValue.Builder encodedValueBuilder = BasicTypes.EncodedValue.newBuilder();
      encodedValueBuilder.setValue(ByteString.copyFrom(rawResponse.getBytes()));
      responseBuilder.setGetResponse(RegionAPI.GetResponse.newBuilder().setResult())
    }
  }
}