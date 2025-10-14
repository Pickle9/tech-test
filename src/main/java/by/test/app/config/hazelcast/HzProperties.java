package by.test.app.config.hazelcast;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("hazelcast")
public class HzProperties {

    private String name;
    private HzMode mode;

}
