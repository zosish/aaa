<!-- 购物车页面 -->
<template>
    <div class="shopping-cart-page">
        <div class="page-header">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
                <el-breadcrumb-item>购物车</el-breadcrumb-item>
            </el-breadcrumb>
            <h1>我的购物车</h1>
        </div>

        <div v-if="loading" class="loading-cart">
            <el-skeleton :rows="5" animated />
        </div>
        
        <div class="cart-container" v-else-if="cartItems.length > 0">
            <!-- 操作栏 -->
            <div class="cart-actions">
                <el-checkbox v-model="selectAll" @change="handleSelectAll" :disabled="loading">全选</el-checkbox>
                <el-button type="danger" link @click="batchDelete" :disabled="selectedItems.length === 0 || loading" :loading="loading">
                    批量删除
                </el-button>
                <el-button type="warning" link @click="clearCart" :disabled="loading" :loading="loading">
                    清空购物车
                </el-button>
                <div class="cart-sort">
                    <span>排序：</span>
                    <el-select v-model="sortBy" size="small" @change="handleSort" :disabled="loading">
                        <el-option label="默认" value="default"></el-option>
                        <el-option label="价格从低到高" value="price-asc"></el-option>
                        <el-option label="价格从高到低" value="price-desc"></el-option>
                        <el-option label="数量从多到少" value="quantity-desc"></el-option>
                    </el-select>
                </div>
            </div>
            
            <el-table :data="cartItems" @selection-change="handleSelectionChange">
                <el-table-column type="selection"></el-table-column>
                <el-table-column label="商品信息">
                    <template #default="scope">
                        <div class="product-info" @click="goToProductDetail(scope.row.product.id)">
                            <img :src="scope.row.product.imageUrl || defaultImage" 
                                 class="product-image"
                                 @error="handleImageError">
                            <span class="product-name">{{ scope.row.product.name }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="product.price" label="单价">
                    <template #default="scope">¥{{ Number(scope.row.product.price).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column label="数量">
                    <template #default="scope">
                        <el-input-number v-model="scope.row.quantity" 
                                         :min="1" 
                                         :max="scope.row.product.stockQuantity || 999"
                                         size="small" 
                                         @change="updateCartItemQuantity(scope.row)"
                                         :disabled="loading"></el-input-number>
                    </template>
                </el-table-column>
                <el-table-column label="小计">
                    <template #default="scope">¥{{ (Number(scope.row.product.price) * scope.row.quantity).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column label="库存状态">
                    <template #default="scope">
                        <el-tag :type="scope.row.product.stockQuantity > 10 ? 'success' : scope.row.product.stockQuantity > 0 ? 'warning' : 'danger'" size="small">
                            {{ scope.row.product.stockQuantity > 10 ? '库存充足' : scope.row.product.stockQuantity > 0 ? '库存紧张' : '已售罄' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template #default="scope">
                        <el-button type="danger" link @click="removeItem(scope.row.id)" :disabled="loading">删除</el-button>
                        <el-button type="primary" link @click="goToProductDetail(scope.row.product.id)" :disabled="loading">查看详情</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="cart-summary">
                <div class="summary-row">
                    <span>已选 {{ selectedItems.length }} 件商品</span>
                    <span>总价：<strong class="total-price">¥{{ totalPrice.toFixed(2) }}</strong></span>
                </div>
                <el-button type="primary" size="large" @click="checkout" :disabled="selectedItems.length === 0 || loading"
                    :loading="checkoutLoading || loading">
                    去结算
                </el-button>
            </div>
        </div>

        <div v-if="loading" class="loading-cart">
            <el-skeleton :rows="5" animated />
        </div>
        <div class="empty-cart" v-else-if="cartItems.length === 0">
            <div class="empty-cart-content">
                <div class="empty-cart-icon">
                    <el-icon class="empty-icon"><ShoppingCart /></el-icon>
                </div>
                <h3>购物车还是空的</h3>
                <p>去挑选一些可爱的猫咪用品吧</p>
                <div class="empty-cart-actions">
                    <el-button type="primary" @click="$router.push('/ProductPage')">去逛逛</el-button>
                    <el-button @click="$router.push('/AppointmentCatsPage')">预约撸猫</el-button>
                </div>
                <div class="empty-cart-recommendations">
                    <h4>热门推荐</h4>
                    <div class="recommendation-list">
                        <div class="recommendation-item" @click="$router.push('/ProductDetailPage?id=1')">
                            <img src="https://picsum.photos/seed/recommend1/100/100" alt="推荐商品">
                            <span>优质猫粮</span>
                        </div>
                        <div class="recommendation-item" @click="$router.push('/ProductDetailPage?id=2')">
                            <img src="https://picsum.photos/seed/recommend2/100/100" alt="推荐商品">
                            <span>猫咪玩具</span>
                        </div>
                        <div class="recommendation-item" @click="$router.push('/ProductDetailPage?id=3')">
                            <img src="https://picsum.photos/seed/recommend3/100/100" alt="推荐商品">
                            <span>猫咪窝</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 地址输入对话框 -->
        <el-dialog v-model="showAddressDialog" title="填写收货地址" width="500px" :before-close="handleAddressClose">
            <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="100px">
                <el-form-item label="收货人" prop="receiverName">
                    <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名"></el-input>
                </el-form-item>
                <el-form-item label="联系电话" prop="phone">
                    <el-input v-model="addressForm.phone" placeholder="请输入联系电话"></el-input>
                </el-form-item>
                <el-form-item label="详细地址" prop="address">
                    <el-input 
                        v-model="addressForm.address" 
                        type="textarea" 
                        :rows="3" 
                        placeholder="请输入详细收货地址">
                    </el-input>
                </el-form-item>
                <el-form-item label="设为默认">
                    <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="showAddressDialog = false">取消</el-button>
                    <el-button type="primary" @click="confirmAddress" :loading="addressLoading">确定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 支付对话框 -->
        <el-dialog v-model="showPaymentDialog" title="确认支付" width="500px" :before-close="handlePaymentClose">
            <div class="payment-info">
                <p><strong>订单总额：</strong><span class="amount">¥{{ paymentAmount.toFixed(2) }}</span></p>
                <p><strong>订单编号：</strong>{{ orderNumber }}</p>
            </div>

            <div class="payment-methods">
                <el-radio-group v-model="selectedPaymentMethod">
                    <el-radio label="ALIPAY" border>
                        <div class="payment-option">
                            <span>支付宝支付</span>
                        </div>
                    </el-radio>
                </el-radio-group>
            </div>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="showPaymentDialog = false">取消</el-button>
                    <el-button type="primary" @click="confirmPayment" :loading="paymentLoading">
                        确认支付
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>
<!-- eslint-disable no-unused-vars -->

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { api } from '../utils/api'
import { getUserId, isLoggedIn } from '../utils/auth'

const router = useRouter()
const cartItems = ref([])
const originalCartItems = ref([])
const selectedItems = ref([])
const selectAll = ref(false)
const checkoutLoading = ref(false)
const showPaymentDialog = ref(false)
const paymentLoading = ref(false)
const paymentAmount = ref(0)
const orderNumber = ref('')
const selectedPaymentMethod = ref('ALIPAY')
const defaultImage = 'https://picsum.photos/seed/product/300/300'
const loading = ref(false)
const sortBy = ref('default')

// 地址相关状态
const showAddressDialog = ref(false)
const addressLoading = ref(false)
const addressFormRef = ref(null)

// 地址表单数据
const addressForm = reactive({
    receiverName: '',
    phone: '',
    address: '',
    isDefault: false
})

// 地址表单验证规则
const addressRules = {
    receiverName: [
        { required: true, message: '请输入收货人姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
    ],
    phone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    address: [
        { required: true, message: '请输入详细地址', trigger: 'blur' },
        { min: 5, message: '地址长度至少5个字符', trigger: 'blur' }
    ]
}

const totalPrice = computed(() => {
    return selectedItems.value.reduce((total, item) => {
        return total + (Number(item.product.price) * item.quantity)
    }, 0)
})

// 处理图片加载错误
const handleImageError = (event) => {
    event.target.src = defaultImage
}

// 处理地址对话框关闭
const handleAddressClose = (done) => {
    ElMessageBox.confirm('确定要取消填写地址吗？')
        .then(() => {
            done()
        })
        .catch(() => {
            // 用户点击了取消
        })
}

// 确认地址并执行购买
const confirmAddress = async () => {
    try {
        // 表单验证
        await addressFormRef.value.validate()
        
        addressLoading.value = true
        
        // 构造完整地址
        const fullAddress = `${addressForm.address} (收货人: ${addressForm.receiverName}, 电话: ${addressForm.phone})`
        
        // 创建订单
        const orderData = {
            userId: getUserId(),
            totalAmount: totalPrice.value,
            shippingAddress: fullAddress,
            customerNotes: addressForm.isDefault ? '默认地址' : '',
            items: selectedItems.value.map(item => ({
                productId: item.productId,
                productName: item.product.name,
                productPrice: Number(item.product.price),
                quantity: item.quantity,
                subtotal: Number(item.product.price) * item.quantity,
                itemType: 'PRODUCT'
            }))
        }

        console.log('准备创建订单的数据:', orderData)

        // 调用创建订单API
        const orderResponse = await api.post('/orders/create', orderData)

        console.log('购物车订单响应:', orderResponse)

        // 检查响应结构是否正确
        if (orderResponse && orderResponse.code === 200 && orderResponse.data && orderResponse.data.orderNumber) {
            // 关闭地址对话框
            showAddressDialog.value = false
            
            orderNumber.value = orderResponse.data.orderNumber
            paymentAmount.value = orderResponse.data.totalAmount || totalPrice.value
            showPaymentDialog.value = true
        } else {
            // 详细的错误信息
            const errorMessage = orderResponse?.message || '创建订单失败'
            throw new Error(errorMessage)
        }

    } catch (error) {
        if (error.field) {
            // 表单验证错误
            console.error('地址填写验证失败:', error)
        } else {
            // 其他错误
            console.error('结算失败:', error)
            ElMessage.error('结算失败：' + (error.message || '未知错误'))
        }
    } finally {
        addressLoading.value = false
    }
}

onMounted(() => {
    // 检查登录状态
    if (!isLoggedIn()) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }
    
    loadCartItems()
})

const loadCartItems = async () => {
    try {
        loading.value = true
        
        // 从后端获取真实的购物车数据
        const response = await api.get('/shopping-cart/list')
        
        console.log('购物车接口响应:', response)
        
        if (response && response.code === 200 && Array.isArray(response.data)) {
            // 获取所有商品的详细信息
            const productDetails = await loadProductDetails()
            
            // 将购物车数据与商品详细信息合并
            originalCartItems.value = response.data.map(item => {
                const productDetail = productDetails.find(p => p.id === item.productId) || {
                    id: item.productId,
                    name: '未知商品',
                    price: 0,
                    imageUrl: defaultImage,
                    stockQuantity: 0
                }
                
                return {
                    id: item.id,
                    productId: item.productId,
                    quantity: item.quantity,
                    product: productDetail
                }
            })
            
            console.log('购物车数据加载成功:', originalCartItems.value)
        } else {
            // 如果没有购物车数据，使用空数组
            originalCartItems.value = []
        }
        
        // 应用排序
        handleSort()
        
        // 重置选择状态
        selectedItems.value = []
        selectAll.value = false
    } catch (error) {
        console.error('加载购物车失败:', error)
        ElMessage.error('加载购物车失败: ' + (error.message || '未知错误'))
        
        // 出错时使用模拟数据（仅用于开发测试）
        originalCartItems.value = [
            {
                id: 1,
                productId: 1,
                quantity: 2,
                product: {
                    id: 1,
                    name: '优质猫粮',
                    price: 89.9,
                    imageUrl: 'https://picsum.photos/seed/product1/300/300',
                    stockQuantity: 50
                }
            },
            {
                id: 2,
                productId: 2,
                quantity: 1,
                product: {
                    id: 2,
                    name: '猫咪玩具',
                    price: 29.9,
                    imageUrl: 'https://picsum.photos/seed/product2/300/300',
                    stockQuantity: 20
                }
            },
            {
                id: 3,
                productId: 3,
                quantity: 3,
                product: {
                    id: 3,
                    name: '猫咪窝',
                    price: 129.9,
                    imageUrl: 'https://picsum.photos/seed/product3/300/300',
                    stockQuantity: 10
                }
            }
        ]
        
        // 应用排序
        handleSort()
        
        // 重置选择状态
        selectedItems.value = []
        selectAll.value = false
    } finally {
        loading.value = false
    }
}

// 加载商品详细信息的辅助函数
const loadProductDetails = async () => {
    try {
        const response = await fetch('http://localhost:8083/catcatecutomer/products/list', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                current: 1,
                size: 9999
            })
        })
        
        const result = await response.json()
        console.log('商品列表响应:', result)
        
        if (result && result.code === 200 && result.data && result.data.records) {
            return result.data.records
        }
        return []
    } catch (error) {
        console.error('加载商品详情失败:', error)
        return []
    }
}

