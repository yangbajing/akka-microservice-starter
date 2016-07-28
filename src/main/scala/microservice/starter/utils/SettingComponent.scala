package microservice.starter.utils

import javax.inject.Inject

import com.typesafe.config.Config
import eri.commons.config.SSConfig

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
case class HttpSetting(interface: String, port: Int)

case class ServerSetting(http: HttpSetting)

class SettingComponent @Inject()(config: Config) {
  private val ss = new SSConfig(config.getConfig(Constants.CONF_PREFIX))

  val server = ServerSetting(
    HttpSetting(ss.server.http.interface.as[String], ss.server.http.port.as[Int])
  )

}

