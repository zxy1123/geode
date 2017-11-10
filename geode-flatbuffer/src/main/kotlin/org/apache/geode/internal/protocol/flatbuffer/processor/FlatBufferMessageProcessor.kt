package org.apache.geode.internal.protocol.flatbuffer.processor

import com.google.flatbuffers.FlatBufferBuilder
import org.apache.geode.internal.protocol.MessageExecutionContext
import org.apache.geode.internal.protocol.Result
import org.apache.geode.internal.protocol.flatbuffer.registry.FlatbufferOperationsRegistry
import org.apache.geode.internal.protocol.flatbuffer.serializer.FlatBufferSerializationService
import org.apache.geode.internal.protocol.flatbuffer.v1.*
import org.apache.geode.internal.protocol.operations.OperationHandler

class FlatBufferMessageProcessor {
    private val operationRegistry = FlatbufferOperationsRegistry()
    private val serializationService = FlatBufferSerializationService()

    fun processMessage(message: Message, messageExecutionContext: MessageExecutionContext, flatBufferBuilder: FlatBufferBuilder): FlatBufferBuilder {
        if( message.messageTypeType() == MessageType.Request) {
            val request = message.messageType(Request()) as Request
            val flatBufferOperationContext = operationRegistry.lookupOperationContext(request)
            val operationHandler = flatBufferOperationContext!!.operationHandler as OperationHandler<GetRequest, GetResponse, ErrorResponse>
            operationHandler.process(serializationService, request.requestApi(GetRequest()) as GetRequest, messageExecutionContext)
        }
        return flatBufferBuilder
    }
}