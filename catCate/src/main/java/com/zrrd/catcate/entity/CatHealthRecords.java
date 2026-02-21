package com.zrrd.catcate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 猫咪健康记录表
 * </p>
 *
 * @author xyd
 * @since 2025-11-24
 */
@TableName("cat_health_records")
public class CatHealthRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 猫咪ID
     */
    private Long catId;

    /**
     * 检查日期
     */
    private LocalDate checkDate;

    /**
     * 健康状态：HEALTHY-健康, SICK-生病, RECOVERING-恢复中
     */
    private String healthStatus;

    /**
     * 症状描述
     */
    private String symptoms;

    /**
     * 处理措施
     */
    private String treatment;

    /**
     * 检查人员
     */
    private String checker;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCatId() {
        return catId;
    }

    public void setCatId(Long catId) {
        this.catId = catId;
    }

    public LocalDate getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(LocalDate checkDate) {
        this.checkDate = checkDate;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getChecker() {
        return checker;
    }

    public void setChecker(String checker) {
        this.checker = checker;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "CatHealthRecords{" +
            "id = " + id +
            ", catId = " + catId +
            ", checkDate = " + checkDate +
            ", healthStatus = " + healthStatus +
            ", symptoms = " + symptoms +
            ", treatment = " + treatment +
            ", checker = " + checker +
            ", remark = " + remark +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
        "}";
    }
}
