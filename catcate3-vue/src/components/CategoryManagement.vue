<!-- 分类管理 -->
<template>
    <div class="category-management">
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h1>商品分类管理</h1>
            <div class="header-actions">
                <el-button type="primary" icon="Plus" @click="handleAddCategory">
                    新增分类
                </el-button>
                <el-button type="success" icon="Upload" @click="handleImport">
                    导入
                </el-button>
                <el-button icon="Download" @click="handleExport">
                    导出
                </el-button>
            </div>
        </div>

        <!-- 统计信息 -->
        <el-card class="stats-card">
            <el-row :gutter="20">
                <el-col :span="6">
                    <div class="stat-item">
                        <div class="stat-number">{{ stats.total }}</div>
                        <div class="stat-label">总分类数</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-item">
                        <div class="stat-number">{{ stats.active }}</div>
                        <div class="stat-label">启用分类</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-item">
                        <div class="stat-number">{{ stats.disabled }}</div>
                        <div class="stat-label">禁用分类</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-item">
                        <div class="stat-number">{{ stats.parents }}</div>
                        <div class="stat-label">一级分类</div>
                    </div>
                </el-col>
            </el-row>
        </el-card>

        <!-- 搜索和筛选区 -->
        <el-card class="filter-card">
            <el-row :gutter="20">
                <el-col :span="8">
                    <el-input v-model="searchForm.name" placeholder="搜索分类名称" clearable prefix-icon="Search" @input="handleSearchDebounced"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-input v-model="searchForm.code" placeholder="搜索分类代码" clearable prefix-icon="Key" @input="handleSearchDebounced"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.isActive" placeholder="选择状态" clearable @change="handleSearchDebounced">
                        <el-option label="启用" value="1"></el-option>
                        <el-option label="禁用" value="0"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="4">
                    <el-select v-model="searchForm.level" placeholder="分类级别" clearable @change="handleSearchDebounced">
                        <el-option label="一级分类" value="1"></el-option>
                        <el-option label="二级分类" value="2"></el-option>
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
                <el-button type="warning" icon="Refresh" @click="fetchCategoryList">
                    刷新
                </el-button>
                <el-button type="danger" icon="Delete" @click="handleBatchDelete"
                    :disabled="selectedCategories.length === 0">
                    批量删除
                </el-button>
                <el-button type="info" icon="Switch" @click="handleBatchStatusChange"
                    :disabled="selectedCategories.length === 0">
                    {{ batchStatusText }}
                </el-button>
            </div>
        </el-card>

        <!-- 分类列表（树形结构） -->
        <el-card class="table-card">
            <div class="table-header">
                <span>分类列表</span>
                <div class="header-actions">
                    <el-checkbox v-model="expandAll" @change="handleExpandAll">
                        {{ expandAll ? '收起全部' : '展开全部' }}
                    </el-checkbox>
                    <el-checkbox v-model="enableDrag" @change="handleDragToggle">
                        {{ enableDrag ? '关闭拖拽' : '开启拖拽排序' }}
                    </el-checkbox>
                </div>
            </div>
            <el-table :data="categoryList" border stripe :loading="loading" @selection-change="handleSelectionChange"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" :row-class-name="tableRowClassName" ref="categoryTable"
                height="500" :virtual-scroll="true" :virtual-item-size="54"
                :row-key="row => row.id"
                @row-drag-start="handleRowDragStart"
                @row-drag-end="handleRowDragEnd"
                :row-draggable="(row) => enableDrag && (!row.children || row.children.length === 0)">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="id" label="分类ID" width="80" align="center" sortable></el-table-column>
                <el-table-column label="分类图标" width="80" align="center">
                    <template #default="scope">
                        <el-avatar :size="32" :src="scope.row.icon || defaultCategoryIcon">
                            <el-icon :size="16">
                                <Box />
                            </el-icon>
                        </el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="name" label="分类名称" min-width="180" sortable>
                    <template #default="scope">
                        <span :style="{ 'padding-left': scope.row.level * 20 + 'px' }">
                            <el-icon v-if="scope.row.hasChildren" class="tree-icon">
                                <FolderOpened />
                            </el-icon>
                            <el-icon v-else class="tree-icon">
                                <Box />
                            </el-icon>
                            {{ scope.row.name }}
                            <el-tag v-if="!scope.row.parentId" size="mini" type="primary" effect="plain" class="level-tag">
                                一级分类
                            </el-tag>
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="code" label="分类代码" min-width="120" align="center" sortable>
                    <template #default="scope">
                        <el-tag :type="getCategoryTagType(scope.row.code)" size="mini" class="code-tag">
                            {{ scope.row.code }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sortOrder" label="排序" min-width="100" align="center" sortable>
                    <template #default="scope">
                        <el-input-number v-model="scope.row.sortOrder" :min="0" :max="100" :step="1"
                            @change="(val) => handleSortChange(scope.row, val)" size="small"
                            :disabled="scope.row.isActive === 0">
                        </el-input-number>
                    </template>
                </el-table-column>
                <el-table-column prop="isActive" label="状态" min-width="100" align="center" sortable>
                    <template #default="scope">
                        <el-switch v-model="scope.row.isActive" active-color="#00d2d3" inactive-color="#ccc"
                            :active-value="1" :inactive-value="0" @change="(val) => handleStatusChange(scope.row, val)">
                        </el-switch>
                        <div class="status-text">{{ scope.row.isActive === 1 ? '启用' : '禁用' }}</div>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="180" align="center" sortable></el-table-column>
                <el-table-column label="操作" min-width="260" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" icon="Plus" @click="handleAddSubCategory(scope.row)" class="action-btn">
                            新增子分类
                        </el-button>
                        <el-button size="small" type="info" icon="View" @click="handlePreviewCategory(scope.row)" class="action-btn">
                            预览
                        </el-button>
                        <el-button size="small" type="info" icon="Edit" @click="handleEditCategory(scope.row)" class="action-btn">
                            编辑
                        </el-button>
                        <el-button size="small" type="warning" icon="Copy" @click="handleCopyCategory(scope.row)" class="action-btn">
                            复制
                        </el-button>
                        <el-button size="small" type="danger" icon="Delete" @click="handleDeleteCategory(scope.row)" class="action-btn"
                            :disabled="scope.row.children && scope.row.children.length > 0">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
                <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
                    :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange">
                </el-pagination>
            </div>
        </el-card>

        <!-- 新增/编辑分类弹窗 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false"
            append-to-body>
            <el-form ref="categoryFormRef" :model="categoryForm" :rules="formRules" label-width="120px"
                class="category-form">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="分类名称" prop="name">
                            <el-input v-model="categoryForm.name" placeholder="请输入分类名称" maxlength="50"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="分类代码" prop="code">
                            <el-input v-model="categoryForm.code" placeholder="请输入分类代码（英文/大写）"
                                maxlength="50" @input="handleCodeInput"></el-input>
                            <div class="form-hint">代码用于系统识别，如：FOOD、CAT_FOOD，不可重复</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="父分类" prop="parentId">
                            <el-cascader
                                v-model="categoryPath"
                                :options="categoryTree"
                                :props="cascaderProps"
                                placeholder="选择父分类（可选）"
                                filterable
                                clearable
                                @change="handleCategoryChange"
                            ></el-cascader>
                            <div class="form-hint">不选择则为一级分类</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="排序序号" prop="sortOrder">
                            <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="100" :step="1"
                                placeholder="输入排序序号"></el-input-number>
                            <div class="form-hint">数值越小，排序越靠前</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="分类图标">
                            <el-upload class="avatar-uploader" :action="uploadUrl" :show-file-list="false"
                                :on-success="handleIconUploadSuccess" :before-upload="beforeIconUpload">
                                <img v-if="categoryForm.icon" :src="categoryForm.icon" class="avatar" />
                                <el-icon v-else class="avatar-uploader-icon">
                                    <Plus />
                                </el-icon>
                            </el-upload>
                            <div class="form-hint">建议尺寸：80x80px，支持jpg/png格式</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="是否启用" prop="isActive">
                            <el-switch v-model="categoryForm.isActive" active-color="#00d2d3" inactive-color="#ccc"
                                active-value="1" inactive-value="0"></el-switch>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="分类描述" prop="description">
                            <el-input v-model="categoryForm.description" type="textarea" :rows="4"
                                placeholder="请输入分类描述（可选）" maxlength="200" show-word-limit></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSaveCategory" :loading="dialogLoading">
                    保存
                </el-button>
            </template>
        </el-dialog>

        <!-- 批量删除确认弹窗 -->
        <el-dialog v-model="batchConfirmVisible" title="批量删除确认" width="400px" :close-on-click-modal="false">
            <p>确定要删除选中的 {{ selectedCategories.length }} 个分类吗？</p>
            <p class="warning-text">注意：包含子分类的分类无法删除，请先删除子分类</p>
            <template #footer>
                <el-button @click="batchConfirmVisible = false">取消</el-button>
                <el-button type="danger" @click="confirmBatchDelete" :loading="batchLoading">
                    确认删除
                </el-button>
            </template>
        </el-dialog>

        <!-- 批量状态变更确认弹窗 -->
        <el-dialog v-model="batchStatusVisible" :title="batchStatusTitle" width="400px" :close-on-click-modal="false">
            <p>确定要{{ batchStatusAction }}选中的 {{ selectedCategories.length }} 个分类吗？</p>
            <template #footer>
                <el-button @click="batchStatusVisible = false">取消</el-button>
                <el-button :type="batchStatusType" @click="confirmBatchStatusChange" :loading="batchLoading">
                    确认操作
                </el-button>
            </template>
        </el-dialog>

        <!-- 批量操作进度条 -->
        <el-dialog v-model="batchProgressVisible" title="批量操作进度" width="500px" :close-on-click-modal="false" :show-close="false">
            <div class="batch-progress-container">
                <p>{{ batchProgressText }}</p>
                <el-progress :percentage="batchProgress" :status="batchProgress === 100 ? 'success' : ''" />
            </div>
            <template #footer>
                <el-button v-if="batchProgress === 100" @click="batchProgressVisible = false">
                    完成
                </el-button>
            </template>
        </el-dialog>

        <!-- 分类预览弹窗 -->
        <el-dialog v-model="previewVisible" title="分类详情预览" width="600px" :close-on-click-modal="false">
            <div class="preview-container" v-if="previewCategory">
                <el-card shadow="never" class="preview-card">
                    <div class="preview-header">
                        <el-avatar :size="64" :src="previewCategory.icon || defaultCategoryIcon">
                            <el-icon :size="32">
                                <Box />
                            </el-icon>
                        </el-avatar>
                        <div class="preview-info">
                            <h3>{{ previewCategory.name }}</h3>
                            <el-tag :type="getCategoryTagType(previewCategory.code)" size="medium" class="preview-code-tag">
                                {{ previewCategory.code }}
                            </el-tag>
                        </div>
                    </div>
                    <el-divider />
                    <div class="preview-details">
                        <el-descriptions :column="2" border>
                            <el-descriptions-item label="分类ID">{{ previewCategory.id }}</el-descriptions-item>
                            <el-descriptions-item label="分类级别">{{ previewCategory.level === 0 ? '一级分类' : '二级分类' }}</el-descriptions-item>
                            <el-descriptions-item label="父分类">{{ getParentCategoryName(categoryList, previewCategory.parentId) }}</el-descriptions-item>
                            <el-descriptions-item label="排序序号">{{ previewCategory.sortOrder }}</el-descriptions-item>
                            <el-descriptions-item label="状态">{{ previewCategory.isActive === 1 ? '启用' : '禁用' }}</el-descriptions-item>
                            <el-descriptions-item label="创建时间">{{ previewCategory.createTime || '未知' }}</el-descriptions-item>
                            <el-descriptions-item label="分类描述" :span="2">{{ previewCategory.description || '无描述' }}</el-descriptions-item>
                        </el-descriptions>
                    </div>
                    <el-divider />
                    <div class="preview-stats" v-if="previewCategory.children && previewCategory.children.length > 0">
                        <h4>子分类列表 ({{ previewCategory.children.length }})</h4>
                        <el-list :data="previewCategory.children" border>
                            <el-list-item v-for="child in previewCategory.children" :key="child.id">
                                <div class="child-category">
                                    <span class="child-name">{{ child.name }}</span>
                                    <el-tag size="small" :type="getCategoryTagType(child.code)">
                                        {{ child.code }}
                                    </el-tag>
                                    <span class="child-status">{{ child.isActive === 1 ? '启用' : '禁用' }}</span>
                                </div>
                            </el-list-item>
                        </el-list>
                    </div>
                </el-card>
            </div>
            <template #footer>
                <el-button @click="previewVisible = false">
                    关闭
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
    Plus, Search, Key, Edit, Delete, Box,
    Upload, Picture, FolderOpened, Refresh,
    Copy, Download, Switch, View
} from '@element-plus/icons-vue';
import {
    exportToCSV,
    exportToExcel,
    addLevelToCategories,
    updateStats,
    getParentCategoryName,
    getCategoryTagType,
    debounce
} from '../utils/categoryUtils';

