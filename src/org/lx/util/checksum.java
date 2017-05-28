package org.lx.util;

import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class checksum
{
  public static void main(String[] argv)
  {
    if (argv.length > 0)
    {
      Checksum checksumEngine = new Adler32();
      byte[] bytes = null;
      checksumEngine.reset();
      for (int i = 0; i < argv.length; i++)
      {
        bytes = argv[i].getBytes();
        checksumEngine.update(bytes, 0, bytes.length);
        System.out.println(argv[i] + "=" + checksumEngine.getValue());

      }
    } else
    {
      System.out.println("checksum a b c ...");
    }
  }
}