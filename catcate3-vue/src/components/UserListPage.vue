<template>
    <div class="user-management">
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h1>用户管理</h1>
            <el-button type="primary" icon="Plus" @click="handleAddUser" style="padding-left: 0px;">
                新增用户
            </el-button>
        </div>

        <!-- 搜索和筛选区 -->
        <el-card class="filter-card">
            <el-row :gutter="20">
                <el-col :span="6">
                    <el-input v-model="searchForm.username" placeholder="搜索用户名" clearable
                        prefix-icon="Search"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-input v-model="searchForm.phone" placeholder="搜索手机号" clearable prefix-icon="Phone"></el-input>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.role" placeholder="选择角色" clearable>
                        <el-option label="管理员" value="ADMIN"></el-option>
                        <el-option label="顾客" value="CUSTOMER"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="6">
                    <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
                        <el-option label="活跃" value="ACTIVE"></el-option>
                        <el-option label="禁用" value="INACTIVE"></el-option>
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
                    :disabled="selectedUsers.length === 0">
                    批量删除
                </el-button>
                <el-button type="warning" icon="Refresh" @click="handleBatchStatusChange" style="padding-left: 0px;"
                    :disabled="selectedUsers.length === 0">
                    {{ batchActionText }}
                </el-button>
            </div>
        </el-card>

        <!-- 用户列表 -->
        <el-card class="table-card">
            <el-table :data="userList" border stripe :loading="loading" @selection-change="handleSelectionChange"
                :row-class-name="tableRowClassName">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="id" label="用户ID" width="80" align="center"></el-table-column>
                <el-table-column prop="username" label="用户名" min-width="120"></el-table-column>
                <el-table-column prop="nickname" label="昵称" min-width="100"></el-table-column>
                <el-table-column prop="email" label="邮箱" min-width="180"></el-table-column>
                <el-table-column prop="phone" label="手机号" min-width="120" align="center"></el-table-column>
                <el-table-column prop="role" label="角色" min-width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.role === 'ADMIN' ? 'primary' : 'info'">
                            {{ scope.row.role === 'ADMIN' ? '管理员' : '顾客' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" min-width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'" style="color: white;">
                            {{ scope.row.status === 'ACTIVE' ? '活跃' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="lastLoginTime" label="最后登录时间" min-width="180" align="center">
                    <template #default="scope">
                        {{ scope.row.lastLoginTime || '从未登录' }}
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="180" align="center"></el-table-column>
                <el-table-column label="操作" min-width="220" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" icon="Eye" @click="handleViewUser(scope.row)"
                            style="padding-left: 0px;">用户详情</el-button>
                        <el-button size="small" type="success" icon="Edit" @click="handleEditUser(scope.row)"
                            style="padding-left: 0px;">编辑用户</el-button>
                        <el-button size="small" :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'"
                            :icon="scope.row.status === 'ACTIVE' ? 'Close' : 'Check'"
                            @click="handleStatusChange(scope.row)" style="padding-left: 0px;">
                            {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
                        </el-button>
                        <el-button size="small" type="danger" icon="Delete" @click="handleDeleteUser(scope.row)"
                            style="padding-left: 0px;" v-if="scope.row.role !== 'ADMIN'">删除用户</el-button>
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

        <!-- 用户详情/新增/编辑弹窗 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
            <el-form ref="userFormRef" :model="userForm" :rules="formRules" label-width="100px" class="user-form">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="用户名" prop="username" :disabled="dialogType === 'edit'">
                            <el-input v-model="userForm.username" placeholder="请输入用户名" />
                            <div class="form-hint" v-if="dialogType === 'add'">
                                用户名一旦创建不可修改
                            </div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="昵称" prop="nickname">
                            <el-input v-model="userForm.nickname" placeholder="请输入昵称"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="邮箱" prop="email">
                            <el-input v-model="userForm.email" type="email" placeholder="请输入邮箱"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="手机号" prop="phone">
                            <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="角色" prop="role">
                            <el-select v-model="userForm.role" placeholder="请选择角色">
                                <el-option label="管理员" value="ADMIN"></el-option>
                                <el-option label="顾客" value="CUSTOMER"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="状态" prop="status" v-if="dialogType !== 'add'">
                            <el-select v-model="userForm.status" placeholder="请选择状态">
                                <el-option label="活跃" value="ACTIVE"></el-option>
                                <el-option label="禁用" value="INACTIVE"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="dialogType === 'add'">
                        <el-form-item label="密码" prop="password">
                            <el-input v-model="userForm.password" type="password" placeholder="请输入密码"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="dialogType === 'add'">
                        <el-form-item label="确认密码" prop="confirmPassword">
                            <el-input v-model="userForm.confirmPassword" type="password" placeholder="请确认密码"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24" v-if="dialogType === 'view'">
                        <el-form-item label="最后登录时间">
                            <el-input v-model="userForm.lastLoginTime" disabled placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24" v-if="dialogType === 'view'">
                        <el-form-item label="创建时间">
                            <el-input v-model="userForm.createTime" disabled placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24" v-if="dialogType === 'view'">
                        <el-form-item label="更新时间">
                            <el-input v-model="userForm.updateTime" disabled placeholder="暂无记录"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSaveUser" v-if="dialogType !== 'view'">
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

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
// import { 
//   Plus, Search, Eye, Edit, Delete, Check, Close,
//   Phone, Refresh, User
// } from '@element-plus/icons-vue';

// 状态管理
const loading = ref(false);
const userList = ref([]);
const selectedUsers = ref([]);
const batchActionType = ref('status'); // status: 状态切换, delete: 删除
const userFormRef = ref(null);

// 搜索表单
const searchForm = reactive({
    username: '',
    phone: '',
    role: '',
    status: ''
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
const userForm = reactive({
    id: '',
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: '',
    role: 'CUSTOMER',
    nickname: '',
    avatar: '',
    status: 'ACTIVE',
    lastLoginTime: '',
    createTime: '',
    updateTime: ''
});

const formRules = reactive({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_-]{3,50}$/, message: '用户名只能包含字母、数字、下划线和短横线或已有该用户名', trigger: 'blur' }
    ],
    nickname: [
        { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
    ],
    email: [
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
        { max: 100, message: '邮箱长度不能超过100个字符', trigger: 'blur' }
    ],
    phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
        { max: 20, message: '手机号长度不能超过20个字符', trigger: 'blur' }
    ],
    role: [
        { required: true, message: '请选择角色', trigger: 'change' }
    ],
    status: [
        { required: true, message: '请选择状态', trigger: 'change' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                if (value !== userForm.password) {
                    callback(new Error('两次输入的密码不一致'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ]
});

// 计算属性：批量操作文本
const batchActionText = computed(() => {
    if (batchActionType.value === 'delete') {
        return '批量删除';
    } else {
        // 判断选中用户的状态，统一操作文本
        const hasActive = selectedUsers.value.some(user => user.status === 'ACTIVE');
        const hasInactive = selectedUsers.value.some(user => user.status === 'INACTIVE');

        if (hasActive && hasInactive) {
            return '批量禁用';
        } else if (hasActive) {
            return '批量禁用';
        } else {
            return '批量启用';
        }
    }
});

// 生命周期
onMounted(() => {
    fetchUserList();
});

// 方法：获取用户列表
const fetchUserList = async () => {
    loading.value = true;
    try {
        const res = await fetch("http://localhost:8081/catcate/users/selectList", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                pageNum: pagination.currentPage,
                pageSize: pagination.pageSize,
                username: searchForm.username,
                phone: searchForm.phone,
                role: searchForm.role,
                status: searchForm.status,
            })
        });

        if (res.ok) {
            const data = await res.json();
            // 根据实际返回格式调整
            if (data && Array.isArray(data.list)) {
                userList.value = data.list;
                pagination.total = data.total || 0;
                loading.value = false;
            } else if (Array.isArray(data)) {
                // 兼容旧格式
                userList.value = data;
                pagination.total = data.length;
                loading.value = false;
            } else {
                ElMessage.error('数据格式不正确');
                loading.value = false;
            }
        } else {
            ElMessage.error('获取用户列表失败: ' + res.status);
            loading.value = false;
        }
    } catch (error) {
        console.error('获取用户列表失败:', error);
        ElMessage.error('获取用户列表失败: ' + error.message);
        loading.value = false;
    }
};

// 搜索和重置
const handleSearch = () => {
    pagination.currentPage = 1;
    fetchUserList();
};

const resetSearch = () => {
    searchForm.username = '';
    searchForm.phone = '';
    searchForm.role = '';
    searchForm.status = '';
    pagination.currentPage = 1;
    fetchUserList();
};

// 分页处理
const handleSizeChange = (size) => {
    pagination.pageSize = size;
    pagination.currentPage = 1;
    fetchUserList();
};

const handleCurrentChange = (page) => {
    pagination.currentPage = page;
    fetchUserList();
};

// 选择变化
const handleSelectionChange = (selection) => {
    selectedUsers.value = selection;
};

// 表格行样式（根据状态区分）
const tableRowClassName = ({ row }) => {
    if (row.status === 'INACTIVE') {
        return 'row-inactive';
    }
    return '';
};

// 状态切换
const handleStatusChange = (row) => {
    const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const confirmText = row.status === 'ACTIVE' ? '禁用' : '启用';

    ElMessageBox.confirm(
        `确定要${confirmText}用户【${row.username}】吗？`,
        '确认操作',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: row.status === 'ACTIVE' ? 'danger' : 'success'
        }
    ).then(async () => {
        loading.value = true;
        // 获取后端数据
        const res = await fetch("http://localhost:8081/catcate/users/updateStatus", {
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
            console.log(data);
            if (res.ok === true) {
                ElMessage.success("操作成功");
                // 刷新用户列表
                fetchUserList();
                // 关闭弹窗
                batchConfirmVisible.value = false;
                loading.value = false;
            }
        }
    }).catch(() => {
        // 取消操作
    });
};

// 批量状态切换
const handleBatchStatusChange = () => {
    if (selectedUsers.value.length === 0) {
        ElMessage.warning('请选择要操作的用户');
        return;
    }

    // 判断操作类型
    const hasActive = selectedUsers.value.some(user => user.status === 'ACTIVE');
    const action = hasActive ? '禁用' : '启用';
    //   const newStatus = hasActive ? 'INACTIVE' : 'ACTIVE';

    batchActionType.value = 'status';
    batchConfirmTitle.value = `批量${action}用户`;
    batchConfirmContent.value = `您确定要${action}选中的 ${selectedUsers.value.length} 个用户吗？`;
    batchConfirmVisible.value = true;
};

// 批量删除
const handleBatchDelete = () => {
    if (selectedUsers.value.length === 0) {
        ElMessage.warning('请选择要删除的用户');
        return;
    }

    // 检查是否包含管理员
    const hasAdmin = selectedUsers.value.some(user => user.role === 'ADMIN');
    if (hasAdmin) {
        ElMessage.error('不能删除管理员用户');
        return;
    }

    batchActionType.value = 'delete';
    batchConfirmTitle.value = '批量删除用户';
    batchConfirmContent.value = `您确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可撤销。`;
    batchConfirmVisible.value = true;
};

// 确认批量操作
const confirmBatchAction = async () => {
    loading.value = true;

    try {
        if (batchActionType.value === 'delete') {
            // 批量删除
            const usersArray = selectedUsers.value.map(user => ({ id: user.id }));

            // 发起后端请求
            const res = await fetch("http://localhost:8081/catcate/users/deleteByIds", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(usersArray)

            })
            // 处理响应
            if (res.status === 200) {
                const data = await res.json();
                console.log(data);
                if (res.ok === true) {
                    ElMessage.success("操作成功");
                    // 刷新用户列表
                    fetchUserList();
                    // 关闭弹窗
                    batchConfirmVisible.value = false;
                    loading.value = false;
                }
            }
        } else {
            // 批量状态切换
            const newStatus = selectedUsers.value[0].status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
            const usersArray = selectedUsers.value.map(user => ({
                id: user.id,
                status: newStatus
            }));
            // 发起后端请求
            const res = await fetch("http://localhost:8081/catcate/users/updateByIdStatus", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(usersArray)
            })
            // 处理响应
            if (res.status === 200) {
                const data = await res.json();
                console.log(data);
                if (res.ok === true) {
                    ElMessage.success("操作成功");
                    // 刷新用户列表
                    fetchUserList();
                    // 关闭弹窗
                    batchConfirmVisible.value = false;
                    loading.value = false;
                }
            }
        }

        batchConfirmVisible.value = false;
        loading.value = false;

    } catch (error) {
        ElMessage.error('批量操作失败');
        batchConfirmVisible.value = false;
        loading.value = false;
    }
};

// 弹窗操作
const handleAddUser = () => {
    dialogType.value = 'add';
    dialogTitle.value = '新增用户';
    // 重置表单
    Object.assign(userForm, {
        id: '',
        username: '',
        password: '',
        confirmPassword: '',
        email: '',
        phone: '',
        role: 'CUSTOMER',
        nickname: '',
        avatar: '',
        status: 'ACTIVE',
        lastLoginTime: '',
        createTime: '',
        updateTime: ''
    });
    dialogVisible.value = true;
};

const handleEditUser = (row) => {
    dialogType.value = 'edit';
    dialogTitle.value = '编辑用户';
    // 填充表单数据（密码不填充）
    Object.assign(userForm, {
        id: row.id,
        username: row.username,
        password: '',
        confirmPassword: '',
        email: row.email,
        phone: row.phone,
        role: row.role,
        nickname: row.nickname,
        avatar: row.avatar,
        status: row.status,
        lastLoginTime: row.lastLoginTime,
        createTime: row.createTime,
        updateTime: row.updateTime
    });
    dialogVisible.value = true;
};

const handleViewUser = (row) => {
    dialogType.value = 'view';
    dialogTitle.value = '查看用户详情';
    // 填充表单数据
    Object.assign(userForm, row);
    dialogVisible.value = true;
};

// 保存用户信息
const handleSaveUser = async () => {
    // 确保表单ref已初始化
    await nextTick();

    try {
        // 表单验证
        await userFormRef.value.validate();
        loading.value = true;
        //请求后端数据
        const res = await fetch("http://localhost:8081/catcate/users/addOrUpdate", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(userForm)
        })
        // 处理响应
        if (res.status === 200) {
            const data = await res.json();
            if (res.ok === true) {
                ElMessage.success("操作成功");
                // 刷新用户列表
                fetchUserList();
                // 关闭弹窗
                dialogVisible.value = false;
            } else {
                ElMessage.error(data.msg || "操作失败");
            }
        } else {
            ElMessage.error("网络错误");
        }
    } catch (error) {
        // 表单验证失败
        return;
    }
};

// 删除操作
const handleDeleteUser = (row) => {
    if (row.role === 'ADMIN') {
        ElMessage.error('不能删除管理员用户');
        return;
    }

    ElMessageBox.confirm(
        `确定要删除用户【${row.username}】吗？此操作不可撤销。`,
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
            const res = await fetch("http://localhost:8081/catcate/users/deleteById", {
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
                    ElMessage.success("操作成功");
                    // 刷新用户列表
                    fetchUserList();
                    // 关闭弹窗
                    dialogVisible.value = false;
                    loading.value = false;
                }
            } else {
                ElMessage.error("网络错误");
            }
        } catch (error) {
            ElMessage.error('删除失败');
            loading.value = false;
        }
    });
};
</script>

<style scoped>
.user-management {
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

.user-form {
    margin-top: 10px;
}

/* 表单提示 */
.form-hint {
    font-size: 12px;
    color: #666;
    margin-top: 5px;
}

/* 表格行样式 */
::v-deep .el-table .row-inactive {
    background-color: rgba(245, 108, 108, 0.05);
    color: #999;
}

::v-deep .el-table .row-inactive .el-tag {
    opacity: 0.7;
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
    .el-table-column:not(.el-table-column--selection):not([label="用户ID"]):not([label="用户名"]):not([label="角色"]):not([label="状态"]):not([label="操作"]) {
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
</style>