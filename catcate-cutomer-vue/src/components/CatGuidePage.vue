<!-- 猫咪图鉴 -->
<template>
    <div class="cat-gallery-page">
        <!-- 页面头部 -->
        <div class="page-header">
            <h1>猫咪图鉴</h1>
            <p>认识我们店里的每一位毛孩子</p>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
            <el-row :gutter="20" align="middle">
                <el-col :span="6">
                    <el-input v-model="name" placeholder="搜索猫咪名字/品种..." prefix-icon="Search"
                        @keyup.enter="handleSearch"></el-input>
                </el-col>
                <el-col :span="4">
                    <el-select v-model="breedFilter" placeholder="选择品种" clearable @change="handleFilterChange">
                        <el-option label="全部品种" value=""></el-option>
                        <el-option v-for="breed in breedOptions" :key="breed" :label="breed" :value="breed"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="4">
                    <el-select v-model="genderFilter" placeholder="选择性别" clearable @change="handleFilterChange">
                        <el-option label="全部性别" value=""></el-option>
                        <el-option label="公猫" value="MALE"></el-option>
                        <el-option label="母猫" value="FEMALE"></el-option>
                    </el-select>
                </el-col>
                <el-col :span="4">
                    <el-select v-model="adoptionFilter" placeholder="领养状态" clearable @change="handleFilterChange">
                        <el-option label="全部状态" value=""></el-option>
                        <el-option label="可领养" value="AVAILABLE"></el-option>
                        <el-option label="已领养" value="ADOPTED"></el-option>
                        <el-option label="不可领养" value="UNAVAILABLE"></el-option>
                    </el-select>
                </el-col>
                <!-- 搜索按钮染色改为255, 152, 0色 -->
                <el-col :span="2">
                    <el-button type="primary" icon="Refresh" @click="getCatList"
                        style="background-color: rgb(255, 152, 0); border-color: rgb(255, 152, 0);padding-left: 0px;">搜索</el-button>
                </el-col>
                <el-col :span="2">
                    <el-button type="primary" icon="Refresh" @click="resetFilters"
                        style="background-color: rgb(255, 152, 0); border-color: rgb(255, 152, 0);padding-left: 0px;">重置</el-button>
                </el-col>
            </el-row>
        </div>

        <!-- 猫咪列表 -->
        <div class="cats-container">
            <div v-if="loading" class="loading-container">
                <el-skeleton :rows="8" animated></el-skeleton>
            </div>

            <div v-else-if="catList.length === 0" class="empty-container">
                <el-empty description="暂无猫咪数据" image-size="120">
                    <el-button type="primary" @click="getCatList">刷新数据</el-button>
                </el-empty>
            </div>

            <div v-else class="cats-grid">
                <el-card v-for="cat in catList" :key="cat.id" class="cat-card" shadow="hover"
                    @click="openCatDetail(cat)">
                    <div class="cat-img-container">
                        <!-- 改进图片显示逻辑 -->
                        <img :src="getCatImageUrl(cat.photoUrl)" :alt="cat.name" class="cat-img">
                        <!-- <div class="cat-tag" :class="getAdoptionTagClass(cat.adoptionStatus)">
                            {{ getAdoptionStatusText(cat.adoptionStatus) }}
                        </div> -->
                    </div>
                    <div class="cat-info">
                        <div class="cat-basic">
                            <h3>{{ cat.name }}</h3>
                            <span class="cat-breed">{{ cat.breed || '未知品种' }}</span>
                        </div>
                        <div class="cat-meta">
                            <div class="meta-item">
                                <el-icon>
                                    <User />
                                </el-icon>
                                {{ cat.gender === 'MALE' ? '公' : '母' }}
                            </div>
                            <div class="meta-item">
                                <el-icon>
                                    <Clock />
                                </el-icon>
                                {{ formatAge(cat.age) }}
                            </div>
                            <div class="meta-item">
                                <el-icon>
                                    <Heart />
                                </el-icon>
                                {{ cat.healthStatus === 'HEALTHY' ? '健康' : '需关注' }}
                            </div>
                        </div>
                        <div class="cat-personality">
                            <el-tag size="small" type="info">{{ cat.personality || '性格未知' }}</el-tag>
                        </div>
                    </div>
                </el-card>
            </div>
        </div>

        <!-- 分页控件 -->
        <div class="pagination-container" v-if="total > 0">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[8, 16, 24, 32]"
                :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                @current-change="handleCurrentChange"></el-pagination>
        </div>

        <!-- 猫咪详情弹窗 -->
        <el-dialog v-model="showDetailDialog" :title="currentCat.name || '猫咪详情'" width="800px" top="20px"
            :close-on-click-modal="false">
            <div v-if="currentCat" class="cat-detail">
                <el-row :gutter="20">
                    <el-col :span="10">
                        <div class="detail-img-container">
                            <img :src="currentCat.photoUrl || defaultCatImage" :alt="currentCat.name"
                                class="detail-img">
                        </div>
                    </el-col>
                    <el-col :span="14">
                        <div class="detail-info">
                            <el-descriptions :column="1" border>
                                <el-descriptions-item label="猫咪姓名">
                                    {{ currentCat.name }}
                                </el-descriptions-item>
                                <el-descriptions-item label="品种">
                                    {{ currentCat.breed || '未知品种' }}
                                </el-descriptions-item>
                                <el-descriptions-item label="性别">
                                    {{ currentCat.gender === 'MALE' ? '公猫' : '母猫' }}
                                </el-descriptions-item>
                                <el-descriptions-item label="年龄">
                                    {{ formatAge(currentCat.age) }}
                                </el-descriptions-item>
                                <el-descriptions-item label="健康状况">
                                    <el-tag :type="currentCat.healthStatus === 'HEALTHY' ? 'success' : 'warning'">
                                        {{ getHealthStatusText(currentCat.healthStatus) }}
                                    </el-tag>
                                </el-descriptions-item>
                                <!-- <el-descriptions-item label="领养状态">
                                    <el-tag :type="getAdoptionTagType(currentCat.adoptionStatus)">
                                        {{ getAdoptionStatusText(currentCat.adoptionStatus) }}
                                    </el-tag>
                                </el-descriptions-item> -->
                                <el-descriptions-item label="性格特点">
                                    {{ currentCat.personality || '暂无描述' }}
                                </el-descriptions-item>
                                <el-descriptions-item label="日常护理">
                                    {{ currentCat.dailyCare || '暂无特殊护理要求' }}
                                </el-descriptions-item>
                            </el-descriptions>

                            <div class="detail-actions" style="margin-top: 20px">
                                <el-button type="primary" icon="Calendar" @click="goToReservation(currentCat.id)"
                                    style="padding-left: 0%;">
                                    预约撸{{ currentCat.name }}
                                </el-button>
                                <!-- <el-button icon="Heart" type="success" v-if="currentCat.adoptionStatus === 'AVAILABLE'"
                                    style="padding-left: 0%;">
                                    咨询领养（可能不需要）
                                </el-button> -->
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </div>
        </el-dialog>
    </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Clock, Heart, Search, Refresh } from '@element-plus/icons-vue';

