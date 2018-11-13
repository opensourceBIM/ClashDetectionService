package org.bimserver.clashdetection;

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

import java.util.HashSet;
import java.util.Set;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.models.store.DoubleType;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ParameterDefinition;
import org.bimserver.models.store.PrimitiveDefinition;
import org.bimserver.models.store.PrimitiveEnum;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.services.AbstractAddExtendedDataService;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.eclipse.emf.ecore.EClass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;

public class ClashDetectionServiceJsonVisualizationPlugin extends AbstractAddExtendedDataService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public ClashDetectionServiceJsonVisualizationPlugin() {
		super("VIS_3D_JSON_1_0");
	}

	@Override
	public void newRevision(RunningService runningService, BimServerClientInterface bimServerClientInterface, long poid, long roid, String userToken, long soid, SObjectType settings) throws Exception {
		SProject project = bimServerClientInterface.getServiceInterface().getProjectByPoid(poid);
		IfcModelInterface model = bimServerClientInterface.getModel(project, roid, true, false, true);
		PackageMetaData packageMetaData = model.getPackageMetaData();
		EClass ifcProductClass = packageMetaData.getEClass("IfcProduct");
		ClashDetector clashDetector = new ClashDetector(model.getAllWithSubTypes(ifcProductClass), runningService.getPluginConfiguration().getDouble("margin").floatValue());
		ClashDetectionResults clashDetectionResults = clashDetector.findClashes();
		
		ObjectNode visNode = OBJECT_MAPPER.createObjectNode();
		visNode.put("name", "Clashes");
		ArrayNode changes = OBJECT_MAPPER.createArrayNode();
		visNode.set("changes", changes);
		ObjectNode mainChange = OBJECT_MAPPER.createObjectNode();
		changes.add(mainChange);
		ObjectNode selector = OBJECT_MAPPER.createObjectNode();
		mainChange.set("selector", selector);
		ArrayNode guids = OBJECT_MAPPER.createArrayNode();
		selector.set("guids", guids);
		ObjectNode effect = OBJECT_MAPPER.createObjectNode();
		mainChange.set("effect", effect);
		ObjectNode color = OBJECT_MAPPER.createObjectNode();
		effect.set("color", color);
		color.put("r", 1);
		color.put("g", 0);
		color.put("b", 0);
		color.put("a", 0.7f);
		
		Set<String> guidsSet = new HashSet<>();
		
		for (Clash clash : clashDetectionResults.getClashes()) {
			guidsSet.add((String) clash.getIfcProduct1().eGet(clash.getIfcProduct1().eClass().getEStructuralFeature("GlobalId")));
			guidsSet.add((String) clash.getIfcProduct2().eGet(clash.getIfcProduct2().eClass().getEStructuralFeature("GlobalId")));
		}
		
		for (String guid : guidsSet) {
			guids.add(guid);
		}
		
		System.out.println("Unique GUID's in clashes: " + guids.size());
		
		addExtendedData(visNode.toString().getBytes(Charsets.UTF_8), "visualizationinfo.json", "Clashes (" + guids.size() + ")", "application/json", bimServerClientInterface, roid);
	}
	
	@Override
	public ObjectDefinition getSettingsDefinition() {
		ObjectDefinition objectDefinition = StoreFactory.eINSTANCE.createObjectDefinition();
		ParameterDefinition marginParameter = StoreFactory.eINSTANCE.createParameterDefinition();
		marginParameter.setIdentifier("margin");
		marginParameter.setName("Margin");
		marginParameter.setRequired(true);
		DoubleType defaultValue = StoreFactory.eINSTANCE.createDoubleType();
		defaultValue.setValue(0.1);
		marginParameter.setDefaultValue(defaultValue);
		PrimitiveDefinition doubleDefinition = StoreFactory.eINSTANCE.createPrimitiveDefinition();
		doubleDefinition.setType(PrimitiveEnum.DOUBLE);
		marginParameter.setType(doubleDefinition);
		objectDefinition.getParameters().add(marginParameter);
		return objectDefinition;
	}
}