package dfialho.yaem.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.sun.jndi.toolkit.url.Uri
import io.mockk.*
import kotlinx.serialization.json.Json
import org.junit.Test
import java.time.Instant

class AccountTest {

    @Test
    fun serializationRoundTrip() {
        val account = Account("My account")

        val serializedAccount = json.stringify(Account.serializer(), account)
        val deserializedAccount = json.parse(Account.serializer(), serializedAccount)

        assertThat(deserializedAccount).isEqualTo(account)
    }

    @Test
    fun optionalValues() {

        //language=JSON
        val serializedAccount = "{ \"name\": \"My Account\" }"
        val deserializedAccount = json.parse(Account.serializer(), serializedAccount)

        println(deserializedAccount)
        assertThat(deserializedAccount.id).isNotNull()
    }
}