// 路由实例
const router = useRouter();

// 状态管理
const loading = ref(false);
const catList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(8);
const name = ref('');
const breedFilter = ref('');
const genderFilter = ref('');
const adoptionFilter = ref('');
const showDetailDialog = ref(false);
const currentCat = ref({});
// 使用本地默认图片作为后备
const defaultCatImage = 'https://placekitten.com/400/300';
// 品种选项（实际项目中可从接口获取）
const breedOptions = ref([
    '布偶猫', '英短', '美短', '橘猫', '加菲猫', '暹罗猫', '蓝猫', '渐层'
]);

// 页面加载时获取猫咪列表
onMounted(() => {
    getCatList();
});

// 获取猫咪列表
const getCatList = async () => {
    try {
        loading.value = true;

        // 构造查询参数
        const params = {
            pageNum: currentPage.value,
            pageSize: pageSize.value,
            name: name.value,
            breed: breedFilter.value,
            gender: genderFilter.value,
            adoptionStatus: adoptionFilter.value
        };

        // 调用真实API接口
        const response = await fetch('http://localhost:8083/catcatecutomer/cats/list', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(params)
        });

        const result = await response.json();

        // 检查响应格式并设置数据
        if (result && result.records) {
            // 处理图片URL
            catList.value = result.records.map(cat => ({
                ...cat,
                photoUrl: processPhotoUrl(cat.photoUrl)
            }));
            total.value = result.total || result.records.length;
        } else {
            catList.value = [];
            total.value = 0;
        }
        loading.value = false;

    } catch (error) {
        loading.value = false;
        ElMessage.error('获取猫咪列表失败，请稍后重试');
        console.error('获取猫咪列表失败：', error);
    }
};

// 处理图片URL的函数
const processPhotoUrl = (url) => {
    if (!url) {
        return null;
    }


    // 如果已经是完整URL，直接返回
    if (url.startsWith('http')) {
        return url;
    }

    // 处理不同类型的路径
    let processedUrl;

    if (url.startsWith('/uploads/cats/')) {
        // 已经是正确的上传路径格式 - 指向管理员端
        processedUrl = `http://localhost:8081${url}`;
    } else if (url.startsWith('uploads/cats/')) {
        // 缺少开头斜杠
        processedUrl = `http://localhost:8081/${url}`;
    } else if (url.startsWith('/cats/')) {
        // cats目录下的图片
        processedUrl = `http://localhost:8081${url}`;
    } else if (url.startsWith('cats/')) {
        // cats目录下的图片（缺少斜杠）
        processedUrl = `http://localhost:8081/${url}`;
    } else {
        // 其他情况，尝试作为相对路径处理
        processedUrl = `http://localhost:8081/${url}`;
    }

    return processedUrl;
};

// 获取猫咪图片URL（用于模板中）
const getCatImageUrl = (photoUrl) => {
    return processPhotoUrl(photoUrl);
};



// 搜索功能
const handleSearch = () => {
    currentPage.value = 1;
    getCatList();
};

