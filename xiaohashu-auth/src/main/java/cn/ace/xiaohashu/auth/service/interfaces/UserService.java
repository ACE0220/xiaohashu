package cn.ace.xiaohashu.auth.service.interfaces;

import cn.ace.framework.common.response.Response;
import cn.ace.xiaohashu.auth.model.vo.user.UserLoginReqVO;

/**
 * @author: 犬小哈
 * @date: 2024/4/7 15:41
 * @version: v1.0.0
 * @description: TODO
 **/
public interface UserService {

    /**
     * 登录与注册
     * @param userLoginReqVO
     * @return
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);
}