const handleSelectionChange = (selection) => {
    selectedItems.value = selection
    // 更新全选状态
    selectAll.value = selectedItems.value.length === cartItems.value.length && cartItems.value.length > 0
}

const handleSelectAll = (value) => {
    selectAll.value = value
    if (value) {
        // 全选所有商品
        selectedItems.value = [...cartItems.value]
    } else {
        // 取消全选
        selectedItems.value = []
    }
}

const updateCartItemQuantity = async (item) => {
    try {
        // 正确的方式：通过请求体发送数据
        const requestData = {
            cartId: item.id,
            quantity: item.quantity
        };
        
        console.log('发送购物车更新请求:', {
            url: '/shopping-cart/update',
            data: requestData
        });
        
        // 使用api工具函数发送请求，确保正确的认证信息
        const result = await api.put('/shopping-cart/update', requestData);
        
        console.log('购物车更新响应:', result);
        
        if (result.code === 200) {
            // 更新本地原始数据
            const index = originalCartItems.value.findIndex(i => i.id === item.id);
            if (index !== -1) {
                originalCartItems.value[index].quantity = item.quantity;
            }
            // 重新应用排序
            handleSort();
            ElMessage.success('数量更新成功');
        } else {
            throw new Error(result.message || '更新失败');
        }
    } catch (error) {
        console.error('更新数量失败:', error);
        ElMessage.error('更新数量失败: ' + (error.message || '未知错误'));
        loadCartItems(); // 恢复原数量
    }
};

