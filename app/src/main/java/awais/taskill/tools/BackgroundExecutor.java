package awais.taskill.tools;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class BackgroundExecutor {
    private static final int CORE_POOL_SIZE = 1;
    private static final int BACKUP_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_SECONDS = 10;

    private static ThreadPoolExecutor sBackupExecutor;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @NonNull
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "BackExecutor #" + mCount.getAndIncrement());
        }
    };
    private static final RejectedExecutionHandler sRunOnSerialPolicy = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
            synchronized (this) {
                if (sBackupExecutor == null) {
                    sBackupExecutor = new ThreadPoolExecutor(BACKUP_POOL_SIZE, BACKUP_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(), sThreadFactory);
                    sBackupExecutor.allowCoreThreadTimeOut(true);
                }
            }
            sBackupExecutor.execute(r);
        }
    };

    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     * <p>
     * Using a single thread pool for a general purpose results in suboptimal behavior
     * for different tasks. Small, CPU-bound tasks benefit from a bounded pool and queueing, and
     * long-running blocking tasks, such as network operations, benefit from many threads. Use or
     * create an {@link Executor} configured for your use case.
     */
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new SynchronousQueue<>(), sThreadFactory);

    static {
        THREAD_POOL_EXECUTOR.setRejectedExecutionHandler(sRunOnSerialPolicy);
    }

    public static Executor getThreadPoolExecutor() {
        return THREAD_POOL_EXECUTOR;
    }
}