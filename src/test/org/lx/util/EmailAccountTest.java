package test.org.lx.util;

import java.io.FileOutputStream;
import java.util.HashMap;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;

import junit.framework.TestCase;

import org.lx.util.EmailAccount;
import org.lx.util.StringEx;

public class EmailAccountTest extends TestCase
{
	public void testEmailMessage()
	{
		EmailAccount account = new EmailAccount("smtp.gmail.com", true, true,
				"zlc123456789@gmail.com", "Zlazlx2011");
		HashMap elements = new HashMap();
		String model_file = StringEx.getPackagePath(EmailAccountTest.class);
		String baseDir = "G:\\studio\\WebContent\\src\\test\\org\\lx\\util\\EmailAccount\\";
		// model_file+ "/EmailAccount";

		elements.put("logo_jpg", new FileDataSource(baseDir + "\\logo.jpg"));

		DataSource[] attachments = new DataSource[2];
		attachments[0] = new FileDataSource(baseDir + "urlRule.xml");
		attachments[1] = new FileDataSource(baseDir + "李代沫-到不了.mp3");

		String bodyHtml = "<h4>内含附件、图文并茂的邮件测试！！！</h4> </br>"
				+ "<a href = http://haolloyin.blog.51cto.com/> 蚂蚁</a></br>"
				+ "<img src = \"cid:logo_jpg\"></a>";

		try
		{
			Message message = account.createHtmlMessage(
					"zlc123456789@gmail.com",
					new String[] { "max_zhaolc@hotmail.com" }, "test email",
					bodyHtml, elements, attachments);
			message.writeTo(new FileOutputStream("G:\\withAttachmentMail.eml"));
			account.send(message);
		} catch (Exception e)
		{
			assertTrue(false);
		}

	}
}
