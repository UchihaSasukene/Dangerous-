package com.vueones.entity;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * UsageRecord 类表示使用记录。
 * 它包含了使用记录的各种属性，如记录ID、危化品ID、用户ID、危化品名称、用户名、使用量、使用单位、使用时间、使用目的、备注、创建时间等。
 * 此外，它还包含了与危化品和用户相关联的信息。
 */

public class UsageRecord {
    private Integer id;
    private Integer chemicalId;
    private Integer userId;
    private String chemicalName;
    private String userName;    
    private Double amount;
    private String unit;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date usageTime;
    private String usagePurpose;
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 关联的危化品信息和用户信息
     private Chemical chemical;
     private Man user;

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getChemicalName() {
        return chemicalName;
    }

    public void setChemicalName(String chemicalName) {
        this.chemicalName = chemicalName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Date getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(Date usageTime) {
        this.usageTime = usageTime;
    }

    public String getUsagePurpose() {
        return usagePurpose;
    }

    public void setUsagePurpose(String usagePurpose) {
        this.usagePurpose = usagePurpose;
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
         if (chemical != null) {
             this.chemicalName = chemical.getName();
         }
     }
    
     public Man getUser() {
         return user;
     }
    
     public void setUser(Man user) {
         this.user = user;
         if (user != null) {
             this.userName = user.getName();
         }
     }

    @Override
    public String toString() {
        return "UsageRecord{" +
                "id=" + id +
                ", chemicalId=" + chemicalId +
                ", userId=" + userId +
                ", chemicalName=" + chemicalName +
                ", userName=" + userName +
                ", amount=" + amount +
                ", unit='" + unit + '\'' +
                ", usageTime=" + usageTime +
                ", usagePurpose='" + usagePurpose + '\'' +
                ", notes='" + notes + '\'' +
                ", createTime=" + createTime +
                ", chemical=" + chemical +
                ", user=" + user +
                '}';
    }
}