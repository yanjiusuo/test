/*
 * @Author: baoshengqiao baoshengqiao@jd.com
 * @Date: 2023-03-02 14:12:43
 * @LastEditors: baoshengqiao baoshengqiao@jd.com
 * @LastEditTime: 2023-03-02 14:30:50
 * @FilePath: /interface-debug-tool/src/utils/request.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import axios from 'axios'
// import qs from 'qs'
import { message } from 'antd'
import { RESPONSE_ERROR_STATUS_TIPS } from '@/utils/constants'

const service = axios.create({
  // baseURL: 'http://cjg.jd.com',
  withCredentials: true,
  headers: {
    'X-Requested-With': 'XMLHttpRequest',
    'Access-Control-Allow-Private-Network': 'true',
  },
})

service.interceptors.request.use(
  (config) => {
    if (config?.headers) {
      //@ts-ignore
      config.headers['sso-origin'] = 'up'
    }
    return config
  },
  (error) => {
    console.log(error)
    return Promise.reject(error)
  },
)
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 0) {
      // 京东登陆接入
      if (res.code === 1024) {
        window.location.href = `${process.env.JD_LOGIN_URL}?ReturnUrl=${encodeURIComponent(
          window.location.href,
        )}`
        return
      }
      message.error(res.message)
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    if (error && error.response) {
      // 1.公共错误处理
      // 2.根据响应码具体处理
      // let showText = ''
      if (error.response.status === 401) {
        // 读取响应头中location信息，若其中包含“/oidc/authorize”则认为该接口已做sso升级改造
        const { headers } = error.response
        const { location } = headers
        if (location && location.indexOf('/oidc/authorize') > 0) {
          window.location.href = location
          return
        }
        // 否则执行原有sso登陆逻辑
        window.location.href = `${process.env.ERP_LOGIN_URL}?ReturnUrl=${encodeURIComponent(
          window.location.href,
        )}`
        return
      }
      const showText = RESPONSE_ERROR_STATUS_TIPS[error.response.status]
        ? RESPONSE_ERROR_STATUS_TIPS[error.response.status]
        : '系统异常'
      message.error(showText)
    }
    /***** 处理结束 *****/
    return Promise.reject(error.response)
  },
)

export default service
