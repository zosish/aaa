<template>
    <AdminLayout>
        <!-- 预约页面 -->
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h1>预约管理</h1>
            <el-button type="primary" :icon="Plus" @click="handleAddReservation">
                新增预约
            </el-button>
        </div>

        <!-- 搜索和筛选区 -->
        <el-card class="filter-card">
            <el-row :gutter="20">
                <el-col :span="4">
                    <el-input v-model="searchForm.username" placeholder="输入用户名" clearable
                        :prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="4">
                    <el-input v-model="searchForm.phone" placeholder="输入手机号" clearable :prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="4">
                    <el-select v-model="searchForm.catId" placeholder="选择猫咪" clearable>
                        <el-option v-for="cat in catOptions" :key="cat.id" :label="cat.name"
                            :value="cat.id"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="4">
                    <el-date-picker v-model="searchForm.reservationDate" type="date" placeholder="选择预约日期"
                        clearable></el-date-picker>
                </el-col>
                <el-col :span="4">
                    <!-- 选择时间段 -->
                    <el-input v-model="searchForm.timeSlot" placeholder="输入时间段" clearable
                        :prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="4">
                    <el-select v-model="searchForm.status" placeholder="预约状态" clearable>
                        <el-option label="待确认" value="PENDING"></el-option>
                        <el-option label="已确认" value="CONFIRMED"></el-option>
                        <el-option label="已取消" value="CANCELLED"></el-option>
                        <el-option label="已完成" value="COMPLETED"></el-option>
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
                <el-button type="danger" :icon="Delete" @click="handleBatchDelete"
                    :disabled="selectedReservations.length === 0">
                    批量删除
                </el-button>
            </div>
        </el-card>

        <!-- 预约列表 -->
        <el-card class="table-card">
            <el-table :data="reservationList" border stripe :loading="loading" @selection-change="handleSelectionChange"
                :row-class-name="tableRowClassName">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="id" label="预约ID" width="80" align="center"></el-table-column>
                <el-table-column prop="username" label="用户信息" min-width="120">
                    <template #default="scope">
                        <div class="user-info">
                            <div class="user-name">{{ scope.row.username }}</div>
                            <div class="user-contact">{{ scope.row.phone }}</div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="catName" label="预约猫咪" min-width="100"></el-table-column>
                <el-table-column prop="reservationDate" label="预约日期" min-width="120" align="center"></el-table-column>
                <el-table-column prop="timeSlot" label="时间段" min-width="120" align="center"></el-table-column>
                <el-table-column prop="visitorCount" label="访客人数" min-width="80" align="center"></el-table-column>
                <el-table-column prop="purpose" label="预约目的" min-width="150"></el-table-column>
                <el-table-column prop="status" label="状态" min-width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.status === 'PENDING' ? 'warning' :
                            scope.row.status === 'CONFIRMED' ? 'success' :
                                scope.row.status === 'COMPLETED' ? 'info' : 'danger'
                            ">
                            {{
                                scope.row.status === 'PENDING' ? '待确认' :
                                    scope.row.status === 'CONFIRMED' ? '已确认' :
                                        scope.row.status === 'COMPLETED' ? '已完成' : '已取消'
                            }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="160" align="center"></el-table-column>
                <el-table-column label="操作" min-width="200" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" :icon="Eye"
                            @click="handleViewReservation(scope.row)">预约详情</el-button>
                        <el-button size="small" type="success" :icon="Edit" @click="handleEditReservation(scope.row)"
                            v-if="scope.row.status === 'PENDING'">修改预约</el-button>
                        <el-button size="small" :type="scope.row.status === 'PENDING' ? 'success' : 'danger'"
                            :icon="scope.row.status === 'PENDING' ? Check : Close"
                            @click="handleStatusChange(scope.row)"
                            v-if="['PENDING', 'CONFIRMED'].includes(scope.row.status)">
                            {{ scope.row.status === 'PENDING' ? '确认' : '取消' }}
                        </el-button>
                        <el-button size="small" type="danger" :icon="Delete"
                            @click="handleDeleteReservation(scope.row)">删除</el-button>
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

        <!-- 预约详情/新增/编辑弹窗 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
            <el-form ref="reservationFormRef" :model="reservationForm" :rules="formRules" label-width="100px"
                class="reservation-form">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="用户名" prop="username">
                            <el-input v-model="reservationForm.username" placeholder="请输入用户名"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="手机号" prop="phone">
                            <el-input v-model="reservationForm.phone" placeholder="请输入手机号"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="预约日期" prop="reservationDate">
                            <el-date-picker v-model="reservationForm.reservationDate" type="date" placeholder="选择预约日期"
                                :disabled-date="disablePastDates" value-format="YYYY-MM-DD" @change="onDateChange"></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="开始时间" prop="startTime">
                            <el-time-picker
                              v-model="reservationForm.startTime"
                              placeholder="选择开始时间"
                              format="HH:mm"
                              value-format="HH:mm"
                              style="width: 100%"
                              @change="onTimeChange"
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="预约时长" prop="duration">
                            <el-select v-model="reservationForm.duration" placeholder="请选择时长" style="width: 100%" @change="onDurationChange">
                                <el-option label="30分钟" :value="30"></el-option>
                                <el-option label="60分钟" :value="60"></el-option>
                                <el-option label="90分钟" :value="90"></el-option>
                                <el-option label="120分钟" :value="120"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="访客人数" prop="visitorCount">
                            <el-input-number v-model.number="reservationForm.visitorCount" :min="1" :max="10" label="人数"
                                placeholder="请输入人数"></el-input-number>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="预约目的" prop="purpose">
                            <el-input v-model="reservationForm.purpose" placeholder="请输入预约目的" type="textarea"
                                rows="2"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24" v-if="dialogType !== 'add'">
                        <el-form-item label="预约状态" prop="status">
                            <el-select v-model="reservationForm.status" placeholder="选择状态">
                                <el-option label="待确认" value="PENDING"></el-option>
                                <el-option label="已确认" value="CONFIRMED"></el-option>
                                <el-option label="已取消" value="CANCELLED"></el-option>
                                <el-option label="已完成" value="COMPLETED"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="管理员备注" prop="adminNotes">
                            <el-input v-model="reservationForm.adminNotes" placeholder="请输入备注信息" type="textarea"
                                rows="2"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24" v-if="reservationForm.status === 'CANCELLED'">
                        <el-form-item label="取消原因">
                            <el-input v-model="reservationForm.cancelReason" placeholder="取消原因" type="textarea" rows="2"
                                disabled></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleNextStep" v-if="dialogType !== 'view'">
                    下一步
                </el-button>
            </template>
        </el-dialog>

        <!-- 桌号选择弹窗 -->
        <el-dialog v-model="tableSelectDialogVisible" title="选择桌号" width="750px" :close-on-click-modal="false">
            <el-alert
                title="预约信息"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px"
            >
                <el-descriptions :column="2" border size="small">
                    <el-descriptions-item label="预约日期">
                        {{ reservationForm.reservationDate }}
                    </el-descriptions-item>
                    <el-descriptions-item label="时间段">
                        {{ reservationForm.startTime }} - {{ calculateEndTime(reservationForm.startTime, reservationForm.duration) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="预约时长">
                        {{ reservationForm.duration }}分钟
                    </el-descriptions-item>
                    <el-descriptions-item label="预约人">
                        {{ reservationForm.username }}
                    </el-descriptions-item>
                </el-descriptions>
            </el-alert>
            
            <el-divider content-position="left">可选桌号 <el-tag type="success" size="small">{{ availableTables.length }}个</el-tag></el-divider>
            
            <div v-if="availableTables.length > 0" class="table-grid">
                <div 
                    v-for="table in availableTables" 
                    :key="table.id"
                    class="table-item"
                    :class="{ selected: reservationForm.tableId === table.id }"
                    @click="selectTable(table)"
                >
                    <div class="table-number">{{ table.tableNumber }}</div>
                    <div class="table-capacity">{{ table.capacity }}人桌</div>
                    <div class="table-status" v-if="reservationForm.tableId === table.id">已选择</div>
                </div>
            </div>
            <div v-else-if="isLoadingTables" class="loading-text">
                <el-icon><Loading /></el-icon>
                正在检测时间冲突并加载桌号中...
            </div>
            <div v-else class="empty-text">
                <el-icon style="font-size: 40px; margin-bottom: 10px; color: #e6a23c;"><Warning /></el-icon>
                <div>该时间段所有桌号都已被预约，请返回选择其他时间</div>
            </div>
            
            <template #footer>
                <el-button @click="handleBackToEdit">返回上一步</el-button>
                <el-button type="primary" @click="handleSaveReservation" :disabled="!reservationForm.tableId">
                    保存预约
                </el-button>
            </template>
        </el-dialog>

        <!-- 批量删除确认弹窗 -->
        <el-dialog v-model="batchDeleteVisible" title="确认删除" width="400px" :close-on-click-modal="false">
            <p>您确定要删除选中的 {{ selectedReservations.length }} 条预约记录吗？此操作不可撤销。</p>
            <template #footer>
                <el-button @click="batchDeleteVisible = false">取消</el-button>
                <el-button type="danger" @click="confirmBatchDelete">
                    确认删除
                </el-button>
            </template>
        </el-dialog>
    </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import AdminLayout from './AdminLayout.vue';
import {
    Plus,
    Search,
    Eye, Edit, Delete, Check, Close, Loading, Warning
} from '@element-plus/icons-vue';

// 状态管理
const loading = ref(false);
const reservationList = ref([]);
const selectedReservations = ref([]);
const catOptions = ref([]); // 猫咪选项
const availableTables = ref([]); // 可用桌号列表
const isLoadingTables = ref(false); // 桌号加载状态
const reservationFormRef = ref(null); // 表单引用

// 搜索表单
const searchForm = reactive({
    // username: '',
    catId: '',
    reservationDate: '',
    timeSlot: '',
    status: '',
    username: '',
    phone: ''
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
const batchDeleteVisible = ref(false);
const tableSelectDialogVisible = ref(false); // 桌号选择弹窗

// 表单数据和验证规则
const reservationForm = reactive({
    id: '',
    userId: '',
    username: '',
    phone: '',
    reservationDate: '',
    startTime: '',
    duration: '',
    visitorCount: 1,
    purpose: '',
    status: 'PENDING',
    adminNotes: '',
    userNotes: '',
    cancelReason: '',
    createTime: '',
    updateTime: '',
    tableId: '',
    tableNumber: ''
});

const formRules = reactive({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { max: 50, message: '用户名长度不能超过50个字符', trigger: 'blur' }
    ],
    phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ],
    reservationDate: [
        { required: true, message: '请选择预约日期', trigger: 'change' }
    ],
    startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' }
    ],
    duration: [
        { required: true, message: '请选择预约时长', trigger: 'change' }
    ],
    visitorCount: [
        { required: true, message: '请输入访客人数', trigger: 'blur' },
        { type: 'number', min: 1, max: 10, message: '人数必须在1-10之间', trigger: 'blur' }
    ],
    purpose: [
        { max: 200, message: '预约目的不能超过200个字符', trigger: 'blur' }
    ],
    adminNotes: [
        { max: 200, message: '备注信息不能超过200个字符', trigger: 'blur' }
    ],
    cancelReason: [
        { max: 200, message: '取消原因不能超过200个字符', trigger: 'blur' }
    ]
});

// 生命周期
onMounted(() => {
    fetchCatOptions();
    fetchReservationList();
});

// 方法：获取猫咪选项
const fetchCatOptions = async () => {
    try {
        // 获取后端数据
        const res = await fetch("http://localhost:8081/catcate/cats/listActive", {
            method: "GET",
        })
        const response = await res.json();
        if (response && Array.isArray(response)) {
            catOptions.value = response.map(item => ({
                id: item.id,
                name: item.name
            }));
        }
    } catch (error) {
        ElMessage.error('获取猫咪列表失败');
    }
};
const getCatNameById = (catId) => {
    const cat = catOptions.value.find(cat => cat.id == catId);
    return cat ? cat.name : '';
};
// 方法：获取预约列表
const fetchReservationList = async () => {
    // 确保猫咪选项已加载
    if (catOptions.value.length === 0) {
        await fetchCatOptions();
    }
    loading.value = true;
    try {
        // 获取后端数据
        const res = await fetch("http://localhost:8081/catcate/reservations/listAppointment", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                pageNum: pagination.currentPage,
                pageSize: pagination.pageSize,
                catId: searchForm.catId,
                reservationDate: searchForm.reservationDate,
                timeSlot: searchForm.timeSlot,
                status: searchForm.status,
                username: searchForm.username,
                phone: searchForm.phone,
            }),
        });

        const response = await res.json();

        // 处理后端返回的数据格式（正确解析 {total: ..., list: [...]} 结构）
        if (response && response.list && Array.isArray(response.list)) {
            // 处理列表数据
            const processedData = response.list.map(item => ({
                ...item,
                // 格式化日期显示
                reservationDate: item.reservationDate ?
                    new Date(item.reservationDate).toISOString().split('T')[0] : '',
                // 格式化创建时间
                createTime: item.createTime ?
                    new Date(item.createTime).toLocaleString('zh-CN', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit'
                    }).replace(/\//g, '-').replace(',', '') : '',
                // 格式化更新时间
                updateTime: item.updateTime ?
                    new Date(item.updateTime).toLocaleString('zh-CN', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit'
                    }).replace(/\//g, '-').replace(',', '') : '',
                // 根据catId查找并设置catName
                catName: getCatNameById(item.catId),
                // 设置默认值
                username: item.username || '',
                phone: item.phone || ''
            }));

            reservationList.value = processedData;
            pagination.total = response.total || processedData.length; // 从后端获取总数
        }

        loading.value = false;
    } catch (error) {
        ElMessage.error('获取预约列表失败');
        loading.value = false;
    }
};

