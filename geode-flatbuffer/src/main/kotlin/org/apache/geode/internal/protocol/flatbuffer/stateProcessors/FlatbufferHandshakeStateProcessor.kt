package org.apache.geode.internal.protocol.flatbuffer.stateProcessors

import org.apache.geode.internal.protocol.MessageExecutionContext
import org.apache.geode.internal.protocol.OperationContext
import org.apache.geode.internal.protocol.state.ConnectionHandshakingStateProcessor
import org.apache.geode.internal.protocol.state.ConnectionStateProcessor
import org.apache.geode.internal.security.SecurityService

class FlatbufferHandshakeStateProcessor(securityService: SecurityService) :ConnectionHandshakingStateProcessor {
    override fun validateOperation(messageContext: MessageExecutionContext, operationContext: OperationContext<*, *, *, *, *>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handshakeSucceeded(): ConnectionStateProcessor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}