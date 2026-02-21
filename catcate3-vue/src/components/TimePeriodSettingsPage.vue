<!-- 时段设置界面 -->
<template>
    <div class="time-slot-management">
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h1>预约时段设置</h1>
            <el-button type="primary" icon="Plus" @click="handleAddSlot" style="padding-left: 0px;">
                新增时段
            </el-button>
        </div>

        <!-- 搜索和筛选区（参考活动管理页面风格） -->
        <el-card class="filter-card">
            <el-row :gutter="20">
                <el-col :span="8">
                    <el-input v-model="searchForm.timeSlot" placeholder="搜索时段（例如：09:00-10:00）" clearable
                        prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.isActive" placeholder="选择状态" clearable>
                        <el-option label="启用" value="1"></el-option>
                        <el-option label="禁用" value="0"></el-option>
                    </el-select>
                </el-col>
            </el-row>
            <div class="filter-actions">
                <el-button type="primary" @click="handleSearch">
                    搜索
                </el-button>
                <el-button @click="handleReset">
                    重置
                </el-button>
                <el-button type="danger" icon="Delete" @click="handleBatchDelete" style="padding-left: 0px;"
                    :disabled="selectedSlots.length === 0">
                    批量删除
                </el-button>
            </div>
        </el-card>

        <!-- 时段列表 -->
        <el-card class="table-card">
            <el-table :data="timeSlotList" border stripe :loading="loading" @selection-change="handleSelectionChange"
                :row-class-name="tableRowClassName">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="id" label="时段ID" width="80" align="center"></el-table-column>
                <el-table-column prop="timeSlot" label="时段名称" min-width="120" align="center"></el-table-column>
                <el-table-column label="开始时间" min-width="100" align="center">
                    <template #default="scope">
                        {{ formatTime(scope.row.startTime) }}
                    </template>
                </el-table-column>
                <el-table-column label="结束时间" min-width="100" align="center">
                    <template #default="scope">
                        {{ formatTime(scope.row.endTime) }}
                    </template>
                </el-table-column>
                <el-table-column prop="maxVisitors" label="最大预约人数" min-width="120" align="center">
                    <template #default="scope">
                        <el-tag type="info" size="mini" style="color: #fff;">
                            {{ scope.row.maxVisitors }}人
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sortOrder" label="排序" min-width="100" align="center">
                    <template #default="scope">
                        <el-input-number v-model="scope.row.sortOrder" :min="0" :max="100" :step="1"
                            @change="(val) => handleSortChange(scope.row, val)" size="small"
                            :disabled="scope.row.isActive === 0">
                        </el-input-number>
                    </template>
                </el-table-column>
                <el-table-column prop="isActive" label="状态" min-width="100" align="center">
                    <template #default="scope">
                        <el-switch v-model="scope.row.isActive" active-color="#00d2d3" inactive-color="#ccc"
                            :active-value="1" :inactive-value="0" @change="(val) => handleStatusChange(scope.row, val)">
                        </el-switch>
                        <div>{{ scope.row.isActive === 1 ? '启用' : '禁用' }}</div>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="时段描述" min-width="180"></el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="180" align="center"></el-table-column>
                <el-table-column label="操作" min-width="180" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" icon="Edit" @click="handleEditSlot(scope.row)"
                            style="padding-left: 0px;">编辑</el-button>
                        <el-button size="small" type="danger" icon="Delete" @click="handleDeleteSlot(scope.row)"
                            style="padding-left: 0px;">删除</el-button>
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

        <!-- 新增/编辑时段弹窗 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false"
            append-to-body>
            <el-form ref="timeSlotFormRef" :model="timeSlotForm" :rules="formRules" label-width="120px"
                class="time-slot-form">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="时段名称" prop="timeSlot">
                            <el-input v-model="timeSlotForm.timeSlot" placeholder="例如：09:00-10:00"
                                maxlength="50"></el-input>
                            <div class="form-hint">请输入规范的时段格式，如：09:00-10:00</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="排序序号" prop="sortOrder">
                            <el-input-number v-model="timeSlotForm.sortOrder" :min="0" :max="100" :step="1"
                                placeholder="输入排序序号"></el-input-number>
                            <div class="form-hint">数值越小，排序越靠前</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="开始时间" prop="startTime">
                            <el-time-picker v-model="timeSlotForm.startTime" placeholder="选择开始时间" format="HH:mm"
                                value-format="HH:mm" style="width: 100%;"></el-time-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="结束时间" prop="endTime">
                            <el-time-picker v-model="timeSlotForm.endTime" placeholder="选择结束时间" format="HH:mm"
                                value-format="HH:mm" style="width: 100%;"></el-time-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="最大预约人数" prop="maxVisitors">
                            <el-input-number v-model="timeSlotForm.maxVisitors" :min="1" :max="100" :step="1"
                                placeholder="输入最大预约人数"></el-input-number>
                            <div class="form-hint">该时段最多可预约的访客人数</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="是否启用" prop="isActive">
                            <el-switch v-model="timeSlotForm.isActive" active-color="#00d2d3" inactive-color="#ccc"
                                active-value="1" inactive-value="0"></el-switch>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="时段描述" prop="description">
                            <el-input v-model="timeSlotForm.description" type="textarea" :rows="4"
                                placeholder="请输入时段描述（可选）" maxlength="200" show-word-limit></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSaveSlot">
                    保存
                </el-button>
            </template>
        </el-dialog>

        <!-- 批量删除确认弹窗 -->
        <el-dialog v-model="batchConfirmVisible" title="批量删除确认" width="400px" :close-on-click-modal="false">
            <p>确定要删除选中的 {{ selectedSlots.length }} 个时段吗？删除后相关预约可能受到影响。</p>
            <template #footer>
                <el-button @click="batchConfirmVisible = false">取消</el-button>
                <el-button type="danger" @click="confirmBatchDelete">
                    确认删除
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Edit, Delete, Refresh, SortUp } from '@element-plus/icons-vue';

