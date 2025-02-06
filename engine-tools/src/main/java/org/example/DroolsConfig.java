package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class DroolsConfig {

    private static final Logger logger = LoggerFactory.getLogger(DroolsConfig.class);

    private static List<String> rules;

    public static List<String> getRules() {
        if (rules == null) {
            rules = new ArrayList<>();
            var inputStream = DroolsConfig.class.getClassLoader().getResourceAsStream("rules/rules.txt");
            final List<String> files = new ArrayList<String>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                reader.lines().forEach(line -> {
                    if (line.trim().endsWith("drl"))
                    {
                        var file_to_load = line.replace("src/main/resources/","");
                        logger.info("Loading rule file: " + file_to_load);
                        files.add(file_to_load);
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to read resource: " + e);
            }
            rules.addAll(files);
            rules.add("rules/rule3.drl");
        }
        return rules;
    }

    public static KieContainer kieContainer() { 
        System.setProperty("drools.enableDynamicWiring", "true");
        final KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        
        for (String rule : getRules()) {
            kieFileSystem.write(ResourceFactory.newClassPathResource(rule));
        }

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }
}