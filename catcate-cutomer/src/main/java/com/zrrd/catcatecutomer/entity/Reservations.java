package com.zrrd.catcatecutomer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 预约记录表
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public class Reservations implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预约ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 猫咪ID
     */
    private Long catId;

    /**
     * 预约日期
     */
    private LocalDate reservationDate;

    /**
     * 时间段
     */
    private String timeSlot;

    /**
     * 访客人数
     */
    private Integer visitorCount;

    /**
     * 预约目的
     */
    private String purpose;

    /**
     * 状态：PENDING-待确认, CONFIRMED-已确认, CANCELLED-已取消, COMPLETED-已完成
     */
    private String status;

    /**
     * 管理员备注
     */
    private String adminNotes;

    /**
     * 用户备注
     */
    private String userNotes;

    /**
     * 取消原因
     */
    private String cancelReason;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCatId() {
        return catId;
    }

    public void setCatId(Long catId) {
        this.catId = catId;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Integer getVisitorCount() {
        return visitorCount;
    }

    public void setVisitorCount(Integer visitorCount) {
        this.visitorCount = visitorCount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
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
        return "Reservations{" +
            "id = " + id +
            ", userId = " + userId +
            ", catId = " + catId +
            ", reservationDate = " + reservationDate +
            ", timeSlot = " + timeSlot +
            ", visitorCount = " + visitorCount +
            ", purpose = " + purpose +
            ", status = " + status +
            ", adminNotes = " + adminNotes +
            ", userNotes = " + userNotes +
            ", cancelReason = " + cancelReason +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
        "}";
    }
}
