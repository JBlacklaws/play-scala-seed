package controllers

import uk.gov.hmrc.play.test.UnitSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.ControllerComponents

class ApplicationControllerSpec extends UnitSpec with GuiceOneAppPerSuite {
  val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
  object TestApplicationController extends ApplicationController(
    controllerComponents
  )

  "ApplicationController .index()" should {

  }

  "ApplicationController .create()" should {

  }

  "ApplicationController .read(id: String)" should {

  }

  "ApplicationController .update(id: String)" should {

  }

  "ApplicationController .delete(id: String)" should {

  }
}
