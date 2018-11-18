package org.bimserver.clashdetection;

import java.io.IOException;
import java.util.Date;

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
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bimserver.bimbots.BimBotContext;
import org.bimserver.bimbots.BimBotsException;
import org.bimserver.bimbots.BimBotsInput;
import org.bimserver.bimbots.BimBotsOutput;
import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.interfaces.objects.SInternalServicePluginConfiguration;
import org.bimserver.models.store.DoubleType;
import org.bimserver.models.store.IfcHeader;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ParameterDefinition;
import org.bimserver.models.store.PrimitiveDefinition;
import org.bimserver.models.store.PrimitiveEnum;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.services.BimBotAbstractService;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.utils.IfcUtils;
import org.eclipse.emf.ecore.EClass;
import org.opensourcebim.bcf.BcfException;
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

public class ClashDetectionServiceBcfPlugin extends BimBotAbstractService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClashDetectionServiceBcfPlugin.class);
	private static DatatypeFactory DATE_FACTORY;

	static {
		try {
			DATE_FACTORY = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void init(PluginContext pluginContext) throws PluginException {
		super.init(pluginContext);
	}

	@Override
	public BimBotsOutput runBimBot(BimBotsInput input, BimBotContext bimBotContext, PluginConfiguration settings)
			throws BimBotsException {
		BcfFile bcf = new BcfFile();

		IfcModelInterface model = input.getIfcModel();
		PackageMetaData packageMetaData = model.getPackageMetaData();
		EClass ifcProductClass = packageMetaData.getEClass("IfcProduct");
		ClashDetector clashDetector = new ClashDetector(model.getAllWithSubTypes(ifcProductClass), settings.getDouble("margin").floatValue());
		ClashDetectionResults clashDetectionResults = clashDetector.findClashes();
		
		for (Clash clash : clashDetectionResults.getClashes()) {
			GregorianCalendar now = new GregorianCalendar();

			TopicFolder topicFolder = bcf.createTopicFolder();
			topicFolder.setDefaultSnapShotToDummy();
			
			Topic topic = topicFolder.createTopic();
			topic.setTitle("Clash");
			topic.setTopicType("CLASH");
			topic.setTopicStatus("ERROR");
			XMLGregorianCalendar newXMLGregorianCalendar = DATE_FACTORY.newXMLGregorianCalendar(now);
			topic.setCreationDate(newXMLGregorianCalendar);
			topic.getReferenceLink().add(getPluginContext().getBasicServerInfo().getSiteAddress());
			
			topic.setCreationAuthor(bimBotContext.getCurrentUser());
			
			Markup markup = topicFolder.getMarkup();
			Header header = new Header();
			markup.setHeader(header);
			List<File> files = header.getFile();
			
			File file1 = new File();
			// TODO maybe just use the single IfcProject (since there should always be just one)
			IdEObject ifcProject = IfcUtils.getIfcProject(clash.getIfcProduct1());
			String ifcProject1 = (String) ifcProject.eGet(ifcProject.eClass().getEStructuralFeature("GlobalId"));
			file1.setIfcProject(ifcProject1);
			file1.setIfcSpatialStructureElement((String) clash.getIfcProduct1().eGet(clash.getIfcProduct1().eClass().getEStructuralFeature("GlobalId")));
			file1.setIsExternal(true);

			String filename = null;
			XMLGregorianCalendar fileDate = null;
			IfcHeader ifcHeader = model.getModelMetaData().getIfcHeader();
			if (ifcHeader != null) {
				if (ifcHeader.getTimeStamp() != null) {
					fileDate = dateToXMLGregorianCalendar(ifcHeader.getTimeStamp(), TimeZone.getDefault());
				}
				filename = ifcHeader.getFilename();
			}
			
			file1.setReference(filename);
			file1.setFilename(filename);
			file1.setDate(fileDate);
			files.add(file1);

			IdEObject ifcProject3 = IfcUtils.getIfcProject(clash.getIfcProduct2());
			String ifcProject2 = (String) ifcProject3.eGet(ifcProject3.eClass().getEStructuralFeature("GlobalId"));
			File file2 = new File();
			file2.setIfcProject(ifcProject2);
			file2.setIfcSpatialStructureElement((String) clash.getIfcProduct2().eGet(clash.getIfcProduct2().eClass().getEStructuralFeature("GlobalId")));
			file2.setIsExternal(true);
			
			file2.setReference(filename);
			file2.setFilename(filename);
			file2.setDate(fileDate);
			files.add(file2);
		}
		try {
			BimBotsOutput bimBotsOutput = new BimBotsOutput(getOutputSchema(), bcf.toBytes());
			bimBotsOutput.setContentType("application/zip");
			bimBotsOutput.setTitle("Clash Detection BCF results");
			return bimBotsOutput;
		} catch (BcfException e) {
			LOGGER.error("", e);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		return null;
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
	
	public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date, TimeZone zone) {
		XMLGregorianCalendar xmlGregorianCalendar = null;
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		gregorianCalendar.setTimeZone(zone);
		try {
			DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
			xmlGregorianCalendar = dataTypeFactory.newXMLGregorianCalendar(gregorianCalendar);
		} catch (Exception e) {
			System.out.println("Exception in conversion of Date to XMLGregorianCalendar" + e);
		}

		return xmlGregorianCalendar;
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

	@Override
	public String getOutputSchema() {
		return "BCF_ZIP_2_0";
	}
}