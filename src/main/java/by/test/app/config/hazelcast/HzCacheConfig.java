package by.test.app.config.hazelcast;

import by.test.app.config.DbStateInfo;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class HzCacheConfig {

    @Bean
    public IQueue<Instant> timeCache(HazelcastInstance instance) {
        return instance.getQueue("timeCache");
    }

    @Bean
    public IMap<String, DbStateInfo> dbStateInfoCache(HazelcastInstance instance) {
        return instance.getMap("dbStateInfoCache");
    }

}