// 状态管理
const loading = ref(false);
const dialogLoading = ref(false);
const batchLoading = ref(false);
const categoryList = ref([]);
const selectedCategories = ref([]);
const categoryFormRef = ref(null);
const categoryTable = ref(null);
const defaultCategoryIcon = 'https://picsum.photos/seed/category/80/80';
const uploadUrl = 'http://localhost:8081/catcate/upload/icon';
const expandAll = ref(false);
const enableDrag = ref(false);
const draggedRow = ref(null);
const batchProgress = ref(0);
const batchProgressVisible = ref(false);
const batchProgressText = ref('');
const previewVisible = ref(false);
const previewCategory = ref(null);

// 分类树相关
const categoryTree = ref([]);
const categoryPath = ref([]);
const cascaderProps = {
    value: 'id',
    label: 'name',
    children: 'children',
    checkStrictly: true,
    emitPath: true,
    expandTrigger: 'click'
};

// 搜索表单
const searchForm = reactive({
    name: '',
    code: '',
    isActive: '',
    level: ''
});

// 分页配置
const pagination = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0
});

// 弹窗相关
const dialogVisible = ref(false);
const dialogType = ref(''); // add, addSub, edit, copy
const dialogTitle = ref('');
const batchConfirmVisible = ref(false);
const batchStatusVisible = ref(false);
const batchStatusAction = ref('');
const batchStatusTitle = ref('');
const batchStatusType = ref('');
const batchStatusValue = ref(0);

