package com.zrrd.catcate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 评价表
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public class Reviews implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称（关联查询字段）
     */
    private String userNickname;

    /**
     * 用户头像（关联查询字段）
     */
    private String userAvatar;

    /**
     * 评价对象类型：CAT-猫咪, PRODUCT-商品, SERVICE-服务
     */
    private String targetType;

    /**
     * 评价对象ID
     */
    private Long targetId;

    /**
     * 评价对象名称（关联查询字段）
     */
    private String targetName;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 订单编号（关联查询字段）
     */
    private String orderNumber;

    /**
     * 评分（1-5分）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片URL（多个用逗号分隔）
     */
    private String images;

    /**
     * 是否匿名：1-是, 0-否
     */
    private Byte isAnonymous;

    /**
     * 状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝
     */
    private String status;

    /**
     * 管理员回复
     */
    private String adminReply;

    /**
     * 点赞数
     */
    private Integer likeCount;

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

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Byte getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Byte isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
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

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return "Reviews{" +
            "id = " + id +
            ", userId = " + userId +
            ", targetType = " + targetType +
            ", targetId = " + targetId +
            ", orderId = " + orderId +
            ", rating = " + rating +
            ", content = " + content +
            ", images = " + images +
            ", isAnonymous = " + isAnonymous +
            ", status = " + status +
            ", adminReply = " + adminReply +
            ", likeCount = " + likeCount +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
        "}";
    }
}
