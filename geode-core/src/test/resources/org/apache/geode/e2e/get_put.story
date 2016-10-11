Scenario: gfsh can start and manage a cluster

Given cluster is started with 1 locator and 3 servers
Given region FOO is created as REPLICATE
When I put 100 entries into region FOO
Then I can get 100 entries from region FOO

Given region BAR is created as PARTITION_REDUNDANT with redundancy 1
When I put 100 entries into region BAR
Then I can get 100 entries from region BAR

Given function org.apache.geode.e2e.FnGetPrimaryBucketSize is deployed
When I call function with id org.apache.geode.e2e.FnGetPrimaryBucketSize on region BAR with argument BAZ it returns 100

Given server 0 is killed
Then I can get 100 entries from region FOO
Then I can get 100 entries from region BAR
