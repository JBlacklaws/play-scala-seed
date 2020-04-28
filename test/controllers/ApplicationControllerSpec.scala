package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.stream.ActorMaterializer
import models.DataModel
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContent, AnyContentAsText, ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.http.Status
import repositories.DataRepository
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}
import play.api.test
import reactivemongo.api.commands.{LastError, WriteResult}
import reactivemongo.core.errors.GenericDriverException

import scala.concurrent.{ExecutionContext, Future}

class ApplicationControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar{
  val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
  val mockDataRepository: DataRepository = mock[DataRepository]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  object TestApplicationController extends ApplicationController(
    controllerComponents, mockDataRepository, ec
  )

  implicit val system: ActorSystem = ActorSystem("Sys")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val dataModel: DataModel = DataModel(
    "abcd",
    "test name",
    "test description",
    100
  )

  val jsonBody: JsObject = Json.obj(
    "_id" -> "abcd",
    "name" -> "test name",
    "description" -> "test description",
    "numSales" -> 100
  )

  val jsonBodyInvalid: JsObject = Json.obj(
    "_id" -> 12,
    "name" -> 34,
    "description" -> 58,
    "numSales" -> "Sam"
  )

//  val resultValid = TestApplicationController.create()(FakeRequest().withBody(jsonBody))
//  val resultInvalid: Future[Result] = TestApplicationController.create()(FakeRequest().withBody(jsonBodyInvalid))

//  when(mockDataRepository.find(any())(any()))
//    .thenReturn(Future(List(dataModel)))

  "ApplicationController .index()" when {

    "the json body is valid" should {
      when(mockDataRepository.find(any())(any()))
        .thenReturn(Future(List(dataModel)))

      val result = TestApplicationController.index()(FakeRequest())

      "return the correct JSON" in {
        await(jsonBodyOf(result)) shouldBe Json.arr(jsonBody)
      }

      "return OK" in {
        status(result) shouldBe Status.OK
      }
    }
    "the mongo data indexing failed" should {
      when(mockDataRepository.find(any())(any()))
        .thenReturn(Future.failed(GenericDriverException("Error")))

      val result = TestApplicationController.index()(FakeRequest())

      "return an error" in {
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR

        await(jsonBodyOf(result)) shouldBe Json.obj("message" -> "Error finding item in Mongo")
      }
    }
  }

  "ApplicationController .create()" when {

    "the json body is valid" should {
      val writeResult: WriteResult = LastError(ok = true, None,
        None, None, 0, None, updatedExisting = false, None, None, wtimeout = false, None, None)

      when(mockDataRepository.create(any()))
        .thenReturn(Future(writeResult))

      val resultValid = TestApplicationController
        .create()(FakeRequest().withBody(jsonBody))

      "return Created" in {
        status(resultValid) shouldBe Status.CREATED
      }
    }
    "the jsonbody is not valid" should {
      val resultInvalid: Future[Result] = TestApplicationController
        .create()(FakeRequest().withBody(jsonBodyInvalid))

      "return BAD_REQUEST" in {
        status(resultInvalid) shouldBe Status.BAD_REQUEST
      }
    }
    "the mongo data creation failed" should {
      when(mockDataRepository.create(any()))
        .thenReturn(Future.failed(GenericDriverException("Error")))

      "return an error" in {
        val result = TestApplicationController
          .create()(FakeRequest().withBody(jsonBody))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR

        await(bodyOf(result)) shouldBe Json.obj("message" -> "Error creating item in Mongo").toString()
      }
    }
  }

  "ApplicationController .read(id: String)" when {

    "the supplied JSON is valid" should {
      when(mockDataRepository.read(any()))
        .thenReturn(Future(dataModel))

      val resultValid = TestApplicationController
        .read("abcd")(FakeRequest())

      "return the correct json" in {
        await(jsonBodyOf(resultValid)) shouldBe jsonBody
      }

      "return the OK status" in{
        status(resultValid) shouldBe Status.OK
      }
    }

    "the mongo data read failed" should {
      when(mockDataRepository.read(any()))
        .thenReturn(Future.failed(GenericDriverException("Error")))

      "return an error" in {
        val result = TestApplicationController
          .read("abcd")(FakeRequest())

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR

        await(jsonBodyOf(result)) shouldBe Json.obj("message" -> "Error reading item in Mongo")
      }
    }
  }

  "ApplicationController .update(id: String)" should {

    "the supplied JSON is valid" should {
      when(mockDataRepository.update(any()))
        .thenReturn(Future(dataModel))

      val resultValid = TestApplicationController
        .update("abcd")(FakeRequest().withBody(jsonBody))

      "return the correct JSON" in {
        await(jsonBodyOf(resultValid)) shouldBe jsonBody
      }

      "return the ACCEPTED status" in {
        status(resultValid) shouldBe Status.ACCEPTED
      }
    }
    "the supplied JSON is invalid" should {
      when(mockDataRepository.update(any()))
        .thenReturn(Future(dataModel))

      val resultInvalid = TestApplicationController
        .update("zxc")(FakeRequest().withBody(jsonBodyInvalid))

      "return the BadRequest status" in {
        status(resultInvalid) shouldBe Status.BAD_REQUEST
      }
    }

    "the mongo data update failed" should {
      when(mockDataRepository.update(any()))
        .thenReturn(Future.failed(GenericDriverException("Error")))

      "return an error" in {
        val result = TestApplicationController
          .update("abcd")(FakeRequest().withBody(jsonBody))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR

        await(jsonBodyOf(result)) shouldBe Json.obj("message" -> "Error updating item in Mongo")
      }
    }
  }

  "ApplicationController .delete(id: String)" should {

    "the supplied JSON is valid" should {
      val writeResult: WriteResult = LastError(ok = true, None,
        None, None, 0, None, updatedExisting = false, None, None, wtimeout = false, None, None)

      when(mockDataRepository.delete(any()))
        .thenReturn(Future(writeResult))

      val resultValid = TestApplicationController
        .delete("acbd")(FakeRequest())

      "return the ACCEPTED status" in {
        status(resultValid) shouldBe Status.ACCEPTED
      }
    }
    "the mongo data delete failed" should {
      when(mockDataRepository.delete(any()))
        .thenReturn(Future.failed(GenericDriverException("Error")))

        "return an error" in {
          val result = TestApplicationController
            .delete("abcd")(FakeRequest())

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR

          await(jsonBodyOf(result)) shouldBe Json.obj("message" -> "Error deleting item in Mongo")
        }
    }
  }
}
