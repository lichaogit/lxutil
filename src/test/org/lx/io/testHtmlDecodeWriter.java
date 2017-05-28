package test.org.lx.io;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.lx.io.HtmlDecodeWriter;

public class testHtmlDecodeWriter extends TestCase
{
	public void testHtmlDecode()
	{
		try
		{
			StringWriter w = null;
			HtmlDecodeWriter hw = null;

			w = new StringWriter();
			hw = new HtmlDecodeWriter(w);
			hw.write("Ãñ&lt;&gt;&amp;&quot;&#39;");
			hw.flush();

			assertEquals("Ãñ<>&\"\'", w.getBuffer().toString());

			w = new StringWriter();
			hw = new HtmlDecodeWriter(w);
			hw.write("&lt;&gt;&amp;&quot;&#39;");
			hw.flush();
			assertEquals("<>&\"\'", w.getBuffer().toString());

			w = new StringWriter();
			hw = new HtmlDecodeWriter(w);
			hw.write("&lt;&gt;&amp;hello&quot;&#39;");
			hw.flush();
			assertEquals("<>&hello\"\'", w.getBuffer().toString());

			w = new StringWriter();
			hw = new HtmlDecodeWriter(w);
			hw.write("&hel;&hellohello;&lt;&gt;&amp;&quot;&#39;");
			hw.flush();
			assertEquals("&hel;&hellohello;<>&\"\'", w.getBuffer().toString());

			w = new StringWriter();
			hw = new HtmlDecodeWriter(w);
			hw.write("&hel;&hellohello&lt;&gt;&amp;&quot;&#39;");
			hw.flush();
			assertEquals("&hel;&hellohello<>&\"\'", w.getBuffer().toString());
			// test offset interface.
			w = new StringWriter();
			hw = new HtmlDecodeWriter(w);
			hw.write("&&hellohello&lt;&gt;&amp;&quot;&#39;", 2, 14);
			hw.flush();
			assertEquals("hellohello<", w.getBuffer().toString());

			w = new StringWriter();
			hw = new HtmlDecodeWriter(w);
			hw.write("&h&amp;&hellohello&lt;&gt;&amp;&quot;&#39;");
			hw.flush();
			assertEquals("&h&&hellohello<>&\"\'", w.getBuffer().toString());

		} catch (IOException e)
		{
			e.printStackTrace();

		}

	}

}
