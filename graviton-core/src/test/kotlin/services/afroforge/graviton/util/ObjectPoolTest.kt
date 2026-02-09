package services.afroforge.graviton.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class ObjectPoolTest {

    class Reusable {
        var id = 0
    }

    @Test
    fun `test acquire and release`() {
        val pool = ObjectPool({ Reusable() })
        
        val obj1 = pool.acquire()
        obj1.id = 1
        pool.release(obj1)
        
        val obj2 = pool.acquire()
        assertEquals(1, obj2.id) // Should be same object (reused)
        
        // Identity check might fail if ConcurrentLinkedQueue decides to create new? No.
        // But implementation uses poll().
        // If pool was empty, create new.
        // We released, so pool has 1.
    }

    @Test
    fun `test overflow behavior`() {
        // Max size 1
        val pool = ObjectPool({ Reusable() }, maxSize = 1)
        
        val obj1 = pool.acquire()
        val obj2 = pool.acquire()
        
        pool.release(obj1)
        pool.release(obj2)
        
        assertEquals(1, pool.size())
    }

    @Test
    fun `test concurrency`() {
        val pool = ObjectPool({ Reusable() })
        val threads = 10
        val iterations = 1000
        val latch = CountDownLatch(threads)
        val errorCount = AtomicInteger(0)

        // Pre-fill? No.

        for (i in 0 until threads) {
            Thread {
                try {
                    repeat(iterations) {
                        val obj = pool.acquire()
                        pool.release(obj)
                    }
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }.start()
        }

        latch.await()
        assertEquals(0, errorCount.get())
        assertTrue(pool.size() <= threads) // Should be roughly low number
    }
}
