package org.apache.geode.e2e;

import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.ResultSender;
import org.apache.geode.cache.partition.PartitionRegionHelper;

public class FnGetPrimaryBucketSize implements Function, Declarable {

  @Override
  public boolean hasResult() {
    return true;
  }

  @Override
  public void execute(final FunctionContext context) {
    String args = (String) context.getArguments();

    Cache cache = CacheFactory.getAnyInstance();
    Region region = PartitionRegionHelper.getLocalPrimaryData(cache.getRegion(args));
    ResultSender rs = context.getResultSender();
    rs.lastResult(region.size());
  }

  @Override
  public String getId() {
    return "region-size";
  }

  @Override
  public boolean optimizeForWrite() {
    return true;
  }

  @Override
  public boolean isHA() {
    return false;
  }

  @Override
  public void init(final Properties props) {
    // Empty
  }
}
