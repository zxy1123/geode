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
package org.apache.geode.serialization.protobuf.translation;

import org.apache.geode.protocol.protobuf.BasicTypes;
import org.apache.geode.serialization.SerializationType;
import org.apache.geode.serialization.protobuf.translation.exception.UnsupportedEncodingTypeException;

public class EncodingTypeToSerializationTypeTranslator {
  public SerializationType getSerializationTypeForEncodingType(BasicTypes.EncodingType encodingType)
      throws UnsupportedEncodingTypeException {
    switch (encodingType) {
      case INT:
        return SerializationType.INT;
      case BYTE:
        return SerializationType.BYTE;
      case JSON:
        return SerializationType.JSON;
      case LONG:
        return SerializationType.LONG;
      case FLOAT:
        return SerializationType.FLOAT;
      case SHORT:
        return SerializationType.SHORT;
      case BINARY:
        return SerializationType.BINARY;
      case DOUBLE:
        return SerializationType.DOUBLE;
      case STRING:
        return SerializationType.STRING;
      case BOOLEAN:
        return SerializationType.BOOLEAN;
      default:
        throw new UnsupportedEncodingTypeException(
            "No serialization type found for protobuf encoding type: " + encodingType);
    }
  }
}
