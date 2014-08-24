package net.kozelka.runjar.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * @author Petr Kozelka
 */
public abstract class AbstractEnhancerMojo extends AbstractMojo {
    /**
     * Identifies the jar that we want to use as the boot jar.
     * @todo should this be configurable ?
     * @todo should we instead use a classname (like RunjarProperties) as the identification ?
     */
    protected final String bootArtifactGA = "net.kozelka.runjar:runjar-boot";

    @Parameter(defaultValue = "${project.build.directory}/runjar")
    File runjarDirectory;

    /**
     * Runnable jar file name (output).
     * Its extension is used as type when attaching to maven lifecycle.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}-run.jar")
    File runnableJar;

    /**
     * Original jar file name (input)
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    File jar;

    @Parameter(property = "project", required = true, readonly = true)
    MavenProject project;

    static File locateBootJar(String bootArtifactGA) throws IOException {
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
