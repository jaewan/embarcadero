package com.aagmon.demos

import predict.PredictRequest
import org.apache.kafka.streams.scala.kstream.{Branched, KStream}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsConfig, Topology}
import org.slf4j.LoggerFactory

import java.util.Properties
import sys.process._
import scala.tools.nsc.io.File

import java.io.{BufferedWriter, FileWriter}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import au.com.bytecode.opencsv.CSVWriter

//Brings all implicit conversions in scope
import org.apache.kafka.streams.scala.ImplicitConversions._
// Bring implicit default serdes in scope
import org.apache.kafka.streams.scala.serialization.Serdes._
import DomainSerdes._


object Main {
  val TRIALS_RUN:Int = 1
  val NUM_TX:Int = 1 //284807
  val logger = LoggerFactory.getLogger("StreamsAppMain")
  val modelPath:String = "/home/jae/kafka_ml/train/fraud_model.bin"
  val TOPIC:String = "creditcard-topic"
  Classifier.Init(modelPath)
  var kafka_addr:Option[String] = sys.env.get("KAFKA_SERVER_ADDR")
  if (kafka_addr.isEmpty){
    kafka_addr = Some("localhost:9092")
  }

  def getKafkaBrokerProperties(appID:String):Properties = {
    val bootstrapServer:String = scala.util.Properties.envOrElse("KAFKA_SERVER", kafka_addr.get)
    val conf = new Properties()
    conf.put(StreamsConfig.APPLICATION_ID_CONFIG, appID)
    conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    conf.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass)
    conf.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass)
    conf
  }


  def getStreamTopology(inputTopic:String):Topology = {

    val builder = new StreamsBuilder()
    val reqStream = builder.stream[String, PredictRequest](inputTopic)
    reqStream
      .map( (_, request) => {
        Classifier.predict(request.recordID, request.featuresVector)
      })
      .split()
      .branch((key, risk) => risk >= 0.5 , Branched.withConsumer(stream => stream.to("suspects-topic")))
      .branch((key, risk) => risk < 0.5 , Branched.withConsumer(stream => stream.to("regular-topic")))

    builder.build()

  }

  def main(args: Array[String]): Unit = {
    var trials_run:Int = 0
    val CREATE_TOPIC_CMD:String = "kafka-topics --create --topic " + TOPIC + " --bootstrap-server " + kafka_addr.get
    val schema = Array("Sleep","Inference","Deserialization")
    var res = List(schema)

    val delete_cmd1:String = "kafka-topics --delete --topic " + TOPIC + " --bootstrap-server " + kafka_addr.get
    val delete_cmd2:String = "kafka-topics --delete --topic suspects-topic  --bootstrap-server " + kafka_addr.get
    val delete_cmd3:String = "kafka-topics --delete --topic regular-topic  --bootstrap-server " + kafka_addr.get

    while(trials_run < TRIALS_RUN){
      CREATE_TOPIC_CMD.!

      logger.info("Starting App")
      "python /home/jae/kafka_ml/train/produce.py".run()
      val topology: Topology =
        getStreamTopology(TOPIC)
      val applicationProps = getKafkaBrokerProperties("test-stream-app-1")
      val streams = new KafkaStreams(topology, applicationProps)

      streams.cleanUp() // only for test
      logger.info("*****************\nStarting Stream*****************\n")
      streams.start()


      Runtime.getRuntime.addShutdownHook(new Thread {
        override def run(): Unit = {
          logger.info("*****************\nClosing Stream*****************\n")
          streams.close()
        }
      })

      var slept_time = 0
      while(Classifier.num_processed < NUM_TX){
        slept_time += 1
        Thread.sleep(50);
      }
      logger.info("Sleep Time: " + slept_time.toString)
      logger.info("Inference Time: " + Classifier.num_processed.toString)
      logger.info("Deserialization Time: " + PredictRequest.desTime.toString)
      logger.info("*****************\n Bye Bye \n*****************\n")

      res :+ Array(slept_time.toString, Classifier.num_processed.toString, PredictRequest.desTime.toString)

      delete_cmd1.!
      delete_cmd2.!
      delete_cmd3.!
      Thread.sleep(5000)
      trials_run += 1
    }
    val out = new BufferedWriter(new FileWriter("/home/jae/kafka_ml/inference_result.csv", true))
    val writer = new CSVWriter(out);

    writer.writeAll(res)

    out.close()
  }
}
