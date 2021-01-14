package pri.liyang.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;
import net.sf.json.JSONObject;
import pri.liyang.entity.Response;
import pri.liyang.inter.BaseController;
import pri.liyang.util.CacheUtils;
import pri.liyang.util.ConstantUtils;
import pri.liyang.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /*
     * 处理请求
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
        if (UriUtils.isInBlackList(fullHttpRequest.uri())) {
            FullHttpResponse httpResponse = createHttpResponse(Response.fail("Illegal uri: " + fullHttpRequest.uri()));
            channelHandlerContext.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        String classpath = ConstantUtils.CLASSPATH_PREFIX +
                UriUtils.parseUri(fullHttpRequest.uri()) +
                ConstantUtils.CLASSPATH_SUFFIX;

        BaseController controller = null;
        Response response = null;

        try {
            controller = CacheUtils.getControllerInstance(classpath);
        } catch (Exception e) {
            FullHttpResponse httpResponse = createHttpResponse(Response.fail("Controller instantiation error: " + e.toString()));
            channelHandlerContext.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        try {
            if (fullHttpRequest.method() == HttpMethod.GET) {
                Map<String, Object> param = getGetParamsFromChannel(fullHttpRequest);
                response = controller.handleGetRequest(param);
            } else if (fullHttpRequest.method() == HttpMethod.POST) {
                Map<String, Object> param = getPostParamsFromChannel(fullHttpRequest);
                response = controller.handlePostRequest(param);
            }

        } catch (Exception e) {
            response = Response.fail("Internal Server Error: " + e.toString());
        }

        // 发送响应
        FullHttpResponse httpResponse = createHttpResponse(response);
        channelHandlerContext.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    }

    /*
     * 获取GET方式传递的参数
     */
    private Map<String, Object> getGetParamsFromChannel(FullHttpRequest fullHttpRequest) {

        Map<String, Object> params = new HashMap<String, Object>();

        if (fullHttpRequest.method() == HttpMethod.GET) {
            // 处理get请求
            QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
            Map<String, List<String>> paramList = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                params.put(entry.getKey(), entry.getValue().get(0));
            }
            return params;
        } else {
            return null;
        }

    }

    /*
     * 获取POST方式传递的参数
     */
    private Map<String, Object> getPostParamsFromChannel(FullHttpRequest fullHttpRequest) {

        Map<String, Object> params = new HashMap<String, Object>();

        if (fullHttpRequest.method() == HttpMethod.POST) {
            // 处理POST请求
            String strContentType = fullHttpRequest.headers().get("Content-Type").trim();
            if (strContentType.contains("x-www-form-urlencoded")) {
                params  = getFormParams(fullHttpRequest);
            } else if (strContentType.contains("application/json")) {
                try {
                    params = getJSONParams(fullHttpRequest);
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            } else {
                return null;
            }
            return params;
        } else {
            return null;
        }
    }

    /*
     * 解析from表单数据（Content-Type = x-www-form-urlencoded）
     */
    private Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>();

        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();

        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }

        return params;
    }

    /*
     * 解析json数据（Content-Type = application/json）
     */
    private Map<String, Object> getJSONParams(FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        Map<String, Object> params = new HashMap<String, Object>();

        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, "UTF-8");

        JSONObject jsonParams = JSONObject.fromObject(strContent);
        for (Object key : jsonParams.keySet()) {
            params.put(key.toString(), jsonParams.get(key));
        }

        return params;
    }

    private FullHttpResponse createHttpResponse(Response response) {
        HttpVersion httpVersion = HttpVersion.HTTP_1_1;
        ByteBuf content = Unpooled.copiedBuffer(JSONObject.fromObject(response).toString(), CharsetUtil.UTF_8);
        HttpResponseStatus httpResponseStatus = getHttpResponseStatusByCode(response.getCode());

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpVersion, httpResponseStatus, content);
        fullHttpResponse.headers().set("Content-Type", "application/json;charset=UTF-8");
        fullHttpResponse.headers().set("Content_Length", fullHttpResponse.content().readableBytes());
        return fullHttpResponse;
    }

    /**
     * 根据状态码，返回对应的状态信息实例，默认返回500
     * @param code 数字状态码
     * @return 对应的HttpResponseStatus实例，默认返回500
     */
    private static HttpResponseStatus getHttpResponseStatusByCode(int code) {
        switch (code) {
            case 100:
                return HttpResponseStatus.CONTINUE;
            case 101:
                return HttpResponseStatus.SWITCHING_PROTOCOLS;
            case 102:
                return HttpResponseStatus.PROCESSING;
            case 200:
                return HttpResponseStatus.OK;
            case 201:
                return HttpResponseStatus.CREATED;
            case 202:
                return HttpResponseStatus.ACCEPTED;
            case 203:
                return HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION;
            case 204:
                return HttpResponseStatus.NO_CONTENT;
            case 205:
                return HttpResponseStatus.RESET_CONTENT;
            case 206:
                return HttpResponseStatus.PARTIAL_CONTENT;
            case 207:
                return HttpResponseStatus.MULTI_STATUS;
            case 300:
                return HttpResponseStatus.MULTIPLE_CHOICES;
            case 301:
                return HttpResponseStatus.MOVED_PERMANENTLY;
            case 302:
                return HttpResponseStatus.FOUND;
            case 303:
                return HttpResponseStatus.SEE_OTHER;
            case 304:
                return HttpResponseStatus.NOT_MODIFIED;
            case 305:
                return HttpResponseStatus.USE_PROXY;
            case 307:
                return HttpResponseStatus.TEMPORARY_REDIRECT;
            case 308:
                return HttpResponseStatus.PERMANENT_REDIRECT;
            case 400:
                return HttpResponseStatus.BAD_REQUEST;
            case 401:
                return HttpResponseStatus.UNAUTHORIZED;
            case 402:
                return HttpResponseStatus.PAYMENT_REQUIRED;
            case 403:
                return HttpResponseStatus.FORBIDDEN;
            case 404:
                return HttpResponseStatus.NOT_FOUND;
            case 405:
                return HttpResponseStatus.METHOD_NOT_ALLOWED;
            case 406:
                return HttpResponseStatus.NOT_ACCEPTABLE;
            case 407:
                return HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED;
            case 408:
                return HttpResponseStatus.REQUEST_TIMEOUT;
            case 409:
                return HttpResponseStatus.CONFLICT;
            case 410:
                return HttpResponseStatus.GONE;
            case 411:
                return HttpResponseStatus.LENGTH_REQUIRED;
            case 412:
                return HttpResponseStatus.PRECONDITION_FAILED;
            case 413:
                return HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE;
            case 414:
                return HttpResponseStatus.REQUEST_URI_TOO_LONG;
            case 415:
                return HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE;
            case 416:
                return HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
            case 417:
                return HttpResponseStatus.EXPECTATION_FAILED;
            case 421:
                return HttpResponseStatus.MISDIRECTED_REQUEST;
            case 422:
                return HttpResponseStatus.UNPROCESSABLE_ENTITY;
            case 423:
                return HttpResponseStatus.LOCKED;
            case 424:
                return HttpResponseStatus.FAILED_DEPENDENCY;
            case 425:
                return HttpResponseStatus.UNORDERED_COLLECTION;
            case 426:
                return HttpResponseStatus.UPGRADE_REQUIRED;
            case 428:
                return HttpResponseStatus.PRECONDITION_REQUIRED;
            case 429:
                return HttpResponseStatus.TOO_MANY_REQUESTS;
            case 431:
                return HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE;
            case 500:
                return HttpResponseStatus.INTERNAL_SERVER_ERROR;
            case 501:
                return HttpResponseStatus.NOT_IMPLEMENTED;
            case 502:
                return HttpResponseStatus.BAD_GATEWAY;
            case 503:
                return HttpResponseStatus.SERVICE_UNAVAILABLE;
            case 504:
                return HttpResponseStatus.GATEWAY_TIMEOUT;
            case 505:
                return HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED;
            case 506:
                return HttpResponseStatus.VARIANT_ALSO_NEGOTIATES;
            case 507:
                return HttpResponseStatus.INSUFFICIENT_STORAGE;
            case 510:
                return HttpResponseStatus.NOT_EXTENDED;
            case 511:
                return HttpResponseStatus.NETWORK_AUTHENTICATION_REQUIRED;
            default:
                return HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }
    }

}
