package dfialho.yaem.app.repositories

import dfialho.yaem.app.ID
import org.joda.time.DateTime
import java.time.Instant
import java.util.*

fun ID.toUUID(): UUID = UUID.fromString(this)

fun UUID.toID(): ID = this.toString()

fun Instant?.toDateTime(): DateTime = DateTime(this?.toEpochMilli() ?: Instant.now())

fun DateTime.toJavaInstant(): Instant = this.toDate().toInstant()