// 搜索和重置
const handleSearch = async () => {
    pagination.currentPage = 1;
    try {
        // 格式化日期，确保时区正确处理
        let formattedDate = '';
        if (searchForm.reservationDate) {
            if (typeof searchForm.reservationDate === 'string') {
                formattedDate = searchForm.reservationDate;
            } else {
                // 使用 YYYY-MM-DD 格式并考虑时区
                const date = new Date(searchForm.reservationDate);
                // 重置为UTC时间避免时区影响
                date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
                formattedDate = date.toISOString().split('T')[0];
            }
        }

        const res = await fetch("http://localhost:8081/catcate/reservations/listAppointment", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                pageNum: pagination.currentPage,
                pageSize: pagination.pageSize,
                catId: searchForm.catId,
                reservationDate: formattedDate,
                timeSlot: searchForm.timeSlot,
                status: searchForm.status,
                username: searchForm.username, 
                phone: searchForm.phone
            }),
        });
        const response = await res.json();
        
        // 正确处理后端返回的数据结构
        if (response && response.list && Array.isArray(response.list)) {
            // 处理列表数据
            const processedData = response.list.map(item => ({
                ...item,
                // 格式化日期显示
                reservationDate: item.reservationDate ?
                    new Date(item.reservationDate).toISOString().split('T')[0] : '',
                // 格式化创建时间
                createTime: item.createTime ?
                    new Date(item.createTime).toLocaleString('zh-CN', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit'
                    }).replace(/\//g, '-').replace(',', '') : '',
                // 格式化更新时间
                updateTime: item.updateTime ?
                    new Date(item.updateTime).toLocaleString('zh-CN', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit'
                    }).replace(/\//g, '-').replace(',', '') : '',
                // 根据catId查找并设置catName
                catName: getCatNameById(item.catId),
                // 设置默认值
                username: item.username || '',
                phone: item.phone || ''
            }));

            reservationList.value = processedData;
            pagination.total = response.total || processedData.length; // 从后端获取总数
        }
    } catch (error) {
        ElMessage.error('搜索失败');
    }
};
const resetSearch = () => {
    searchForm.username = '';
    searchForm.phone = '';
    searchForm.catId = '';
    searchForm.reservationDate = '';
    searchForm.timeSlot = '';
    searchForm.status = '';
    pagination.currentPage = 1;
    fetchReservationList();
};

