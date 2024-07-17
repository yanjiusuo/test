import axios from 'axios';
const service = axios.create({
    // baseURL: 'http://cjg.jd.com',
    withCredentials: true,
    headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'Access-Control-Allow-Private-Network': 'true',
    },
})
var req = {
    post:axios.post,
    get:axios.get
}

export default service;