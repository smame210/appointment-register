package com.study.common.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(203, "服务异常"),
    DATA_ERROR(204, "数据异常"),
    DATA_UPDATE_ERROR(205, "数据版本异常"),

    LOGIN_AUTH(208, "未登陆"),
    PERMISSION(209, "没有权限"),

    CODE_ERROR(210, "验证码错误"),
    //    LOGIN_MOBLE_ERROR(211, "账号不正确"),
    LOGIN_DISABLED_ERROR(212, "该用户已被禁用"),
    REGISTER_MOBLE_ERROR(213, "手机号已被使用"),
    LOGIN_AURH(214, "需要登录"),
    LOGIN_ACL(215, "没有权限"),

    URL_ENCODE_ERROR( 216, "URL编码失败"),
    ILLEGAL_CALLBACK_REQUEST_ERROR( 217, "非法回调请求"),
    FETCH_ACCESSTOKEN_FAILD( 218, "获取accessToken失败"),
    FETCH_USERINFO_ERROR( 219, "获取用户信息失败"),
    //LOGIN_ERROR( 23005, "登录失败"),

    PAY_RUN(220, "支付中"),
    GET_PAYMENTQRC_ERROR(221, "获取支付二维码出错"),
    CANCEL_ORDER_FAIL(225, "取消订单失败"),
    CANCEL_ORDER_NO(225, "不能取消预约"),

    HOSCODE_EXIST(230, "医院编号已经存在"),
    NUMBER_NO(240, "可预约号不足"),
    TIME_NO(250, "当前时间不可以预约"),

    SIGN_ERROR(300, "签名错误"),
    HOSPITAL_OPEN(310, "医院未开通，暂时不能访问"),
    HOSPITAL_LOCK(320, "医院被锁定，暂时不能访问"),

    BODY_NOT_MATCH(400,"请求的数据格式不符!"),
    SIGNATURE_NOT_MATCH(401,"请求的数字签名不匹配!"),
    NOT_FOUND(404, "未找到该资源!"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误!"),
    SERVER_BUSY(503,"服务器正忙，请稍后再试!")
    ;

    private Integer code;
    private String message;

    ResultCodeEnum(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}
