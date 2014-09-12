package net.kozelka.runjar.boot;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void testClasspath() throws Exception {
        final String result = Utils.classpath(Arrays.asList(new File("Hello/World"), new File("Petr/Kozelka")));
        Assert.assertTrue("Result is: " + result, result.contains(File.separator + "Petr"));
        Assert.assertEquals(result.indexOf(File.pathSeparatorChar), result.lastIndexOf(File.pathSeparatorChar));
    }

    @Test
    public void testReplaceProperties() throws Exception {
        final Properties props = new Properties();
        props.setProperty("a", "AAAA");
        props.setProperty("b", "BBBB");
        final String result = Utils.replaceProperties("c = ${a} + ${b}", props);
        Assert.assertEquals("c = AAAA + BBBB", result);
    }

    @Test
    public void testArgsList() throws Exception {
        final List<String> commaSeparated = Utils.argsList(",item1,item2");
        Assert.assertEquals(2, commaSeparated.size());
        Assert.assertEquals("item1", commaSeparated.get(0));
        Assert.assertEquals("item2", commaSeparated.get(1));
    }
}