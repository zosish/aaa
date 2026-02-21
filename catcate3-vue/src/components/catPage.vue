<template>
  <div class="cat-management">
    <!-- 页面标题和操作区 -->
    <div class="page-header">
      <h1>猫咪管理</h1>
      <el-button type="primary" icon="Plus" @click="handleAddCat" style="padding-left: 0px;">

        新增猫咪
      </el-button>
    </div>

    <!-- 搜索和筛选区 -->
    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.name" placeholder="搜索猫咪名字" clearable prefix-icon="Search"></el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.breed" placeholder="选择品种" clearable>
            <el-option v-for="breed in breedOptions" :key="breed" :label="breed" :value="breed"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.healthStatus" placeholder="健康状况" clearable>
            <el-option label="健康" value="HEALTHY"></el-option>
            <el-option label="生病" value="SICK"></el-option>
            <el-option label="恢复中" value="RECOVERING"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.adoptionStatus" placeholder="领养状态" clearable>
            <el-option label="可领养" value="AVAILABLE"></el-option>
            <el-option label="已领养" value="ADOPTED"></el-option>
            <el-option label="不可领养" value="UNAVAILABLE"></el-option>
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
        <el-button type="danger" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </el-card>

    <!-- 猫咪列表 -->
    <el-card class="table-card">
      <el-table :data="catList" border stripe :loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="name" label="猫咪名字" min-width="100"></el-table-column>
        <el-table-column prop="breed" label="品种" min-width="100"></el-table-column>
        <el-table-column prop="age" label="年龄" min-width="80" align="center" :formatter="formatAge"></el-table-column>
        <el-table-column prop="gender" label="性别" min-width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.gender === 'MALE' ? 'primary' : 'success'">
              {{ scope.row.gender === 'MALE' ? '公' : '母' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="healthStatus" label="健康状况" min-width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.healthStatus === 'HEALTHY' ? 'success' :
              scope.row.healthStatus === 'SICK' ? 'danger' : 'warning'
              ">
              {{
                scope.row.healthStatus === 'HEALTHY' ? '健康' :
                  scope.row.healthStatus === 'SICK' ? '生病' : '恢复中'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adoptionStatus" label="领养状态" min-width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.adoptionStatus === 'AVAILABLE' ? 'info' :
              scope.row.adoptionStatus === 'ADOPTED' ? 'success' : 'danger'
              ">
              {{
                scope.row.adoptionStatus === 'AVAILABLE' ? '可领养' :
                  scope.row.adoptionStatus === 'ADOPTED' ? '已领养' : '不可领养'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="是否启用" min-width="100" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.isActive" active-value="1" inactive-value="0"
              @change="handleStatusChange(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" icon="Eye" style="padding-left: 0px;"
              @click="handleViewCat(scope.row)">查看详情</el-button>
            <el-button size="small" type="success" icon="Edit" style="padding-left: 0px;"
              @click="handleEditCat(scope.row)">编辑猫咪</el-button>
            <el-button size="small" type="danger" icon="Delete" style="padding-left: 0px;"
              @click="handleDeleteCat(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"></el-pagination>
      </div>
    </el-card>

    <!-- 猫咪详情/新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="catFormRef" :model="catForm" :rules="formRules" label-width="100px" class="cat-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="猫咪名字" prop="name">
              <el-input v-model="catForm.name" placeholder="请输入猫咪名字"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品种" prop="breed">
              <el-input v-model="catForm.breed" placeholder="请输入猫咪品种"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄（月）" prop="age">
              <el-input v-model.number="catForm.age" placeholder="请输入年龄（月）" type="number" min="1"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="catForm.gender">
                <el-radio label="MALE">公</el-radio>
                <el-radio label="FEMALE">母</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康状况" prop="healthStatus">
              <el-select v-model="catForm.healthStatus" placeholder="请选择健康状况">
                <el-option label="健康" value="HEALTHY"></el-option>
                <el-option label="生病" value="SICK"></el-option>
                <el-option label="恢复中" value="RECOVERING"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="领养状态" prop="adoptionStatus">
              <el-select v-model="catForm.adoptionStatus" placeholder="请选择领养状态">
                <el-option label="可领养" value="AVAILABLE"></el-option>
                <el-option label="已领养" value="ADOPTED"></el-option>
                <el-option label="不可领养" value="UNAVAILABLE"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="性格描述" prop="personality">
              <el-input v-model="catForm.personality" placeholder="请输入猫咪性格描述" type="textarea" rows="3"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="日常护理" prop="dailyCare">
              <el-input v-model="catForm.dailyCare" placeholder="请输入日常护理信息" type="textarea" rows="3"></el-input>
            </el-form-item>
          </el-col>
          
          <!-- 图片上传区域 -->
          <el-col :span="24">
            <el-form-item label="猫咪照片">
              <div class="upload-container">
                <!-- 预览图片 -->
                <div v-if="catForm.photoUrl" class="image-preview">
                  <img :src="getImageUrl(catForm.photoUrl)" alt="猫咪照片" class="preview-image">
                  <el-button type="danger" icon="Delete" circle size="small" 
                           @click="removeImage" class="remove-btn"></el-button>
                </div>
                
                <!-- 上传组件 -->
                <el-upload
                  v-show="!catForm.photoUrl"
                  class="avatar-uploader"
                  action=""
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleImageUpload"
                  accept="image/*"
                >
                  <el-button type="primary" icon="Upload">点击上传照片</el-button>
                  <template #tip>
                    <div class="el-upload__tip">
                      jpg/png/gif 文件，且不超过5MB
                    </div>
                  </template>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
          
          <el-col :span="24" v-if="dialogType !== 'view'">
            <el-form-item label="是否活跃">
              <el-switch v-model="catForm.isActive" active-value="1" inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCat" v-if="dialogType !== 'view'">
          保存
        </el-button>
      </template>
    </el-dialog>
 
  </div>
</template>
<!-- eslint-disable no-unused-vars -->
<script setup>
import {
  ref, reactive, onMounted,
  // computed 
} from 'vue';
import {
  // columnAlignment, 
  ElMessage, ElMessageBox
} from 'element-plus';
import {
  //   Plus, Search, Eye, Edit, Delete, 
  //   Check, Close 
} from '@element-plus/icons-vue';

// 状态管理
const loading = ref(false);
const catList = ref([]);
const selectedCats = ref([]);
const catFormRef = ref(null);

// 搜索表单
const searchForm = reactive({
  name: '',
  breed: '',
  healthStatus: '',
  adoptionStatus: ''
});

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 品种选项（实际项目中可能从接口获取）
const breedOptions = ref([
  '布偶猫', '英短', '美短', '橘猫', '蓝猫', '暹逻猫', '波斯猫', '加菲猫', '其他'
]);

// 弹窗相关
const dialogVisible = ref(false);
const dialogType = ref(''); // add, edit, view
const dialogTitle = ref('');
const batchDeleteVisible = ref(false);

// 表单数据和验证规则
const catForm = reactive({
  id: '',
  name: '',
  breed: '',
  age: '',
  gender: 'MALE',
  healthStatus: 'HEALTHY',
  personality: '',
  photoUrl: '',  // 图片URL
  adoptionStatus: 'AVAILABLE',
  dailyCare: '',
  isActive: '1'
});

const formRules = reactive({
  name: [
    { required: true, message: '请输入猫咪名字', trigger: 'blur' },
    { max: 50, message: '名字长度不能超过50个字符', trigger: 'blur' }
  ],
  breed: [
    { max: 50, message: '品种长度不能超过50个字符', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 1, message: '年龄必须大于0', trigger: 'blur' }
  ],
  healthStatus: [
    { required: true, message: '请选择健康状况', trigger: 'change' }
  ],
  adoptionStatus: [
    { required: true, message: '请选择领养状态', trigger: 'change' }
  ]
});

// 处理图片上传
const handleImageUpload = async (file) => {
  try {
    // 验证文件
    if (!file.raw.type.startsWith('image/')) {
      ElMessage.error('只能上传图片文件！');
      return false;
    }
    
    // 减小文件大小限制到8MB以内以匹配后端配置
    if (file.raw.size > 8 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过8MB！');
      return false;
    }

    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', file.raw);

    console.log('开始上传文件:', file.raw.name);
    console.log('文件大小:', file.raw.size, 'bytes');
    
    // 使用相对路径通过代理访问
    const response = await fetch('/api/catcate/cats/uploadPhoto', {
      method: 'POST',
      body: formData
    });

    console.log('响应状态:', response.status);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    console.log('上传结果:', result);
    
    if (result.code === 200) {
      catForm.photoUrl = result.data.url;
      ElMessage.success('图片上传成功！');
    } else {
      ElMessage.error(result.message || '图片上传失败');
      // 显示详细错误信息用于调试
      console.error('上传失败详情:', result);
    }
  } catch (error) {
    console.error('上传失败:', error);
    ElMessage.error('上传失败：' + error.message);
    // 显示网络错误的详细信息
    if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
      ElMessage.error('网络连接失败，请检查后端服务是否正常运行');
    }
  }
};
// 移除图片
const removeImage = () => {
  catForm.photoUrl = '';
  ElMessage.info('已移除图片');
};

// 获取完整图片URL
const getImageUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http')) {
    return url;
  }
  // 如果是相对路径，加上基础URL
  return `http://localhost:8081${url}`;
};


// 生命周期
onMounted(() => {
  fetchCatList();
});

// 方法：获取猫咪列表
const fetchCatList = async () => {
  loading.value = true;
  try {
    const res = await fetch('http://localhost:8081/catcate/cats/list', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize,
        name: searchForm.name,
        breed: searchForm.breed,
        healthStatus: searchForm.healthStatus,
        adoptionStatus: searchForm.adoptionStatus
      })
    })
    const response = await res.json();
    console.log(response, "获取猫咪列表");

    // 正确处理分页数据
    if (response.records) {
      catList.value = response.records.map(cat => ({
        ...cat,
        isActive: String(cat.isActive)
      }));
      pagination.total = response.total;  // 设置总记录数
    } else {
      catList.value = [];
      pagination.total = 0;
    }
    loading.value = false;
  } catch (error) {
    ElMessage.error('获取猫咪列表失败: ' + error.message);
    loading.value = false;
  }
};

