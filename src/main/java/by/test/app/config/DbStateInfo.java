package by.test.app.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain=true)
public class DbStateInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean isAlive;
    private Instant lostConnectionTime;

}