const batchDelete = async () => {
    if (selectedItems.value.length === 0) return
    
    try {
        await ElMessageBox.confirm('确定要删除选中的商品吗？', '确认删除', {
            type: 'warning'
        })
        
        loading.value = true
        
        // 获取选中商品的ID列表
        const itemIds = selectedItems.value.map(item => item.id)
        
        // 批量删除API调用
        const response = await api.delete('/shopping-cart/batch-remove', {
            data: { ids: itemIds }
        })
        
        if (response && response.code === 200) {
            // 更新原始数据
            originalCartItems.value = originalCartItems.value.filter(item => !itemIds.includes(item.id))
            // 重新应用排序
            handleSort()
            ElMessage.success('批量删除成功')
        } else {
            throw new Error(response?.message || '删除失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('批量删除失败:', error)
            ElMessage.error('批量删除失败: ' + (error.message || '未知错误'))
        }
    } finally {
        loading.value = false
    }
}

const clearCart = async () => {
    try {
        await ElMessageBox.confirm('确定要清空购物车吗？', '确认清空', {
            type: 'warning'
        })
        
        loading.value = true
        
        // 清空购物车API调用
        const response = await api.delete('/shopping-cart/clear')
        
        if (response && response.code === 200) {
            // 清空原始数据
            originalCartItems.value = []
            // 重新应用排序
            handleSort()
            ElMessage.success('购物车已清空')
        } else {
            throw new Error(response?.message || '清空失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('清空购物车失败:', error)
            ElMessage.error('清空购物车失败: ' + (error.message || '未知错误'))
        }
    } finally {
        loading.value = false
    }
}

const goToProductDetail = (productId) => {
    router.push(`/product/${productId}`)
}

const handleSort = () => {
    // 复制原始数据进行排序
    const sortedItems = [...originalCartItems.value]
    
    switch (sortBy.value) {
        case 'price-asc':
            // 价格从低到高
            sortedItems.sort((a, b) => Number(a.product.price) - Number(b.product.price))
            break
        case 'price-desc':
            // 价格从高到低
            sortedItems.sort((a, b) => Number(b.product.price) - Number(a.product.price))
            break
        case 'quantity-desc':
            // 数量从多到少
            sortedItems.sort((a, b) => b.quantity - a.quantity)
            break
        default:
            // 默认排序（保持原始顺序）
            break
    }
    
    cartItems.value = sortedItems
    
    // 重新选择已选商品
    selectedItems.value = selectedItems.value.filter(item => 
        cartItems.value.some(cartItem => cartItem.id === item.id)
    )
    
    // 更新全选状态
    selectAll.value = selectedItems.value.length === cartItems.value.length && cartItems.value.length > 0
}

const removeItem = async (itemId) => {
    try {
        await ElMessageBox.confirm('确定要删除这个商品吗？', '确认删除', {
            type: 'warning'
        })

        // 删除购物车项的API调用
        const response = await api.delete(`/shopping-cart/remove/${itemId}`)
        
        if (response && response.code === 200) {
            // 更新原始数据
            originalCartItems.value = originalCartItems.value.filter(item => item.id !== itemId)
            // 重新应用排序
            handleSort()
            ElMessage.success('删除成功')
        } else {
            throw new Error(response?.message || '删除失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除失败:', error)
            ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
    }
}

const checkout = async () => {
    if (selectedItems.value.length === 0) return

    // 显示地址填写对话框
    showAddressDialog.value = true
    
    // 重置表单数据
    addressForm.receiverName = ''
    addressForm.phone = ''
    addressForm.address = ''
    addressForm.isDefault = false
}

const confirmPayment = async () => {
    if (!orderNumber.value) {
        ElMessage.error('订单信息不完整')
        return
    }

    paymentLoading.value = true

    try {
        // 调用支付宝支付接口
        const response = await api.post('/payment/alipay/create', {
            orderNumber: orderNumber.value,
            amount: paymentAmount.value,
            subject: '猫咪用品订单'
        })

        // 检查响应类型并正确处理
        if (typeof response === 'string' && response.includes('<form')) {
            // 支付宝返回的是HTML表单，直接在新窗口打开
            const paymentWindow = window.open('', '_blank', 'width=800,height=600')
            paymentWindow.document.write(response)
            paymentWindow.document.close()

            // 关闭支付对话框
            showPaymentDialog.value = false

            ElMessage.success('正在跳转到支付页面...')
            
            // 支付成功后清空购物车
            setTimeout(() => {
                cartItems.value = []
                selectedItems.value = []
            }, 2000)
            
            // 监听支付窗口关闭事件，跳转到支付成功页面
            const checkPaymentWindow = setInterval(() => {
                if (paymentWindow.closed) {
                    clearInterval(checkPaymentWindow);
                    // 跳转到支付成功页面
                    router.push(`/PaymentSuccessfulPage?orderNumber=${orderNumber.value}`);
                }
            }, 1000);
        } else if (response && response.code === 200) {
            // 如果是JSON响应，处理data字段
            const paymentHtml = response.data
            if (typeof paymentHtml === 'string' && paymentHtml.includes('<form')) {
                const paymentWindow = window.open('', '_blank', 'width=800,height=600')
                paymentWindow.document.write(paymentHtml)
                paymentWindow.document.close()
                showPaymentDialog.value = false
                ElMessage.success('正在跳转到支付页面...')
                setTimeout(() => {
                    cartItems.value = []
                    selectedItems.value = []
                }, 2000)
            } else {
                throw new Error('支付表单格式不正确')
            }
        } else {
            // 处理错误响应
            const errorMessage = response?.message || response?.error || '发起支付失败'
            throw new Error(errorMessage)
        }
    } catch (error) {
        console.error('支付失败:', error)
        ElMessage.error('支付失败：' + (error.message || '未知错误'))
    } finally {
        paymentLoading.value = false
    }
}

const handlePaymentClose = (done) => {
    ElMessageBox.confirm('确定要取消支付吗？')
        .then(() => {
            done()
        })
        .catch(() => {
            // 用户点击了取消
        })
}
</script>

<style scoped>
.shopping-cart-page {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 30px;
}

.page-header h1 {
    margin: 10px 0;
    color: #333;
}

.cart-container {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cart-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #e0e0e0;
    flex-wrap: wrap;
    gap: 10px;
}

.cart-sort {
    display: flex;
    align-items: center;
    gap: 10px;
}

.product-info {
    display: flex;
    align-items: center;
    gap: 15px;
}

.product-image {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 4px;
}

.cart-summary {
    margin-top: 20px;
    padding: 20px;
    background: #f5f5f5;
    border-radius: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.summary-row {
    display: flex;
    gap: 30px;
    align-items: center;
}

.total-price {
    font-size: 24px;
    color: #ff6b35;
}

.loading-cart {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    margin: 20px 0;
}

.empty-cart {
    text-align: center;
    padding: 60px 20px;
}

.empty-cart-content {
    max-width: 600px;
    margin: 0 auto;
}

.empty-cart-icon {
    margin-bottom: 20px;
}

.empty-icon {
    font-size: 80px;
    color: #e0e0e0;
}

.empty-cart h3 {
    margin: 0 0 10px 0;
    font-size: 20px;
    color: #333;
}

.empty-cart p {
    margin: 0 0 30px 0;
    color: #999;
}

.empty-cart-actions {
    display: flex;
    justify-content: center;
    gap: 15px;
    margin-bottom: 40px;
    flex-wrap: wrap;
}

.empty-cart-recommendations {
    margin-top: 40px;
    padding-top: 30px;
    border-top: 1px solid #e0e0e0;
}

.empty-cart-recommendations h4 {
    margin: 0 0 20px 0;
    font-size: 16px;
    color: #333;
}

.recommendation-list {
    display: flex;
    justify-content: center;
    gap: 30px;
    flex-wrap: wrap;
}

.recommendation-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
    padding: 15px;
    border-radius: 8px;
}

.recommendation-item:hover {
    background: #f5f5f5;
    transform: translateY(-2px);
}

.recommendation-item img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 4px;
}

.recommendation-item span {
    font-size: 14px;
    color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .shopping-cart-page {
        padding: 10px;
    }
    
    .cart-actions {
        flex-direction: column;
        align-items: flex-start;
    }
    
    .cart-summary {
        flex-direction: column;
        align-items: stretch;
        gap: 15px;
    }
    
    .summary-row {
        justify-content: space-between;
    }
    
    .product-info {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }
    
    .product-image {
        width: 80px;
        height: 80px;
    }
    
    .empty-cart-actions {
        flex-direction: column;
        align-items: center;
    }
    
    .recommendation-list {
        gap: 20px;
    }
    
    .recommendation-item img {
        width: 60px;
        height: 60px;
    }
}

.payment-info {
    margin-bottom: 20px;
}

.amount {
    color: #ff6b35;
    font-size: 18px;
    font-weight: bold;
}

.payment-methods {
    margin: 20px 0;
}

.payment-option {
    display: flex;
    align-items: center;
    gap: 10px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}
</style>