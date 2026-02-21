<template>
    <div class="activity-management">
        <!-- 活动列表页面 -->
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h1>活动管理</h1>
            <el-button type="primary" icon="Plus" @click="handleAddActivity" style="padding-left: 0px;">
                新增活动
            </el-button>
        </div>

        <!-- 搜索和筛选区 -->
        <el-card class="filter-card">
            <el-row :gutter="20">
                <el-col :span="6">
                    <el-input v-model="searchForm.title" placeholder="搜索活动标题" clearable prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.activityType" placeholder="选择活动类型" clearable>
                        <el-option label="折扣活动" value="DISCOUNT"></el-option>
                        <el-option label="主题活动" value="EVENT"></el-option>
                        <el-option label="促销活动" value="PROMOTION"></el-option>
                        <el-option label="套餐" value="SETMEAL"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.status" placeholder="选择活动状态" clearable>
                        <el-option label="进行中" value="ACTIVE"></el-option>
                        <el-option label="已结束" value="ENDED"></el-option>
                        <el-option label="已取消" value="CANCELLED"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.isFeatured" placeholder="是否推荐" clearable>
                        <el-option label="推荐" value="1"></el-option>
                        <el-option label="不推荐" value="0"></el-option>
                    </el-select>
                </el-col>
            </el-row>
            <div class="filter-actions">
                <el-button type="primary" @click="handleSearch">
                    搜索
                </el-button>
                <el-button @click="resetSearch">
                    重置
                </el-button>
                <el-button type="danger" icon="Delete" @click="handleBatchDelete" style="padding-left: 0px;"
                    :disabled="selectedActivities.length === 0">
                    批量删除
                </el-button>
                <el-button type="warning" icon="Refresh" @click="handleBatchStatusChange" style="padding-left: 0px;"
                    :disabled="selectedActivities.length === 0">
                    {{ batchStatusText }}
                </el-button>
                <el-button type="success" icon="Star" @click="handleBatchFeatured" style="padding-left: 0px;"
                    :disabled="selectedActivities.length === 0">
                    {{ batchFeaturedText }}
                </el-button>
            </div>
        </el-card>

        <!-- 活动列表 -->
        <el-card class="table-card">
            <el-table :data="activityList" border stripe :loading="loading" @selection-change="handleSelectionChange"
                :row-class-name="tableRowClassName">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="id" label="活动ID" width="80" align="center"></el-table-column>
                <el-table-column label="封面图片" width="100" align="center">
                    <template #default="scope">
                        <el-image :src="scope.row.coverImage || defaultImage"
                            :preview-src-list="[scope.row.coverImage || defaultImage]" width="60" height="60"
                            fit="cover" class="activity-image"></el-image>
                    </template>
                </el-table-column>
                <el-table-column prop="title" label="活动标题" min-width="180"></el-table-column>
                <el-table-column prop="activityType" label="活动类型" min-width="120" align="center">
                    <template #default="scope">
                        <el-tag :type="getActivityTypeTagType(scope.row.activityType)">
                            {{ formatActivityType(scope.row.activityType) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <!-- 添加套餐价格列 -->
                <el-table-column label="套餐价格" min-width="120" align="center">
                    <template #default="scope">
                        <div v-if="scope.row.activityType === 'SETMEAL'">
                            <el-tag type="danger" size="small">
                                ¥{{ scope.row.setmealPrice ? parseFloat(scope.row.setmealPrice).toFixed(2) : '0.00' }}
                            </el-tag>
                        </div>
                        <div v-else>-</div>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="活动状态" min-width="120" align="center">
                    <template #default="scope">
                        <el-tag :type="getActivityStatusTagType(scope.row.status)" style="color: white;">
                            {{ formatActivityStatus(scope.row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="isFeatured" label="是否推荐" min-width="120" align="center">
                    <template #default="scope">
                        <el-switch v-model="scope.row.isFeatured" active-color="#ff9f43" inactive-color="#ccc"
                            :active-value="1" :inactive-value="0" :disabled="false"
                            @change="(val) => handleFeaturedChange(scope.row, val)">
                        </el-switch>
                        <div>{{ scope.row.isFeatured === 1 ? '推荐' : '不推荐' }}</div>
                    </template>
                </el-table-column>
                <el-table-column prop="startTime" label="开始时间" min-width="180" align="center"></el-table-column>
                <el-table-column prop="endTime" label="结束时间" min-width="180" align="center"></el-table-column>
                <!-- 在表格中添加一列显示剩余时间 -->
                <el-table-column label="剩余时间" min-width="120" align="center">
                    <template #default="scope">
                        <div v-if="scope.row.status === 'ACTIVE'">
                            {{ calculateRemainingTime(scope.row.endTime) }}
                        </div>
                        <div v-else>-</div>
                    </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="浏览次数" min-width="100" align="center">
                    <template #default="scope">
                        <el-tag type="info" size="mini" style="color: #fff;">
                            {{ scope.row.viewCount || 0 }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="180" align="center"></el-table-column>
                <el-table-column label="操作" min-width="280" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" icon="Eye" @click="handleViewActivity(scope.row)"
                            style="padding-left: 0px;">查看详情</el-button>
                        <el-button size="small" type="success" icon="Edit" @click="handleEditActivity(scope.row)"
                            style="padding-left: 0px;" :disabled="scope.row.status === 'ENDED'">编辑活动</el-button>
                        <el-button size="small" :type="getActionButtonType(scope.row.status)"
                            :icon="getActionButtonIcon(scope.row.status)" @click="handleStatusChange(scope.row)"
                            style="padding-left: 0px;">
                            {{ getActionButtonText(scope.row.status) }}
                        </el-button>
                        <el-button size="small" type="danger" icon="Delete" @click="handleDeleteActivity(scope.row)"
                            style="padding-left: 0px;">删除活动</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
                <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
                    :page-sizes="[10, 20, 50]" :total="pagination.total"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"></el-pagination>
            </div>
        </el-card>

        <!-- 活动详情/新增/编辑弹窗 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" :close-on-click-modal="false"
            append-to-body>
            <el-form ref="activityFormRef" :model="activityForm" :rules="formRules" label-width="120px"
                class="activity-form">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="活动标题" prop="title">
                            <el-input v-model="activityForm.title" placeholder="请输入活动标题"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="活动类型" prop="activityType">
                            <el-select v-model="activityForm.activityType" placeholder="请选择活动类型" @change="handleActivityTypeChange">
                                <el-option label="折扣活动" value="DISCOUNT"></el-option>
                                <el-option label="主题活动" value="EVENT"></el-option>
                                <el-option label="促销活动" value="PROMOTION"></el-option>
                                <el-option label="套餐" value="SETMEAL"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <!-- 套餐价格输入框，仅当活动类型为套餐时显示 -->
                    <el-col :span="12" v-if="activityForm.activityType === 'SETMEAL'">
                        <el-form-item label="套餐价格" prop="setmealPrice">
                            <el-input 
                                v-model.number="activityForm.setmealPrice" 
                                placeholder="请输入套餐价格"
                                type="number"
                                :min="0"
                                step="0.01"
                                :disabled="dialogType === 'view'"
                            >
                                <template #prefix>¥</template>
                            </el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="活动状态" prop="status" v-if="dialogType !== 'view'">
                            <el-select v-model="activityForm.status" placeholder="请选择活动状态">
                                <el-option label="进行中" value="ACTIVE"></el-option>
                                <el-option label="已结束" value="ENDED"></el-option>
                                <el-option label="已取消" value="CANCELLED"></el-option>
                            </el-select>
                        </el-form-item>
                        <el-form-item label="活动状态" v-if="dialogType === 'view'">
                            <el-input :value="formatActivityStatus(activityForm.status)" disabled
                                placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="是否推荐" prop="isFeatured" v-if="dialogType !== 'view'">
                            <el-switch v-model="activityForm.isFeatured" active-color="#ff9f43" inactive-color="#ccc"
                                active-value="1" inactive-value="0"></el-switch>
                        </el-form-item>
                        <el-form-item label="是否推荐" v-if="dialogType === 'view'">
                            <el-input :value="activityForm.isFeatured === '1' ? '推荐' : '不推荐'" disabled
                                placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="开始时间" prop="startTime">
                            <el-date-picker v-model="activityForm.startTime" type="datetime" placeholder="选择开始时间"
                                :disabled="dialogType === 'view' || activityForm.status === 'ENDED'"
                                style="width: 100%;"></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="结束时间" prop="endTime">
                            <el-date-picker v-model="activityForm.endTime" type="datetime" placeholder="选择结束时间"
                                :disabled="dialogType === 'view' || activityForm.status === 'ENDED'"
                                style="width: 100%;"></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="封面图片" prop="coverImage" v-if="dialogType !== 'view'">
                            <div class="image-upload-container">
                                <!-- 文件选择区域 -->
                                <div class="upload-area" @click="triggerFileInput">
                                    <input 
                                        ref="fileInput" 
                                        type="file" 
                                        accept="image/*" 
                                        @change="handleFileSelect" 
                                        style="display: none;"
                                    />
                                    <div v-if="!activityForm.coverImage" class="upload-placeholder">
                                        <el-icon size="40" color="#c0c4cc"><Plus /></el-icon>
                                        <p>点击上传封面图片</p>
                                        <p class="upload-tips">支持JPG、PNG格式，建议尺寸16:9，大小不超过8MB</p>
                                    </div>
                                    <div v-else class="uploaded-preview">
                                        <img :src="activityForm.coverImage" alt="封面图片" class="preview-image" />
                                        <div class="preview-overlay">
                                            <el-button 
                                                type="primary" 
                                                icon="Edit" 
                                                circle 
                                                size="small" 
                                                @click.stop="triggerFileInput"
                                            ></el-button>
                                            <el-button 
                                                type="danger" 
                                                icon="Delete" 
                                                circle 
                                                size="small" 
                                                @click.stop="removeCoverImage"
                                            ></el-button>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- 上传进度条 -->
                                <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
                                    <el-progress :percentage="uploadProgress" :stroke-width="6" />
                                </div>
                                
                                <!-- 错误提示 -->
                                <div v-if="uploadError" class="upload-error">
                                    <el-alert :title="uploadError" type="error" show-icon closable @close="uploadError = ''" />
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item label="封面图片" v-if="dialogType === 'view'">
                            <el-image :src="activityForm.coverImage || defaultImage" width="200" height="112"
                                fit="cover" :preview-src-list="[activityForm.coverImage || defaultImage]"></el-image>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="活动内容" prop="content">
                            <el-input v-model="activityForm.content" type="textarea" :rows="8"
                                placeholder="请输入活动详细内容（包括活动规则、参与方式、注意事项等）" :disabled="dialogType === 'view'"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="dialogType === 'view'">
                        <el-form-item label="浏览次数">
                            <el-input v-model="activityForm.viewCount" disabled placeholder="0"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="dialogType === 'view'">
                        <el-form-item label="创建时间">
                            <el-input v-model="activityForm.createTime" disabled placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="dialogType === 'view'">
                        <el-form-item label="更新时间">
                            <el-input v-model="activityForm.updateTime" disabled placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <!-- 在查看模式下显示套餐价格 -->
                    <el-col :span="12" v-if="dialogType === 'view' && activityForm.activityType === 'SETMEAL'">
                        <el-form-item label="套餐价格">
                            <el-input 
                                :value="'¥' + (activityForm.setmealPrice ? parseFloat(activityForm.setmealPrice).toFixed(2) : '0.00')" 
                                disabled
                            ></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSaveActivity" v-if="dialogType !== 'view'"
                    :disabled="activityForm.status === 'ENDED'">
                    保存
                </el-button>
            </template>
        </el-dialog>

        <!-- 批量操作确认弹窗 -->
        <el-dialog v-model="batchConfirmVisible" :title="batchConfirmTitle" width="400px" :close-on-click-modal="false">
            <p>{{ batchConfirmContent }}</p>
            <template #footer>
                <el-button @click="batchConfirmVisible = false">取消</el-button>
                <el-button
                    :type="batchActionType === 'delete' ? 'danger' : (batchActionType === 'status' ? 'warning' : 'success')"
                    @click="confirmBatchAction">
                    确认{{ batchActionText }}
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<!-- eslint-disable no-unused-vars -->
<script setup>
import { ref, reactive, onMounted, nextTick, computed, onUnmounted } from 'vue';

import { ElMessage, ElMessageBox } from 'element-plus';

import {
    Plus, Search, Eye, Edit, Delete, Check, Close,
    Refresh, Star, StarOff, Time, Calendar,
    SortUp
} from '@element-plus/icons-vue';

// 状态管理
const loading = ref(false);
const activityList = ref([]);
const selectedActivities = ref([]);
const batchActionType = ref('status'); // status: 状态切换, delete: 删除, featured: 推荐切换
const activityFormRef = ref(null);
const defaultImage = 'https://picsum.photos/seed/activity/800/450';
const uploadUrl = 'http://localhost:8081/catcate/activities/uploadImage'; // 图片上传接口
// 图片上传相关引用
const fileInput = ref(null);
const uploadProgress = ref(0);
const uploadError = ref('');
// 添加定时器引用
const statusCheckTimer = ref(null);
// 搜索表单
const searchForm = reactive({
    title: '',
    activityType: '',
    status: '',
    isFeatured: ''
});

// 分页配置
const pagination = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0
});

// 弹窗相关
const dialogVisible = ref(false);
const dialogType = ref(''); // add, edit, view
const dialogTitle = ref('');
const batchConfirmVisible = ref(false);
const batchConfirmTitle = ref('');
const batchConfirmContent = ref('');

// 表单数据和验证规则
const activityForm = reactive({
    id: '',
    title: '',
    content: '',
    coverImage: '',
    activityType: 'EVENT',
    setmealPrice: null, // 添加套餐价格字段
    startTime: '',
    endTime: '',
    status: 'ACTIVE',
    isFeatured: '0',
    viewCount: 0,
    createTime: '',
    updateTime: ''
});

const formRules = reactive({
    title: [
        { required: true, message: '请输入活动标题', trigger: 'blur' },
        { min: 2, max: 100, message: '活动标题长度必须在2-100个字符之间', trigger: 'blur' }
    ],
    activityType: [
        { required: true, message: '请选择活动类型', trigger: 'change' }
    ],
    setmealPrice: [
        { 
            required: true, 
            message: '请输入套餐价格', 
            trigger: 'blur',
            validator: (rule, value, callback) => {
                // 只有当活动类型为套餐时才需要验证套餐价格
                if (activityForm.activityType === 'SETMEAL') {
                    if (value === null || value === undefined || value === '') {
                        callback(new Error('请输入套餐价格'));
                    } else if (isNaN(value) || Number(value) < 0) {
                        callback(new Error('套餐价格必须大于等于0'));
                    } else {
                        callback();
                    }
                } else {
                    callback();
                }
            }
        }
    ],
    status: [
        { required: true, message: '请选择活动状态', trigger: 'change' }
    ],
    startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' }
    ],
    endTime: [
        { required: true, message: '请选择结束时间', trigger: 'change' },
        {
            validator: (rule, value, callback) => {
                if (activityForm.startTime && new Date(value) <= new Date(activityForm.startTime)) {
                    callback(new Error('结束时间必须晚于开始时间'));
                } else {
                    callback();
                }
            },
            trigger: 'change'
        }
    ],
    content: [
        { required: true, message: '请输入活动内容', trigger: 'blur' },
        { min: 10, max: 5000, message: '活动内容长度必须在10-5000个字符之间', trigger: 'blur' }
    ],
    coverImage: [
        { required: false, message: '请上传封面图片', trigger: 'change' }
    ]
});

// 处理活动类型变化
const handleActivityTypeChange = (value) => {
    // 如果不是套餐类型，清空套餐价格
    if (value !== 'SETMEAL') {
        activityForm.setmealPrice = null;
    }
    // 如果是套餐类型且价格为空，则设置默认值
    else if (activityForm.setmealPrice === null || activityForm.setmealPrice === undefined) {
        activityForm.setmealPrice = 0;
    }
};

// 添加定时检查函数
const checkAndEndExpiredActivities = async () => {
    try {
        const now = new Date();
        // 查找所有进行中的活动
        const activeActivities = activityList.value.filter(activity =>
            activity.status === 'ACTIVE' && new Date(activity.endTime) < now
        );

        if (activeActivities.length > 0) {
            // 批量更新这些活动的状态为ENDED
            const updateRequests = activeActivities.map(activity => ({
                id: activity.id,
                status: 'ENDED'
            }));

            const res = await fetch("http://localhost:8081/catcate/activities/updateByIdStatus", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(updateRequests)
            });

            if (res.ok) {
                // 更新成功后刷新列表
                fetchActivityList();
                ElMessage.info(`${activeActivities.length}个活动已自动结束`);
            }
        }
    } catch (error) {
        console.error('检查过期活动失败:', error);
    }
};

onMounted(() => {
    fetchActivityList();
    // 启动定时器
    // 每分钟检查一次过期活动
    statusCheckTimer.value = setInterval(checkAndEndExpiredActivities, 60000);
});

// 组件卸载时清理定时器
onUnmounted(() => {
    if (statusCheckTimer.value) {
        clearInterval(statusCheckTimer.value);
    }
});

// 计算剩余活动时间
const calculateRemainingTime = (endTime) => {
    const end = new Date(endTime);
    const now = new Date();
    const diff = end.getTime() - now.getTime();

    if (diff <= 0) {
        return '已结束';
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    if (days > 0) {
        return `${days}天${hours}小时`;
    } else if (hours > 0) {
        return `${hours}小时${minutes}分钟`;
    } else {
        return `${minutes}分钟`;
    }
};

// 计算属性：批量操作文本
const batchStatusText = computed(() => {
    // 判断选中活动的状态，统一操作文本
    const hasActive = selectedActivities.value.some(activity => activity.status === 'ACTIVE');
    const hasCancelled = selectedActivities.value.some(activity => activity.status === 'CANCELLED');
    const hasEnded = selectedActivities.value.some(activity => activity.status === 'ENDED');

    if (hasEnded) {
        return '状态不可改';
    } else if (hasActive && hasCancelled) {
        return '批量结束';
    } else if (hasActive) {
        return '批量结束';
    } else {
        return '批量激活';
    }
});

const batchFeaturedText = computed(() => {
    const hasFeatured = selectedActivities.value.some(activity => activity.isFeatured === 1);
    const hasUnfeatured = selectedActivities.value.some(activity => activity.isFeatured === 0);

    if (hasFeatured && hasUnfeatured) {
        return '批量推荐';
    } else if (hasFeatured) {
        return '取消推荐';
    } else {
        return '批量推荐';
    }
});

const batchActionText = computed(() => {
    if (batchActionType.value === 'delete') {
        return '删除';
    } else if (batchActionType.value === 'status') {
        return batchStatusText.value;
    } else {
        return batchFeaturedText.value;
    }
});

// 生命周期
onMounted(() => {
    fetchActivityList();
});

// 方法：获取活动列表
const fetchActivityList = async () => {
    loading.value = true;
    try {
        const res = await fetch("http://localhost:8081/catcate/activities/selectList", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                title: searchForm.title,
                activityType: searchForm.activityType,
                status: searchForm.status,
                isFeatured: searchForm.isFeatured,
                pageNum: pagination.currentPage,
                pageSize: pagination.pageSize
            })
        });

        if (res.ok) {
            const data = await res.json();
            console.log(data, "处理后的数据");
            // 假设后端返回格式：{ total: 总数, list: 活动列表 }
            if (data && Array.isArray(data.list)) {
                activityList.value = data.list.map(item => ({
                    ...item,
                    // 确保时间格式正确
                    startTime: item.startTime || '',
                    endTime: item.endTime || '',
                    createTime: item.createTime || '',
                    updateTime: item.updateTime || '',
                    isFeatured: Number(item.isFeatured)
                }));
                pagination.total = data.total || 0;
                loading.value = false;
            } else {
                ElMessage.error('数据格式不正确');
                loading.value = false;
            }
        } else {
            ElMessage.error('获取活动列表失败: ' + res.status);
            loading.value = false;
        }
    } catch (error) {
        console.error('获取活动列表失败:', error);
        ElMessage.error('获取活动列表失败: ' + error.message);
        loading.value = false;
    }
};