// 分页处理
const handleSizeChange = (size) => {
    pagination.pageSize = size;
    pagination.currentPage = 1;
    fetchReservationList();
};

const handleCurrentChange = (page) => {
    pagination.currentPage = page;
    fetchReservationList();
};

// 选择变化
const handleSelectionChange = (selection) => {
    selectedReservations.value = selection;
};

// 表格行样式（根据状态区分）
const tableRowClassName = ({ row }) => {
    if (row.status === 'PENDING') {
        return 'row-pending';
    } else if (row.status === 'CANCELLED') {
        return 'row-cancelled';
    }
    return '';
};

// 禁用过去的日期
const disablePastDates = (time) => {
    return time.getTime() < Date.now() - 86400000; // 禁用今天之前的日期
};

// 加载可用桌号
const loadAvailableTables = async () => {
    if (!reservationForm.reservationDate || !reservationForm.startTime || !reservationForm.duration) {
        availableTables.value = [];
        return;
    }
    
    isLoadingTables.value = true;
    try {
        // 计算当前选择的时间区间
        const [currentStartHours, currentStartMinutes] = reservationForm.startTime.split(':').map(Number);
        const currentStartTimeMinutes = currentStartHours * 60 + currentStartMinutes;
        const currentEndTimeMinutes = currentStartTimeMinutes + parseInt(reservationForm.duration);
        
        console.log('=== 检测时间冲突 ===');
        console.log('选择的日期:', reservationForm.reservationDate);
        console.log('当前时间段:', reservationForm.startTime, '至', calculateEndTime(reservationForm.startTime, reservationForm.duration));
        console.log('时间区间（分钟）:', currentStartTimeMinutes, '-', currentEndTimeMinutes);
        
        // 从预约列表中获取已被预约的座位号
        const res = await fetch("http://localhost:8081/catcate/reservations/listAppointment", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                pageNum: 1,
                pageSize: 1000,
                reservationDate: reservationForm.reservationDate
            })
        });
        
        let bookedTables = [];
        if (res.ok) {
            const response = await res.json();
            console.log('后端返回数据:', response);
            
            // 尝试多种数据格式
            let reservations = [];
            if (Array.isArray(response)) {
                reservations = response;
            } else if (response && response.list) {
                reservations = response.list;
            } else if (response && response.data) {
                reservations = Array.isArray(response.data) ? response.data : [];
            } else if (response && response.records) {
                reservations = Array.isArray(response.records) ? response.records : [];
            } else if (response && response.result) {
                reservations = Array.isArray(response.result) ? response.result : [];
            }
            
            console.log('预约数量:', reservations.length);
            
            if (reservations && reservations.length > 0) {
                // 提取已被预约的桌号 - 只在时间段有重叠时才标记为已预约
                const validReservations = reservations.filter(item => {
                    const isCancelled = item.status === 'CANCELLED';
                    const hasTableNumber = !!(item.tableNumber || item.table_number);
                    const hasTimeSlot = !!(item.timeSlot || item.time_slot);
                    return !isCancelled && hasTableNumber && hasTimeSlot;
                });
                
                console.log('有效预约数量:', validReservations.length);
                
                // 检查每个预约是否与当前时间段有重叠
                bookedTables = validReservations
                    .filter(item => {
                        try {
                            // 如果是编辑模式，排除当前正在编辑的预约
                            if (dialogType.value === 'edit' && item.id === reservationForm.id) {
                                return false;
                            }
                            
                            // 解析预约时间段 (格式为 "HH:mm-HH:mm")，支持驼峰和下划线
                            const timeSlotValue = item.timeSlot || item.time_slot;
                            const tableNum = item.tableNumber || item.table_number;
                            
                            if (!timeSlotValue) {
                                return false;
                            }
                            
                            const timeSlotParts = timeSlotValue.split('-');
                            if (timeSlotParts.length !== 2) {
                                return false;
                            }
                            
                            const [bookedStart, bookedEnd] = timeSlotParts;
                            const [bookedStartHours, bookedStartMinutes] = bookedStart.split(':').map(Number);
                            const [bookedEndHours, bookedEndMinutes] = bookedEnd.split(':').map(Number);
                            
                            const bookedStartTimeMinutes = bookedStartHours * 60 + bookedStartMinutes;
                            const bookedEndTimeMinutes = bookedEndHours * 60 + bookedEndMinutes;
                            
                            // 判断时间区间是否有重叠
                            // 重叠条件：当前开始 < 预约结束 且 当前结束 > 预约开始
                            const hasOverlap = currentStartTimeMinutes < bookedEndTimeMinutes && 
                                              currentEndTimeMinutes > bookedStartTimeMinutes;
                            
                            if (hasOverlap) {
                                console.log('发现时间冲突:', tableNum, timeSlotValue);
                            }
                            
                            return hasOverlap;
                        } catch (e) {
                            console.error('解析预约时间失败:', e);
                            return false;
                        }
                    })
                    .map(item => item.tableNumber || item.table_number)
                    .filter(tableNumber => tableNumber);
            }
        }
        
        console.log('已预约桌号:', bookedTables);
        
        // 直接过滤掉已被预约的桌号，只显示可用的
        availableTables.value = getDefaultTables().filter(table => {
            return !bookedTables.includes(table.tableNumber);
        });
        
        console.log('可用桌号:', availableTables.value.map(t => t.tableNumber));
        
        // 如果当前选中的桌号不在可用列表中，清空选择
        if (reservationForm.tableId) {
            const isTableStillAvailable = availableTables.value.some(table => table.id === reservationForm.tableId);
            if (!isTableStillAvailable) {
                reservationForm.tableId = '';
                reservationForm.tableNumber = '';
            }
        }
        
    } catch (error) {
        console.error('加载可用桌号失败:', error);
        availableTables.value = [];
        ElMessage.error('加载可用桌号失败，请重试');
    } finally {
        isLoadingTables.value = false;
    }
};

