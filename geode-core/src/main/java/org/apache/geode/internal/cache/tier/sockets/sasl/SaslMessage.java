package org.apache.geode.internal.cache.tier.sockets.sasl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class SaslMessage {

  byte[] contents;

  public SaslMessage(byte[] contents) {
    this.contents = contents;
  }

  public SaslMessage(DataInputStream dataInputStream) throws IOException {
    int length = dataInputStream.readInt();
    if (length > 1000000) {
      throw new IllegalStateException("invalid length read from stream");
    }
    this.contents = new byte[length];
    dataInputStream.readFully(this.contents);
  }

  public void writeTo(DataOutputStream dataOutputStream) throws IOException {
    dataOutputStream.writeInt(this.contents.length);
    dataOutputStream.write(this.contents);
  }

  public byte[] getContents() {
    return this.contents;
  }
}
