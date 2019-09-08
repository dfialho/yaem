package dfialho.yaem.app.repositories

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID

/**
 * Repository holding Accounts.
 */
interface AccountRepository {

    /**
     * Creates an [account] in the repository.
     * If any error occurs when performing this operation, the repository will be left in the original state.
     * @throws DuplicateKeyException If an account already exists with specified ID or name.
     * @throws UnknownRepositoryException If an unexpected error occurs.
     */
    fun create(account: Account)

    /**
     * Retrieves the account with the [accountID] from this repository.
     * @throws NotFoundException If this repository does not hold an account with [accountID].
     * @throws UnknownRepositoryException If an unexpected error occurs.
     */
    fun get(accountID: ID): Account

    /**
     * Retrieves a list containing all accounts stored in this repository.
     * @throws UnknownRepositoryException If an unexpected error occurs.
     */
    fun list(): List<Account>

    /**
     * Returns true if this repository holds an account with [accountID], or false if otherwise.
     * @throws UnknownRepositoryException If an unexpected error occurs.
     */
    fun exists(accountID: ID): Boolean

    /**
     * Updates the information of an account with the contents of [account]. The ID of the account being updated is
     * given by the ID of [account].
     * @throws NotFoundException If this repository does not hold an account with the ID of [account].
     * @throws DuplicateKeyException If an account already exists with specified name.
     * @throws UnknownRepositoryException If an unexpected error occurs.
     */
    fun update(account: Account)

    /**
     * Deletes the account with [accountID] from this repository.
     * @throws NotFoundException If this repository does not hold an account with [accountID].
     * @throws ChildExistsException If some resource exists that depends on the account being deleted.
     * @throws UnknownRepositoryException If an unexpected error occurs.
     */
    fun delete(accountID: String)
}