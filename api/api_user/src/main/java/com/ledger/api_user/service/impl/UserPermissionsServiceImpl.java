package com.ledger.api_user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ledger.api_user.mapper.UserPermissionsMapper;
import com.ledger.api_user.model.domain.Permissions;
import com.ledger.api_user.model.domain.UserPermissions;
import com.ledger.api_user.service.UserPermissionsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 22866
* @description 针对表【user_permissions(存储用户和权限之间的关联信息)】的数据库操作Service实现
* @createDate 2023-10-01 18:04:42
*/
@Service
public class UserPermissionsServiceImpl extends ServiceImpl<UserPermissionsMapper, UserPermissions>
    implements UserPermissionsService {


    @Resource
    private UserPermissionsMapper userPermissionsMapper;

    public List<String> getAuthByUserId(String userId) {
        List<Permissions> permissionsByUserId = userPermissionsMapper.getPermissionsByUserId(userId);

        return permissionsByUserId.stream().map(Permissions::getName).collect(Collectors.toList());
    }


    @Override
    public List<Permissions> getPermissionsByUserId(String userId) {
        return userPermissionsMapper.getPermissionsByUserId(userId);
    }



}




