package by.test.app.scheduler;

import by.test.app.service.TimeCacheManager;
import com.hazelcast.collection.IQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static by.test.app.constant.BeanNames.TIME_WRITER_ASYNC_POOL;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "scheduler.time.write",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class TimeWriterScheduler {

    private final IQueue<Instant> timeCache;
    private final TimeCacheManager timeCacheManager;

    //add shedlock
    @Async(TIME_WRITER_ASYNC_POOL)
    @Scheduled(fixedDelayString = "${scheduler.time.write.delay}")
    public void executeAsync() {
        executeInternal(true);
    }

    public void executeInternal(Boolean isAsync) {
        timeCache.add(Instant.now());
        if (isAsync) {
            timeCacheManager.syncCacheWithDbAsync();
        } else {
            timeCacheManager.syncCacheWithDb();
        }
    }
}
