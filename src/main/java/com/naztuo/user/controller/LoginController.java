package com.naztuo.user.controller;

import com.naztuo.common.Consts;
import com.naztuo.common.resultbean.RspMsg;
import com.naztuo.user.bean.LoginVo;
import com.naztuo.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @RequestMapping("/to_login")
    public String tologin(LoginVo loginVo, Model model) {
        LOGGER.info(loginVo.toString());
        //未完成
        userService.vistorCount(Consts.COUNTLOGIN);
        long count = userService.getVistorCount(Consts.COUNTLOGIN);
        LOGGER.info("访问网站的次数为:{}",count);
        model.addAttribute("count",count);
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public RspMsg dologin(HttpServletResponse response, @Valid LoginVo loginVo) {
        userService.login(response, loginVo);
        return RspMsg.success();
    }


    @RequestMapping("/create_token")
    @ResponseBody
    public String createToken(HttpServletResponse response, @Valid LoginVo loginVo) {
        LOGGER.info(loginVo.toString());
        String token = userService.createToken(response, loginVo);
        return token;
    }
}