// 父分类选项（缓存）
const parentCategoryOptions = ref([]);
const parentOptionsLoaded = ref(false);

// 统计信息
const stats = reactive({
    total: 0,
    active: 0,
    disabled: 0,
    parents: 0
});

// 表单数据和验证规则
const categoryForm = reactive({
    id: '',
    name: '',
    code: '',
    parentId: '',
    sortOrder: 0,
    icon: '',
    isActive: 1,
    description: ''
});

/**
 * 检查分类是否被禁用（用于父分类选择）
 */
const isCategoryDisabled = (categoryId) => {
    // 编辑模式下，不能选择自己作为父分类
    if (dialogType.value === 'edit' && categoryForm.id === categoryId) {
        return true;
    }
    
    // 不能选择自己的子分类作为父分类（避免循环依赖）
    if (dialogType.value === 'edit') {
        const isChildCategory = (id, parentId) => {
            if (parentId === categoryForm.id) {
                return true;
            }
            const parentCategory = parentCategoryOptions.value.find(cat => cat.id === parentId);
            return parentCategory && isChildCategory(id, parentCategory.parentId);
        };
        
        const category = parentCategoryOptions.value.find(cat => cat.id === categoryId);
        return category && isChildCategory(categoryId, category.parentId);
    }
    
    return false;
};

