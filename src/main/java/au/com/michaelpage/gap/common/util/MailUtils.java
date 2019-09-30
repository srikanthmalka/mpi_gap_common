package au.com.michaelpage.gap.common.util;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.michaelpage.gap.common.Settings;

public class MailUtils {
	private static final Logger logger = LoggerFactory.getLogger(MailUtils.class);
	private static final String SMTP_HOST = "APAMailRelay.michaelpage.local";
	private static final String FROM = "APAUgap-support@michaelpage.com.au";
	private static final String TO = "APAUgap-support@michaelpage.com.au";

	public void sendUnknownJobBoardsEmail(List<String> jobBoards, String fileName) {
		String subject = "Unknown job boards - " + Settings.INSTANCE.getHostName() + " - " + jobBoards;
		String body = subject + "\r\n\r\n" + "File: " + fileName; 
		sendEmail(subject, body);
	}

	private void sendEmail(String subject, String message) {
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", SMTP_HOST);

		Session session = Session.getDefaultInstance(properties, null);

		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setSentDate(new Date());
			msg.setFrom(new InternetAddress(FROM));
			msg.setRecipients(Message.RecipientType.TO, new InternetAddress[] { new InternetAddress(TO) });
			msg.setSubject(subject, "UTF-8");
			msg.setContent(createMimeMultipart(message));

			Transport transport = session.getTransport("smtp");
			transport.connect();
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
		} catch (Throwable t) {
			logger.error("Unable to send email. Error message: " + t.getMessage(), t);
		}
	}

	private MimeMultipart createMimeMultipart(String message) throws MessagingException {
		MimeMultipart mimeMultipart = new MimeMultipart();
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(message + System.getProperty("line.separator") + System.getProperty("line.separator")
				+ System.getProperty("line.separator"));
		mimeMultipart.addBodyPart(messageBodyPart);
		return mimeMultipart;
	}

	public void sendUnknownJobBoardsEmailFailureNotification(List<String> jobBoards, String fileName) {
		String subject = "Found an Unknown job board that has more than 20 applications and aborting the process - " + Settings.INSTANCE.getHostName() + " - " + jobBoards;
		String body = subject + "\r\n\r\n" + "File: " + fileName; 
		sendEmail(subject, body);
	}
}
