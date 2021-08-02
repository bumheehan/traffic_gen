package kr.openbase.traffic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HeapDB {
  private static final Logger log = LoggerFactory.getLogger(HeapDB.class);
  private Map<String, SendRequest> reqMap = new HashMap<>();
  private Map<Integer, String> reqIdxMap = new HashMap<>();
  private ObjectMapper om = new ObjectMapper();

  public void updateData(String host, Integer status) {
    SendRequest sendRequest = reqMap.get(host);
    Map<String, Integer> xMap = sendRequest.getxMap();
    xMap.put("total", xMap.get("total") + 1);
    int s = status / 100;
    switch (s) {
      case 2:
        xMap.put("x2xx", xMap.get("x2xx") + 1);
        break;
      case 3:
        xMap.put("x3xx", xMap.get("x3xx") + 1);
        break;
      case 4:
        xMap.put("x4xx", xMap.get("x4xx") + 1);
        break;
      case 5:
        xMap.put("x5xx", xMap.get("x5xx") + 1);
        break;
      default:
        xMap.put("fail", xMap.get("fail") + 1);
        break;
    }

  }

  public String getList() {
    String retVal = "";
    try {
      retVal =
          om.writeValueAsString(reqMap.values().stream().sorted().collect(Collectors.toList()));
    } catch (JsonProcessingException e) {
      log.error("json error", e);
    }
    return retVal;
  }

  private int idx = 0;

  public void appendReq(SendRequest req) {
    if (reqMap.containsKey(req.getHost())) {
      log.info("이미 존재, host: {}", req.getHost());
      return;
    }
    log.info("req 추가 , Host : {}, 시간별 요청 횟수 :{}", req.getHost(), req.getHourlyCnt().toString());
    req.setIndex(idx++);
    //초기화
    Map<String, Integer> xMap = req.getxMap();
    xMap.put("total", 0);
    xMap.put("fail", 0);
    xMap.put("x2xx", 0);
    xMap.put("x3xx", 0);
    xMap.put("x4xx", 0);
    xMap.put("x5xx", 0);

    List<Integer> data = req.getHourlyCnt();

    WeightedObservedPoints obs = new WeightedObservedPoints();
    int addEndPointCnt = 3;
    int x = 1;
    for (int y : data) {
      if (x == 24) {
        obs.add(0, y);
      } else {
        obs.add(60 * x++, y);
      }
    }
    for (int i = 1; i <= addEndPointCnt; i++) {
      obs.add(60 * (i * -1), data.get(data.size() - i));
      obs.add(60 * x++, data.get(i - 1));
    }
    PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);

    double[] coeff = fitter.fit(obs.toList());
    req.setCoeff(coeff);

    reqMap.put(req.getHost(), req);
    reqIdxMap.put(req.getIndex(), req.getHost());

  }

  public void appendReq(String req) {

    List<SendRequest> list;
    try {
      list = om.readValue(req, new TypeReference<List<SendRequest>>() {});
      list.stream().forEach(s -> appendReq(s));
    } catch (Exception e) {
      log.error("json error", e);
    }
  }

  public void deleteReq(int idx) {
    String host = reqIdxMap.get(idx);
    log.info("req 삭제 , Host : {}", host);
    reqMap.remove(host);
    reqIdxMap.remove(idx);
  }

  public List<SendRequest> getReqList() {
    return new ArrayList<>(reqMap.values());
  }
}
