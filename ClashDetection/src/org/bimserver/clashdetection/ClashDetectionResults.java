package org.bimserver.clashdetection;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ClashDetectionResults {

	private final Set<Clash> clashes = new HashSet<>();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	public void add(Clash clash) {
		clashes.add(clash);		
	}
	
	public Set<Clash> getClashes() {
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
