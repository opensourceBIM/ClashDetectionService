package org.bimserver.clashdetection;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ClashDetectionResults {

	private final List<Clash> clashes = new ArrayList<>();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	public void add(Clash clash) {
		clashes.add(clash);		
	}
	
	public List<Clash> getClashes() {
		return clashes;
	}
	
	public ArrayNode toJson() {
		ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
		for (Clash clash : clashes) {
			arrayNode.add(clash.toJson());
		}
		return arrayNode;
	}

	public int size() {
		return clashes.size();
	}
}
