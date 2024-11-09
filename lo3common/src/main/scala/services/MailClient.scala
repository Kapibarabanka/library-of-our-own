package kapibarabanka.lo3.common
package services

import java.io.File
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.*
import javax.mail.util.ByteArrayDataSource
import scala.util.Try

object MailClient {
  private val senderEmail    = AppConfig.senderEmail
  private val senderPassword = AppConfig.senderPassword

  private val hostName = "mail.gmx.com"
  private val port     = "587"
  private val auth = new Authenticator() {
    override protected def getPasswordAuthentication = new PasswordAuthentication(senderEmail, senderPassword)
  }

  private val properties = new Properties
  properties.setProperty("mail.transport.protocol", "smtp")
  properties.setProperty("mail.user", senderEmail)
  properties.setProperty("mail.password", senderPassword)
  properties.put("mail.smtp.host", hostName)
  properties.put("mail.smtp.port", port)
  properties.put("mail.smtp.starttls.enable", "true")
  properties.put("mail.smtp.auth", "true")
  properties.put("mail.smtp.ssl.trust", hostName)
  properties.put("mail.smtp.starttls.required", "true")
  properties.put("mail.smtp.ssl.protocols", "TLSv1.2")

  def send(text: String, receiverEmail: String): Try[Unit] = {
    Try({
      val session = Session.getInstance(properties, auth)
      val msg     = new MimeMessage(session)
      msg.setText(text)
      msg.setFrom(new InternetAddress(senderEmail))
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail))
      Transport.send(msg)
    })
  }

  def sendBytes(bytes: Array[Byte], fileName: String, receiverEmail: String): Try[Unit] = {
    Try({
      val session = Session.getInstance(properties, auth)
      val msg     = new MimeMessage(session)
      msg.setFrom(new InternetAddress(senderEmail))
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail))

      val multipart      = new MimeMultipart()
      val attachmentPart = new MimeBodyPart()
      val bds            = new ByteArrayDataSource(bytes, "application/octet-stream");
      attachmentPart.setDataHandler(new DataHandler(bds));
      attachmentPart.setFileName(fileName)
      multipart.addBodyPart(attachmentPart)

      msg.setContent(multipart)
      Transport.send(msg)
    })
  }

  def sendFile(file: File, fileName: String, receiverEmail: String): Unit = {
    val session = Session.getInstance(properties, auth)
    val msg     = new MimeMessage(session)
    msg.setFrom(new InternetAddress(senderEmail))
    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail))

    val multipart      = new MimeMultipart()
    val attachmentPart = new MimeBodyPart()
    attachmentPart.attachFile(file)
    attachmentPart.setFileName(fileName)
    multipart.addBodyPart(attachmentPart)

    msg.setContent(multipart)
    Transport.send(msg)
  }
}
