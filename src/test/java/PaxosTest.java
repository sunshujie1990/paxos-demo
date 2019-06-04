import com.shujie.paxos.Acceptor;
import com.shujie.paxos.Proposer;
import com.shujie.paxos.TransportSimulator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p> description: PaxosTest
 * <p> 2019/06/04
 *
 * @author ssj
 * @version 1.0.0
 */
public class PaxosTest {
    // acceptor 数目
    private static final int ACC_NUM = 5;
    // proposer 数目
    private static final int PRO_NUM = 5;

    private List<Proposer> proposerList = new ArrayList<>(PRO_NUM);
    private List<Acceptor> acceptorList = new ArrayList<>(ACC_NUM);

    private ExecutorService pool = Executors.newCachedThreadPool();

    @Before
    public void prepare() {
        TransportSimulator transportSimulator =
                new TransportSimulator(acceptorList, proposerList);

        for (int i = 0; i< ACC_NUM; i++) {
            Acceptor acceptor = new Acceptor(transportSimulator);
            acceptorList.add(acceptor);
        }
        for (int i = 0; i< PRO_NUM; i++) {
            Proposer proposer = new Proposer(transportSimulator);
            proposerList.add(proposer);
        }
    }

    @Test
    public void test01() throws InterruptedException {
        // 多线程模拟Proposer开始提案
        proposerList.forEach(it->{
            pool.execute(it::start);
        });

        // 不停的尝试获取Proposer提案结果
        while (true) {
            proposerList.forEach(Proposer::learn);
            TimeUnit.SECONDS.sleep(3);
        }
    }

}
