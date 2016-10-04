Scenario: gfsh can start and manage a cluster
Given cluster is started with 1 locators and 2 servers
Given region FOO is created as REPLICATE
When I put 100 entries into region FOO
Then I can get 100 entries from region FOO
