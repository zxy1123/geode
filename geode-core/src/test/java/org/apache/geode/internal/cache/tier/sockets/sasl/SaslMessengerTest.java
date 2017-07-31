package org.apache.geode.internal.cache.tier.sockets.sasl;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.apache.geode.internal.ByteArrayDataInput;
import org.apache.geode.internal.HeapDataOutputStream;
import org.apache.geode.internal.InternalDataSerializer;
import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;

@Category(UnitTest.class)
public class SaslMessengerTest {

    private DataInputStream inputStream;
    private HeapDataOutputStream outputStream;
    private SaslMessenger saslMessenger;
    private byte[] message;
    private byte[] outPutArray;

    @Before
    public void setup() {
        message = new byte[]{2, 2};
        outPutArray = new byte[1000];
        outputStream = new HeapDataOutputStream(outPutArray);
        saslMessenger = new SaslMessenger(inputStream, outputStream);
    }

    @Test
    public void sendMessageWritesMessageToStream() throws IOException {
        saslMessenger.sendMessage(message);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
        byte[] ret =InternalDataSerializer.readByteArray(new DataInputStream(byteArrayInputStream));
        assertArrayEquals(message, ret);
    }

    @Test
    public void readMessageReturnsTheNextMessage() throws IOException {
        HeapDataOutputStream tempOutputStream = new HeapDataOutputStream(outPutArray);
        InternalDataSerializer.writeByteArray(message, tempOutputStream);
        byte[] serializedBytes = tempOutputStream.toByteArray();

        inputStream = new DataInputStream(new ByteInputStream(serializedBytes, serializedBytes.length));
        SaslMessenger saslMessenger = new SaslMessenger(inputStream, tempOutputStream);

        byte[] readMessage = saslMessenger.readMessage();
        assertArrayEquals(message, readMessage);
    }

    @Test
    public void readMessageRetunrsOnlyTheNextMessageIfThereAreMultiple() {}
}
