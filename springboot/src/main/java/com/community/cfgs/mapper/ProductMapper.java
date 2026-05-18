package com.community.cfgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.cfgs.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    @Select("SELECT DISTINCT category FROM products WHERE category IS NOT NULL ORDER BY category")
    List<String> selectCategories();
}
