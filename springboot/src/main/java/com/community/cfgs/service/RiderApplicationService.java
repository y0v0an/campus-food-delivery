package com.community.cfgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.cfgs.entity.RiderApplication;
import com.community.cfgs.entity.RiderInfo;
import com.community.cfgs.mapper.RiderApplicationMapper;
import com.community.cfgs.mapper.RiderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RiderApplicationService {

    @Autowired
    private RiderApplicationMapper applicationMapper;

    @Autowired
    private RiderInfoMapper riderInfoMapper;

    @Autowired
    private UserService userService;

    public boolean apply(RiderApplication application) {
        // 检查是否已有待审核的申请
        LambdaQueryWrapper<RiderApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiderApplication::getStudentId, application.getStudentId())
               .eq(RiderApplication::getStatus, "pending");
        if (applicationMapper.selectCount(wrapper) > 0) {
            return false;
        }
        
        application.setId("ra_" + UUID.randomUUID().toString().substring(0, 8));
        application.setStatus("pending");
        application.setCreatedAt(LocalDateTime.now());
        return applicationMapper.insert(application) > 0;
    }

    public List<RiderApplication> listByStatus(String status) {
        LambdaQueryWrapper<RiderApplication> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            wrapper.eq(RiderApplication::getStatus, status);
        }
        wrapper.orderByDesc(RiderApplication::getCreatedAt);
        return applicationMapper.selectList(wrapper);
    }

    public List<RiderApplication> listPending() {
        return listByStatus("pending");
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public boolean approve(String applicationId) {
        RiderApplication application = applicationMapper.selectById(applicationId);
        if (application == null || !"pending".equals(application.getStatus())) {
            return false;
        }
        
        // 更新申请状态
        LambdaUpdateWrapper<RiderApplication> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RiderApplication::getId, applicationId)
               .set(RiderApplication::getStatus, "approved")
               .set(RiderApplication::getReviewedAt, LocalDateTime.now());
        applicationMapper.update(null, wrapper);
        
        // 更新用户的骑手状态
        userService.setRiderStatus(application.getStudentId(), true);
        
        // 创建骑手信息
        RiderInfo riderInfo = new RiderInfo();
        riderInfo.setId("ri_" + UUID.randomUUID().toString().substring(0, 8));
        riderInfo.setUserId(application.getStudentId());
        riderInfo.setAvailableTime(application.getAvailableTime());
        riderInfo.setTotalDeliveries(0);
        riderInfo.setRating(new java.math.BigDecimal("5.0"));
        riderInfoMapper.insert(riderInfo);
        
        return true;
    }

    public boolean reject(String applicationId) {
        LambdaUpdateWrapper<RiderApplication> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RiderApplication::getId, applicationId)
               .eq(RiderApplication::getStatus, "pending")
               .set(RiderApplication::getStatus, "rejected")
               .set(RiderApplication::getReviewedAt, LocalDateTime.now());
        return applicationMapper.update(null, wrapper) > 0;
    }

    public RiderApplication getByStudentId(String studentId) {
        LambdaQueryWrapper<RiderApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiderApplication::getStudentId, studentId)
               .orderByDesc(RiderApplication::getCreatedAt)
               .last("LIMIT 1");
        return applicationMapper.selectOne(wrapper);
    }
}
