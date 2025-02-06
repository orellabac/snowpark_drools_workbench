package com.example.mavenplugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.apache.maven.plugins.annotations.Parameter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "process-sources", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ProcessSourcesMojo extends AbstractMojo {

    @Parameter(defaultValue = "rules")
    private String targetStage;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/handlers")
    private File outputDirectory;

    @Parameter(defaultValue = "${project.build.directory}/generated-sql")
    private File outputDirectoryScripts;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        List<String> sourceRoots = project.getCompileSourceRoots();

        getLog().info("Processing source roots: " + sourceRoots);
        ConstructorVisitor visitor = new ConstructorVisitor();
        StringBuilder sb = new StringBuilder();
        for (String sourceRoot : sourceRoots) {
            File folder = new File(sourceRoot);
            processFolder(folder, visitor);
        }
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        System.out.println("Processed: " + visitor.classInfoList.size());
        int generated = 0;

        for (String key : visitor.classInfoList.keySet()) {
            System.out.println("Key: " + key);
            ClassInfo classInfo = visitor.classInfoList.get(key);
            System.out.println("Class: " + classInfo.constructors.size());
            // We are only interested in classes with constructors with arguments
            // Because DataModels will always have a Default Constructor and we don't want to generate a handler for that
            if (classInfo.constructors.size() == 0)
                continue;
            String content = Templates.generateExecutorClass(classInfo.simpleName, classInfo.fullName,
                    classInfo.getArgsAndTypes(), classInfo.getArgs());
            Path targetPath = Paths
                    .get(outputDirectory.getAbsolutePath() + "/GenericExecutor_" + classInfo.simpleName + ".java");
            if (writeToFile(targetPath, content))
                generated++;
            sb.append(
                Templates.generateFunctionSQL(classInfo.simpleName, classInfo.getArgsAndTypesSF(), targetStage)
            );
        }
        // We need to add the MyDroolsConfig Helper
        Path targetPath = Paths.get(outputDirectory.getAbsolutePath() + "/MyDroolsConfig.java");
        if (generated > 0) {
            writeToFile(targetPath, Templates.generateDroolsConfigSource());
            Path targetScripts = Paths.get(outputDirectory.getAbsolutePath() + "/registrations.sql");
            writeToFile(targetScripts, sb.toString());
        }
    }



    private boolean writeToFile(Path targetPath, String content) {
        try {
            // Write the content to the file
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("File saved successfully to: " + outputDirectory.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Recursively process a folder to find all .java files.
     */
    private void processFolder(File folder, ConstructorVisitor visitor) {
        if (!folder.exists()) {
            getLog().warn("Folder does not exist: " + folder.getAbsolutePath());
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                processFolder(file, visitor);
            } else if (file.getName().endsWith(".java")) {
                processFile(file, visitor);
            }
        }
    }

    private void processFile(File file, ConstructorVisitor visitor) {
        getLog().info("Processing file: " + file.getAbsolutePath());
        try {
            // Read the file content as a string
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            // Parse the file content using JavaParser
            ParseResult<CompilationUnit> result = new JavaParser().parse(content);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                CompilationUnit cu = result.getResult().get();
                // Visit the AST to find constructors
                cu.accept(visitor, null);
            } else {
                getLog().warn("Could not parse file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            getLog().error("Error reading file: " + file.getAbsolutePath(), e);
        }
    }

}