// 搜索和重置
const handleSearch = async () => {
  pagination.currentPage = 1;
  await fetchCatList();  // 直接调用查询
};

const resetSearch = () => {
  searchForm.name = '';
  searchForm.breed = '';
  searchForm.healthStatus = '';
  searchForm.adoptionStatus = '';
  pagination.currentPage = 1;
  fetchCatList();
};

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size;
  pagination.currentPage = 1;
  fetchCatList();
};

const handleCurrentChange = (page) => {
  pagination.currentPage = page;
  fetchCatList();
};

// 格式化年龄显示（月转年+月）
const formatAge = (row) => {
  if (!row.age) return '';
  const years = Math.floor(row.age / 12);
  const months = row.age % 12;
  let result = '';
  if (years > 0) result += `${years}年`;
  if (months > 0) result += `${months}月`;
  return result;
};

// 选择变化
const handleSelectionChange = (selection) => {
  selectedCats.value = selection;
};

// 状态切换
const handleStatusChange = (row) => {
  // 保存原始状态用于回滚
  const originalStatus = row.isActive === '1' ? '0' : '1';
  // 目标状态是当前状态的反值
  const targetStatus = row.isActive;

  ElMessageBox.confirm(
    `确定要${targetStatus === '1' ? '启用' : '停用'}猫咪【${row.name}】吗？`,
    '确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 发送修改是否活动请求到后端
      const res = await fetch('http://localhost:8081/catcate/cats/changeActive', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          id: row.id,
          isActive: targetStatus
        }),
      });

      const response = await res.json();
      console.log(response, "修改是否活动");

      // 更灵活地处理后端响应
      if (response && (response.success !== false)) {
        // 更新成功，保持当前状态不变
        ElMessage.success(`${row.name}${targetStatus === '1' ? '已启用' : '已停用'}`);
      } else {
        // 更新失败，恢复原始状态
        row.isActive = originalStatus;
        ElMessage.error(response.message || '状态更新失败');
      }
    } catch (error) {
      // 请求失败，恢复原始状态
      row.isActive = originalStatus;
      ElMessage.error('状态更新失败: ' + (error.message || '网络错误'));
    }
  }).catch(() => {
    // 取消操作，恢复原来的状态
    row.isActive = originalStatus;
  });
};

