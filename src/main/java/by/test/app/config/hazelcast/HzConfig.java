package by.test.app.config.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HzConfig {

    @Bean
    @ConditionalOnProperty(prefix = "hazelcast", value = "mode", havingValue = "LOCAL")
    public HazelcastInstance hazelcastConfig(HzProperties properties) {
        var config = new Config()
                .setInstanceName(properties.getName());

        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        config.getNetworkConfig().setPort(0);

        return Hazelcast.newHazelcastInstance(config);
    }
}
