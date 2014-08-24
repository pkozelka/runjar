package net.kozelka.runjar.mojo;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import net.kozelka.runjar.boot.RunjarProperties;
import net.kozelka.runjar.enhancer.RunjarEnhancer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;

/**
 * Enhances existing JAR (with a Main-Class attribute) to be an executable jar; adds libraries, boot etc.
 * @author Petr Kozelka
 */
@Mojo(name = "enhance-jar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class EnhanceJarMojo extends AbstractEnhancerMojo {

    @Parameter(defaultValue = "run")
    String classifier;

    @Parameter(defaultValue = "true")
    boolean attach;

    /**
     * the entry point.
     * @todo add automatic lookup: if there is exactly one main in the jar, it will be used
     */
    @Parameter(defaultValue = "${project.groupId}.${project.artifactId}.Main")
    String mainClass;

    @Component
    MavenProjectHelper helper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        @SuppressWarnings("unchecked")
        final Iterable<Artifact> artifacts = (Iterable<Artifact>) project.getRuntimeArtifacts();
        final Set<File> jars = new LinkedHashSet<File>();
        for (Artifact artifact : artifacts) {
            if (artifact.getFile() == null) {
                throw new MojoFailureException(String.format("Artifact is not resolved: %s", artifact));
            }
            jars.add(artifact.getFile());
        }

        jars.add(jar);

        try {
            final File bootJar = locateBootJar(bootArtifactGA);
            final RunjarEnhancer enhancer = new RunjarEnhancer(runjarDirectory);
            enhancer.addJars(jars);
            enhancer.expandBootJar(bootJar);
            final Properties properties = new Properties();
            properties.setProperty(RunjarProperties.MAIN_CLASS_PROP, mainClass);
            properties.setProperty(RunjarProperties.SHUTDOWN_FILE_PROP, "${runjar.basedir}/.shutdown");
            enhancer.saveProperties(properties);
            enhancer.compress(runnableJar);

            if (attach) {
                final String extension = FileUtils.extension(runnableJar.getName());
                helper.attachArtifact(project, extension, classifier, runnableJar);
            }
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
