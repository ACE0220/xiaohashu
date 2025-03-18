package cn.ace.xiaohashu.auth.model.vo.veriticationcode;

import cn.ace.xiaohashu.auth.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendVerificationCodeReqVO {

    @NotBlank(message = "Phone number can not be empty")
    @PhoneNumber
    private String phone;
}
