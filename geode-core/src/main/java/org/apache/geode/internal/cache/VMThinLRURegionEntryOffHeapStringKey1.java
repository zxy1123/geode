/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.cache;

// DO NOT modify this class. It was generated from LeafRegionEntry.cpp



import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import org.apache.geode.internal.cache.lru.EnableLRU;

import org.apache.geode.internal.cache.lru.LRUClockNode;
import org.apache.geode.internal.cache.lru.NewLRUClockHand;

import org.apache.geode.internal.offheap.OffHeapRegionEntryHelper;
import org.apache.geode.internal.offheap.annotations.Released;
import org.apache.geode.internal.offheap.annotations.Retained;
import org.apache.geode.internal.offheap.annotations.Unretained;

import org.apache.geode.internal.util.concurrent.CustomEntryConcurrentHashMap.HashEntry;

// macros whose definition changes this class:
// disk: DISK
// lru: 1
// stats: STATS
// versioned: VERSIONED
// offheap: 1
// One of the following key macros must be defined:
// key object: KEY_OBJECT
// key int: KEY_INT
// key long: KEY_LONG
// key uuid: KEY_UUID
// key string1: 1
// key string2: KEY_STRING2

/**
 * Do not modify this class. It was generated. Instead modify LeafRegionEntry.cpp and then run
 * ./dev-tools/generateRegionEntryClasses.sh (it must be run from the top level directory).
 */
