package com.eightbitlab.blurview_sample.net;

import android.content.Context;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.cache.CacheEvictor;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;

import java.io.File;

@UnstableApi
public class VideoPlayerCache {

    private static SimpleCache simpleCache;

    public static synchronized SimpleCache getInstance(Context context) {
        if (simpleCache == null) {
            File cacheDir = new File(context.getCacheDir(), "video_cache");
            CacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(300L * 1024 * 1024); // 300MB
            simpleCache = new SimpleCache(
                    cacheDir,
                    evictor,
                    new StandaloneDatabaseProvider(context.getApplicationContext())
            );
        }
        return simpleCache;
    }
}