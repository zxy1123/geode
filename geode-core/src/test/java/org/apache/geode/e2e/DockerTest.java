package org.apache.geode.e2e;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.e2e.container.DockerCluster;

public class DockerTest {

  private DockerCluster cluster;

  @Before
  public void setup() throws Exception {
    cluster = new DockerCluster("testy");
    cluster.setLocatorCount(1);
    cluster.setServerCount(2);
  }

  @After
  public void teardown() throws Exception {
    cluster.stop();
  }

  @Test
  public void sanity() throws Exception {
    cluster.start();
    assertNotNull("Locator host is null", cluster.getLocatorHost());
  }

  @Test
  public void testInvalidGfshCommandReturnsNonZero() throws Exception {
    String id = cluster.startContainer("test-host-1");
    int r = cluster.execCommand(id, false, null, new String[] { "/tmp/work/bin/gfsh", "startx" });
    assertEquals(1, r);
  }

  @Test
  public void testCreateRegion() throws Exception {
    cluster.start();
    cluster.gfshCommand("create region --name=FOO --type=REPLICATE", null);
  }
}
