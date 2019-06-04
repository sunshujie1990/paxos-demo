package com.shujie.paxos;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p> description: 模拟网络链接
 * <p> 2019/06/04
 *
 * @author ssj
 * @version 1.0.0
 */
public class TransportSimulator {

    private List<Acceptor> acceptors;
    private List<Proposer> proposers;
    private Random random = new Random();
    private ExecutorService pool = Executors.newFixedThreadPool(10);

    public TransportSimulator(List<Acceptor> acceptors, List<Proposer> proposers) {
        this.acceptors = acceptors;
        this.proposers = proposers;
    }

    /**
     * 获取有多少个Acceptor
     * @return
     */
    public int getAcceptorSize() {
        return acceptors.size();
    }

    /**
     * 发送提案给acceptors
     * @param proposal
     */
    public void notifiyAcceptors(Proposal proposal) {
        acceptors.forEach(it -> {
            pool.execute(() -> {
                if (checkNetWork()) {
                    it.onMessage(proposal);
                }
            });
        });
    }

    /**
     * 发送响应给某一个proposer
     * @param response
     */
    public void notifiyProposer(String proposerId, Response response) {
        proposers.stream()
                .filter(proposer -> proposer.id.equals(proposerId))
                .forEach(proposer -> {
                    pool.execute(() -> {
                        if (checkNetWork()) {
                            proposer.onMessage(response);
                        }
                    });
                });
    }

    /**
     * 模拟网络失败与延迟，可以调节这里的设置观察选举过程，网络状况越差选举花费的时间越长
     * @return true 网络正常 false 网络无法连通
     */
    private boolean checkNetWork() {
        try {
            // 消息延迟3秒内
            int sleep = random.nextInt(3000);
            TimeUnit.MICROSECONDS.sleep(sleep);
            // 30%可能性消息丢失
            int access = random.nextInt(10);
            return access >= 2;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }
}
