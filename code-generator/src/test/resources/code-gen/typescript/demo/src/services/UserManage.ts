import {UserAddDto,User} from "../type/user";
import {CommonResult,Page} from "../common/type";

export async function add(param:UserAddDto):Promise<CommonResult<number>{
    return null;
}

export async function list(params:{
    pageNo ? :number,//分页编号
    pageSize:number // 分页size
}):Promise<CommonResult<Page<Record<User>>{
    return null;
}

export async function getById(params:{
    id:number
}):Promise<CommonResult<User>{
    return null;
}