// 搜索和重置
const handleSearch = () => {
    pagination.currentPage = 1;
    fetchActivityList();
};

const resetSearch = () => {
    searchForm.title = '';
    searchForm.activityType = '';
    searchForm.status = '';
    searchForm.isFeatured = '';
    pagination.currentPage = 1;
    fetchActivityList();
};

// 分页处理
const handleSizeChange = (size) => {
    pagination.pageSize = size;
    pagination.currentPage = 1;
    fetchActivityList();
};

const handleCurrentChange = (page) => {
    pagination.currentPage = page;
    fetchActivityList();
};

// 选择变化
const handleSelectionChange = (selection) => {
    selectedActivities.value = selection;
};

// 表格行样式（根据状态区分）
const tableRowClassName = ({ row }) => {
    if (row.status === 'ENDED') {
        return 'row-ended';
    } else if (row.status === 'CANCELLED') {
        return 'row-cancelled';
    }
    return '';
};

// 格式化活动类型显示
const formatActivityType = (type) => {
    const typeMap = {
        'DISCOUNT': '折扣活动',
        'EVENT': '主题活动',
        'PROMOTION': '促销活动',
        'SETMEAL': '套餐'
    };
    return typeMap[type] || type;
};

// 格式化活动状态显示
const formatActivityStatus = (status) => {
    const statusMap = {
        'ACTIVE': '进行中',
        'ENDED': '已结束',
        'CANCELLED': '已取消'
    };
    return statusMap[status] || status;
};

