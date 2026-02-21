<!-- 角色权限管理页面 -->
<template>
    <div class="permission-management">
        <!-- 页面标题和操作区 -->
        <div class="page-header">
            <h2>角色权限管理</h2>
            <el-button type="primary" @click="handleAddRole">
                <el-icon>
                    <Plus />
                </el-icon>
                <span>新增角色</span>
            </el-button>
        </div>

        <!-- 角色列表 -->
        <el-card class="role-list-card">
            <div class="table-toolbar">
                <el-input v-model="searchKeyword" placeholder="搜索角色名称或编码" prefix-icon="Search"
                    :style="{ width: '300px' }" @input="handleSearch"></el-input>
            </div>

            <el-table :data="filteredRoles" border stripe :header-cell-style="{ background: '#fff7ee' }"
                style="width: 100%; margin-top: 15px">
                <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
                <el-table-column prop="roleName" label="角色名称" align="center"></el-table-column>
                <el-table-column prop="roleCode" label="角色编码" align="center"></el-table-column>
                <el-table-column prop="description" label="角色描述" align="center"></el-table-column>
                <el-table-column prop="isActive" label="状态" width="100" align="center">
                    <template #default="scope">
                        <el-switch v-model="scope.row.isActive" active-value="1" inactive-value="0"
                            @change="handleStatusChange(scope.row)"></el-switch>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" align="center"></el-table-column>
                <el-table-column label="操作" width="220" align="center">
                    <template #default="scope">
                        <el-button size="small" type="primary" @click="handleAssignPermissions(scope.row)">
                            <el-icon>
                                <Key />
                            </el-icon>
                            分配权限
                        </el-button>
                        <el-button size="small" type="success" @click="handleEditRole(scope.row)">
                            <el-icon>
                                <Edit />
                            </el-icon>
                            编辑
                        </el-button>
                        <el-button size="small" type="danger" @click="handleDeleteRole(scope.row)"
                            v-if="!scope.row.isSystemRole">
                            <el-icon>
                                <Delete />
                            </el-icon>
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50]" :total="filteredRoles.length"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"></el-pagination>
            </div>
        </el-card>

        <!-- 新增/编辑角色弹窗 -->
        <el-dialog v-model="roleDialogVisible" :title="isEditRole ? '编辑角色' : '新增角色'" width="500px"
            @close="resetRoleForm">
            <el-form ref="roleFormRef" :model="roleForm" :rules="roleFormRules" label-width="100px">
                <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model="roleForm.roleName" placeholder="请输入角色名称"></el-input>
                </el-form-item>
                <el-form-item label="角色编码" prop="roleCode">
                    <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码（英文大写）"
                        :disabled="isEditRole && roleForm.isSystemRole"></el-input>
                    <div class="form-hint">角色编码用于权限控制，建议使用大写英文和冒号，如：ADMIN、USER</div>
                </el-form-item>
                <el-form-item label="角色描述" prop="description">
                    <el-input v-model="roleForm.description" placeholder="请输入角色描述" type="textarea" :rows="3"></el-input>
                </el-form-item>
                <el-form-item label="状态" prop="isActive">
                    <el-switch v-model="roleForm.isActive" active-value="1" inactive-value="0" active-text="启用"
                        inactive-text="禁用"></el-switch>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="roleDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitRoleForm">确定</el-button>
            </template>
        </el-dialog>

        <!-- 分配权限弹窗 -->
        <el-dialog v-model="permissionDialogVisible" title="分配权限" width="600px" max-height="70vh" overflow="auto">
            <div class="permission-dialog-header">
                <span class="role-info">当前角色：{{ currentRole.roleName }}（{{ currentRole.roleCode }}）</span>
                <el-button size="small" type="primary" @click="handleExpandAll">
                    {{ isAllExpanded ? '全部折叠' : '全部展开' }}
                </el-button>
            </div>

            <el-tree ref="permissionTree" :data="permissionTreeData" show-checkbox node-key="id"
                :default-expanded-keys="expandedKeys" :default-checked-keys="checkedKeys" :props="treeProps"
                @check="handlePermissionCheck" class="permission-tree"></el-tree>

            <template #footer>
                <el-button @click="permissionDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitPermissionAssign">保存权限</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, ElTree } from 'element-plus';
import { Plus, Edit, Delete, Key, Search } from '@element-plus/icons-vue';

// 路由实例
const router = useRouter();

// 角色列表数据
const roles = ref([]);
// 筛选后的角色列表
const filteredRoles = ref([]);
// 搜索关键词
const searchKeyword = ref('');
// 分页参数
const currentPage = ref(1);
const pageSize = ref(10);

// 权限树数据
const permissionTreeData = ref([]);
// 当前选中的角色
const currentRole = ref({});
// 权限树选中的节点
const checkedKeys = ref([]);
// 权限树展开的节点
const expandedKeys = ref([]);
// 是否全部展开
const isAllExpanded = ref(false);

