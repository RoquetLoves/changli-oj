package com.lk.luooj.model.dto.questionsubmit;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lk.luooj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {


    /**
     * 编程语言
     */
    private String language;


    /**
     * 题目id
     */
    private Long questionId;


    /**
     * 提交状态
     */
    private Integer status;

    /**
     * userId
     */
    private Long userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}