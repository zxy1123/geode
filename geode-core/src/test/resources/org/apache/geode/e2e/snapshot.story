Scenario: Use gfsh to export and import data into Geode

Given cluster is started with 1 locator and 3 servers
Given region BAR is created as PARTITION_REDUNDANT with redundancy 1
When I put 1000 entries into region BAR
Then I can get 1000 entries from region BAR

When I export region BAR
When I destroy region BAR
Given region BAR is created as PARTITION_REDUNDANT with redundancy 1
Then I can get 0 entries from region BAR
Then I import region BAR
Then I can get 1000 entries from region BAR