// 状态管理
const loading = ref(false);
const timeSlotList = ref([]);
const selectedSlots = ref([]);
const timeSlotFormRef = ref(null);

// 搜索表单（参考活动管理页面结构）
const searchForm = reactive({
    timeSlot: '',
    isActive: ''
});

// 分页配置
const pagination = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0
});

// 弹窗相关
const dialogVisible = ref(false);
const dialogType = ref(''); // add, edit
const dialogTitle = ref('');
const batchConfirmVisible = ref(false);

// 表单数据和验证规则
const timeSlotForm = reactive({
    id: '',
    timeSlot: '',
    startTime: '',
    endTime: '',
    sortOrder: 0,
    maxVisitors: 10,
    isActive: 1,
    description: ''
});

const formRules = reactive({
    timeSlot: [
        { required: true, message: '请输入时段名称', trigger: 'blur' },
        { pattern: /^([01]\d|2[0-3]):[0-5]\d-([01]\d|2[0-3]):[0-5]\d$/, message: '请输入规范的时段格式（如：09:00-10:00）', trigger: 'blur' }
    ],
    startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' }
    ],
    endTime: [
        { required: true, message: '请选择结束时间', trigger: 'change' },
        {
            validator: (rule, value, callback) => {
                if (timeSlotForm.startTime && value) {
                    const start = timeSlotForm.startTime.split(':').join('');
                    const end = value.split(':').join('');
                    if (Number(end) <= Number(start)) {
                        callback(new Error('结束时间必须晚于开始时间'));
                    } else {
                        callback();
                    }
                } else {
                    callback();
                }
            },
            trigger: 'change'
        }
    ],
    maxVisitors: [
        { required: true, message: '请输入最大预约人数', trigger: 'blur' },
        { type: 'number', min: 1, max: 100, message: '最大预约人数必须在1-100之间', trigger: 'blur' }
    ],
    sortOrder: [
        { required: true, message: '请输入排序序号', trigger: 'blur' },
        { type: 'number', min: 0, max: 100, message: '排序序号必须在0-100之间', trigger: 'blur' }
    ],
    description: [
        { max: 200, message: '时段描述不能超过200个字符', trigger: 'blur' }
    ]
});

// 生命周期
onMounted(() => {
    fetchTimeSlotList();
});

/**
 * 获取时段列表（参考活动管理页面的请求逻辑）
 */
