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
package org.apache.geode.protocol.protobuf.operations;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;
import org.apache.geode.protocol.operations.OperationHandler;
import org.apache.geode.protocol.protobuf.BasicTypes;
import org.apache.geode.protocol.protobuf.RegionAPI;
import org.apache.geode.protocol.protobuf.utilities.ProtobufResponseUtilities;
import org.apache.geode.protocol.protobuf.utilities.ProtobufUtilities;
import org.apache.geode.serialization.SerializationService;
import org.apache.geode.serialization.exception.UnsupportedEncodingTypeException;
import org.apache.geode.serialization.registry.exception.CodecNotRegisteredForTypeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetRequestOperationHandler
    implements OperationHandler<RegionAPI.GetRequest, RegionAPI.GetResponse> {
  private static Logger logger = LogManager.getLogger();

  @Override
  public RegionAPI.GetResponse process(SerializationService serializationService,
                                       RegionAPI.GetRequest request, Cache cache) {

    String regionName = request.getRegionName();
    Region region = cache.getRegion(regionName);
    if (region == null) {
      return null;
//      return ProtobufResponseUtilities.createErrorResponse("Region not found");
    }

    try {
      Object decodedKey = ProtobufUtilities.decodeValue(serializationService, request.getKey());
      Object resultValue = region.get(decodedKey);

      if (resultValue == null) {
        return ProtobufResponseUtilities.createNullGetResponse().getGetResponse();
      }

      BasicTypes.EncodedValue encodedValue =
          ProtobufUtilities.createEncodedValue(serializationService, resultValue);
      return ProtobufResponseUtilities.createGetResponse(encodedValue).getGetResponse();
    } catch (UnsupportedEncodingTypeException ex) {
      // can be thrown by encoding or decoding.
      return null;
//      return ProtobufResponseUtilities.createAndLogErrorResponse("Encoding not supported.", logger,
//          ex);
    } catch (CodecNotRegisteredForTypeException ex) {
      return null;
//      return ProtobufResponseUtilities
//          .createAndLogErrorResponse("Codec error in protobuf deserialization.", logger, ex);
    }
  }

}
