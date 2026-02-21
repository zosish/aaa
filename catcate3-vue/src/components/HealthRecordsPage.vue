<template>
    <div class="cat-health-management">
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h2 class="page-title">猫咪健康记录管理</h2>
            <el-button type="primary" icon="Plus" @click="openAddRecordDialog" class="add-btn"
                style="padding-left: 0px;">
                新增健康记录
            </el-button>
        </div>

        <!-- 搜索筛选区域（参考活动管理页面风格） -->
        <el-card class="filter-card">
            <el-row :gutter="20">
                <el-col :span="6">
                    <el-input v-model="searchForm.symptomsKeyword" placeholder="搜索症状描述" clearable
                        prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="5">
                    <el-select v-model="searchForm.catId" placeholder="选择猫咪" clearable filterable>
                        <el-option v-for="cat in catList" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="5">
                    <el-select v-model="searchForm.healthStatus" placeholder="筛选健康状态" clearable>
                        <el-option label="全部状态" value=""></el-option>
                        <el-option label="健康" value="HEALTHY"></el-option>
                        <el-option label="生病" value="SICK"></el-option>
                        <el-option label="恢复中" value="RECOVERING"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="6">
                    <el-input v-model="searchForm.checker" placeholder="搜索检查人员" clearable
                        prefix-icon="User"></el-input>
                </el-col>
            </el-row>

            <!-- 搜索和重置按钮区域（参考活动管理页面布局） -->
            <div class="filter-actions">
                <el-button type="primary" icon="Search" @click="handleSearch" class="search-btn"
                    style="padding-left: 0px;">
                    搜索
                </el-button>
                <el-button type="default" icon="Refresh" @click="handleReset" class="reset-btn"
                    style="padding-left: 0px;">
                    重置
                </el-button>

                <!-- 批量删除按钮（保持与活动管理页面一致的批量操作风格） -->
                <el-button type="danger" icon="Delete" @click="handleBatchDelete" style="padding-left: 0px;"
                    :disabled="selectedRecords.length === 0">
                    批量删除
                </el-button>
            </div>
        </el-card>

        <!-- 健康记录列表 -->
        <el-card class="record-list-card">
            <el-table :data="healthRecordList" border stripe :loading="loading"
                @selection-change="handleSelectionChange" style="width: 100%;">
                <el-table-column type="selection" width="55" align="center"></el-table-column>
                <el-table-column prop="id" label="记录ID" width="100" align="center"></el-table-column>
                <el-table-column label="猫咪信息" min-width="200">
                    <template #default="scope">
                        <div class="cat-info">
                            <el-avatar :src="getCatPhoto(scope.row.catId) || defaultAvatar" size="48"
                                class="cat-avatar"></el-avatar>
                            <div class="cat-details">
                                <div class="cat-name">
                                    {{ getCatName(scope.row.catId) }}
                                    <el-tag :type="getStatusTagType(scope.row.healthStatus)" class="status-tag"
                                        style="color: #fff;">
                                        {{ getStatusText(scope.row.healthStatus) }}
                                    </el-tag>
                                </div>
                                <div class="cat-meta">
                                    {{ getCatBreed(scope.row.catId) }} · {{ getCatAge(scope.row.catId) }}个月 · {{
                                        getGenderText(getCatGender(scope.row.catId)) }}
                                </div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="checkDate" label="检查日期" width="140" align="center"></el-table-column>
                <el-table-column prop="symptoms" label="症状描述" min-width="220">
                    <template #default="scope">
                        <div class="text-ellipsis" :title="scope.row.symptoms">
                            {{ scope.row.symptoms || '无' }}
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="treatment" label="处理措施" min-width="220">
                    <template #default="scope">
                        <div class="text-ellipsis" :title="scope.row.treatment">
                            {{ scope.row.treatment || '无' }}
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="checker" label="检查人员" width="120" align="center"></el-table-column>
                <el-table-column prop="updateTime" label="最后更新" width="180" align="center"></el-table-column>
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="info" icon="Eye" style="padding-left: 0px;"
                            @click="openViewRecordDialog(scope.row)">查看</el-button>
                        <el-button size="small" type="primary" icon="Edit" style="padding-left: 0px;"
                            @click="openEditRecordDialog(scope.row)">编辑</el-button>
                        <el-button size="small" type="danger" icon="Delete" style="padding-left: 0px;"
                            @click="handleDeleteRecord(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页控件 -->
            <div class="pagination-container">
                <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
                    :page-sizes="[10, 20, 50]" :total="pagination.total"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"></el-pagination>
            </div>
        </el-card>

        <!-- 批量删除确认弹窗 -->
        <el-dialog v-model="batchDeleteDialogVisible" title="批量删除确认" width="400px" :close-on-click-modal="false">
            <p>确定要删除选中的 {{ selectedRecords.length }} 条健康记录吗？删除后不可恢复。</p>
            <template #footer>
                <el-button @click="batchDeleteDialogVisible = false">取消</el-button>
                <el-button type="danger" @click="confirmBatchDelete">确认删除</el-button>
            </template>
        </el-dialog>

        <!-- 新增/编辑健康记录弹窗 -->
        <el-dialog v-model="recordDialogVisible" :title="isEditMode ? '编辑健康记录' : '新增健康记录'" width="700px"
            :close-on-click-modal="false" append-to-body>
            <el-form ref="recordFormRef" :model="recordForm" :rules="recordFormRules" label-width="100px"
                class="record-form">
                <el-form-item label="猫咪选择" prop="catId">
                    <el-select v-model="recordForm.catId" placeholder="请选择猫咪" filterable @change="handleCatSelect"
                        style="width: 100%;">
                        <el-option v-for="cat in catList" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
                    </el-select>
                </el-form-item>

                <!-- 猫咪信息预览 -->
                <el-form-item label="猫咪信息" class="cat-preview-item">
  <div class="cat-preview-card">
    <el-avatar 
      :src="getProcessedCatImageUrl(selectedCat.photoUrl)" 
      size="56"
      class="preview-avatar"
      @error="handleAvatarError"
    ></el-avatar>
    <div class="preview-details">
      <div class="preview-name">
        {{ selectedCat.name || '未选择猫咪' }}
        <el-tag 
          style="color: #fff;" 
          :type="getStatusTagType(selectedCat.healthStatus)" 
          class="preview-status-tag"
        >
          {{ getStatusText(selectedCat.healthStatus) || '未知状态' }}
        </el-tag>
      </div>
      <div class="preview-meta">
        <span>品种：{{ selectedCat.breed || '-' }}</span>
        <span>年龄：{{ selectedCat.age || '-' }}个月</span>
        <span>性别：{{ getGenderText(selectedCat.gender) || '-' }}</span>
        <span>领养状态：{{ getAdoptionStatusText(selectedCat.adoptionStatus) || '-' }}</span>
      </div>
    </div>
  </div>
