package org.apache.geode.internal.cache.tier.sockets;

import static org.apache.geode.distributed.ConfigurationProperties.LOCATORS;
import static org.apache.geode.distributed.ConfigurationProperties.STATISTIC_ARCHIVE_FILE;
import static org.apache.geode.distributed.ConfigurationProperties.STATISTIC_SAMPLING_ENABLED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.internal.HeapDataOutputStream;
import org.apache.geode.internal.Version;
import org.apache.geode.internal.VersionedDataInputStream;
import org.apache.geode.internal.cache.EventID;
import org.apache.geode.internal.cache.ha.HARegionQueue;
import org.apache.geode.test.dunit.DistributedTestUtils;
import org.apache.geode.test.dunit.Host;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.cache.internal.JUnit4CacheTestCase;
import org.apache.geode.test.dunit.standalone.VersionManager;
import org.apache.geode.test.junit.categories.BackwardCompatibilityTest;
import org.apache.geode.test.junit.categories.ClientServerTest;
import org.apache.geode.test.junit.categories.DistributedTest;

@Category({DistributedTest.class, ClientServerTest.class, BackwardCompatibilityTest.class})
public class ClientServerIDCompatibilityBCDUnitTest extends JUnit4CacheTestCase {

  static final int NUM_OPERATIONS = 10;

  public static final String CLIENT_REGION = "clientRegion";

  @After
  public void cleanup() {
    disconnectAllFromDS();
  }

  @Test
  public void testPassingClientIDAroundSucceeds() throws Exception {
//    List<String> versions = VersionManager.getInstance().getVersionsWithoutCurrent();
    List<String> versions = Arrays.asList(new String[] {"110", "100"});
    System.out.println("VERSIONS ARE " + versions);
    int numVersions = versions.size();
    int numClients = Math.min(versions.size(), 3);

    VM newServer = Host.getHost(0).getVM(0);
    List<VM> clientVMs = new ArrayList(numClients);

    int clientBaseVMIndex = 1;
    List<String> clientVersions = new ArrayList(numClients);
    for (int i=0; i<numClients; i++) {
      System.out.println("getting client for version " + versions.get(i));
      clientVersions.add(versions.get(i));
      clientVMs.add(Host.getHost(0).getVM(clientVersions.get(i), i+clientBaseVMIndex));
    }

//    clientVersions.add(VersionManager.CURRENT_VERSION);
//    clientVMs.add(Host.getHost(0).getVM(clientBaseVMIndex+numClients));
//    numClients++;
    VM newClient = Host.getHost(0).getVM(clientBaseVMIndex+numClients);

    final Properties props = getDistributedSystemProperties();
    // props.put(LOG_LEVEL, "fine");

    int locatorPort = DistributedTestUtils.getDUnitLocatorPort();
    final String locatorSpec = "localhost["+locatorPort+"]";
    props.put(LOCATORS, locatorSpec);
    int newServerPort = newServer.invoke("create server cache", () -> {
          return startCacheServer(props);
        });
    props.remove(LOCATORS);

    for (VM oldClient: clientVMs) {
      startCacheClient(oldClient, props, locatorPort);
    }

    startCacheClient(newClient, props, locatorPort);

    // debugging: give some time after startup so stats will show up correctly
    // in the server.  Otherwise they look like a flatline
    Thread.sleep(2000);

    doCacheOps(clientVMs.get(0));

    VM oldClient = clientVMs.get(1);

//    for (VM oldClient: clientVMs) {
      ClientProxyMembershipID clientId = getClientId(oldClient);
      newServer.invoke("wait for client queue to be created", () -> {
            waitForQueueInServer(clientId);
          });
      newServer.invoke("wait for client queue to drain", () -> {
        waitForQueueToDrain(clientId);
      });
//    }

//    newServer.invoke("wait for client queue to drain", () -> {
//          waitForQueueToDrain(getClientId(newClient));
//        });

    assertValuesArePresent(oldClient);

//    for (VM oldClient: clientVMs) {
      newServer.invoke("compare received and created event IDs in " + newServer, () -> {
        assertSameMembershipIDBytes(getClientId(oldClient), getClientIdBytes(oldClient));
      });
//    }


  }

  private void doCacheOps(VM clientVM) throws Exception {
    clientVM.invoke("doCacheOps in " + clientVM, () -> {
          ClientCache myCache = ClientCacheFactory.getAnyInstance();
          Region region = myCache.getRegion(
              CLIENT_REGION);
          for (int i=0; i<NUM_OPERATIONS; i++) {
            region.put("object_" + i, "value_"+i);
          }
        }
    );
  }

  private void assertValuesArePresent(VM clientVM) {
    clientVM.invoke("assert values are present in " + clientVM, () -> {
      ClientCache myCache = ClientCacheFactory.getAnyInstance();
      Region region = myCache.getRegion(
          CLIENT_REGION);
      for (int i = 0; i < NUM_OPERATIONS; i++) {
        assertTrue(region.containsKey("object_" + i));
      }
    });
  }

  public ClientCache getClientCache() {
    return ClientCacheFactory.getAnyInstance();
  }

