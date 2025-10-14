package by.test.app.controller;

import by.test.app.service.TimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/time")
public class TimeController {

    private final TimeService timeService;

    @GetMapping
    public List<Instant> get() {
        return timeService.getAll();
    }

}