</el-form-item>

                <el-form-item label="检查日期" prop="checkDate">
                    <el-date-picker v-model="recordForm.checkDate" type="date" placeholder="请选择检查日期" format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD" :disabled-date="disabledPastDate"
                        style="width: 100%;"></el-date-picker>
                </el-form-item>

                <el-form-item label="健康状态" prop="healthStatus">
                    <el-select v-model="recordForm.healthStatus" placeholder="请选择健康状态" style="width: 100%;">
                        <el-option label="健康" value="HEALTHY"></el-option>
                        <el-option label="生病" value="SICK"></el-option>
                        <el-option label="恢复中" value="RECOVERING"></el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="症状描述" prop="symptoms">
                    <el-input v-model="recordForm.symptoms" type="textarea" :rows="3"
                        placeholder="请描述猫咪的症状（健康状态为'健康'时可留空）" maxlength="500" show-word-limit></el-input>
                </el-form-item>

                <el-form-item label="处理措施" prop="treatment">
                    <el-input v-model="recordForm.treatment" type="textarea" :rows="3" placeholder="请描述采取的处理措施（如用药、护理等）"
                        maxlength="500" show-word-limit></el-input>
                </el-form-item>

                <el-form-item label="检查人员" prop="checker">
                    <el-input v-model="recordForm.checker" placeholder="请输入检查人员姓名" maxlength="50"></el-input>
                </el-form-item>

                <el-form-item label="备注信息">
                    <el-input v-model="recordForm.remark" type="textarea" :rows="2" placeholder="请输入备注信息（可选）"
                        maxlength="300" show-word-limit></el-input>
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="recordDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitRecordForm">保存记录</el-button>
            </template>
        </el-dialog>

        <!-- 查看健康记录弹窗 -->
        <el-dialog v-model="viewDialogVisible" title="查看健康记录详情" width="700px" :close-on-click-modal="false"
            append-to-body>
            <div class="view-record-container">
                <!-- 头部信息 -->
                <div class="view-header">
                    <el-avatar :src="getCatPhoto(currentRecord.catId) || defaultAvatar" size="64"
                        class="view-avatar"></el-avatar>
                    <div class="view-header-info">
                        <h3 class="view-cat-name">
                            {{ getCatName(currentRecord.catId) }}
                            <el-tag style="color: #fff;" :type="getStatusTagType(currentRecord.healthStatus)"
                                class="view-status-tag">
                                {{ getStatusText(currentRecord.healthStatus) }}
                            </el-tag>
                        </h3>
                        <div class="view-header-meta">
                            <span class="meta-item">记录ID：{{ currentRecord.id }}</span>
                            <span class="meta-item">检查日期：{{ currentRecord.checkDate }}</span>
                            <span class="meta-item">检查人员：{{ currentRecord.checker || '未知' }}</span>
                            <span class="meta-item">创建时间：{{ formatDateTime(currentRecord.createTime) }}</span>
                            <span class="meta-item">最后更新：{{ formatDateTime(currentRecord.updateTime) }}</span>
                        </div>
                    </div>
                </div>

                <!-- 猫咪基础信息 -->
                <div class="view-section">
                    <h4 class="section-title">猫咪基础信息</h4>
                    <div class="section-content">
                        <el-row :gutter="30">
                            <el-col :span="8">
                                <span class="content-label">品种：</span>
                                <span class="content-value">{{ getCatBreed(currentRecord.catId) || '-' }}</span>
                            </el-col>
                            <el-col :span="8">
                                <span class="content-label">年龄：</span>
                                <span class="content-value">{{ getCatAge(currentRecord.catId) || '-' }}个月</span>
                            </el-col>
                            <el-col :span="8">
                                <span class="content-label">性别：</span>
                                <span class="content-value">{{ getGenderText(getCatGender(currentRecord.catId)) || '-'
                                    }}</span>
                            </el-col>
                            <el-col :span="8">
                                <span class="content-label">领养状态：</span>
                                <span class="content-value">{{
                                    getAdoptionStatusText(getCatAdoptionStatus(currentRecord.catId))
                                    ||
                                    '-'
                                }}</span>
                            </el-col>
                            <el-col :span="8">
                                <span class="content-label">性格：</span>
                                <span class="content-value">{{ getCatPersonality(currentRecord.catId) || '-' }}</span>
                            </el-col>
                            <el-col :span="8">
                                <span class="content-label">日常护理：</span>
                                <span class="content-value">{{ getCatDailyCare(currentRecord.catId) || '-' }}</span>
                            </el-col>
                        </el-row>
                    </div>
                </div>
                <!-- 健康记录详情 -->
                <div class="view-section">
                    <h4 class="section-title">健康记录详情</h4>
                    <div class="section-content">
                        <div class="detail-item">
                            <span class="content-label">症状描述：</span>
                            <div class="content-value">{{ currentRecord.symptoms || '无' }}</div>
                        </div>
                        <div class="detail-item">
                            <span class="content-label">处理措施：</span>
                            <div class="content-value">{{ currentRecord.treatment || '无' }}</div>
                        </div>
                        <div class="detail-item">
                            <span class="content-label">备注信息：</span>
                            <div class="content-value">{{ currentRecord.remark || '无' }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </el-dialog>
    </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
    Plus, Search, Eye, Edit, Delete,
    Check, Close, User, Cat, Calendar, Refresh
} from '@element-plus/icons-vue';

