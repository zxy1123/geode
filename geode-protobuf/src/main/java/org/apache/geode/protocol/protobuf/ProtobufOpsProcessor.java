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
package org.apache.geode.protocol.protobuf;

import org.apache.geode.cache.Cache;
import org.apache.geode.protocol.operations.OperationHandler;
import org.apache.geode.protocol.protobuf.operations.GetAllRequestOperationHandler;
import org.apache.geode.protocol.protobuf.operations.GetRequestOperationHandler;
import org.apache.geode.protocol.protobuf.operations.PutRequestOperationHandler;
import org.apache.geode.serialization.SerializationService;

import java.util.HashMap;
import java.util.function.Function;

/**
 * This handles protobuf requests by determining the operation type of the request and dispatching
 * it to the appropriate handler.
 */
public class ProtobufOpsProcessor {

  private HashMap<ClientProtocol.Request.RequestAPICase, Blah> operationHandlers = new HashMap<>();
  private final SerializationService serializationService;

  public ProtobufOpsProcessor(SerializationService serializationService) {
    this.serializationService = serializationService;
    addOperationHandlers();
  }

  public ClientProtocol.Response process(ClientProtocol.Request request, Cache cache) {
    ClientProtocol.Request.RequestAPICase requestType = request.getRequestAPICase();
    Blah blah = operationHandlers.get(requestType);

    Object opResponse =
        blah.opH.process(serializationService, blah.fromRequest.apply(request), cache);
    ClientProtocol.Response.Builder builder =
        (ClientProtocol.Response.Builder) blah.toResponse.apply(opResponse);
    return builder.build();
  }

  private void addOperationHandlers(){
//    registry.registerOperationHandlerForOperationId(
//        ClientProtocol.Request.RequestAPICase.GETREQUEST,
//        new GetRequestOperationHandler());
//    registry.registerOperationHandlerForOperationId(
//        ClientProtocol.Request.RequestAPICase.PUTREQUEST,
//        new PutRequestOperationHandler());
//    registry.registerOperationHandlerForOperationId(
//        ClientProtocol.Request.RequestAPICase.GETREGIONNAMESREQUEST,
//        new GetRegionNamesRequestOperationHandler());
//    registry.registerOperationHandlerForOperationId(
//        ClientProtocol.Request.RequestAPICase.GETALLREQUEST,
//        new GetAllRequestOperationHandler());
//    registry.registerOperationHandlerForOperationId(
//        ClientProtocol.Request.RequestAPICase.PUTALLREQUEST,
//        new PutAllRequestOperationHandler());
//    registry.registerOperationHandlerForOperationId(
//        ClientProtocol.Request.RequestAPICase.REMOVEREQUEST,
//        new RemoveRequestOperationHandler());

    operationHandlers.put(ClientProtocol.Request.RequestAPICase.GETALLREQUEST,
        new Blah<>(new GetAllRequestOperationHandler(),
            request -> request.getGetAllRequest(),
            opsResp -> ClientProtocol.Response.newBuilder().setGetAllResponse(opsResp)));

    operationHandlers.put(ClientProtocol.Request.RequestAPICase.PUTREQUEST,
        new Blah<>(new PutRequestOperationHandler(),
            request -> request.getPutRequest(),
            opsResp -> ClientProtocol.Response.newBuilder().setPutResponse(opsResp)));

    operationHandlers.put(ClientProtocol.Request.RequestAPICase.GETREQUEST,
        new Blah<>(new GetRequestOperationHandler(),
            request -> request.getGetRequest(),
            opsResp -> ClientProtocol.Response.newBuilder().setGetResponse(opsResp)));
  }

  //TODO This needs to get a nicer name.
  private class Blah<OperationReq, OpResp> {
    private OperationHandler<OperationReq, OpResp> opH;
    private Function<ClientProtocol.Request, OperationReq> fromRequest;
    private Function<OpResp, ClientProtocol.Response.Builder> toResponse;

    public Blah(OperationHandler<OperationReq, OpResp> opH,
                Function<ClientProtocol.Request, OperationReq> fromRequest,
                Function<OpResp, ClientProtocol.Response.Builder> toResponse) {
      this.opH = opH;
      this.fromRequest = fromRequest;
      this.toResponse = toResponse;
    }
  }
}
