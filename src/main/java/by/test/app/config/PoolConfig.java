package by.test.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static by.test.app.constant.BeanNames.TIME_READER_ASYNC_POOL;
import static by.test.app.constant.BeanNames.TIME_WRITER_ASYNC_POOL;

@Configuration
public class PoolConfig {

    @Bean(TIME_READER_ASYNC_POOL)
    public ExecutorService timeReaderAsyncPool() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean(TIME_WRITER_ASYNC_POOL)
    public ExecutorService timeWriterAsyncPool() {
        return Executors.newSingleThreadExecutor();
    }

}
