package com.zrrd.catcatecutomer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 猫咪信息表
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public class Cats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 猫咪ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 猫咪名字
     */
    private String name;

    /**
     * 品种
     */
    private String breed;

    /**
     * 年龄（月）
     */
    private Integer age;

    /**
     * 性别：MALE-公, FEMALE-母
     */
    private String gender;

    /**
     * 健康状况：HEALTHY-健康, SICK-生病, RECOVERING-恢复中
     */
    private String healthStatus;

    /**
     * 性格描述
     */
    private String personality;

    /**
     * 照片URL
     */
    private String photoUrl;

    /**
     * 领养状态：AVAILABLE-可领养, ADOPTED-已被领养
     */
    private String adoptionStatus;

    /**
     * 日常护理信息
     */
    private String dailyCare;

    /**
     * 是否活跃：1-是, 0-否
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAdoptionStatus() {
        return adoptionStatus;
    }

    public void setAdoptionStatus(String adoptionStatus) {
        this.adoptionStatus = adoptionStatus;
    }

    public String getDailyCare() {
        return dailyCare;
    }

    public void setDailyCare(String dailyCare) {
        this.dailyCare = dailyCare;
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
        return "Cats{" +
            "id = " + id +
            ", name = " + name +
            ", breed = " + breed +
            ", age = " + age +
            ", gender = " + gender +
            ", healthStatus = " + healthStatus +
            ", personality = " + personality +
            ", photoUrl = " + photoUrl +
            ", adoptionStatus = " + adoptionStatus +
            ", dailyCare = " + dailyCare +
            ", isActive = " + isActive +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
        "}";
    }
}
