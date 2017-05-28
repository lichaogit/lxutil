package org.lx.arch.expr;

public class ExprException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2048439962211945042L;

	public ExprException()
	{
		super();
	}

	public ExprException(String message)
	{
		super(message);
	}

	public ExprException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ExprException(Throwable cause)
	{
		super(cause);
	}
}