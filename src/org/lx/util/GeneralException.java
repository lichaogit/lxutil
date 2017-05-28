package org.lx.util;

public class GeneralException extends Exception
{
	// serialVersionUID ����������Ĳ�ͬ�汾��ļ�����.������޸��˴���, Ҫ�޸Ĵ�ֵ. ������ǰ���ϰ汾�������л�����ָ�ʱ�����
	private static final long serialVersionUID = 1L;

	public final static int E_UNKOWN = -1;

	protected Throwable throwable;

	protected int m_err_code;

	public Throwable getCause()
	{
		return (throwable == null ? this : throwable);
	}

	public GeneralException()
	{
	}

	public GeneralException(String message)
	{
		this(null, message, E_UNKOWN);
	}

	public GeneralException(Throwable throwable, String message)
	{
		this(throwable, message, E_UNKOWN);
	}

	public GeneralException(Throwable throwable, String message, int err_code)
	{
		super(message);
		this.throwable = throwable;
		this.m_err_code = err_code;
	}

	public GeneralException(Throwable throwable)
	{
		this(throwable, null, E_UNKOWN);
	}

	public GeneralException(int err_code)
	{
		this(null, null, err_code);
	}

	public GeneralException(String err, int err_code)
	{
		this(null, err, err_code);
	}

	public int getErrorCode()
	{
		return m_err_code;
	}

	public String toString()
	{
		String retval = super.getLocalizedMessage();
		do
		{
			if (retval != null)
			{
				break;
			}

			if (throwable != null)
			{
				retval = throwable.getLocalizedMessage();
			}
			if (retval != null)
			{
				break;
			}

			if (retval == null)
			{
				int code;
				if (GeneralException.class.isAssignableFrom(throwable
						.getClass()))
				{
					code = ((GeneralException) throwable).getErrorCode();
				} else
				{
					code = getErrorCode();
				}
				retval = "[err_code=" + code + "]";
			}
		} while (false);
		return retval;
	}
}
