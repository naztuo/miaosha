package com.naztuo.miaosha.service;

import com.naztuo.miaosha.bean.GoodsVo;
import com.naztuo.miaosha.dao.GoodsDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GoodsService {

    @Resource
    private GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

}
