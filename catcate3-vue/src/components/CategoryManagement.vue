<!-- 分类管理 -->
<template>
    <div class="category-management">
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h1>商品分类管理</h1>
            <el-button type="primary" icon="Plus" @click="handleAddCategory" style="padding-left: 0px;">
                新增分类
            </el-button>
        </div>

        <!-- 搜索和筛选区 -->
        <el-card class="filter-card">
            <el-row :gutter="20">
                <el-col :span="8">
                    <el-input v-model="searchForm.name" placeholder="搜索分类名称" clearable prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-input v-model="searchForm.code" placeholder="搜索分类代码" clearable prefix-icon="Key"></el-input>
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
                    :disabled="selectedCategories.length === 0">
                    批量删除
                </el-button>
            </div>
        </el-card>

        <!-- 分类列表（树形结构） -->
        <el-card class="table-card">
            <el-table :data="categoryList" border stripe :loading="loading" @selection-change="handleSelectionChange"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" :row-class-name="tableRowClassName">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="id" label="分类ID" width="80" align="center"></el-table-column>
                <el-table-column label="分类图标" width="80" align="center">
                    <template #default="scope">
                        <el-avatar :size="32" :src="scope.row.icon || defaultCategoryIcon">
                            <el-icon :size="16">
                                <Box />
                            </el-icon>
                        </el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="name" label="分类名称" min-width="150">
                    <template #default="scope">
                        <span :style="{ 'padding-left': scope.row.level * 20 + 'px' }">
                            {{ scope.row.name }}
                            <el-tag v-if="!scope.row.parent_id" size="mini" type="primary" effect="plain"
                                style="color: #fff;">
                                一级分类
                            </el-tag>
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="code" label="分类代码" min-width="120" align="center">
                    <template #default="scope">
                        <el-tag :type="getCategoryTagType(scope.row.code)" size="mini"
                            style="color: #fff; background-color: #00d2d3;">
                            {{ scope.row.code }}
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
                <el-table-column prop="createTime" label="创建时间" min-width="180" align="center"></el-table-column>
                <el-table-column label="操作" min-width="200" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" icon="Plus" @click="handleAddSubCategory(scope.row)"
                            style="padding-left: 0px;">
                            新增子分类
                        </el-button>
                        <el-button size="small" type="info" icon="Edit" @click="handleEditCategory(scope.row)"
                            style="padding-left: 0px;">编辑</el-button>
                        <el-button size="small" type="danger" icon="Delete" @click="handleDeleteCategory(scope.row)"
                            style="padding-left: 0px;"
                            :disabled="scope.row.children && scope.row.children.length > 0">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
                <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
                    :page-sizes="[10, 20, 50]" :total="pagination.total"
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
                                maxlength="50"></el-input>
                            <div class="form-hint">代码用于系统识别，如：FOOD、CAT_FOOD，不可重复</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="父分类" prop="parentId">
                            <el-select v-model="categoryForm.parentId" placeholder="选择父分类（可选）" filterable clearable>
                                <el-option v-for="category in parentCategoryOptions" :key="category.id"
                                    :label="category.name" :value="category.id">
                                </el-option>
                            </el-select>
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
                <el-button type="primary" @click="handleSaveCategory">
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
                <el-button type="danger" @click="confirmBatchDelete">
                    确认删除
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
    Upload, Picture, FolderOpened
} from '@element-plus/icons-vue';

// 状态管理
const loading = ref(false);
const categoryList = ref([]);
const selectedCategories = ref([]);
const categoryFormRef = ref(null);
const defaultCategoryIcon = 'https://picsum.photos/seed/category/80/80';
const uploadUrl = 'http://localhost:8081/catcate/upload/icon';

