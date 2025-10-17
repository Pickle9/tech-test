package by.test.app.scheduler;

import by.test.app.AbstractTestWithDb;
import by.test.app.entity.TimeEntity;
import by.test.app.repository.TimeRepository;
import by.test.app.service.TimeCacheManager;
import by.test.app.service.TimeService;
import com.hazelcast.collection.IQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionTimedOutException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@SpringBootTest
public class TimeWriterSchedulerTest extends AbstractTestWithDb {

    @Autowired
    private IQueue<Instant> timeCache;

    @Autowired
    private TimeCacheManager timeCacheManager;

    @SpyBean
    private TimeRepository timeRepositorySpy;

    @SpyBean
    private TimeService timeServiceSpy;

    @Value("${db-state-check.unavailability.delay-ms}")
    private Long dbCheckUnavailabilityDelayMs;

    @Test
    public void testWriterStoresTimesOneByOne() {
        var timeWriterScheduler = new TimeWriterScheduler(timeCache, timeCacheManager);

        IntStream.range(0, 10).forEach(i ->
                timeWriterScheduler.executeInternal(false));

        var actual = stream(timeRepositorySpy.findAll().spliterator(), false)
                .sorted(Comparator.comparingLong(TimeEntity::getId))
                .toList();

        var isAllItemsAscOrder = isAllItemsInAscOrder(actual);

        assertThat(actual).size().isEqualTo(10);
        Assertions.assertTrue(isAllItemsAscOrder);
    }

    @Test
    public void testWriterStoresTimesOneByOneWithDbConnectionError() throws Exception {
        var timeWriterScheduler = new TimeWriterScheduler(timeCache, timeCacheManager);

        //regular execution
        IntStream.range(0, 5).forEach(i ->
                timeWriterScheduler.executeInternal(false));
        assertThat(timeRepositorySpy.findAll()).size().isEqualTo(5);

        //execution while db not available
        doThrow(CannotCreateTransactionException.class)
                .when(timeRepositorySpy)
                .save(any(Instant.class));

        IntStream.range(0, 5).forEach(i ->
                timeWriterScheduler.executeInternal(false));

        reset(timeRepositorySpy);

        Thread.sleep(dbCheckUnavailabilityDelayMs);

        //execution after connection restored
        IntStream.range(0, 5).forEach(i ->
                timeWriterScheduler.executeInternal(false));

        var actual = stream(timeRepositorySpy.findAll().spliterator(), false)
                .sorted(Comparator.comparingLong(TimeEntity::getId))
                .toList();

        var isAllItemsAscOrder = isAllItemsInAscOrder(actual);

        assertThat(actual).size().isEqualTo(15);
        Assertions.assertTrue(isAllItemsAscOrder);
    }

    @Test
    public void testWriterStoresTimesOneByOneWithDbTimeouts() throws Exception {
        var timeWriterScheduler = new TimeWriterScheduler(timeCache, timeCacheManager);

        //regular execution
        IntStream.range(0, 5).forEach(i ->
                timeWriterScheduler.executeInternal(false));
        assertThat(timeRepositorySpy.findAll()).size().isEqualTo(5);

        //execution while db is busy
        doThrow(TransactionTimedOutException.class)
                .when(timeServiceSpy)
                .save(any(Instant.class));

        IntStream.range(0, 5).forEach(i ->
                timeWriterScheduler.executeInternal(false));

        reset(timeServiceSpy);

        //execution after db became less loaded
        IntStream.range(0, 5).forEach(i ->
                timeWriterScheduler.executeInternal(false));

        var actual = stream(timeRepositorySpy.findAll().spliterator(), false)
                .sorted(Comparator.comparingLong(TimeEntity::getId))
                .toList();

        var isAllItemsAscOrder = isAllItemsInAscOrder(actual);

        assertThat(actual).size().isEqualTo(15);
        Assertions.assertTrue(isAllItemsAscOrder);
    }

    private Boolean isAllItemsInAscOrder(List<TimeEntity> actual) {
        return IntStream.range(0, actual.size() - 2)
                .allMatch(i -> {
                    var i1 = actual.get(i);
                    var i2 = actual.get(i + 1);

                    return i1.getId() < i2.getId()
                            && i1.getTime().compareTo(i2.getTime()) <= 0;
                });
    }

}
