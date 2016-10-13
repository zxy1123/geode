package org.apache.geode.e2e;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class Utils {

  /**
   * If the provided class has been loaded from a jar file that is on the local file system,
   * will find the absolute path to that jar file. If the provided class is simply within a directory,
   * the class will be zipped up into a temporary file which will be returned to the caller.
   *
   * @param clazz the name of the class for which we want to know it's location in the classpath
   *
   * @throws IllegalStateException If the specified class was loaded from a non-local location
   * (such as via HTTP, from a database, or some other custom classloading device).
   */
  public static String getJarForClassName(String clazz)
    throws IOException, IllegalStateException, ClassNotFoundException {
    Class context = Class.forName(clazz);
    String rawName = context.getName();
    String classFileName;
    // rawName is something like package.name.ContainingClass$ClassName.
    // We need to turn this into ContainingClass$ClassName.class.
    {
      int idx = rawName.lastIndexOf('.');
      classFileName = (idx == -1 ? rawName : rawName.substring(idx + 1)) + ".class";
    }

    String uri = context.getResource(classFileName).toString();
    if (uri.startsWith("file:")) {
      return jarredClass(context.getPackage().getName(), uri.substring("file:".length()));
    }
    if (!uri.startsWith("jar:file:")) {
      int idx = uri.indexOf(':');
      String protocol = idx == -1 ? "(unknown)" : uri.substring(0, idx);
      throw new IllegalStateException("This class has been loaded remotely via the " + protocol + " protocol. Only loading from a jar on the local file system is supported.");
    }

    int idx = uri.indexOf('!');
    //As far as I know, the if statement below can't ever trigger, so it's more of a sanity check thing.
    if (idx == -1) {
      throw new IllegalStateException("You appear to have loaded this class from a local jar file, but I can't make sense of the URL! " + uri.toString());
    }

    try {
      String fileName = URLDecoder.decode(uri.substring("jar:file:".length(), idx), Charset.defaultCharset().name());
      return new File(fileName).getAbsolutePath();
    } catch (UnsupportedEncodingException e) {
      throw new InternalError("default charset doesn't exist. Your VM is borked.");
    }
  }

  /**
   * Used to temporarily jar up a class file. Given a file, create a jar containing on the file.
   * The jar will be deleted on JVM exit.
   * @param packageName the name of the package to which the given classes belong
   * @param classFiles a vararg list of local class files, belonging to the given package
   * @return a zip file containing the given URI
   */
  private static String jarredClass(String packageName, String... classFiles) throws IOException {
    File tempFile = File.createTempFile("class-", ".zip");
    tempFile.deleteOnExit();
    ZipOutputStream zipfile = new ZipOutputStream(new FileOutputStream(tempFile));

    String packagePath = packageName.replace(".", File.separator);

    for (String file : classFiles) {
      int idx = file.lastIndexOf("/");
      String baseClassName = file.substring(idx + 1);
      String zipFileName = packagePath + File.separator + baseClassName;
      zipfile.putNextEntry(new ZipEntry(zipFileName));

      InputStream fileInputStream = new FileInputStream(file);
      IOUtils.copy(fileInputStream, zipfile);
      fileInputStream.close();
    }
    zipfile.close();

    return tempFile.getAbsolutePath();
  }
}
