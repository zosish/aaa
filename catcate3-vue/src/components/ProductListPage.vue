<template>
    <div class="product-management">
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h1>商品管理</h1>
            <el-button type="primary" icon="Plus" @click="handleAddProduct" style="padding-left: 0px;">
                新增商品
            </el-button>
        </div>

        <!-- 搜索和筛选区 -->
        <el-card class="filter-card">
            <el-row :gutter="20">
                <el-col :span="6">
                    <el-input v-model="searchForm.name" placeholder="搜索商品名称" clearable prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.category" placeholder="选择商品分类" clearable>
                        <el-option label="食品" value="FOOD"></el-option>
                        <el-option label="玩具" value="TOY"></el-option>
                        <el-option label="用品" value="SUPPLIES"></el-option>
                        <el-option label="其他" value="OTHER"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="6">
                    <el-input v-model="searchForm.brand" placeholder="搜索品牌" clearable prefix-icon="Shop"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.isAvailable" placeholder="选择上架状态" clearable>
                        <el-option label="已上架" value="1"></el-option>
                        <el-option label="已下架" value="0"></el-option>
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
                    :disabled="selectedProducts.length === 0">
                    批量删除
                </el-button>
                <el-button type="warning" icon="Refresh" @click="handleBatchStatusChange" style="padding-left: 0px;"
                    :disabled="selectedProducts.length === 0">
                    {{ batchActionText }}
                </el-button>
            </div>
        </el-card>

        <!-- 商品列表 -->
        <el-card class="table-card">
            <el-table :data="productList" border stripe :loading="loading" @selection-change="handleSelectionChange"
                :row-class-name="tableRowClassName" :cell-style="cellStyle">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="id" label="商品ID" width="80" align="center"></el-table-column>
                <el-table-column label="商品图片" width="100" align="center">
                    <template #default="scope">
                        <el-image :src="scope.row.imageUrl || defaultImage"
                            :preview-src-list="[scope.row.imageUrl || defaultImage]" width="60" height="60" fit="cover"
                            class="product-image"></el-image>
                    </template>
                </el-table-column>
                <el-table-column prop="name" label="商品名称" min-width="150"></el-table-column>
                <el-table-column prop="category" label="分类" min-width="100" align="center">
                    <template #default="scope">
                        <el-tag type="info">
                            {{ formatCategory(scope.row.category) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="brand" label="品牌" min-width="100" align="center"></el-table-column>
                <el-table-column prop="price" label="价格(¥)" min-width="100" align="center">
                    <template #default="scope">
                        {{ scope.row.price.toFixed(2) }}
                    </template>
                </el-table-column>
                <el-table-column prop="stockQuantity" label="库存" min-width="100" align="center">
                    <template #default="scope">
                        <span :class="scope.row.stockQuantity <= 10 ? 'low-stock' : ''">
                            {{ scope.row.stockQuantity }}
                            <el-tag size="mini" type="danger" v-if="scope.row.stockQuantity <= 10" style="color: #fff;">
                                库存紧张
                            </el-tag>
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="salesCount" label="销量" min-width="100" align="center"></el-table-column>
                <el-table-column prop="isAvailable" label="状态" min-width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.isAvailable === 1 ? 'success' : 'danger'" style="color: white;">
                            {{ scope.row.isAvailable === 1 ? '已上架' : '已下架' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="180" align="center"></el-table-column>
                <el-table-column label="操作" min-width="240" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" icon="Eye" @click="handleViewProduct(scope.row)"
                            style="padding-left: 0px;">商品详情</el-button>
                        <el-button size="small" type="success" icon="Edit" @click="handleEditProduct(scope.row)"
                            style="padding-left: 0px;">编辑商品</el-button>
                        <el-button size="small" :type="scope.row.isAvailable === 1 ? 'danger' : 'success'"
                            :icon="scope.row.isAvailable === 1 ? 'Close' : 'Check'"
                            @click="handleStatusChange(scope.row)" style="padding-left: 0px;">
                            {{ scope.row.isAvailable === 1 ? '下架' : '上架' }}
                        </el-button>
                        <el-button size="small" type="danger" icon="Delete" @click="handleDeleteProduct(scope.row)"
                            style="padding-left: 0px;">删除商品</el-button>
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

        <!-- 商品详情/新增/编辑弹窗 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" :close-on-click-modal="false">
            <el-form ref="productFormRef" :model="productForm" :rules="formRules" label-width="120px"
                class="product-form">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="商品名称" prop="name">
                            <el-input v-model="productForm.name" placeholder="请输入商品名称"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="商品分类" prop="category">
                            <el-select v-model="productForm.category" placeholder="请选择商品分类">
                                <el-option label="食品" value="FOOD"></el-option>
                                <el-option label="玩具" value="TOY"></el-option>
                                <el-option label="用品" value="SUPPLIES"></el-option>
                                <el-option label="其他" value="OTHER"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="品牌" prop="brand">
                            <el-input v-model="productForm.brand" placeholder="请输入品牌"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="售价(¥)" prop="price">
                            <el-input v-model="productForm.price" type="number" placeholder="请输入售价" min="0"
                                step="0.01"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="库存数量" prop="stockQuantity">
                            <el-input v-model="productForm.stockQuantity" type="number" placeholder="请输入库存数量" min="0"
                                step="1"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="上架状态" prop="isAvailable" v-if="dialogType !== 'view'">
                            <el-select v-model="productForm.isAvailable" placeholder="请选择上架状态">
                                <el-option label="已上架" value="1"></el-option>
                                <el-option label="已下架" value="0"></el-option>
                            </el-select>
                        </el-form-item>
                        <el-form-item label="上架状态" v-if="dialogType === 'view'">
                            <el-input :value="productForm.isAvailable === 1 ? '已上架' : '已下架'" disabled
                                placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <!-- 修改商品图片上传部分 -->
<el-col :span="24">
  <el-form-item label="商品图片" prop="imageUrl" v-if="dialogType !== 'view'">
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
        <div v-if="!productForm.imageUrl" class="upload-placeholder">
          <el-icon size="40" color="#c0c4cc"><Plus /></el-icon>
          <p>点击上传图片</p>
          <p class="upload-tips">支持JPG、PNG格式，建议尺寸1:1，大小不超过8MB</p>
        </div>
        <div v-else class="uploaded-preview">
          <img :src="productForm.imageUrl" alt="商品图片" class="preview-image" />
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
              @click.stop="removeImage"
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
  <el-form-item label="商品图片" v-if="dialogType === 'view'">
    <el-image 
      :src="productForm.imageUrl || defaultImage" 
      width="120" 
      height="120"
      fit="cover"
      :preview-src-list="[productForm.imageUrl || defaultImage]"
    ></el-image>
  </el-form-item>
</el-col>
                    <el-col :span="24">
                        <el-form-item label="规格参数" prop="specifications">
                            <el-input v-model="productForm.specifications" type="textarea" :rows="3"
                                placeholder="请输入规格参数（如：尺寸、材质、适用年龄等）" :disabled="dialogType === 'view'"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="商品描述" prop="description">
                            <el-input v-model="productForm.description" type="textarea" :rows="5"
                                placeholder="请输入商品详细描述" :disabled="dialogType === 'view'"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="dialogType === 'view'">
                        <el-form-item label="销售数量">
                            <el-input v-model="productForm.salesCount" disabled placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="dialogType === 'view'">
                        <el-form-item label="创建时间">
                            <el-input v-model="productForm.createTime" disabled placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="dialogType === 'view'">
                        <el-form-item label="更新时间">
                            <el-input v-model="productForm.updateTime" disabled placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSaveProduct" v-if="dialogType !== 'view'">
                    保存
                </el-button>
            </template>
        </el-dialog>

        <!-- 批量操作确认弹窗 -->
        <el-dialog v-model="batchConfirmVisible" :title="batchConfirmTitle" width="400px" :close-on-click-modal="false">
            <p>{{ batchConfirmContent }}</p>
            <template #footer>
                <el-button @click="batchConfirmVisible = false">取消</el-button>
                <el-button :type="batchActionType === 'delete' ? 'danger' : 'warning'" @click="confirmBatchAction">
                    确认{{ batchActionText }}
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>
<!-- eslint-disable no-unused-vars -->
<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import { ElMessage, ElMessageBox, ElImage } from 'element-plus';
import { Plus, Search, Eye, Edit, Delete, Check, Close, Refresh, Shop, Upload } from '@element-plus/icons-vue';

// 状态管理
const loading = ref(false);
const productList = ref([]);
const selectedProducts = ref([]);
const batchActionType = ref('status'); // status: 状态切换, delete: 删除
const productFormRef = ref(null);
const defaultImage = 'https://picsum.photos/seed/product/100/100';//默认图片
const uploadUrl = ref('http://localhost:8081/catcate/products/uploadImage'); // 图片上传接口
const fileInput = ref(null);
const uploadProgress = ref(0);
const uploadError = ref('');

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
          if (response.code === 200 && response.data && response.data.url) {
            productForm.imageUrl = response.data.url;
            ElMessage.success('图片上传成功');
          } else {
            uploadError.value = response.msg || '图片上传失败';
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
    xhr.open('POST', uploadUrl.value);
    xhr.send(formData);

  } catch (error) {
    uploadError.value = '上传过程中发生错误';
    ElMessage.error(uploadError.value);
    uploadProgress.value = 0;
  }
};

// 移除图片
const removeImage = () => {
  ElMessageBox.confirm('确定要移除这张图片吗？', '确认移除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    productForm.imageUrl = '';
    if (fileInput.value) {
      fileInput.value.value = '';
    }
    ElMessage.success('图片已移除');
  }).catch(() => {
    // 取消操作
  });
};


// 搜索表单
const searchForm = reactive({
    name: '',
    category: '',
    brand: '',
    isAvailable: ''
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
const productForm = reactive({
    id: '',
    name: '',
    category: 'FOOD',
    description: '',
    price: 0,
    stockQuantity: 0,
    imageUrl: '',
    brand: '',
    specifications: '',
    isAvailable: 1,
    salesCount: 0,
    createTime: '',
    updateTime: ''
});

const formRules = reactive({
    name: [
        { required: true, message: '请输入商品名称', trigger: 'blur' },
        { min: 2, max: 100, message: '商品名称长度必须在2-100个字符之间', trigger: 'blur' }
    ],
    category: [
        { required: true, message: '请选择商品分类', trigger: 'change' }
    ],
    brand: [
        { max: 50, message: '品牌名称长度不能超过50个字符', trigger: 'blur' }
    ],
    price: [
        { required: true, message: '请输入售价', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (value <= 0) {
                    callback(new Error('售价必须大于0'));
                } else if (!/^\d+(\.\d{1,2})?$/.test(value)) {
                    callback(new Error('售价最多保留两位小数'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ],
    stockQuantity: [
        { required: true, message: '请输入库存数量', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const num = Number(value);
                if (isNaN(num) || num < 0 || !Number.isInteger(num)) {
                    callback(new Error('库存数量必须是非负整数'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ],
    isAvailable: [
        { required: true, message: '请选择上架状态', trigger: 'change' }
    ],
    specifications: [
        { max: 500, message: '规格参数长度不能超过500个字符', trigger: 'blur' }
    ],
    description: [
        { max: 2000, message: '商品描述长度不能超过2000个字符', trigger: 'blur' }
    ],
    imageUrl: [
        {
            validator: (rule, value, callback) => {
                if (dialogType.value !== 'view' && (!value || value === '')) {
                    // 允许不上传图片，只在有值时验证格式
                    callback();
                } else if (value && !/^https?:\/\/.+/.test(value)) {
                    callback(new Error('图片链接格式不正确'));
                } else {
                    callback();
                }
            },
            trigger: 'change'
        }
    ]
});

// 计算属性：批量操作文本
const batchActionText = computed(() => {
    if (batchActionType.value === 'delete') {
        return '批量删除';
    } else {
        // 判断选中商品的状态，统一操作文本
        const hasAvailable = selectedProducts.value.some(product => product.isAvailable === 1);
        const hasUnavailable = selectedProducts.value.some(product => product.isAvailable === 0);

        if (hasAvailable && hasUnavailable) {
            return '批量下架';
        } else if (hasAvailable) {
            return '批量下架';
        } else {
            return '批量上架';
        }
    }
});

// 生命周期
onMounted(() => {
    fetchProductList();
});

// 方法：获取商品列表
const fetchProductList = async () => {
    loading.value = true;
    try {
        const res = await fetch("http://localhost:8081/catcate/products/selectList", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                name: searchForm.name,
                category: searchForm.category,
                brand: searchForm.brand,
                isAvailable: searchForm.isAvailable,
                pageNum: pagination.currentPage,
                pageSize: pagination.pageSize
            })
        });

        if (res.ok) {
            const data = await res.json();
            // 假设后端返回格式：{ total: 总数, list: 商品列表 }
            if (data && Array.isArray(data.list)) {
                productList.value = data.list;
                pagination.total = data.total || 0;
                loading.value = false;
            } else {
                ElMessage.error('数据格式不正确');
                loading.value = false;
            }
        } else {
            ElMessage.error('获取商品列表失败: ' + res.status);
            loading.value = false;
        }
    } catch (error) {
        console.error('获取商品列表失败:', error);
        ElMessage.error('获取商品列表失败: ' + error.message);
        loading.value = false;
    }
};

// 搜索和重置
const handleSearch = () => {
    pagination.currentPage = 1;
    fetchProductList();
};

const resetSearch = () => {
    searchForm.name = '';
    searchForm.category = '';
    searchForm.brand = '';
    searchForm.isAvailable = '';
    pagination.currentPage = 1;
    fetchProductList();
};

// 分页处理
const handleSizeChange = (size) => {
    pagination.pageSize = size;
    pagination.currentPage = 1;
    fetchProductList();
};

const handleCurrentChange = (page) => {
    pagination.currentPage = page;
    fetchProductList();
};

// 选择变化
const handleSelectionChange = (selection) => {
    selectedProducts.value = selection;
};

// 表格行样式（根据状态区分）
const tableRowClassName = ({ row }) => {
    if (row.isAvailable === 0) {
        return 'row-unavailable';
    }
    return '';
};

// 单元格样式（库存紧张时高亮）
const cellStyle = ({ row, column }) => {
    if (column.label === '库存' && row.stockQuantity <= 10) {
        return { color: '#ff6b6b', fontWeight: 'bold' };
    }
    return {};
};

// 格式化分类显示
const formatCategory = (category) => {
    const categoryMap = {
        'FOOD': '食品',
        'TOY': '玩具',
        'SUPPLIES': '用品',
        'OTHER': '其他'
    };
    return categoryMap[category] || category;
};

// 状态切换（上架/下架）
const handleStatusChange = (row) => {
    const newStatus = row.isAvailable === 1 ? 0 : 1;
    const confirmText = row.isAvailable === 1 ? '下架' : '上架';

    ElMessageBox.confirm(
        `确定要${confirmText}商品【${row.name}】吗？`,
        '确认操作',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: row.isAvailable === 1 ? 'danger' : 'success'
        }
    ).then(async () => {
        loading.value = true;
        // 获取后端数据
        const res = await fetch("http://localhost:8081/catcate/products/updateStatus", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                id: row.id,
                isAvailable: newStatus
            })
        })
        // 处理响应
        if (res.status === 200) {
            const data = await res.json();
            console.log(data);
            if (res.ok === true) {
                ElMessage.success(`商品已${confirmText}成功`);
                // 刷新商品列表
                fetchProductList();
                loading.value = false;
            }
        } else {
            ElMessage.error(`商品${confirmText}失败`);
            loading.value = false;
        }
    }).catch(() => {
        // 取消操作
    });
};

// 批量状态切换
const handleBatchStatusChange = () => {
    if (selectedProducts.value.length === 0) {
        ElMessage.warning('请选择要操作的商品');
        return;
    }

    // 判断操作类型
    const hasAvailable = selectedProducts.value.some(product => product.isAvailable === 1);
    const action = hasAvailable ? '下架' : '上架';

    batchActionType.value = 'status';
    batchConfirmTitle.value = `批量${action}商品`;
    batchConfirmContent.value = `您确定要${action}选中的 ${selectedProducts.value.length} 个商品吗？`;
    batchConfirmVisible.value = true;
};

// 批量删除
const handleBatchDelete = () => {
    if (selectedProducts.value.length === 0) {
        ElMessage.warning('请选择要删除的商品');
        return;
    }

    batchActionType.value = 'delete';
    batchConfirmTitle.value = '批量删除商品';
    batchConfirmContent.value = `您确定要删除选中的 ${selectedProducts.value.length} 个商品吗？此操作不可撤销，相关订单数据将保留。`;
    batchConfirmVisible.value = true;
};

// 确认批量操作
const confirmBatchAction = async () => {
    loading.value = true;

    try {
        if (batchActionType.value === 'delete') {
            // 批量删除
            const productsArray = selectedProducts.value.map(product => ({ id: product.id }));

            // 发起后端请求
            const res = await fetch("http://localhost:8081/catcate/products/deleteByIds", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(productsArray)
            })
            // 处理响应
            if (res.status === 200) {
                const data = await res.json();
                console.log(data);
                if (res.ok === true) {
                    ElMessage.success("批量删除成功");
                    // 刷新商品列表
                    fetchProductList();
                    // 关闭弹窗
                    batchConfirmVisible.value = false;
                    selectedProducts.value = [];
                }
            } else {
                ElMessage.error("批量删除失败");
            }
        } else {
            // 批量状态切换
            const newStatus = selectedProducts.value[0].isAvailable === 1 ? 0 : 1;
            const action = newStatus === 0 ? '下架' : '上架';
            const productsArray = selectedProducts.value.map(product => ({
                id: product.id,
                isAvailable: newStatus
            }));
            // 发起后端请求
            const res = await fetch("http://localhost:8081/catcate/products/updateByIdStatus", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(productsArray)
            })
            // 处理响应
            if (res.status === 200) {
                const data = await res.json();
                console.log(data);
                if (res.ok === true) {
                    ElMessage.success(`批量${action}成功`);
                    // 刷新商品列表
                    fetchProductList();
                    // 关闭弹窗
                    batchConfirmVisible.value = false;
                    selectedProducts.value = [];
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
const handleAddProduct = () => {
    dialogType.value = 'add';
    dialogTitle.value = '新增商品';
    // 重置表单
    // 直接赋值，确保所有字段都被正确初始化
    productForm.id = '';
    productForm.name = '';
    productForm.category = 'FOOD';
    productForm.description = '';
    productForm.price = 0;
    productForm.stockQuantity = 0;
    productForm.imageUrl = '';
    productForm.brand = '';
    productForm.specifications = '';
    productForm.isAvailable = 1;
    productForm.salesCount = 0;
    productForm.createTime = '';
    productForm.updateTime = '';

    dialogVisible.value = true;
};

const handleEditProduct = (row) => {
    dialogType.value = 'edit';
    dialogTitle.value = '编辑商品';
    // 填充表单数据
    Object.assign(productForm, {
        id: row.id,
        name: row.name,
        category: row.category,
        description: row.description,
        price: row.price,
        stockQuantity: row.stockQuantity,
        imageUrl: row.imageUrl,
        brand: row.brand,
        specifications: row.specifications,
        isAvailable: row.isAvailable,
        salesCount: row.salesCount,
        createTime: row.createTime,
        updateTime: row.updateTime
    });
    dialogVisible.value = true;
};

const handleViewProduct = (row) => {
    dialogType.value = 'view';
    dialogTitle.value = '查看商品详情';
    // 填充表单数据
    Object.assign(productForm, row);
    dialogVisible.value = true;
};

// 保存商品信息
const handleSaveProduct = async () => {


    try {
        // 确保表单ref已初始化
        await nextTick();
        // 表单验证
        await productFormRef.value.validate();
        loading.value = true;

        // 请求后端数据
        const res = await fetch("http://localhost:8081/catcate/products/addOrUpdate", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(productForm)
        })

        // 处理响应
        if (res.status === 200) {
            const data = await res.json();
            if (res.ok === true) {
                ElMessage.success(dialogType.value === 'add' ? "商品新增成功" : "商品更新成功");
                // 刷新商品列表
                fetchProductList();
                // 关闭弹窗
                dialogVisible.value = false;
                // 保存成功后清空表单
                resetProductForm();
            } else {
                ElMessage.error(data.msg || (dialogType.value === 'add' ? "商品新增失败" : "商品更新失败"));
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
// 重置表单的辅助函数
const resetProductForm = () => {
    Object.keys(productForm).forEach(key => {
        if (key === 'category') {
            productForm[key] = 'FOOD';
        } else if (key === 'isAvailable') {
            productForm[key] = 1;
        } else if (['price', 'stockQuantity', 'salesCount'].includes(key)) {
            productForm[key] = 0;
        } else {
            productForm[key] = '';
        }
    });
};

// 删除操作
const handleDeleteProduct = (row) => {
    ElMessageBox.confirm(
        `确定要删除商品【${row.name}】吗？此操作不可撤销，相关订单数据将保留。`,
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
            const res = await fetch("http://localhost:8081/catcate/products/deleteById", {
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
                console.log(data);
                if (res.ok === true) {
                    ElMessage.success("商品删除成功");
                    // 刷新商品列表
                    fetchProductList();
                    loading.value = false;
                }
            } else {
                ElMessage.error("商品删除失败");
                loading.value = false;
            }
        } catch (error) {
            ElMessage.error('删除失败');
            loading.value = false;
        }
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

.product-management {
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

.product-form {
    margin-top: 10px;
}

/* 商品图片样式 */
.product-image {
    border-radius: 4px;
    cursor: pointer;
    transition: transform 0.3s ease;
}

.product-image:hover {
    transform: scale(1.05);
}

/* 表单提示 */
.form-hint {
    font-size: 12px;
    color: #666;
    margin-top: 5px;
}

/* 表格行样式 */
::v-deep .el-table .row-unavailable {
    background-color: rgba(204, 204, 204, 0.05);
    color: #999;
}

::v-deep .el-table .row-unavailable .el-tag {
    opacity: 0.7;
}

/* 低库存样式 */
.low-stock {
    color: #ff6b6b;
    font-weight: bold;
}

/* 上传组件样式 */
::v-deep .avatar-uploader {
    margin-bottom: 10px;
}

::v-deep .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 120px;
    height: 120px;
    line-height: 120px;
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
    width: 120px;
    height: 120px;
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
    .el-table-column:not(.el-table-column--selection):not([label="商品ID"]):not([label="商品名称"]):not([label="分类"]):not([label="价格(¥)"]):not([label="库存"]):not([label="状态"]):not([label="操作"]) {
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

::v-deep .el-tag--info {
    background-color: #00d2d3;
}

::v-deep .el-tag--success {
    background-color: #00d2d3;
}

::v-deep .el-tag--danger {
    background-color: #ff6b6b;
}

::v-deep .el-input__inner,
::v-deep .el-select-dropdown,
::v-deep .el-date-editor .el-input__inner {
    border-radius: 4px;
}

::v-deep .el-table th {
    background-color: #fff7ee;
}

/* 图片预览样式 */
::v-deep .el-image-viewer__wrapper {
    z-index: 3000 !important;
}
</style>