package com.zrrd.catcatecutomer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 桌号信息表
 * </p>
 *
 * @author xyd
 * @since 2026-05-13
 */
@TableName("tables")
public class Tables implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 桌号ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 桌号（如：1, 2, 3...）
     */
    private String tableNumber;

    /**
     * 座位数
     */
    private Integer capacity;

    /**
     * 状态：AVAILABLE-可用, OCCUPIED-占用, RESERVED-已预约, MAINTENANCE-维护中
     */
    private String status;

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

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        return "Tables{" +
        "id=" + id +
        ", tableNumber=" + tableNumber +
        ", capacity=" + capacity +
        ", status=" + status +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        "}";
    }
}