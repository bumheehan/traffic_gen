package kr.openbase.packet;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpMethod;

public class SendRequest {
    private int index;
    private String host;
    private HttpMethod method = HttpMethod.GET;
    private List<Integer> hourlyCnt = new ArrayList<>();
    private int errPer; //percent
    private int totalReq;
    private int x2xx;
    private int x3xx;
    private int x4xx;
    private int x5xx;
    private int xfail;
    private boolean run;

    public int getIndex() {
	return index;
    }

    public void setIndex(int index) {
	this.index = index;
    }

    public String getHost() {
	return host;
    }

    public void setHost(String host) {
	this.host = host;
    }

    public List<Integer> getHourlyCnt() {
	return hourlyCnt;
    }

    public void setHourlyCnt(List<Integer> hourlyCnt) {
	this.hourlyCnt = hourlyCnt;
    }

    public int getX2xx() {
	return x2xx;
    }

    public void setX2xx(int x2xx) {
	this.x2xx = x2xx;
    }

    public int getX3xx() {
	return x3xx;
    }

    public void setX3xx(int x3xx) {
	this.x3xx = x3xx;
    }

    public int getX4xx() {
	return x4xx;
    }

    public void setX4xx(int x4xx) {
	this.x4xx = x4xx;
    }

    public int getX5xx() {
	return x5xx;
    }

    public void setX5xx(int x5xx) {
	this.x5xx = x5xx;
    }

    public int getXfail() {
	return xfail;
    }

    public void setXfail(int xfail) {
	this.xfail = xfail;
    }

    public HttpMethod getMethod() {
	return method;
    }

    public void setMethod(HttpMethod method) {
	this.method = method;
    }

    public int getErrPer() {
        return errPer;
    }

    public void setErrPer(int errPer) {
        this.errPer = errPer;
    }

    public int getTotalReq() {
        return totalReq;
    }

    public void setTotalReq(int totalReq) {
        this.totalReq = totalReq;
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }
    

}
