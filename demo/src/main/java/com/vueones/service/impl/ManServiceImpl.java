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
    
}