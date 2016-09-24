package io.takari.jdkget.extract;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.takari.jdkget.IOutput;
import io.takari.jdkget.JdkGetter.JdkVersion;

public class BinJDKExtractor extends AbstractZipExtractor {

  private static final int[] ZIP_PREFIX = new int[] {0x50, 0x4b, 0x03, 0x04};
  private static final int MAX_ZIP_READ = 0x20000;

  @Override
  public boolean extractJdk(JdkVersion version, File jdkImage, File outputDir, File workDir, IOutput output)
    throws IOException, InterruptedException {

    output.info("Extracting jdk image into " + outputDir);

    String versionPrefix = "jdk" + version.longVersion();
    outputDir.mkdir();

    try (InputStream in = new BufferedInputStream(new FileInputStream(jdkImage))) {
      // find start of zip 
      findZipStream(in);

      ZipInputStream zip = new ZipInputStream(in);

      ZipEntry e;
      while ((e = zip.getNextEntry()) != null) {
        extractEntry(outputDir, versionPrefix, e, zip);
      }
    }

    return true;
  }

  private void findZipStream(InputStream in) throws IOException {

    int total = 0;
    int idx = 0;
    while (true) {
      if (idx == 0) {
        in.mark(ZIP_PREFIX.length);
      }

      int b = in.read();
      if (b == -1) {
        break;
      }
      total++;
      if (b == ZIP_PREFIX[idx]) {
        idx++;
        if (idx >= ZIP_PREFIX.length) {
          // found it!
          in.reset();
          return;
        }
      } else {
        idx = 0;
      }

      if (total > MAX_ZIP_READ) {
        break;
      }
    }
    throw new IllegalStateException("Cannot find start of zip stream");
  }

}