const router = useRouter();

// 状态管理
const loading = ref(false);
const defaultAvatar = 'https://picsum.photos/seed/cat/100/100';

// 猫咪列表
const catList = ref([]);

// 健康记录列表
const healthRecordList = ref([]);

// 分页配置
const pagination = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0
});

// 搜索筛选表单（参考活动管理页面结构）
const searchForm = reactive({
    symptomsKeyword: '',
    catId: '',
    healthStatus: '',
    checker: ''
});

// 弹窗控制
const recordDialogVisible = ref(false);
const viewDialogVisible = ref(false);
const batchDeleteDialogVisible = ref(false);

// 编辑模式标识
const isEditMode = ref(false);

// 选中的记录（批量删除用）
const selectedRecords = ref([]);

// 当前查看/编辑的记录
const currentRecord = ref({});

// 选中的猫咪信息（表单预览用）
const selectedCat = ref({});

// 健康记录表单
const recordFormRef = ref(null);
const recordForm = reactive({
    id: '',
    catId: '',
    checkDate: '',
    healthStatus: '',
    symptoms: '',
    treatment: '',
    checker: '',
    remark: ''
});

// 表单验证规则
const recordFormRules = reactive({
    catId: [
        { required: true, message: '请选择猫咪', trigger: 'blur' }
    ],
    checkDate: [
        { required: true, message: '请选择检查日期', trigger: 'blur' }
    ],
    healthStatus: [
        { required: true, message: '请选择健康状态', trigger: 'blur' }
    ],
    checker: [
        { required: true, message: '请输入检查人员姓名', trigger: 'blur' },
        { min: 2, max: 50, message: '检查人员姓名长度必须在2-50个字符之间', trigger: 'blur' }
    ],
    symptoms: [
        { max: 500, message: '症状描述不能超过500个字符', trigger: 'blur' }
    ],
    treatment: [
        { max: 500, message: '处理措施不能超过500个字符', trigger: 'blur' }
    ],
    remark: [
        { max: 300, message: '备注信息不能超过300个字符', trigger: 'blur' }
    ]
});