// 获取默认桌号数据（作为备选）
const getDefaultTables = () => {
    return [
        { id: 1, tableNumber: '1号桌', capacity: 2, status: 'AVAILABLE' },
        { id: 2, tableNumber: '2号桌', capacity: 2, status: 'AVAILABLE' },
        { id: 3, tableNumber: '3号桌', capacity: 4, status: 'AVAILABLE' },
        { id: 4, tableNumber: '4号桌', capacity: 4, status: 'AVAILABLE' },
        { id: 5, tableNumber: '5号桌', capacity: 6, status: 'AVAILABLE' },
        { id: 6, tableNumber: '6号桌', capacity: 6, status: 'AVAILABLE' },
        { id: 7, tableNumber: '7号桌', capacity: 8, status: 'AVAILABLE' },
        { id: 8, tableNumber: '8号桌', capacity: 8, status: 'AVAILABLE' }
    ];
};

// 选择桌号
const selectTable = (table) => {
    reservationForm.tableId = table.id;
    reservationForm.tableNumber = table.tableNumber;
};

// 日期改变时加载桌号
const onDateChange = () => {
    loadAvailableTables();
};

// 时间改变时加载桌号
const onTimeChange = () => {
    reservationForm.tableId = '';
    reservationForm.tableNumber = '';
    loadAvailableTables();
};

