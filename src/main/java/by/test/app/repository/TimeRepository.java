package by.test.app.repository;

import by.test.app.annotation.PreCheckDbConnection;
import by.test.app.entity.TimeEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@PreCheckDbConnection
public interface TimeRepository extends CrudRepository<TimeEntity, Long> {

    @Modifying
    @Query("insert into times(time) values (:currentTime)")
    void save(Instant currentTime);

    @Query("select time from times")
    List<Instant> getAllTimes();

}
