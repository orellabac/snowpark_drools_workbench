package com.example.mavenplugin;

public class Templates {
    public static String generateDroolsConfigSource() {
        return "import java.io.BufferedReader;\n" +
                "import java.io.InputStreamReader;\n" +
                "import java.nio.charset.StandardCharsets;\n" +
                "import java.util.*;\n" +
                "import org.kie.api.KieServices;\n" +
                "import org.kie.api.builder.KieBuilder;\n" +
                "import org.kie.api.builder.KieFileSystem;\n" +
                "import org.kie.api.builder.KieModule;\n" +
                "import org.kie.api.runtime.KieContainer;\n" +
                "import org.kie.internal.io.ResourceFactory;\n" +
                "import org.slf4j.Logger;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "import java.io.InputStream;\n" +
                "\n" +
                "public class MyDroolsConfig {\n" +
                "\n" +
                "    private static final Logger logger = LoggerFactory.getLogger(MyDroolsConfig.class);\n" +
                "\n" +
                "    private static List<String> rules;\n" +
                "\n" +
                "    public static List<String> getRules() {\n" +
                "        if (rules == null) {\n" +
                "            rules = new ArrayList<>();\n" +
                "            InputStream inputStream = MyDroolsConfig.class.getClassLoader().getResourceAsStream(\"rules/rules.txt\");\n"
                +
                "            final List<String> files = new ArrayList<String>();\n" +
                "            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {\n"
                +
                "                reader.lines().forEach(line -> {\n" +
                "                    if (line.trim().endsWith(\"drl\")) {\n" +
                "                        String file_to_load = line.replace(\"src/main/resources/\", \"\");\n" +
                "                        logger.info(\"Loading rule file: \" + file_to_load);\n" +
                "                        files.add(file_to_load);\n" +
                "                    }\n" +
                "                });\n" +
                "            } catch (Exception e) {\n" +
                "                logger.error(\"Failed to read resource: \" + e);\n" +
                "            }\n" +
                "            rules.addAll(files);\n" +
                "        }\n" +
                "        return rules;\n" +
                "    }\n" +
                "\n" +
                "    public static KieContainer kieContainer() { \n" +
                "        System.setProperty(\"drools.enableDynamicWiring\", \"true\");\n" +
                "        final KieServices kieServices = KieServices.Factory.get();\n" +
                "        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();\n" +
                "        \n" +
                "        for (String rule : getRules()) {\n" +
                "            kieFileSystem.write(ResourceFactory.newClassPathResource(rule));\n" +
                "        }\n" +
                "\n" +
                "        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);\n" +
                "        kb.buildAll();\n" +
                "        KieModule kieModule = kb.getKieModule();\n" +
                "        return kieServices.newKieContainer(kieModule.getReleaseId());\n" +
                "    }\n" +
                "}\n";
    }

    public static String generateExecutorClass(String modelName, String modelQualifiedName,
            String argsAndTypes, String args) {
        return "import java.util.stream.Stream;\n" +
                "import org.kie.api.runtime.KieContainer;\n" +
                "import org.kie.api.runtime.KieSession;\n" +
                "import com.fasterxml.jackson.core.JsonProcessingException;\n" +
                "import com.fasterxml.jackson.databind.ObjectMapper;\n" +
                "import org.slf4j.Logger;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "\n" +
                "class OutputGeneric_" + modelName + " {\n" +
                "    public String output;\n" +
                "\n" +
                "    public OutputGeneric_" + modelName + "(String output) {\n" +
                "        this.output = output;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "public class GenericExecutor_" + modelName + " {\n" +
                "    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();\n" +
                "    private static final Logger logger = LoggerFactory.getLogger(GenericExecutor_" + modelName
                + ".class);\n" +
                "    private static KieContainer kieContainer;\n" +
                "\n" +
                "    public static KieContainer getContainerInstance() {\n" +
                "        if (kieContainer == null) {  // First check (no locking)\n" +
                "            synchronized (GenericExecutor_" + modelName + ".class) {\n" +
                "                if (kieContainer == null) {  // Second check (with locking)\n" +
                "                    kieContainer = MyDroolsConfig.kieContainer();\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        return kieContainer;\n" +
                "    }\n" +
                "\n" +
                "    private static String factsToString(Object[] facts) {\n" +
                "        StringBuilder results = new StringBuilder();\n" +
                "        results.append(\"[\");\n" +
                "        boolean first = true;\n" +
                "        for (Object fact : facts) {\n" +
                "            try {\n" +
                "                String json = OBJECT_MAPPER.writeValueAsString(fact);\n" +
                "                if (!first) results.append(\", \");\n" +
                "                results.append(json);\n" +
                "            } catch (JsonProcessingException e) {\n" +
                "                logger.error(\"Error converting fact to JSON\", e);\n" +
                "            }\n" +
                "            first = false;\n" +
                "        }\n" +
                "        results.append(\"]\");\n" +
                "        return results.toString();\n" +
                "    }\n" +
                "\n" +
                "    public String apply(Object obj) {\n" +
                "        KieSession kieSession = getContainerInstance().newKieSession();\n" +
                "        kieSession.insert(obj);\n" +
                "        kieSession.fireAllRules();\n" +
                "        Object[] facts = kieSession.getObjects(x -> !x.equals(obj)).toArray(new Object[0]);\n" +
                "        kieSession.dispose();\n" +
                "        return factsToString(facts);\n" +
                "    }\n" +
                "\n" +
                "    public static Class getOutputClass() {\n" +
                "        return OutputGeneric_" + modelName + ".class;\n" +
                "    }\n" +
                "\n" +
                "    public Stream<OutputGeneric_" + modelName + "> process(" + argsAndTypes + ") {\n" +
                "        String response = apply(new " + modelQualifiedName + "(" + args + "));\n" +
                "        return Stream.of(new OutputGeneric_" + modelName + "(response));\n" +
                "    }\n" +
                "}\n";
    }

    /**
     * Genera el código SQL para crear o reemplazar la función Java en Snowflake.
     *
     * @param className       El nombre de la clase que se usará en el nombre de la función y el handler.
     * @param constructorArgs Los argumentos del constructor que se incluirán en la definición de la función.
     * @param stage           El stage desde el cual se cargarán los JARs.
     * @return El código SQL generado como un String.
     */
    public static String generateFunctionSQL(String className, String constructorArgs, String stage) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nCREATE OR REPLACE FUNCTION DROOLS_")
          .append(className)
          .append("(")
          .append(constructorArgs)
          .append(")\n")
          .append(" RETURNS TABLE(output VARIANT)\n")
          .append(" LANGUAGE JAVA\n")
          .append(" RUNTIME_VERSION = '11'\n")
          .append(" IMPORTS = ('@")
          .append(stage)
          .append("/drools-utils-0.0.1-FAT.jar','@")
          .append(stage)
          .append("/demo-1.0.jar')\n")
          .append(" HANDLER = 'GenericExecutor_")
          .append(className)
          .append("'\n")
          .append(" PACKAGES = ('com.snowflake:snowpark:latest','com.snowflake:telemetry:latest');");
        
        return sb.toString();
    }

}
