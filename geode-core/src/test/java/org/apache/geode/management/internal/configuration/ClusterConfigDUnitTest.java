/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.management.internal.configuration;


import static org.apache.geode.distributed.ConfigurationProperties.CLUSTER_CONFIGURATION_DIR;
import static org.apache.geode.distributed.ConfigurationProperties.ENABLE_CLUSTER_CONFIGURATION;
import static org.apache.geode.distributed.ConfigurationProperties.GROUPS;
import static org.apache.geode.distributed.ConfigurationProperties.LOAD_CLUSTER_CONFIGURATION_FROM_DIR;
import static org.apache.geode.distributed.ConfigurationProperties.LOCATORS;
import static org.apache.geode.distributed.ConfigurationProperties.LOG_FILE_SIZE_LIMIT;
import static org.apache.geode.distributed.ConfigurationProperties.USE_CLUSTER_CONFIGURATION;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;
import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.internal.InternalLocator;
import org.apache.geode.distributed.internal.SharedConfiguration;
import org.apache.geode.internal.ClassPathLoader;
import org.apache.geode.internal.JarClassLoader;
import org.apache.geode.management.internal.configuration.domain.Configuration;
import org.apache.geode.management.internal.configuration.utils.ZipUtils;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.internal.JUnit4DistributedTestCase;
import org.apache.geode.test.dunit.rules.LocatorServerStartupRule;
import org.apache.geode.test.junit.categories.DistributedTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

@Category(DistributedTest.class)
public class ClusterConfigDUnitTest extends JUnit4DistributedTestCase {
  // cluster: {maxLogFileSize: 5000, regions: regionForCluster }, group1: { regions:

  private static final ExpectedConfiguration
      EXPECTED_CLUSTER_CONFIG =
      new ExpectedConfiguration("5000",
          new String[]{"regionForCluster"},
          new String[]{"cluster.jar"});
  private static final ExpectedConfiguration
      EXPECTED_GROUP1_CONFIG =
      new ExpectedConfiguration("6000",
          new String[]{"regionForCluster", "regionForGroup1"},
          new String[]{"cluster.jar", "group1.jar"});
  private static final ExpectedConfiguration
      EXPECTED_GROUP2_CONFIG =
      new ExpectedConfiguration("7000",
          new String[]{"regionForCluster", "regionForGroup2"},
          new String[]{"cluster.jar", "group2.jar"});
  private static final ExpectedConfiguration
      EXPECTED_GROUP1and2_CONFIG =
      new ExpectedConfiguration("7000",
          new String[]{"regionForCluster", "regionForGroup1", "regionForGroup2"},
          new String[]{"cluster.jar", "group1.jar", "group2.jar"});

  private static final String[] CONFIG_NAMES = new String[]{"cluster", "group1", "group2"};


  private static class ExpectedConfiguration {
    public String maxLogFileSize;
    public String[] regions;
    public String[] jars;

    public ExpectedConfiguration(String maxLogFileSize, String[] regions, String[] jars) {
      this.maxLogFileSize = maxLogFileSize;
      this.regions = regions;
      this.jars = jars;
    }
  }


  private static final String EXPORTED_CLUSTER_CONFIG_ZIP = "cluster_config.zip";
  private String locatorString = null;

  @Rule
  public LocatorServerStartupRule lsRule = new LocatorServerStartupRule();

  @Before
  // starts up locator-0 with --load-cluster-coniguration-from-dir and verify it's loaded
  public void before() throws Exception {
    File locator0Dir = lsRule.getRootFolder().newFolder("locator-0");
    // unzip the cluster_config.zip into the tempFolder/locator-0
    // the content is tempFolder/locator-0/cluster_config/cluster...
    ZipUtils.unzip(getClass().getResource(EXPORTED_CLUSTER_CONFIG_ZIP).getPath(),
        locator0Dir.getCanonicalPath());

    // start the locator with --load-from-configuration-dir=true
    Properties locator0Props = new Properties();
    locator0Props.setProperty(LOAD_CLUSTER_CONFIGURATION_FROM_DIR, "true");
    locator0Props.setProperty(CLUSTER_CONFIGURATION_DIR,
        locator0Dir.getCanonicalPath());
    locator0Props.setProperty(ENABLE_CLUSTER_CONFIGURATION, "true");
    VM locator0 = lsRule.startLocatorVM(0, locator0Props);
    verifyConfigFilesExistOnLocator(lsRule.getWorkingDir(0));
    locatorString = "localhost[" + lsRule.getPort(0) + "]";

    locator0.invoke(() -> {
      InternalLocator locator = LocatorServerStartupRule.locatorStarter.locator;
      verifyConfigExistInLocatorInternalRegion(locator.getSharedConfiguration());
    });

    verifyConfigFilesExistOnLocator(lsRule.getWorkingDir(0));
  }

