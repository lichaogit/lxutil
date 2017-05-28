package org.lx.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import sun.misc.BASE64Encoder;

public class EmailAccount
{
	String m_host = null;

	String m_user = null;

	String m_passwd = null;

	boolean m_auth = false;

	Session m_mailSession = null;

	final String EMAIL_CHARSET = "UTF-8";

	protected void init(String host, int port, boolean ssl, boolean auth,
			String user, String password)
	{
		m_host = host;
		m_auth = auth;
		m_user = user;
		m_passwd = password;

		Properties props = new Properties();
		props.put("mail.smtp.host", m_host);
		props.put("mail.smtp.auth", String.valueOf(m_auth));

		// props.put("mail.smtp.starttls.enable", "true");
		// props.put("mail.smtp.EnableSSL.enable", "true");
		String sslStr = Boolean.toString(ssl);

		props.put("mail.smtp.starttls.enable", sslStr);
		props.put("mail.smtp.EnableSSL.enable", sslStr);
		props.setProperty("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", Integer.toString(port));

		m_mailSession = Session.getDefaultInstance(props);
		m_mailSession.setDebug(false);// show debug in console
	}

	public EmailAccount(String host, boolean ssl, boolean auth, String user,
			String password)
	{
		int sendPort = ssl ? 465 : 25;
		init(host, sendPort, ssl, auth, user, password);
	}

	public EmailAccount(String host, int port, boolean ssl, boolean auth,
			String user, String password)
	{
		init(host, port, ssl, auth, user, password);
	}

	public Message createTextMessage(String fromEmail, String[] toEmails,
			String subject, String content) throws MessagingException
	{
		Message message = new MimeMessage(m_mailSession);
		addSendInfo(message, fromEmail, toEmails, subject);
		message.setText(content);
		message.saveChanges();
		return message;
	}

	protected MimeBodyPart createAttachment(DataSource ds) throws Exception
	{
		MimeBodyPart attachmentPart = new MimeBodyPart();
		attachmentPart.setDataHandler(new DataHandler(ds));
		attachmentPart.setFileName(ds.getName());
		return attachmentPart;
	}

	/**
	 * body,
	 * @param body
	 *        :"text/html;charset=gbk"
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	protected MimeBodyPart createContent(String bodyType, String body,
			HashMap elements) throws Exception
	{
		// 用于保存最终正文部分
		MimeBodyPart contentBody = new MimeBodyPart();
		// 用于组合文本和图片，"related"型的MimeMultipart对象
		MimeMultipart contentMulti = new MimeMultipart("related");

		// text part
		MimeBodyPart textBody = new MimeBodyPart();
		textBody.setContent(body, bodyType);
		contentMulti.addBodyPart(textBody);

		// media part.
		if (elements != null && elements.size() > 0)
		{
			Iterator it = elements.keySet().iterator();
			String dsId = null;
			DataSource ds = null;
			MimeBodyPart jpgBody = null;
			while (it.hasNext())
			{
				dsId = (String) it.next();
				ds = (DataSource) elements.get(dsId);

				jpgBody = new MimeBodyPart();
				jpgBody.setDataHandler(new DataHandler(ds));
				jpgBody.setContentID(dsId);
				contentMulti.addBodyPart(jpgBody);
			}
		}
		contentBody.setContent(contentMulti);
		return contentBody;
	}

	protected void addSendInfo(Message message, String fromEmail,
			String[] toEmails, String subject) throws AddressException,
			MessagingException
	{
		message.setFrom(new InternetAddress(fromEmail));
		for (int i = 0; i < toEmails.length; i++)
		{
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					toEmails[i]));
		}
		// add charset info.
		BASE64Encoder enc = new BASE64Encoder();
		try
		{
			subject = "=?" + EMAIL_CHARSET + "?B?"
					+ enc.encode(subject.getBytes(EMAIL_CHARSET)) + "?=";
			message.setSubject(subject);
		} catch (UnsupportedEncodingException e)
		{
		}
	}

	public MimeMessage createHtmlMessage(String fromEmail, String[] toEmails,
			String subject, String bodyHtml, HashMap contentElement,
			DataSource[] attachments) throws Exception
	{
		MimeMessage msg = new MimeMessage(m_mailSession);
		addSendInfo(msg, fromEmail, toEmails, subject);

		MimeMultipart allPart = new MimeMultipart("mixed");
		MimeBodyPart content = createContent("text/html;charset="
				+ EMAIL_CHARSET, bodyHtml, contentElement);

		// add content part.
		allPart.addBodyPart(content);

		// add attachment part.
		MimeBodyPart attachmentPart = null;
		if (attachments != null && attachments.length > 0)
		{
			for (int i = 0; i < attachments.length; i++)
			{
				attachmentPart = createAttachment(attachments[i]);
				allPart.addBodyPart(attachmentPart);
			}
		}
		msg.setContent(allPart);
		msg.saveChanges();
		return msg;
	}

	public void send(Message message) throws AddressException,
			MessagingException
	{
		String subject = message.getSubject();

		Transport transport = m_mailSession.getTransport("smtp");
		transport.connect(m_host, m_user, m_passwd);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}

}
