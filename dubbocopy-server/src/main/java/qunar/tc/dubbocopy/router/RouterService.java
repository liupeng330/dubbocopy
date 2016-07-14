package qunar.tc.dubbocopy.router;

import qunar.tc.dubbocopy.api.model.Group;
import qunar.tc.dubbocopy.api.model.Router;
import qunar.tc.dubbocopy.request.DubboRequestInfo;

import java.util.List;

/**
 * @author song.xue created on 15/4/23
 * @version 1.0.0
 */
public interface RouterService extends qunar.tc.dubbocopy.api.service.RouterService {
    
    List<Group> selectGroups(DubboRequestInfo dubboRequestInfo);

    void refreshAllRouters(List<Router> routers);
}
