package com.zrrd.catcatecutomer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * <p>
 * 预约时段设置表
 * </p>
 *
 * @author xyd
 * @since 2026-01-18
 */
@TableName("reservation_time_slots")
public class ReservationTimeSlots implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 时段ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 时间段，例如：09:00-10:00
     */
    private String timeSlot;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 排序字段
     */
    private Integer sortOrder;

    /**
     * 该时段最大可预约人数
     */
    private Integer maxVisitors;

    /**
     * 是否启用：1-是, 0-否
     */
    private Byte isActive;

    /**
     * 时段描述
     */
    private String description;

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

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getMaxVisitors() {
        return maxVisitors;
    }

    public void setMaxVisitors(Integer maxVisitors) {
        this.maxVisitors = maxVisitors;
    }

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return "ReservationTimeSlots{" +
            "id = " + id +
            ", timeSlot = " + timeSlot +
            ", startTime = " + startTime +
            ", endTime = " + endTime +
            ", sortOrder = " + sortOrder +
            ", maxVisitors = " + maxVisitors +
            ", isActive = " + isActive +
            ", description = " + description +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
        "}";
    }
}
