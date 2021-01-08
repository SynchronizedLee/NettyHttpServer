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
        HttpResponseStatus httpResponseStatus = null;

        if (response.getCode() == 200) {
            httpResponseStatus = HttpResponseStatus.OK;
        } else {
            httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpVersion, httpResponseStatus, content);
        fullHttpResponse.headers().set("Content-Type", "application/json;charset=UTF-8");
        fullHttpResponse.headers().set("Content_Length", fullHttpResponse.content().readableBytes());
        return fullHttpResponse;
    }

}
