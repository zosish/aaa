package com.zrrd.catcatecutomer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 活动信息表
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public class Activities implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动内容
     */
    private String content;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 活动类型：DISCOUNT-折扣, EVENT-活动, PROMOTION-促销, SETMEAL-套餐
     */
    private String activityType;

    /**
     * 套餐价格（仅当activity_type为SETMEAL时有意义）
     */
    private BigDecimal setmealPrice;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态：ACTIVE-进行中, ENDED-已结束, CANCELLED-已取消
     */
    private String status;

    public BigDecimal getSetmealPrice() {
        return setmealPrice;
    }

    public void setSetmealPrice(BigDecimal setmealPrice) {
        this.setmealPrice = setmealPrice;
    }

    /**
     * 是否推荐：1-是, 0-否
     */
    private Byte isFeatured;

    /**
     * 浏览次数
     */
    private Integer viewCount;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Byte getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Byte isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
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
        return "Activities{" +
            "id = " + id +
            ", title = " + title +
            ", content = " + content +
            ", coverImage = " + coverImage +
            ", activityType = " + activityType +
            ", startTime = " + startTime +
            ", endTime = " + endTime +
            ", status = " + status +
            ", isFeatured = " + isFeatured +
            ", viewCount = " + viewCount +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
        "}";
    }
}
