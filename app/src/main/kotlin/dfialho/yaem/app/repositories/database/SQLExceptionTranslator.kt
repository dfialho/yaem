package dfialho.yaem.app.repositories.database

import dfialho.yaem.app.repositories.RepositoryException
import java.sql.SQLException

interface SQLExceptionTranslator {
    fun translate(exception: SQLException): RepositoryException
}