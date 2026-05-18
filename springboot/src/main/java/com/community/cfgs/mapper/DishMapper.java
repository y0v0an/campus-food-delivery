package com.community.cfgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.cfgs.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
