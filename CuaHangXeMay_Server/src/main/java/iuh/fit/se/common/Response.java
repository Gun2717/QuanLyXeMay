package iuh.fit.se.common;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;
    private String message;
    private Object data;

    public Response() {
    }

    public Response(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static Response success(String message, Object data) {
        return new Response(Constants.SUCCESS, message, data);
    }

    public static Response success(Object data) {
        return new Response(Constants.SUCCESS, "Success", data);
    }

    public static Response error(String message) {
        return new Response(Constants.ERROR, message);
    }

    public static Response unauthorized(String message) {
        return new Response(Constants.UNAUTHORIZED, message);
    }

    public static Response notFound(String message) {
        return new Response(Constants.NOT_FOUND, message);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return Constants.SUCCESS.equals(status);
    }

    @Override
    public String toString() {
        return "Response{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