  @Test
  // verify another locator starts up and sc is loaded to this locator.
  public void testLocatorStartup() throws IOException {
    // start another locator and verify it gets the same cc as the first one.
    Properties locator1Props = new Properties();
    locator1Props.setProperty(LOCATORS, locatorString);
    locator1Props.setProperty(ENABLE_CLUSTER_CONFIGURATION, "true");
    VM locator1 = lsRule.startLocatorVM(1, locator1Props);

    locator1.invoke(() -> {
      InternalLocator locator = LocatorServerStartupRule.locatorStarter.locator;
      verifyConfigExistInLocatorInternalRegion(locator.getSharedConfiguration());
    });

    verifyConfigFilesExistOnLocator(lsRule.getWorkingDir(1));
  }

  @Test
  // verify another server starts up and sc is loaded to this server
  public void testServerStarup() throws Exception {
    // start the server and verify the sc is applied correctly
    Properties serverProps = new Properties();
    serverProps.setProperty(LOCATORS, locatorString);
    serverProps.setProperty(USE_CLUSTER_CONFIGURATION, "true");
    VM serverWithNoGroup = lsRule.startServerVM(1, serverProps);

    // verify cluster config is applied
    serverWithNoGroup.invoke(() -> this.verifyClusterConfig(EXPECTED_CLUSTER_CONFIG, lsRule.getWorkingDir(1)));

    serverProps.setProperty(GROUPS, "group1");
    VM serverForGroup1 = lsRule.startServerVM(2, serverProps);
    serverForGroup1.invoke(() -> this.verifyClusterConfig(EXPECTED_GROUP1_CONFIG, lsRule.getWorkingDir(2)));

    serverProps.setProperty(GROUPS, "group2");
    VM serverForGroup2 = lsRule.startServerVM(3, serverProps);
    serverForGroup2.invoke(() -> this.verifyClusterConfig(EXPECTED_GROUP2_CONFIG, lsRule.getWorkingDir(3)));
  }

  @Test
  // verify another server starts up and sc is loaded to this server
  public void testServerWithMultipleGroups() throws Exception {
    // start the server and verify the sc is applied correctly
    Properties serverProps = new Properties();
    serverProps.setProperty(LOCATORS, locatorString);
    serverProps.setProperty(USE_CLUSTER_CONFIGURATION, "true");
    serverProps.setProperty(GROUPS, "group1,group2");
    VM serverWithNoGroup = lsRule.startServerVM(1, serverProps);

    serverWithNoGroup.invoke(() -> this.verifyClusterConfig(EXPECTED_GROUP1and2_CONFIG, lsRule.getWorkingDir(1)));
  }

  private void verifyClusterConfig(ExpectedConfiguration expectedConfiguration, File workingDir)
      throws ClassNotFoundException {
    verifyConfigInMemory(expectedConfiguration);
    verifyJarFilesExistOnServer(workingDir, expectedConfiguration.jars);
  }

  private void verifyConfigInMemory(ExpectedConfiguration expectedConfiguration)
      throws ClassNotFoundException {
    Cache cache = LocatorServerStartupRule.serverStarter.cache;
    for (String region : expectedConfiguration.regions) {
      assertThat(cache.getRegion(region)).isNotNull();
    }
    Properties props = cache.getDistributedSystem().getProperties();
    assertThat(props.getProperty(LOG_FILE_SIZE_LIMIT))
        .isEqualTo(expectedConfiguration.maxLogFileSize);

    for (String jar : expectedConfiguration.jars) {
      JarClassLoader jarClassLoader = findJarClassLoader(jar);
      assertThat(jarClassLoader).isNotNull();
      assertThat(jarClassLoader.loadClass(getNameOfClassInJar(jar))).isNotNull();
    }
  }

  private JarClassLoader findJarClassLoader(final String jarName) {
    Collection<ClassLoader> classLoaders = ClassPathLoader.getLatest().getClassLoaders();
    for (ClassLoader classLoader : classLoaders) {
      if (classLoader instanceof JarClassLoader
          && ((JarClassLoader) classLoader).getJarName().equals(jarName)) {
        return (JarClassLoader) classLoader;
      }
    }
    return null;
  }

  private static void verifyConfigExistInLocatorInternalRegion(SharedConfiguration sc) throws Exception {
    for (String configName : CONFIG_NAMES) {
      Configuration config = sc.getConfiguration(configName);
      assertThat(config).isNotNull();
    }
  }

  private void verifyConfigFilesExistOnLocator(File workingDir) {
    File cluster_config_dir = new File(workingDir, "cluster_config");
    assertThat(cluster_config_dir).exists();

    for (String configName : CONFIG_NAMES) {
      File configDir = new File(cluster_config_dir, configName);
      assertThat(configDir).exists();

      File jar = new File(configDir, configName + ".jar");
      File properties = new File(configDir, configName + ".properties");
      File xml = new File(configDir, configName + ".xml");
      assertThat(configDir.listFiles()).contains(jar, properties, xml);
    }
  }

  private void verifyJarFilesExistOnServer(File workingDir, String[] jarNames) {
    assertThat(workingDir.listFiles()).isNotEmpty();

    for (String jarName : jarNames) {
      assertThat(workingDir.listFiles())
          .filteredOn((File file) -> file.getName().contains(jarName))
          .isNotEmpty();
    }
  }

  private String getNameOfClassInJar(String jarName) {
    //We expect e.g. cluster.jar to contain a class named Cluster
    return StringUtils.capitalize(jarName.replace(".jar", ""));
  }
}
