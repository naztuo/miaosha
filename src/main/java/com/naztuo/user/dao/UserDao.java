package com.naztuo.user.dao;

import com.naztuo.user.bean.MiaoshaUser;
import org.apache.ibatis.annotations.Param;

public interface UserDao {

    void insertMiaoShaUser(MiaoshaUser miaoshaUser);

    MiaoshaUser getByNickname(@Param("nickname") String nickname ) ;
}
