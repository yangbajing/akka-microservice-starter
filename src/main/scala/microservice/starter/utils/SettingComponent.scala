package microservice.starter.utils

import javax.inject.Inject

import com.typesafe.config.Config
import eri.commons.config.SSConfig

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
case class HttpSetting(interface: String, port: Int)

case class ServerSetting(http: HttpSetting)

case class SettingRedis(host: String, port: Int, database: Int)

class SettingComponent @Inject()(config: Config) {
  private val ssc = new SSConfig(config.getConfig(Constants.CONF_PREFIX))

  val server = ServerSetting(
    HttpSetting(ssc.server.http.interface.as[String], ssc.server.http.port.as[Int])
  )

  val redis = SettingRedis(
    ssc.redis.host.as[String],
    ssc.redis.port.as[Int],
    ssc.redis.database.as[Int]
  )

}

