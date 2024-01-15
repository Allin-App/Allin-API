package allin.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeLong(value.toEpochSecond())
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val epoch = decoder.decodeLong()
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault())
    }
}