  public ClientProxyMembershipID getClientId(VM clientVM) throws Exception {
    // serialVersionUID of the ID class has changed
    byte[] idBytes = clientVM.invoke("fetch client id from " + clientVM, () -> {
      ClientProxyMembershipID id = ClientProxyMembershipID.getClientId(
          getClientCache().getDistributedSystem().getDistributedMember());
      return dataSerializeObject(id);
    });
    return (ClientProxyMembershipID)deDataSerializeObject(idBytes);
  }

  public byte[] getClientIdBytes(VM clientVM) {
    return clientVM.invoke("fetch client id bytes from " + clientVM, () -> { return EventID.getMembershipId(getClientCache().getDistributedSystem()); });
  }

  public void waitForQueueInServer(final ClientProxyMembershipID clientId) throws Exception {
    Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> {
      CacheClientProxy proxy = CacheClientNotifier.getInstance().getClientProxy(clientId);
      if (proxy == null) {
        return false;
      }
      HARegionQueue queue = proxy.getHARegionQueue();
      return queue != null;
    });
  }

  public void waitForQueueToDrain(final ClientProxyMembershipID clientId) throws Exception {
    CacheClientProxy proxy = CacheClientNotifier.getInstance().getClientProxy(clientId);
    HARegionQueue queue = proxy.getHARegionQueue();
    System.out.println("first dump of queue region (eventsRemoved="+queue.getStatistics().getEventsRemoved()+")");
    queue.getRegion().dumpBackingMap();
    try {
      // from Barry:
//      Awaitility.await("wait for queue to drain").atMost(15, TimeUnit.SECONDS)
//          .until(() -> queue.getRegion().size() < NUM_OPERATIONS);

      Awaitility.await("wait for queue to drain").atMost(10, TimeUnit.SECONDS)
          .until(() -> queue.getStatistics().getEventsEnqued() < NUM_OPERATIONS);

    } catch (Throwable t) {
      System.out.println("second dump of queue region (eventsRemoved="+queue.getStatistics().getEventsRemoved()+")");
      queue.getRegion().dumpBackingMap();
      // HAContainerMap doesn't support entrySet so you must modify the class to use the following,
      // though it just shows a lot of wrappers with "no message" and a refCount of 1
//      System.out.println("dump of HA container");
//      for (Iterator<Map.Entry> iterator = CacheClientNotifier.getInstance().getHaContainer().entrySet().iterator();
//           iterator.hasNext(); ) {
//        Map.Entry entry = iterator.next();
//        System.out.format("key=%1s; value=%2s\n", entry.getKey(), entry.getValue());
//      };
      throw t;
    }
  }

  /**
   * dataserialize the Geode version and the given object for transport across RMI
   */
  private byte[] dataSerializeObject(Object obj) throws Exception {
    HeapDataOutputStream stream = new HeapDataOutputStream(256, null);
    Version.CURRENT.writeOrdinal(stream, false);
    DataSerializer.writeObject(obj, stream);
    return stream.toByteArray();
  }

  /**
   * deserialize and return an object serialized by dataSerializeObject()
   */
  private Object deDataSerializeObject(byte[] dataSerializedObject) throws Exception {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dataSerializedObject);
    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
    Version clientVersion = Version.readVersion(dataInputStream, false);
    VersionedDataInputStream inputStream = new VersionedDataInputStream(dataInputStream, clientVersion);
    return DataSerializer.readObject(inputStream);
  }

  private void assertSameMembershipIDBytes(ClientProxyMembershipID clientId, byte[] clientIDBytes) {
    CacheClientProxy proxy = CacheClientNotifier.getInstance().getClientProxy(clientId);
    byte[] proxyIDBytes = EventID.getMembershipId(proxy.getProxyID());
    assertTrue(("expected " + clientIDBytes.length + " bytes but found " + proxyIDBytes.length),
        Arrays.equals(clientIDBytes, proxyIDBytes));
  }

  private void startCacheClient(VM clientVM, final Properties props, final int locatorPort) throws Exception {
    clientVM.invoke("create client cache and register interest", () -> {
      boolean registerInterest = (Version.CURRENT_ORDINAL == Version.GFE_90.ordinal());

      props.put(STATISTIC_SAMPLING_ENABLED, "true");
      props.put(STATISTIC_ARCHIVE_FILE, "clientStatArchive.gfs");

      ClientCacheFactory factory = new ClientCacheFactory(props);
      factory.addPoolLocator("localhost", locatorPort);
      if (registerInterest) {
        factory.setPoolSubscriptionEnabled(true);
        factory.setPoolSubscriptionRedundancy(0);
      }

      ClientCache myCache = factory.create();
      Region region = myCache.createClientRegionFactory(ClientRegionShortcut.CACHING_PROXY).create(
          CLIENT_REGION);
      region.put("clientIsReady", Boolean.TRUE);
      if (registerInterest) {
        region.registerInterest("ALL_KEYS");
      }
    });
  }

  private int startCacheServer(Properties props) throws Exception {
    props.put(STATISTIC_SAMPLING_ENABLED, "true");
    props.put(STATISTIC_ARCHIVE_FILE, "statArchive.gfs");
    Cache cache = new CacheFactory(props).create();
    cache.createRegionFactory(RegionShortcut.REPLICATE).create(CLIENT_REGION);
    CacheServer server = cache.addCacheServer();
    server.setPort(0);
    server.start();
    return server.getPort();
  }



}


