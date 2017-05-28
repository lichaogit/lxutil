package test.org.lx.arch;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.org.lx.arch.expr.ExprParserTest;
import test.org.lx.arch.logic.ConditionMatcherTest;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for test.org.lx.arch");
		// $JUnit-BEGIN$
		suite.addTestSuite(testPluginManager.class);
		suite.addTestSuite(testConfigurationView.class);
		suite.addTestSuite(testTimerService.class);
		suite.addTestSuite(testStreamExchange.class);
		suite.addTestSuite(testThreadPool.class);
		suite.addTestSuite(BusyPipeTest.class);
		suite.addTestSuite(testPluginManager.class);
		suite.addTestSuite(ConditionMatcherTest.class);
		suite.addTestSuite(ExprParserTest.class);
		// $JUnit-END$
		return suite;
	}

}
