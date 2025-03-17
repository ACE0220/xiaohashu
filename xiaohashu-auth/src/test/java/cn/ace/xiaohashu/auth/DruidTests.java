package cn.ace.xiaohashu.auth;

import com.alibaba.druid.filter.config.ConfigTools;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class DruidTests {

    @Test
    @SneakyThrows
    void testEncodePassword() {
        String password = "abc123456";
        String[] arr = ConfigTools.genKeyPair(512);

        log.info("private key: {}", arr[0]);
        log.info("public key: {}", arr[1]);

        String encodePassword = ConfigTools.encrypt(arr[0], password);
        log.info("encrypt password: {}", encodePassword);
    }
}
