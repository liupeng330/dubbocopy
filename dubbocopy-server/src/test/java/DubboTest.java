import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import qunar.tc.dubbocopy.api.model.Router;
import qunar.tc.dubbocopy.api.model.Target;
import qunar.tc.dubbocopy.api.service.RouterService;

import javax.annotation.Resource;

/**
 * @author song.xue created on 15/4/24
 * @version 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext-consumer.xml", "classpath:applicationContext-qconfig.xml"})
public class DubboTest {

    @Resource
    private RouterService routerService;

    @Test
    public void test() throws Exception {
        while (true) {
        	
            routerService.setRouter(new Router("qunar.tc.api.CalcService", "add", Sets.newHashSet(new Target("127.0.0.1", 20881))));
            Thread.sleep(50);
        }

    }
}
