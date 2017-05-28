package org.lx.arch;

public class RESULT
{

	final public static RESULT RESULT_SUCCESS = new RESULT(0);

	final public static RESULT RESULT_FAIL = new RESULT(-1);

	final public static RESULT RESULT_PENDING = new RESULT(-2);

	final public static RESULT RESULT_REJECT = new RESULT(-3);

	final public static RESULT RESULT_NULL_PLUGIN = new RESULT(-4);

	final public static RESULT RESULT_FILTER_BLOCKED = new RESULT(-5);

	final public static RESULT RESULT_INVALID_PARAMETER = new RESULT(-6);

	final public static RESULT RESULT_NULL_ACTION = new RESULT(-7);

	int code;

	public RESULT(int code)
	{
		this.code = code;
	}

	public int getCode()
	{
		return this.code;
	}
}
