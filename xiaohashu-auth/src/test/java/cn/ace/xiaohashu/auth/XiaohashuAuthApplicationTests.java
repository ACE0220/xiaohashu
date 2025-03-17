package cn.ace.xiaohashu.auth;

import cn.ace.framework.common.util.JsonUtils;
import cn.ace.xiaohashu.auth.domain.dataobject.UserDO;
import cn.ace.xiaohashu.auth.domain.mapper.UserDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
@Slf4j
class XiaohashuAuthApplicationTests {

    @Resource
    private UserDOMapper userDOMapper;

    /**
     * 测试插入数据
     */
    @Test
    void testInsert() {
        UserDO userDO = UserDO.builder()
                .username("testusername")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        userDOMapper.insert(userDO);
    }

    @Test
    void testSelect() {
        UserDO userDO = userDOMapper.selectByPrimaryKey(1L);
        log.info("userDO:{}", JsonUtils.toJsonString(userDO));
    }

}

