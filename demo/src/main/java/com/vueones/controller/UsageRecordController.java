package com.vueones.controller;

import com.vueones.entity.UsageRecord;
import com.vueones.service.IUsageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用记录控制器
 * 特别说明：
 * 1. 使用记录处理会自动更新库存
 * 2. 使用前会检查库存是否充足
 * 3. 提供了按用户查询使用记录的功能
 * 4. 支持按时间范围统计使用量
 */
@RestController
@RequestMapping("/usage")
@CrossOrigin(origins = "*")
public class UsageRecordController {
    
    private static final Logger log = LoggerFactory.getLogger(UsageRecordController.class);
    
    @Autowired
    private IUsageRecordService usageRecordService;
    
    /**
     * 添加使用记录
     * @param record 使用记录
     * @return 操作结果
     */
    @PostMapping("/add")
    public ResponseEntity<?> addUsageRecord(@RequestBody UsageRecord record) {
        int result = usageRecordService.addUsageRecord(record);
        if (result > 0) {
            return ResponseEntity.ok(record);
        }
        return ResponseEntity.badRequest().body("添加使用记录失败");
    }
    /**
     * 更新使用记录
     * @param id 使用记录ID
     * @param record 使用记录
     * @return 操作结果
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUsageRecord(@PathVariable Integer id, @RequestBody UsageRecord record) {
        record.setId(id);
        int result = usageRecordService.updateUsageRecord(record);
        if (result > 0) {
            return ResponseEntity.ok(record);
        }
        return ResponseEntity.badRequest().body("更新使用记录失败");
    }
    /**
     * 删除使用记录
     * @param id 使用记录ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUsageRecord(@PathVariable Integer id) {
        int result = usageRecordService.deleteUsageRecord(id);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("删除使用记录失败");
    }
    /**
     * 获取指定使用记录
     * @param id 使用记录ID
     * @return 使用记录
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<Map<String, Object>> getUsageRecord(@PathVariable Integer id) {
        UsageRecord record = usageRecordService.getUsageRecordById(id);
        if (record != null) {
            System.out.println("获取使用记录成功：" + record);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取使用记录成功");
            response.put("data", record);
            return ResponseEntity.status(200).body(response);

        }else{
            System.out.println("获取使用记录失败：" + id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "获取使用记录失败");
            response.put("data", null);
            return ResponseEntity.status(404).body(response);
        }
    }
    /**
     * 获取使用记录列表
     * @param chemicalName 化学品名称
     * @param userName 用户名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 使用记录列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUsageRecords(
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        
        log.info("接收查询参数：chemicalName={}（长度={}）, userName={}（长度={}）, startTime={}, endTime={}, page={}, size={}", 
                chemicalName, chemicalName != null ? chemicalName.length() : 0,
                userName, userName != null ? userName.length() : 0,
                startTime, endTime, page, size);
        
        try {
            // 查询记录
            List<UsageRecord> records = usageRecordService.getUsageRecordList(chemicalName, userName, startTime, endTime);
            
            log.info("查询到记录数量：{}", records.size());
            
            // 手动实现分页
            int total = records.size();
            int fromIndex = (page - 1) * size;
            if (fromIndex < 0) fromIndex = 0;
            if (fromIndex > total) fromIndex = total;
            
            int toIndex = fromIndex + size;
            if (toIndex > total) toIndex = total;
            
            log.info("分页处理：total={}, fromIndex={}, toIndex={}", total, fromIndex, toIndex);
            
            List<UsageRecord> pageRecords = total > 0 && fromIndex < total 
                ? records.subList(fromIndex, toIndex) 
                : new ArrayList<>();
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("records", pageRecords);
            data.put("total", total);
            data.put("size", size);
            data.put("current", page);
            data.put("pages", (total + size - 1) / size);
            
            result.put("code", 200);
            result.put("message", "获取使用记录成功");
            result.put("data", data);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取使用记录失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取使用记录失败：" + e.getMessage());
            result.put("data", null);
            
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取指定化学品的使用记录统计数据
     * @param chemicalId 化学品ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @GetMapping("/statistics/{chemicalId}")
    public ResponseEntity<Map<String, Object>> getTotalUsageAmount(
            @PathVariable Integer chemicalId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        Double totalAmount = usageRecordService.getTotalUsageAmount(chemicalId, startTime, endTime);
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("chemicalId", chemicalId);
        statistics.put("totalAmount", totalAmount);
        statistics.put("startTime", startTime);
        statistics.put("endTime", endTime);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取使用记录统计数据
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statisticsData = new HashMap<>();
            Integer dailyCount = usageRecordService.getTodayUsageCount();
            Integer monthlyCount = usageRecordService.getMonthUsageCount();
            Integer activeCount = usageRecordService.getActiveUsageCount();
            Integer userCount = usageRecordService.getDistinctUserCount();
            
            log.info("统计数据：今日使用次数={}, 本月使用次数={}, 活跃使用次数={}, 使用人数={}",
                    dailyCount, monthlyCount, activeCount, userCount);
            
            statisticsData.put("dailyCount", dailyCount != null ? dailyCount : 0);
            statisticsData.put("monthlyCount", monthlyCount != null ? monthlyCount : 0);
            statisticsData.put("dailyTotal", activeCount != null ? activeCount : 0);
            statisticsData.put("monthlyTotal", userCount != null ? userCount : 0);
            
            // 包装响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取统计数据成功");
            response.put("data", statisticsData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取使用记录统计信息失败", e);
            
            Map<String, Object> statisticsData = new HashMap<>();
            statisticsData.put("dailyCount", 0);
            statisticsData.put("monthlyCount", 0);
            statisticsData.put("dailyTotal", 0);
            statisticsData.put("monthlyTotal", 0);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            response.put("data", statisticsData);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取化学品使用总量
     * @param chemicalName 化学品名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用总量
     */
    @GetMapping("/amount")
    public ResponseEntity<?> getTotalAmount(
            @RequestParam String chemicalName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        String amount = usageRecordService.getTotalUsageAmount(chemicalName, startTime, endTime);
        return ResponseEntity.ok(amount);
    }
    
} 