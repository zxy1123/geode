package com.gemstone.gemfire.internal.cache.partitioned.rebalance;

import java.util.Map;

import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.control.InternalResourceManager;
import com.gemstone.gemfire.internal.cache.partitioned.PartitionedRegionRebalanceOp;

public class BucketOperatorImpl implements BucketOperator {
  
  private PartitionedRegion leaderRegion;
  private boolean isRebalance;
  private boolean replaceOfflineData;

  public BucketOperatorImpl(PartitionedRegion leaderRegion, boolean isRebalance, boolean replaceOfflineData) {
    this.leaderRegion = leaderRegion;
    this.isRebalance = isRebalance;
    this.replaceOfflineData = replaceOfflineData;
  }

  @Override
  public boolean moveBucket(InternalDistributedMember source,
      InternalDistributedMember target, int bucketId,
      Map<String, Long> colocatedRegionBytes) {

    InternalResourceManager.getResourceObserver().movingBucket(
        leaderRegion, bucketId, source, target);
    return PartitionedRegionRebalanceOp.moveBucketForRegion(source, target, bucketId, leaderRegion);
  }

  @Override
  public boolean movePrimary(InternalDistributedMember source,
      InternalDistributedMember target, int bucketId) {

    InternalResourceManager.getResourceObserver().movingPrimary(
        leaderRegion, bucketId, source, target);
    return PartitionedRegionRebalanceOp.movePrimaryBucketForRegion(target, bucketId, leaderRegion, isRebalance); 
  }

  @Override
  public void createRedundantBucket(
      InternalDistributedMember targetMember, int bucketId,
      Map<String, Long> colocatedRegionBytes, Completion completion) {
    boolean result = false;
    try {
      result = PartitionedRegionRebalanceOp.createRedundantBucketForRegion(targetMember, bucketId,
        leaderRegion, isRebalance, replaceOfflineData);
    } finally {
      if(result) {
        completion.onSuccess();
      } else {
        completion.onFailure();
      }
    }
  }
  
  @Override
  public void waitForOperations() {
    //do nothing, all operations are synchronous
  }

  @Override
  public boolean removeBucket(InternalDistributedMember targetMember, int bucketId,
      Map<String, Long> colocatedRegionBytes) {
    return PartitionedRegionRebalanceOp.removeRedundantBucketForRegion(targetMember, bucketId,
        leaderRegion);
  }
}