// 搜索表单
const searchForm = reactive({
    name: '',
    code: '',
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
const dialogType = ref(''); // add, addSub, edit
const dialogTitle = ref('');
const batchConfirmVisible = ref(false);

// 父分类选项
const parentCategoryOptions = ref([]);

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
 * 检查分类代码唯一性
 */
const checkCodeUnique = async (rule, value, callback) => {
    if (!value) {
        callback();
        return;
    }

    // 编辑模式下，如果代码未修改，直接通过
    if (dialogType.value === 'edit' && categoryForm.code === value) {
        callback();
        return;
    }

    try {
        const res = await fetch(`http://localhost:8081/catcate/productCategories/checkCodeUnique?code=${value}`, {
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
            callback(new Error('检查代码唯一性失败'));
        }
    } catch (error) {
        console.error('检查代码唯一性失败:', error);
        callback(new Error('检查代码唯一性失败'));
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
        { validator: checkCodeUnique, message: '分类代码已存在', trigger: 'blur' }
    ],
    sortOrder: [
        { required: true, message: '请输入排序序号', trigger: 'blur' },
        { type: 'number', min: 0, max: 100, message: '排序序号必须在0-100之间', trigger: 'blur' }
    ],
    description: [
        { max: 200, message: '分类描述不能超过200个字符', trigger: 'blur' }
    ]
});

// 生命周期
onMounted(() => {
    fetchCategoryList();
    fetchParentCategoryOptions();
});

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
 * 为分类添加层级标识
 */
const addLevelToCategories = (categories, parentId = null, level = 0) => {
    return categories
        .filter(category => category.parentId === parentId)
        .map(category => {
            const children = addLevelToCategories(categories, category.id, level + 1);
            return {
                ...category,
                level,
                children: children.length > 0 ? children : undefined,
                hasChildren: children.length > 0
            };
        });
};

/**
 * 获取父分类选项（一级分类）
 */
const fetchParentCategoryOptions = async () => {
    try {
        const res = await fetch("http://localhost:8081/catcate/productCategories/selectParentOptions", {
            method: "GET"
        });

        if (res.ok) {
            const data = await res.json();
            parentCategoryOptions.value = data || [];
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
 * 获取分类标签类型
 */
const getCategoryTagType = (code) => {
    const typeMap = {
        'FOOD': 'success',
        'TOY': 'primary',
        'SUPPLIES': 'warning',
        'OTHER': 'info'
    };

    // 匹配前缀
    if (code.startsWith('FOOD')) return 'success';
    if (code.startsWith('TOY')) return 'primary';
    if (code.startsWith('SUPPLIES')) return 'warning';
    return typeMap[code] || 'default';
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
        loading.value = true;

        const submitForm = { ...categoryForm };

        const res = await fetch("http://localhost:8081/catcate/productCategories/addOrUpdate", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(submitForm)
        });

        if (res.ok) {
            ElMessage.success(dialogType.value.includes('add') ? "分类新增成功" : "分类更新成功");
            dialogVisible.value = false;
            fetchCategoryList();
            fetchParentCategoryOptions(); // 更新父分类选项
        } else {
            ElMessage.error(dialogType.value.includes('add') ? "分类新增失败" : "分类更新失败");
        }
    } catch (error) {
        return;
    } finally {
        loading.value = false;
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
    loading.value = true;
    try {
        const categoryIds = selectedCategories.value.map(cat => cat.id);

        const res = await fetch("http://localhost:8081/catcate/productCategories/deleteByIds", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ ids: categoryIds })
        });

        if (res.ok) {
            ElMessage.success(`成功删除 ${selectedCategories.value.length} 个分类`);
            batchConfirmVisible.value = false;
            selectedCategories.value = [];
            fetchCategoryList();
            fetchParentCategoryOptions(); // 更新父分类选项
        } else {
            ElMessage.error('批量删除分类失败');
        }
    } catch (error) {
        ElMessage.error('批量删除失败: ' + error.message);
    } finally {
        loading.value = false;
    }
};
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

.page-header h1 {
    margin: 0;
    font-size: 20px;
    color: #1f2329;
}

/* 筛选卡片 */
.filter-card {
    margin-bottom: 20px;
    padding: 15px;
    background-color: #fff7ee;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    border-radius: 8px;
}

/* 筛选按钮区域 */
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
    .el-table-column:not(.el-table-column--selection):not([label="分类ID"]):not([label="分类名称"]):not([label="状态"]):not([label="操作"]) {
        display: none !important;
    }
}
</style>