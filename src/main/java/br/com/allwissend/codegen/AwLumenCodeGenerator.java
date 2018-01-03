package br.com.allwissend.codegen;

import io.swagger.codegen.languages.*;

import io.swagger.codegen.*;
import io.swagger.models.properties.*;

import java.util.*;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwLumenCodeGenerator extends AbstractPhpCodegen
{
    static Logger LOGGER = LoggerFactory.getLogger(AbstractPhpCodegen.class);

     @SuppressWarnings("hiding")
    protected String apiVersion = "1.0.1";

    /**
     * Configures the type of generator.
     *
     * @return  the CodegenType for this generator
     * @see     io.swagger.codegen.CodegenType
     */
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return "aw-lumen";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a LumenServerCodegen server library.";
    }

    public AwLumenCodeGenerator() {
        super();

        embeddedTemplateDir = templateDir = "AwLumenCodeGenerator";

        /*
         * packPath
         */
        invokerPackage = "lumen";
        packagePath = "";

        /*
         * Api Package.  Optional, if needed, this can be used in templates
         */
        apiPackage = "app.Http.Controllers";

        /*
         * Model Package.  Optional, if needed, this can be used in templates
         */
        modelPackage = "app.Models";

        // template files want to be ignored
        modelTemplateFiles.clear();
        modelDocTemplateFiles.clear();
        apiTestTemplateFiles.clear();
        apiDocTemplateFiles.clear();


        // Adding model templates
        modelTemplateFiles.put("model.mustache", ".php");
        modelTestTemplateFiles.put("model_test.mustache", ".php");
        modelDocTemplateFiles.put("model_doc.mustache", ".md");

        // Adding api templates
        apiTemplateFiles.put("api.mustache", ".php");
        apiTestTemplateFiles.put("api_test.mustache", ".php");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");

        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);

        /*
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
        supportingFiles.add(new SupportingFile("composer.mustache", packagePath + File.separator + srcBasePath, "composer.json"));
        supportingFiles.add(new SupportingFile("readme.md", packagePath + File.separator + srcBasePath, "readme.md"));
        supportingFiles.add(new SupportingFile("app.php", packagePath + File.separator + srcBasePath + File.separator + "bootstrap", "app.php"));
        supportingFiles.add(new SupportingFile("index.php", packagePath + File.separator + srcBasePath + File.separator + "public", "index.php"));
        supportingFiles.add(new SupportingFile("User.php", packagePath + File.separator + srcBasePath + File.separator + "app", "User.php"));
        supportingFiles.add(new SupportingFile("Kernel.php", packagePath + File.separator + srcBasePath + File.separator + "app"  + File.separator + "Console", "Kernel.php"));
        supportingFiles.add(new SupportingFile("Handler.php", packagePath + File.separator + srcBasePath + File.separator + "app"  + File.separator + "Exceptions", "Handler.php"));
        supportingFiles.add(new SupportingFile("routes.mustache", packagePath + File.separator + srcBasePath + File.separator + "app"  + File.separator + "Http", "routes.php"));

        supportingFiles.add(new SupportingFile("Controller.php", packagePath + File.separator + srcBasePath + File.separator + "app"  + File.separator + "Http" + File.separator + "Controllers" + File.separator, "Controller.php"));
        supportingFiles.add(new SupportingFile("Authenticate.php", packagePath + File.separator + srcBasePath + File.separator + "app"  + File.separator + "Http" + File.separator + "Middleware" + File.separator, "Authenticate.php"));

        // Docker
        supportingFiles.add(new SupportingFile("docker-compose.mustache", packagePath, "docker-compose.yml"));
        supportingFiles.add(new SupportingFile("Dockerfile.php7", packagePath, "Dockerfile.php7"));


    }

    // override with any special post-processing
    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
        @SuppressWarnings("unchecked")
        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
        @SuppressWarnings("unchecked")
        String opTipo = "Command";

        for (CodegenOperation op : operations) {
            op.httpMethod = op.httpMethod.toLowerCase();

            if (op.httpMethod != null && (Objects.equals("get",op.httpMethod) || Objects.equals("head", op.httpMethod) || Objects.equals("options", op.httpMethod))) {
              opTipo = "Query";
            }
            // LOGGER.warn("Check if " + op.httpMethod + " is command or query:" + opTipo);
        }

        // for (CodegenOperation op : operations) {
        //     op.httpMethod = op.httpMethod.toLowerCase();
        //     LOGGER.warn("Check if " + op.httpMethod + " contains '.' (dots)");
        //
        //     // check to see if the path contains ".", which is not supported by Lumen
        //     // ref: https://github.com/swagger-api/swagger-codegen/issues/6897
        //     if (op.path != null && op.path.contains(".")) {
        //         throw new IllegalArgumentException("'.' (dot) is not supported by PHP Lumen. Please refer to https://github.com/swagger-api/swagger-codegen/issues/6897 for more info.");
        //     }
        // }

        // sort the endpoints in ascending to avoid the route priority issure.
        // https://github.com/swagger-api/swagger-codegen/issues/2643
        Collections.sort(operations, new Comparator<CodegenOperation>() {
            @Override
            public int compare(CodegenOperation lhs, CodegenOperation rhs) {
                return lhs.path.compareTo(rhs.path);
            }
        });

        return objs;
    }
}
