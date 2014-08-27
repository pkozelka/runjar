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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Enhances existing ANT buildfile with an executable jar with ant in it
 * @author Petr Kozelka
 */
@Mojo(name = "enhance-ant", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
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
            properties.setProperty(RunjarProperties.PROP_META_CLASS, "org.apache.tools.ant.Main");
            properties.setProperty(RunjarProperties.PROP_META_ARGS_PREPEND, ",-f,${runjar.basedir}/" + antFile);
            properties.setProperty(RunjarProperties.PROP_META_SHUTDOWN_FILE, RunjarProperties.DEFAULT_SHUTDOWN_FILE); //todo: shouldn't this be the default?
            enhancer.saveProperties(properties);

            enhancer.compress(runnableJar);
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

}