// 生命周期钩子
onMounted(() => {
    fetchCatList();
    fetchHealthRecordList();
});

/**
 * 获取猫咪列表
 */
const fetchCatList = async () => {
  loading.value = true;
  try {
    const res = await fetch('http://localhost:8081/catcate/cats/listAll', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (res.ok) {
      const result = await res.json();
      // 修改判断逻辑，适配后端直接返回List<Cats>的情况
      if (Array.isArray(result)) {
        // 后端直接返回数组的情况
        catList.value = result.map(cat => ({
          ...cat,
          photoUrl: getProcessedCatImageUrl(cat.photoUrl)
        }));
        console.log('获取到的猫咪列表:', catList.value);
      } else if (result.code === 200 && Array.isArray(result.data)) {
        // 后端返回包装对象的情况
        catList.value = result.data.map(cat => ({
          ...cat,
          photoUrl: getProcessedCatImageUrl(cat.photoUrl)
        }));
        console.log('获取到的猫咪列表:', catList.value);
      } else {
        ElMessage.error('获取猫咪列表失败: 数据格式不正确');
        catList.value = [];
      }
    } else {
      ElMessage.error('获取猫咪列表失败: HTTP ' + res.status);
      catList.value = [];
    }
  } catch (error) {
    ElMessage.error('获取猫咪列表异常: ' + error.message);
    console.error('获取猫咪列表异常:', error);
    catList.value = [];
  } finally {
    loading.value = false;
  }
};
// 添加辅助方法来根据catId获取猫咪信息
const getCatById = (catId) => {
    return catList.value.find(cat => cat.id === catId) || {};
};

const getCatName = (catId) => {
    const cat = getCatById(catId);
    return cat.name || '未知猫咪';
};

const getCatPhoto = (catId) => {
    const cat = getCatById(catId);
    return cat.photoUrl || '';
};

const getCatBreed = (catId) => {
    const cat = getCatById(catId);
    return cat.breed || '-';
};

const getCatAge = (catId) => {
    const cat = getCatById(catId);
    return cat.age || '-';
};

const getCatGender = (catId) => {
    const cat = getCatById(catId);
    return cat.gender || '';
};
const getCatAdoptionStatus = (catId) => {
    const cat = getCatById(catId);
    return cat.adoptionStatus || '';
};

const getCatPersonality = (catId) => {
    const cat = getCatById(catId);
    return cat.personality || '-';
};

const getCatDailyCare = (catId) => {
    const cat = getCatById(catId);
    return cat.dailyCare || '-';
};

/**
 * 获取健康记录列表（参考活动管理页面实现）
 */
const fetchHealthRecordList = async () => {
    loading.value = true;
    try {
        // 构建查询参数（与活动管理页面保持一致的传参方式）
        const params = {
            pageNum: pagination.currentPage,
            pageSize: pagination.pageSize,
            symptomsKeyword: searchForm.symptomsKeyword || '',
            catId: searchForm.catId || '',
            healthStatus: searchForm.healthStatus || '',
            checker: searchForm.checker || ''
        };

        const response = await fetch('http://localhost:8081/catcate/catHealthRecords/selectList', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(params)
        });

        if (response.ok) {
            const data = await response.json();
            healthRecordList.value = data.list || [];
            pagination.total = data.total || 0;
        } else {
            ElMessage.error('获取健康记录列表失败');
            console.error('获取健康记录列表失败:', response.statusText);
        }
    } catch (error) {
        ElMessage.error('获取健康记录列表异常: ' + error.message);
        console.error('获取健康记录列表异常:', error);
    } finally {
        loading.value = false;
    }
};

