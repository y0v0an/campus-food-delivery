package com.community.cfgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.cfgs.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<CartItem> {
}
