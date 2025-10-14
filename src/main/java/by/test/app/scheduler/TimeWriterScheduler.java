package by.test.app.scheduler;

import by.test.app.service.TimeCacheManager;
import com.hazelcast.collection.IQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static by.test.app.constant.BeanNames.TIME_WRITER_ASYNC_POOL;

@Component
@RequiredArgsConstructor
public class TimeWriterScheduler {

    private final IQueue<Instant> timeCache;
    private final TimeCacheManager timeCacheManager;

    @Async(TIME_WRITER_ASYNC_POOL)
    @Scheduled(fixedDelayString = "${scheduler.time.write.delay}")
    public void execute() {
        timeCache.add(Instant.now());
        timeCacheManager.syncCacheWithDb();
    }

}
