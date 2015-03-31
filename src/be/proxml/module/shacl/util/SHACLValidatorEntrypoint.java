package be.proxml.module.shacl.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import org.topbraid.shacl.lite.SHACLLiteConstraintValidator;
import org.topbraid.shacl.vocabulary.SHACL;

import java.io.*;

/**
 * Created by yyz on 3/30/15.
 */
public class SHACLValidatorEntrypoint {
    public static void main(String[] args) {
        String filePath;
        System.out.println("===SHACL Lite Constraint Validator===");
        if (args.length != 1) {
            System.out.println("Error: Missing arguments");
            System.out.println("Usage: java -jar jar-name.jar <rdf-file.(xml|rdf|nt|nq|ttl|jsonld|trig)>");
            return;
        }
        filePath = args[0];
        //filePath = "resource/test.ttl";
        Model model = loadModel(filePath);
        SHACLLiteConstraintValidator validator = new SHACLLiteConstraintValidator(model);
        Model result = validator.validateGraph();
        if (result.listStatements().hasNext()) {
            printError(result);
            exportModel(result, filePath.substring(0, filePath.lastIndexOf(".")) + "-result.ttl", "TURTLE");
        }
        else System.out.println("All constraints validated, no error found.");
        return;
    }

    public static Model loadModel(String filePath) {
        Model model = ModelFactory.createDefaultModel();
        InputStream inputStream = FileManager.get().open(filePath);
        if (inputStream == null) {
            String msg = "File " + filePath + " not found";
            //logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        model.read(filePath);
        return model;
    }

    public static void exportModel(Model model, String filePath, String format) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath), "utf-8"));
            model.write(writer, format);
            writer.close();
            //logger.info("Exporting completed successfully");
        } catch (IOException ioe) {
            String msg = "The provided file path is not writable";
            //logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void printError(Model model) {
        System.out.println("SHACL Validator has detected the following errors:");
        System.out.println("---");
         for (Resource error : model.listSubjectsWithProperty(RDF.type, SHACL.Error).toList()) {
            System.out.println(model.listObjectsOfProperty(error, SHACL.message).next().toString() + " at: ");
            System.out.println("    root node: " + model.listObjectsOfProperty(error, SHACL.root).next().toString());
            System.out.println("    faulty predicate: " + model.listObjectsOfProperty(error, SHACL.path).next().toString());
        }
    }
}
