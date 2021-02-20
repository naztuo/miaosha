package com.naztuo.miaosha.dao;

import com.naztuo.miaosha.bean.GoodsVo;
import com.naztuo.miaosha.bean.MiaoshaGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsDao {

    List<GoodsVo> listGoodsVo();

    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    int reduceStock(MiaoshaGoods g);
}