// 获取活动类型标签样式
const getActivityTypeTagType = (type) => {
    const typeMap = {
        'DISCOUNT': 'primary',
        'EVENT': 'success',
        'PROMOTION': 'warning',
        'SETMEAL': 'danger'
    };
    return typeMap[type] || 'info';
};

// 获取活动状态标签样式
const getActivityStatusTagType = (status) => {
    const statusMap = {
        'ACTIVE': 'success',
        'ENDED': 'info',
        'CANCELLED': 'danger'
    };
    return statusMap[status] || 'default';
};

// 获取操作按钮文本
const getActionButtonText = (status) => {
    switch (status) {
        case 'ACTIVE':
            return '结束活动';
        case 'CANCELLED':
            return '激活活动';
        case 'ENDED':
            return '已结束';
        default:
            return '状态切换';
    }
};

// 获取操作按钮类型
const getActionButtonType = (status) => {
    switch (status) {
        case 'ACTIVE':
            return 'warning';
        case 'CANCELLED':
            return 'success';
        case 'ENDED':
            return 'default';
        default:
            return 'primary';
    }
};

// 获取操作按钮图标
const getActionButtonIcon = (status) => {
    switch (status) {
        case 'ACTIVE':
            return 'Time';
        case 'CANCELLED':
            return 'Check';
        case 'ENDED':
            return 'Close';
        default:
            return 'Refresh';
    }
};

