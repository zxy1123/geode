package org.apache.geode;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.container.DockerCluster;

public class DockerTest {

  private DockerCluster cluster;

  @Before
  public void setup() throws Exception {
    cluster = new DockerCluster("testy", 2);
  }

  @After
  public void teardown() throws Exception {
    cluster.stop();
  }

//  @Test
  public void sanity() throws Exception {
    cluster.start();
    assertNotNull("Locator address is null", cluster.getLocatorAddress());
  }

//  @Test
  public void testInvalidGfshCommand() throws Exception {
    String id = cluster.startContainer(0);
    int r = cluster.execCommand(id, new String[] { "/tmp/work/bin/gfsh", "startx" });
    assertEquals(1, r);
  }

  @Test
  public void testCreateRegion() throws Exception {
    cluster.start();
    cluster.gfshCommand("create region --name=FOO --type=REPLICATE");
    cluster.gfshCommand("list regions");
  }

}
