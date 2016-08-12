package microservice.starter.components

import javax.inject.{Inject, Singleton}

import com.redis.{RedisClient, RedisClientPool}
import microservice.starter.inject.ApplicationLifecycle
import microservice.starter.utils.SettingComponent

import scala.concurrent.Future

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
@Singleton
class RedisComponent @Inject()(settingComponent: SettingComponent,
                               lifecycle: ApplicationLifecycle) {

  private val _pool = new RedisClientPool(settingComponent.redis.host,
    settingComponent.redis.port,
    database = settingComponent.redis.database)

  lifecycle.addStopHook(() => Future.successful {
    _pool.close
  })

  def withClient[R](func: RedisClient => R): R = {
    _pool.withClient(func)
  }

}
