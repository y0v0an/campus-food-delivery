import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 可以在这里添加token等认证信息
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo) {
            try {
                const user = JSON.parse(userInfo)
                if (user && user.id) {
                    config.headers['X-User-Id'] = user.id
                }
            } catch (e) {
                console.error('解析用户信息失败:', e)
            }
        }
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)


// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === 200) {
            return res.data
        } else {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
    },
    error => {
        console.error('响应错误:', error)
        const { response } = error
        if (response) {
            switch (response.status) {
                case 401:
                    ElMessage.error('未登录或登录已过期')
                    break
                case 403:
                    ElMessage.error('权限不足')
                    break
                case 404:
                    ElMessage.error('资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(response.data?.message || '请求失败')
            }
        } else {
            ElMessage.error('网络连接失败')
        }
        return Promise.reject(error)
    }
)

export default request
