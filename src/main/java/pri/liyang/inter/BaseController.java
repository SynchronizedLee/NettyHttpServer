package pri.liyang.inter;

import pri.liyang.entity.Response;

import java.util.Map;

public interface BaseController {

    Response handleGetRequest(Map<String, Object> param);

    Response handlePostRequest(Map<String, Object> param);

    Response handlePutRequest(Map<String, Object> param);

    Response handleDeleteRequest(Map<String, Object> param);

}
