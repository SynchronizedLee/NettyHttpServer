package pri.liyang.entity;

public class Response {

    private Integer code;
    private String message;
    private Object data;

    public Integer getCode() {
        return code;
    }

    public Response setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Response setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Response setData(Object data) {
        this.data = data;
        return this;
    }

    public static Response success(String message) {
        return new Response().setCode(200).setMessage(message);
    }

    public static Response successWithData(String message, Object data) {
        return new Response().setCode(200).setMessage(message).setData(data);
    }

    public static Response fail(String message) {
        return new Response().setCode(500).setMessage(message);
    }

    public static Response userDefine(Integer code, String message, Object data) {
        return new Response().setCode(code).setMessage(message).setData(data);
    }

}
