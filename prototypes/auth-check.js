// 通用权限检查脚本
// 用于在所有页面中根据用户角色控制菜单显示

// 当前登录用户（模拟）
// 在实际项目中，这个数据应该从 localStorage 或后端 API 获取
// 💡 测试方法：修改 roleIds 来模拟不同角色的登录
//    - roleIds: [1] → 超级管理员（可看到所有菜单）
//    - roleIds: [2] → 设备管理员（隐藏用户管理和角色管理）
let currentUser = { id: 1, username: 'admin', roleIds: [1] }; // 默认超级管理员

// 角色定义
const availableRoles = [
    { id: 1, name: '超级管理员', isSuperAdmin: true },
    { id: 2, name: '设备管理员', isSuperAdmin: false },
    { id: 3, name: '数据查看员', isSuperAdmin: false },
    { id: 4, name: '运维人员', isSuperAdmin: false }
];

// 检查当前用户是否为超级管理员
function isSuperAdmin() {
    return currentUser.roleIds.some(roleId => {
        const role = availableRoles.find(r => r.id === roleId);
        return role && role.isSuperAdmin;
    });
}

// 页面加载时自动执行菜单权限控制
document.addEventListener('DOMContentLoaded', function() {
    controlMenuPermission();
});

// 控制菜单显示权限
function controlMenuPermission() {
    const isSuper = isSuperAdmin();
    
    // 如果不是超级管理员，隐藏敏感菜单项
    if (!isSuper) {
        // 隐藏"用户管理"菜单
        const userMenuItem = document.querySelector('a[href="user-manage.html"]');
        if (userMenuItem) {
            userMenuItem.style.display = 'none';
        }
        
        // 隐藏"角色管理"菜单
        const roleMenuItem = document.querySelector('a[href="role-manage.html"]');
        if (roleMenuItem) {
            roleMenuItem.style.display = 'none';
        }
    }
}

// 导出函数供其他脚本使用
window.authCheck = {
    isSuperAdmin: isSuperAdmin,
    currentUser: currentUser,
    availableRoles: availableRoles
};
