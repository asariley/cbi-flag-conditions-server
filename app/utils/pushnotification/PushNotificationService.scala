package utils.pushnotification

import akka.actor.{ Actor, ActorRef, Props }
import java.net.InetSocketAddress
import java.io.{FileInputStream, IOException}
import java.nio.ByteOrder

import play.api.Logger
import akka.util.{ByteString, ByteStringBuilder}
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManagerFactory
import java.security.KeyStore
import akka.stream.io.{SslTls, Closing, Client, NegotiateNewSession, SslTlsOutbound, SslTlsInbound, SendBytes}
import akka.stream.ActorFlowMaterializer

import akka.stream.scaladsl.{Tcp, Flow, Source, Sink, BidiFlow}

import play.api.libs.concurrent.Akka
import play.api.Play.{current => currentPlayApp}


sealed abstract class Notification

/** TODO implement complete notification spec
final case class DetailedNotification(
    address: String,
    alert: Option[Alert],
    badge: Option[Int],
    sound: Option[String],
    contentAvailable: Boolean,
    customData: Map[String, JsValue] //Which Json AST should we use?
) extends Notification
final case class Alert(
    title: String,
    body: String,
    titleLocKey: Option[String],
    titleLocArgs: Option[Seq[String]],
    actionLocKey: Option[String],
    locKey: Option[String],
    locArgs: Option[Seq[String]],
    launchImage: Option[String]
)
*/
final case class SimpleNotification(address: String, alert: String) extends Notification
final case class EmptyNotification(address: String) extends Notification

//interface: Submit a Source to run from
object PushService {
    implicit val actorSystem = Akka.system //FIXME make this a param?

    lazy val sslContext: SSLContext = {
        val cert: FileInputStream = new FileInputStream("/Users/asariley/Dropbox/cbifc/package_dev.p12") //FIXME make the cert be an input param to this thing
        val password = "changeme" //FIXME make this input param

        val ks: KeyStore = KeyStore.getInstance(constants.KEYSTORE_TYPE)
        ks.load(cert, password.toArray)
        val kmf: KeyManagerFactory = KeyManagerFactory.getInstance(constants.KEY_ALGORITHM)
        kmf.init(ks, password.toArray)
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(constants.KEY_ALGORITHM)
        tmf.init(ks)
        val context: SSLContext = SSLContext.getInstance("TLS")
        context.init(kmf.getKeyManagers(), null, null)
        context
    }

    lazy val cipherSuites = NegotiateNewSession.withCipherSuites("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA")

    def tlsStuff: BidiFlow[SslTlsOutbound, ByteString, ByteString, SslTlsInbound, Unit] = {
        SslTls(sslContext, cipherSuites, Client, Closing.ignoreComplete)
    }

    def makeNotification: ByteString = {
        implicit val byteOrder = ByteOrder.BIG_ENDIAN

        val DEVICE_TOKEN_ID: Byte = 1
        val PAYLOAD_ID: Byte = 2
        val NOTIFICATION_ID: Byte = 3
        val EXPIRATION_DATE_ID: Byte = 4
        val PRIORITY_ID: Byte = 5

        val deviceToken: Array[Byte] = constants.myIdBytes
        val payload: Array[Byte] = """{"aps":{"alert":"Push Notifications Ready!"}}""".getBytes("UTF-8")
        val notificationId: Int = 1
        val nowSeconds: Int = (System.currentTimeMillis/1000L).asInstanceOf[Int]
        val expiration: Int =  nowSeconds + 2/*HOURS*/ * 60 * 60 /*SECONDS*/
        val priority: Byte = 10 //NOW!

        var bytes: ByteStringBuilder = ByteString.newBuilder
        bytes.putByte(2).putInt(deviceToken.length + payload.length + 4 + 4 + 1) //FRAME INFO
            .putByte(DEVICE_TOKEN_ID).putShort(deviceToken.length.asInstanceOf[Short]).putBytes(deviceToken)
            .putByte(PAYLOAD_ID).putShort(payload.length.asInstanceOf[Short]).putBytes(payload)
            .putByte(NOTIFICATION_ID).putShort(4).putInt(notificationId)
            .putByte(EXPIRATION_DATE_ID).putShort(4).putInt(expiration)
            .putByte(PRIORITY_ID).putShort(1).putByte(priority)
            .result()
    }

    def start(): Unit = {
        Logger.info("Starting Push")

        implicit val materializer = ActorFlowMaterializer()

        val byteFlow: Flow[SslTlsOutbound, SslTlsInbound, Unit] = tlsStuff join Tcp().outgoingConnection(constants.APNS_ADDRESS)

        Source(List(SendBytes(makeNotification)))
            .via(byteFlow)
            .runWith(Sink.foreach(elem => Logger.debug(s"received $elem from apns")))
    }
}


/**
TODO
Make keystore a param
Make password a param
Make interface be "connect a Source[Notification]"
Construct Flow to support resending notifications
*/

