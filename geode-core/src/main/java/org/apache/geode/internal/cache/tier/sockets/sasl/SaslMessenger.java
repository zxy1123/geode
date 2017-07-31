package org.apache.geode.internal.cache.tier.sockets.sasl;

import org.apache.geode.internal.HeapDataOutputStream;
import org.apache.geode.internal.InternalDataSerializer;
import org.apache.geode.internal.util.BlobHelper;

import java.io.*;
import java.net.Socket;

public class SaslMessenger {
  private DataInput inputStream;
  private DataOutput outputStream;

  public SaslMessenger(DataInput inputStream, DataOutput outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  public void sendMessage(byte[] capture) throws IOException {
    //InternalDataSerializer.writeByteArray(capture, outputStream);
    outputStream.writeInt(capture.length);
    outputStream.write(capture);
  }

  public byte[] readMessage() throws IOException {
    //byte[] ret = InternalDataSerializer.readByteArray(inputStream);
    int byteArrayLength = inputStream.readInt();
    byte[] ret = new byte[byteArrayLength];
    inputStream.readFully(ret);
    return ret;
  }
}
