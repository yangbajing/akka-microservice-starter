package microservice.starter.inject

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.Provider
import com.typesafe.scalalogging.StrictLogging
import microservice.starter.utils.http.HttpClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
@Singleton
class ActorSystemProvider @Inject()(applicationLifecycle: ApplicationLifecycle) extends Provider[ActorSystem] with StrictLogging {
  override def get(): ActorSystem = {
    val system = ActorSystem()
    applicationLifecycle.addStopHook(() => {
      system.terminate()
      // wait until it is shutdown
      val status = Await.result(system.whenTerminated, Duration.Inf)
      Future.successful(status)
    })
    system
  }
}

@Singleton
class MaterializerProvider @Inject()(implicit val system: ActorSystem) extends Provider[Materializer] {
  override def get(): Materializer = ActorMaterializer()
}

@Singleton
class ExecutionContextProvider @Inject()(actorSystem: ActorSystem) extends Provider[ExecutionContextExecutor] {
  def get: ExecutionContextExecutor = actorSystem.dispatcher
}

@Singleton
class HttpClientProvider @Inject()(actorSystem: ActorSystem,
                                   applicationLifecycle: ApplicationLifecycle) extends Provider[HttpClient] {
  override def get(): HttpClient = {
    val httpClient = HttpClient()
    applicationLifecycle.addStopHook(() => Future.successful(httpClient.close()))
    httpClient
  }
}

