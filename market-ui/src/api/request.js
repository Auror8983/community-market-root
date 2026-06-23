import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api', // 指向后端网关端口
  timeout: 10000
})

// 响应拦截器
request.interceptors.response.use(
  res => {
    if (res.data.code === 200) {
      return res.data.data
    } else {
      ElMessage.error(res.data.message || '系统错误')
      return Promise.reject(new Error(res.data.message))
    }
  },
  error => {
    console.error('API Error:', error)
    ElMessage.error(error.message || '网络连接失败')
    return Promise.reject(error)
  }
)

export default request