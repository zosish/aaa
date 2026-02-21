package com.zrrd.catcatecutomer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @author xyd
 * @since 2026-01-18
 */
public class Permissions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称（如：用户管理-查看）
     */
    private String permissionName;

    /**
     * 权限编码（如：sys:user:list）
     */
    private String permissionCode;

    /**
     * 权限类型：MENU-菜单, BUTTON-按钮, API-接口
     */
    private String permissionType;

    /**
     * 父权限ID（用于菜单层级）
     */
    private Long parentId;

    /**
     * 权限对应路径（如菜单URL）
     */
    private String path;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序字段
     */
    private Integer sortOrder;

    /**
     * 是否启用：1-是, 0-否
     */
    private Byte isActive;

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

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
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
        return "Permissions{" +
            "id = " + id +
            ", permissionName = " + permissionName +
            ", permissionCode = " + permissionCode +
            ", permissionType = " + permissionType +
            ", parentId = " + parentId +
            ", path = " + path +
            ", icon = " + icon +
            ", sortOrder = " + sortOrder +
            ", isActive = " + isActive +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
        "}";
    }
}
