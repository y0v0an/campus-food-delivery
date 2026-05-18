package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.Product;
import com.community.cfgs.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public List<Product> list(String status, String category) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Product::getStatus, status);
        }
        if (StringUtils.hasText(category) && !"全部".equals(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        return productMapper.selectList(wrapper);
    }

    public Product getById(String id) {
        return productMapper.selectById(id);
    }

    public Product add(Product product) {
        product.setId("prod_" + UUID.randomUUID().toString().substring(0, 8));
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        if (product.getStock() == 0) {
            product.setStatus("soldout");
        } else if (product.getStatus() == null) {
            product.setStatus("active");
        }
        productMapper.insert(product);
        return product;
    }

    public boolean update(String id, Product product) {
        product.setId(id);
        product.setUpdatedAt(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
    }

    public boolean updateStatus(String id, String status) {
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Product::getId, id)
               .set(Product::getStatus, status)
               .set(Product::getUpdatedAt, LocalDateTime.now());
        return productMapper.update(null, wrapper) > 0;
    }

    public Integer restock(String id, Integer quantity) {
        Product product = productMapper.selectById(id);
        if (product == null) return null;
        
        int newStock = product.getStock() + quantity;
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Product::getId, id)
               .set(Product::getStock, newStock)
               .set(Product::getUpdatedAt, LocalDateTime.now());
        
        // 如果之前是售罄，补货后变为上架
        if ("soldout".equals(product.getStatus())) {
            wrapper.set(Product::getStatus, "active");
        }
        
        productMapper.update(null, wrapper);
        return newStock;
    }

    public boolean reduceStock(String id, Integer quantity) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStock() < quantity) return false;
        
        int newStock = product.getStock() - quantity;
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Product::getId, id)
               .set(Product::getStock, newStock)
               .set(Product::getUpdatedAt, LocalDateTime.now());
        
        if (newStock == 0) {
            wrapper.set(Product::getStatus, "soldout");
        }
        
        return productMapper.update(null, wrapper) > 0;
    }

    public List<String> getCategories() {
        return productMapper.selectCategories();
    }
}
