package org.bimserver.clashdetection;

import org.bimserver.emf.IdEObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Clash {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private IdEObject ifcProduct1;
	private IdEObject ifcProduct2;
	private long oid1;
	private long oid2;

	public Clash(IdEObject ifcProduct1, IdEObject ifcProduct2) {
		this.ifcProduct1 = ifcProduct1;
		this.ifcProduct2 = ifcProduct2;
		if (ifcProduct1.getOid() == ifcProduct2.getOid()) {
			throw new RuntimeException("Clashes cannot happen within the same object in this implementation");
		}
		// Make the Clash itself canonical by always putting the objects with the lowest oid first
		// This works because the hashCode/equals methods only rely on the oids
		if (ifcProduct1.getOid() < ifcProduct2.getOid()) {
			this.oid1 = ifcProduct1.getOid();
			this.oid2 = ifcProduct2.getOid();
		} else {
			this.oid1 = ifcProduct2.getOid();
			this.oid2 = ifcProduct1.getOid();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (oid1 ^ (oid1 >>> 32));
		result = prime * result + (int) (oid2 ^ (oid2 >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Clash other = (Clash) obj;
		if (oid1 != other.oid1)
			return false;
		if (oid2 != other.oid2)
			return false;
		return true;
	}

	public IdEObject getIfcProduct1() {
		return ifcProduct1;
	}
	
	public IdEObject getIfcProduct2() {
		return ifcProduct2;
	}

	public JsonNode toJson() {
		ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
		ObjectNode object1Node = OBJECT_MAPPER.createObjectNode();
		ObjectNode object2Node = OBJECT_MAPPER.createObjectNode();
		
		EStructuralFeature guidFeature = ifcProduct1.eClass().getEStructuralFeature("GlobalId");
		EStructuralFeature nameFeature = ifcProduct1.eClass().getEStructuralFeature("Name");
		
		object1Node.put("guid", (String)ifcProduct1.eGet(guidFeature));
		object2Node.put("guid", (String)ifcProduct2.eGet(guidFeature));

		object1Node.put("name", (String)ifcProduct1.eGet(nameFeature));
		object2Node.put("name", (String)ifcProduct2.eGet(nameFeature));
		
		objectNode.set("object1", object1Node);
		objectNode.set("object2", object2Node);
		
		return objectNode;
	}
}
