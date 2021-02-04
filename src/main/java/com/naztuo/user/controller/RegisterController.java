package com.naztuo.user.controller;


import com.naztuo.common.resultbean.RspMsg;
import com.naztuo.user.service.UserService;
import com.naztuo.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

/**
 * 用户注册类
 */
@Controller
@RequestMapping("/user")
public class RegisterController {

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/verifyCodeRegister", method = RequestMethod.GET)
    @ResponseBody
    public RspMsg<String> getMiaoshaVerifyCod(HttpServletRequest request, HttpServletResponse response) {
        BufferedImage image = userService.createVerifyCodeRegister(RequestUtil.getRealIp(request));
        OutputStream out = response.getOutputStream();
        ImageIO.write(image, "JPEG", out);
        out.flush();
        out.close();
        return result;

    }
}
