package microservice.starter

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}
import microservice.starter.inject._
import microservice.starter.utils.http.HttpClient

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
class AppModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[ActorSystem]).toProvider(classOf[ActorSystemProvider])
    bind(classOf[Materializer]).toProvider(classOf[MaterializerProvider])

    bind(classOf[ExecutionContextExecutor]).toProvider(classOf[ExecutionContextProvider])
    bind(classOf[ExecutionContext]).to(classOf[ExecutionContextExecutor])

    bind(classOf[Config]).toInstance(ConfigFactory.load())

    bind(classOf[HttpClient]).toProvider(classOf[HttpClientProvider])
  }

}