const fetchTimeSlotList = async () => {
    loading.value = true;
    try {
        const requestData = {
            timeSlot: searchForm.timeSlot,
            isActive: searchForm.isActive,
            pageNum: pagination.currentPage,
            pageSize: pagination.pageSize
        };
        console.log('请求参数:', requestData);
        
        const res = await fetch("http://localhost:8081/catcate/reservationTimeSlots/selectList", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestData)
        });

        if (res.ok) {
            const data = await res.json();
            console.log('时段列表数据:', data);
            if (data && Array.isArray(data.list)) {
                timeSlotList.value = data.list.map(item => ({
                    ...item,
                    // 确保数据格式正确
                    isActive: Number(item.isActive),
                    sortOrder: Number(item.sortOrder),
                    maxVisitors: Number(item.maxVisitors),
                    createTime: item.createTime || ''
                }));
                pagination.total = data.total || 0;
            } else {
                ElMessage.error('数据格式不正确');
            }
        } else {
            ElMessage.error('获取时段列表失败: ' + res.status);
        }
    } catch (error) {
        console.error('获取时段列表失败:', error);
        ElMessage.error('获取时段列表失败: ' + error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * 搜索功能（与活动管理页面保持一致）
 */
const handleSearch = () => {
    pagination.currentPage = 1;
    console.log('搜索参数:', searchForm); // 添加日志查看搜索条件
    fetchTimeSlotList();
    ElMessage.info('正在搜索符合条件的时段...');
};
/**
 * 重置功能（与活动管理页面保持一致）
 */

const handleReset = () => {
    searchForm.timeSlot = '';
    searchForm.isActive = '';
    pagination.currentPage = 1;
    console.log('重置后搜索参数:', searchForm); // 添加日志
    fetchTimeSlotList();
    ElMessage.success('搜索条件已重置');
};

/**
 * 分页处理
 */
const handleSizeChange = (size) => {
    pagination.pageSize = size;
    pagination.currentPage = 1;
    fetchTimeSlotList();
};

const handleCurrentChange = (page) => {
    pagination.currentPage = page;
    fetchTimeSlotList();
};

/**
 * 选择变化（批量操作）
 */
const handleSelectionChange = (selection) => {
    selectedSlots.value = selection;
};

/**
 * 表格行样式（根据状态区分）
 */
const tableRowClassName = ({ row }) => {
    if (row.isActive === 0) {
        return 'row-disabled';
    }
    return '';
};

/**
 * 格式化时间显示
 */
const formatTime = (timeStr) => {
    return timeStr || '-';
};

/**
 * 排序变更
 */
const handleSortChange = async (row, val) => {
    const originalSortOrder = row.sortOrder; // 保存原始值
    if (val === originalSortOrder) return;

    loading.value = true;
    try {
        const res = await fetch("http://localhost:8081/catcate/reservationTimeSlots/updateSort", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                id: row.id,
                sortOrder: val
            })
        });

        if (res.ok) {
            ElMessage.success('排序更新成功');
            // 不需要刷新整个列表，直接更新当前行
            row.sortOrder = val;
        } else {
            ElMessage.error('排序更新失败');
            // 恢复原排序
            row.sortOrder = originalSortOrder; // 使用保存的原始值
        }
    } catch (error) {
        console.error('排序更新失败:', error);
        ElMessage.error('排序更新失败: ' + error.message);
        // 恢复原排序
        row.sortOrder = originalSortOrder; // 使用保存的原始值
    } finally {
        loading.value = false;
    }
};
/**
 * 状态变更（启用/禁用）
 */
const handleStatusChange = async (row, val) => {
    const statusText = val === 1 ? '启用' : '禁用';

    ElMessageBox.confirm(
        `确定要${statusText}该时段吗？`,
        '确认操作',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: val === 1 ? 'success' : 'warning'
        }
    ).then(async () => {
        loading.value = true;
        try {
            const res = await fetch("http://localhost:8081/catcate/reservationTimeSlots/updateStatus", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    id: row.id,
                    isActive: val
                })
            });

            if (res.ok) {
                ElMessage.success(`时段${statusText}成功`);
                row.isActive = val;
            } else {
                ElMessage.error(`时段${statusText}失败`);
                // 恢复原状态
                row.isActive = row.isActive === 1 ? 0 : 1;
            }
        } catch (error) {
            console.error(`时段${statusText}失败:', error`);
            ElMessage.error(`时段${statusText}失败: ' + error.message`);
            // 恢复原状态
            row.isActive = row.isActive === 1 ? 0 : 1;
        } finally {
            loading.value = false;
        }
    }).catch(() => {
        // 取消操作，恢复原状态
        row.isActive = row.isActive === 1 ? 0 : 1;
    });
};

/**
 * 新增时段
 */
const handleAddSlot = () => {
    dialogType.value = 'add';
    dialogTitle.value = '新增预约时段';
    // 重置表单
    Object.assign(timeSlotForm, {
        id: '',
        timeSlot: '',
        startTime: '',
        endTime: '',
        sortOrder: 0,
        maxVisitors: 10,
        isActive: 1,
        description: ''
    });
    dialogVisible.value = true;
    nextTick(() => {
        timeSlotFormRef.value?.clearValidate();
    });
};

/**
 * 编辑时段
 */
const handleEditSlot = (row) => {
    dialogType.value = 'edit';
    dialogTitle.value = '编辑预约时段';
    // 填充表单数据
    Object.assign(timeSlotForm, {
        id: row.id,
        timeSlot: row.timeSlot,
        startTime: row.startTime,
        endTime: row.endTime,
        sortOrder: row.sortOrder,
        maxVisitors: row.maxVisitors,
        isActive: row.isActive,
        description: row.description || ''
    });
    dialogVisible.value = true;
    nextTick(() => {
        timeSlotFormRef.value?.clearValidate();
    });
};

