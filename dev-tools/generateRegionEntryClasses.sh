#!/bin/bash
# This script should only be run from the top level build directory (i.e. the one that contains build.xml).
# It reads LeafRegionEntry.cpp, preprocesses it and generates all the leaf classes that subclass AbstractRegionEntry.
# It executes cpp. It has been tested with gnu's cpp on linux and the mac.

SRCDIR=geode-core/src/main/java/org/apache/geode/internal/cache
SRCFILE=$SRCDIR/LeafRegionEntry.cpp

for VERTYPE in VM Versioned
do
  for RETYPE in Thin Stats ThinLRU StatsLRU ThinDisk StatsDisk ThinDiskLRU StatsDiskLRU
  do
    for KEY_INFO in 'ObjectKey KEY_OBJECT' 'IntKey KEY_INT' 'LongKey KEY_LONG' 'UUIDKey KEY_UUID' 'StringKey1 KEY_STRING1' 'StringKey2 KEY_STRING2'
    do
      for MEMTYPE in Heap OffHeap
      do
      declare -a KEY_ARRAY=($KEY_INFO)
      KEY_CLASS=${KEY_ARRAY[0]}
      KEY_TYPE=${KEY_ARRAY[1]}
      BASE=${VERTYPE}${RETYPE}RegionEntry${MEMTYPE}
      OUT=${BASE}${KEY_CLASS}
      WP_ARGS=-Wp,-C,-P,-D${KEY_TYPE},-DPARENT_CLASS=$BASE,-DLEAF_CLASS=$OUT
      if [ "$VERTYPE" = "Versioned" ]; then
        WP_ARGS=${WP_ARGS},-DVERSIONED
      fi
      if [[ "$RETYPE" = *Stats* ]]; then
        WP_ARGS=${WP_ARGS},-DSTATS
      fi
      if [[ "$RETYPE" = *Disk* ]]; then
        WP_ARGS=${WP_ARGS},-DDISK
      fi
      if [[ "$RETYPE" = *LRU* ]]; then
        WP_ARGS=${WP_ARGS},-DLRU
      fi
      if [[ "$MEMTYPE" = "OffHeap" ]]; then
        WP_ARGS=${WP_ARGS},-DOFFHEAP
      fi
      echo generating $SRCDIR/$OUT.java
      cpp -E $WP_ARGS $SRCFILE >$SRCDIR/$OUT.java
      #echo VERTYPE=$VERTYPE RETYPE=$RETYPE $KEY_INFO KEY_CLASS=$KEY_CLASS KEY_TYPE=$KEY_TYPE args=$WP_ARGS 
      done
    done
  done
done