/**
 * 点击搜索按钮触发搜索（完全参考活动管理页面实现）
 */
const handleSearch = () => {
    pagination.currentPage = 1; // 搜索时重置页码
    fetchHealthRecordList();
    ElMessage.info('正在搜索符合条件的健康记录...');
};

/**
 * 重置搜索条件
 */
const handleReset = () => {
    // 清空所有搜索条件
    searchForm.symptomsKeyword = '';
    searchForm.catId = '';
    searchForm.healthStatus = '';
    searchForm.checker = '';

    // 重置分页并刷新列表
    pagination.currentPage = 1;
    fetchHealthRecordList();

    ElMessage.success('搜索条件已重置');
};

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
    pagination.pageSize = size;
    pagination.currentPage = 1;
    fetchHealthRecordList();
};

/**
 * 当前页变化
 */
const handleCurrentChange = (page) => {
    pagination.currentPage = page;
    fetchHealthRecordList();
};

/**
 * 选择记录（批量操作）
 */
const handleSelectionChange = (selection) => {
    selectedRecords.value = selection;
};

/**
 * 打开新增记录弹窗
 */
const openAddRecordDialog = () => {
    isEditMode.value = false;
    // 重置表单
    recordForm.id = '';
    recordForm.catId = '';
    recordForm.checkDate = '';
    recordForm.healthStatus = '';
    recordForm.symptoms = '';
    recordForm.treatment = '';
    recordForm.checker = '';
    recordForm.remark = '';

    // 重置选中猫咪信息
    selectedCat.value = {};

    // 打开弹窗并重置验证
    recordDialogVisible.value = true;
    nextTick(() => {
        recordFormRef.value?.clearValidate();
    });
};

/**
 * 打开编辑记录弹窗
 */
const openEditRecordDialog = (row) => {
    isEditMode.value = true;
    // 复制记录数据到表单
    Object.assign(recordForm, {
        id: row.id,
        catId: row.catId,
        checkDate: row.checkDate,
        healthStatus: row.healthStatus,
        symptoms: row.symptoms,
        treatment: row.treatment,
        checker: row.checker,
        remark: row.remark
    });

    // 获取猫咪详情
    const cat = catList.value.find(item => item.id === row.catId);
    if (cat) {
        selectedCat.value = {
            id: cat.id,
            name: cat.name,
            breed: cat.breed,
            age: cat.age,
            gender: cat.gender,
            // 注意这里字段名要与后端返回一致
            healthStatus: cat.healthStatus,  // 原来是 cat.health_status
            adoptionStatus: cat.adoptionStatus, // 原来是 cat.adoption_status
            photoUrl: cat.photoUrl,  // 原来是 cat.photo_url
            personality: cat.personality,
            dailyCare: cat.dailyCare  // 原来是 cat.daily_care
        };
    } else {
        selectedCat.value = {};
    }

    // 打开弹窗并重置验证
    recordDialogVisible.value = true;
    nextTick(() => {
        recordFormRef.value?.clearValidate();
    });
};

