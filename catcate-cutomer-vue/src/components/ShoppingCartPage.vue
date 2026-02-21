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

        <div class="cart-container" v-if="cartItems.length > 0">
            <el-table :data="cartItems" @selection-change="handleSelectionChange">
                <el-table-column type="selection"></el-table-column>
                <el-table-column label="商品信息">
                    <template #default="scope">
                        <div class="product-info">
                            <img :src="scope.row.product.imageUrl || defaultImage" 
                                 class="product-image"
                                 @error="handleImageError">
                            <span>{{ scope.row.product.name }}</span>
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
                                         @change="updateCartItemQuantity(scope.row)"></el-input-number>
                    </template>
                </el-table-column>
                <el-table-column label="小计">
                    <template #default="scope">¥{{ (Number(scope.row.product.price) * scope.row.quantity).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column label="操作">
                    <template #default="scope">
                        <el-button type="danger" link @click="removeItem(scope.row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="cart-summary">
                <div class="summary-row">
                    <span>已选 {{ selectedItems.length }} 件商品</span>
                    <span>总价：<strong class="total-price">¥{{ totalPrice.toFixed(2) }}</strong></span>
                </div>
                <el-button type="primary" size="large" @click="checkout" :disabled="selectedItems.length === 0"
                    :loading="checkoutLoading">
                    去结算
                </el-button>
            </div>
        </div>

        <div class="empty-cart" v-else>
            <el-empty description="购物车为空">
                <el-button type="primary" @click="$router.push('/ProductPage')">去逛逛</el-button>
            </el-empty>
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
import { api } from '../utils/api'
import { getUserId, isLoggedIn } from '../utils/auth'

const router = useRouter()
const cartItems = ref([])
const selectedItems = ref([])
const checkoutLoading = ref(false)
const showPaymentDialog = ref(false)
const paymentLoading = ref(false)
const paymentAmount = ref(0)
const orderNumber = ref('')
const selectedPaymentMethod = ref('ALIPAY')
const defaultImage = 'https://picsum.photos/seed/product/300/300'

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
        // 从后端获取真实的购物车数据
        const response = await api.get('/shopping-cart/list')
        
        console.log('购物车接口响应:', response)
        
        if (response && response.code === 200 && Array.isArray(response.data)) {
            // 获取所有商品的详细信息
            const productDetails = await loadProductDetails()
            
            // 将购物车数据与商品详细信息合并
            cartItems.value = response.data.map(item => {
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
            
            console.log('购物车数据加载成功:', cartItems.value)
        } else {
            // 如果没有购物车数据，使用空数组
            cartItems.value = []
        }
    } catch (error) {
        console.error('加载购物车失败:', error)
        ElMessage.error('加载购物车失败: ' + (error.message || '未知错误'))
        
        // 出错时使用模拟数据（仅用于开发测试）
        cartItems.value = [
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
            }
        ]
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
        
        // 直接使用fetch确保请求体正确发送
        const response = await fetch('http://localhost:8083/catcatecutomer/shopping-cart/update', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(requestData)
        });
        
        const result = await response.json();
        console.log('购物车更新响应:', result);
        
        if (result.code === 200) {
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

const removeItem = async (itemId) => {
    try {
        await ElMessageBox.confirm('确定要删除这个商品吗？', '确认删除', {
            type: 'warning'
        })

        // 删除购物车项的API调用
        const response = await api.delete(`/shopping-cart/remove/${itemId}`)
        
        if (response && response.code === 200) {
            cartItems.value = cartItems.value.filter(item => item.id !== itemId)
            selectedItems.value = selectedItems.value.filter(item => item.id !== itemId)
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

.empty-cart {
    text-align: center;
    padding: 60px 20px;
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