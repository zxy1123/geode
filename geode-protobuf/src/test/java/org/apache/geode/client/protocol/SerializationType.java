package org.apache.geode.client.protocol;

import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public enum SerializationType {
  STRING(String.class, new EncodingHandler<String>() {
    @Override
    public String deserialze(byte[] incoming) {
      return new String(incoming, UTF8);
    }

    @Override
    public byte[] serialize(String incoming) {
      return incoming != null ? incoming.getBytes(UTF8) : new byte[0];
    }
  }),
  BYTE_BLOB(byte[].class, new EncodingHandler<byte[]>() {
    @Override
    public byte[] deserialze(byte[] incoming) {
      return incoming;
    }

    @Override
    public byte[] serialize(byte[] incoming) {
      return incoming;
    }
  }),
  INT(int.class, new EncodingHandler<Integer>() {
    @Override
    public Integer deserialze(byte[] incoming) {
      return ByteBuffer.wrap(incoming).getInt();
    }

    @Override
    public byte[] serialize(Integer incoming) {
      return ByteBuffer.allocate(Integer.BYTES).putInt(incoming).array();
    }
  }),
  BYTE(byte.class, new EncodingHandler<Byte>() {
    @Override
    public Byte deserialze(byte[] incoming) {
      return ByteBuffer.wrap(incoming).get();
    }

    @Override
    public byte[] serialize(Byte incoming) {
      return ByteBuffer.allocate(Byte.BYTES).put(incoming).array();
    }
  }),
  SHORT(short.class, new EncodingHandler<Short>() {
    @Override
    public Short deserialze(byte[] incoming) {
      return ByteBuffer.wrap(incoming).getShort();
    }

    @Override
    public byte[] serialize(Short incoming) {
      return ByteBuffer.allocate(Short.BYTES).putShort(incoming).array();
    }
  }),
  LONG(long.class, new EncodingHandler<Long>() {
    @Override
    public Long deserialze(byte[] incoming) {
      return ByteBuffer.wrap(incoming).getLong();
    }

    @Override
    public byte[] serialize(Long incoming) {
      return ByteBuffer.allocate(Long.BYTES).putLong(incoming).array();
    }
  }),
  JSON(PdxInstance.class, new EncodingHandler<PdxInstance>() {
    @Override
    public PdxInstance deserialze(byte[] incoming) {
      return JSONFormatter.fromJSON(incoming);
    }

    @Override
    public byte[] serialize(PdxInstance incoming) {
      return JSONFormatter.toJSONByteArray(incoming);
    }
  });

  private static final Charset UTF8 = Charset.forName("UTF-8");
  public final Class klass;
  public final EncodingHandler encodingHandler;

  <T> SerializationType(Class<T> klass, EncodingHandler<T> encodingHandler) {
    this.klass = klass;
    this.encodingHandler = encodingHandler;
  }
}
