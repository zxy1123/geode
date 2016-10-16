/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.security;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.apis.BDDCatchException.caughtException;
import static org.apache.geode.distributed.ConfigurationProperties.LOCATORS;
import static org.apache.geode.distributed.ConfigurationProperties.MCAST_PORT;
import static org.apache.geode.distributed.ConfigurationProperties.SECURITY_MANAGER;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.security.templates.SimpleSecurityManager;
import org.apache.geode.security.templates.UserPasswordAuthInit;
import org.apache.geode.test.dunit.IgnoredException;
import org.apache.geode.test.dunit.internal.JUnit4DistributedTestCase;
import org.apache.geode.test.dunit.rules.LocatorServerStartupRule;
import org.apache.geode.test.junit.categories.DistributedTest;
import org.apache.geode.test.junit.categories.SecurityTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Properties;

@Category({DistributedTest.class, SecurityTest.class})
public class ClientAuthDUnitTest extends JUnit4DistributedTestCase {

  @Rule
  public LocatorServerStartupRule lsRule = new LocatorServerStartupRule();

  @Before
  public void before() throws Exception {
    IgnoredException.addIgnoredException("org.apache.geode.security.AuthenticationFailedException");
    lsRule.getServerVM(0, new Properties() {
      {
        setProperty(SECURITY_MANAGER, SimpleSecurityManager.class.getName());
      }
    });
  }

  @Test
  public void authWithCorrectPasswordShouldPass() {
    ClientCache cache = new ClientCacheFactory(createClientProperties("user", "user"))
        .setPoolSubscriptionEnabled(true).addPoolServer("localhost", lsRule.getPort(0)).create();
  }

  @Test
  public void authWithIncorrectPasswordShouldFail() {
    catchException(new ClientCacheFactory(createClientProperties("user", "wrong"))
        .setPoolSubscriptionEnabled(true).addPoolServer("localhost", lsRule.getPort(0))).create();
    assertThat((Throwable) caughtException()).isInstanceOf(AuthenticationFailedException.class);
  }

  private static Properties createClientProperties(String userName, String password) {
    Properties props = new Properties();
    props.setProperty(UserPasswordAuthInit.USER_NAME, userName);
    props.setProperty(UserPasswordAuthInit.PASSWORD, password);
    // props.setProperty(SECURITY_CLIENT_AUTH_INIT, UserPasswordAuthInit.class.getName());
    props.setProperty(LOCATORS, "");
    props.setProperty(MCAST_PORT, "0");
    return props;
  }
}
