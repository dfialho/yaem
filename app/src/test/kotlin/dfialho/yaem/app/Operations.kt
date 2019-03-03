package dfialho.yaem.app

import io.ktor.server.testing.TestApplicationEngine
import kotlinx.serialization.json.Json
import kotlinx.serialization.list


fun TestApplicationEngine.createAccount(account: Account): Account = handleCreateAccountRequest(account).run {
    Json.parse(Account.serializer(), response.content ?: "")
}

fun TestApplicationEngine.listAccounts(): List<Account> = handleListAccountsRequest().run {
    Json.parse(Account.serializer().list, response.content ?: "")
}


