package dfialho.yaem.app.exceptions

import dfialho.yaem.app.ID

class ParentMissingException(val parentID: ID? = null) : Exception()
