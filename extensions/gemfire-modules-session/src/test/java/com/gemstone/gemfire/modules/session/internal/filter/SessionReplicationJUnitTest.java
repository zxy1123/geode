/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.modules.session.internal.filter;

import com.gemstone.gemfire.modules.session.filter.SessionCachingFilter;
import com.gemstone.gemfire.test.junit.categories.UnitTest;
import com.mockrunner.mock.web.MockFilterConfig;
import com.mockrunner.mock.web.WebMockObjectFactory;
import org.junit.Before;
import org.junit.experimental.categories.Category;

/**
 * This runs all tests with a local cache disabled
 */
@Category(UnitTest.class)
public class SessionReplicationJUnitTest extends CommonTests {

  @Before
  public void setUp() throws Exception {
    super.setUp();

    WebMockObjectFactory factory = getWebMockObjectFactory();
    MockFilterConfig config = factory.getMockFilterConfig();

    config.setInitParameter("gemfire.property.mcast-port", "0");
    config.setInitParameter("cache-type", "peer-to-peer");

    factory.getMockServletContext().setContextPath(CONTEXT_PATH);

    factory.getMockRequest().setRequestURL("/test/foo/bar");
    factory.getMockRequest().setContextPath(CONTEXT_PATH);

    createFilter(SessionCachingFilter.class);
    createServlet(CallbackServlet.class);

    setDoChain(true);
  }
}