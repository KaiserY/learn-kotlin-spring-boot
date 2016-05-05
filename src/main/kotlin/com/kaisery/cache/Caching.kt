package com.kaisery.cache

import com.kaisery.controller.User
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

object Caching {
    val cacheManager: CacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

    val userCache: Cache<Int, User> = cacheManager.createCache("userCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    Int::class.javaObjectType,
                    User::class.java,
                    ResourcePoolsBuilder.heap(10).build()))
}