public class VMThinLRURegionEntryOffHeapStringKey1 extends VMThinLRURegionEntryOffHeap {
  public VMThinLRURegionEntryOffHeapStringKey1(RegionEntryContext context, String key,

      @Retained

      Object value

      , boolean byteEncode

  ) {
    super(context,



        value

    );
    // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

    // caller has already confirmed that key.length <= MAX_INLINE_STRING_KEY
    long tmpBits1 = 0L;
    if (byteEncode) {
      for (int i = key.length() - 1; i >= 0; i--) {
        // Note: we know each byte is <= 0x7f so the "& 0xff" is not needed. But I added it in to
        // keep findbugs happy.
        tmpBits1 |= (byte) key.charAt(i) & 0xff;
        tmpBits1 <<= 8;
      }
      tmpBits1 |= 1 << 6;
    } else {
      for (int i = key.length() - 1; i >= 0; i--) {
        tmpBits1 |= key.charAt(i);
        tmpBits1 <<= 16;
      }
    }
    tmpBits1 |= key.length();
    this.bits1 = tmpBits1;

  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  // common code
  protected int hash;
  private HashEntry<Object, Object> next;
  @SuppressWarnings("unused")
  private volatile long lastModified;
  private static final AtomicLongFieldUpdater<VMThinLRURegionEntryOffHeapStringKey1> lastModifiedUpdater =
      AtomicLongFieldUpdater.newUpdater(VMThinLRURegionEntryOffHeapStringKey1.class,
          "lastModified");

  /**
   * All access done using ohAddrUpdater so it is used even though the compiler can not tell it is.
   */
  @SuppressWarnings("unused")
  @Retained
  @Released
  private volatile long ohAddress;
  /**
   * I needed to add this because I wanted clear to call setValue which normally can only be called
   * while the re is synced. But if I sync in that code it causes a lock ordering deadlock with the
   * disk regions because they also get a rw lock in clear. Some hardware platforms do not support
   * CAS on a long. If gemfire is run on one of those the AtomicLongFieldUpdater does a sync on the
   * re and we will once again be deadlocked. I don't know if we support any of the hardware
   * platforms that do not have a 64bit CAS. If we do then we can expect deadlocks on disk regions.
   */
  private final static AtomicLongFieldUpdater<VMThinLRURegionEntryOffHeapStringKey1> ohAddrUpdater =
      AtomicLongFieldUpdater.newUpdater(VMThinLRURegionEntryOffHeapStringKey1.class, "ohAddress");

  @Override
  public Token getValueAsToken() {
    return OffHeapRegionEntryHelper.getValueAsToken(this);
  }

  @Override
  protected Object getValueField() {
    return OffHeapRegionEntryHelper._getValue(this);
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp
  @Override

  @Unretained
  protected void setValueField(@Unretained Object v) {



    OffHeapRegionEntryHelper.setValue(this, v);
  }

  @Override

  @Retained

  public Object _getValueRetain(RegionEntryContext context, boolean decompress) {
    return OffHeapRegionEntryHelper._getValueRetain(this, decompress, context);
  }

  @Override
  public long getAddress() {
    return ohAddrUpdater.get(this);
  }

  @Override
  public boolean setAddress(long expectedAddr, long newAddr) {
    return ohAddrUpdater.compareAndSet(this, expectedAddr, newAddr);
  }

  @Override

  @Released

  public void release() {
    OffHeapRegionEntryHelper.releaseEntry(this);
  }

  @Override
  public void returnToPool() {
    // Deadcoded for now; never was working
    // if (this instanceof VMThinRegionEntryLongKey) {
    // factory.returnToPool((VMThinRegionEntryLongKey)this);
    // }
  }

  protected long getLastModifiedField() {
    return lastModifiedUpdater.get(this);
  }

  protected boolean compareAndSetLastModifiedField(long expectedValue, long newValue) {
    return lastModifiedUpdater.compareAndSet(this, expectedValue, newValue);
  }

  /**
   * @see HashEntry#getEntryHash()
   */
  public int getEntryHash() {
    return this.hash;
  }

  protected void setEntryHash(int v) {
    this.hash = v;
  }

  /**
   * @see HashEntry#getNextEntry()
   */
  public HashEntry<Object, Object> getNextEntry() {
    return this.next;
  }

  /**
   * @see HashEntry#setNextEntry
   */
  public void setNextEntry(final HashEntry<Object, Object> n) {
    this.next = n;
  }



  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  // lru code
  @Override
  public void setDelayedDiskId(LocalRegion r) {



    // nothing needed for LRUs with no disk

  }

  public synchronized int updateEntrySize(EnableLRU capacityController) {
    return updateEntrySize(capacityController, _getValue()); // OFHEAP: _getValue ok w/o incing
                                                             // refcount because we are synced and
                                                             // only getting the size
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  public synchronized int updateEntrySize(EnableLRU capacityController, Object value) {
    int oldSize = getEntrySize();
    int newSize = capacityController.entrySize(getKeyForSizing(), value);
    setEntrySize(newSize);
    int delta = newSize - oldSize;
    return delta;
  }

  public boolean testRecentlyUsed() {
    return areAnyBitsSet(RECENTLY_USED);
  }

  @Override
  public void setRecentlyUsed() {
    setBits(RECENTLY_USED);
  }

  public void unsetRecentlyUsed() {
    clearBits(~RECENTLY_USED);
  }

  public boolean testEvicted() {
    return areAnyBitsSet(EVICTED);
  }

  public void setEvicted() {
    setBits(EVICTED);
  }

  public void unsetEvicted() {
    clearBits(~EVICTED);
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  private LRUClockNode nextLRU;
  private LRUClockNode prevLRU;
  private int size;

  public void setNextLRUNode(LRUClockNode next) {
    this.nextLRU = next;
  }

  public LRUClockNode nextLRUNode() {
    return this.nextLRU;
  }

  public void setPrevLRUNode(LRUClockNode prev) {
    this.prevLRU = prev;
  }

  public LRUClockNode prevLRUNode() {
    return this.prevLRU;
  }

  public int getEntrySize() {
    return this.size;
  }

  protected void setEntrySize(int size) {
    this.size = size;
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  @Override
  public Object getKeyForSizing() {



    // inline keys always report null for sizing since the size comes from the entry size
    return null;

  }



  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  // key code

  private final long bits1;

  private int getKeyLength() {
    return (int) (this.bits1 & 0x003fL);
  }

  private int getEncoding() {
    // 0 means encoded as char
    // 1 means encoded as bytes that are all <= 0x7f;
    return (int) (this.bits1 >> 6) & 0x03;
  }

  @Override
  public Object getKey() {
    int keylen = getKeyLength();
    char[] chars = new char[keylen];
    long tmpBits1 = this.bits1;
    if (getEncoding() == 1) {
      for (int i = 0; i < keylen; i++) {
        tmpBits1 >>= 8;
        chars[i] = (char) (tmpBits1 & 0x00ff);
      }
    } else {
      for (int i = 0; i < keylen; i++) {
        tmpBits1 >>= 16;
        chars[i] = (char) (tmpBits1 & 0x00FFff);
      }
    }
    return new String(chars);
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  @Override
  public boolean isKeyEqual(Object k) {
    if (k instanceof String) {
      String str = (String) k;
      int keylen = getKeyLength();
      if (str.length() == keylen) {
        long tmpBits1 = this.bits1;
        if (getEncoding() == 1) {
          for (int i = 0; i < keylen; i++) {
            tmpBits1 >>= 8;
            char c = (char) (tmpBits1 & 0x00ff);
            if (str.charAt(i) != c) {
              return false;
            }
          }
        } else {
          for (int i = 0; i < keylen; i++) {
            tmpBits1 >>= 16;
            char c = (char) (tmpBits1 & 0x00FFff);
            if (str.charAt(i) != c) {
              return false;
            }
          }
        }
        return true;
      }
    }
    return false;
  }


  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp
}

