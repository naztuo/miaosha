package com.naztuo.user.controller;


import com.naztuo.common.enums.ResultStatus;
import com.naztuo.common.resultbean.RspMsg;
import com.naztuo.user.service.UserService;
import com.naztuo.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import static com.naztuo.common.enums.ResultStatus.SUCCESS;

/**
 * 用户注册类
 */
@Controller
@RequestMapping("/user")
public class RegisterController {

    @Autowired
    private UserService userService;


    @RequestMapping("/do_register")
    public String registerIndex(){
        return "register";
    }

    @RequestMapping(value = "/verifyCodeRegister", method = RequestMethod.GET)
    @ResponseBody
    public RspMsg getMiaoshaVerifyCod(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedImage image = userService.createVerifyCodeRegister(RequestUtil.getRealIp(request));
        OutputStream out = response.getOutputStream();
        ImageIO.write(image, "JPEG", out);
        out.flush();
        out.close();
        return RspMsg.success(SUCCESS.getMessage(),image);
    }


    /**
     * 注册网站
     *
     * @param userName
     * @param passWord
     * @param salt
     * @return
     */
    @RequestMapping("/register")
    @ResponseBody
    public RspMsg register(@RequestParam("username") String userName, @RequestParam("password") String passWord,
                           @RequestParam("verifyCode") String verifyCode, @RequestParam("salt") String salt,
                           HttpServletResponse response, HttpServletRequest request) {
        /**
         * 校验验证码
         */
        boolean check = userService.checkVerifyCodeRegister(Integer.valueOf(verifyCode), RequestUtil.getRealIp(request));
        if (!check) {
            return RspMsg.error(ResultStatus.CODE_FAIL);
        }
        boolean registerInfo = userService.register(response, userName, passWord, salt);
        if (!registerInfo) {
            return RspMsg.error(ResultStatus.RESIGETER_FAIL);
        }
        return RspMsg.success(ResultStatus.RESIGETR_SUCCESS.getMessage());
    }

}