// 弹窗操作
const handleAddCat = () => {
  dialogType.value = 'add';
  dialogTitle.value = '新增猫咪';
  // 重置表单
  Object.assign(catForm, {
    id: '',
    name: '',
    breed: '',
    age: '',
    gender: 'MALE',
    healthStatus: 'HEALTHY',
    personality: '',
    photoUrl: '',
    adoptionStatus: 'AVAILABLE',
    dailyCare: '',
    isActive: '1'
  });
  dialogVisible.value = true;
};

const handleEditCat = (row) => {
  dialogType.value = 'edit';
  dialogTitle.value = '编辑猫咪';
  // 填充表单数据
  Object.assign(catForm, {
    id: row.id,
    name: row.name,
    breed: row.breed,
    age: row.age,
    gender: row.gender,
    healthStatus: row.healthStatus,
    personality: row.personality,
    photoUrl: row.photoUrl,
    adoptionStatus: row.adoptionStatus,
    dailyCare: row.dailyCare,
    isActive: row.isActive
  });
  dialogVisible.value = true;
};

const handleViewCat = (row) => {
  dialogType.value = 'view';
  dialogTitle.value = '查看猫咪详情';
  // 填充表单数据
  Object.assign(catForm, row);
  dialogVisible.value = true;
};

// 保存猫咪信息
const handleSaveCat = async () => {
  try {
    await catFormRef.value.validate();
    
    const catData = {
      ...catForm,
      age: Number(catForm.age),
      isActive: catForm.isActive === '1' ? 1 : 0
    };

    // 设置时间戳
    const now = new Date();
    if (dialogType.value === 'add') {
      catData.createTime = now;
    }
    catData.updateTime = now;

    const response = await fetch('http://localhost:8081/catcate/cats/addOrUpdate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(catData)
    });

    const result = await response.json();
    
    if (result) {
      ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功');
      dialogVisible.value = false;
      resetForm();
      fetchCatList();
    } else {
      ElMessage.error('保存失败');
    }
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败：' + error.message);
  }
};

