package org.bimserver.clashdetection;

import org.bimserver.emf.IdEObject;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Clash {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private IdEObject ifcProduct1;
	private IdEObject ifcProduct2;

	public Clash(IdEObject ifcProduct1, IdEObject ifcProduct2) {
		this.ifcProduct1 = ifcProduct1;
		this.ifcProduct2 = ifcProduct2;
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
		
		object1Node.put("guid", (String)ifcProduct1.eGet(guidFeature));
		object2Node.put("guid", (String)ifcProduct2.eGet(guidFeature));
		
		objectNode.set("object1", object1Node);
		objectNode.set("object2", object2Node);
		
		return objectNode;
	}
}
