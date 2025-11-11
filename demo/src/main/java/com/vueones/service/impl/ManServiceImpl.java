package com.vueones.service.impl;

import com.vueones.entity.Man;
import com.vueones.mapper.ManMapper;
import com.vueones.service.IManService;
import com.vueones.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManServiceImpl implements IManService {

    @Autowired
    private ManMapper manMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 查询Man数据，支持分页和条件查询
     * @param params 查询参数
     * @return List<Man>
     */
    @Override
    public List<Man> listMans(java.util.Map<String, Object> params) {
        return manMapper.listMans(params);
    }

    /**
     * 查询全部Man数据
     * @return List<Man>
     */
    @Override
    public List<Man> listMans() {
        return manMapper.selectList();
    }
    

    @Override
    public Man selectById(Integer id) {
        return manMapper.selectById(id);
    }
    
    @Override
    public Man selectByEmail(String email) {
        return manMapper.selectByEmail(email);
    }

    /**
     * 新增一条 Man 数据
     *
     * @param man
     * @return
     */
    @Override
    public boolean insertMan(Man man) {
        // 对明文密码进行加密（避免重复加密）
        if (man.getPassword() != null && !man.getPassword().trim().isEmpty()) {
            String pwd = man.getPassword().trim();
            if (!pwd.startsWith("$2a$") && !pwd.startsWith("$2b$") && !pwd.startsWith("$2y$")) {
                man.setPassword(passwordEncoder.encode(pwd));
            }
        }
        return manMapper.insert(man) > 0;
    }

    /**
     * 更新一条 Man 数据
     *
     * @param man
     * @return
     */
    @Override
    public int updateMan(Man man) {
        // 若提交了密码，则进行加密（避免重复加密）
        if (man.getPassword() != null && !man.getPassword().trim().isEmpty()) {
            String pwd = man.getPassword().trim();
            if (!pwd.startsWith("$2a$") && !pwd.startsWith("$2b$") && !pwd.startsWith("$2y$")) {
                man.setPassword(passwordEncoder.encode(pwd));
            }
        }
        return manMapper.updateMan(man);
    }

    /**
     * 根据 id 删除一条 Man 数据
     *
     * @param id
     * @return
     */
    @Override
    public int deleteManById(Integer id) {
        return manMapper.deleteById(id);
    }

    @Override
    public int getTotal(java.util.Map<String, Object> params) {
        return manMapper.getTotal(params);
    }
    
    @Override
    public int updateLastLoginTime(Integer id) {
        return manMapper.updateLastLoginTime(id);
    }
    
    @Override
    public Man login(String email, String password, Integer userType) {
        // 通过邮箱查询用户
        Man man = manMapper.selectByEmail(email);
        
        // 用户不存在或状态禁用
        if (man == null || (man.getStatus() != null && man.getStatus() == 0)) {
            return null;
        }
        
        // 用户类型不匹配
        if (userType != null && man.getUserType() != null && !man.getUserType().equals(userType)) {
            return null;
        }
        
        // 验证密码
        if (man.getPassword() != null && passwordEncoder.matches(password, man.getPassword())) {
            // 更新最后登录时间
            manMapper.updateLastLoginTime(man.getId());
            return man;
        }
        
        return null;
    }
    
    @Override
    public boolean register(Man man) {
        // 检查邮箱是否已存在
        Man existingMan = manMapper.selectByEmail(man.getEmail());
        if (existingMan != null) {
            return false;
        }
        
        // 设置默认值
        if (man.getUserType() == null) {
            man.setUserType(0); // 默认普通用户
        }
        if (man.getStatus() == null) {
            man.setStatus(1); // 默认启用
        }
        
        // 加密密码
        if (man.getPassword() != null) {
            man.setPassword(passwordEncoder.encode(man.getPassword()));
        }
        
        return manMapper.insert(man) > 0;
    }
}