package org.apache.geode.serialization.codec;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.nio.charset.Charset;

@Category(UnitTest.class)
public class StringCodecJUnitTest {
  private static final Charset UTF8 = Charset.forName("UTF-8");
  private static final Charset UTF16 = Charset.forName("UTF-16");
  private String testString = "Test String";

  private StringCodec stringCodec;

  @Before
  public void startup() {
    stringCodec = new StringCodec();
  }

  @Test
  public void testStringEncoding() {
    assertArrayEquals(testString.getBytes(UTF8), stringCodec.encode(testString));
  }

  @Test
  public void testStringIncompatibleEncoding() {
    byte[] expectedEncodedString = stringCodec.encode(testString);
    byte[] incorrectEncodedString = testString.getBytes(UTF16);
    assertNotEquals(expectedEncodedString.length, incorrectEncodedString.length);
  }

  @Test
  public void testStringDecodingWithIncorrectEncodedString() {
    byte[] encodedString = testString.getBytes(UTF16);
    assertNotEquals(testString, stringCodec.decode(encodedString));
  }

  @Test
  public void testStringDecoding() {
    byte[] encodedString = testString.getBytes(UTF8);
    assertEquals(testString, stringCodec.decode(encodedString));
  }
}