// 时长改变时重新加载桌号
const onDurationChange = () => {
    reservationForm.tableId = '';
    reservationForm.tableNumber = '';
    loadAvailableTables();
};

// 状态切换
const handleStatusChange = (row) => {
    const newStatus = row.status === 'PENDING' ? 'CONFIRMED' : 'CANCELLED';
    const confirmText = row.status === 'PENDING' ? '确认' : '取消';

    ElMessageBox.confirm(
        `确定要${confirmText}该预约吗？`,
        '确认操作',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: row.status === 'PENDING' ? 'success' : 'danger'
        }
    ).then(async () => {
        loading.value = true;
        // 修改后端数据
        try {
            const res = await fetch(`http://localhost:8081/catcate/reservations/${row.id}/status`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: newStatus })
            });
            if (res.ok) {
                // 更新本地数据
                row.status = newStatus;
                row.updateTime = new Date().toISOString().replace('T', ' ').substring(0, 19);
                if (newStatus === 'CANCELLED') {
                    row.cancelReason = '管理员操作取消';
                }
                ElMessage.success(`预约已${confirmText}成功`);
                loading.value = false;
                // 刷新当前页数据
                fetchReservationList();
            } else {
                ElMessage.error('操作失败');
                loading.value = false;
            }
        } catch (error) {
            ElMessage.error('操作失败');
            loading.value = false;
        }
    }).catch(() => {
        // 取消操作
    });
};