// 状态切换
const handleStatusChange = (row) => {
    let newStatus = '';
    let confirmText = '';

    if (row.status === 'ACTIVE') {
        newStatus = 'ENDED';
        confirmText = '结束';
    } else if (row.status === 'CANCELLED') {
        newStatus = 'ACTIVE';
        confirmText = '激活';
    } else {
        ElMessage.warning('已结束的活动无法修改状态');
        return;
    }

    ElMessageBox.confirm(
        `确定要${confirmText}活动【${row.title}】吗？`,
        '确认操作',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: row.status === 'ACTIVE' ? 'warning' : 'success'
        }
    ).then(async () => {
        loading.value = true;
        // 获取后端数据
        const res = await fetch("http://localhost:8081/catcate/activities/updateStatus", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                id: row.id,
                status: newStatus
            })
        })
        // 处理响应
        if (res.status === 200) {
            const data = await res.json();
            if (res.ok === true) {
                ElMessage.success(`活动已${confirmText}成功`);
                // 刷新活动列表
                fetchActivityList();
                loading.value = false;
            }
        } else {
            ElMessage.error(`活动${confirmText}失败`);
            loading.value = false;
        }
    }).catch(() => {
        // 取消操作
    });
};

// 推荐状态切换
const handleFeaturedChange = (row, newValue) => {
    const newFeatured = newValue;
    const confirmText = newFeatured === 1 ? '推荐' : '取消推荐';

    ElMessageBox.confirm(
        `确定要${confirmText}活动【${row.title}】吗？`,
        '确认操作',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'success'
        }
    ).then(async () => {
        loading.value = true;
        const res = await fetch("http://localhost:8081/catcate/activities/updateFeatured", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                id: row.id,
                isFeatured: newFeatured
            })
        })
        if (res.status === 200) {
            const data = await res.json();
            if (res.ok === true) {
                ElMessage.success(`活动${confirmText}成功`);
                // 直接更新当前行的状态
                row.isFeatured = newFeatured;
                fetchActivityList(); // 或者可以注释掉这行，只更新当前行
                loading.value = false;
            }
        } else {
            ElMessage.error(`活动${confirmText}失败`);
            // 恢复原状态
            row.isFeatured = row.isFeatured === 1 ? 0 : 1;
            loading.value = false;
        }
    }).catch(() => {
        // 取消操作，恢复原状态
        row.isFeatured = row.isFeatured === 1 ? 0 : 1;
    });
};

