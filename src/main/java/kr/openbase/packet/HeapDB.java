package kr.openbase.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	sendRequest.setTotalReq(sendRequest.getTotalReq()+1);
	int s = status / 100;
	switch (s) {
	case 2:
	    sendRequest.setX2xx(sendRequest.getX2xx() + 1);
	    break;
	case 3:
	    sendRequest.setX3xx(sendRequest.getX3xx() + 1);
	    break;
	case 4:
	    sendRequest.setX4xx(sendRequest.getX4xx() + 1);
	    break;
	case 5:
	    sendRequest.setX5xx(sendRequest.getX5xx() + 1);
	    break;
	default:
	    sendRequest.setXfail(sendRequest.getXfail() + 1);
	    break;
	}
    }

    public String getList() {
	String retVal = "";
	try {
	    retVal = om.writeValueAsString(reqMap.values());
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
	reqMap.put(req.getHost(), req);
	reqIdxMap.put(req.getIndex(), req.getHost());

    }

    public void appendReq(String req) {

	List<SendRequest> list;
	try {
	    list = om.readValue(req, new TypeReference<List<SendRequest>>() {
	    });
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
