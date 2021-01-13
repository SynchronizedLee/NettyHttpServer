package pri.liyang.inter;

import pri.liyang.entity.Response;

import java.util.Map;

public class ControllerAdapter implements BaseController {

    private static final String NOT_IMPLEMENTED_MESSAGE = "Method not implemented";

    @Override
    public Response handleGetRequest(Map<String, Object> param) {
        return Response.fail(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public Response handlePostRequest(Map<String, Object> param) {
        return Response.fail(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public Response handlePutRequest(Map<String, Object> param) {
        return Response.fail(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public Response handleDeleteRequest(Map<String, Object> param) {
        return Response.fail(NOT_IMPLEMENTED_MESSAGE);
    }
}
