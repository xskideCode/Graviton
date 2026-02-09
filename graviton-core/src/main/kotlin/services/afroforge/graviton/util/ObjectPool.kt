package services.afroforge.graviton.util

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Thread-safe object pool for reusing objects to avoid allocations.
 * @param factory Function to create new instances
 * @param reset Function to reset an object before reuse
 * @param maxSize Maximum pool size (-1 for unlimited)
 */
open class ObjectPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit = {},
    private val maxSize: Int = 1000,
) {
    private val pool = ConcurrentLinkedQueue<T>()
    private val currentSize = java.util.concurrent.atomic.AtomicInteger(0)

    /**
     * Acquire an object from the pool or create a new one.
     */
    fun acquire(): T {
        val obj = pool.poll()
        return if (obj != null) {
            currentSize.decrementAndGet()
            reset(obj)
            obj
        } else {
            factory()
        }
    }

    /**
     * Return an object to the pool.
     */
    fun release(obj: T) {
        if (maxSize < 0 || currentSize.get() < maxSize) {
            reset(obj)
            pool.offer(obj)
            currentSize.incrementAndGet()
        }
        // If pool is full, object will be garbage collected
    }

    /**
     * Execute a block with a pooled object, automatically returning it.
     */
    inline fun <R> use(block: (T) -> R): R {
        val obj = acquire()
        try {
            return block(obj)
        } finally {
            release(obj)
        }
    }

    /**
     * Clear the pool.
     */
    fun clear() {
        pool.clear()
        currentSize.set(0)
    }

    /**
     * Get current pool size.
     */
    fun size() = currentSize.get()
}

/**
 * Interface for objects that can be pooled.
 */
interface Poolable {
    /**
     * Reset the object to its initial state.
     */
    fun reset()
}

/**
 * Object pool specifically for Poolable objects.
 */
class PoolableObjectPool<T : Poolable>(
    factory: () -> T,
    maxSize: Int = 1000,
) : ObjectPool<T>(
        factory = factory,
        reset = { it.reset() },
        maxSize = maxSize,
    )
