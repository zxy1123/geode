package org.apache.geode.internal.protocol.flatbuffer

import com.google.flatbuffers.FlatBufferBuilder
import org.apache.geode.internal.protocol.ClientProtocolMessageHandler
import org.apache.geode.internal.protocol.MessageExecutionContext
import org.apache.geode.internal.protocol.flatbuffer.processor.FlatBufferMessageProcessor
import org.apache.geode.internal.protocol.flatbuffer.serializer.FlatBufferProtocolSerializer
import org.apache.geode.internal.protocol.flatbuffer.v1.Message
import java.io.InputStream
import java.io.OutputStream

class FlatbufferClientProtocolMessageHandler : ClientProtocolMessageHandler {
    val flatBufferMessageProcessor = FlatBufferMessageProcessor()
    val flatBufferProtocolSerializer = FlatBufferProtocolSerializer()

    override fun receiveMessage(inputStream: InputStream, outputStream: OutputStream, messageExecutionContext: MessageExecutionContext) {
        val messageReceived = flatBufferProtocolSerializer.deserialize(inputStream).first!!
        val messageBuilder = processMessage(messageReceived, messageExecutionContext)
        sendMessageToStream(outputStream, messageBuilder)
    }

    private fun processMessage(messageReceived: Message, messageExecutionContext: MessageExecutionContext): FlatBufferBuilder {
        return flatBufferMessageProcessor.processMessage(messageReceived, messageExecutionContext,FlatBufferBuilder(0))
    }

    private fun sendMessageToStream(outputStream: OutputStream, messageBuilder: FlatBufferBuilder) {
        flatBufferProtocolSerializer.serialize(Pair(null,messageBuilder),outputStream)
    }
}