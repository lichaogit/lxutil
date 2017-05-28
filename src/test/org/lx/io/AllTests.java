package test.org.lx.io;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for test.org.lx.io");
		//$JUnit-BEGIN$
		suite.addTestSuite(testMultiThread.class);
		suite.addTestSuite(testXMLEncodeDecode.class);
		suite.addTestSuite(testHtmlDecodeWriter.class);
		//$JUnit-END$
		return suite;
	}

}
