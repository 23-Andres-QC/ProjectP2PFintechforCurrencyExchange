package com.example.p2p.navigation

sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object Register      : Screen("register")
    object ForgotPass    : Screen("forgot_pass")
    object Kyc           : Screen("kyc")
    object Market        : Screen("market")
    object Publish       : Screen("publish")
    object History : Screen("history?filter={filter}") {
        fun createRoute(filter: Int = 0) = "history?filter=$filter"
    }
    object Profile       : Screen("profile")
    object EditProfile   : Screen("edit_profile")
    object BankAccounts  : Screen("bank_accounts")
    object Admin         : Screen("admin")
    object Rating : Screen("rating/{transactionId}/{score}") {
        fun createRoute(id: String, score: Int = 5) = "rating/$id/$score"
    }
    object Notifications : Screen("notifications")
    object MyOffers      : Screen("my_offers")
    object MyDisputes    : Screen("my_disputes")
    object RegisterDispute : Screen("register_dispute/{transactionId}") {
        fun createRoute(id: String) = "register_dispute/$id"
    }
    object Reviews       : Screen("reviews")
    object Complaints    : Screen("complaints")
    object Terms         : Screen("terms")
    object Privacy       : Screen("privacy")
    object About         : Screen("about")
    object Help          : Screen("help")
    object Pending       : Screen("pending")

    object Transaction : Screen("transaction/{transactionId}") {
        fun createRoute(id: String) = "transaction/$id"
    }
    object Receipt : Screen("receipt/{transactionId}") {
        fun createRoute(id: String) = "receipt/$id"
    }
    object TransactionDetail : Screen("tx_detail/{transactionId}") {
        fun createRoute(id: String) = "tx_detail/$id"
    }
    object DisputeDetail : Screen("dispute_detail/{disputeId}") {
        fun createRoute(id: String) = "dispute_detail/$id"
    }
}