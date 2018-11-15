//package org.clash;
//
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.List;
//
//import org.bimserver.clashdetection.Clash;
//import org.bimserver.clashdetection.ClashDetectionResults;
//import org.bimserver.clashdetection.ClashDetector;
//import org.bimserver.emf.IdEObject;
//import org.bimserver.emf.IfcModelInterface;
//import org.bimserver.emf.OfflineGeometryGenerator;
//import org.bimserver.emf.PackageMetaData;
//import org.bimserver.emf.Schema;
//import org.bimserver.ifc.step.deserializer.Ifc2x3tc1StepDeserializer;
//import org.bimserver.ifc.step.serializer.Ifc2x3tc1StepSerializer;
//import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
//import org.bimserver.models.ifc2x3tc1.IfcProduct;
//import org.bimserver.plugins.deserializers.DeserializeException;
//import org.bimserver.plugins.renderengine.RenderEngine;
//import org.bimserver.plugins.renderengine.RenderEngineException;
//import org.ifcopenshell.IfcGeomServerClient;
//import org.ifcopenshell.IfcOpenShellEngine;
//
//public class Test {
//	@org.junit.Test
//	public void test() {
//		try {
//			Ifc2x3tc1StepDeserializer deserializer = new Ifc2x3tc1StepDeserializer(Schema.IFC2X3TC1);
//			Ifc2x3tc1StepSerializer serializer = new Ifc2x3tc1StepSerializer(null);
//			
//			IfcGeomServerClient test = new IfcGeomServerClient(IfcGeomServerClient.ExecutableSource.S3, Paths.get("tmp"));
//			String executableFilename = test.getExecutableFilename();
//			test.close();
//			
//			RenderEngine renderEngine = new IfcOpenShellEngine(executableFilename);
//			
//			renderEngine.init();
//			
//			PackageMetaData packageMetaData = new PackageMetaData(Ifc2x3tc1Package.eINSTANCE, Schema.IFC2X3TC1, Paths.get("."));
//			deserializer.init(packageMetaData);
//			IfcModelInterface model = deserializer.read(Paths.get("C:\\Git\\TestFiles\\TestData\\data\\AC11-Institute-Var-2-IFC.ifc").toFile());
//
//			OfflineGeometryGenerator offlineGeometryGenerator = new OfflineGeometryGenerator(model, serializer, renderEngine);
//			offlineGeometryGenerator.generateForAllElements();
//			
//			List<IdEObject> products = model.getAllWithSubTypes(model.getPackageMetaData().getEClass("IfcProduct"));
//			
//			ClashDetector clashDetector = new ClashDetector(products, 1f);
//			ClashDetectionResults findClashes = clashDetector.findClashes();
//			
//			System.out.println(findClashes.size());
//		} catch (DeserializeException e) {
//			e.printStackTrace();
//		} catch (RenderEngineException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}