// 筛选条件变化
const handleFilterChange = () => {
    currentPage.value = 1;
    getCatList();
};

// 重置筛选条件
const resetFilters = () => {
    name.value = '';
    breedFilter.value = '';
    genderFilter.value = '';
    adoptionFilter.value = '';
    currentPage.value = 1;
    getCatList();
};

// 分页大小变化
const handleSizeChange = (val) => {
    pageSize.value = val;
    getCatList();
};

// 页码变化
const handleCurrentChange = (val) => {
    currentPage.value = val;
    getCatList();
};

// 打开猫咪详情弹窗
const openCatDetail = (cat) => {
    currentCat.value = cat;
    showDetailDialog.value = true;
};

// 跳转到预约页面
const goToReservation = (catId) => {
    showDetailDialog.value = false;
    router.push({
        path: '/reservation-cat',
        query: { catId }
    });
};

// 格式化年龄（月转年+月）
const formatAge = (months) => {
    if (!months) return '未知';
    const years = Math.floor(months / 12);
    const remainMonths = months % 12;
    let result = '';
    if (years > 0) result += `${years}年`;
    if (remainMonths > 0) result += `${remainMonths}月`;
    return result || '1个月以下';
};

// // 获取领养状态文本
// const getAdoptionStatusText = (status) => {
//     const statusMap = {
//         AVAILABLE: '可领养',
//         ADOPTED: '已领养',
//         UNAVAILABLE: '不可领养'
//     };
//     return statusMap[status] || '未知状态';
// };

// 获取健康状态文本
const getHealthStatusText = (status) => {
    const statusMap = {
        HEALTHY: '健康',
        SICK: '生病',
        RECOVERING: '恢复中'
    };
    return statusMap[status] || '未知状态';
};

// 获取领养状态标签样式类
const getAdoptionTagClass = (status) => {
    const classMap = {
        AVAILABLE: 'tag-available',
        ADOPTED: 'tag-adopted',
        UNAVAILABLE: 'tag-unavailable'
    };
    return classMap[status] || '';
};

// 获取领养状态标签类型
const getAdoptionTagType = (status) => {
    const typeMap = {
        AVAILABLE: 'success',
        ADOPTED: 'info',
        UNAVAILABLE: 'warning'
    };
    return typeMap[status] || 'default';
};
</script>

<style scoped>
.cat-gallery-page {
    min-height: calc(100vh - 70px);
    background-color: #fff9f5;
    padding: 30px 40px;
}

/* 页面头部 */
.page-header {
    text-align: center;
    margin-bottom: 30px;
}

.page-header h1 {
    font-size: 32px;
    color: #5d4037;
    margin: 0 0 10px;
}

.page-header p {
    font-size: 16px;
    color: #795548;
    margin: 0;
}

/* 筛选栏 */
.filter-bar {
    background-color: white;
    padding: 20px;
    border-radius: 10px;
    margin-bottom: 30px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 猫咪列表容器 */
.cats-container {
    margin-bottom: 30px;
}

.loading-container,
.empty-container {
    padding: 40px 0;
}

.cats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 25px;
}

/* 猫咪卡片样式 */
.cat-card {
    border-radius: 15px;
    overflow: hidden;
    transition: all 0.3s ease;
    cursor: pointer;
    border: none;
    background-color: white;
}

.cat-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(255, 167, 71, 0.15);
}

.cat-img-container {
    position: relative;
    height: 220px;
}

.cat-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.cat-tag {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    color: white;
}

.tag-available {
    background-color: #4caf50;
}

.tag-adopted {
    background-color: #2196f3;
}

.tag-unavailable {
    background-color: #ff9800;
}

.cat-info {
    padding: 15px;
}

.cat-basic {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.cat-basic h3 {
    margin: 0;
    font-size: 18px;
    color: #5d4037;
}

.cat-breed {
    font-size: 12px;
    color: #795548;
    background-color: #fff3e0;
    padding: 2px 8px;
    border-radius: 8px;
}

.cat-meta {
    display: flex;
    gap: 15px;
    margin-bottom: 10px;
    font-size: 13px;
    color: #795548;
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
}

.cat-personality {
    margin-top: 10px;
}

/* 分页控件 */
.pagination-container {
    display: flex;
    justify-content: center;
    padding: 20px 0;
}

/* 详情弹窗样式 */
.cat-detail {
    padding: 10px 0;
}

.detail-img-container {
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-img {
    width: 100%;
    height: 400px;
    object-fit: cover;
}

.detail-info {
    height: 100%;
    display: flex;
    flex-direction: column;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .cat-gallery-page {
        padding: 20px;
    }

    .filter-bar {
        padding: 15px;
    }

    .cats-grid {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        gap: 15px;
    }

    .detail-img {
        height: 300px;
    }
}

@media (max-width: 480px) {
    .page-header h1 {
        font-size: 24px;
    }

    .filter-bar {
        padding: 10px;
    }

    .cats-grid {
        grid-template-columns: 1fr;
    }

    .detail-img {
        height: 200px;
    }
}
</style>