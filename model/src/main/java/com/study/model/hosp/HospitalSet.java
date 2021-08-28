package com.study.model.hosp;

import com.study.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 医院信息设置表
 * </p>
 *
 * @author smame210
 * @since 2021-08-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HospitalSet extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 医院名称
     */
    private String hosname;

    /**
     * 医院编号
     */
    private String hoscode;

    /**
     * api根路径
     */
    private String apiUrl;

    /**
     * 签名秘钥
     */
    private String signKey;

    /**
     * 联系人姓名
     */
    private String contactsName;

    /**
     * 联系人手机
     */
    private String contactsPhone;

    /**
     * 状态  1 使用 0 禁用
     */
    private Integer status;

}
