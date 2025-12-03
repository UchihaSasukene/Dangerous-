package com.vueones.mapper;

import com.vueones.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录用户表操作
 */
@Mapper
public interface UserMapper {

    User selectById(@Param("id") Integer id);

    User selectByEmail(@Param("email") String email);

    int insert(User user);

    int updateLastLoginTime(@Param("id") Integer id);

    int insertRegisterRecord(@Param("email") String email,
                             @Param("name") String name,
                             @Param("userType") Integer userType,
                             @Param("registerIp") String registerIp,
                             @Param("registerChannel") String registerChannel);
}

