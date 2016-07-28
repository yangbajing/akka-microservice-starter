package microservice.starter

import com.google.inject.Guice
import com.typesafe.scalalogging.StrictLogging
import microservice.starter.inject.DefaultApplicationLifecycle

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
object AppServer extends StrictLogging {
  val injector = Guice.createInjector(new AppModule())

  def stop(): Unit = {
    val future = injector.getInstance(classOf[DefaultApplicationLifecycle]).stop()
    val status = Await.result(future, 60.seconds)
    logger.info("stop status: " + status)
  }

}
