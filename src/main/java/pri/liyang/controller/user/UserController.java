package pri.liyang.controller.user;

import pri.liyang.entity.Response;
import pri.liyang.entity.User;
import pri.liyang.inter.ControllerAdapter;
import pri.liyang.repository.UserRepository;

import java.util.Map;

public class UserController extends ControllerAdapter {

    @Override
    public Response handleGetRequest(Map<String, Object> param) {
        if (!param.containsKey("operate")) {
            return Response.fail("missing operation type name");
        }

        String operate = param.get("operate").toString();
        User user = User.extractParamFromMap(param);

        switch (operate) {
            case "all":
                return Response.successOnlyData(UserRepository.getAllUser());
            case "one":
                return Response.successOnlyData(UserRepository.getUserById(user.getId()));
            case "add":
                UserRepository.addUser(user);
                return Response.success("add user success");
            case "update":
                UserRepository.updateUser(user);
                return Response.success("update user success");
            case "delete":
                UserRepository.deleteUser(user.getId());
                return Response.success("delete user success");
            default:
                return Response.fail("unknown operation: " + operate);
        }
    }

}
