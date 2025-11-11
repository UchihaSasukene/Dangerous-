package com.vueones.controller;

import com.vueones.entity.Man;
import com.vueones.service.IManService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.log4j.Log4j2;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/man")
@CrossOrigin(origins = "*")
@Log4j2
public class ManController {

    @Autowired
    private IManService manService;


    /**
     * 查询所有员工信息
     * @return 员工信息列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", (page - 1) * size);
        params.put("size", size);
        params.put("name", name);
        params.put("phone", phone);
        params.put("gender", gender);
        params.put("email", email);
        params.put("department", department);
        params.put("position", position);
        try {
            List<Man> list = manService.listMans(params);
            int total = manService.getTotal(params);

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "获取人员列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }

    }

    /**
     * 根据ID获取人员信息
     * @param id 人员ID 
     * @return 人员信息
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id) {
        try {
            Man man = manService.selectById(id);
            if (man == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 404);
                error.put("message", "人员不存在");

                return ResponseEntity.status(404).body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", man);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "获取人员信息失败: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 新增员工
     * @param man 员工信息
     * @return 是否成功
     */
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody Man man) {
        try {
            // 手动验证
            Map<String, Object> error = validateMan(man);
            if (error != null) {
                return ResponseEntity.status(400).body(error);
            }

            boolean result = manService.insertMan(man);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "添加成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", 500);
                errorResponse.put("message", "添加失败");
                return ResponseEntity.status(500).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "添加员工失败：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 更新员工
     * @param man 员工信息
     * @return 是否成功
     */
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Man man) {
        try {
            // 验证ID
            if (man.getId() == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "id不存在");
                return ResponseEntity.status(400).body(error);
            }

            // 手动验证其他字段
            Map<String, Object> error = validateMan(man);
            if (error != null) {
                return ResponseEntity.status(400).body(error);
            }

            // 检查人员是否存在
            Man existingMan = manService.selectById(man.getId());
            if (existingMan == null) {
                Map<String, Object> notFoundError = new HashMap<>();
                notFoundError.put("code", 404);
                notFoundError.put("message", "人员不存在");
                return ResponseEntity.status(404).body(notFoundError);
            }

            int result = manService.updateMan(man);
            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "更新成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> updateError = new HashMap<>();
                updateError.put("code", 500);
                updateError.put("message", "更新失败");
                return ResponseEntity.status(500).body(updateError);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "更新人员失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 验证Man对象的字段
     * @param man 待验证的对象
     * @return 如果验证失败返回错误信息，验证成功返回null
     */
    private Map<String, Object> validateMan(Man man) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", 400);

        // 验证姓名
        if (man.getName() == null || man.getName().trim().isEmpty()) {
            error.put("message", "姓名不能为空");
            return error;
        }
        if (man.getName().length() < 1 || man.getName().length() > 50) {
            error.put("message", "姓名长度必须在1-50个字符之间");
            return error;
        }

        // 验证性别
        if (man.getGender() != null && !man.getGender().trim().isEmpty() 
            && !man.getGender().matches("^(男|女|其他)$")) {
            error.put("message", "性别只能是男、女或其他");
            return error;
        }

        // 验证电话
        if (man.getPhone() != null && !man.getPhone().trim().isEmpty() 
            && !man.getPhone().matches("^1[1-9]\\d{9}$")) {
            error.put("message", "请输入正确的手机号码");
            return error;
        }

        // 验证邮箱
        if (man.getEmail() != null && !man.getEmail().trim().isEmpty() 
            && !man.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            error.put("message", "请输入正确的邮箱地址");
            return error;
        }

        // 验证部门
        if (man.getDepartment() != null && man.getDepartment().length() > 50) {
            error.put("message", "部门名称不能超过50个字符");
            return error;
        }

        // 验证职位
        if (man.getPosition() != null && man.getPosition().length() > 50) {
            error.put("message", "职位名称不能超过50个字符");
            return error;
        }

        return null;
    }

    /**
     * 删除人员
     * @param id 人员ID
     * @return 是否成功
     */
    @GetMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Integer id) {
        try {
            // 检查人员是否存在
            Man existingMan = manService.selectById(id);
            if (existingMan == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 404);
                error.put("message", "人员不存在");

                return ResponseEntity.status(404).body(error);
            }

            int result = manService.deleteManById(id);
            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "删除成功");

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 500);
                error.put("message", "删除失败");

                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "删除人员失败: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 批量删除人员
     * @param params 包含ids的Map
     * @return 操作结果
     */
    @PostMapping("/batchDelete")
    public ResponseEntity<?> batchDelete(@RequestBody Map<String, List<Integer>> params) {
        try {
            List<Integer> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "请选择要删除的记录");
                return ResponseEntity.status(400).body(error);
            }

            int successCount = 0;
            List<Integer> failedIds = new ArrayList<>();

            for (Integer id : ids) {
                try {
                    int result = manService.deleteManById(id);
                    if (result > 0) {
                        successCount++;
                    } else {
                        failedIds.add(id);
                    }
                } catch (Exception e) {
                    failedIds.add(id);
                }
            }

            Map<String, Object> response = new HashMap<>();
            if (failedIds.isEmpty()) {
                response.put("code", 200);
                response.put("message", String.format("成功删除 %d 条记录", successCount));
            } else {
                response.put("code", 207);
                response.put("message", String.format("成功删除 %d 条记录，失败 %d 条", 
                    successCount, failedIds.size()));
                response.put("failedIds", failedIds);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "批量删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 导出人员数据
     * @param name 姓名
     * @param phone 手机号
     * @param gender 性别
     * @param email 邮箱
     * @param department 部门
     * @param position 职位
     */
    @GetMapping("/export")
    public ResponseEntity<?> export(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position) {
        try {
            // 记录导出请求的日志
            log.info("接收到导出人员数据请求：name={}, phone={}, gender={}, email={}, department={}, position={}", 
                    name, phone, gender, email, department, position);
            
            Map<String, Object> params = new HashMap<>();
            params.put("name", name);
            params.put("phone", phone);
            params.put("gender", gender);
            params.put("email", email);
            params.put("department", department);
            params.put("position", position);

            List<Man> list = manService.listMans(params);
            
            if (list == null || list.isEmpty()) {
                log.warn("没有找到符合条件的数据");
                Map<String, Object> error = new HashMap<>();
                error.put("code", 404);
                error.put("message", "没有找到符合条件的数据");
                return ResponseEntity.status(404).body(error);
            }

            // 创建字节数组输出流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // 写入UTF-8 BOM标记，确保Excel能正确识别中文
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // 生成CSV内容
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("ID,姓名,性别,手机号,邮箱,部门,职位,创建时间\n");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            for (Man man : list) {
                csvContent.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    man.getId(),
                    man.getName() != null ? man.getName().replace("\"", "\"\"") : "",
                    man.getGender() != null ? man.getGender().replace("\"", "\"\"") : "",
                    man.getPhone() != null ? man.getPhone().replace("\"", "\"\"") : "",
                    man.getEmail() != null ? man.getEmail().replace("\"", "\"\"") : "",
                    man.getDepartment() != null ? man.getDepartment().replace("\"", "\"\"") : "",
                    man.getPosition() != null ? man.getPosition().replace("\"", "\"\"") : "",
                    man.getCreateTime() != null ? dateFormat.format(man.getCreateTime()) : ""));
            }
            
            // 将CSV内容写入字节数组
            baos.write(csvContent.toString().getBytes("UTF-8"));
            
            // 设置响应头
            String filename = URLEncoder.encode("人员数据.csv", "UTF-8");
            
            log.info("成功导出{}条人员数据记录", list.size());
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(baos.toByteArray());

        } catch (Exception e) {
            log.error("导出人员数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "导出失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

}