/**
 * 检查分类代码唯一性
 */
const checkCodeUnique = async (rule, value, callback) => {
    if (!value) {
        callback();
        return;
    }

    // 编辑模式下，如果代码未修改，直接通过
    if (dialogType.value === 'edit' && categoryForm.code.toUpperCase() === value.toUpperCase()) {
        callback();
        return;
    }

    try {
        const res = await fetch(`http://localhost:8081/catcate/productCategories/checkCodeUnique?code=${value.toUpperCase()}${dialogType.value === 'edit' ? `&id=${categoryForm.id}` : ''}`, {
            method: "GET"
        });

        if (res.ok) {
            const data = await res.json();
            if (data.isUnique) {
                callback();
            } else {
                callback(new Error('分类代码已存在'));
            }
        } else {
            // 后端服务不可用时，暂时允许通过
            console.warn('后端服务不可用，暂时允许分类代码通过验证');
            callback();
        }
    } catch (error) {
        // 网络错误时，暂时允许通过
        console.warn('网络错误，暂时允许分类代码通过验证:', error);
        callback();
    }
};


const formRules = reactive({
    name: [
        { required: true, message: '请输入分类名称', trigger: 'blur' },
        { min: 2, max: 50, message: '分类名称长度必须在2-50个字符之间', trigger: 'blur' }
    ],
    code: [
        { required: true, message: '请输入分类代码', trigger: 'blur' },
        { pattern: /^[A-Za-z0-9_]+$/, message: '分类代码只能包含字母、数字和下划线', trigger: 'blur' },
        { min: 2, max: 50, message: '分类代码长度必须在2-50个字符之间', trigger: 'blur' },
        { validator: checkCodeUnique, message: '', trigger: 'blur' }
    ],
    sortOrder: [
        { required: true, message: '请输入排序序号', trigger: 'blur' },
        { type: 'number', min: 0, max: 100, message: '排序序号必须在0-100之间', trigger: 'blur' }
    ],
    description: [
        { max: 200, message: '分类描述不能超过200个字符', trigger: 'blur' }
    ]
});

// 防抖搜索
const handleSearchDebounced = debounce(() => {
    pagination.currentPage = 1;
    fetchCategoryList();
}, 500);

// 生命周期
onMounted(() => {
    fetchCategoryList();
});

// 构建分类树
const buildCategoryTree = (categories) => {
    const map = {};
    const tree = [];

    // 首先创建所有分类的映射
    categories.forEach(category => {
        map[category.id] = {
            ...category,
            children: []
        };
    });

    // 然后构建树结构
    categories.forEach(category => {
        if (!category.parentId) {
            // 根分类
            tree.push(map[category.id]);
        } else {
            // 子分类
            if (map[category.parentId]) {
                map[category.parentId].children.push(map[category.id]);
            }
        }
    });

    return tree;
};

// 处理分类选择变化
const handleCategoryChange = (value) => {
    if (value && value.length > 0) {
        // 只使用最后一级分类的ID
        categoryForm.parentId = value[value.length - 1];
    } else {
        categoryForm.parentId = null;
    }
};

// 查找分类路径
const findCategoryPath = (categoryId) => {
    const path = [];
    const findPath = (nodes) => {
        for (const node of nodes) {
            if (node.id === categoryId) {
                path.unshift(node.id);
                return true;
            }
            if (node.children && node.children.length > 0) {
                if (findPath(node.children)) {
                    path.unshift(node.id);
                    return true;
                }
            }
        }
        return false;
    };
    findPath(categoryTree.value);
    return path;
};

/**
 * 获取分类列表
 */
