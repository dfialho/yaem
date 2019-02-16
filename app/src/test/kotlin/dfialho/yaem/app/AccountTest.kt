package dfialho.yaem.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.json.Json
import org.junit.Test

class AccountTest {

    @Test
    fun serializationRoundTrip() {
        val account = Account("My account")

        val serializedAccount = Json.stringify(Account.serializer(), account)
        val deserializedAccount = Json.parse(Account.serializer(), serializedAccount)

        assertThat(deserializedAccount).isEqualTo(account)
    }
}