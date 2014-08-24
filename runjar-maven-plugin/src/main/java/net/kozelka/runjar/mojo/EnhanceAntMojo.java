package net.kozelka.runjar.mojo;

import net.kozelka.runjar.boot.RunjarProperties;
import net.kozelka.runjar.enhancer.RunjarEnhancer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Enhances existing ANT buildfile with an executable jar with ant in it
 * @author Petr Kozelka
 */
@Mojo(name = "enhance-ant", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class EnhanceAntMojo extends AbstractEnhancerMojo {

    @Parameter(defaultValue = "${project.build.scriptSourceDirectory}")
    File antScriptsDirectory;

    @Parameter(defaultValue = "main.ant.xml")
    String antFile;

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
            final RunjarEnhancer enhancer = new RunjarEnhancer(runjarDirectory);
            enhancer.addJars(jars);
            enhancer.addScripts(antScriptsDirectory);
            enhancer.expandBootJar(bootJar);
            final Properties properties = new Properties();
            properties.setProperty(RunjarProperties.MAIN_CLASS_PROP, "org.apache.tools.ant.Main");
            properties.setProperty(RunjarProperties.ARGS_PREPEND_PROP, ",-f,${runjar.basedir}/" + antFile);
            properties.setProperty(RunjarProperties.SHUTDOWN_FILE_PROP, "${runjar.basedir}/.shutdown.properties"); //todo: shouldn't this be the default?
            enhancer.saveProperties(properties);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

}
