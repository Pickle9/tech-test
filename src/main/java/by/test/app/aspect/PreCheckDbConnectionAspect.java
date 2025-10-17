package by.test.app.aspect;

import by.test.app.config.DbStateInfo;
import by.test.app.exception.DbNotAvailableException;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;

import java.net.ConnectException;
import java.sql.SQLNonTransientConnectionException;
import java.time.Instant;

import static by.test.app.constant.HzKeys.DB_STATE_INFO_KEY;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PreCheckDbConnectionAspect {

    private final IMap<String, DbStateInfo> dbStateInfoCache;

    @Value("${db-state-check.unavailability.delay-ms}")
    private Long unavailabilityCheckDelayMs;

    @Around("@within(by.test.app.annotation.PreCheckDbConnection)")
    public Object aroundAnnotation(ProceedingJoinPoint pjp) {
        var dbStateInfo = dbStateInfoCache.get(DB_STATE_INFO_KEY);
        if (dbStateInfo == null) {
            dbStateInfo = new DbStateInfo()
                    .setIsAlive(TRUE);
        }

        if (FALSE.equals(dbStateInfo.getIsAlive())
                &&
                dbStateInfo.getLostConnectionTime()
                        .plusMillis(unavailabilityCheckDelayMs)
                        .compareTo(Instant.now()) > 0
        ) {
//            log.error("Db not available. Retry later");
            throw new DbNotAvailableException();
        }

        try {
            var result = pjp.proceed();
            if (FALSE.equals(dbStateInfo.getIsAlive())) {
                dbStateInfoCache.put(DB_STATE_INFO_KEY, dbStateInfo.setIsAlive(TRUE));
            }

            return result;
        } catch (SQLNonTransientConnectionException
                 | ConnectException
                 | CannotGetJdbcConnectionException
                 | CannotCreateTransactionException e
        ) {
            log.error("Db not available. Retry after [{}] ms", unavailabilityCheckDelayMs);
            dbStateInfoCache.put(DB_STATE_INFO_KEY,
                    dbStateInfo.setIsAlive(FALSE)
                            .setLostConnectionTime(Instant.now()));

            throw new DbNotAvailableException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
