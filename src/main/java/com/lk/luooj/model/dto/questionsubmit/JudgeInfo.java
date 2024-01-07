package com.lk.luooj.model.dto.questionsubmit;

import lombok.Data;

/**
 * 题目提交
 */
@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗的内存
     */
    private Long memory;


    /**
     * 消耗的时间
     */
    private Long time;
}
