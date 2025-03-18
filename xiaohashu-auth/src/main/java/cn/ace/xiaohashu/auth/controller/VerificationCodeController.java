package cn.ace.xiaohashu.auth.controller;

import cn.ace.framework.biz.operationlog.aspect.ApiOperationLog;
import cn.ace.framework.common.response.Response;
import cn.ace.xiaohashu.auth.model.vo.veriticationcode.SendVerificationCodeReqVO;
import cn.ace.xiaohashu.auth.service.interfaces.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class VerificationCodeController {

    @Resource
    private VerificationCodeService verificationCodeService;

    @PostMapping("/verification/code/send")
    @ApiOperationLog(description = "发送短信验证码")
    public Response<?> send(@Validated @RequestBody SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        return verificationCodeService.send(sendVerificationCodeReqVO);
    }

}
