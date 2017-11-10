package org.apache.geode.internal.protocol.flatbuffer.serializer

import org.apache.geode.internal.protocol.flatbuffer.v1.EncodingType
import org.apache.geode.internal.protocol.serialization.SerializationService

class FlatBufferSerializationService:SerializationService<EncodingType> {
    override fun decode(encodingTypeValue: EncodingType, value: ByteArray): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun encode(encodingTypeValue: EncodingType, value: Any): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}