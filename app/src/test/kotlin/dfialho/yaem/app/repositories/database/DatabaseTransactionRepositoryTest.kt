package dfialho.yaem.app.repositories.database

class DatabaseTransactionRepositoryTest {

//    @Test
//    fun `create new one-way transaction`() {
//        val repositoryManager = uniqueRepositoryManager()
//        val account = createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//        val transaction = Transaction(amount = 10.5, receiver = account.id)
//
//        repository.create(transaction)
//
//        assertAll {
//            assertThat(repository.get(transaction.id)).isEqualTo(transaction)
//            assertThat(repository.exists(transaction.id)).isTrue()
//            assertThat(repository.list()).containsOnly(transaction)
//        }
//    }
//
//    @Test
//    fun `create new transfer transaction`() {
//        val repositoryManager = uniqueRepositoryManager()
//        val account1 = createAnAccount(repositoryManager)
//        val account2 = createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//        val transaction = Transaction(amount = 10.5, receiver = account1.id, sender = account2.id)
//
//        repository.create(transaction)
//
//        assertAll {
//            assertThat(repository.get(transaction.id)).isEqualTo(transaction)
//            assertThat(repository.exists(transaction.id)).isTrue()
//            assertThat(repository.list()).containsOnly(transaction)
//        }
//    }
//
//    @Test
//    fun `creating a transaction for non-existing account should throw error`() {
//        val repositoryManager = uniqueRepositoryManager()
//        createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//        val nonExistingAccountID = randomID()
//
//        assertThat {
//            repository.create(Transaction(receiver = nonExistingAccountID, amount = 10.5))
//        }.thrownError {
//            isInstanceOf(ParentMissingException::class)
//        }
//    }
//
//    @Test
//    fun `creating a transaction for non-existing sender account should throw error`() {
//        val repositoryManager = uniqueRepositoryManager()
//        val account = createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//        val nonExistingAccountID = randomID()
//
//        assertThat {
//            repository.create(Transaction(receiver = account.id, sender = nonExistingAccountID, amount = 10.5))
//        }.thrownError {
//            isInstanceOf(ParentMissingException::class)
//        }
//    }
//
//    @Test
//    fun `list transactions before adding any should return an empty list`() {
//        val repositoryManager = uniqueRepositoryManager()
//        createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//
//        val transactions = repository.list()
//
//        assertThat(transactions).isEmpty()
//    }
//
//    @Test
//    fun `getting a transaction before adding any should throw error`() {
//        val repositoryManager = uniqueRepositoryManager()
//        createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//
//        assertThat {
//            repository.get(randomID())
//        }.thrownError {
//            isInstanceOf(NotFoundException::class)
//        }
//    }
//
//    @Test
//    fun `getting non-existing transaction should throw an error`() {
//        val repositoryManager = uniqueRepositoryManager()
//        val account = createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//        (1..5)
//            .map { Transaction(receiver = account.id, amount = 10.5, description = "trx-$it") }
//            .forEach { repository.create(it) }
//
//        assertThat {
//            repository.get(randomID())
//        }.thrownError {
//            isInstanceOf(NotFoundException::class)
//        }
//    }
//
//    @Test
//    fun `deleting a non-existing transaction throws exception`() {
//        val repositoryManager = uniqueRepositoryManager()
//        createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//        val nonExistingID = randomID()
//
//        assertThat {
//            repository.delete(nonExistingID)
//        }.thrownError {
//            isInstanceOf(NotFoundException::class)
//        }
//    }
//
//    @Test
//    fun `after deleting an existing transaction the account is no longer listed`() {
//        val repositoryManager = uniqueRepositoryManager()
//        val account = createAnAccount(repositoryManager)
//        val repository = repositoryManager.getTransactionRepository()
//
//        val otherTransactions = (1..3)
//            .map { Transaction(10.0, account.id) }
//            .map {  }
//        val deletedTransaction = Transaction(50.0, account.id)
//        val allTransactions = otherTransactions + deletedTransaction
//        allTransactions.forEach { repository.create(it) }
//
//        repository.delete(deletedTransaction.id)
//
//        assertAll {
//            assertThat(repository.exists(deletedTransaction.id)).isFalse()
//            assertThat(repository.list()).containsOnly(*otherTransactions)
//        }
//    }
//
//    // TODO list transactions by account should include those associated with the account as sender and receiver
//    // TODO list transactions by account when account does not exist should return empty list
//    // TODO list transactions by account from account without transactions should return empty list
//
//    private fun createAnAccount(repositoryManager: DatabaseRepositoryManager): Account {
//        val accountRepository = repositoryManager.getAccountRepository()
//        val account = Account("account-${UUID.randomUUID().toString().substring(0, 5)}")
//        accountRepository.create(account)
//        return account
//    }
}
