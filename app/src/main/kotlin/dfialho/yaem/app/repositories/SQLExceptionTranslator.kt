package dfialho.yaem.app.repositories

import java.sql.SQLException

interface SQLExceptionTranslator {
    fun translate(exception: SQLException): DatabaseException
}