package com.valtecna.iadoc.cache

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

data class CacheEntry(
    val htmlDoc: String,
    val timestamp: Long
)

@Service(Service.Level.APP)
class DocumentationCache {

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val maxAge = 3600000L
    private val maxSize = 500

    fun get(context: String, isPro: Boolean): String? {
        val key = generateKey(context, isPro)
        val entry = cache[key] ?: return null

        if (System.currentTimeMillis() - entry.timestamp > maxAge) {
            cache.remove(key)
            return null
        }

        return entry.htmlDoc
    }

    fun put(context: String, isPro: Boolean, htmlDoc: String) {
        if (cache.size >= maxSize) {
            evictOldest()
        }

        val key = generateKey(context, isPro)
        cache[key] = CacheEntry(htmlDoc, System.currentTimeMillis())
    }

    fun clear() {
        cache.clear()
    }

    private fun generateKey(context: String, isPro: Boolean): String {
        val content = "$context|$isPro"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(content.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun evictOldest() {
        val oldest = cache.entries.minByOrNull { it.value.timestamp }
        oldest?.let { cache.remove(it.key) }
    }

    companion object {
        fun getInstance(): DocumentationCache = service()
    }
}
