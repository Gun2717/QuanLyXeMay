package iuh.fit.se.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private Map<String, Object> data;
    private String token;

    public Request() {
        this.data = new HashMap<>();
    }

    public Request(String type) {
        this.type = type;
        this.data = new HashMap<>();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public void addData(String key, Object value) { this.data.put(key, value); }
    public Object getData(String key) { return this.data.get(key); }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getAction() {
        return this.type;
    }
}
