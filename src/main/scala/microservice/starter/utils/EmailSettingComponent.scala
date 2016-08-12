package microservice.starter.utils

import javax.inject.{Inject, Singleton}

import com.typesafe.config.Config

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
case class EmailSetting(smtp: String,
                        port: Int,
                        ssl: Boolean,
                        nickname: String,
                        username: String,
                        password: String)

case class NotifiesSetting(apikey: List[String])

@Singleton
class EmailSettingComponent @Inject()(config: Config) {
  private val PREFIX = "microservice.email"

  private def getEmailSetting(partial: String) =
    EmailSetting(
      config.getString(s"$PREFIX.$partial.smtp"),
      config.getInt(s"$PREFIX.$partial.port"),
      config.getBoolean(s"$PREFIX.$partial.ssl"),
      config.getString(s"$PREFIX.$partial.nickname"),
      config.getString(s"$PREFIX.$partial.username"),
      config.getString(s"$PREFIX.$partial.password"))

  val hellocodeHellocode = getEmailSetting(EmailSettingComponent.HELLOCODE_HELLOCODE)

  def findEmailSetting(name: String): Option[EmailSetting] = {
    if (name.contains(EmailSettingComponent.HELLOCODE_HELLOCODE)) Some(hellocodeHellocode)
    else None
  }
}

object EmailSettingComponent {
  val HELLOCODE_HELLOCODE = "hellocode.hellocode"
}