/**
 * 打开查看记录弹窗
 */
const openViewRecordDialog = (row) => {
    currentRecord.value = { ...row };
    viewDialogVisible.value = true;
};

// 添加图片URL处理函数
const getProcessedCatImageUrl = (url) => {
  if (!url) {
    return defaultAvatar;
  }
  
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http')) {
    return url;
  }
  
  // 处理相对路径
  if (url.startsWith('/')) {
    return `http://localhost:8081${url}`;
  } else if (url.startsWith('uploads/')) {
    return `http://localhost:8081/${url}`;
  } else {
    return `http://localhost:8081/${url}`;
  }
};

// 头像加载错误处理
const handleAvatarError = (event) => {
  console.log('猫咪头像加载失败:', event.target.src);
  event.target.src = defaultAvatar;
};

/**
 * 表单选择猫咪变化
 */
const handleCatSelect = (catId) => {
  if (!catId) {
    selectedCat.value = {};
    return;
  }

  const cat = catList.value.find(item => item.id === catId);
  if (cat) {
    // 确保字段名与实际数据一致，并处理图片URL
    selectedCat.value = {
      id: cat.id,
      name: cat.name,
      breed: cat.breed,
      age: cat.age,
      gender: cat.gender,
      healthStatus: cat.healthStatus,
      adoptionStatus: cat.adoptionStatus,
      photoUrl: getProcessedCatImageUrl(cat.photoUrl),
      personality: cat.personality,
      dailyCare: cat.dailyCare
    };
    console.log('选中的猫咪信息:', selectedCat.value);
  } else {
    selectedCat.value = {};
  }
};
/**
 * 提交健康记录表单
 */
const submitRecordForm = async () => {
    try {
        // 表单验证
        await recordFormRef.value.validate();

        loading.value = true;
        // 添加或删除
        const res = await fetch("http://localhost:8081/catcate/catHealthRecords/addOrUpdate", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(recordForm)
        });
        if (res.ok) {
            ElMessage.success('提交表单成功');
            // 刷新列表
            fetchHealthRecordList();
            // 关闭弹窗
            recordDialogVisible.value = false;
        } else {
            ElMessage.error('提交表单失败');
        }
    }catch (error) {
        ElMessage.error('提交表单异常: ' + error.message);
        console.error('提交表单异常:', error);
    }
}
/**
 * 更新猫咪健康状态
 */
// const updateCatHealthStatus = async (catId, healthStatus) => {
//     try {
//         const response = await fetch(`http://localhost:8081/catcate/cats/updateHealthStatus/${catId}`, {
//             method: 'POST',
//             headers: {
//                 'Content-Type': 'application/json'
//             },
//             body: JSON.stringify({ healthStatus })
//         });

//         if (response.ok) {
//             // 刷新猫咪列表
//             fetchCatList();
//         } else {
//             console.warn('更新猫咪健康状态失败:', response.statusText);
//         }
//     } catch (error) {
//         console.error('更新猫咪健康状态异常:', error);
//     }
// };

/**
 * 删除单条记录
 */
const handleDeleteRecord = (row) => {
    ElMessageBox.confirm(
        `确定要删除猫咪【${row.catName}】的健康记录（ID: ${row.id}）吗？删除后不可恢复。`,
        '确认删除',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'danger'
        }
    ).then(async () => {
        loading.value = true;
        try {
            const response = await fetch("http://localhost:8081/catcate/catHealthRecords/deleteById", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify( row)
            });

            if (response.ok) {
                ElMessage.success('健康记录删除成功');
                fetchHealthRecordList();
            } else {
                ElMessage.error('健康记录删除失败');
                console.error('健康记录删除失败:', response.statusText);
            }
        } catch (error) {
            ElMessage.error('健康记录删除异常: ' + error.message);
            console.error('健康记录删除异常:', error);
        } finally {
            loading.value = false;
        }
    }).catch(() => {
        // 取消删除
    });
};

/**
 * 批量删除（参考活动管理页面的批量操作风格）
 */
const handleBatchDelete = () => {
    if (selectedRecords.value.length === 0) {
        ElMessage.warning('请选择要删除的健康记录');
        return;
    }

    batchDeleteDialogVisible.value = true;
};

