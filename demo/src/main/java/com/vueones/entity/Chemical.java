package com.vueones.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public class Chemical {
    private Integer id;
    private String name;
    private String category;
    private String dangerLevel;
    private String storageCondition;
    private Double warningThreshold;
    private String description;
    
    // 反向引用StorageRecord
    @JsonIgnore
    private List<StorageRecord> storageRecords;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(String dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    public String getStorageCondition() {
        return storageCondition;
    }

    public void setStorageCondition(String storageCondition) {
        this.storageCondition = storageCondition;
    }

    public Double getWarningThreshold() {
        return warningThreshold;
    }

    public void setWarningThreshold(Double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StorageRecord> getStorageRecords() {
        return storageRecords;
    }

    public void setStorageRecords(List<StorageRecord> storageRecords) {
        this.storageRecords = storageRecords;
    }

    @Override
    public String toString() {
        return "Chemical{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", dangerLevel='" + dangerLevel + '\'' +
                ", storageCondition='" + storageCondition + '\'' +
                ", warningThreshold=" + warningThreshold +
                ", description='" + description + '\'' +
                '}';
    }
}
