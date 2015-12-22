package com.gemstone.gemfire.modules.session;

import com.gemstone.gemfire.modules.session.catalina.Tomcat6DeltaSessionManager;
import com.gemstone.gemfire.test.junit.categories.UnitTest;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

/**
 * @author Jens Deppe
 */
@Category(UnitTest.class)
public class Tomcat6SessionsJUnitTest extends TestSessionsBase {

  // Set up the session manager we need
  @BeforeClass
  public static void setupClass() throws Exception {
    setupServer(new Tomcat6DeltaSessionManager());
  }
}