// 批量状态切换
const handleBatchStatusChange = () => {
    if (selectedActivities.value.length === 0) {
        ElMessage.warning('请选择要操作的活动');
        return;
    }

    // 检查是否包含已结束的活动
    const hasEnded = selectedActivities.value.some(activity => activity.status === 'ENDED');
    if (hasEnded) {
        ElMessage.error('已结束的活动无法批量修改状态');
        return;
    }

    // 判断操作类型
    const hasActive = selectedActivities.value.some(activity => activity.status === 'ACTIVE');
    const action = hasActive ? '结束' : '激活';

    batchActionType.value = 'status';
    batchConfirmTitle.value = `批量${action}活动`;
    batchConfirmContent.value = `您确定要${action}选中的 ${selectedActivities.value.length} 个活动吗？`;
    batchConfirmVisible.value = true;
};

// 批量推荐切换
const handleBatchFeatured = () => {
    if (selectedActivities.value.length === 0) {
        ElMessage.warning('请选择要操作的活动');
        return;
    }

    const hasFeatured = selectedActivities.value.some(activity => activity.isFeatured === 1);
    const action = hasFeatured ? '取消推荐' : '批量推荐';

    batchActionType.value = 'featured';
    batchConfirmTitle.value = action;
    batchConfirmContent.value = `您确定要${action}选中的 ${selectedActivities.value.length} 个活动吗？`;
    batchConfirmVisible.value = true;
};

