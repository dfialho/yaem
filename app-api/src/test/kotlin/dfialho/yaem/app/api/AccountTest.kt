package dfialho.yaem.app.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import dfialho.yaem.json.lib.json
import dfialho.yaem.json.lib.json
import org.junit.Test

class AccountTest {

    @Test
    fun `serialization round-trip`() {
        val account = Account("My account")

        val serializedAccount = json.stringify(Account.serializer(), account)
        val deserializedAccount = json.parse(Account.serializer(), serializedAccount)

        assertThat(deserializedAccount).isEqualTo(account)
    }

    @Test
    fun `deserialize when missing optional parameters`() {

        //language=JSON
        val serializedAccount = "{ \"name\": \"My Account\" }"
        val deserializedAccount = json.parse(Account.serializer(), serializedAccount)

        println(deserializedAccount)
        assertThat(deserializedAccount.id).isNotNull()
    }
}