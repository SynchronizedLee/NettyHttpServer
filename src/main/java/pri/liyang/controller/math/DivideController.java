package pri.liyang.controller.math;

import pri.liyang.entity.Response;
import pri.liyang.inter.BaseController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class DivideController implements BaseController {

    private static final Integer DEFAULT_SCALE = 6;

    @Override
    public Response handleGetRequest(Map<String, Object> param) {
        Object scaleObj = param.get("scale");
        Integer scale = null;

        if (scaleObj == null) {
            scale = DEFAULT_SCALE;
        } else {
            scale = Integer.parseInt((String) scaleObj);
        }

        BigDecimal first = new BigDecimal(param.get("first").toString());
        BigDecimal second = new BigDecimal(param.get("second").toString());
        BigDecimal result = first.divide(second, scale, RoundingMode.HALF_UP);
        return Response.successWithData("success counted", result.toString());
    }

    @Override
    public Response handlePostRequest(Map<String, Object> param) {
        return null;
    }
}