// 批量删除
const handleBatchDelete = () => {
    if (selectedActivities.value.length === 0) {
        ElMessage.warning('请选择要删除的活动');
        return;
    }

    batchActionType.value = 'delete';
    batchConfirmTitle.value = '批量删除活动';
    batchConfirmContent.value = `您确定要删除选中的 ${selectedActivities.value.length} 个活动吗？此操作不可撤销。`;
    batchConfirmVisible.value = true;
};

// 确认批量操作
const confirmBatchAction = async () => {
    loading.value = true;

    try {
        if (batchActionType.value === 'delete') {
            // 批量删除
            const activitiesArray = selectedActivities.value.map(activity => ({ id: activity.id }));

            // 发起后端请求
            const res = await fetch("http://localhost:8081/catcate/activities/deleteByIds", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(activitiesArray)
            })
            // 处理响应
            if (res.status === 200) {
                const data = await res.json();
                if (res.ok === true) {
                    ElMessage.success("批量删除成功");
                    // 刷新活动列表
                    fetchActivityList();
                    // 关闭弹窗
                    batchConfirmVisible.value = false;
                    selectedActivities.value = [];
                }
            } else {
                ElMessage.error("批量删除失败");
            }
        } else if (batchActionType.value === 'status') {
            // 批量状态切换
            const hasActive = selectedActivities.value.some(activity => activity.status === 'ACTIVE');
            const newStatus = hasActive ? 'ENDED' : 'ACTIVE';
            const action = hasActive ? '结束' : '激活';

            const activitiesArray = selectedActivities.value.map(activity => ({
                id: activity.id,
                status: newStatus
            }));

            // 发起后端请求
            const res = await fetch("http://localhost:8081/catcate/activities/updateByIdStatus", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(activitiesArray)
            })
            // 处理响应
            if (res.status === 200) {
                const data = await res.json();
                if (res.ok === true) {
                    ElMessage.success(`批量${action}成功`);
                    // 刷新活动列表
                    fetchActivityList();
                    // 关闭弹窗
                    batchConfirmVisible.value = false;
                    selectedActivities.value = [];
                }
            } else {
                ElMessage.error(`批量${action}失败`);
            }
        } else {
            // 批量推荐切换
            const hasFeatured = selectedActivities.value.some(activity => activity.isFeatured === 1);
            const newFeatured = hasFeatured ? 0 : 1;
            const action = hasFeatured ? '取消推荐' : '推荐';

            const activitiesArray = selectedActivities.value.map(activity => ({
                id: activity.id,
                isFeatured: newFeatured
            }));

            // 发起后端请求
            const res = await fetch("http://localhost:8081/catcate/activities/updateByIdFeatured", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(activitiesArray)
            })
            // 处理响应
            if (res.status === 200) {
                const data = await res.json();
                if (res.ok === true) {
                    ElMessage.success(`批量${action}成功`);
                    // 刷新活动列表
                    fetchActivityList();
                    // 关闭弹窗
                    batchConfirmVisible.value = false;
                    selectedActivities.value = [];
                }
            } else {
                ElMessage.error(`批量${action}失败`);
            }
        }

        loading.value = false;

    } catch (error) {
        ElMessage.error('批量操作失败');
        batchConfirmVisible.value = false;
        loading.value = false;
    }
};

// 弹窗操作
const handleAddActivity = () => {
    dialogType.value = 'add';
    dialogTitle.value = '新增活动';
    // 重置表单
    Object.assign(activityForm, {
        id: '',
        title: '',
        content: '',
        coverImage: '',
        activityType: 'EVENT',
        setmealPrice: null, // 重置套餐价格
        startTime: new Date(),
        endTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 默认7天后结束
        status: 'ACTIVE',
        isFeatured: '0',
        viewCount: 0,
        createTime: '',
        updateTime: ''
    });
    dialogVisible.value = true;
};

const handleEditActivity = (row) => {
    dialogType.value = 'edit';
    dialogTitle.value = '编辑活动';
    // 填充表单数据
    Object.assign(activityForm, {
        id: row.id,
        title: row.title,
        content: row.content,
        coverImage: row.coverImage,
        activityType: row.activityType,
        setmealPrice: row.setmealPrice, // 包含套餐价格
        startTime: row.startTime ? new Date(row.startTime) : '',
        endTime: row.endTime ? new Date(row.endTime) : '',
        status: row.status,
        isFeatured: row.isFeatured.toString(),
        viewCount: row.viewCount,
        createTime: row.createTime ? new Date(row.createTime) : '',
        updateTime: row.updateTime ? new Date(row.updateTime) : ''
    });
    dialogVisible.value = true;
};

const handleViewActivity = (row) => {
    dialogType.value = 'view';
    dialogTitle.value = '查看活动详情';
    // 填充表单数据
    Object.assign(activityForm, {
        id: row.id,
        title: row.title,
        content: row.content,
        coverImage: row.coverImage,
        activityType: row.activityType,
        setmealPrice: row.setmealPrice, // 包含套餐价格
        startTime: row.startTime ? new Date(row.startTime) : '',
        endTime: row.endTime ? new Date(row.endTime) : '',
        status: row.status,
        isFeatured: row.isFeatured.toString(),
        viewCount: row.viewCount,
        createTime: row.createTime ? new Date(row.createTime) : '',
        updateTime: row.updateTime ? new Date(row.updateTime) : ''
    });
    dialogVisible.value = true;
};

