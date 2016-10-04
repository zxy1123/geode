package org.apache.geode.e2e;

import static org.junit.Assert.assertEquals;

import com.spotify.docker.client.exceptions.DockerException;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.e2e.container.DockerCluster;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class GetPutSteps {

  private DockerCluster cluster;

  @BeforeStories
  public void teardownContainers() throws DockerException, InterruptedException {
    DockerCluster.scorch();
  }

  @BeforeScenario
  public void beforeScenario() {
    cluster = new DockerCluster("test-cluster");
  }

  @AfterScenario
  public void afterScenario() throws Exception {
    cluster.stop();
  }

  @Given("cluster is started with $locators locators and $servers servers")
  public void startCluster(int locatorCount, int serverCount) throws Exception {
    cluster.setLocatorCount(locatorCount);
    cluster.setServerCount(serverCount);
    cluster.start();
  }

  @Given("region $name is created as $type")
  public void createRegion(String name, String type) throws Exception {
    cluster.gfshCommand(String.format("create region --name=%s --type=%s", name, type));
  }

  @When("I put $count entries into region $name")
  public void when(int count, String name) throws Exception {
    ClientCache cache = new ClientCacheFactory().
      set("log-level", "warn").
      addPoolLocator("localhost", cluster.getLocatorPort()).
      create();
    Region region = cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(name);
    for (int i = 0; i < count; i++) {
      region.put("key_" + i, "value_" + i);
    }
  }

  @Then("I can get $count entries from region $name")
  public void then(int count, String name) throws Exception {
    Region region = CacheFactory.getAnyInstance().getRegion(name);

    for (int i = 0; i < count; i++) {
      assertEquals("value_" + i, region.get("key_" + i));
    }
  }
}

