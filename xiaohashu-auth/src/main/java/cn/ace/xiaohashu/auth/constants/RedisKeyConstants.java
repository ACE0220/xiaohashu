package cn.ace.xiaohashu.auth.constants;

public class RedisKeyConstants {
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code";

    public static String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }

    public static final String XIAOHASHU_ID_GENERATOR_KEY = "xiaohashu.id.generator";

    private static final String USER_ROLES_KEY_PREFIX = "user:roles";

    public static String buildUserRoleKey(String phone) {
        return USER_ROLES_KEY_PREFIX + phone;
    }

    /**
     * 角色对应的权限集合 KEY 前缀
     */
    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    /**
     * 构建角色对应的权限集合 KEY
     * @param roleId
     * @return
     */
    public static String buildRolePermissionsKey(Long roleId) {
        return ROLE_PERMISSIONS_KEY_PREFIX + roleId;
    }
}
