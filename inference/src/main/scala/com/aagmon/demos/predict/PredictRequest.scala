// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.aagmon.demos.predict
import org.slf4j.LoggerFactory

@SerialVersionUID(0L)
final case class PredictRequest(
    recordID: _root_.scala.Predef.String = "",
    featuresVector: _root_.scala.Seq[_root_.scala.Float] = _root_.scala.Seq.empty,
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[PredictRequest] {
    private[this] def featuresVectorSerializedSize = {
      4 * featuresVector.size
    }
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = recordID
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
        }
      };
      if (featuresVector.nonEmpty) {
        val __localsize = featuresVectorSerializedSize
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__localsize) + __localsize
      }
      __size += unknownFields.serializedSize
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      {
        val __v = recordID
        if (!__v.isEmpty) {
          _output__.writeString(1, __v)
        }
      };
      if (featuresVector.nonEmpty) {
        _output__.writeTag(4, 2)
        _output__.writeUInt32NoTag(featuresVectorSerializedSize)
        featuresVector.foreach(_output__.writeFloatNoTag)
      };
      unknownFields.writeTo(_output__)
    }
    def withRecordID(__v: _root_.scala.Predef.String): PredictRequest = copy(recordID = __v)
    def clearFeaturesVector = copy(featuresVector = _root_.scala.Seq.empty)
    def addFeaturesVector(__vs: _root_.scala.Float*): PredictRequest = addAllFeaturesVector(__vs)
    def addAllFeaturesVector(__vs: Iterable[_root_.scala.Float]): PredictRequest = copy(featuresVector = featuresVector ++ __vs)
    def withFeaturesVector(__v: _root_.scala.Seq[_root_.scala.Float]): PredictRequest = copy(featuresVector = __v)
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = recordID
          if (__t != "") __t else null
        }
        case 4 => featuresVector
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PString(recordID)
        case 4 => _root_.scalapb.descriptors.PRepeated(featuresVector.iterator.map(_root_.scalapb.descriptors.PFloat(_)).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.aagmon.demos.predict.PredictRequest
    // @@protoc_insertion_point(GeneratedMessage[com.aagmon.demos.PredictRequest])
}

object PredictRequest extends scalapb.GeneratedMessageCompanion[com.aagmon.demos.predict.PredictRequest] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.aagmon.demos.predict.PredictRequest] = this
  val logger = LoggerFactory.getLogger("StreamsAppMain")
  var desTime:Long = 0
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.aagmon.demos.predict.PredictRequest = {
    val start_time = System.nanoTime
    var __recordID: _root_.scala.Predef.String = ""
    val __featuresVector: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Float] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Float]
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __recordID = _input__.readStringRequireUtf8()
        case 37 =>
          __featuresVector += _input__.readFloat()
        case 34 => {
          val length = _input__.readRawVarint32()
          val oldLimit = _input__.pushLimit(length)
          while (_input__.getBytesUntilLimit > 0) {
            __featuresVector += _input__.readFloat()
          }
          _input__.popLimit(oldLimit)
        }
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    val ret = com.aagmon.demos.predict.PredictRequest(
        recordID = __recordID,
        featuresVector = __featuresVector.result(),
        unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
    val end_time = System.nanoTime
    desTime += (end_time - start_time)
    //logger.info("=============================")
    //logger.info((end_time - start_time).toString)
    //logger.info("=============================")
    ret
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.aagmon.demos.predict.PredictRequest] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      com.aagmon.demos.predict.PredictRequest(
        recordID = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        featuresVector = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Seq[_root_.scala.Float]]).getOrElse(_root_.scala.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = PredictProto.javaDescriptor.getMessageTypes().get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = PredictProto.scalaDescriptor.messages(0)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.aagmon.demos.predict.PredictRequest(
    recordID = "",
    featuresVector = _root_.scala.Seq.empty
  )
  implicit class PredictRequestLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.aagmon.demos.predict.PredictRequest]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.aagmon.demos.predict.PredictRequest](_l) {
    def recordID: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.recordID)((c_, f_) => c_.copy(recordID = f_))
    def featuresVector: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Float]] = field(_.featuresVector)((c_, f_) => c_.copy(featuresVector = f_))
  }
  final val RECORDID_FIELD_NUMBER = 1
  final val FEATURESVECTOR_FIELD_NUMBER = 4
  def of(
    recordID: _root_.scala.Predef.String,
    featuresVector: _root_.scala.Seq[_root_.scala.Float]
  ): _root_.com.aagmon.demos.predict.PredictRequest = _root_.com.aagmon.demos.predict.PredictRequest(
    recordID,
    featuresVector
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[com.aagmon.demos.PredictRequest])
}
