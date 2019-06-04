package com.shujie.paxos;

import java.util.UUID;

/**
 * <p> description: Acceptor 提案审批者
 * <p> 2019/06/04
 *
 * @author ssj
 * @version 1.0.0
 */
public class Acceptor {
    /**
     * 已经批准的编号最大的提案
     */
    private Proposal lastProposal;
    /**
     * Acceptor id
     */
    private final String id = UUID.randomUUID().toString().substring(0, 8);
    /**
     * 模拟网络发送
     */
    private TransportSimulator transport;

    private boolean logEnabled = true;

    public Acceptor(TransportSimulator transport) {
        this.transport = transport;
    }

    /**
     * 接收到Proposer提案的回调
     * @param proposal
     */
    public synchronized void onMessage(Proposal proposal) {

        // 首次提交，通过
        if (lastProposal == null) {
            accept(proposal);
            return;
        }

        // 提案号小，驳回
        if (lastProposal.getNumber() >= proposal.getNumber()) {
            reject(proposal);
            return;
        }

        // 提案号大，通过
        accept(proposal);
    }

    private void reject(Proposal proposal) {
        Response response = new Response();
        response.setStatus(Response.REJECT);
        response.setValue(lastProposal);
        response.setVersion(proposal.getNumber());
        transport.notifiyProposer(proposal.getOwner(), response);
        log("驳回提案："+proposal);
    }

    private void accept(Proposal proposal) {
        lastProposal = proposal;
        Response response = new Response();
        response.setStatus(Response.ACCEPT);
        response.setValue(lastProposal);
        response.setVersion(proposal.getNumber());
        transport.notifiyProposer(proposal.getOwner(), response);
        log("通过提案："+proposal);
    }


    private void log(String message) {
        if (logEnabled) {
            System.out.println("acceptor["+id + "]:" + message);
        }
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }
}
