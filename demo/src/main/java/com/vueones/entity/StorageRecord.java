package com.vueones.entity;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class StorageRecord {
    private Integer id;
    private Integer chemicalId;
    private String chemicalName;
    private Integer inventoryId;
    private Double amount;
    private String unit;
    private String batchNo;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date storageTime;
    private Integer operatorId;
    private String supplier;
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    // 关联对象
     private Chemical chemical;
     private Inventory inventory;
    private Man operator;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChemicalId() {
        return chemicalId;
    }

    public void setChemicalId(Integer chemicalId) {
        this.chemicalId = chemicalId;
    }
    
    public String getChemicalName() {
        return chemicalName;
    }

    public void setChemicalName(String chemicalName) {
        this.chemicalName = chemicalName;
    }

    public Integer getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Integer inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Date getStorageTime() {
        return storageTime;
    }

    public void setStorageTime(Date storageTime) {
        this.storageTime = storageTime;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

     public Chemical getChemical() {
         return chemical;
     }

     public void setChemical(Chemical chemical) {
         this.chemical = chemical;
     }

     public Inventory getInventory() {
         return inventory;
     }

     public void setInventory(Inventory inventory) {
         this.inventory = inventory;
     }

    public Man getOperator() {
        return operator;
    }

    public void setOperator(Man operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "StorageRecord{" +
                "id=" + id +
                ", chemicalId=" + chemicalId +
                ", chemicalName=" + chemicalName +
                ", inventoryId=" + inventoryId +
                ", amount=" + amount +
                ", unit='" + unit + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", storageTime=" + storageTime +
                ", operatorId=" + operatorId +
                ", supplier='" + supplier + '\'' +
                ", notes='" + notes + '\'' +
                ", createTime=" + createTime + '\'' +
                ", chemical=" + chemical +
                ", inventory=" + inventory +
                ", operator=" + operator +
                '}';
    }
}