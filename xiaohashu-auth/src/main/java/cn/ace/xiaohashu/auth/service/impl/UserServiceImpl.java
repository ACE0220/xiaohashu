package cn.ace.xiaohashu.auth.service.impl;

import cn.ace.framework.common.exception.BizException;
import cn.ace.framework.common.response.Response;
import cn.ace.framework.common.util.JsonUtils;
import cn.ace.xiaohashu.auth.constants.RedisKeyConstants;
import cn.ace.xiaohashu.auth.constants.RoleConstants;
import cn.ace.xiaohashu.auth.domain.dataobject.UserDO;
import cn.ace.xiaohashu.auth.domain.dataobject.UserRoleRelDO;
import cn.ace.xiaohashu.auth.domain.mapper.UserDOMapper;
import cn.ace.xiaohashu.auth.domain.mapper.UserRoleRelDOMapper;
import cn.ace.xiaohashu.auth.enums.DeletedEnum;
import cn.ace.xiaohashu.auth.enums.LoginTypeEnum;
import cn.ace.xiaohashu.auth.enums.ResponseCodeEnum;
import cn.ace.xiaohashu.auth.enums.StatusEnum;
import cn.ace.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import cn.ace.xiaohashu.auth.service.interfaces.UserService;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Preconditions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDOMapper userDOMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserRoleRelDOMapper userRoleRelDOMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        Long userId = null;
        switch (loginTypeEnum) {
            case VERIFICATION_CODE:
                String verificationCode = userLoginReqVO.getCode();
                // 校验入参验证码是否为空
                Preconditions.checkArgument(StringUtils.isNoneBlank(verificationCode), "验证码不可为空");
                // 构建验证码 Redis key
                String key = RedisKeyConstants.buildVerificationCodeKey(phone);
                String sentCode = (String) redisTemplate.opsForValue().get(key);

                if(!StringUtils.equals(verificationCode, sentCode)){
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }
                // 通过手机号查询记录
                UserDO userDO = userDOMapper.selectByPhone(phone);

                log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO));

                if (Objects.isNull(userDO)) {
                    userId = registerUser(phone);
                } else {
                    userId = userDO.getId();
                }
                break;

            case PASSWORD:
                // todo
                break;
            default:
                break;
        }
        // SaToken 登录用户，并返回 token 令牌
        StpUtil.login(userId);
        // 获取token
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        // 返回token
        return Response.success(tokenInfo.tokenValue);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long registerUser(String phone) {
        return transactionTemplate.execute(status -> {
            try {
                // 获取全局自增的小哈书 ID
                Long xiaohashuId = redisTemplate.opsForValue().increment(RedisKeyConstants.XIAOHASHU_ID_GENERATOR_KEY);

                UserDO userDO = UserDO.builder()
                        .phone(phone)
                        .xiaohashuId(String.valueOf(xiaohashuId)) // 自动生成小红书号 ID
                        .nickname("小红薯" + xiaohashuId) // 自动生成昵称, 如：小红薯10000
                        .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                        .build();

                // 添加入库
                userDOMapper.insert(userDO);
                // 获取刚刚添加入库的用户 ID
                Long userId = userDO.getId();



                // 给该用户分配一个默认角色
                UserRoleRelDO userRoleRelDO = UserRoleRelDO.builder()
                        .userId(userId)
                        .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue())
                        .build();
                userRoleRelDOMapper.insert(userRoleRelDO);

                // 将该用户的角色 ID 存入 Redis 中
                List<Long> roles = Lists.newArrayList();
                roles.add(RoleConstants.COMMON_USER_ROLE_ID);
                String userRolesKey = RedisKeyConstants.buildUserRoleKey(phone);
                redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

                return userId;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("==>系统注册异常", e);
                return null;
            }
        });

    }
}
