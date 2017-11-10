package org.apache.geode.internal.protocol.flatbuffer.registry

import com.google.flatbuffers.FlatBufferBuilder
import org.apache.geode.internal.protocol.flatbuffer.operations.GetRequestOperationHandler
import org.apache.geode.internal.protocol.flatbuffer.v1.GetRequest
import org.apache.geode.internal.protocol.flatbuffer.v1.GetResponse
import org.apache.geode.internal.protocol.flatbuffer.v1.Request
import org.apache.geode.internal.protocol.flatbuffer.v1.RequestUnion
import org.apache.geode.security.ResourcePermission

class FlatbufferOperationsRegistry {
    val operationRegistry = HashMap<Byte,FlatBufferOperationContext<*,*>>()

    init {
        addContext()
    }


    private fun addContext() {
        TODO("we have to find a nicer way to configure and fix the operations registry")
//        operationRegistry.put(RequestUnion.GetRequest, FlatBufferOperationContext(
//                java.util.function.Function { it.requestApi(GetRequest()) as GetRequest },
//                GetRequestOperationHandler(),
//                java.util.function.Function { FlatBufferBuilder() },
//                ResourcePermission(ResourcePermission.Resource.DATA,
//                ResourcePermission.Operation.READ)))
    }

    fun lookupOperationContext(request: Request): FlatBufferOperationContext<*, *>? {
        return operationRegistry[request.requestApiType()]
    }

}