const fetchCategoryList = async () => {
    loading.value = true;
    try {
        const res = await fetch("http://localhost:8081/catcate/productCategories/selectList", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                name: searchForm.name,
                code: searchForm.code,
                isActive: searchForm.isActive,
                pageNum: pagination.currentPage,
                pageSize: pagination.pageSize
            })
        });

        if (res.ok) {
            const data = await res.json();
            console.log('分类列表数据:', data);
            if (data && Array.isArray(data.list)) {
                // 处理树形结构，添加层级标识
                const processedData = addLevelToCategories(data.list);
                categoryList.value = processedData;
                pagination.total = data.total || 0;
                
                // 构建分类树
                categoryTree.value = buildCategoryTree(data.list);
                
                // 更新统计信息
                const newStats = updateStats(data.list);
                Object.assign(stats, newStats);
            } else {
                ElMessage.error('数据格式不正确');
            }
        } else {
            ElMessage.error('获取分类列表失败: ' + res.status);
        }
    } catch (error) {
        console.error('获取分类列表失败:', error);
        ElMessage.error('获取分类列表失败: ' + error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * 获取父分类选项（一级分类）
 */
const fetchParentCategoryOptions = async () => {
    if (parentOptionsLoaded.value) return;
    
    try {
        const res = await fetch("http://localhost:8081/catcate/productCategories/selectParentOptions", {
            method: "GET"
        });

        if (res.ok) {
            const data = await res.json();
            parentCategoryOptions.value = data || [];
            parentOptionsLoaded.value = true;
        }
    } catch (error) {
        console.error('获取父分类选项失败:', error);
    }
};

/**
 * 搜索功能
 */
const handleSearch = () => {
    pagination.currentPage = 1;
    fetchCategoryList();
    ElMessage.info('正在搜索符合条件的分类...');
};

/**
 * 重置功能
 */
const handleReset = () => {
    searchForm.name = '';
    searchForm.code = '';
    searchForm.isActive = '';
    searchForm.level = '';
    pagination.currentPage = 1;
    fetchCategoryList();
    ElMessage.success('搜索条件已重置');
};

/**
 * 分页处理
 */
const handleSizeChange = (size) => {
    pagination.pageSize = size;
    pagination.currentPage = 1;
    fetchCategoryList();
};

const handleCurrentChange = (page) => {
    pagination.currentPage = page;
    fetchCategoryList();
};

/**
 * 选择变化（批量操作）
 */
const handleSelectionChange = (selection) => {
    selectedCategories.value = selection;
};

/**
 * 表格行样式（根据状态区分）
 */
const tableRowClassName = ({ row }) => {
    const classes = [];
    if (row.isActive === 0) {
        classes.push('row-disabled');
    }
    return classes.join(' ');
};

/**
 * 展开/收起全部
 */
const handleExpandAll = (value) => {
    if (categoryTable.value) {
        if (value) {
            categoryTable.value.expandAll();
        } else {
            categoryTable.value.collapseAll();
        }
    }
};



/**
 * 排序变更
 */
const handleSortChange = async (row, val) => {
    if (val === row.sortOrder) return;

    loading.value = true;
    try {
        const res = await fetch("http://localhost:8081/catcate/productCategories/updateSort", {
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
            row.sortOrder = val;
        } else {
            ElMessage.error('排序更新失败');
        }
    } catch (error) {
        console.error('排序更新失败:', error);
        ElMessage.error('排序更新失败: ' + error.message);
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
        `确定要${statusText}分类【${row.name}】吗？`,
        '确认操作',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: val === 1 ? 'success' : 'warning'
        }
    ).then(async () => {
        loading.value = true;
        try {
            const res = await fetch("http://localhost:8081/catcate/productCategories/updateStatus", {
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
                ElMessage.success(`分类${statusText}成功`);
                row.isActive = val;
                // 更新统计信息
                fetchCategoryList();
            } else {
                ElMessage.error(`分类${statusText}失败`);
                row.isActive = row.isActive === 1 ? 0 : 1;
            }
        } catch (error) {
            console.error(`分类${statusText}失败:', error`);
            ElMessage.error(`分类${statusText}失败: ' + error.message`);
            row.isActive = row.isActive === 1 ? 0 : 1;
        } finally {
            loading.value = false;
        }
    }).catch(() => {
        row.isActive = row.isActive === 1 ? 0 : 1;
    });
};

/**
 * 批量状态变更
 */
const handleBatchStatusChange = () => {
    if (selectedCategories.value.length === 0) {
        ElMessage.warning('请选择要操作的分类');
        return;
    }

    // 检查是否所有选中分类都处于同一状态
    const firstStatus = selectedCategories.value[0].isActive;
    const allSameStatus = selectedCategories.value.every(cat => cat.isActive === firstStatus);
    
    if (!allSameStatus) {
        ElMessage.warning('请选择状态相同的分类');
        return;
    }

    const newStatus = firstStatus === 1 ? 0 : 1;
    batchStatusValue.value = newStatus;
    batchStatusAction.value = newStatus === 1 ? '启用' : '禁用';
    batchStatusTitle.value = `批量${newStatus === 1 ? '启用' : '禁用'}确认`;
    batchStatusType.value = newStatus === 1 ? 'success' : 'warning';
    batchStatusVisible.value = true;
};

/**
 * 确认批量状态变更
 */
const confirmBatchStatusChange = async () => {
    batchLoading.value = true;
    batchProgressVisible.value = true;
    batchProgress.value = 0;
    batchProgressText.value = `正在${batchStatusAction.value}分类...`;
    
    try {
        const categoryIds = selectedCategories.value.map(cat => cat.id);
        const total = categoryIds.length;
        let successCount = 0;
        
        // 逐个更新状态
        for (let i = 0; i < total; i++) {
            const id = categoryIds[i];
            const res = await fetch("http://localhost:8081/catcate/productCategories/updateStatus", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    id: id,
                    isActive: batchStatusValue.value
                })
            });
            
            if (res.ok) {
                successCount++;
            }
            
            // 更新进度
            batchProgress.value = Math.round(((i + 1) / total) * 100);
            batchProgressText.value = `正在${batchStatusAction.value}分类... (${i + 1}/${total})`;
            
            // 短暂延迟，让用户看到进度
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        batchProgressText.value = `批量${batchStatusAction.value}完成`;
        ElMessage.success(`成功${batchStatusAction.value} ${successCount} 个分类`);
        
        // 延迟关闭进度弹窗
        setTimeout(() => {
            batchProgressVisible.value = false;
            batchStatusVisible.value = false;
            selectedCategories.value = [];
            fetchCategoryList();
        }, 1000);
    } catch (error) {
        ElMessage.error('批量操作失败: ' + error.message);
        batchProgressVisible.value = false;
    } finally {
        batchLoading.value = false;
    }
};

/**
 * 新增分类
 */
const handleAddCategory = () => {
    dialogType.value = 'add';
    dialogTitle.value = '新增商品分类';
    resetCategoryForm();
    dialogVisible.value = true;
    nextTick(() => {
        categoryFormRef.value?.clearValidate();
    });
};

/**
 * 新增子分类
 */
const handleAddSubCategory = (parentRow) => {
    dialogType.value = 'addSub';
    dialogTitle.value = `新增【${parentRow.name}】的子分类`;
    resetCategoryForm();
    categoryForm.parentId = parentRow.id;
    dialogVisible.value = true;
    nextTick(() => {
        categoryFormRef.value?.clearValidate();
    });
};

/**
 * 编辑分类
 */
const handleEditCategory = (row) => {
    dialogType.value = 'edit';
    dialogTitle.value = '编辑商品分类';
    Object.assign(categoryForm, {
        id: row.id,
        name: row.name,
        code: row.code,
        parentId: row.parentId || '',
        sortOrder: row.sortOrder,
        icon: row.icon || '',
        isActive: row.isActive,
        description: row.description || ''
    });
    dialogVisible.value = true;
    nextTick(() => {
        categoryFormRef.value?.clearValidate();
    });
};

/**
 * 复制分类
 */
const handleCopyCategory = (row) => {
    dialogType.value = 'copy';
    dialogTitle.value = `复制【${row.name}】分类`;
    Object.assign(categoryForm, {
        id: '',
        name: `${row.name} (复制)`,
        code: `${row.code}_COPY`,
        parentId: row.parentId || '',
        sortOrder: row.sortOrder,
        icon: row.icon || '',
        isActive: row.isActive,
        description: row.description || ''
    });
    dialogVisible.value = true;
    nextTick(() => {
        categoryFormRef.value?.clearValidate();
    });
};

/**
 * 重置分类表单
 */
const resetCategoryForm = () => {
    Object.assign(categoryForm, {
        id: '',
        name: '',
        code: '',
        parentId: '',
        sortOrder: 0,
        icon: '',
        isActive: 1,
        description: ''
    });
};

/**
 * 代码输入处理（自动大写）
 */
const handleCodeInput = (value) => {
    categoryForm.code = value.toUpperCase();
};

/**
 * 图标上传成功处理
 */
const handleIconUploadSuccess = (response) => {
    if (response.code === 200) {
        categoryForm.icon = response.data.url;
        ElMessage.success('图标上传成功');
    } else {
        ElMessage.error('图标上传失败');
    }
};

/**
 * 图标上传前验证
 */
const beforeIconUpload = (file) => {
    const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
    const isLt2M = file.size / 1024 / 1024 < 2;

    if (!isImage) {
        ElMessage.error('只能上传JPG/PNG格式的图片!');
        return false;
    }
    if (!isLt2M) {
        ElMessage.error('图片大小不能超过2MB!');
        return false;
    }
    return true;
};

/**
 * 保存分类信息
 */
const handleSaveCategory = async () => {
    try {
        await categoryFormRef.value.validate();
        dialogLoading.value = true;

        const submitForm = { ...categoryForm };

        const res = await fetch("http://localhost:8081/catcate/productCategories/addOrUpdate", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(submitForm)
        });

        if (res.ok) {
            ElMessage.success(dialogType.value.includes('add') || dialogType.value === 'copy' ? "分类新增成功" : "分类更新成功");
            dialogVisible.value = false;
            fetchCategoryList();
            fetchParentCategoryOptions(); // 更新父分类选项
        } else {
            ElMessage.error(dialogType.value.includes('add') || dialogType.value === 'copy' ? "分类新增失败" : "分类更新失败");
        }
    } catch (error) {
        return;
    } finally {
        dialogLoading.value = false;
    }
};

