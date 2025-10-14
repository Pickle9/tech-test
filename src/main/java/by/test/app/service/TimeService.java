package by.test.app.service;

import by.test.app.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeService {

    private final TimeRepository timeRepository;

    @Transactional(timeout = 1)
    public void save(Instant currentTime) {
        timeRepository.save(currentTime);
        log.info("Saved time {}", currentTime);
    }

    public List<Instant> getAll() {
        return timeRepository.getAllTimes();
    }

}
