package com.gemstone.gemfire.internal.cache.partitioned.rebalance;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.control.PartitionRebalanceDetailsImpl;
import com.gemstone.gemfire.internal.cache.control.ResourceManagerStats;
import com.gemstone.gemfire.internal.cache.partitioned.rebalance.BucketOperator.Completion;
import com.gemstone.gemfire.internal.cache.partitioned.rebalance.BucketOperatorImpl;
import com.gemstone.gemfire.internal.cache.partitioned.rebalance.BucketOperatorWrapper;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

@Category(UnitTest.class)
public class BucketOperatorWrapperTest {
  
  public BucketOperatorWrapper createBucketOperatorWrapper(BucketOperatorImpl delegate) {
    
    ResourceManagerStats stats = mock(ResourceManagerStats.class);
    doNothing().when(stats).startBucketCreate(anyInt());
    doNothing().when(stats).endBucketCreate(anyInt(), anyBoolean(), anyLong(), anyLong());
    
    Set<PartitionRebalanceDetailsImpl> rebalanceDetails = new HashSet<PartitionRebalanceDetailsImpl>();
    PartitionedRegion region = mock(PartitionedRegion.class);
    
    BucketOperatorWrapper wrapper = new BucketOperatorWrapper(delegate, rebalanceDetails, stats, region);
    
    return wrapper;
  }
  
  @Test
  public void bucketWrapperShouldInvokeOnFailureWhenCreateBucketFails() throws UnknownHostException {
    BucketOperatorImpl delegate = mock(BucketOperatorImpl.class);   
    BucketOperatorWrapper wrapper = createBucketOperatorWrapper(delegate);
        
    Map<String, Long> colocatedRegionBytes = new HashMap<String, Long>();
    int bucketId = 1;
    InternalDistributedMember targetMember = new InternalDistributedMember(InetAddress.getByName("127.0.0.1"), 1);
        
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        //3rd argument is Completion object sent to BucketOperatorImpl.createRedundantBucket
        ((Completion) invocation.getArguments()[3]).onFailure();
        return null;
      }
    }).when(delegate).createRedundantBucket(eq(targetMember), eq(bucketId), eq(colocatedRegionBytes), any(Completion.class));
    
    Completion completionSentToWrapper = mock(Completion.class);
    wrapper.createRedundantBucket(targetMember, bucketId, colocatedRegionBytes, completionSentToWrapper);
    
    verify(completionSentToWrapper, times(1)).onFailure();
  }
  
  @Test
  public void bucketWrapperShouldInvokeOnSuccessWhenCreateBucketFails() throws UnknownHostException {   
    BucketOperatorImpl delegate = mock(BucketOperatorImpl.class);   
    BucketOperatorWrapper wrapper =  createBucketOperatorWrapper(delegate);
    
    Map<String, Long> colocatedRegionBytes = new HashMap<String, Long>();
    int bucketId = 1;
    InternalDistributedMember targetMember = new InternalDistributedMember(InetAddress.getByName("127.0.0.1"), 1);
        
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        //3rd argument is Completion object sent to BucketOperatorImpl.createRedundantBucket
        ((Completion) invocation.getArguments()[3]).onSuccess();
        return null;
      }
    }).when(delegate).createRedundantBucket(eq(targetMember), eq(bucketId), eq(colocatedRegionBytes), any(Completion.class));
    
    Completion completionSentToWrapper = mock(Completion.class);
    wrapper.createRedundantBucket(targetMember, bucketId, colocatedRegionBytes, completionSentToWrapper);
    
    verify(completionSentToWrapper, times(1)).onSuccess();
  }
}
