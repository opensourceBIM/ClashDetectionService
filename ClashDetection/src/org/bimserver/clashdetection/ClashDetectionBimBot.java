package org.bimserver.clashdetection;

import org.bimserver.bimbots.BimBotContext;
import org.bimserver.bimbots.BimBotsException;
import org.bimserver.bimbots.BimBotsInput;
import org.bimserver.bimbots.BimBotsOutput;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.models.store.DoubleType;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ParameterDefinition;
import org.bimserver.models.store.PrimitiveDefinition;
import org.bimserver.models.store.PrimitiveEnum;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.services.BimBotAbstractService;
import org.eclipse.emf.ecore.EClass;

import com.google.common.base.Charsets;

public class ClashDetectionBimBot extends BimBotAbstractService {

	@Override
	public BimBotsOutput runBimBot(BimBotsInput input, BimBotContext bimBotContext, SObjectType settings) throws BimBotsException {
		IfcModelInterface model = input.getIfcModel();
		PluginConfiguration pluginConfiguration = new PluginConfiguration(settings);
		PackageMetaData packageMetaData = model.getPackageMetaData();
		EClass ifcProductClass = packageMetaData.getEClass("IfcProduct");
		ClashDetector clashDetector = new ClashDetector(model.getAllWithSubTypes(ifcProductClass), pluginConfiguration.getDouble("margin").floatValue());
		ClashDetectionResults clashDetectionResults = clashDetector.findClashes();

		BimBotsOutput bimBotsOutput = new BimBotsOutput("CLASHDETECTION_RESULT_JSON_1_0", clashDetectionResults.toJson().toString().getBytes(Charsets.UTF_8));
		bimBotsOutput.setTitle("Clashdetection results");
		bimBotsOutput.setContentType("application/json");
		
		return bimBotsOutput;
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
	public String getOutputSchema() {
		return "CLASHDETECTION_RESULT_JSON_1_0";
	}
}
