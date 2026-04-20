import { ElMessage } from 'element-plus';

/**
 * 导出分类数据为CSV格式
 * @param {Array} categories - 分类列表
 */
export const exportToCSV = (categories) => {
    try {
        // 准备CSV数据
        const headers = ['分类ID', '分类名称', '分类代码', '父分类ID', '排序序号', '状态', '创建时间', '描述'];
        const rows = [];
        
        // 递归收集所有分类
        const collectCategories = (categories) => {
            categories.forEach(category => {
                rows.push([
                    category.id,
                    category.name,
                    category.code,
                    category.parentId || '',
                    category.sortOrder,
                    category.isActive === 1 ? '启用' : '禁用',
                    category.createTime || '',
                    category.description || ''
                ]);
                if (category.children && category.children.length > 0) {
                    collectCategories(category.children);
                }
            });
        };
        
        collectCategories(categories);
        
        // 生成CSV内容
        const csvContent = [
            headers.join(','),
            ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
        ].join('\n');
        
        // 创建Blob并下载
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.setAttribute('href', url);
        link.setAttribute('download', `分类数据_${new Date().toISOString().slice(0, 10)}.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        ElMessage.success('分类数据已导出为CSV文件');
    } catch (error) {
        console.error('导出CSV失败:', error);
        ElMessage.error('导出CSV失败: ' + error.message);
    }
};

/**
 * 导出分类数据为Excel格式
 * @param {Array} categories - 分类列表
 */
export const exportToExcel = (categories) => {
    try {
        // 准备Excel数据（使用简单的HTML表格格式）
        const headers = ['分类ID', '分类名称', '分类代码', '父分类ID', '排序序号', '状态', '创建时间', '描述'];
        const rows = [];
        
        // 递归收集所有分类
        const collectCategories = (categories) => {
            categories.forEach(category => {
                rows.push([
                    category.id,
                    category.name,
                    category.code,
                    category.parentId || '',
                    category.sortOrder,
                    category.isActive === 1 ? '启用' : '禁用',
                    category.createTime || '',
                    category.description || ''
                ]);
                if (category.children && category.children.length > 0) {
                    collectCategories(category.children);
                }
            });
        };
        
        collectCategories(categories);
        
        // 生成HTML表格
        let tableHtml = '<table border="1">';
        tableHtml += '<tr>';
        headers.forEach(header => {
            tableHtml += `<th>${header}</th>`;
        });
        tableHtml += '</tr>';
        
        rows.forEach(row => {
            tableHtml += '<tr>';
            row.forEach(cell => {
                tableHtml += `<td>${cell}</td>`;
            });
            tableHtml += '</tr>';
        });
        tableHtml += '</table>';
        
        // 创建Blob并下载
        const blob = new Blob([tableHtml], { type: 'application/vnd.ms-excel' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.setAttribute('href', url);
        link.setAttribute('download', `分类数据_${new Date().toISOString().slice(0, 10)}.xls`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        ElMessage.success('分类数据已导出为Excel文件');
    } catch (error) {
        console.error('导出Excel失败:', error);
        ElMessage.error('导出Excel失败: ' + error.message);
    }
};

/**
 * 为分类添加层级标识
 * @param {Array} categories - 分类列表
 * @param {number|null} parentId - 父分类ID
 * @param {number} level - 层级
 * @returns {Array} 处理后的分类列表
 */
export const addLevelToCategories = (categories, parentId = null, level = 0) => {
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
 * 更新统计信息
 * @param {Array} categories - 分类列表
 * @returns {Object} 统计信息
 */
export const updateStats = (categories) => {
    return {
        total: categories.length,
        active: categories.filter(cat => cat.isActive === 1).length,
        disabled: categories.filter(cat => cat.isActive === 0).length,
        parents: categories.filter(cat => !cat.parentId || cat.parentId === 0).length
    };
};

/**
 * 获取父分类名称
 * @param {Array} categories - 分类列表
 * @param {number} parentId - 父分类ID
 * @returns {string} 父分类名称
 */
export const getParentCategoryName = (categories, parentId) => {
    if (!parentId) return '无（一级分类）';
    
    // 递归查找父分类
    const findParent = (categories, id) => {
        for (const category of categories) {
            if (category.id === id) {
                return category.name;
            }
            if (category.children && category.children.length > 0) {
                const parent = findParent(category.children, id);
                if (parent) return parent;
            }
        }
        return '未知';
    };
    
    return findParent(categories, parentId);
};

/**
 * 获取分类标签类型
 * @param {string} code - 分类代码
 * @returns {string} 标签类型
 */
export const getCategoryTagType = (code) => {
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
 * 防抖函数
 * @param {Function} func - 要执行的函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 防抖处理后的函数
 */
export const debounce = (func, wait) => {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
};