export interface CommonResult<T>{
    code:number, // 编码
    message:string //
    data:T
}

export interface Page<T>{
    pageNo:number
    pageSize:number
    total:number
    data:T
}