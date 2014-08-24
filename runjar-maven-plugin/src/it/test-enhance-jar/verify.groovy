File runnableJar = new File(basedir, 'target/test-enhance-jar-run.jar')
assert runnableJar.exists()
File installedArtifact = new File(basedir, "../../local-repo/com/example/test-enhance-jar/0-SNAPSHOT/test-enhance-jar-0-SNAPSHOT-run.jar")
assert installedArtifact.exists()

File signal = new File(basedir, "target/signal.txt")
println("Executing " + runnableJar)
println([System.getProperty("java.home") + "/bin/java",
         "-Drunjar.keep=true", "-Drunjar.verbose=true", "-Drunjar.basedir="+signal.getParentFile()+"/runjar.tmp",
         "-jar", runnableJar.toString(), signal.getAbsolutePath()].execute().text)
assert signal.exists()
File shutdownExecuted = new File(signal.toString() + ".shutdown-was-called")
assert shutdownExecuted.exists()

