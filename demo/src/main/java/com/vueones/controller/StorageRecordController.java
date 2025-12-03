package com.vueones.controller;

import com.vueones.entity.StorageRecord;
import com.vueones.entity.Chemical;
import com.vueones.service.IStorageRecordService;
import com.vueones.service.IInventoryService;
import com.vueones.service.IChemicalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 入库记录
 * 特别说明：
 * 1. 支持批量入库操作
 * 2. 入库时自动记录操作时间和操作员
 * 3. 支持按供货商、化学品名称、时间范围查询
 * 4. 提供入库量统计功能
 * 5. 入库记录关联危化品信息
* 6. 支持按批次号追溯
 */
@RestController
@RequestMapping("/storage")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class StorageRecordController {
    
    private static final Logger log = LoggerFactory.getLogger(StorageRecordController.class);
    
    @Autowired
    private IStorageRecordService storageRecordService;
    
    @Autowired
    private IInventoryService inventoryService;
    
    @Autowired
    private IChemicalService chemicalService;
    
    /**
     * 添加一个私有方法，用于从StorageRecord中提取关键信息，并返回简化的Map对象
     * @param record 入库记录
     * @return 简化的Map对象
     */
    private Map<String, Object> simplifyStorageRecord(StorageRecord record) {
        if (record == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("chemicalId", record.getChemicalId());
        result.put("chemicalName", record.getChemicalName());
        result.put("amount", record.getAmount());
        result.put("unit", record.getUnit());
        result.put("batchNo", record.getBatchNo());
        result.put("supplier", record.getSupplier());
        result.put("storageTime", record.getStorageTime());
        result.put("operatorId", record.getOperatorId());
        result.put("notes", record.getNotes());
        result.put("createTime", record.getCreateTime());
        
        // 关联对象
        Chemical chemical = record.getChemical();
        // 如果Mapper未加载关联对象，再从服务层补充
        if (chemical == null && record.getChemicalId() != null) {
            chemical = chemicalService.selectChemicalById(record.getChemicalId());
        }
        if (chemical != null) {
            Map<String, Object> chemicalMap = new HashMap<>();
            chemicalMap.put("id", chemical.getId());
            chemicalMap.put("name", chemical.getName());
            chemicalMap.put("category", chemical.getCategory());
            chemicalMap.put("dangerLevel", chemical.getDangerLevel());
            chemicalMap.put("storageCondition", chemical.getStorageCondition());
            chemicalMap.put("warningThreshold", chemical.getWarningThreshold());
            chemicalMap.put("description", chemical.getDescription());
            result.put("chemical", chemicalMap);
        }
        
        if (record.getOperator() != null) {
            Map<String, Object> operator = new HashMap<>();
            operator.put("id", record.getOperator().getId());
            operator.put("name", record.getOperator().getName());
            operator.put("department", record.getOperator().getDepartment());
            result.put("operator", operator);
        }
        
        return result;
    }

    // 批量简化记录
    private List<Map<String, Object>> simplifyStorageRecords(List<StorageRecord> records) {
        if (records == null) {
            return Collections.emptyList();
        }
        
        return records.stream()
            .map(this::simplifyStorageRecord)
            .collect(Collectors.toList());
    }

    /**
     * 添加入库记录
     * @param record 入库记录
     * @return 操作结果
     */
    @PostMapping("/add")
    public ResponseEntity<?> addStorageRecord(@RequestBody StorageRecord record) {
        log.info("接收到添加入库记录请求: {}", record);
        
        try {
            // 设置创建时间
            if (record.getCreateTime() == null) {
                record.setCreateTime(new Date());
            }
            
            // 设置入库时间，如果没有指定
            if (record.getStorageTime() == null) {
                record.setStorageTime(new Date());
            }
            
            // 如果没有操作员ID，设置默认值
            if (record.getOperatorId() == null) {
                record.setOperatorId(1); // 默认操作员ID
                log.warn("未指定操作员ID，使用默认值: 1");
            }
            
            // 记录关键字段
            log.info("准备添加入库记录: chemicalId={}, amount={}, unit={}, supplier={}, operatorId={}", 
                record.getChemicalId(), record.getAmount(), record.getUnit(), 
                record.getSupplier(), record.getOperatorId());
            
            // 确保化学品ID和名称一致
            if (record.getChemicalId() != null) {
                try {
                    Chemical chemical = chemicalService.selectChemicalById(record.getChemicalId());
                    if (chemical != null) {
                        record.setChemicalName(chemical.getName());
                        log.info("设置化学品名称: {}", chemical.getName());
                    }
                } catch (Exception e) {
                    log.warn("获取化学品信息失败", e);
                }
            }
            
        int result = storageRecordService.addStorageRecord(record);
            
        if (result > 0) {
                log.info("添加入库记录成功, ID: {}", record.getId());
                
                // 添加成功后，更新库存
                boolean inventoryUpdated = inventoryService.processStorageIn(record.getChemicalId(), record.getAmount());
                if (inventoryUpdated) {
                    log.info("库存更新成功: 化学品ID={}, 增加数量={}", record.getChemicalId(), record.getAmount());
                } else {
                    log.warn("库存更新失败: 化学品ID={}, 增加数量={}", record.getChemicalId(), record.getAmount());
                }
                
                // 构建成功响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "添加入库记录成功");
                response.put("data", simplifyStorageRecord(record));
                
                return ResponseEntity.ok(response);
            } else {
                log.error("添加入库记录失败");
                
                // 构建失败响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", "添加入库记录失败");
                
                return ResponseEntity.status(200).body(response);
            }
        } catch (Exception e) {
            log.error("添加入库记录异常", e);
            
            // 构建异常响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "添加入库记录失败: " + e.getMessage());
            
            return ResponseEntity.status(200).body(response);
        }
    }
    /**
     * 更新入库记录
     * @param id 入库记录ID
     * @param record 入库记录
     * @return 操作结果
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateStorageRecord(@PathVariable Integer id, @RequestBody StorageRecord record) {
        log.info("接收到更新入库记录请求: id={}, record={}", id, record);
        
        try {
            // 设置记录ID
        record.setId(id);
            
            // 查询原始记录
            StorageRecord originalRecord = storageRecordService.getStorageRecordById(id);
            if (originalRecord == null) {
                log.warn("未找到ID为 {} 的入库记录", id);
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "未找到指定的入库记录");
                return ResponseEntity.status(200).body(response);
            }
            
            // 记录原始数据，用于计算库存变化
            Integer originalChemicalId = originalRecord.getChemicalId();
            Double originalAmount = originalRecord.getAmount();
            
            // 获取新的化学品信息
            if (record.getChemicalId() != null && !record.getChemicalId().equals(originalChemicalId)) {
                // 如果化学品ID发生变化，需要获取新的化学品信息
                Chemical chemical = null;
                try {
                    chemical = chemicalService.selectChemicalById(record.getChemicalId());
                } catch (Exception e) {
                    log.warn("获取化学品信息失败", e);
                }
                
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
            
            // 设置入库时间，如果没有提供
            if (record.getStorageTime() == null) {
                record.setStorageTime(originalRecord.getStorageTime());
            }
            
            log.info("准备更新入库记录: id={}", id);
            
            // 执行更新操作
        int result = storageRecordService.updateStorageRecord(record);
            
        if (result > 0) {
                log.info("更新入库记录成功: {}", id);
                
                // 调整库存
                boolean inventoryUpdated = true;
                
                // 1. 如果化学品ID发生变化
                if (!originalChemicalId.equals(record.getChemicalId())) {
                    // 从原来的化学品库存中减去原来的入库量
                    inventoryUpdated = inventoryService.processStorageOut(originalChemicalId, originalAmount);
                    if (!inventoryUpdated) {
                        log.warn("调整原化学品库存失败: chemicalId={}, amount={}", originalChemicalId, originalAmount);
                    }
                    
                    // 向新的化学品库存中增加新的入库量
                    inventoryUpdated = inventoryService.processStorageIn(record.getChemicalId(), record.getAmount());
                    if (!inventoryUpdated) {
                        log.warn("调整新化学品库存失败: chemicalId={}, amount={}", record.getChemicalId(), record.getAmount());
                    }
                } 
                // 2. 如果化学品ID相同但数量发生变化
                else if (!originalAmount.equals(record.getAmount())) {
                    // 计算差额
                    double diff = record.getAmount() - originalAmount;
                    
                    if (diff > 0) {
                        // 入库量增加，需要额外增加库存
                        inventoryUpdated = inventoryService.processStorageIn(record.getChemicalId(), diff);
                    } else {
                        // 入库量减少，需要减少库存
                        inventoryUpdated = inventoryService.processStorageOut(record.getChemicalId(), -diff);
                    }
                    
                    if (!inventoryUpdated) {
                        log.warn("调整库存差额失败: chemicalId={}, diff={}", record.getChemicalId(), diff);
                    }
                }
                
                // 构建成功响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "更新入库记录成功");
                response.put("data", simplifyStorageRecord(record));
                
                return ResponseEntity.ok(response);
            } else {
                log.error("更新入库记录失败: {}", id);
                
                // 构建失败响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", "更新入库记录失败");
                
                return ResponseEntity.status(200).body(response);
            }
        } catch (Exception e) {
            log.error("更新入库记录异常", e);
            
            // 构建异常响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新入库记录失败: " + e.getMessage());
            
            return ResponseEntity.status(200).body(response);
        }
    }
    /**
     * 删除入库记录
     * @param id 入库记录ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteStorageRecord(@PathVariable Integer id) {
        log.info("接收到删除入库记录请求: {}", id);
        
        try {
            // 先查询入库记录，获取化学品ID和入库量，用于后续库存调整
            StorageRecord record = storageRecordService.getStorageRecordById(id);
            if (record == null) {
                log.warn("未找到ID为 {} 的入库记录", id);
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "未找到指定的入库记录");
                return ResponseEntity.status(200).body(response);
            }
            
            // 记录原始入库记录信息，用于调整库存
            Integer chemicalId = record.getChemicalId();
            Double amount = record.getAmount();
            
            log.info("准备删除入库记录: id={}, chemicalId={}, amount={}", id, chemicalId, amount);
            
            // 执行删除操作
        int result = storageRecordService.deleteStorageRecord(id);
            
        if (result > 0) {
                log.info("删除入库记录成功: {}", id);
                
                // 删除成功后，调整库存（入库操作会增加库存，删除入库记录后需要减少）
                if (chemicalId != null && amount != null) {
                    boolean inventoryUpdated = inventoryService.processStorageOut(chemicalId, amount);
                    if (inventoryUpdated) {
                        log.info("删除入库记录后调整库存成功: chemicalId={}, amount={}", chemicalId, amount);
                    } else {
                        log.warn("删除入库记录后调整库存失败: chemicalId={}, amount={}", chemicalId, amount);
                    }
                }
                
                // 构建成功响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "删除入库记录成功");
                return ResponseEntity.ok(response);
            } else {
                log.error("删除入库记录失败: {}", id);
                
                // 构建失败响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", "删除入库记录失败");
                return ResponseEntity.status(200).body(response);
            }
        } catch (Exception e) {
            log.error("删除入库记录异常", e);
            
            // 构建异常响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除入库记录失败: " + e.getMessage());
            return ResponseEntity.status(200).body(response);
        }
    }
    /**
     * 获取指定入库记录
     * @param id 入库记录ID
     * @return 入库记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStorageRecord(@PathVariable Integer id) {
        try {
            log.info("接收到获取入库记录请求: {}", id);
            
        StorageRecord record = storageRecordService.getStorageRecordById(id);
            
        if (record != null) {
                log.info("成功获取入库记录: {}", id);
                
                // 准备响应
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "获取入库记录成功");
                response.put("data", simplifyStorageRecord(record));
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("未找到ID为 {} 的入库记录", id);
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "未找到指定的入库记录");
                response.put("data", null);
                
                return ResponseEntity.status(200).body(response);
            }
        } catch (Exception e) {
            log.error("获取入库记录异常", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取入库记录失败: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.status(200).body(response);
        }
    }
    /**
     * 获取入库记录列表
     * @param chemicalId 化学品ID
     * @param chemicalName 化学品名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param supplier 供应商
     * @param page 页码
     * @param size 每页大小
     * @return 入库记录列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getStorageRecordList(
            @RequestParam(required = false) Integer chemicalId,
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        
        log.info("接收到入库记录查询请求：chemicalId={}, chemicalName={}, supplier={}, startTime={}, endTime={}, page={}, size={}", 
                 chemicalId, chemicalName, supplier, startTime, endTime, page, size);
        
        try {
            // 将page调整为基于0的索引
            Integer offset = (page - 1) * size;
            
            // 获取入库记录列表和总数
            List<StorageRecord> records = storageRecordService.getStorageRecordList(chemicalId, chemicalName, supplier, startTime, endTime, offset, size);
            int total = storageRecordService.countStorageRecords(chemicalId, chemicalName, supplier, startTime, endTime);
            
            // 准备分页数据
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", simplifyStorageRecords(records));
            pageData.put("total", total);
            pageData.put("page", page);
            pageData.put("size", size);
            
            // 准备响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取入库记录成功");
            response.put("data", pageData);
            
            log.info("返回响应：记录数={}, 总数={}", records.size(), total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取入库记录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取入库记录失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(200).body(response);
        }
    }
    
    // /**
    //  * 提供测试数据接口，用于前端开发测试
    //  * @return 返回测试数据
    //  */
    // @GetMapping("/test")
    // public ResponseEntity<Map<String, Object>> getTestData() {
    //     log.info("接收到入库记录测试数据请求");
        
    //     try {
    //         // 创建测试数据
    //         List<StorageRecord> testRecords = new ArrayList<>();
            
    //         // 创建化学品
    //         Chemical chemical1 = new Chemical();
    //         chemical1.setId(1);
    //         chemical1.setName("硫酸");
    //         chemical1.setCategory("酸类");
    //         chemical1.setDangerLevel("高危");
            
    //         Chemical chemical2 = new Chemical();
    //         chemical2.setId(2);
    //         chemical2.setName("氢氧化钠");
    //         chemical2.setCategory("碱类");
    //         chemical2.setDangerLevel("中危");
            
    //         // 创建操作员
    //         Man operator = new Man();
    //         operator.setId(3);
    //         operator.setName("王五");
    //         operator.setDepartment("仓储部");
            
    //         // 创建入库记录1
    //         StorageRecord record1 = new StorageRecord();
    //         record1.setId(1);
    //         record1.setChemicalId(1);
    //         record1.setAmount(50.0);
    //         record1.setUnit("kg");
    //         record1.setBatchNo("BATCH-001");
    //         record1.setSupplier("化工供应商A");
    //         record1.setStorageTime(new Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000)); // 10天前
    //         record1.setOperatorId(3);
    //         record1.setNotes("首次入库");
    //         record1.setCreateTime(new Date());
    //         record1.setChemical(chemical1);
    //         record1.setOperator(operator);
            
    //         // 创建入库记录2
    //         StorageRecord record2 = new StorageRecord();
    //         record2.setId(2);
    //         record2.setChemicalId(2);
    //         record2.setAmount(100.0);
    //         record2.setUnit("kg");
    //         record2.setBatchNo("BATCH-002");
    //         record2.setSupplier("化工供应商B");
    //         record2.setStorageTime(new Date(System.currentTimeMillis() - 8 * 24 * 60 * 60 * 1000)); // 8天前
    //         record2.setOperatorId(3);
    //         record2.setNotes("常规补充");
    //         record2.setCreateTime(new Date());
    //         record2.setChemical(chemical2);
    //         record2.setOperator(operator);
            
    //         testRecords.add(record1);
    //         testRecords.add(record2);
            
    //         // 准备分页数据
    //         Map<String, Object> pageData = new HashMap<>();
    //         pageData.put("records", testRecords);
    //         pageData.put("total", testRecords.size());
    //         pageData.put("page", 1);
    //         pageData.put("size", 10);
            
    //         // 准备响应
    //         Map<String, Object> response = new HashMap<>();
    //         response.put("code", 200);
    //         response.put("message", "获取入库记录测试数据成功");
    //         response.put("data", pageData);
            
    //         log.info("返回测试数据：记录数={}", testRecords.size());
            
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         log.error("获取入库记录测试数据失败", e);
    //         Map<String, Object> response = new HashMap<>();
    //         response.put("code", 500);
    //         response.put("message", "获取入库记录测试数据失败: " + e.getMessage());
    //         return ResponseEntity.status(200).body(response);
    //     }
    // }
    
    /**
     * 批量添加入库记录
     * @param records 入库记录列表
     * @return 操作结果
     */
    @PostMapping("/batch")
    public ResponseEntity<?> batchAddStorageRecords(@RequestBody List<StorageRecord> records) {
        log.info("接收到批量添加入库记录请求, 记录数量: {}", records != null ? records.size() : 0);
        
        try {
            if (records == null || records.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "没有提供有效的入库记录数据");
                return ResponseEntity.status(200).body(response);
            }
            
            // 确保每条记录都有化学品名称
            for (StorageRecord record : records) {
                if (record.getChemicalId() != null && (record.getChemicalName() == null || record.getChemicalName().isEmpty())) {
                    try {
                        Chemical chemical = chemicalService.selectChemicalById(record.getChemicalId());
                        if (chemical != null) {
                            record.setChemicalName(chemical.getName());
                        }
                    } catch (Exception e) {
                        log.warn("获取化学品信息失败: {}", e.getMessage());
                    }
                }
                
                // 设置默认值
                if (record.getCreateTime() == null) {
                    record.setCreateTime(new Date());
                }
                if (record.getStorageTime() == null) {
                    record.setStorageTime(new Date());
                }
                if (record.getOperatorId() == null) {
                    record.setOperatorId(1); // 默认操作员ID
                }
            }
            
        int result = storageRecordService.batchAddStorageRecords(records);
            
        if (result > 0) {
                log.info("批量添加入库记录成功, 添加数量: {}", result);
                
                // 批量更新库存
                for (StorageRecord record : records) {
                    try {
                        boolean updated = inventoryService.processStorageIn(record.getChemicalId(), record.getAmount());
                        if (updated) {
                            log.info("更新库存成功: 化学品ID={}, 数量={}", record.getChemicalId(), record.getAmount());
                        } else {
                            log.warn("更新库存失败: 化学品ID={}, 数量={}", record.getChemicalId(), record.getAmount());
                        }
                    } catch (Exception e) {
                        log.error("更新库存异常: 化学品ID={}, 数量={}, 错误: {}", record.getChemicalId(), record.getAmount(), e.getMessage());
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "批量添加入库记录成功，记录数量: " + result);
                return ResponseEntity.ok(response);
            } else {
                log.error("批量添加入库记录失败");
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", "批量添加入库记录失败");
                return ResponseEntity.status(200).body(response);
            }
        } catch (Exception e) {
            log.error("批量添加入库记录异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "批量添加入库记录失败: " + e.getMessage());
            return ResponseEntity.status(200).body(response);
        }
    }
    /**
     * 获取入库量统计数据
     * @param chemicalId 化学品ID
     * @param chemicalName 化学品名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStorageStatistics(
            @RequestParam(required = false) Integer chemicalId,
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        try {
            log.info("接收到入库统计请求：chemicalId={}, chemicalName={}, startTime={}, endTime={}", 
                    chemicalId, chemicalName, startTime, endTime);
            
            // 获取月度统计数据
            Calendar now = Calendar.getInstance();
            Calendar firstDayOfMonth = Calendar.getInstance();
            firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
            firstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0);
            firstDayOfMonth.set(Calendar.MINUTE, 0);
            firstDayOfMonth.set(Calendar.SECOND, 0);
            
            // 月度入库总量
            Double monthlyTotal = storageRecordService.sumStorageAmount(chemicalId, chemicalName, firstDayOfMonth.getTime(), now.getTime());
            
            // 月度入库记录数
            Integer monthlyCount = storageRecordService.getMonthlyStorageCount(chemicalId, chemicalName);
            
            // 月度入库次数
            Integer monthlyTimes = storageRecordService.getMonthlyStorageTimes(chemicalId, chemicalName);
            
            // 获取日统计数据
            Calendar startOfDay = Calendar.getInstance();
            startOfDay.set(Calendar.HOUR_OF_DAY, 0);
            startOfDay.set(Calendar.MINUTE, 0);
            startOfDay.set(Calendar.SECOND, 0);
            
            Calendar endOfDay = Calendar.getInstance();
            endOfDay.set(Calendar.HOUR_OF_DAY, 23);
            endOfDay.set(Calendar.MINUTE, 59);
            endOfDay.set(Calendar.SECOND, 59);
            
            // 日入库总量
            Double dailyTotal = storageRecordService.sumStorageAmount(chemicalId, chemicalName, startOfDay.getTime(), endOfDay.getTime());
            
            // 日入库记录数
            Integer dailyCount = storageRecordService.getDailyStorageCount(chemicalId, chemicalName);
            
            // 日入库次数
            Integer dailyTimes = storageRecordService.getDailyStorageTimes(chemicalId, chemicalName);
            
            // 准备统计数据
        Map<String, Object> statistics = new HashMap<>();
            statistics.put("monthlyTotal", monthlyTotal != null ? monthlyTotal : 0.0);
            statistics.put("monthlyCount", monthlyCount != null ? monthlyCount : 0);
            statistics.put("monthlyTimes", monthlyTimes != null ? monthlyTimes : 0);
            statistics.put("dailyTotal", dailyTotal != null ? dailyTotal : 0.0);
            statistics.put("dailyCount", dailyCount != null ? dailyCount : 0);
            statistics.put("dailyTimes", dailyTimes != null ? dailyTimes : 0);
            
            // 准备响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取入库统计数据成功");
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取入库统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取入库统计数据失败: " + e.getMessage());
            return ResponseEntity.status(200).body(response);
        }
    }

    /**
     * 导出入库记录数据
     * @param chemicalName 化学品名称（可选）
     * @param supplier 供应商（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 导出的CSV文件
     */
    @GetMapping("/export")
    public ResponseEntity<?> export(
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        try {
            log.info("接收到导出入库记录请求：chemicalName={}, supplier={}, startTime={}, endTime={}", 
                    chemicalName, supplier, startTime, endTime);
            
            // 查询符合条件的数据（不分页，获取所有）
            List<StorageRecord> records = storageRecordService.getStorageRecordList(null, chemicalName, supplier, startTime, endTime, null, null);
            
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
            csvContent.append("ID,化学品ID,化学品名称,入库数量,单位,批次号,供应商,入库时间,操作员ID,备注,创建时间\n");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            for (StorageRecord record : records) {
                csvContent.append(String.format("%d,%d,\"%s\",%s,%s,\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"\n",
                    record.getId(),
                    record.getChemicalId(),
                    record.getChemicalName() != null ? record.getChemicalName().replace("\"", "\"\"") : "",
                    record.getAmount(),
                    record.getUnit(),
                    record.getBatchNo() != null ? record.getBatchNo().replace("\"", "\"\"") : "",
                    record.getSupplier() != null ? record.getSupplier().replace("\"", "\"\"") : "",
                    record.getStorageTime() != null ? dateFormat.format(record.getStorageTime()) : "",
                    record.getOperatorId(),
                    record.getNotes() != null ? record.getNotes().replace("\"", "\"\"") : "",
                    record.getCreateTime() != null ? dateFormat.format(record.getCreateTime()) : ""));
            }
            
            // 将CSV内容写入字节数组
            baos.write(csvContent.toString().getBytes("UTF-8"));
            
            // 设置响应头
            String filename = URLEncoder.encode("入库记录数据.csv", "UTF-8");
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(baos.toByteArray());
        } catch (Exception e) {
            log.error("导出入库记录失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "导出入库记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 提供入库记录导入模板
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
            csvContent.append("化学品ID,化学品名称,入库数量,单位,批次号,供应商,入库时间,操作员ID,备注\n");
            csvContent.append("1,硫酸,50,kg,BATCH-001,化工供应商A,2023-01-01 10:00:00,1,示例备注\n");
            csvContent.append("2,氢氧化钠,100,kg,BATCH-002,化工供应商B,2023-01-02 10:00:00,1,示例备注\n");
            
            // 将CSV内容写入字节数组
            baos.write(csvContent.toString().getBytes("UTF-8"));
            
            // 设置响应头
            String filename = URLEncoder.encode("入库记录导入模板.csv", "UTF-8");
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(baos.toByteArray());
        } catch (Exception e) {
            log.error("获取入库记录导入模板失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "获取入库记录导入模板失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 导入入库记录数据
     * @param file 导入的文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseEntity<?> importData(@RequestParam("file") MultipartFile file) {
        try {
            log.info("接收到入库记录导入请求，文件名: {}", file.getOriginalFilename());
            
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
            
            // 处理CSV文件 - 这里只是一个示例，需要使用专门的CSV解析库
            List<StorageRecord> records = new ArrayList<>();
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
                if (columns.length < 7) {
                    continue; // 跳过格式不正确的行
                }
                
                try {
                    StorageRecord record = new StorageRecord();
                    record.setChemicalId(Integer.parseInt(columns[0].trim()));
                    record.setChemicalName(columns[1].trim());
                    record.setAmount(Double.parseDouble(columns[2].trim()));
                    record.setUnit(columns[3].trim());
                    record.setBatchNo(columns[4].trim());
                    record.setSupplier(columns[5].trim());
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    record.setStorageTime(dateFormat.parse(columns[6].trim()));
                    
                    if (columns.length > 7) {
                        record.setOperatorId(Integer.parseInt(columns[7].trim()));
                    } else {
                        record.setOperatorId(1); // 默认操作员ID
                    }
                    
                    if (columns.length > 8) {
                        record.setNotes(columns[8].trim());
                    }
                    
                    record.setCreateTime(new Date());
                    records.add(record);
                } catch (Exception e) {
                    log.warn("解析行数据失败: {}", line, e);
                    // 继续处理下一行
                }
            }
            
            if (records.isEmpty()) {
                log.warn("没有有效的入库记录数据");
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "没有有效的入库记录数据");
                return ResponseEntity.status(400).body(error);
            }
            
            // 批量添加记录
            int result = storageRecordService.batchAddStorageRecords(records);
            
            if (result > 0) {
                log.info("成功导入 {} 条入库记录", records.size());
                
                // 更新库存
                for (StorageRecord record : records) {
                    try {
                        boolean updated = inventoryService.processStorageIn(record.getChemicalId(), record.getAmount());
                        if (updated) {
                            log.info("更新库存成功: 化学品ID={}, 增加数量={}", record.getChemicalId(), record.getAmount());
                        } else {
                            log.warn("更新库存失败: 化学品ID={}, 增加数量={}", record.getChemicalId(), record.getAmount());
                        }
                    } catch (Exception e) {
                        log.error("更新库存异常: 化学品ID={}, 数量={}, 错误: {}", record.getChemicalId(), record.getAmount(), e.getMessage());
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "成功导入 " + records.size() + " 条入库记录");
                return ResponseEntity.ok(response);
            } else {
                log.error("导入入库记录失败");
                Map<String, Object> error = new HashMap<>();
                error.put("code", 500);
                error.put("message", "导入入库记录失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            log.error("导入入库记录数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "导入入库记录数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}