// 重置表单
const resetForm = () => {
  catFormRef.value.resetFields();
  Object.assign(catForm, {
    id: '',
    name: '',
    breed: '',
    age: '',
    gender: 'MALE',
    healthStatus: 'HEALTHY',
    personality: '',
    photoUrl: '',
    adoptionStatus: 'AVAILABLE',
    dailyCare: '',
    isActive: '1'
  });
};


// 删除操作
const handleDeleteCat = (row) => {
  ElMessageBox.confirm(
    `确定要删除猫咪【${row.name}】的信息吗？此操作不可撤销。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(async () => {
    try {
      loading.value = true;
      const res = await fetch("http://localhost:8081/catcate/cats/deleteCat", {
        method: "Post",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          id: row.id,
        })
      })
      const response = await res.json();
      if (response == true) {
        ElMessage.success('删除成功');
      } else {
        ElMessage.error('删除失败');
      }
      //刷新整个页面
      window.location.reload();
    } catch (error) {
      ElMessage.error('删除失败');
      loading.value = false;
    }
  }).catch(() => {
    ElMessage.success('取消删除');
  });
};

// 批量删除
const handleBatchDelete = async () => {
  // 检查是否有选中的猫咪
  if (selectedCats.value.length === 0) {
    ElMessage.warning("请先选择要删除的猫咪");
    return;
  }

  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedCats.value.length} 只猫咪吗？此操作不可撤销。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(async () => {
    try {
      const catsList = selectedCats.value.map(cat => ({id: cat.id}));
      const response = await fetch("http://localhost:8081/catcate/cats/deleteByIds", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(catsList)
      });

      if (response.ok) {
        const result = await response.json();
        if (result === true) {
          ElMessage.success("批量删除成功");
          fetchCatList(); // 刷新列表而不是刷新整个页面
        } else {
          ElMessage.error("批量删除失败: " + (result.message || ""));
        }
      } else {
        ElMessage.error("批量删除失败，状态码: " + response.status);
      }
    } catch (error) {
      ElMessage.error("批量删除失败: " + error.message);
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};
</script>

<style scoped>
.cat-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 图片上传相关样式 */
.upload-container {
  width: 100%;
}

.image-preview {
  position: relative;
  display: inline-block;
  margin-bottom: 10px;
}

.preview-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.2s;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.el-upload__tip {
  font-size: 12px;
  color: #606266;
  margin-top: 7px;
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

.cat-form {
  margin-top: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-row {
    flex-direction: column;
  }

  .el-col {
    width: 100% !important;
    margin-bottom: 10px;
  }

  .filter-actions {
    flex-direction: column;
  }

  .filter-actions .el-button {
    width: 100%;
  }
}
</style>