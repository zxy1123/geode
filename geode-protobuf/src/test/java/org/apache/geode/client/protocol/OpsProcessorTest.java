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
package org.apache.geode.client.protocol;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.geode.protocol.operations.OperationHandler;
//import org.apache.geode.protocol.operations.ProtobufRequestOperationParser;
import org.apache.geode.protocol.operations.ProtobufRequestOperationParser;
import org.apache.geode.protocol.operations.registry.OperationsHandlerRegistry;
import org.apache.geode.protocol.operations.registry.exception.OperationHandlerNotRegisteredException;
import org.apache.geode.protocol.protobuf.ClientProtocol;
import org.apache.geode.protocol.protobuf.RegionAPI;
import org.apache.geode.serialization.SerializationService;
import org.apache.geode.serialization.registry.exception.CodecNotRegisteredForTypeException;
import org.junit.Assert;
import org.junit.Test;

public class OpsProcessorTest {
  @Test
  public void testOpsProcessor() throws CodecNotRegisteredForTypeException {
    OperationsHandlerRegistry opsHandlerRegistry = mock(OperationsHandlerRegistry.class);
    OperationHandler operationHandlerStub = mock(OperationHandler.class);
    SerializationService serializationService = mock(SerializationService.class);

//    when(serializationService.encode(BasicTypes.EncodingType.STRING,"10"))
//        .thenReturn("10".getBytes(Charset.forName("UTF-8")));

    ClientProtocol.Request messageRequest = ClientProtocol.Request.newBuilder()
        .setGetRequest(RegionAPI.GetRequest.newBuilder()).build();
    RegionAPI.GetResponse expectedResponse = RegionAPI.GetResponse.newBuilder().build();
    try {
      when(opsHandlerRegistry.getOperationHandlerForOperationId(2))
          .thenReturn(operationHandlerStub);
    } catch (OperationHandlerNotRegisteredException e) {
      e.printStackTrace();
    }
    when(operationHandlerStub.process(serializationService,
        ProtobufRequestOperationParser.getRequestForOperationTypeID(messageRequest)))
            .thenReturn(expectedResponse);



    OpsProcessor processor = new OpsProcessor(opsHandlerRegistry, serializationService);
    ClientProtocol.Response response = processor.process(messageRequest);
    Assert.assertEquals(expectedResponse, response.getGetResponse());

  }

  private class OpsProcessor {
    private final OperationsHandlerRegistry opsHandlerRegistry;
    private final SerializationService serializationService;

    public OpsProcessor(OperationsHandlerRegistry opsHandlerRegistry,
                        SerializationService serializationService) {
      this.opsHandlerRegistry = opsHandlerRegistry;
      this.serializationService = serializationService;
    }

    public ClientProtocol.Response process(ClientProtocol.Request request) {
      OperationHandler opsHandler = null;
      try {
        opsHandler = opsHandlerRegistry.getOperationHandlerForOperationId(2);
      } catch (OperationHandlerNotRegisteredException e) {
        e.printStackTrace();
      }

      Object responseMessage = opsHandler.process(serializationService,
          ProtobufRequestOperationParser.getRequestForOperationTypeID(request));
      return ClientProtocol.Response.newBuilder()
          .setGetResponse((RegionAPI.GetResponse) responseMessage).build();
    }
  }
}