/**
 * 确认批量删除
 */
const confirmBatchDelete = async () => {
    if (selectedRecords.value.length === 0) {
        ElMessage.warning('请选择要删除的健康记录');
        return;
    }

    loading.value = true;
    try {
        const recordIds = selectedRecords.value.map(record => ({ id: record.id }));

        const response = await fetch('http://localhost:8081/catcate/catHealthRecords/deleteByIds', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(recordIds)
        });

        if (response.ok) {
            ElMessage.success(`成功删除 ${selectedRecords.value.length} 条健康记录`);
            batchDeleteDialogVisible.value = false;
            selectedRecords.value = [];
            fetchHealthRecordList();
        } else {
            ElMessage.error('批量删除健康记录失败');
            console.error('批量删除健康记录失败:', response.statusText);
        }
    } catch (error) {
        ElMessage.error('批量删除健康记录异常: ' + error.message);
        console.error('批量删除健康记录异常:', error);
    } finally {
        loading.value = false;
    }
};

/**
 * 禁用过去的日期（只能选择今天及以后）
 */
const disabledPastDate = (time) => {
    return time.getTime() < new Date().setHours(0, 0, 0, 0);
};

/**
 * 格式化日期时间
 */
const formatDateTime = (dateStr) => {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
};

/**
 * 获取健康状态文本
 */
const getStatusText = (status) => {
    const statusMap = {
        'HEALTHY': '健康',
        'SICK': '生病',
        'RECOVERING': '恢复中'
    };
    return statusMap[status] || '未知状态';
};

/**
 * 获取健康状态标签类型
 */
const getStatusTagType = (status) => {
    const typeMap = {
        'HEALTHY': 'success',
        'SICK': 'danger',
        'RECOVERING': 'warning'
    };
    return typeMap[status] || 'info';
};

/**
 * 获取性别文本
 */
const getGenderText = (gender) => {
    const genderMap = {
        'MALE': '公',
        'FEMALE': '母'
    };
    return genderMap[gender] || '未知';
};

/**
 * 获取领养状态文本
 */
const getAdoptionStatusText = (status) => {
    const statusMap = {
        'AVAILABLE': '可领养',
        'ADOPTED': '已被领养',
        'UNAVAILABLE': '不可领养'
    };
    return statusMap[status] || '未知';
};
</script>

<style scoped>
/* 猫咪预览卡片样式优化 */
.cat-preview-card {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.preview-avatar {
  margin-right: 15px;
  flex-shrink: 0;
}

.preview-details {
  flex: 1;
}

.preview-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 13px;
  color: #666;
}

.preview-meta span {
  display: flex;
  align-items: center;
}

.preview-meta span::before {
  content: "•";
  margin-right: 5px;
  color: #999;
}

.preview-status-tag {
  margin-left: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .cat-preview-card {
    flex-direction: column;
    text-align: center;
  }
  
  .preview-avatar {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .preview-meta {
    justify-content: center;
  }
}

.cat-health-management {
    padding: 20px;
    background-color: #f5f7fa;
    min-height: calc(100vh - 60px);
}

/* 页面标题区域 */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1f2329;
    margin: 0;
}

.add-btn {
    background-color: #ff9f43;
    border-color: #ff9f43;
}

.add-btn:hover {
    background-color: #ff8c21;
    border-color: #ff8c21;
}

/* 筛选卡片（完全参考活动管理页面样式） */
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

/* 搜索按钮样式（与活动管理页面保持一致） */
.search-btn {
    background-color: #ff9f43;
    border-color: #ff9f43;
}

.search-btn:hover {
    background-color: #ff8c21;
    border-color: #ff8c21;
}

/* 重置按钮样式（与活动管理页面保持一致） */
.reset-btn {
    background-color: #f0f0f0;
    color: #666;
    border-color: #e0e0e0;
}

.reset-btn:hover {
    background-color: #e0e0e0;
    color: #333;
    border-color: #d0d0d0;
}

/* 记录列表卡片 */
.record-list-card {
    background-color: #fff;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    border-radius: 8px;
    overflow: hidden;
}

/* 猫咪信息样式 */
.cat-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 4px 0;
}

.cat-avatar {
    border: 2px solid #fff7ee;
}