// 弹窗操作
const handleAddReservation = () => {
    dialogType.value = 'add';
    dialogTitle.value = '新增预约';
    // 重置表单
    Object.assign(reservationForm, {
        id: '',
        userId: '',
        username: '',
        phone: '',
        reservationDate: '',
        startTime: '',
        duration: '',
        visitorCount: 1,
        purpose: '',
        status: 'PENDING',
        adminNotes: '',
        userNotes: '',
        cancelReason: '',
        tableId: '',
        tableNumber: ''
    });
    availableTables.value = [];
    dialogVisible.value = true;
};
const handleEditReservation = (row) => {
    dialogType.value = 'edit';
    dialogTitle.value = '修改预约';
    
    // 解析时间槽，提取开始时间
    let extractedStartTime = row.startTime || '';
    let extractedDuration = row.duration || '';
    
    // 如果 start time 为空但有 timeSlot，则解析 timeSlot
    if (!extractedStartTime && row.timeSlot) {
        const timeSlotParts = row.timeSlot.split('-');
        if (timeSlotParts.length === 2) {
            extractedStartTime = timeSlotParts[0];
            // 计算持续时间
            try {
                const [startH, startM] = timeSlotParts[0].split(':').map(Number);
                const [endH, endM] = timeSlotParts[1].split(':').map(Number);
                const startMinutes = startH * 60 + startM;
                const endMinutes = endH * 60 + endM;
                extractedDuration = endMinutes - startMinutes;
            } catch (e) {
                // 如果解析失败，保持原样
            }
        }
    }
    
    // 填充表单数据
    Object.assign(reservationForm, {
        id: row.id,
        userId: row.userId,
        username: row.username,
        phone: row.phone,
        reservationDate: row.reservationDate,
        startTime: extractedStartTime,
        duration: extractedDuration,
        visitorCount: row.visitorCount,
        purpose: row.purpose,
        status: row.status,
        adminNotes: row.adminNotes,
        userNotes: row.userNotes,
        cancelReason: row.cancelReason,
        tableId: row.tableId || '',
        tableNumber: row.tableNumber || ''
    });
    
    // 加载可用桌号
    setTimeout(() => {
        loadAvailableTables();
    }, 100);
    dialogVisible.value = true;
};

