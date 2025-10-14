package by.test.app.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("times")
public class TimeEntity {

    @Id
    private Long id;
    private Instant time;

}