/**
 * 删除单个分类
 */
const handleDeleteCategory = (row) => {
    if (row.children && row.children.length > 0) {
        ElMessage.warning('该分类包含子分类，无法删除');
        return;
    }

    ElMessageBox.confirm(
        `确定要删除分类【${row.name}】吗？`,
        '确认删除',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'danger'
        }
    ).then(async () => {
        try {
            loading.value = true;
            const res = await fetch("http://localhost:8081/catcate/productCategories/deleteById", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    id: row.id
                })
            });

            if (res.ok) {
                ElMessage.success("分类删除成功");
                fetchCategoryList();
                fetchParentCategoryOptions(); // 更新父分类选项
            } else {
                ElMessage.error("分类删除失败");
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
    if (selectedCategories.value.length === 0) {
        ElMessage.warning('请选择要删除的分类');
        return;
    }

    // 检查是否有分类包含子分类
    const invalidCategories = selectedCategories.value.filter(cat =>
        cat.children && cat.children.length > 0
    );

    if (invalidCategories.length > 0) {
        ElMessage.error(`选中的分类中包含有子分类的分类，无法删除`);
        return;
    }

    batchConfirmVisible.value = true;
};

/**
 * 确认批量删除
 */
const confirmBatchDelete = async () => {
    batchLoading.value = true;
    batchProgressVisible.value = true;
    batchProgress.value = 0;
    batchProgressText.value = '正在删除分类...';
    
    try {
        const categoryIds = selectedCategories.value.map(cat => cat.id);
        const total = categoryIds.length;

        const res = await fetch("http://localhost:8081/catcate/productCategories/deleteByIds", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ ids: categoryIds })
        });

        // 模拟进度更新
        for (let i = 0; i <= 100; i += 20) {
            batchProgress.value = i;
            batchProgressText.value = `正在删除分类... (${Math.round((i/100)*total)}/${total})`;
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        if (res.ok) {
            batchProgressText.value = '批量删除完成';
            ElMessage.success(`成功删除 ${total} 个分类`);
            
            // 延迟关闭进度弹窗
            setTimeout(() => {
                batchProgressVisible.value = false;
                batchConfirmVisible.value = false;
                selectedCategories.value = [];
                fetchCategoryList();
                fetchParentCategoryOptions(); // 更新父分类选项
            }, 1000);
        } else {
            ElMessage.error('批量删除分类失败');
            batchProgressVisible.value = false;
        }
    } catch (error) {
        ElMessage.error('批量删除失败: ' + error.message);
        batchProgressVisible.value = false;
    } finally {
        batchLoading.value = false;
    }
};