// 保存活动信息
const handleSaveActivity = async () => {
    // 确保表单ref已初始化
    await nextTick();

    try {
        // 表单验证
        await activityFormRef.value.validate();
        loading.value = true;

        // 处理时间格式
        const submitForm = {
            ...activityForm,
            startTime: activityForm.startTime ? new Date(activityForm.startTime).toISOString() : '',
            endTime: activityForm.endTime ? new Date(activityForm.endTime).toISOString() : ''
        };

        // 请求后端数据
        const res = await fetch("http://localhost:8081/catcate/activities/addOrUpdate", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(submitForm)
        })

        // 处理响应
        if (res.status === 200) {
            const data = await res.json();
            if (res.ok === true) {
                ElMessage.success(dialogType.value === 'add' ? "活动新增成功" : "活动更新成功");
                // 刷新活动列表
                fetchActivityList();
                // 关闭弹窗
                dialogVisible.value = false;
            } else {
                ElMessage.error(data.msg || (dialogType.value === 'add' ? "活动新增失败" : "活动更新失败"));
            }
        } else {
            ElMessage.error("网络错误");
        }

        loading.value = false;
    } catch (error) {
        // 表单验证失败
        loading.value = false;
        return;
    }
};

// 删除操作
const handleDeleteActivity = (row) => {
    ElMessageBox.confirm(
        `确定要删除活动【${row.title}】吗？此操作不可撤销。`,
        '确认删除',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'danger'
        }
    ).then(async () => {
        try {
            loading.value = true;
            // 获取请求
            const res = await fetch("http://localhost:8081/catcate/activities/deleteById", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    id: row.id
                })
            })
            // 处理响应
            if (res.status === 200) {
                const data = await res.json();
                if (res.ok === true) {
                    ElMessage.success("活动删除成功");
                    // 刷新活动列表
                    fetchActivityList();
                    loading.value = false;
                }
            } else {
                ElMessage.error("活动删除失败");
                loading.value = false;
            }
        } catch (error) {
            ElMessage.error('删除失败');
            loading.value = false;
        }
    });
};

// 图片上传相关函数

// 触发文件选择
const triggerFileInput = () => {
    fileInput.value.click();
};

// 处理文件选择
const handleFileSelect = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // 验证文件
    if (!validateFile(file)) {
        return;
    }

    // 开始上传
    await uploadImage(file);
};

// 文件验证
const validateFile = (file) => {
    // 检查文件类型
    const validTypes = ['image/jpeg', 'image/png', 'image/jpg'];
    if (!validTypes.includes(file.type)) {
        ElMessage.error('请上传 JPG 或 PNG 格式的图片');
        return false;
    }

    // 检查文件大小 (8MB)
    if (file.size > 8 * 1024 * 1024) {
        ElMessage.error('图片大小不能超过 8MB');
        return false;
    }

    return true;
};

// 上传图片
const uploadImage = async (file) => {
    try {
        uploadError.value = '';
        uploadProgress.value = 0;

        // 创建FormData对象
        const formData = new FormData();
        formData.append('file', file);

        // 使用XMLHttpRequest实现进度监控
        const xhr = new XMLHttpRequest();

        // 监听上传进度
        xhr.upload.addEventListener('progress', (event) => {
            if (event.lengthComputable) {
                uploadProgress.value = Math.round((event.loaded / event.total) * 100);
            }
        });

        // 监听上传完成
        xhr.addEventListener('load', () => {
            if (xhr.status === 200) {
                try {
                    const response = JSON.parse(xhr.responseText);
                    console.log('上传响应:', response); // 调试信息
                    
                    if (response.code === 200 && response.data) {
                        // 修复：从response.data.url获取实际的URL
                        activityForm.coverImage = response.data.url || response.data;
                        ElMessage.success('图片上传成功');
                        console.log('设置的图片URL:', activityForm.coverImage); // 调试信息
                    } else {
                        uploadError.value = response.message || response.msg || '图片上传失败';
                        ElMessage.error(uploadError.value);
                    }
                } catch (e) {
                    uploadError.value = '服务器响应格式错误';
                    ElMessage.error(uploadError.value);
                }
            } else {
                uploadError.value = '上传失败，请重试';
                ElMessage.error(uploadError.value);
            }
            uploadProgress.value = 0;
        });

        // 监听错误
        xhr.addEventListener('error', () => {
            uploadError.value = '网络错误，请检查网络连接';
            ElMessage.error(uploadError.value);
            uploadProgress.value = 0;
        });

        // 发送请求
        xhr.open('POST', uploadUrl);
        xhr.send(formData);

    } catch (error) {
        uploadError.value = '上传过程中发生错误';
        ElMessage.error(uploadError.value);
        uploadProgress.value = 0;
    }
};
// 移除封面图片
const removeCoverImage = () => {
    ElMessageBox.confirm('确定要移除这张封面图片吗？', '确认移除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        activityForm.coverImage = '';
        if (fileInput.value) {
            fileInput.value.value = '';
        }
        ElMessage.success('封面图片已移除');
    }).catch(() => {
        // 取消操作
    });
};
</script>

<style scoped>
/* 图片上传容器样式 */
.image-upload-container {
    width: 100%;
}

