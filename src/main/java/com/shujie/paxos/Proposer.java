package com.shujie.paxos;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p> description: 提案发布者
 * 使用paxos算法模拟leader选举
 * <p> 2019/06/04
 *
 * @author ssj
 * @version 1.0.0
 */
public class Proposer {

    /**
     * 当前编号,失败后自增
     */
    private AtomicInteger curNum = new AtomicInteger(0);
    private int acceptCount = 0;
    private int rejectCount = 0;

    /**
     * 众多acceptors返回的提案中编号最大的提案
     */
    private Proposal lastProposal;

    /**
     * 超时检测相关，当超过TIME_OUT时间没有接收到消息，重新发起提案
     */
    private ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    private static final int TIME_OUT = 5;

    /**
     * 模拟网络传输
     */
    private final TransportSimulator transport;

    final String id = UUID.randomUUID().toString().substring(0, 8);
    private volatile boolean finished = false;
    private boolean logEnabled = true;

    public Proposer(TransportSimulator simulator) {
        this.transport = simulator;
    }

    /**
     * 开始，包括重新开始，除了提案编号（curNum）自增之外，其他数据重置
     */
    public void start() {
        curNum.getAndIncrement();
        rejectCount = 0;
        acceptCount = 0;
        sendProposal();
    }

    /**
     * 接收到Acceptor响应信息后的回调
     *
     * @param response
     */
    public synchronized void onMessage(Response response) {

        if (!checkVersion(response)) {
            return;
        }

        // 如果收到消息后5秒，没有再收到消息，重新开始提案
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        scheduledFuture = scheduledExecutorService.schedule(this::start, TIME_OUT, TimeUnit.SECONDS);

        Proposal resProposal = response.getValue();

        // 取返回结果中，提案号最大的那个
        if (resProposal != null && resProposal.getNumber() >= curNum.get()) {
            lastProposal = resProposal;
        }

        if (response.getStatus() == Response.ACCEPT) {
            acceptCount++;
            // 半数批准，完成提案
            if (acceptCount >= getMajoritySize()) {
                finish();
            }
        } else {
            rejectCount++;
            // 半数驳回，重新开始提案
            if (rejectCount >= getMajoritySize()) {
                start();
            }
        }

    }

    /**
     * 检查acceptor响应是不是对应当前版本，这里的版本指的是当前希望被批准的提案编号（curNum）
     * 暂时没有考虑数据重复发送的问题。
     *
     * @return true 是当前版本 false 不是当前版本
     */
    private boolean checkVersion(Response response) {
        return response.getVersion() == curNum.get() && !finished;
    }

    /**
     * 提案完成
     */
    private void finish() {
        finished = true;
        scheduledFuture.cancel(true);
        log("提案流程结束！");
    }

    /**
     * 获取acceptors多数集合的大小
     */
    private int getMajoritySize() {
        int size = transport.getAcceptorSize();
        return size / 2 + 1;
    }

    /**
     * 发送提案
     */
    private void sendProposal() {
        Proposal proposal = new Proposal();
        proposal.setNumber(curNum.get());
        proposal.setOwner(id);
        if (lastProposal == null) {
            // 默认选自己
            proposal.setValue(this.id);
        } else {
            proposal.setValue(lastProposal.getValue());
        }
        log("申请提案：" + proposal);
        transport.notifiyAcceptors(proposal);
    }

    /**
     * 获取提案的值，这里只是简单打印
     */
    public void learn() {
        if (finished) {
            log("已经完成提案:" + lastProposal.getValue());
        }
    }

    private void log(String message) {
        if (logEnabled) {
            System.out.println("proposer[" + id + "]:" + message);
        }
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }
}
