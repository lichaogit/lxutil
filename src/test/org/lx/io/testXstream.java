package test.org.lx.io;

import junit.framework.TestCase;

import com.thoughtworks.xstream.XStream;

public class testXstream extends TestCase
{

	class Person
	{
		public String name;

		public int age;

		public int getAge()
		{
			return age;
		}

		public void setAge(int age)
		{
			this.age = age;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}

	public void testXstream()
	{
		XStream xstream = new XStream();
		Person p = new Person();
		p.age = 10;
		p.name = "zlc";
		String xml = xstream.toXML(p);
		assertEquals("", xml);

	}
}
