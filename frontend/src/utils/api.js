// API 配置和工具函数
const API_BASE_URL = 'http://localhost:8080';

class ApiClient {
  constructor() {
    this.baseURL = API_BASE_URL;
    this.token = localStorage.getItem('token');
  }

  // 设置 token
  setToken(token) {
    this.token = token;
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }

  // 获取 token
  getToken() {
    return this.token || localStorage.getItem('token');
  }

  // 发送请求
  async request(url, options = {}) {
    const token = this.getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers,
    };

    try {
      const response = await fetch(`${this.baseURL}${url}`, config);
      const data = await response.json();

      // 处理 token 过期
      if (data.code === 401) {
        this.setToken(null);
        window.location.href = '/login.html';
        throw new Error('登录已过期，请重新登录');
      }

      return data;
    } catch (error) {
      console.error('API请求失败:', error);
      throw error;
    }
  }

  // GET 请求
  async get(url) {
    return this.request(url, { method: 'GET' });
  }

  // POST 请求
  async post(url, data) {
    return this.request(url, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // PUT 请求
  async put(url, data) {
    return this.request(url, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  // DELETE 请求
  async delete(url, data) {
    return this.request(url, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }
}

// 创建单例
const api = new ApiClient();

// 导出 API 方法
export const authApi = {
  // 登录
  login: (username, password) => api.post('/auth/login', { username, password }),
  
  // 退出登录
  logout: () => {
    api.setToken(null);
    localStorage.removeItem('userInfo');
    window.location.href = '/login.html';
  },
};

export const deviceApi = {
  // 设备列表
  list: (params = {}) => api.post('/devices/list', params),
  
  // 设备详情
  detail: (deviceCode) => api.post('/devices/detail', { deviceCode }),
  
  // 创建设备
  create: (data) => api.post('/devices', data),
  
  // 更新设备
  update: (data) => api.post('/devices/update', data),
  
  // 删除设备
  delete: (deviceCode) => api.post('/devices/delete', { deviceCode }),
  
  // 最新数据
  latestData: (deviceCode) => api.post('/devices/latest-data', { deviceCode }),
};

export const deviceDataApi = {
  // 历史数据
  list: (params) => api.post('/device-data/list', params),
};

export const productApi = {
  // 产品列表
  list: (params = {}) => api.post('/products/list', params),
  
  // 产品详情
  detail: (id) => api.post('/products/detail', { id }),
};

export const commandApi = {
  // 下发命令
  send: (deviceCode, commands) => api.post('/commands/send', { deviceCode, commands }),
  
  // 产品命令列表
  productCommands: (productId) => api.post('/commands/product', { productId }),
};

export const groupApi = {
  // 分组树
  tree: () => api.post('/device-groups/tree'),
  
  // 分组列表
  list: () => api.post('/device-groups/list'),
};

export default api;
