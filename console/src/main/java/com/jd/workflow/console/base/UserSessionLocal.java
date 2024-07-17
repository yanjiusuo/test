package com.jd.workflow.console.base;


public class UserSessionLocal {

    private static final ThreadLocal<UserInfoInSession> LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> TICKET = new ThreadLocal<>();

    /**
     * 设置用户信息
     */
    public static void setUser(UserInfoInSession user) {
        LOCAL.set(user);
    }

    /**
     * 获取登录用户信息
     */
    public static UserInfoInSession getUser() {
        UserInfoInSession userInfoInSession = LOCAL.get();
        if(userInfoInSession == null){
            userInfoInSession = new UserInfoInSession();
        }
        return userInfoInSession;
    }

    /**
     * 清除缓存信息
     */
    public static void removeUser() {
        LOCAL.remove();
    }

    /**
     * 设置用户信息
     */
    public static void setTicket(String ticket) {
        TICKET.set(ticket);
    }

    /**
     * 获取登录用户信息
     */
    public static String getTicket() {
        return TICKET.get();
    }

    /**
     * 清除缓存信息
     */
    public static void removeTicket() {
        TICKET.remove();
    }

}
