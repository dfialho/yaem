package dfialho.yaem.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import dfialho.yaem.app.validators.ValidationError
import io.kotlintest.specs.StringSpec
import io.ktor.http.HttpStatusCode

class HttpStatusTranslatorTest : StringSpec({

    val translator = HttpStatusTranslator()

    fun statusTests(code: HttpStatusCode, errors: List<ValidationError>) {
        errors.forEach { error ->
            "Validation error '${error::class.simpleName}' translates into '${code}'" {
                assertThat(translator.translate(error), name = error::class.qualifiedName)
                    .isEqualTo(code)
            }
        }
    }

    statusTests(
        code = HttpStatusCode.BadRequest,
        errors = listOf(
            ValidationError.InvalidID("id"),
            ValidationError.InvalidJson("item"),
            ValidationError.Transactions.CommonAccounts("account-123"),
            ValidationError.Accounts.Name.Blank("  "),
            ValidationError.Accounts.Name.TooLong("name-too-long")
        )
    )

    statusTests(
        code = HttpStatusCode.NotFound,
        errors = listOf(
            ValidationError.Transactions.NotFound("trx-id"),
            ValidationError.Transactions.MissingDependency("account-123")
        )
    )

    statusTests(
        code = HttpStatusCode.Conflict,
        errors = listOf(
            ValidationError.Accounts.NameExists("name"),
            ValidationError.Accounts.References("account-123")
        )
    )
})