const handleViewReservation = (row) => {
    dialogType.value = 'view';
    dialogTitle.value = '查看预约详情';
    // 填充表单数据
    Object.assign(reservationForm, row);
    dialogVisible.value = true;
};

// 计算结束时间
const calculateEndTime = (startTime, duration) => {
    if (!startTime || !duration) return '';
    const [hours, minutes] = startTime.split(':').map(Number);
    const totalMinutes = hours * 60 + minutes + duration;
    const endHours = Math.floor(totalMinutes / 60) % 24;
    const endMinutes = totalMinutes % 60;
    return `${String(endHours).padStart(2, '0')}:${String(endMinutes).padStart(2, '0')}`;
};

// 下一步 - 进入桌号选择
const handleNextStep = async () => {
    // 确保表单ref已初始化
    await nextTick();

    if (!reservationFormRef.value) {
        ElMessage.error('表单初始化失败');
        return;
    }

    // 表单验证
    try {
        await reservationFormRef.value.validate();
        
        // 检查必要条件
        if (!reservationForm.reservationDate || !reservationForm.startTime || !reservationForm.duration) {
            ElMessage.warning('请先选择预约日期、时间和时长');
            return;
        }
        
        // 显示加载中提示
        const loadingMsg = ElMessage({
            message: '正在检测可用桌号...',
            type: 'info',
            duration: 0,
            iconClass: 'el-icon-loading'
        });
        
        // 加载可用桌号
        await loadAvailableTables();
        
        loadingMsg.close();
        
        if (availableTables.value.length === 0) {
            ElMessage.warning('该时间段所有桌号都已被预约，请选择其他时间');
            return;
        }
        
        // 关闭第一步弹窗，打开桌号选择弹窗
        dialogVisible.value = false;
        tableSelectDialogVisible.value = true;
        
    } catch (error) {
        // 表单验证失败
        ElMessage.warning('请完善表单信息');
    }
};

// 返回上一步
const handleBackToEdit = () => {
    tableSelectDialogVisible.value = false;
    dialogVisible.value = true;
};

// 保存修改预约信息
const handleSaveReservation = async () => {
    // 检查是否选择了桌号
    if (!reservationForm.tableId) {
        ElMessage.warning('请选择桌号');
        return;
    }

    loading.value = true;
    
    try {
        // 格式化日期时间为 yyyy-MM-dd HH:mm:ss
        const now = new Date();
        const formattedDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
        
        // 构建请求数据
        const reservationData = {
            userId: 1,
            catId: 1,
            username: reservationForm.username,
            phone: reservationForm.phone,
            reservationDate: reservationForm.reservationDate,
            timeSlot: `${reservationForm.startTime}-${calculateEndTime(reservationForm.startTime, reservationForm.duration)}`,
            visitorCount: parseInt(reservationForm.visitorCount),
            purpose: reservationForm.purpose || '',
            status: reservationForm.status,
            adminNotes: reservationForm.adminNotes || '',
            userNotes: '',
            createTime: formattedDate,
            updateTime: formattedDate
        };
        
        // 只在有值时才添加桌号字段
        if (reservationForm.tableId) {
            reservationData.tableId = parseInt(reservationForm.tableId);
        }
        if (reservationForm.tableNumber) {
            reservationData.tableNumber = reservationForm.tableNumber;
        }
        
        // 编辑时添加id和userId（如果有值）
        if (dialogType.value !== 'add') {
            if (reservationForm.id) {
                reservationData.id = parseInt(reservationForm.id);
            }
            if (reservationForm.userId) {
                reservationData.userId = parseInt(reservationForm.userId);
            }
        }
        
        // 请求后端
        const res = await fetch("http://localhost:8081/catcate/reservations/addOrUpdateReservation", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(reservationData)
        });
        
        if (res.status === 200) {
            const response = await res.json();
            if (response == true) {
                ElMessage.success(dialogType.value === 'add' ? '预约创建成功' : '预约更新成功');
            } else {
                ElMessage.error(dialogType.value === 'add' ? '预约创建失败' : '预约更新失败');
            }
            tableSelectDialogVisible.value = false;
            // 刷新当前页数据
            fetchReservationList();
        } else {
            ElMessage.error('请求失败，请检查数据格式');
        }
    } catch (error) {
        console.error(error);
        ElMessage.warning('保存预约失败');
    } finally {
        loading.value = false;
    }
};

