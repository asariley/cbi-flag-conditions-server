package models

import scala.Enumeration

import play.api.libs.json._
import play.api.libs.json.{Json, Format}
import scala.slick.driver.PostgresDriver.simple.{queryToAppliedQueryInvoker}
import scala.slick.jdbc.JdbcBackend.Session
import scala.slick.jdbc.StaticQuery.interpolation

import Writes.{BooleanWrites, StringWrites}


object NotificationPref {
    implicit val notificationPrefFormat: Format[NotificationPref] = Json.format[NotificationPref]

    def fetchNotificationPref(deviceIdentifier: String)(implicit session: Session): Option[NotificationPref] =
        QueryHelpers.adHocQuery (
            { rs =>
                NotificationPref(rs.getString(1), rs.getBoolean(2), rs.getBoolean(3), rs.getBoolean(4), rs.getBoolean(5),
                    rs.getBoolean(6), rs.getBoolean(7), rs.getBoolean(8), rs.getBoolean(9))

            },
            sql"""
                SELECT
                    np.address,
                    np.weekday,
                    np.weekend,
                    np.daytime,
                    np.evening,
                    np.red_flag,
                    np.yellow_flag,
                    np.green_flag,
                    np.closed_flag
                FROM notification_pref np
                JOIN device d on d.device_id = np.device_id
                WHERE d.device_uuid = $deviceIdentifier
            """
        ).firstOption

    /** Upserts notification pref */
    def updateNotificationPref(np: NotificationPref, device: Device)(implicit session: Session): Unit = {
        /** returns device_id for device, creating a new device if necessary */
        def discoverDeviceId(): (Int, Option[Int]) = {
            QueryHelpers.adHocQuery (rs => (rs.getInt(1), Option(rs.getObject(2))), sql"""
                SELECT d.device_id, np.notification_pref_id
                FROM device d
                LEFT JOIN notification_pref np ON d.device_id = np.device_id
                WHERE device_uuid = ${device.deviceIdentifier}
            """).firstOption match {
                case None => {
                    sqlu"INSERT INTO device (device_uuid) VALUES (${device.deviceIdentifier})".execute
                    discoverDeviceId()
                }
                case Some((deviceId, npIdOpt)) => (deviceId, npIdOpt.map(_.asInstanceOf[Int]))
            }
        }

        discoverDeviceId() match {
            case (deviceId, Some(npId)) => sqlu"""
                UPDATE notification_pref
                SET address = ${np.address},
                    weekday = ${np.weekday},
                    weekend = ${np.weekend},
                    daytime = ${np.daytime},
                    evening = ${np.evening},
                    red_flag = ${np.redFlag},
                    yellow_flag = ${np.yellowFlag},
                    green_flag = ${np.greenFlag},
                    closed_flag = ${np.closedFlag}
                WHERE device_id = $deviceId
            """.execute
            case (deviceId, None) => sqlu"""
                INSERT INTO notification_pref (
                    device_id,
                    address,
                    weekday,
                    weekend,
                    daytime,
                    evening,
                    red_flag,
                    yellow_flag,
                    green_flag,
                    closed_flag
                ) VALUES (
                    $deviceId,
                    ${np.address},
                    ${np.weekday},
                    ${np.weekend},
                    ${np.daytime},
                    ${np.evening},
                    ${np.redFlag},
                    ${np.yellowFlag},
                    ${np.greenFlag},
                    ${np.closedFlag}
                )
            """.execute
        }
    }
}

case class NotificationPref (
    address: String,
    weekday: Boolean,
    weekend: Boolean,
    daytime: Boolean,
    evening: Boolean,
    redFlag: Boolean,
    yellowFlag: Boolean,
    greenFlag: Boolean,
    closedFlag: Boolean
)


