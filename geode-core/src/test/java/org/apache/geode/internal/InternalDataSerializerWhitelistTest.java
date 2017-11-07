package org.apache.geode.internal;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import org.apache.geode.DataSerializer;
import org.apache.geode.codeAnalysis.AnalyzeSerializablesJUnitTest;
import org.apache.geode.distributed.internal.DistributedSystemService;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.distributed.internal.DistributionConfigImpl;

public class InternalDataSerializerWhitelistTest {

  @Test
  public void nonWhitelistedObjectIsRejected() throws Exception {
    List<String> excludedClasses = AnalyzeSerializablesJUnitTest.loadExcludedClasses();
    DistributionConfig distributionConfig = new DistributionConfigImpl(new Properties());
    InternalDataSerializer.initialize(distributionConfig, new ArrayList<DistributedSystemService>());

    for (String filePath: excludedClasses) {
      String className = filePath.replaceAll("/", ".");
      System.out.println("testing class " + className);

      Class excludedClass = Class.forName(className);
      assertTrue(excludedClass.getName() + " is not Serializable and should be removed from excludedClasses.txt",
          Serializable.class.isAssignableFrom(excludedClass));

      if (excludedClass.isEnum()) {
        for (Object instance: excludedClass.getEnumConstants()) {
          serializeAndDeserializeObject(instance);
        }
      } else {
        final Object excludedInstance;
        try {
          excludedInstance = excludedClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
          // okay - it's in the excludedClasses.txt file after all
          // IllegalAccessException means that the constructor is private.
          continue;
        }
        serializeAndDeserializeObject(excludedInstance);
      }
    }
  }

  private void serializeAndDeserializeObject(Object object) throws Exception {
    HeapDataOutputStream outputStream = new HeapDataOutputStream(Version.CURRENT);
    try {
      DataSerializer.writeObject(object, outputStream);
    } catch (IOException e) {
      // some classes, such as BackupLock, are Serializable because the extend something
      // like ReentrantLock but we never serialize them & it doesn't work to try to do so
      System.out.println("Not Serializable: " + object.getClass().getName());
      e.printStackTrace();
      return;
    }
    DataSerializer.readObject(new DataInputStream(new ByteArrayInputStream(outputStream.toByteArray())));
  }
}
