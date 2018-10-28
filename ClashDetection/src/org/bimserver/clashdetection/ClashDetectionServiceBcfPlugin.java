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

import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SInternalServicePluginConfiguration;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.store.DoubleType;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ParameterDefinition;
import org.bimserver.models.store.PrimitiveDefinition;
import org.bimserver.models.store.PrimitiveEnum;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.services.AbstractAddExtendedDataService;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.utils.IfcUtils;
import org.opensourcebim.bcf.BcfFile;
import org.opensourcebim.bcf.TopicFolder;
import org.opensourcebim.bcf.markup.Header;
import org.opensourcebim.bcf.markup.Header.File;
import org.opensourcebim.bcf.markup.Markup;
import org.opensourcebim.bcf.markup.Topic;
import org.opensourcebim.bcf.visinfo.Direction;
import org.opensourcebim.bcf.visinfo.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClashDetectionServiceBcfPlugin extends AbstractAddExtendedDataService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClashDetectionServiceBcfPlugin.class);

	public ClashDetectionServiceBcfPlugin() {
		super("BCF_ZIP_2_0");
	}

	@Override
	public void init(PluginContext pluginContext) throws PluginException {
		super.init(pluginContext);
	}

	@Override
	public void newRevision(RunningService runningService, BimServerClientInterface bimServerClientInterface, long poid, long roid, String userToken, long soid, SObjectType settings) throws Exception {
		BcfFile bcf = new BcfFile();

		SProject project = bimServerClientInterface.getServiceInterface().getProjectByPoid(poid);
		IfcModelInterface model = bimServerClientInterface.getModel(project, roid, true, false, true);
		ClashDetector clashDetector = new ClashDetector(model.getAllWithSubTypes(IfcProduct.class), runningService.getPluginConfiguration().getDouble("margin").floatValue());
		List<Clash> clashes = clashDetector.findClashes();
		
		for (Clash clash : clashes) {
			GregorianCalendar now = new GregorianCalendar();

			TopicFolder topicFolder = bcf.createTopicFolder();
			topicFolder.setDefaultSnapShotToDummy();
			
			Topic topic = topicFolder.createTopic();
			topic.setTitle("Clash");
			XMLGregorianCalendar newXMLGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
			topic.setCreationDate(newXMLGregorianCalendar);
			topic.setGuid(UUID.randomUUID().toString());
			
			// TODO
			topic.setCreationAuthor("Test");
			
			Markup markup = topicFolder.getMarkup();
			Header header = new Header();
			markup.setHeader(header);
			List<File> files = header.getFile();
			
			File file1 = new File();
			String ifcProject1 = IfcUtils.getIfcProject(clash.getIfcProduct1()).getGlobalId();
			file1.setIfcProject(ifcProject1);
			file1.setIfcSpatialStructureElement(clash.getIfcProduct1().getGlobalId());
			file1.setIsExternal(true);

			// TODO
			file1.setReference("http://bimserver.org");
			file1.setDate(newXMLGregorianCalendar);
			files.add(file1);

			String ifcProject2 = IfcUtils.getIfcProject(clash.getIfcProduct2()).getGlobalId();
			File file2 = new File();
			file2.setIfcProject(ifcProject2);
			file2.setIfcSpatialStructureElement(clash.getIfcProduct1().getGlobalId());
			file2.setIsExternal(true);
			
			// TODO
			file2.setReference("http://bimserver.org");
			file2.setDate(newXMLGregorianCalendar);
			files.add(file2);
		}
		
		bcf.validate();
		
		addExtendedData(bcf.toBytes(), project.getName() + ".bcfzip", "Clash Detection (BCF)", "application/zip", bimServerClientInterface, roid);
	}

	@SuppressWarnings("unused")
	private Point newPoint(double x, double y, double z) {
		Point point = new Point();
		point.setX(x);
		point.setY(y);
		point.setZ(z);
		return point;
	}

	@SuppressWarnings("unused")
	private Direction newDirection(double x, double y, double z) {
		Direction direction = new Direction();
		direction.setX(x);
		direction.setY(y);
		direction.setZ(z);
		return direction;
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

	@Override
	public void unregister(SInternalServicePluginConfiguration internalService) {
	}
}