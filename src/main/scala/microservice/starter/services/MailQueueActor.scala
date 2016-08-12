package microservice.starter.services

import akka.actor.{Actor, Cancellable, Props}
import com.typesafe.scalalogging.StrictLogging
import microservice.starter.components.RedisComponent
import microservice.starter.domain.{EmailFormat, EmailMessage}
import microservice.starter.services.email.BaseEmailSender
import microservice.starter.utils.EmailSettingComponent
import org.apache.commons.mail.EmailException
import play.api.libs.json.Json

import scala.concurrent.duration._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
class MailQueueActor(emailSettingComponent: EmailSettingComponent,
                     redisComponent: RedisComponent) extends Actor with StrictLogging {

  import MailQueueActor._
  import context.dispatcher

  var cancellable: Cancellable = _
  @volatile var constantlyCount = 0

  override def preStart(): Unit = {
    constantlyCount = 0
    cancellable = context.system.scheduler.scheduleOnce(10.seconds, self, FetchQueue)
  }

  override def postStop(): Unit = {
    if (!cancellable.isCancelled) {
      cancellable.cancel()
    }
  }

  override def receive: Receive = {
    case FetchQueue =>
      try {
        handleQueue()
      } catch {
        case e: EmailException =>
          if (!cancellable.isCancelled) {
            cancellable.cancel()
          }
          cancellable = context.system.scheduler.scheduleOnce(10.seconds, self, FetchQueue)
          logger.error(s"[${self.path}] handleQueue error: $e", e)
      }
  }

  private def handleQueue(): Unit = {
    import microservice.starter.utils.PlayJsonSupport._
    redisComponent.withClient { client =>
      val result = client.brpop(1, MAIL_QUEUE)
      result match {
        case Some((name, value)) =>
          val emailMessage = Json.parse(value).as[EmailMessage]
          sendEmail(emailMessage) match {
            case Some(retMsg) =>
              constantlyCount += 1
            case None =>
              logger.error(s"[${self.path}] email setting: ${emailMessage.account} not found")
          }

          // 连续发送超过 CONSTANTLY_THRESHOLD 则暂停5秒
          if (constantlyCount < CONSTANTLY_THRESHOLD) {
            self ! FetchQueue
          } else {
            constantlyCount = 0
            cancellable = context.system.scheduler.scheduleOnce(5.seconds, self, FetchQueue)
          }

        case None => // 未读到邮件则暂停5秒
          constantlyCount = 0
          cancellable = context.system.scheduler.scheduleOnce(5.seconds, self, FetchQueue)
      }
    }
  }

  private def sendEmail(message: EmailMessage): Option[String] =
    emailSettingComponent.findEmailSetting(message.account).map { emailSetting =>
      val sender = BaseEmailSender(emailSetting.smtp, emailSetting.port, emailSetting.ssl, emailSetting.username,
        emailSetting.password)
      message.format.getOrElse(EmailFormat.TEXT) match {
        case EmailFormat.TEXT => sender.sendText(message)
        case EmailFormat.HTML => sender.sendHtml(message)
      }
    }

}

object MailQueueActor {

  val MAIL_QUEUE = s"cg:emailQueue"

  val CONSTANTLY_THRESHOLD = 20

  case object FetchQueue

  def props(emailSettingComponent: EmailSettingComponent,
            redisComponent: RedisComponent) =
    Props(new MailQueueActor(emailSettingComponent, redisComponent))

}