/**
 * 导入分类
 */
const handleImport = () => {
    ElMessage.info('导入功能开发中');
};

/**
 * 导出分类
 */
const handleExport = () => {
    ElMessageBox.confirm(
        '请选择导出格式',
        '导出分类数据',
        {
            confirmButtonText: '导出为CSV',
            cancelButtonText: '导出为Excel',
            type: 'info'
        }
    ).then(() => {
        exportToCSV(categoryList.value);
    }).catch(() => {
        exportToExcel(categoryList.value);
    });
};

/**
 * 分类预览
 */
const handlePreviewCategory = (row) => {
    previewCategory.value = row;
    previewVisible.value = true;
};



/**
 * 切换拖拽模式
 */
const handleDragToggle = (value) => {
    if (value) {
        ElMessage.info('已开启拖拽排序模式，拖动分类行可调整顺序');
    } else {
        ElMessage.info('已关闭拖拽排序模式');
    }
};

/**
 * 拖拽开始
 */
const handleRowDragStart = (row) => {
    draggedRow.value = row;
    ElMessage.info('开始拖拽分类：' + row.name);
};

/**
 * 拖拽结束
 */
const handleRowDragEnd = async (draggedRow, targetRow, position) => {
    if (!targetRow) {
        ElMessage.warning('请将分类拖放到有效的位置');
        return;
    }

    try {
        loading.value = true;
        
        // 计算新的排序序号
        const newSortOrder = targetRow.sortOrder + (position === 'before' ? -0.5 : 0.5);
        
        // 更新排序
        const res = await fetch("http://localhost:8081/catcate/productCategories/updateSort", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                id: draggedRow.id,
                sortOrder: Math.max(0, Math.round(newSortOrder))
            })
        });

        if (res.ok) {
            ElMessage.success('分类排序更新成功');
            fetchCategoryList();
        } else {
            ElMessage.error('分类排序更新失败');
        }
    } catch (error) {
        console.error('拖拽排序失败:', error);
        ElMessage.error('拖拽排序失败: ' + error.message);
    } finally {
        loading.value = false;
        draggedRow.value = null;
    }
};

/**
 * 批量状态文本
 */
const batchStatusText = computed(() => {
    if (selectedCategories.value.length === 0) return '批量操作';
    const firstStatus = selectedCategories.value[0].isActive;
    const allSameStatus = selectedCategories.value.every(cat => cat.isActive === firstStatus);
    if (!allSameStatus) return '批量操作';
    return firstStatus === 1 ? '批量禁用' : '批量启用';
});
</script>

