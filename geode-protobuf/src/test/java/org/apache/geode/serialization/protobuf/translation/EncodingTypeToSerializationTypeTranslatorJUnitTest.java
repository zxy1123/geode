package org.apache.geode.serialization.protobuf.translation;

import static org.junit.Assert.assertSame;

import org.apache.geode.protocol.protobuf.BasicTypes;
import org.apache.geode.serialization.SerializationType;
import org.apache.geode.serialization.protobuf.translation.exception.UnsupportedEncodingTypeException;
import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class EncodingTypeToSerializationTypeTranslatorJUnitTest {

  @Test
  public void testTranslateEncodingTypes() throws UnsupportedEncodingTypeException {
    EncodingTypeToSerializationTypeTranslator translator =
        new EncodingTypeToSerializationTypeTranslator();
    assertSame(SerializationType.INT,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.INT));
    assertSame(SerializationType.LONG,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.LONG));
    assertSame(SerializationType.SHORT,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.SHORT));
    assertSame(SerializationType.BYTE,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.BYTE));
    assertSame(SerializationType.BOOLEAN,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.BOOLEAN));
    assertSame(SerializationType.BYTE_BLOB,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.BINARY));
    assertSame(SerializationType.FLOAT,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.FLOAT));
    assertSame(SerializationType.DOUBLE,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.DOUBLE));
    assertSame(SerializationType.STRING,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.STRING));
    assertSame(SerializationType.JSON,
        translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.JSON));
  }

  @Test(expected = UnsupportedEncodingTypeException.class)
  public void testTranslateInvalidEncoding_throwsException()
      throws UnsupportedEncodingTypeException {

    EncodingTypeToSerializationTypeTranslator translator =
        new EncodingTypeToSerializationTypeTranslator();
    translator.getSerializationTypeForEncodingType(BasicTypes.EncodingType.INVALID);
  }

  @Test
  public void testAllEncodingTypeTranslations() {
    EncodingTypeToSerializationTypeTranslator translator =
        new EncodingTypeToSerializationTypeTranslator();
    for (BasicTypes.EncodingType encodingType : BasicTypes.EncodingType.values()) {
      if (!(encodingType.equals(BasicTypes.EncodingType.UNRECOGNIZED) || encodingType
          .equals(BasicTypes.EncodingType.INVALID))) {
        try {
          translator.getSerializationTypeForEncodingType(encodingType);
        } catch (UnsupportedEncodingTypeException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
