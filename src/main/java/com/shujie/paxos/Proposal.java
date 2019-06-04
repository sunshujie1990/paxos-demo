package com.shujie.paxos;

import lombok.Data;

/**
 * <p> description: 提案实体类
 * <p> 2019/06/04
 *
 * @author ssj
 * @version 1.0.0
 */
@Data
public class Proposal {
    /**
     * 编号
     */
    private int number;
    /**
     * 提案值，因为模拟leader选举，这里指的是leader的id
     */
    private Object value;
    /**
     * 提案提交人id
     */
    private String owner;

}
