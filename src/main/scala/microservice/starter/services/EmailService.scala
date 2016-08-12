package microservice.starter.services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import microservice.starter.components.RedisComponent
import microservice.starter.domain.EmailMessage
import microservice.starter.utils.EmailSettingComponent
import play.api.libs.json.Json

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
@Singleton
class EmailService @Inject()(redisComponent: RedisComponent,
                             system: ActorSystem,
                             emailSettingComponent: EmailSettingComponent) {

  private val emailActor = system.actorOf(MailQueueActor.props(emailSettingComponent, redisComponent), "email-queue")

  def sendEmail(message: EmailMessage): Option[Long] = {
    import microservice.starter.utils.PlayJsonSupport._
    redisComponent.withClient { client =>
      val value = Json.stringify(Json.toJson(message))
      client.lpush(MailQueueActor.MAIL_QUEUE, value)
    }
  }

}
