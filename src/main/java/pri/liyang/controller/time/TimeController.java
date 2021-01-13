package pri.liyang.controller.time;

import pri.liyang.entity.Response;
import pri.liyang.inter.ControllerAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class TimeController extends ControllerAdapter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Response handleGetRequest(Map<String, Object> param) {
        String currentTime = DATE_FORMAT.format(new Date());
        return Response.successWithData("get time success", currentTime);
    }

}
