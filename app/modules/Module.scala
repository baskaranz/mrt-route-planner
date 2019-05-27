package modules

import com.google.inject.AbstractModule
import services.StationsMapper

class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[StationsMapper]).asEagerSingleton
  }
}