.cat-details {
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.cat-name {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;
}

.cat-meta {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
}

.status-tag {
    font-size: 12px;
    padding: 2px 6px;
}

/* 文本溢出处理 */
.text-ellipsis {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
    color: #666;
    line-height: 1.5;
}

/* 分页容器（与活动管理页面保持一致） */
.pagination-container {
    margin-top: 15px;
    text-align: right;
    padding: 16px;
    border-top: 1px solid #f0f0f0;
    background-color: #fff;
}

/* 表单样式 */
.record-form {
    margin-top: 10px;
}

.cat-preview-item {
    margin-bottom: 20px;
}

.cat-preview-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background-color: #fff7ee;
    border-radius: 8px;
}

.preview-avatar {
    border: 2px solid #fff;
}

.preview-details {
    flex: 1;
}

.preview-name {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}

.preview-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    font-size: 13px;
    color: #666;
}

.preview-status-tag {
    font-size: 12px;
}

/* 查看记录弹窗样式 */
.view-record-container {
    padding: 8px 0;
}

.view-header {
    display: flex;
    align-items: center;
    gap: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 20px;
}

.view-avatar {
    border: 3px solid #fff7ee;
}

.view-header-info {
    flex: 1;
}

.view-cat-name {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
    display: flex;
    align-items: center;
    gap: 8px;
}

.view-status-tag {
    font-size: 12px;
}

.view-header-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    font-size: 13px;
    color: #666;
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
}

.view-section {
    margin-bottom: 20px;
}

.section-title {
    font-size: 15px;
    font-weight: 500;
    color: #333;
    margin: 0 0 12px 0;
    padding-left: 8px;
    border-left: 3px solid #ff9f43;
}

.section-content {
    background-color: #f9f9f9;
    border-radius: 8px;
    padding: 16px;
    color: #666;
    line-height: 1.8;
}

.detail-item {
    margin-bottom: 12px;
}

.detail-item:last-child {
    margin-bottom: 0;
}

.content-label {
    display: inline-block;
    width: 80px;
    font-weight: 500;
    color: #333;
    vertical-align: top;
}

.content-value {
    display: inline-block;
    width: calc(100% - 80px);
}

/* 覆盖Element Plus样式，与活动管理页面保持一致 */
:v-deep .el-card {
    border-radius: 8px;
}

::-deep .el-button--primary {
    background-color: #ff9f43;
    border-color: #ff9f43;
}

:v-deep .el-button--primary:hover {
    background-color: #ff8c21;
    border-color: #ff8c21;
}

:v-deep .el-button--success {
    background-color: #00d2d3;
    border-color: #00d2d3;
}

:v-deep .el-button--success:hover {
    background-color: #00b8b9;
    border-color: #00b8b9;
}

:v-deep .el-button--danger {
    background-color: #ff6b6b;
    border-color: #ff6b6b;
}

:v-deep .el-button--danger:hover {
    background-color: #ff5252;
    border-color: #ff5252;
}

:v-deep .el-button--info {
    background-color: #409eff;
    border-color: #409eff;
}

:v-deep .el-button--info:hover {
    background-color: #2691f6;
    border-color: #2691f6;
}

::v-deep .el-tag--success {
    background-color: #00d2d3;
}

:v-deep .el-tag--danger {
    background-color: #ff6b6b;
}

:v-deep .el-tag--warning {
    background-color: #ffa801;
}

:v-deep .el-table th {
    background-color: #fff7ee;
}

:v-deep .el-input__inner,
:v-deep .el-select-dropdown,
:v-deep .el-date-editor .el-input__inner,
:v-deep .el-textarea__inner {
    border-radius: 4px;
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
    .el-table-column:not(.el-table-column--selection):not([label="记录ID"]):not([label="猫咪信息"]):not([label="检查日期"]):not([label="健康状态"]):not([label="操作"]) {
        display: none !important;
    }

    .preview-meta {
        flex-direction: column;
        gap: 5px;
    }

    .view-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }

    .view-header-meta {
        flex-direction: column;
        gap: 5px;
    }

    .content-label,
    .content-value {
        display: block;
        width: 100%;
    }

    .content-label {
        margin-bottom: 4px;
    }
}
</style>