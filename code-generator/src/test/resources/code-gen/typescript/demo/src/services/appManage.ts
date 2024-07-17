import {AppAddDto,App} from "../type/app";
import {CommonResult,Page} from "../common/type";
import request from '../utils/request';
import {ONLINE_URL} from "../utils/constants";


/**
 * 添加应用
 * @param appAddDto
 */
export async function add(appAddDto:AppAddDto):Promise<CommonResult<number>{
    return  request.post(`${ONLINE_URL}/app/add`,appAddDto );
}

export async function list(params:{
    pageNo:number,
    pageSize:number
}):Promise<CommonResult<Page<App>{
    return request.get(`${ONLINE_URL}/app/list`,params);
}

export async function getById(params:{
   id:number
}):Promise<CommonResult<App>{
    return request.get(`${ONLINE_URL}/app/getById`,params);
}