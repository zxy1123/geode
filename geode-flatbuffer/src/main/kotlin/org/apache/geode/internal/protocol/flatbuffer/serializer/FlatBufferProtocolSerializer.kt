package org.apache.geode.internal.protocol.flatbuffer.serializer

import com.google.flatbuffers.FlatBufferBuilder
import org.apache.commons.io.IOUtils
import org.apache.geode.internal.protocol.flatbuffer.v1.Message
import org.apache.geode.internal.protocol.protobuf.v1.serializer.ProtocolSerializer
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class FlatBufferProtocolSerializer: ProtocolSerializer<Pair<Message?, FlatBufferBuilder?>> {
    override fun serialize(inputMessage: Pair<Message?, FlatBufferBuilder?>, outputStream: OutputStream) {
        val flatBufferBuilder = inputMessage.second!!
        val endMessage = Message.endMessage(flatBufferBuilder)
        flatBufferBuilder.finish(endMessage)
        val dataBuffer = flatBufferBuilder.dataBuffer()
        IOUtils.writeChunked(dataBuffer.array(), outputStream)
    }

    override fun deserialize(inputStream: InputStream): Pair<Message?, FlatBufferBuilder?> {
        var bytesToRead = ByteArray(4)
        IOUtils.readFully(inputStream, bytesToRead)


        var objectBytes = ByteArray(ByteBuffer.wrap(bytesToRead).getInt())
        IOUtils.readFully(inputStream, objectBytes)

        return Pair(Message.getRootAsMessage(ByteBuffer.wrap(objectBytes)),null)
    }
}