<style scoped>
.category-management {
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

.header-actions {
    display: flex;
    gap: 10px;
}

.page-header h1 {
    margin: 0;
    font-size: 20px;
    color: #1f2329;
}

/* 统计卡片 */
.stats-card {
    margin-bottom: 20px;
    background-color: #fff;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    border-radius: 12px;
    overflow: hidden;
}

.stat-item {
    text-align: center;
    padding: 20px;
    border-radius: 12px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
}

.stat-item::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: linear-gradient(90deg, #ff9f43, #ff6b6b);
}

.stat-item:hover {
    transform: translateY(-4px) scale(1.02);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-number {
    font-size: 28px;
    font-weight: bold;
    color: #ff9f43;
    margin-bottom: 8px;
    transition: all 0.3s ease;
}

.stat-item:hover .stat-number {
    transform: scale(1.05);
    color: #ff6b6b;
}

.stat-label {
    font-size: 14px;
    color: #666;
    font-weight: 500;
}

/* 筛选卡片 */
.filter-card {
    margin-bottom: 20px;
    padding: 20px;
    background: linear-gradient(135deg, #fff7ee 0%, #fff3e0 100%);
    border: none;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border-radius: 12px;
    border-left: 4px solid #ff9f43;
}

/* 筛选按钮区域 */
.filter-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 15px;
    gap: 10px;
    flex-wrap: wrap;
}

/* 表格卡片 */
.table-card {
    overflow: hidden;
    background-color: #fff;
    border: none;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border-radius: 12px;
    transition: all 0.3s ease;
}

.table-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 表格头部 */
.table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
    background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
    border-top-left-radius: 12px;
    border-top-right-radius: 12px;
}

.table-header span {
    font-weight: bold;
    color: #333;
}

/* 分页容器 */
.pagination {
    margin-top: 15px;
    text-align: right;
    padding: 16px;
    border-top: 1px solid #f0f0f0;
    background-color: #fff;
}

/* 表单样式 */
.category-form {
    margin-top: 10px;
}

/* 表单提示 */
.form-hint {
    font-size: 12px;
    color: #666;
    margin-top: 5px;
}

/* 警告文本 */
.warning-text {
    color: #f56c6c;
    font-size: 13px;
    margin-top: 8px;
}

/* 头像上传样式 */
.avatar-uploader {
    display: flex;
    align-items: center;
}

.avatar {
    width: 80px;
    height: 80px;
    display: block;
    border-radius: 8px;
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 80px;
    height: 80px;
    line-height: 80px;
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 8px;
    cursor: pointer;
}

/* 表格行样式 */
::v-deep .el-table .row-disabled {
    background-color: rgba(220, 220, 220, 0.1);
    color: #999;
    transition: all 0.3s ease;
}

::v-deep .el-table .row-disabled .el-tag,
::v-deep .el-table .row-disabled .el-switch,
::v-deep .el-table .row-disabled .el-input-number {
    opacity: 0.7;
}

/* 表格行悬停效果 */
::v-deep .el-table__row {
    transition: all 0.3s ease;
}

::v-deep .el-table__row:hover {
    background-color: rgba(255, 159, 67, 0.05) !important;
    transform: translateX(4px);
}

/* 表格单元格样式 */
::v-deep .el-table__cell {
    transition: all 0.3s ease;
}

::v-deep .el-table__cell:hover {
    background-color: rgba(255, 159, 67, 0.1) !important;
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

::v-deep .el-tag--success {
    background-color: #67c23a;
}

::v-deep .el-tag--primary {
    background-color: #409eff;
}

::v-deep .el-tag--warning {
    background-color: #e6a23c;
}

::v-deep .el-tag--info {
    background-color: #909399;
}

::v-deep .el-input__inner,
::v-deep .el-select-dropdown,
::v-deep .el-input-number,
::v-deep .el-textarea__inner {
    border-radius: 4px;
}

::v-deep .el-table th {
    background-color: #fff7ee;
}

/* 树形表格缩进 */
::v-deep .el-table .el-table__indent {
    width: 20px;
    flex: 0 0 20px;
}

/* 树图标 */
.tree-icon {
    margin-right: 5px;
    font-size: 14px;
}

/* 级别标签 */
.level-tag {
    margin-left: 5px;
    font-size: 10px;
}

/* 代码标签 */
.code-tag {
    font-size: 10px;
    padding: 2px 8px;
}

/* 状态文本 */
.status-text {
    font-size: 12px;
    margin-top: 4px;
    color: #666;
}

/* 批量操作进度条容器 */
.batch-progress-container {
    padding: 20px 0;
}

.batch-progress-container p {
    margin-bottom: 15px;
    color: #333;
    font-size: 14px;
}

/* 预览容器 */
.preview-container {
    padding: 10px 0;
}

.preview-card {
    border-radius: 12px;
    overflow: hidden;
}

.preview-header {
    display: flex;
    align-items: center;
    padding: 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
    border-radius: 12px 12px 0 0;
}

.preview-info {
    margin-left: 20px;
    flex: 1;
}

.preview-info h3 {
    margin: 0 0 10px 0;
    font-size: 20px;
    color: #333;
}

.preview-code-tag {
    font-size: 12px;
    padding: 4px 12px;
}

.preview-details {
    padding: 20px;
}

.preview-stats {
    padding: 20px;
}

.preview-stats h4 {
    margin: 0 0 15px 0;
    font-size: 16px;
    color: #333;
}

.child-category {
    display: flex;
    align-items: center;
    gap: 15px;
}

.child-name {
    flex: 1;
    font-weight: 500;
    color: #333;
}

.child-status {
    font-size: 12px;
    color: #666;
}

/* 操作按钮 */
.action-btn {
    margin-right: 8px;
    transition: all 0.3s ease;
    border-radius: 6px;
    overflow: hidden;
    position: relative;
}

.action-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: all 0.5s ease;
}

.action-btn:hover::before {
    left: 100%;
}

/* 拖拽相关样式 */
::v-deep .el-table__row.dragging {
    background-color: rgba(64, 158, 255, 0.1) !important;
    border: 1px dashed #409eff !important;
}

::v-deep .el-table__row.hover-row {
    background-color: rgba(64, 158, 255, 0.05) !important;
}

/* 表格头部操作区域 */
.table-header .header-actions {
    display: flex;
    gap: 20px;
    align-items: center;
}

/* 响应式调整 */
@media (max-width: 1200px) {
    .el-table-column {
        min-width: 100px !important;
    }
    
    .header-actions {
        flex-wrap: wrap;
    }
    
    .table-header .header-actions {
        flex-direction: column;
        align-items: flex-end;
        gap: 10px;
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
    
    .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }
    
    .header-actions {
        width: 100%;
        justify-content: flex-start;
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
    .el-table-column:not(.el-table-column--selection):not([label="分类ID"]):not([label="分类名称"]):not([label="状态"]):not([label="操作"]) {
        display: none !important;
    }
    
    .stats-card .el-col {
        width: 50% !important;
    }
}
</style>