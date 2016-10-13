package org.apache.geode.e2e;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.util.CacheListenerAdapter;

public class CopyingCacheListener<K, V> extends CacheListenerAdapter<String, String> {

  private Cache cache;

  public void afterCreate(EntryEvent event) {
    Region r  = getDstRegion(event);
    r.put(event.getKey(), event.getNewValue());
  }


  private Region<K, V> getDstRegion(EntryEvent e) {
    String srcRegion = e.getRegion().getName();
    return getCache().getRegion(srcRegion + "-copy");
  }

  private synchronized Cache getCache() {
    if (cache == null) {
      cache = CacheFactory.getAnyInstance();
    }

    return cache;
  }
}