// 删除操作
const handleDeleteReservation = (row) => {
    ElMessageBox.confirm(
        `确定要删除预约ID: ${row.id} 的记录吗？此操作不可撤销。`,
        '确认删除',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'danger'
        }
    ).then(async () => {
        try {
            loading.value = true;
            // 发送删除请求
            const res = await fetch("http://localhost:8081/catcate/reservations/deleteReservation", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(row.id)
            });
            const response = await res.json();
            if (response == true) {
                ElMessage.success('删除成功');
            } else {
                ElMessage.error('删除失败');
            }
            loading.value = false;
            // 刷新当前页数据
            fetchReservationList();
        } catch (error) {
            ElMessage.error('删除失败');
            loading.value = false;
        }
    });
};

// 批量删除
const handleBatchDelete = () => {
    if (selectedReservations.value.length === 0) {
        ElMessage.warning('请选择要删除的预约记录');
        return;
    }
    batchDeleteVisible.value = true;
};

const confirmBatchDelete = async () => {
    try {
        loading.value = true;
        //发送批量删除请求
        const res = await fetch("http://localhost:8081/catcate/reservations/delReservations", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(selectedReservations.value.map(item => item.id))
        });
        const response = await res.json();
        if (response == true) {
            ElMessage.success(`成功删除${selectedReservations.value.length}条预约记录`);
        } else {
            ElMessage.error('批量删除失败');
        }
        batchDeleteVisible.value = false;
        loading.value = false;
        // 刷新当前页数据
        fetchReservationList();
    } catch (error) {
        ElMessage.error('批量删除失败');
        loading.value = false;
    }
};
</script>

<style scoped>
.reservation-management {
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
}

.filter-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 15px;
    gap: 10px;
}

.table-card {
    overflow: hidden;
}

.pagination {
    margin-top: 15px;
    text-align: right;
}

.reservation-form {
    margin-top: 10px;
}

/* 用户信息样式 */
.user-info {
    display: flex;
    flex-direction: column;
}

.user-name {
    font-weight: 500;
    color: #1f2329;
}

.user-contact {
    font-size: 12px;
    color: #666;
}

/* 表格行样式 */
:deep(.el-table .row-pending) {
    background-color: rgba(255, 248, 225, 0.3);
}

:deep(.el-table .row-cancelled) {
    background-color: rgba(248, 248, 255, 0.3);
}

/* 桌号选择样式 */
.table-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-top: 8px;
}

.table-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 15px 10px;
    border: 2px solid #e4e7ed;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    background-color: #fff;
    min-height: 70px;
}

.table-item:hover {
    border-color: #409eff;
    background-color: #ecf5ff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.table-item.selected {
    border-color: #409eff;
    background-color: #ecf5ff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.table-number {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.table-capacity {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
}

.table-status {
    font-size: 11px;
    color: #67c23a;
    margin-top: 4px;
    font-weight: 500;
}

.loading-text, .empty-text {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: #909399;
    text-align: center;
}

.loading-text svg {
    margin-right: 8px;
    animation: rotate 2s linear infinite;
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
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
    .el-table-column:not(.el-table-column--selection):not([label="预约ID"]):not([label="用户信息"]):not([label="状态"]):not([label="操作"]) {
        display: none !important;
    }
}
</style>