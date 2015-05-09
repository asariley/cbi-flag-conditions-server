package utils.pushnotification

import scala.annotation.tailrec

import java.lang.IllegalStateException

import java.net.InetSocketAddress

object constants {
    val APNS_ADDRESS = new InetSocketAddress("gateway.sandbox.push.apple.com", 2195) //FIXME make this config
    val FEEDBACK_ADDRESS = new InetSocketAddress("feedback.sandbox.push.apple.com", 2196) //FIXME make this config

    val STREAM_QUEUE_SIZE = 1000
    val MAX_WORKERS = 2

    val KEYSTORE_TYPE = "PKCS12"
    val KEY_ALGORITHM = "sunx509"

    val myId = "1edc1189189b23787bcc55d2638efdf3b086b12923a2bde4ffeb7feb2aa7b49c"
    val myIdBytes: Array[Byte] = hexToBytes(myId.toArray)

    def hexToBytes(in: Array[Char]): Array[Byte] =
        if (in.length % 2 != 0)
            throw new IllegalStateException("input length not a modulus of 2")
        else {
            val limit = in.length >> 1
            val result = Array.ofDim[Byte](limit)
            def nybbleToByte(in: Char): Byte =
                in match {
                    case ch if ch >= '0' && ch <= '9' => (ch - '0').asInstanceOf[Byte]
                    case ch if ch >= 'a' && ch <= 'f' => (ch - 'a' + 0xa).asInstanceOf[Byte]
                    case ch if ch >= 'A' && ch <= 'F' => (ch - 'A' + 0xa).asInstanceOf[Byte]
                    case _ => 0x7f
                }
            @tailrec def iterate(off: Int): Unit =
                if (off < limit) {
                    val hich = in( off << 1     ); val hi = nybbleToByte(hich)
                    val loch = in((off << 1) + 1); val lo = nybbleToByte(loch)
                    if      (hi == 0x7f) throw new IllegalStateException(hich + " is not valid hexadecimal")
                    else if (lo == 0x7f) throw new IllegalStateException(loch + " is not a valid hexadecimal")
                    else {
                        result(off) = ((hi << 4) | lo).asInstanceOf[Byte]
                        iterate(off+1)
                    }
                }
            iterate(0)
            result
        }
}