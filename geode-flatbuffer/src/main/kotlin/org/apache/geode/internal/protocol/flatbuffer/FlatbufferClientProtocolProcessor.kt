package org.apache.geode.internal.protocol.flatbuffer

import org.apache.geode.cache.Cache
import org.apache.geode.internal.cache.client.protocol.ClientProtocolProcessor
import org.apache.geode.internal.protocol.ClientProtocolMessageHandler
import org.apache.geode.internal.protocol.MessageExecutionContext
import org.apache.geode.internal.protocol.flatbuffer.stateProcessors.FlatbufferHandshakeStateProcessor
import org.apache.geode.internal.protocol.state.ConnectionStateProcessor
import org.apache.geode.internal.protocol.statistics.ProtocolClientStatistics
import org.apache.geode.internal.security.SecurityService
import java.io.InputStream
import java.io.OutputStream

class FlatbufferClientProtocolProcessor(val clientProtocolMessageHandler: ClientProtocolMessageHandler,
                                        val statistics: ProtocolClientStatistics, val cache: Cache,
                                        val securityService: SecurityService) : ClientProtocolProcessor {
    private val messageExecutionContext: MessageExecutionContext
    private val connectionStateProcessor: ConnectionStateProcessor = FlatbufferHandshakeStateProcessor(securityService)

    init {
        messageExecutionContext = MessageExecutionContext(cache, statistics, connectionStateProcessor)
        statistics.clientConnected()
    }

    override fun close() {
        statistics.clientDisconnected()
    }

    override fun processMessage(inputStream: InputStream?, outputStream: OutputStream?) {
        clientProtocolMessageHandler.receiveMessage(inputStream, outputStream, messageExecutionContext);
    }
}