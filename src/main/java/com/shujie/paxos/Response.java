package com.shujie.paxos;

import lombok.Data;

/**
 * <p> description: Acceptor -> Proposer 的响应信息实体类
 * <p> 2019/06/04
 *
 * @author ssj
 * @version 1.0.0
 */
@Data
public class Response {
    /**
     * 拒绝
     */
    public static final int REJECT = -1;

    /**
     * 接受
     */
    public static final int ACCEPT = 0;

    /**
     * 状态，包含REJECT与ACCEPT
     */
    private int status;

    /**
     * 响应的版本，和提案的number相同
     */
    private int version;

    /**
     * 返回值，已经批准的编号最大的提案
     */
    private Proposal value;

}
