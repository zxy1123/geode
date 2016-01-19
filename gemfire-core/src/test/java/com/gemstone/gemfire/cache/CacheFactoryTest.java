package com.gemstone.gemfire.cache;

import com.gemstone.gemfire.distributed.DistributedSystem;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created by sbawaskar on 1/18/16.
 */
public class CacheFactoryTest {
  @Test
  public void x() {
    DistributedSystem mockDS = mock(DistributedSystem.class);
    CacheFactory cf = new CacheFactory();

  }
}
