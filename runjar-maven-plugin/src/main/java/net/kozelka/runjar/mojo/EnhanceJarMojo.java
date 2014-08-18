package net.kozelka.runjar.mojo;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Enhances existing JAR (with a Main-Class attribute) to be an executable jar; adds libraries, boot etc.
 * @author Petr Kozelka
 */
@Mojo(name = "enhance-jar")
public class EnhanceJarMojo extends AbstractMojo {

    /**
     * Jar file name
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.fileName}.jar")
    File jar;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // TODO add boot
        // TODO add dependencies
        // TODO add ant
        throw new UnsupportedOperationException();
    }
}
