package by.test.app.service;

import by.test.app.exception.DbNotAvailableException;
import com.hazelcast.collection.IQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionTimedOutException;

import java.time.Instant;

import static by.test.app.constant.BeanNames.TIME_READER_ASYNC_POOL;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeCacheManager {

    private final IQueue<Instant> timeCache;
    private final TimeService timeService;

    @Async(TIME_READER_ASYNC_POOL)
    public void syncCacheWithDbAsync() {
        syncCacheWithDb();
    }

    public void syncCacheWithDb() {
        try {
            while (!timeCache.isEmpty()) {
                var time = timeCache.peek();
                if (time != null) {
                    timeService.save(time);
                    timeCache.remove(time);
                }
            }
        } catch (DbNotAvailableException e) {
            log.error("DB availability error while trying to save 'time'");
        } catch (TransactionTimedOutException e) {
            log.error("Transaction timed out while saving 'time'");
        } catch (Exception e) {
            log.error("Some error while trying to save 'time'", e);
        }
    }
}
