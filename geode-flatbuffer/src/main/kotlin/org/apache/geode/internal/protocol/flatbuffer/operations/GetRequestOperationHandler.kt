package org.apache.geode.internal.protocol.flatbuffer.operations

import org.apache.geode.internal.protocol.MessageExecutionContext
import org.apache.geode.internal.protocol.Result
import org.apache.geode.internal.protocol.Success
import org.apache.geode.internal.protocol.flatbuffer.v1.*
import org.apache.geode.internal.protocol.operations.OperationHandler
import org.apache.geode.internal.protocol.serialization.SerializationService

class GetRequestOperationHandler : OperationHandler<GetRequest, GetResponse, ErrorResponse> {
    override fun process(serializationService: SerializationService<*>, request: GetRequest,
                         messageExecutionContext: MessageExecutionContext): Result<GetResponse, ErrorResponse> {
        val regionName = request.regionName()
        val key = getKeyFromRequest(request)

        return Success.of<GetResponse,ErrorResponse>(GetResponse())
    }

    private fun getKeyFromRequest(request: GetRequest): Any {
        when (request.keyType()) {
            EncodedValue.BinaryResult -> return request.key(BinaryResult())
            EncodedValue.BooleanResult -> return request.key(BooleanResult())
            EncodedValue.ByteResult -> return request.key(ByteResult())
            EncodedValue.CustomEncodedValue -> return request.key(CustomEncodedValue())
            EncodedValue.DoubleResult -> return request.key(DoubleResult())
            EncodedValue.FloatResult -> return request.key(FloatResult())
            EncodedValue.IntResult -> return request.key(IntResult())
            EncodedValue.LongResult -> return request.key(LongResult())
            EncodedValue.ShortResult -> return request.key(ShortResult())
            EncodedValue.StringResult -> return request.key(StringResult())
        }
        throw RuntimeException("Incorrect EncodingType for the key")
    }
}