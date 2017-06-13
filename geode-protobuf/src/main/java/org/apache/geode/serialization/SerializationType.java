package org.apache.geode.serialization;

import org.apache.geode.pdx.PdxInstance;

import java.nio.charset.Charset;

public enum SerializationType {
  STRING(String.class),
  BINARY(byte[].class),
  INT(int.class),
  BYTE(byte.class),
  SHORT(short.class),
  LONG(long.class),
  JSON(PdxInstance.class),
  BOOLEAN(boolean.class),
  FLOAT(float.class),
  DOUBLE(double.class);

  public final Class klass;

  SerializationType(Class klass) {
    this.klass = klass;
  }
}
