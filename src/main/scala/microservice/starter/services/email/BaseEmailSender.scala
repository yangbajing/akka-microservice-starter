package microservice.starter.services.email

import com.typesafe.scalalogging.LazyLogging
import microservice.starter.domain.EmailMessage
import microservice.starter.utils.Constants
import org.apache.commons.mail.{Email, EmailException, HtmlEmail, SimpleEmail}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
/**
  * 一个简单封装的commons-email邮件发送者。
  *
  * @param smtp     SMTP服务器
  * @param port     SMTP服务器端口
  * @param ssl      是否使用SSL
  * @param username 发送邮箱账号
  * @param password 发送邮箱账号密码
  */
case class BaseEmailSender(smtp: String,
                           port: Int,
                           ssl: Boolean,
                           username: String,
                           password: String) extends LazyLogging {

  private def setEmail(email: Email, message: EmailMessage): Unit = {
    email.setCharset(message.charset.getOrElse(Constants.CHARSET))
    email.setHostName(smtp)
    email.setSmtpPort(port)
    email.setAuthentication(username, password)
    email.setSSLOnConnect(true)
    email.addTo(message.tos: _*)
    email.setFrom(username, message.nickname, message.charset.getOrElse(Constants.CHARSET))
    email.setSubject(message.subject)

  }

  @throws(classOf[EmailException])
  def sendHtml(message: EmailMessage): String = {
    val email: HtmlEmail = new HtmlEmail
    setEmail(email, message)
    email.setHtmlMsg(message.content)
    email.send()
  }

  @throws(classOf[EmailException])
  def sendText(message: EmailMessage): String = {
    val email: SimpleEmail = new SimpleEmail
    setEmail(email, message)
    email.setMsg(message.content)
    email.send()
  }

}
