package net.kozelka.runjar.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import net.kozelka.runjar.boot.RunjarProperties;
import net.kozelka.runjar.enhancer.RunjarEnhancer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Enhances existing ANT buildfile with an executable jar with ant in it
 * @author Petr Kozelka
 */
@Mojo(name = "enhance-ant", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class EnhanceAntMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    File classesDirectory;

    @Parameter(defaultValue = "${project.build.directory}")
    File outputDirectory;

    @Parameter(defaultValue = "${project.build.scriptSourceDirectory}")
    File antScriptsDirectory;

    @Parameter(defaultValue = "main.ant.xml")
    String antFile;

    @Parameter(property = "project", required = true, readonly = true)
    MavenProject project;

    /**
     * Identifies the jar that we want to use as the boot jar.
     * @todo should this be configurable ?
     * @todo should we instead use a classname (like RunjarProperties) as the identification ?
     */
    private final String bootArtifactGA = "net.kozelka.runjar:runjar-boot";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

//        littleDiagnosticHack();

        @SuppressWarnings("unchecked")
        final Iterable<Artifact> artifacts = (Iterable<Artifact>) project.getRuntimeArtifacts();
        final Set<File> jars = new LinkedHashSet<File>();
        File bootJar = null;
        for (Artifact artifact : artifacts) {
            if (artifact.getFile() == null) {
                throw new MojoFailureException(String.format("Artifact is not resolved: %s", artifact));
            }
            final String ga = artifact.getGroupId() + ":" + artifact.getArtifactId();
            if (ga.equals(bootArtifactGA)) {
                bootJar = artifact.getFile();
            } else {
                jars.add(artifact.getFile());
            }
        }
        try {
            if (bootJar == null) {
                bootJar = locateBootJar(bootArtifactGA);
            }
            final RunjarEnhancer enhancer = new RunjarEnhancer(classesDirectory);
            enhancer.addJars(jars);
            enhancer.addScripts(antScriptsDirectory);
            enhancer.expandBootJar(bootJar);
            final Properties properties = new Properties();
            properties.setProperty(RunjarProperties.MAIN_CLASS_PROP, "org.apache.tools.ant.Main");
            properties.setProperty(RunjarProperties.ARGS_PREPEND_PROP, ",-f,${runjar.basedir}/" + antFile);
            properties.setProperty(RunjarProperties.SHUTDOWN_FILE_PROP, "${runjar.basedir}/.shutdown.properties");
            enhancer.saveProperties(properties);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private static File locateBootJar(String bootArtifactGA) throws IOException {
        final String[] ga = bootArtifactGA.split(":");
        final String res = "/META-INF/maven/" + ga[0] + "/" + ga[1] + "/pom.properties";
        final String path = EnhanceAntMojo.class.getResource(res).getPath();
        final String fileStr = new URL(path.split("!")[0]).getPath();
        final File file = new File(fileStr);
        if (!file.exists()) {
            throw new FileNotFoundException(fileStr);
        }
        return file;
    }

    @SuppressWarnings("unchecked")
    private void littleDiagnosticHack() throws MojoExecutionException {
        try {
            show("runtime", project.getRuntimeArtifacts());
            show("plugins", project.getPluginArtifacts());
            show("compile", project.getCompileArtifacts());
            show("artifacts", project.getArtifacts());
            show("dep.artifacts", project.getDependencyArtifacts());
            show("compile.cpe", project.getCompileClasspathElements());
            show("runtime.cpe", project.getRuntimeClasspathElements());
            show("runtime.deps", project.getRuntimeDependencies());
            show("ep.runtime", project.getExecutionProject().getRuntimeArtifacts());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void show(String prefix, Collection<Artifact> artifacts) {
        System.out.println("Showing " + prefix);
        for (Object o : artifacts) {
            System.out.println(String.format("%s %s : %s", prefix, o.getClass(), o));
        }
        System.out.println();
    }
}
