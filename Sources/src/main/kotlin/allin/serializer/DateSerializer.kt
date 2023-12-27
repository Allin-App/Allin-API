package allin.serializer

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

@Serializer(Date::class)
class DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)

    override fun deserialize(decoder: Decoder): Date {
        val dateString = decoder.decodeString()
        return formatter.parse(dateString)
    }

    override fun serialize(encoder: Encoder, value: Date) {
        val dateString = formatter.format(value)
        encoder.encodeString(dateString)
    }
}