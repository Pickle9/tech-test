package by.test.app.aspect;

import by.test.app.AbstractTestWithDb;
import by.test.app.config.DbStateInfo;
import by.test.app.exception.DbNotAvailableException;
import by.test.app.repository.TimeRepository;
import com.hazelcast.map.IMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.Instant;

import static by.test.app.constant.HzKeys.DB_STATE_INFO_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PreCheckDbConnectionAspectTest extends AbstractTestWithDb {

    @Autowired
    private IMap<String, DbStateInfo> dbStateInfoCache;

    @SpyBean
    private TimeRepository timeRepositorySpy;

    @SpyBean
    private PreCheckDbConnectionAspect preCheckDbConnectionAspectSpy;

    @Value("${db-state-check.unavailability.delay-ms}")
    private Long unavailabilityCheckDelayMs;

    @Test
    void testAspectCalledOnRepositoryMethod() {
        var time = Instant.now();

        timeRepositorySpy.save(time);

        verify(preCheckDbConnectionAspectSpy, times(1))
                .aroundAnnotation(any(ProceedingJoinPoint.class));
    }

    @Test
    void testAspectStoreStateOnConnectionError() throws Throwable {
        var pjpMock = mock(ProceedingJoinPoint.class);
        doThrow(CannotCreateTransactionException.class)
                .when(pjpMock).proceed();

        try {
            preCheckDbConnectionAspectSpy.aroundAnnotation(pjpMock);
        } catch (Throwable ignore) {
        }

        var actual = dbStateInfoCache.get(DB_STATE_INFO_KEY);
        assertNotNull(actual);
        assertFalse(actual.getIsAlive());
        assertNotNull(actual.getLostConnectionTime());
    }

    @Test
    void testDbNotCalledIfUnavailabilityCheckDelayNotPassed() throws Throwable {
        var pjpMock = mock(ProceedingJoinPoint.class);
        dbStateInfoCache.put(
                DB_STATE_INFO_KEY,
                new DbStateInfo()
                        .setIsAlive(false)
                        .setLostConnectionTime(Instant.now()));

        assertThrows(DbNotAvailableException.class,
                () -> preCheckDbConnectionAspectSpy.aroundAnnotation(pjpMock));
        verify(pjpMock, times(0)).proceed();
    }

    @Test
    void testDbTryingToExecuteQueryIfUnavailabilityCheckDelayPassed() throws Throwable {
        var pjpMock = mock(ProceedingJoinPoint.class);
        var timeAfterDelay = Instant.now().minusMillis(unavailabilityCheckDelayMs);
        dbStateInfoCache.put(
                DB_STATE_INFO_KEY,
                new DbStateInfo()
                        .setIsAlive(false)
                        .setLostConnectionTime(timeAfterDelay));

        preCheckDbConnectionAspectSpy.aroundAnnotation(pjpMock);

        verify(pjpMock, times(1)).proceed();
    }

}