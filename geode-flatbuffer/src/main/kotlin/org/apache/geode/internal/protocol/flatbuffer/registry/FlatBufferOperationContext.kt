package org.apache.geode.internal.protocol.flatbuffer.registry

import com.google.flatbuffers.FlatBufferBuilder
import org.apache.geode.internal.protocol.OperationContext
import org.apache.geode.internal.protocol.flatbuffer.v1.ErrorResponse
import org.apache.geode.internal.protocol.flatbuffer.v1.Request
import org.apache.geode.internal.protocol.operations.OperationHandler
import org.apache.geode.security.ResourcePermission

class FlatBufferOperationContext<OperationRequest, OperationResponse>(fromRequest: (Request) -> OperationRequest,
                                                                      operationHandler: OperationHandler<OperationRequest, OperationResponse, ErrorResponse>,
                                                                      toResponse: (OperationResponse) -> FlatBufferBuilder,
                                                                      permissionRequired: ResourcePermission)
    : OperationContext<OperationRequest, OperationResponse, ErrorResponse, Request, FlatBufferBuilder>(fromRequest, operationHandler, toResponse, permissionRequired) {

    override fun makeErrorBuilder(errorResponse: ErrorResponse) = FlatBufferBuilder()
}