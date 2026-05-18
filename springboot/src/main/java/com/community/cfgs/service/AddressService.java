package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.Address;
import com.community.cfgs.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    public List<Address> listByUserId(String userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .orderByDesc(Address::getIsDefault)
               .orderByDesc(Address::getCreatedAt);
        return addressMapper.selectList(wrapper);
    }

    public Address getById(String id) {
        return addressMapper.selectById(id);
    }

    public Address getDefaultByUserId(String userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .eq(Address::getIsDefault, true);
        return addressMapper.selectOne(wrapper);
    }

    public Address add(Address address) {
        address.setId("addr_" + UUID.randomUUID().toString().substring(0, 8));
        address.setCreatedAt(LocalDateTime.now());
        
        // 如果是第一个地址，设为默认
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, address.getUserId());
        if (addressMapper.selectCount(wrapper) == 0) {
            address.setIsDefault(true);
        } else if (address.getIsDefault() == null) {
            address.setIsDefault(false);
        }
        
        // 如果设为默认，先取消其他默认
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefault(address.getUserId());
        }
        
        addressMapper.insert(address);
        return address;
    }

    public boolean update(Address address) {
        // 如果设为默认，先取消其他默认
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefault(address.getUserId());
        }
        return addressMapper.updateById(address) > 0;
    }

    public boolean delete(String id) {
        Address address = addressMapper.selectById(id);
        if (address == null) return false;
        
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        String userId = address.getUserId();
        
        addressMapper.deleteById(id);
        
        // 如果删除的是默认地址，将第一个地址设为默认
        if (wasDefault) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, userId)
                   .orderByAsc(Address::getCreatedAt)
                   .last("LIMIT 1");
            Address first = addressMapper.selectOne(wrapper);
            if (first != null) {
                first.setIsDefault(true);
                addressMapper.updateById(first);
            }
        }
        return true;
    }

    public boolean setDefault(String id) {
        Address address = addressMapper.selectById(id);
        if (address == null) return false;
        
        // 先取消其他默认
        clearDefault(address.getUserId());
        
        // 设置新默认
        LambdaUpdateWrapper<Address> setWrapper = new LambdaUpdateWrapper<>();
        setWrapper.eq(Address::getId, id)
                  .set(Address::getIsDefault, true);
        return addressMapper.update(null, setWrapper) > 0;
    }

    private void clearDefault(String userId) {
        LambdaUpdateWrapper<Address> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .set(Address::getIsDefault, false);
        addressMapper.update(null, wrapper);
    }
}
