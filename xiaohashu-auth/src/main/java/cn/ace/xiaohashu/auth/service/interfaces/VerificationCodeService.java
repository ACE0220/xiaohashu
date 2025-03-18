package cn.ace.xiaohashu.auth.service.interfaces;

import cn.ace.framework.common.response.Response;
import cn.ace.xiaohashu.auth.model.vo.veriticationcode.SendVerificationCodeReqVO;

public interface VerificationCodeService {
    Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}
