package com.vueones.controller;

import com.vueones.entity.OutboundRecord;
import com.vueones.entity.Chemical;
import com.vueones.entity.Man;
import com.vueones.entity.Inventory;
import com.vueones.service.IOutboundRecordService;
import com.vueones.service.IChemicalService;
import com.vueones.service.IInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;

/**
 * 出库记录控制器
 * 特别说明：
 * 1. 支持批量出库操作
 * 2. 出库时自动记录操作时间和操作员
 * 3. 支持按领用人、用途查询
 * 4. 提供出库量统计功能
 * 5. 出库记录关联危化品信息
 * 6. 支持按批次号追溯
 */
@RestController
@RequestMapping("/outbound")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class OutboundRecordController {
    
    private static final Logger log = LoggerFactory.getLogger(OutboundRecordController.class);
    
    @Autowired
    private IOutboundRecordService outboundRecordService;
    
    @Autowired
    private IChemicalService chemicalService;
    
    @Autowired
    private IInventoryService inventoryService;
    
    /**
     * 添加出库记录
     * @param record 出库记录
     * @return 是否成功
     */
    @PostMapping("/add")
    public ResponseEntity<?> addOutboundRecord(@RequestBody OutboundRecord record) {
        log.info("接收到添加出库记录请求: {}", record);
        
        try {
            // 设置创建时间
            if (record.getCreateTime() == null) {
                record.setCreateTime(new Date());
            }
            
            // 设置出库时间，如果没有指定
            if (record.getOutboundTime() == null) {
                record.setOutboundTime(new Date());
            }
            
            // 如果没有设置操作员ID，使用默认值
            if (record.getOperatorId() == null) {
                record.setOperatorId(1); // 默认操作员
                log.warn("未指定操作员ID，使用默认值: 1");
            }
            
            // 根据化学品ID获取库存信息
            if (record.getChemicalId() != null) {
                // 获取化学品信息
                Chemical chemical = chemicalService.selectChemicalById(record.getChemicalId());
                if (chemical != null) {
                    log.info("找到化学品信息: {}", chemical.getName());
                    
                    // 设置化学品名称
                    if (chemical.getName() != null) {
                        record.setChemicalName(chemical.getName());
                    }
                    
                    // 获取当前库存
                    Double currentInventory = inventoryService.getTotalAmount(record.getChemicalId());
                    log.info("当前库存数量: {}", currentInventory);
                    
                    // 检查库存是否足够
                    if (currentInventory == null || currentInventory < record.getAmount()) {
                        log.warn("库存不足，当前库存: {}, 需要出库: {}", currentInventory, record.getAmount());
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 400);
                        response.put("message", "库存不足，当前库存: " + currentInventory + ", 需要出库: " + record.getAmount());
                        return ResponseEntity.status(400).body(response);
                    }
                } else {
                    log.warn("找不到ID为 {} 的化学品信息", record.getChemicalId());
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 400);
                    response.put("message", "找不到指定的化学品");
                    return ResponseEntity.status(400).body(response);
                }
            } else {
                log.error("缺少化学品ID");
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "缺少化学品ID");
                return ResponseEntity.status(400).body(response);
            }
            
            // 执行出库操作
        int result = outboundRecordService.addOutboundRecord(record);
            
        if (result > 0) {
                // 出库成功，更新库存
                boolean inventoryUpdated = inventoryService.processStorageOut(record.getChemicalId(), record.getAmount());
                
                if (inventoryUpdated) {
                    log.info("出库成功，ID: {}, 库存已更新", record.getId());
                } else {
                    log.warn("出库记录已创建，但库存更新失败");
                }
                
                // 构建成功响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "添加出库记录成功");
                response.put("data", record);
                
                return ResponseEntity.ok(response);
            } else {
                log.error("添加出库记录失败");
                
                // 构建失败响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", "添加出库记录失败");
                
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("添加出库记录异常", e);
            
            // 构建异常响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "添加出库记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取化学品当前库存信息
     * @param chemicalId 化学品ID
     * @return 库存信息
     */
    @GetMapping("/inventory/{chemicalId}")
    public ResponseEntity<?> getChemicalInventory(@PathVariable Integer chemicalId) {
        try {
            // 获取化学品信息
            Chemical chemical = chemicalService.selectChemicalById(chemicalId);
            if (chemical == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "找不到指定的化学品");
                return ResponseEntity.status(404).body(response);
            }
            
            // 获取库存信息
            Double currentAmount = inventoryService.getTotalAmount(chemicalId);
            List<Inventory> inventoryList = inventoryService.getInventoryList(chemicalId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("chemicalId", chemicalId);
            data.put("chemicalName", chemical.getName());
            data.put("totalAmount", currentAmount != null ? currentAmount : 0.0);
            data.put("inventoryList", inventoryList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取库存信息成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取库存信息异常", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取库存信息失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 更新出库记录
     * @param id 出库记录id
     * @param record 出库记录
     * @return 是否成功
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOutboundRecord(@PathVariable Integer id, @RequestBody OutboundRecord record) {
        log.info("接收到更新出库记录请求: id={}, record={}", id, record);
        
        try {
            // 设置记录ID
        record.setId(id);
            
            // 查询原始记录
            OutboundRecord originalRecord = outboundRecordService.getOutboundRecordById(id);
            if (originalRecord == null) {
                log.warn("未找到ID为 {} 的出库记录", id);
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "未找到指定的出库记录");
                return ResponseEntity.status(404).body(response);
            }
            
            // 记录原始数据，用于计算库存变化
            Integer originalChemicalId = originalRecord.getChemicalId();
            Double originalAmount = originalRecord.getAmount();
            
            // 获取新的化学品信息
            if (record.getChemicalId() != null && !record.getChemicalId().equals(originalChemicalId)) {
                // 如果化学品ID发生变化，需要检查新的化学品
                Chemical chemical = chemicalService.selectChemicalById(record.getChemicalId());
                if (chemical != null) {
                    log.info("化学品ID发生变化，设置新的化学品名称: {}", chemical.getName());
                    record.setChemicalName(chemical.getName());
                } else {
                    log.warn("找不到ID为 {} 的化学品信息", record.getChemicalId());
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 400);
                    response.put("message", "找不到指定的化学品");
                    return ResponseEntity.status(200).body(response);
                }
            } else if (record.getChemicalId() == null) {
                // 如果未提供化学品ID，使用原始记录的值
                record.setChemicalId(originalChemicalId);
                record.setChemicalName(originalRecord.getChemicalName());
            }
            
            // 检查库存是否足够（如果出库量增加或者化学品变化）
            boolean needCheckInventory = false;
            Integer chemicalIdToCheck = record.getChemicalId();
            Double amountToCheck = record.getAmount();
            
            if (chemicalIdToCheck != null && amountToCheck != null) {
                if (!chemicalIdToCheck.equals(originalChemicalId)) {
                    // 化学品变化，需要完整检查新化学品的库存
                    needCheckInventory = true;
                } else if (amountToCheck > originalAmount) {
                    // 同一化学品，但出库量增加，需要检查额外的库存量
                    amountToCheck = amountToCheck - originalAmount;
                    needCheckInventory = true;
                }
            }
            
            if (needCheckInventory) {
                Double currentInventory = inventoryService.getTotalAmount(chemicalIdToCheck);
                log.info("检查库存: chemicalId={}, 需要的库存={}, 当前库存={}", chemicalIdToCheck, amountToCheck, currentInventory);
                
                if (currentInventory == null || currentInventory < amountToCheck) {
                    log.warn("库存不足，当前库存: {}, 需要出库: {}", currentInventory, amountToCheck);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 400);
                    response.put("message", "库存不足，当前库存: " + currentInventory + ", 需要出库: " + amountToCheck);
                    return ResponseEntity.status(400).body(response);
                }
            }
            
            // 设置更新时间
            if (record.getOutboundTime() == null) {
                record.setOutboundTime(originalRecord.getOutboundTime());
            }
            
            log.info("准备更新出库记录: id={}", id);
            
            // 执行更新操作
        int result = outboundRecordService.updateOutboundRecord(record);
            
        if (result > 0) {
                log.info("更新出库记录成功: {}", id);
                
                // 调整库存
                boolean inventoryUpdated = true;
                
                // 1. 如果化学品ID发生变化
                if (!originalChemicalId.equals(record.getChemicalId())) {
                    // 将原来的化学品库存恢复
                    inventoryUpdated = inventoryService.processStorageIn(originalChemicalId, originalAmount);
                    if (!inventoryUpdated) {
                        log.warn("恢复原化学品库存失败: chemicalId={}, amount={}", originalChemicalId, originalAmount);
                    }
                    
                    // 从新的化学品库存中扣除
                    inventoryUpdated = inventoryService.processStorageOut(record.getChemicalId(), record.getAmount());
                    if (!inventoryUpdated) {
                        log.warn("更新新化学品库存失败: chemicalId={}, amount={}", record.getChemicalId(), record.getAmount());
                    }
                } 
                // 2. 如果化学品ID相同但数量发生变化
                else if (!originalAmount.equals(record.getAmount())) {
                    // 计算差额
                    double diff = record.getAmount() - originalAmount;
                    
                    if (diff > 0) {
                        // 出库量增加，需要额外减少库存
                        inventoryUpdated = inventoryService.processStorageOut(record.getChemicalId(), diff);
                    } else {
                        // 出库量减少，需要增加库存
                        inventoryUpdated = inventoryService.processStorageIn(record.getChemicalId(), -diff);
                    }
                    
                    if (!inventoryUpdated) {
                        log.warn("调整库存差额失败: chemicalId={}, diff={}", record.getChemicalId(), diff);
                    }
                }
                
                // 构建成功响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "更新出库记录成功");
                response.put("data", record);
                
                return ResponseEntity.ok(response);
            } else {
                log.error("更新出库记录失败: {}", id);
                
                // 构建失败响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", "更新出库记录失败");
                
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("更新出库记录异常", e);
            
            // 构建异常响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新出库记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 删除出库记录
     * @param id 出库记录id
     * @return 是否成功
     */    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOutboundRecord(@PathVariable Integer id) {
        log.info("接收到删除出库记录请求: {}", id);
        
        try {
            // 先查询出库记录，获取化学品ID和出库量，用于后续库存恢复
            OutboundRecord record = outboundRecordService.getOutboundRecordById(id);
            if (record == null) {
                log.warn("未找到ID为 {} 的出库记录", id);
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "未找到指定的出库记录");
                return ResponseEntity.status(404).body(response);
            }
            
            // 记录原始出库记录信息，用于恢复库存
            Integer chemicalId = record.getChemicalId();
            Double amount = record.getAmount();
            
            log.info("准备删除出库记录: id={}, chemicalId={}, amount={}", id, chemicalId, amount);
            
            // 执行删除操作
        int result = outboundRecordService.deleteOutboundRecord(id);
            
        if (result > 0) {
                log.info("删除出库记录成功: {}", id);
                
                // 删除成功后，恢复库存（出库操作会减少库存，删除出库记录后需要恢复）
                if (chemicalId != null && amount != null) {
                    boolean inventoryUpdated = inventoryService.processStorageIn(chemicalId, amount);
                    if (inventoryUpdated) {
                        log.info("删除出库记录后恢复库存成功: chemicalId={}, amount={}", chemicalId, amount);
                    } else {
                        log.warn("删除出库记录后恢复库存失败: chemicalId={}, amount={}", chemicalId, amount);
                    }
                }
                
                // 构建成功响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "删除出库记录成功");
                return ResponseEntity.ok(response);
            } else {
                log.error("删除出库记录失败: {}", id);
                
                // 构建失败响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", "删除出库记录失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("删除出库记录异常", e);
            
            // 构建异常响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除出库记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 根据id查询出库记录
     * @param id 出库记录id
     * @return 出库记录
     */    
    @GetMapping("/get/{id}")
    public ResponseEntity<OutboundRecord> getOutboundRecord(@PathVariable Integer id) {
        OutboundRecord record = outboundRecordService.getOutboundRecordById(id);
        if (record != null) {
            return ResponseEntity.ok(record);
        }
        return ResponseEntity.notFound().build();
    }
    /**
     * 根据化学品名称、领用人、用途查询出库记录
     * @param chemicalId 化学品id
     * @param chemicalName 化学品名称
     * @param recipient 领用人
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 出库记录列表
     */         
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getOutboundRecordList(
            @RequestParam(required = false) Integer chemicalId,
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        log.info("接收到出库记录查询请求：chemicalId={}, chemicalName={}, recipient={}, startTime={}, endTime={}, page={}, size={}",
                chemicalId, chemicalName, recipient, startTime, endTime, page, size);
                
        try {
            // 调整页码，转为从0开始的偏移量
            Integer offset = page > 0 ? (page - 1) * size : 0;
            
            log.info("调整后的分页参数：offset={}, size={}", offset, size);
            
            // 修正参数顺序与接口定义匹配
            List<OutboundRecord> records = outboundRecordService.getOutboundRecordList(
                chemicalId, chemicalName, recipient, startTime, endTime, offset, size);
            
            // 获取记录总数
            int total = outboundRecordService.countOutboundRecords(
                chemicalId, chemicalName, recipient, startTime, endTime);
            
            log.info("服务层返回记录总数：{}", (records != null ? records.size() : "null"));
            if (records != null && !records.isEmpty()) {
                log.debug("第一条记录示例：{}", records.get(0));
            } else {
                log.warn("查询结果为空，请检查查询参数或数据库中是否有对应记录");
            }
            
            // 构建分页对象
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", records);
            pageData.put("total", total);
            pageData.put("size", size);
            pageData.put("current", page);
            pageData.put("pages", (total + size - 1) / size);
            
            // 包装响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取出库记录成功");
            response.put("data", pageData);
            
            log.info("返回响应：记录数={}, 总数={}", records.size(), total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取出库记录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取出库记录失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 批量添加出库记录
     * @param records 出库记录列表
     * @return 是否成功
     */    
    @PostMapping("/batch")
    public ResponseEntity<?> batchAddOutboundRecords(@RequestBody List<OutboundRecord> records) {
        int result = outboundRecordService.batchAddOutboundRecords(records);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("批量添加出库记录失败");
    }
    /**
     * 获取出库量统计
     * @param chemicalId 化学品id（可选）
     * @param chemicalName 化学品名称（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 出库量统计
     */     
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false) Integer chemicalId,
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        try {
            log.info("接收到出库统计请求：chemicalId={}, chemicalName={}, startTime={}, endTime={}", 
                    chemicalId, chemicalName, startTime, endTime);
                    
            // 获取月度统计数据
            Calendar now = Calendar.getInstance();
            Calendar firstDayOfMonth = Calendar.getInstance();
            firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
            firstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0);
            firstDayOfMonth.set(Calendar.MINUTE, 0);
            firstDayOfMonth.set(Calendar.SECOND, 0);
            
            // 月度出库总量
            Double monthlyTotal = outboundRecordService.sumAmountByChemicalId(
                chemicalId, firstDayOfMonth.getTime(), now.getTime());
            
            // 月度出库记录数
            Integer monthlyCount = outboundRecordService.getMonthlyOutboundCount(chemicalId, chemicalName);
            
            // 月度出库次数
            Integer monthlyTimes = outboundRecordService.getMonthlyOutboundTimes(chemicalId, chemicalName);
            
            // 获取日统计数据
            Calendar startOfDay = Calendar.getInstance();
            startOfDay.set(Calendar.HOUR_OF_DAY, 0);
            startOfDay.set(Calendar.MINUTE, 0);
            startOfDay.set(Calendar.SECOND, 0);
            
            Calendar endOfDay = Calendar.getInstance();
            endOfDay.set(Calendar.HOUR_OF_DAY, 23);
            endOfDay.set(Calendar.MINUTE, 59);
            endOfDay.set(Calendar.SECOND, 59);
            
            // 日出库总量
            Double dailyTotal = outboundRecordService.sumAmountByChemicalId(
                chemicalId, startOfDay.getTime(), endOfDay.getTime());
            
            // 日出库记录数
            Integer dailyCount = outboundRecordService.getDailyOutboundCount(chemicalId, chemicalName);
            
            // 日出库次数
            Integer dailyTimes = outboundRecordService.getDailyOutboundTimes(chemicalId, chemicalName);
            
            // 获取总量
            Double totalAmount = outboundRecordService.sumAmountByChemicalId(chemicalId, null, null);
            
            // 创建统计结果
            Map<String, Object> statisticsData = new HashMap<>();
            statisticsData.put("monthlyTotal", monthlyTotal != null ? monthlyTotal : 0.0);
            statisticsData.put("monthlyCount", monthlyCount != null ? monthlyCount : 0);
            statisticsData.put("monthlyTimes", monthlyTimes != null ? monthlyTimes : 0);
            statisticsData.put("dailyTotal", dailyTotal != null ? dailyTotal : 0.0);
            statisticsData.put("dailyCount", dailyCount != null ? dailyCount : 0);
            statisticsData.put("dailyTimes", dailyTimes != null ? dailyTimes : 0);
            statisticsData.put("totalAmount", totalAmount != null ? totalAmount : 0.0);
            
            // 包装响应对象
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取统计数据成功");
            response.put("data", statisticsData);
            
            log.info("统计数据：monthlyTotal={}, monthlyCount={}, monthlyTimes={}, dailyTotal={}, dailyCount={}, dailyTimes={}, totalAmount={}",
                    monthlyTotal, monthlyCount, monthlyTimes, dailyTotal, dailyCount, dailyTimes, totalAmount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取出库统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(200).body(response);
        }
    }
    /**
     * 测试接口，返回一条测试出库记录
     * @return 测试出库记录
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testData() {
        try {
            log.info("调用出库测试接口");
            
            // 创建测试数据
            List<OutboundRecord> testRecords = new ArrayList<>();
            
            // 创建化学品
            Chemical chemical1 = new Chemical();
            chemical1.setId(1);
            chemical1.setName("硫酸");
            chemical1.setCategory("酸类");
            chemical1.setDangerLevel("高危");
            
            Chemical chemical2 = new Chemical();
            chemical2.setId(2);
            chemical2.setName("氢氧化钠");
            chemical2.setCategory("碱类");
            chemical2.setDangerLevel("中危");
            
            // 创建操作员
            Man operator = new Man();
            operator.setId(3);
            operator.setName("王五");
            operator.setDepartment("仓储部");
            
            // 创建出库记录1
            OutboundRecord record1 = new OutboundRecord();
            record1.setId(1);
            record1.setChemicalId(1);
            record1.setAmount(10.0);
            record1.setUnit("kg");
            record1.setBatchNo("OUT-001");
            record1.setRecipient("张三");
            record1.setPurpose("实验使用");
            record1.setOutboundTime(new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000)); // 3天前
            record1.setOperatorId(3);
            record1.setNotes("常规领用");
            record1.setCreateTime(new Date());
            record1.setChemical(chemical1);
            record1.setOperator(operator);
            
            // 创建出库记录2
            OutboundRecord record2 = new OutboundRecord();
            record2.setId(2);
            record2.setChemicalId(2);
            record2.setAmount(20.0);
            record2.setUnit("kg");
            record2.setBatchNo("OUT-002");
            record2.setRecipient("李四");
            record2.setPurpose("质检使用");
            record2.setOutboundTime(new Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)); // 2天前
            record2.setOperatorId(3);
            record2.setNotes("加急领用");
            record2.setCreateTime(new Date());
            record2.setChemical(chemical2);
            record2.setOperator(operator);
            
            testRecords.add(record1);
            testRecords.add(record2);
            
            // 构建分页对象
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", testRecords);
            pageData.put("total", testRecords.size());
            pageData.put("size", 10);
            pageData.put("current", 1);
            pageData.put("pages", 1);
            
            // 包装响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "测试数据获取成功");
            response.put("data", pageData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("测试接口出错", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "测试接口出错: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(200).body(response);
        }
    }

    /**
     * 导出出库记录数据
     * @param chemicalName 化学品名称（可选）
     * @param recipient 领用人（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 导出的CSV文件
     */
    @GetMapping("/export")
    public ResponseEntity<?> export(
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        try {
            log.info("接收到导出出库记录请求：chemicalName={}, recipient={}, startTime={}, endTime={}", 
                    chemicalName, recipient, startTime, endTime);
            
            // 查询符合条件的数据（不分页，获取所有）
            List<OutboundRecord> records = outboundRecordService.getOutboundRecordList(null, chemicalName, recipient, startTime, endTime, null, null);
            
            if (records == null || records.isEmpty()) {
                log.warn("没有找到符合条件的数据");
                Map<String, Object> error = new HashMap<>();
                error.put("code", 404);
                error.put("message", "没有找到符合条件的数据");
                return ResponseEntity.status(404).body(error);
            }
            
            // 创建字节数组输出流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // 写入UTF-8 BOM标记
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // 生成CSV内容
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("ID,化学品ID,化学品名称,出库数量,单位,批次号,领用人,用途,出库时间,操作员ID,备注,创建时间\n");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            for (OutboundRecord record : records) {
                csvContent.append(String.format("%d,%d,\"%s\",%s,%s,\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"\n",
                    record.getId(),
                    record.getChemicalId(),
                    record.getChemicalName() != null ? record.getChemicalName().replace("\"", "\"\"") : "",
                    record.getAmount(),
                    record.getUnit(),
                    record.getBatchNo() != null ? record.getBatchNo().replace("\"", "\"\"") : "",
                    record.getRecipient() != null ? record.getRecipient().replace("\"", "\"\"") : "",
                    record.getPurpose() != null ? record.getPurpose().replace("\"", "\"\"") : "",
                    record.getOutboundTime() != null ? dateFormat.format(record.getOutboundTime()) : "",
                    record.getOperatorId(),
                    record.getNotes() != null ? record.getNotes().replace("\"", "\"\"") : "",
                    record.getCreateTime() != null ? dateFormat.format(record.getCreateTime()) : ""));
            }
            
            // 将CSV内容写入字节数组
            baos.write(csvContent.toString().getBytes("UTF-8"));
            
            // 设置响应头
            String filename = URLEncoder.encode("出库记录数据.csv", "UTF-8");
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(baos.toByteArray());
        } catch (Exception e) {
            log.error("导出出库记录失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "导出出库记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 提供出库记录导入模板
     * @return 返回模板文件
     */
    @GetMapping("/template")
    public ResponseEntity<?> template() {
        try {
            // 创建字节数组输出流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // 写入UTF-8 BOM标记
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);
            
            // 生成模板CSV内容
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("化学品ID,化学品名称,出库数量,单位,批次号,领用人,用途,出库时间,操作员ID,备注\n");
            csvContent.append("1,硫酸,20,kg,OUT-001,张三,实验使用,2023-01-01 10:00:00,1,示例备注\n");
            csvContent.append("2,氢氧化钠,30,kg,OUT-002,李四,质检使用,2023-01-02 10:00:00,1,示例备注\n");
            
            // 将CSV内容写入字节数组
            baos.write(csvContent.toString().getBytes("UTF-8"));
            
            // 设置响应头
            String filename = URLEncoder.encode("出库记录导入模板.csv", "UTF-8");
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(baos.toByteArray());
        } catch (Exception e) {
            log.error("获取出库记录导入模板失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "获取出库记录导入模板失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 导入出库记录数据
     * @param file 导入的文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseEntity<?> importData(@RequestParam("file") MultipartFile file) {
        try {
            log.info("接收到出库记录导入请求，文件名: {}", file.getOriginalFilename());
            
            if (file.isEmpty()) {
                log.warn("上传的文件为空");
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "上传的文件为空");
                return ResponseEntity.status(400).body(error);
            }
            
            // 检查文件格式
            String filename = file.getOriginalFilename();
            if (filename == null || !(filename.endsWith(".csv") || filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
                log.warn("不支持的文件格式: {}", filename);
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "不支持的文件格式，请上传CSV或Excel文件");
                return ResponseEntity.status(400).body(error);
            }
            
            // 处理CSV文件 - 这里只是一个示例，实际实现可能需要使用专门的CSV解析库
            List<OutboundRecord> records = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    // 跳过表头
                    isFirstLine = false;
                    continue;
                }
                
                String[] columns = line.split(",");
                if (columns.length < 8) {
                    continue; // 跳过格式不正确的行
                }
                
                try {
                    OutboundRecord record = new OutboundRecord();
                    record.setChemicalId(Integer.parseInt(columns[0].trim()));
                    record.setChemicalName(columns[1].trim());
                    record.setAmount(Double.parseDouble(columns[2].trim()));
                    record.setUnit(columns[3].trim());
                    record.setBatchNo(columns[4].trim());
                    record.setRecipient(columns[5].trim());
                    record.setPurpose(columns[6].trim());
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    record.setOutboundTime(dateFormat.parse(columns[7].trim()));
                    
                    if (columns.length > 8) {
                        record.setOperatorId(Integer.parseInt(columns[8].trim()));
                    } else {
                        record.setOperatorId(1); // 默认操作员ID
                    }
                    
                    if (columns.length > 9) {
                        record.setNotes(columns[9].trim());
                    }
                    
                    record.setCreateTime(new Date());
                    
                    // 检查库存是否足够
                    Double currentInventory = inventoryService.getTotalAmount(record.getChemicalId());
                    if (currentInventory == null || currentInventory < record.getAmount()) {
                        log.warn("库存不足，化学品ID: {}, 当前库存: {}, 需要出库: {}", 
                                 record.getChemicalId(), currentInventory, record.getAmount());
                        continue; // 跳过库存不足的记录
                    }
                    
                    records.add(record);
                } catch (Exception e) {
                    log.warn("解析行数据失败: {}", line, e);
                    // 继续处理下一行
                }
            }
            
            if (records.isEmpty()) {
                log.warn("没有有效的出库记录数据");
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "没有有效的出库记录数据，可能是格式错误或库存不足");
                return ResponseEntity.status(400).body(error);
            }
            
            // 批量添加记录
            int result = outboundRecordService.batchAddOutboundRecords(records);
            
            if (result > 0) {
                log.info("成功导入 {} 条出库记录", records.size());
                
                // 更新库存
                for (OutboundRecord record : records) {
                    inventoryService.processStorageOut(record.getChemicalId(), record.getAmount());
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "成功导入 " + records.size() + " 条出库记录");
                return ResponseEntity.ok(response);
            } else {
                log.error("导入出库记录失败");
                Map<String, Object> error = new HashMap<>();
                error.put("code", 500);
                error.put("message", "导入出库记录失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            log.error("导入出库记录数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "导入出库记录数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
} 