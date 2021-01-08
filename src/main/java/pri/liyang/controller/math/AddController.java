package pri.liyang.controller.math;

import pri.liyang.entity.Response;
import pri.liyang.inter.BaseController;

import java.math.BigDecimal;
import java.util.Map;

public class AddController implements BaseController {
    @Override
    public Response handleGetRequest(Map<String, Object> param) {
        BigDecimal first = new BigDecimal(param.get("first").toString());
        BigDecimal second = new BigDecimal(param.get("second").toString());
        BigDecimal result = first.add(second);
        return Response.successWithData("success counted", result.doubleValue());
    }

    @Override
    public Response handlePostRequest(Map<String, Object> param) {
        return null;
    }
}
