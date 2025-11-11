package com.vueones.controller;

import com.vueones.entity.Inventory;
import com.vueones.service.IInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Random;
import java.text.SimpleDateFormat;

/**
 * 库存管理控制器
 * 特别说明：
 * 1. 支持实时库存监控和阈值预警
 * 2. 入库和出库操作会自动更新库存
 * 3. 支持按化学品名称、存储位置和库存状态查询库存
 * 4. 提供库存不足预警功能
 * 5. 支持批量更新库存信息
 * 6. 提供库存统计和分析功能
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {
    
    @Autowired
    private IInventoryService inventoryService;
    
    /**
     * 添加库存记录
     * @param inventory
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<?> addInventory(@RequestBody Inventory inventory) {
        int result = inventoryService.addInventory(inventory);
        if (result > 0) {
            return ResponseEntity.ok(inventory);
        }
        return ResponseEntity.badRequest().body("添加库存记录失败");
    }

    /**
     * 更新库存记录
     * @param id
     * @param inventory
     * @return
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInventory(@PathVariable Integer id, @RequestBody Inventory inventory) {
        inventory.setId(id);
        int result = inventoryService.updateInventory(inventory);
        if (result > 0) {
            return ResponseEntity.ok(inventory);
        }
        return ResponseEntity.badRequest().body("更新库存记录失败");
    }

    /**
     * 删除库存记录
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable Integer id) {
        int result = inventoryService.deleteInventory(id);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("删除库存记录失败");
    }
    /**
     * 根据id查询库存记录
     * @param id
     * @return
     */    
    @GetMapping("/get/{id}")
    public ResponseEntity<Map<String, Object>> getInventory(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Inventory inventory = inventoryService.getInventoryById(id);
            
            if (inventory != null) {
                response.put("code", 200);
                response.put("message", "获取库存记录成功");
                response.put("data", inventory);
            } else {
                response.put("code", 404);
                response.put("message", "未找到指定库存记录");
                response.put("data", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取库存记录失败: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 根据化学品id和存储位置查询全部库存记录
     * @param chemicalId 化学品ID
     * @param chemicalName 化学品名称
     * @param location 存储位置
     * @param status 库存状态（normal:正常,low:不足,high:超储）
     * @param page 页码
     * @param size 每页记录数
     * @return 库存记录列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getInventoryList(
            @RequestParam(required = false) Integer chemicalId,
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Inventory> inventories = inventoryService.getInventoryList(chemicalId, chemicalName, location, status);
            page = Math.max(1, page);
            Integer offset = (page - 1) * size;
            List<Inventory> pageRecords = inventories.size() > 0 && offset < inventories.size() ? 
                inventories.subList(offset, Math.min(offset + size, inventories.size())) : new ArrayList<>();
            
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", pageRecords);
            pageData.put("total", inventories.size());
            pageData.put("size", size);
            pageData.put("current", page);
            pageData.put("pages", (inventories.size() + size - 1) / size);
            
            response.put("code", 200);
            response.put("message", "获取库存记录成功");
            response.put("data", pageData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取库存记录失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 更新库存数量
     * @param id
     * @param amount
     * @return
     */
    @PutMapping("/update/{id}/amount")
    public ResponseEntity<Map<String, Object>> updateInventoryAmount(
            @PathVariable Integer id,
            @RequestParam Double amount) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int result = inventoryService.updateInventoryAmount(id, amount);
            
            if (result > 0) {
                response.put("code", 200);
                response.put("message", "更新库存数量成功");
                response.put("data", null);
            } else {
                response.put("code", 400);
                response.put("message", "更新库存数量失败");
                response.put("data", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "更新库存数量失败: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 获取低于预警阈值的库存记录
     * @return
     */
    @GetMapping("/below-threshold")
    public ResponseEntity<Map<String, Object>> getBelowThresholdInventory() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Inventory> inventories = inventoryService.getBelowThresholdInventory();
            
            response.put("code", 200);
            response.put("message", "获取低于预警阈值的库存记录成功");
            response.put("data", inventories);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取低于预警阈值的库存记录失败: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 批量更新库存记录
     * @param inventories
     * @return
     */
    @PutMapping("/batch")
    public ResponseEntity<?> batchUpdateInventory(@RequestBody List<Inventory> inventories) {
        int result = inventoryService.batchUpdateInventory(inventories);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("批量更新库存失败");
    }
    /**
     * 获取化学品总库存量
     * @param chemicalId 化学品ID
     * @return 化学品总库存量
     */
    @GetMapping("/getTotalAmount")
    public ResponseEntity<Map<String, Object>> getTotalAmount(
            @RequestParam Integer chemicalId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Double totalAmount = inventoryService.getTotalAmount(chemicalId);
            
            response.put("code", 200);
            response.put("message", "获取库存总量成功");
            response.put("data", totalAmount != null ? totalAmount : 0.0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "获取库存总量失败: " + e.getMessage());
            response.put("data", 0.0);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 入库操作
     * @param chemicalId 化学品ID
     * @param amount 入库数量
     * @return 操作结果
     */
    @PostMapping("/storage-in")
    public ResponseEntity<Map<String, Object>> storageIn(
            @RequestParam Integer chemicalId,
            @RequestParam Double amount) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean result = inventoryService.processStorageIn(chemicalId, amount);
            
            if (result) {
                response.put("code", 200);
                response.put("message", "入库操作成功");
                response.put("data", null);
            } else {
                response.put("code", 400);
                response.put("message", "入库操作失败");
                response.put("data", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "入库操作失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 出库操作
     * @param chemicalId 化学品ID
     * @param amount 出库数量
     * @return 操作结果
     */
    @PostMapping("/storage-out")
    public ResponseEntity<Map<String, Object>> storageOut(
            @RequestParam Integer chemicalId,
            @RequestParam Double amount) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean result = inventoryService.processStorageOut(chemicalId, amount);
            
            if (result) {
                response.put("code", 200);
                response.put("message", "出库操作成功");
                response.put("data", null);
            } else {
                response.put("code", 400);
                response.put("message", "出库操作失败，可能是库存不足");
                response.put("data", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "出库操作失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 获取库存统计数据
     * @param chemicalId 化学品ID
     * @param location 存储位置
     * @return 库存统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false) Integer chemicalId,
            @RequestParam(required = false) String location) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 只查询一次数据库
            List<Inventory> allInventory = inventoryService.getInventoryList(chemicalId, null, location, null);
            
            // 准备符合前端需要的统计数据
            Map<String, Object> data = new HashMap<>();
            
            // 计算总库存种类数
            data.put("totalTypes", (int) allInventory.stream()
                .map(Inventory::getChemicalId)
                .distinct()
                .count());
            
            // 计算库存预警数量 (库存量低于预警阈值的1.2倍但高于预警阈值)
            long warningCount = allInventory.stream()
                .filter(inventory -> inventory.getChemical() != null && 
                       inventory.getCurrentAmount() < inventory.getChemical().getWarningThreshold() * 1.2 &&
                       inventory.getCurrentAmount() >= inventory.getChemical().getWarningThreshold())
                .count();
            data.put("warningCount", warningCount);
            
            // 计算库存不足数量 (库存量低于预警阈值)
            long lowCount = allInventory.stream()
                .filter(inventory -> inventory.getChemical() != null && 
                       inventory.getCurrentAmount() < inventory.getChemical().getWarningThreshold())
                .count();
            data.put("lowCount", lowCount);
            
            // 计算超储数量 (暂时设为0，可以根据业务需求补充逻辑)
            data.put("highCount", 0);
            
            // 总库存记录数
            data.put("totalRecords", allInventory.size());
            
            // 计算总库存量
            double totalAmount = allInventory.stream()
                .mapToDouble(Inventory::getCurrentAmount)
                .sum();
            data.put("totalAmount", totalAmount);
            
            // 低于预警阈值的记录数
            data.put("belowThreshold", lowCount);
            
            response.put("code", 200);
            response.put("message", "获取统计数据成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 获取库存历史记录
     * @param inventoryId 库存ID
     * @return 库存历史记录列表
     */
    @GetMapping("/{inventoryId}/history")
    public ResponseEntity<Map<String, Object>> getInventoryHistory(@PathVariable Integer inventoryId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 这里实际应该调用Service层获取历史记录，暂时返回空列表
            List<Object> historyList = new ArrayList<>();
            
            response.put("code", 200);
            response.put("message", "获取历史记录成功");
            response.put("data", historyList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取历史记录失败: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 获取库存趋势图数据
     * @param chemicalId 化学品ID
     * @return 趋势图数据
     */
    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getInventoryTrend(@RequestParam Integer chemicalId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 模拟数据，实际应该从数据库获取历史库存变化记录
            Map<String, Object> data = new HashMap<>();
            List<String> dates = new ArrayList<>();
            List<Double> amounts = new ArrayList<>();
            
            // 生成近30天的示例数据
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            Random random = new Random();
            double baseAmount = 100.0; // 基础库存量
            
            for (int i = 29; i >= 0; i--) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.DAY_OF_MONTH, -i);
                
                dates.add(sdf.format(cal.getTime()));
                // 生成一个在基础库存量上下浮动的值
                double fluctuation = random.nextDouble() * 20 - 10; // -10到10之间的随机数
                amounts.add(Math.max(0, baseAmount + fluctuation));
            }
            
            data.put("dates", dates);
            data.put("amounts", amounts);
            
            response.put("code", 200);
            response.put("message", "获取趋势图数据成功");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取趋势图数据失败: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.status(200).body(response);
        }
    }

    /**
     * 获取库存盘点功能
     * */ 
    @GetMapping("/inventory-check")
    public ResponseEntity<Map<String, Object>> getInventoryCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取所有库存记录
            List<Inventory> inventories = inventoryService.getInventoryCheck();
            
            // 准备符合前端需要的盘点数据
            Map<String, Object> data = new HashMap<>();
            data.put("code", 200);
            data.put("message", "获取盘点数据成功");
            data.put("data", inventories);
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取盘点数据失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

} 