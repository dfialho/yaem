package dfialho.yaem.app.repositories.exceptions

import java.sql.SQLException

interface SQLExceptionTranslator {
    fun translate(exception: SQLException): DatabaseException
}