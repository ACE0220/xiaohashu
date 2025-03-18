package cn.ace.xiaohashu.auth.runner;

import cn.ace.framework.common.util.JsonUtils;
import cn.ace.xiaohashu.auth.constants.RedisKeyConstants;
import cn.ace.xiaohashu.auth.domain.dataobject.PermissionDO;
import cn.hutool.core.collection.CollUtil;
import cn.ace.xiaohashu.auth.domain.dataobject.RoleDO;
import cn.ace.xiaohashu.auth.domain.dataobject.RolePermissionRelDO;
import cn.ace.xiaohashu.auth.domain.mapper.PermissionDOMapper;
import cn.ace.xiaohashu.auth.domain.mapper.RoleDOMapper;
import cn.ace.xiaohashu.auth.domain.mapper.RolePermissionRelDOMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PushRolePermissions2RedisRunner implements ApplicationRunner {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private PermissionDOMapper permissionDOMapper;
    @Resource
    private RolePermissionRelDOMapper rolePermissionRelDOMapper;

    // 权限同步标记 Key
    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";

    @Override
    public void run(ApplicationArguments args) {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        try {
            // 是否能够同步数据: 原子操作，只有在键 PUSH_PERMISSION_FLAG 不存在时，才会设置该键的值为 "1"，并设置过期时间为 1 天
            boolean canPushed = redisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, "1", 1, TimeUnit.DAYS);
            // 如果无法同步权限数据
            if (!canPushed) {
                log.warn("==> 角色权限数据已经同步至 Redis 中，不再同步...");
                return;
            }


            List<RoleDO> roleDOS = roleDOMapper.selectEnabledList();
            if (CollUtil.isNotEmpty(roleDOS)) {
                // 拿到所有角色的 ID
                List<Long> roleIds = roleDOS.stream().map(RoleDO::getId).toList();
                // 根据角色ID拿到所有角色对应的权限
                List<RolePermissionRelDO> rolePermissionRelDOS = rolePermissionRelDOMapper.selectByRoleIds(roleIds);
                // 按角色 ID 分组, 每个角色 ID 对应多个权限 ID
                Map<Long, List<Long>> roleIdPermissionIdsMap = rolePermissionRelDOS.stream().collect(
                        Collectors.groupingBy(RolePermissionRelDO::getRoleId,
                                Collectors.mapping(RolePermissionRelDO::getPermissionId, Collectors.toList()))
                );
                // 查询 APP 端所有被启用的权限
                List<PermissionDO> permissionDOS = permissionDOMapper.selectAppEnabledList();
                // 权限 ID - 权限 DO
                Map<Long, PermissionDO> permissionIdDOMap = permissionDOS.stream().collect(
                        Collectors.toMap(PermissionDO::getId, permissionDO -> permissionDO)
                );
                // 组织 角色ID-权限 关系
                Map<Long, List<PermissionDO>> roldIdPermissionRelDoMap = Maps.newHashMap();
                // 循环所有角色
                roleDOS.forEach(roleDO -> {
                    // 角色id
                    Long roleId = roleDO.getId();
                    // 当前角色ID对应的权限ID集合
                    List<Long> permissionIds = roleIdPermissionIdsMap.get(roleId);
                    if (CollUtil.isNotEmpty(permissionIds)) {
                        List<PermissionDO> perDOS = Lists.newArrayList();
                        permissionIds.forEach(permissionId -> {
                            // 根据权限 ID 获取具体的权限 DO 对象
                            PermissionDO permissionDO = permissionIdDOMap.get(permissionId);
                            if (Objects.nonNull(permissionDO)) {
                                perDOS.add(permissionDO);
                            }
                        });
                        roldIdPermissionRelDoMap.put(roleId, perDOS);
                    }
                });
                // 同步至 Redis 中，方便后续网关查询鉴权使用
                roldIdPermissionRelDoMap.forEach((roleId, pDOS) -> {
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleId);
                    redisTemplate.opsForValue().set(key, JsonUtils.toJsonString(pDOS));
                });
            }
            log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
        } catch (Exception e) {
            log.error("==> 同步角色权限数据到 Redis 中失败: ", e);
        }


    }
}