.upload-area {
    border: 2px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s ease;
    background-color: #fafafa;
    min-height: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.upload-area:hover {
    border-color: #409eff;
}

.upload-placeholder {
    text-align: center;
    color: #666;
}

.upload-placeholder p {
    margin: 8px 0;
    font-size: 14px;
}

.upload-tips {
    font-size: 12px !important;
    color: #999 !important;
}

.uploaded-preview {
    position: relative;
    width: 100%;
    height: 100%;
    min-height: 180px;
}

.preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

.preview-overlay {
    position: absolute;
    top: 10px;
    right: 10px;
    display: flex;
    gap: 8px;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.upload-area:hover .preview-overlay {
    opacity: 1;
}

.upload-progress {
    margin-top: 15px;
}

.upload-error {
    margin-top: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .upload-area {
        min-height: 150px;
    }
    
    .preview-overlay {
        top: 5px;
        right: 5px;
    }
    
    .preview-overlay .el-button {
        width: 28px;
        height: 28px;
        padding: 0;
    }
}

.activity-management {
    padding: 20px;
    background-color: #f5f7fa;
    min-height: calc(100vh - 60px);
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-header h1 {
    margin: 0;
    font-size: 20px;
    color: #1f2329;
}

.filter-card {
    margin-bottom: 20px;
    padding: 15px;
    background-color: #fff7ee;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 15px;
    gap: 10px;
}

.table-card {
    overflow: hidden;
    background-color: #fff;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.pagination {
    margin-top: 15px;
    text-align: right;
}

.activity-form {
    margin-top: 10px;
}

/* 活动图片样式 */
.activity-image {
    border-radius: 4px;
    cursor: pointer;
    transition: transform 0.3s ease;
}

.activity-image:hover {
    transform: scale(1.05);
}

/* 表单提示 */
.form-hint {
    font-size: 12px;
    color: #666;
    margin-top: 5px;
}

/* 表格行样式 */
::v-deep .el-table .row-ended {
    background-color: rgba(144, 202, 249, 0.05);
    color: #999;
}

::v-deep .el-table .row-cancelled {
    background-color: rgba(245, 108, 108, 0.05);
    color: #999;
}

::v-deep .el-table .row-ended .el-tag,
::v-deep .el-table .row-cancelled .el-tag {
    opacity: 0.7;
}

/* 上传组件样式 */
::v-deep .avatar-uploader {
    margin-bottom: 10px;
}

::v-deep .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 200px;
    height: 112px;
    line-height: 112px;
    text-align: center;
    border: 1px dashed #dcdfe6;
    border-radius: 6px;
    cursor: pointer;
    transition: border-color 0.3s ease;
}

::v-deep .avatar-uploader-icon:hover {
    border-color: #ff9f43;
}

::v-deep .upload-image {
    width: 200px;
    height: 112px;
    display: block;
    border-radius: 6px;
    object-fit: cover;
}

/* 响应式调整 */
@media (max-width: 1200px) {
    .el-table-column {
        min-width: 100px !important;
    }
}

@media (max-width: 992px) {
    .el-row {
        flex-direction: column;
    }

    .el-col {
        width: 100% !important;
        margin-bottom: 10px;
    }

    .filter-actions {
        flex-wrap: wrap;
    }
}

@media (max-width: 768px) {
    .filter-actions {
        flex-direction: column;
    }

    .filter-actions .el-button {
        width: 100%;
    }

    /* 隐藏部分列 */
    .el-table-column:not(.el-table-column--selection):not([label="活动ID"]):not([label="活动标题"]):not([label="活动类型"]):not([label="活动状态"]):not([label="操作"]) {
        display: none !important;
    }
}

/* 保持与首页一致的样式 */
::v-deep .el-card {
    border-radius: 8px;
}

::v-deep .el-button--primary {
    background-color: #ff9f43;
    border-color: #ff9f43;
}

::v-deep .el-button--primary:hover {
    background-color: #ff8c21;
    border-color: #ff8c21;
}

::v-deep .el-button--success {
    background-color: #00d2d3;
    border-color: #00d2d3;
}

::v-deep .el-button--success:hover {
    background-color: #00b8b9;
    border-color: #00b8b9;
}

::v-deep .el-button--danger {
    background-color: #ff6b6b;
    border-color: #ff6b6b;
}

::v-deep .el-button--danger:hover {
    background-color: #ff5252;
    border-color: #ff5252;
}

::v-deep .el-button--warning {
    background-color: #ffa801;
    border-color: #ffa801;
}

::v-deep .el-button--warning:hover {
    background-color: #ff9100;
    border-color: #ff9100;
}

::v-deep .el-tag--primary {
    background-color: #ff9f43;
}

::v-deep .el-tag--success {
    background-color: #00d2d3;
}

::v-deep .el-tag--info {
    background-color: #409eff;
}

::v-deep .el-tag--danger {
    background-color: #ff6b6b;
}

::v-deep .el-tag--warning {
    background-color: #ffa801;
}

::v-deep .el-input__inner,
::v-deep .el-select-dropdown,
::v-deep .el-date-editor .el-input__inner,
::v-deep .el-textarea__inner {
    border-radius: 4px;
}

::v-deep .el-table th {
    background-color: #fff7ee;
}

/* 图片预览样式 */
::v-deep .el-image-viewer__wrapper {
    z-index: 3000 !important;
}

/* 开关样式调整 */
::v-deep .el-switch__core {
    width: 50px !important;
}

::v-deep .el-switch__label {
    width: 20px !important;
}
</style>