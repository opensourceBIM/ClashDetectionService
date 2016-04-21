package org.clash;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.bimserver.clashdetection.Clash;
import org.bimserver.clashdetection.ClashDetector;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.OfflineGeometryGenerator;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.emf.Schema;
import org.bimserver.ifc.step.deserializer.Ifc2x3tc1StepDeserializer;
import org.bimserver.ifc.step.serializer.Ifc2x3tc1StepSerializer;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.renderengine.RenderEngine;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.ifcopenshell.IfcOpenShellEngine;

public class Test {
	@org.junit.Test
	public void test() {
		try {
			Ifc2x3tc1StepDeserializer deserializer = new Ifc2x3tc1StepDeserializer(Schema.IFC2X3TC1);
			Ifc2x3tc1StepSerializer serializer = new Ifc2x3tc1StepSerializer(null);
			RenderEngine renderEngine = new IfcOpenShellEngine("C:\\Git\\IfcOpenShell-BIMserver-plugin\\exe\\64\\win\\IfcGeomServer.exe");
			
			renderEngine.init();
			
			PackageMetaData packageMetaData = new PackageMetaData(Ifc2x3tc1Package.eINSTANCE, Schema.IFC2X3TC1, Paths.get("."));
			deserializer.init(packageMetaData);
			IfcModelInterface model = deserializer.read(Paths.get("C:\\Git\\TestFiles\\TestData\\data\\export1.ifc").toFile());

			OfflineGeometryGenerator offlineGeometryGenerator = new OfflineGeometryGenerator(model, serializer, renderEngine);
			offlineGeometryGenerator.generateForAllElements();
			
			List<IfcProduct> products = model.getAllWithSubTypes(IfcProduct.class);
			
			ClashDetector clashDetector = new ClashDetector(products, 1f);
			List<Clash> clashes = clashDetector.findClashes();
			
			System.out.println(clashes.size());
		} catch (DeserializeException e) {
			e.printStackTrace();
		} catch (RenderEngineException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}