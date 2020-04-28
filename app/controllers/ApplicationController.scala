package controllers

import javax.inject.Inject
import jdk.net.SocketFlow.Status
import models.DataModel
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Result}
import reactivemongo.core.errors.{DatabaseException, DriverException, GenericDriverException, ReactiveMongoException}
import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(val controllerComponents: ControllerComponents, val dataRepository: DataRepository,
                                      implicit  val ec: ExecutionContext) extends BaseController{
  def index(): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.find().map(items => Ok(Json.toJson(items))) recover {
      case _: ReactiveMongoException => InternalServerError(Json.obj(
        "message" -> "Error finding item in Mongo"
      ))
    }
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.create(dataModel).map(_ => Created) recover {
          case _: ReactiveMongoException => InternalServerError(Json.obj(
            "message" -> "Error creating item in Mongo"
          ))
        }
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.read(id).map(items => Ok(Json.toJson(items))) recover {
      case _: ReactiveMongoException => InternalServerError(Json.obj(
        "message" -> "Error reading item in Mongo"
      ))
    }
  }

  def update(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.update(dataModel).map(items => Accepted(Json.toJson(items))) recover {
          case _: ReactiveMongoException => InternalServerError(Json.obj(
            "message" -> "Error updating item in Mongo"
          ))
        }
      case JsError(_) => Future(BadRequest)
    }
  }


  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.delete(id).map(_ => Status(ACCEPTED)) recover {
      case _: ReactiveMongoException => InternalServerError(Json.obj(
        "message" -> "Error deleting item in Mongo"
      ))
    }
  }
}
