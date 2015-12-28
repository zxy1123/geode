
package com.gemstone.gemfire.modules.session.internal.jmx;

/**
 * MXBean interface to retrieve Session statistics
 */
public interface SessionStatisticsMXBean {

  public int getActiveSessions();

  public int getTotalSessions();

  public long getRegionUpdates();
}
