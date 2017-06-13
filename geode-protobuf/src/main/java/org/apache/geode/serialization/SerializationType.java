package org.apache.geode.serialization;

import org.apache.geode.pdx.PdxInstance;

import java.nio.charset.Charset;

public enum SerializationType {
  STRING(String.class),
  BYTE_BLOB(byte[].class),
  INT(int.class),
  BYTE(byte.class),
  SHORT(short.class),
  LONG(long.class),
  JSON(PdxInstance.class);

  private static final Charset UTF8 = Charset.forName("UTF-8");
  public final Class klass;

  SerializationType(Class klass) {
    this.klass = klass;
  }
}