// 角色弹窗状态
const roleDialogVisible = ref(false);
// 是否为编辑角色
const isEditRole = ref(false);
// 角色表单数据
const roleForm = reactive({
    id: '',
    roleName: '',
    roleCode: '',
    description: '',
    isActive: '1',
    isSystemRole: false
});
// 角色表单验证规则
const roleFormRules = reactive({
    roleName: [
        { required: true, message: '请输入角色名称', trigger: 'blur' },
        { min: 2, max: 50, message: '角色名称长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    roleCode: [
        { required: true, message: '请输入角色编码', trigger: 'blur' },
        { pattern: /^[A-Z_:]+$/, message: '角色编码只能包含大写字母、下划线和冒号', trigger: 'blur' },
        { min: 2, max: 50, message: '角色编码长度在 2 到 50 个字符', trigger: 'blur' }
    ]
});
// 角色表单引用
const roleFormRef = ref(null);

// 权限分配弹窗状态
const permissionDialogVisible = ref(false);

// 权限树配置
const treeProps = {
    children: 'children',
    label: 'permissionName',
    disabled: (data) => {
        // 系统级权限不可修改
        return data.isSystem;
    }
};

// 初始化
onMounted(() => {
    fetchRoles();
    fetchPermissions();
});

// 监听搜索关键词变化
watch(searchKeyword, (newVal) => {
    filterRoles(newVal);
});

// 获取角色列表
const fetchRoles = async () => {
    try {
        // 实际项目中替换为真实接口
        const response = await fetch('http://localhost:8081/catcate/roles');
        if (!response.ok) {
            throw new Error('获取角色列表失败');
        }
        const data = await response.json();
        // 模拟系统角色（不可删除）
        roles.value = data.map(role => ({
            ...role,
            isSystemRole: role.roleCode === 'SUPER_ADMIN' || role.roleCode === 'ADMIN'
        }));
        filterRoles(searchKeyword.value);
    } catch (error) {
        console.error('获取角色列表失败:', error);
        ElMessage.error('获取角色列表失败，请刷新页面重试');
    }
};

// 获取权限列表
const fetchPermissions = async () => {
    try {
        // 实际项目中替换为真实接口
        const response = await fetch('http://localhost:8081/catcate/permissions');
        if (!response.ok) {
            throw new Error('获取权限列表失败');
        }
        const data = await response.json();
        // 格式化权限为树形结构
        permissionTreeData.value = buildPermissionTree(data);
    } catch (error) {
        console.error('获取权限列表失败:', error);
        ElMessage.error('获取权限列表失败，请刷新页面重试');
    }
};

// 构建权限树形结构
const buildPermissionTree = (permissions) => {
    const tree = [];
    const permissionMap = {};

    // 先将所有权限存入Map
    permissions.forEach(permission => {
        permissionMap[permission.id] = {
            ...permission,
            children: []
        };
    });

    // 构建树形结构
    permissions.forEach(permission => {
        const current = permissionMap[permission.id];
        if (permission.parentId === null || !permissionMap[permission.parentId]) {
            // 没有父权限，作为根节点
            tree.push(current);
        } else {
            // 有父权限，添加到父权限的children中
            permissionMap[permission.parentId].children.push(current);
        }
    });

    return tree;
};

// 筛选角色
const filterRoles = (keyword) => {
    if (!keyword) {
        filteredRoles.value = [...roles.value];
        return;
    }

    const lowerKeyword = keyword.toLowerCase();
    filteredRoles.value = roles.value.filter(role =>
        role.roleName.toLowerCase().includes(lowerKeyword) ||
        role.roleCode.toLowerCase().includes(lowerKeyword)
    );
    // 筛选后重置当前页
    currentPage.value = 1;
};

// 搜索角色
const handleSearch = () => {
    filterRoles(searchKeyword.value);
};

// 分页大小变化
const handleSizeChange = (val) => {
    pageSize.value = val;
    currentPage.value = 1;
};

// 当前页变化
const handleCurrentChange = (val) => {
    currentPage.value = val;
};

// 新增角色
const handleAddRole = () => {
    isEditRole.value = false;
    resetRoleForm();
    roleDialogVisible.value = true;
};

// 编辑角色
const handleEditRole = (role) => {
    isEditRole.value = true;
    // 复制角色数据到表单
    Object.assign(roleForm, {
        id: role.id,
        roleName: role.roleName,
        roleCode: role.roleCode,
        description: role.description,
        isActive: role.isActive.toString(),
        isSystemRole: role.isSystemRole
    });
    roleDialogVisible.value = true;
};

// 删除角色
const handleDeleteRole = (role) => {
    ElMessageBox.confirm(
        `确定要删除角色"${role.roleName}"吗？删除后将无法恢复。`,
        '确认删除',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(async () => {
        try {
            // 实际项目中替换为真实接口
            const response = await fetch(`http://localhost:8081/catcate/roles/${role.id}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('删除角色失败');
            }

            ElMessage.success('角色删除成功');
            fetchRoles(); // 重新获取角色列表
        } catch (error) {
            console.error('删除角色失败:', error);
            ElMessage.error('删除角色失败，请重试');
        }
    });
};

// 更改角色状态
const handleStatusChange = async (role) => {
    try {
        // 实际项目中替换为真实接口
        const response = await fetch(`http://localhost:8081/catcate/roles/${role.id}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ isActive: role.isActive })
        });

        if (!response.ok) {
            // 状态更新失败，恢复原来的状态
            role.isActive = role.isActive === '1' ? '0' : '1';
            throw new Error('更新角色状态失败');
        }

        ElMessage.success(`角色已${role.isActive === '1' ? '启用' : '禁用'}`);
    } catch (error) {
        console.error('更新角色状态失败:', error);
        ElMessage.error('更新角色状态失败，请重试');
    }
};

// 分配权限
const handleAssignPermissions = async (role) => {
    currentRole.value = { ...role };
    permissionDialogVisible.value = true;

    try {
        // 实际项目中替换为真实接口
        const response = await fetch(`http://localhost:8081/catcate/roles/${role.id}/permissions`);
        if (!response.ok) {
            throw new Error('获取角色权限失败');
        }

        const data = await response.json();
        // 设置已选中的权限ID
        checkedKeys.value = data.map(item => item.permissionId);

        // 默认展开所有节点
        expandAllNodes();
    } catch (error) {
        console.error('获取角色权限失败:', error);
        ElMessage.error('获取角色权限失败，请重试');
    }
};

// 展开/折叠所有节点
const handleExpandAll = () => {
    if (isAllExpanded.value) {
        // 折叠所有节点
        expandedKeys.value = [];
    } else {
        // 展开所有节点
        expandAllNodes();
    }
    isAllExpanded.value = !isAllExpanded.value;
};

// 展开所有节点
const expandAllNodes = () => {
    const keys = [];
    const traverse = (nodes) => {
        nodes.forEach(node => {
            keys.push(node.id);
            if (node.children && node.children.length > 0) {
                traverse(node.children);
            }
        });
    };
    traverse(permissionTreeData.value);
    expandedKeys.value = keys;
};

// 权限勾选事件
const handlePermissionCheck = (checked, checkedNodes) => {
    // 可以在这里处理权限勾选逻辑
    console.log('Checked permissions:', checked, checkedNodes);
};

// 提交权限分配
const submitPermissionAssign = async () => {
    try {
        // 实际项目中替换为真实接口
        const response = await fetch(`http://localhost:8081/catcate/roles/${currentRole.value.id}/permissions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ permissionIds: checkedKeys.value })
        });

        if (!response.ok) {
            throw new Error('保存权限失败');
        }

        ElMessage.success('权限分配成功');
        permissionDialogVisible.value = false;
    } catch (error) {
        console.error('保存权限失败:', error);
        ElMessage.error('保存权限失败，请重试');
    }
};

// 提交角色表单
const submitRoleForm = async () => {
    // 表单验证
    if (!roleFormRef.value) return;
    const valid = await roleFormRef.value.validate();
    if (!valid) return;

    try {
        if (isEditRole.value) {
            // 编辑角色
            const response = await fetch(`http://localhost:8081/catcate/roles/${roleForm.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    roleName: roleForm.roleName,
                    roleCode: roleForm.roleCode,
                    description: roleForm.description,
                    isActive: roleForm.isActive
                })
            });

            if (!response.ok) {
                throw new Error('更新角色失败');
            }

            ElMessage.success('角色更新成功');
        } else {
            // 新增角色
            const response = await fetch('http://localhost:8081/catcate/roles', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    roleName: roleForm.roleName,
                    roleCode: roleForm.roleCode,
                    description: roleForm.description,
                    isActive: roleForm.isActive
                })
            });

            if (!response.ok) {
                throw new Error('创建角色失败');
            }

            ElMessage.success('角色创建成功');
        }

        roleDialogVisible.value = false;
        fetchRoles(); // 重新获取角色列表
    } catch (error) {
        console.error(isEditRole.value ? '更新角色失败:' : '创建角色失败:', error);
        ElMessage.error(`${isEditRole.value ? '更新' : '创建'}角色失败，请重试`);
    }
};

// 重置角色表单
const resetRoleForm = () => {
    if (roleFormRef.value) {
        roleFormRef.value.resetFields();
    }
    Object.assign(roleForm, {
        id: '',
        roleName: '',
        roleCode: '',
        description: '',
        isActive: '1',
        isSystemRole: false
    });
};
</script>

<style scoped>
.permission-management {
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

.page-header h2 {
    font-size: 20px;
    font-weight: 600;
    color: #1f2329;
    margin: 0;
}

.role-list-card {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.table-toolbar {
    padding: 15px 0;
    display: flex;
    justify-content: flex-start;
    align-items: center;
}

.pagination-container {
    margin-top: 15px;
    display: flex;
    justify-content: flex-end;
}

.form-hint {
    color: #666;
    font-size: 12px;
    margin-top: 5px;
}

.permission-dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
}

.role-info {
    font-weight: 500;
    color: #333;
}

.permission-tree {
    max-height: 500px;
    overflow-y: auto;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }

    .table-toolbar {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }

    .el-input {
        width: 100% !important;
    }

    .pagination-container {
        justify-content: center;
    }
}
</style>