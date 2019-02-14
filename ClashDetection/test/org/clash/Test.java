package org.clash;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.bimserver.clashdetection.ClashDetectionResults;
import org.bimserver.clashdetection.ClashDetector;
import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.OfflineGeometryGenerator;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.emf.Schema;
import org.bimserver.ifc.step.deserializer.Ifc2x3tc1StepDeserializer;
import org.bimserver.ifc.step.serializer.Ifc2x3tc1StepSerializer;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.renderengine.RenderEngine;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.ifcopenshell.IfcGeomServerClient;
import org.ifcopenshell.IfcOpenShellEngine;
import org.junit.Assert;

public class Test {
	@org.junit.Test
	public void test() throws URISyntaxException {
		try {
			Ifc2x3tc1StepDeserializer deserializer = new Ifc2x3tc1StepDeserializer(); //Schema.IFC2X3TC1);
			Ifc2x3tc1StepSerializer serializer = new Ifc2x3tc1StepSerializer(null);
			
			IfcGeomServerClient test = new IfcGeomServerClient(IfcGeomServerClient.ExecutableSource.S3, "4380e1d", Paths.get("tmp"));
			Path executableFilename = test.getExecutableFilename();
			test.close();
			
			RenderEngine renderEngine = new IfcOpenShellEngine(executableFilename, true, true);
			
			renderEngine.init();
			
			PackageMetaData packageMetaData = new PackageMetaData(Ifc2x3tc1Package.eINSTANCE, Schema.IFC2X3TC1, Paths.get("."));
			deserializer.init(packageMetaData);
			File ifcTestFile = new File(getClass().getResource("/AC11-Institute-Var-2-IFC.ifc").toURI());
			IfcModelInterface model = deserializer.read(ifcTestFile);

			OfflineGeometryGenerator offlineGeometryGenerator = new OfflineGeometryGenerator(model, serializer, renderEngine);
			offlineGeometryGenerator.generateForAllElements();
			
			List<IdEObject> products = model.getAllWithSubTypes(model.getPackageMetaData().getEClass("IfcProduct"));
			
			ClashDetector clashDetector = new ClashDetector(products, 1f);
			ClashDetectionResults findClashes = clashDetector.findClashes();
			
			Assert.assertEquals(0,  findClashes.getClashes().size());
			
			// In order to avoid a crash from the underlying IFCOpenShell process
			renderEngine.close();
			System.out.println(findClashes.size());
		} catch (DeserializeException e) {
			e.printStackTrace();
		} catch (RenderEngineException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}