package com.ledger.api_user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.api_common.response.Result;
import com.ledger.api_user.model.domain.Permissions;
import com.ledger.api_user.model.domain.SecurityUser;
import com.ledger.api_user.model.domain.UserInfo;
import com.ledger.api_user.model.domain.UserPermissions;
import com.ledger.api_user.model.dto.UserInfoLogin;
import com.ledger.api_user.model.dto.UserInfoRegister;
import com.ledger.api_user.service.UserInfoService;
import com.ledger.api_user.mapper.UserInfoMapper;
import com.ledger.api_user.service.UserPermissionsService;
import com.ledger.api_user.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
* @author 22866
* @description 针对表【user_info(存储用户信息)】的数据库操作Service实现
* @createDate 2023-09-30 17:32:12
*/
@Service
@Transactional
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService{

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Resource
    private UserPermissionsService userPermissionsService;


    private static final BCryptPasswordEncoder bCryptPasswordEncoder;

    static {
        bCryptPasswordEncoder=new BCryptPasswordEncoder();
    }

    @Override
    public Result<String> login(UserInfoLogin user, HttpServletResponse response) {
        String username = user.getUsername();
        UserInfo userByUsername = getUserByUsername(username);
        if (userByUsername == null) {
            return Result.fail("用户名不存在",403);
        }
        if (!bCryptPasswordEncoder.matches(user.getPassword(), userByUsername.getPassword())) {
            return Result.fail("密码错误", 403);
        }
        List<String> auth  = userPermissionsService.getAuthByUserId(userByUsername.getId());
        SecurityUser securityUser = new SecurityUser(userByUsername,auth);
        String token = JwtUtil.createJwt(securityUser, auth,secret);
        response.setHeader("Authorization", tokenHead + " " + token);
        return Result.success("登录成功");
    }
    @Override
    public Result<String> register(UserInfoRegister user, HttpServletResponse response) {
        String username = user.getUsername();
        String password = user.getPassword();
        String password2 = user.getPassword2();
        if(!Objects.equals(password,password2)){
            return Result.fail("两次密码输入不一致", 403);
        }
        if(getUserByUsername(username)!=null){
            return Result.fail("用户已经存在", 403);
        }
        //保存用户
        UserInfo userInfo = new UserInfo();
        String userId = UUID.randomUUID().toString();
        userInfo.setId(userId);
        userInfo.setUsername(user.getUsername());
        userInfo.setPassword(bCryptPasswordEncoder.encode(password));
        //保存权限
        UserPermissions userPermissions = new UserPermissions();
        userPermissions.setId(UUID.randomUUID().toString());
        userPermissions.setUser_id(userId);
        userPermissions.setPermission_id("0");
        save(userInfo);
        userPermissionsService.save(userPermissions);
        return Result.success("注册成功");
    }

    @Override
    public UserInfo getUserByUsername(String username) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUsername, username);
        return getOne(wrapper);
    }


}




