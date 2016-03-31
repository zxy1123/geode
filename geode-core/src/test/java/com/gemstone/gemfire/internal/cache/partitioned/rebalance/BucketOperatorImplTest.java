package com.gemstone.gemfire.internal.cache.partitioned.rebalance;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.control.InternalResourceManager;
import com.gemstone.gemfire.internal.cache.partitioned.PartitionedRegionRebalanceOp;
import com.gemstone.gemfire.internal.cache.partitioned.rebalance.BucketOperator.Completion;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

@Category(UnitTest.class)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("*.UnitTest")
@PrepareForTest({ InternalResourceManager.class, PartitionedRegionRebalanceOp.class })
public class BucketOperatorImplTest {

  private InternalResourceManager.ResourceObserver resourceObserver;

  private BucketOperatorImpl operator;

  @Mock
  private PartitionedRegion region;
  private boolean isRebalance = true;
  private boolean replaceOfflineData = true;

  private Map<String, Long> colocatedRegionBytes = new HashMap<String, Long>();
  private int bucketId = 1;
  private InternalDistributedMember sourceMember, targetMember;
  @Mock
  Completion completion;

  @Before
  public void setup() throws UnknownHostException {
    resourceObserver = spy(new InternalResourceManager.ResourceObserverAdapter());

    PowerMockito.mockStatic(InternalResourceManager.class);
    when(InternalResourceManager.getResourceObserver()).thenReturn(resourceObserver);

    operator = new BucketOperatorImpl(region, isRebalance, replaceOfflineData);

    sourceMember = new InternalDistributedMember(InetAddress.getByName("127.0.0.1"), 1);
    targetMember = new InternalDistributedMember(InetAddress.getByName("127.0.0.2"), 1);
  }

  @After
  public void after() {
    reset(resourceObserver);
  }

  @Test
  public void moveBucketShouldDelegateToParRegRebalanceOpMoveBucketForRegion() throws UnknownHostException {
    PowerMockito.mockStatic(PartitionedRegionRebalanceOp.class);
    when(PartitionedRegionRebalanceOp.moveBucketForRegion(sourceMember, targetMember, bucketId, region)).thenReturn(true);

    operator.moveBucket(sourceMember, targetMember, bucketId, colocatedRegionBytes);

    verify(resourceObserver, times(1)).movingBucket(region, bucketId, sourceMember, targetMember);

    PowerMockito.verifyStatic(times(1));
    PartitionedRegionRebalanceOp.moveBucketForRegion(sourceMember, targetMember, bucketId, region);
  }

  @Test
  public void movePrimaryShouldDelegateToParRegRebalanceOpMovePrimaryBucketForRegion() throws UnknownHostException {
    PowerMockito.mockStatic(PartitionedRegionRebalanceOp.class);
    when(PartitionedRegionRebalanceOp.movePrimaryBucketForRegion(targetMember, bucketId, region, isRebalance)).thenReturn(true);

    operator.movePrimary(sourceMember, targetMember, bucketId);

    verify(resourceObserver, times(1)).movingPrimary(region, bucketId, sourceMember, targetMember);

    PowerMockito.verifyStatic(times(1));
    PartitionedRegionRebalanceOp.movePrimaryBucketForRegion(targetMember, bucketId, region, isRebalance);
  }

  @Test
  public void createBucketShouldDelegateToParRegRebalanceOpCreateRedundantBucketForRegion() throws UnknownHostException {
    PowerMockito.mockStatic(PartitionedRegionRebalanceOp.class);
    when(PartitionedRegionRebalanceOp.createRedundantBucketForRegion(targetMember, bucketId, region, isRebalance, replaceOfflineData)).thenReturn(true);

    operator.createRedundantBucket(targetMember, bucketId, colocatedRegionBytes, completion);

    PowerMockito.verifyStatic(times(1));
    PartitionedRegionRebalanceOp.createRedundantBucketForRegion(targetMember, bucketId, region, isRebalance, replaceOfflineData);
  }

  @Test
  public void createBucketShouldInvokeOnSuccessIfCreateBucketSucceeds() {
    PowerMockito.mockStatic(PartitionedRegionRebalanceOp.class);
    when(PartitionedRegionRebalanceOp.createRedundantBucketForRegion(targetMember, bucketId, region, isRebalance, replaceOfflineData)).thenReturn(true);

    operator.createRedundantBucket(targetMember, bucketId, colocatedRegionBytes, completion);

    PowerMockito.verifyStatic(times(1));
    PartitionedRegionRebalanceOp.createRedundantBucketForRegion(targetMember, bucketId, region, isRebalance, replaceOfflineData);

    verify(completion, times(1)).onSuccess();
  }

  @Test
  public void createBucketShouldInvokeOnFailureIfCreateBucketFails() {
    PowerMockito.mockStatic(PartitionedRegionRebalanceOp.class);
    when(PartitionedRegionRebalanceOp.createRedundantBucketForRegion(targetMember, bucketId, region, isRebalance, replaceOfflineData)).thenReturn(false); //return false for create fail

    operator.createRedundantBucket(targetMember, bucketId, colocatedRegionBytes, completion);

    PowerMockito.verifyStatic(times(1));
    PartitionedRegionRebalanceOp.createRedundantBucketForRegion(targetMember, bucketId, region, isRebalance, replaceOfflineData);

    verify(completion, times(1)).onFailure();
  }

  @Test
  public void removeBucketShouldDelegateToParRegRebalanceOpRemoveRedundantBucketForRegion() {
    PowerMockito.mockStatic(PartitionedRegionRebalanceOp.class);
    when(PartitionedRegionRebalanceOp.removeRedundantBucketForRegion(targetMember, bucketId, region)).thenReturn(true);

    operator.removeBucket(targetMember, bucketId, colocatedRegionBytes);

    PowerMockito.verifyStatic(times(1));
    PartitionedRegionRebalanceOp.removeRedundantBucketForRegion(targetMember, bucketId, region);
  }

}
