package kr.openbase.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpMethod;

public class SendRequest implements Comparable<SendRequest> {
  private int index;
  private String host;
  private HttpMethod method = HttpMethod.GET;
  private List<Integer> hourlyCnt = new ArrayList<>();
  private int errPer; //percent
  private boolean run;
  private Map<String, Integer> xMap = new ConcurrentHashMap<>();
  private double[] coeff;

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

  public boolean isRun() {
    return run;
  }

  public void setRun(boolean run) {
    this.run = run;
  }

  public Map<String, Integer> getxMap() {
    return xMap;
  }

  public void setxMap(Map<String, Integer> xMap) {
    this.xMap = xMap;
  }


  public double[] getCoeff() {
    return coeff;
  }

  public void setCoeff(double[] coeff) {
    this.coeff = coeff;
  }

  @Override
  public int compareTo(SendRequest o) {
    return Integer.compare(this.index, o.index);
  }


}
