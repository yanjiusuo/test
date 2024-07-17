export interface UserAddDto{
    userName: string// 用户名称
    userCode: string// 用户编码
}
export interface User{
    id : number,//应用id
    userName: string// 用户名称
    userCode: string// 用户编码
    desc:string[]
    anyType:any[]
}