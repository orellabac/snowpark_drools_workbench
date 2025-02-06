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

/**
 * A visitor that processes constructor declarations.
 */
class ConstructorVisitor extends VoidVisitorAdapter<Void> {
    public Map<String, ClassInfo> classInfoList = new HashMap<String, ClassInfo>();

    public static String getPackageName(ClassOrInterfaceDeclaration classDecl) {
        // Find the CompilationUnit that contains this class declaration
        java.util.Optional<CompilationUnit> cuOptional = classDecl.findCompilationUnit();
        if (cuOptional.isPresent()) {
            CompilationUnit cu = cuOptional.get();
            // Get the package declaration from the CompilationUnit
            java.util.Optional<com.github.javaparser.ast.PackageDeclaration> packageDeclOptional = cu
                    .getPackageDeclaration();
            if (packageDeclOptional.isPresent()) {
                return packageDeclOptional.get().getNameAsString();
            }
        }
        return ""; // Return empty if no package declaration is found
    }

    @Override
    public void visit(ConstructorDeclaration cd, Void arg) {
        super.visit(cd, arg);

        // Get the enclosing class or interface for this constructor.
        java.util.Optional<ClassOrInterfaceDeclaration> enclosingClassOpt = cd
                .findAncestor(ClassOrInterfaceDeclaration.class);
        if (!enclosingClassOpt.isPresent()) {
            return; // no enclosing class found; skip this constructor.
        }

        ClassOrInterfaceDeclaration enclosingClass = enclosingClassOpt.get();
        java.util.Optional<ClassOrInterfaceDeclaration> additionalEnclosingClass = enclosingClass
                .findAncestor(ClassOrInterfaceDeclaration.class);
        if (additionalEnclosingClass.isPresent()) {
            return; // no compilation unit; skip
        }

        if (cd.getParameters().size() > 1) {

            // Attempt to retrieve the name of the primary type from the compilation unit.
            // Find the enclosing class or interface declaration
            java.util.Optional<ClassOrInterfaceDeclaration> parentClassOpt = cd
                    .findAncestor(ClassOrInterfaceDeclaration.class);

            // Return the name if present, otherwise return an empty string or handle as
            // needed.
            String className = parentClassOpt.map(ClassOrInterfaceDeclaration::getNameAsString)
                    .orElse("UnknownClass");
            String packageName = null;
            String fullName = className;
            if (parentClassOpt.isPresent()) {
                packageName = getPackageName(parentClassOpt.get());
                fullName = packageName + "." + className;
            }
            ClassInfo info = classInfoList.get(fullName);
            if (info == null) {
                info = new ClassInfo(className, packageName);
                classInfoList.put(fullName, info);
            }
            ConstructorInfo cpConstructorInfo = new ConstructorInfo();
            info.constructors.add(cpConstructorInfo);
            System.out.println("Found constructor in class " + fullName + " with more than one argument:");
            cd.getParameters().forEach(param -> {
                cpConstructorInfo.parameters.add(new ParamInfo(param.getNameAsString(), param.getType().toString()));
                System.out.println("  Parameter: " + param.getType() + " " + param.getNameAsString());
            });
        }
    }
}
