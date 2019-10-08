package dfialho.yaem.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import dfialho.yaem.app.validators.errors.AccountsValidationErrors
import dfialho.yaem.app.validators.errors.TransactionsValidationErrors
import dfialho.yaem.app.validators.errors.ValidationError
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
            TransactionsValidationErrors.CommonAccounts("account-123"),
            AccountsValidationErrors.Name.Blank("  "),
            AccountsValidationErrors.Name.TooLong("name-too-long")
        )
    )

    statusTests(
        code = HttpStatusCode.NotFound,
        errors = listOf(
            TransactionsValidationErrors.NotFound("trx-id"),
            TransactionsValidationErrors.MissingDependency("account-123")
        )
    )

    statusTests(
        code = HttpStatusCode.Conflict,
        errors = listOf(
            AccountsValidationErrors.NameExists("name"),
            AccountsValidationErrors.References("account-123")
        )
    )
})