/**
 * 保存时段信息
 */
const handleSaveSlot = async () => {
    try {
        // 表单验证
        await timeSlotFormRef.value.validate();
        loading.value = true;

        const submitForm = { ...timeSlotForm };

        // 请求后端数据
        const res = await fetch("http://localhost:8081/catcate/reservationTimeSlots/addOrUpdate", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(submitForm)
        });

        if (res.ok) {
            ElMessage.success(dialogType.value === 'add' ? "时段新增成功" : "时段更新成功");
            dialogVisible.value = false;
            fetchTimeSlotList(); // 刷新列表
        } else {
            ElMessage.error(dialogType.value === 'add' ? "时段新增失败" : "时段更新失败");
        }
    } catch (error) {
        // 表单验证失败
        return;
    } finally {
        loading.value = false;
    }
};

/**
 * 删除单个时段
 */
const handleDeleteSlot = (row) => {
    ElMessageBox.confirm(
        `确定要删除时段【${row.timeSlot}】吗？删除后相关预约可能受到影响。`,
        '确认删除',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'danger'
        }
    ).then(async () => {
        try {
            loading.value = true;
            const res = await fetch("http://localhost:8081/catcate/reservationTimeSlots/deleteById", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    id: row.id
                })
            });

            if (res.ok) {
                ElMessage.success("时段删除成功");
                fetchTimeSlotList();
            } else {
                ElMessage.error("时段删除失败");
            }
        } catch (error) {
            ElMessage.error('删除失败: ' + error.message);
        } finally {
            loading.value = false;
        }
    });
};

/**
 * 批量删除
 */
const handleBatchDelete = () => {
    if (selectedSlots.value.length === 0) {
        ElMessage.warning('请选择要删除的时段');
        return;
    }

    batchConfirmVisible.value = true;
};

/**
 * 确认批量删除
 */
const confirmBatchDelete = async () => {
    if (selectedSlots.value.length === 0) {
        ElMessage.warning('请选择要删除的时段');
        return;
    }

    loading.value = true;
    try {
        const slotIds = selectedSlots.value.map(slot => ({ id: slot.id }));

        const res = await fetch("http://localhost:8081/catcate/reservationTimeSlots/deleteByIds", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(slotIds)
        });
        console.log(res,"res");
        if (res.ok) {
            ElMessage.success(`成功删除 ${selectedSlots.value.length} 个时段`);
            batchConfirmVisible.value = false;
            selectedSlots.value = [];
            fetchTimeSlotList();
        } else {
            ElMessage.error('批量删除时段失败');
        }
    } catch (error) {
        ElMessage.error('批量删除失败: ' + error.message);
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
.time-slot-management {
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

/* 筛选卡片（与活动管理页面保持一致） */
.filter-card {
    margin-bottom: 20px;
    padding: 15px;
    background-color: #fff7ee;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    border-radius: 8px;
}

/* 筛选按钮区域（参考活动管理页面布局） */
.filter-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 15px;
    gap: 10px;
}

/* 表格卡片 */
.table-card {
    overflow: hidden;
    background-color: #fff;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    border-radius: 8px;
}

/* 分页容器（与活动管理页面保持一致） */
.pagination {
    margin-top: 15px;
    text-align: right;
    padding: 16px;
    border-top: 1px solid #f0f0f0;
    background-color: #fff;
}

/* 表单样式 */
.time-slot-form {
    margin-top: 10px;
}

/* 表单提示 */
.form-hint {
    font-size: 12px;
    color: #666;
    margin-top: 5px;
}

/* 表格行样式 */
::v-deep .el-table .row-disabled {
    background-color: rgba(220, 220, 220, 0.1);
    color: #999;
}

::v-deep .el-table .row-disabled .el-tag,
::v-deep .el-table .row-disabled .el-switch,
::v-deep .el-table .row-disabled .el-input-number {
    opacity: 0.7;
}

/* 覆盖Element Plus样式，与系统保持一致 */
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

::v-deep .el-tag--info {
    background-color: #409eff;
}

::v-deep .el-input__inner,
::v-deep .el-select-dropdown,
::v-deep .el-time-editor .el-input__inner,
::v-deep .el-textarea__inner,
::v-deep .el-input-number {
    border-radius: 4px;
}

::v-deep .el-table th {
    background-color: #fff7ee;
}

/* 响应式调整（参考活动管理页面） */
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
    .el-table-column:not(.el-table-column--selection):not([label="时段ID"]):not([label="时段名称"]):not([label="状态"]):not([label="操作"]) {
        display: none !important;
    }
}
</style>