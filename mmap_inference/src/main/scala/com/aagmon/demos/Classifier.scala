package com.aagmon.demos

import com.aagmon.demos.Classifier.model
import ml.dmlc.xgboost4j.LabeledPoint
import ml.dmlc.xgboost4j.scala.{Booster, DMatrix, XGBoost}
import org.slf4j.{Logger, LoggerFactory}

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

//wraps the machine learning classifier logic
object Classifier {
  val logger: Logger = LoggerFactory.getLogger("StreamsClassifierModel")
  var model: Option[Booster] = None
  var num_processed: Int = 0
  var process_time: Long = 0
  var xgbInput = new DMatrix("/dev/shm/one_tx.csv?format=csv")

  def Init(modelFile:String): Unit = {
    if (model.isEmpty) {
      model = Some(XGBoost.loadModel(modelFile))
      logger.info(s"Model loaded from $modelFile")
    }
  }



  def predict(): (String, Float) = {
    val start_time = System.nanoTime

    val SIZE:Int = 8
    var idx:Int = 0

    // can also be expressed as:
    //val result:Array[Array[Float]] = model.get.predict(xgbInput)
    val prediction:Float = model
      .map(m => m.predict(xgbInput))
      .map(result => result(0)(0))
      .getOrElse(-1)
    //logger.info(recordID+" predicted " + prediction)
    process_time =  process_time + (System.nanoTime - start_time)
    num_processed += 1
    idx += 1
    (idx.toString, prediction)
  }

}
