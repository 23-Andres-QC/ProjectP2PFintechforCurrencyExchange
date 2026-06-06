package com.example.p2p.navigation


import com.example.p2p.presentation.dispute.DisputeDetailScreen
import com.example.p2p.presentation.dispute.DisputesViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.p2p.core.security.TokenManager
import com.example.p2p.data.repository.AuthRepositoryImpl
import com.example.p2p.presentation.about.AboutScreen
import com.example.p2p.presentation.admin.AdminScreen
import com.example.p2p.presentation.admin.AdminViewModel
import com.example.p2p.presentation.auth.ForgotPasswordScreen
import com.example.p2p.presentation.auth.LoginScreen
import com.example.p2p.presentation.auth.LoginViewModel
import com.example.p2p.presentation.auth.RegisterScreen
import com.example.p2p.presentation.auth.RegisterViewModel
import com.example.p2p.presentation.bank_accounts.BankAccountsScreen
import com.example.p2p.presentation.complaints.ComplaintsScreen
import com.example.p2p.presentation.complaints.ComplaintsViewModel
import com.example.p2p.presentation.dispute.MyDisputesScreen
import com.example.p2p.presentation.dispute.RegisterDisputeScreen
import com.example.p2p.presentation.help.HelpScreen
import com.example.p2p.presentation.history.HistoryScreen
import com.example.p2p.presentation.kyc.KycScreen
import com.example.p2p.presentation.legal.PrivacyScreen
import com.example.p2p.presentation.legal.TermsScreen
import com.example.p2p.presentation.market.MarketScreen
import com.example.p2p.data.repository.NotificationRepositoryImpl
import com.example.p2p.presentation.notifications.NotificationsScreen
import com.example.p2p.presentation.notifications.NotificationsViewModel
import com.example.p2p.presentation.offer.MyOffersScreen
import com.example.p2p.presentation.offer.PublishScreen
import com.example.p2p.presentation.profile.EditProfileScreen
import com.example.p2p.presentation.profile.EditProfileViewModel
import com.example.p2p.presentation.profile.ProfileScreen
import com.example.p2p.presentation.rating.RatingScreen
import com.example.p2p.presentation.rating.RatingViewModel
import com.example.p2p.presentation.receipt.ReceiptScreen
import com.example.p2p.presentation.reviews.ReviewsScreen
import com.example.p2p.presentation.reviews.ReviewsViewModel
import com.example.p2p.presentation.transaction.TransactionDetailScreen
import com.example.p2p.presentation.transaction.TransactionScreen
import com.example.p2p.presentation.vendor.VendorInboxScreen
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.SurfaceColor
import kotlinx.coroutines.launch

// Rutas donde NO se muestra la barra inferior (auth)
private val authRoutes = setOf(
    Screen.Login.route,
    Screen.Register.route,
    Screen.ForgotPass.route,
    Screen.Kyc.route
)

@Composable
fun NavGraph(startDestination: String = Screen.Login.route) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = TokenManager.getInstance(context)
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute !in authRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    tokenManager = tokenManager
                ) { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        popUpTo(Screen.Market.route) { saveState = true }
                        restoreState = true
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ── Auth ─────────────────────────────────────────────────────────────
            composable(Screen.Login.route) {
                val authRepo = AuthRepositoryImpl(tokenManager)
                val vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(authRepo))
                LoginScreen(
                    viewModel = vm,
                    onLoginSuccess = {
                        navController.navigate(Screen.Market.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                val authRepo = AuthRepositoryImpl(tokenManager)
                val vm: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory(authRepo))
                RegisterScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Kyc.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPass.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Kyc.route) {
                val userRepo = com.example.p2p.data.repository.UserRepositoryImpl(com.example.p2p.core.network.ApiClient.userApi)
                val kycVm: com.example.p2p.presentation.kyc.KycViewModel = viewModel(factory = com.example.p2p.presentation.kyc.KycViewModel.Factory(userRepo))
                KycScreen(
                    viewModel = kycVm,
                    onNavigateBack = {
                        navController.navigate(Screen.Market.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── Main ─────────────────────────────────────────────────────────────
            composable(Screen.Market.route) {
                var userName by remember { mutableStateOf("Usuario") }
                var currentUserId by remember { mutableStateOf("") }
                LaunchedEffect(Unit) {
                    userName = tokenManager.getUserName() ?: "Usuario"
                    currentUserId = tokenManager.getUserId() ?: ""
                }

                val offerRepo  = com.example.p2p.data.repository.OfferRepositoryImpl(com.example.p2p.core.network.ApiClient.offerApi)
                val txnRepo    = com.example.p2p.data.repository.TransactionRepositoryImpl(com.example.p2p.core.network.ApiClient.transactionApi)
                val bankRepo   = com.example.p2p.data.repository.BankAccountRepositoryImpl(com.example.p2p.core.network.ApiClient.bankAccountsApi)
                val notifRepo  = com.example.p2p.data.repository.NotificationRepositoryImpl(com.example.p2p.core.network.ApiClient.notificationApi)
                val vm: com.example.p2p.presentation.market.MarketViewModel = viewModel(
                    factory = com.example.p2p.presentation.market.MarketViewModel.Factory(
                        offerRepo, txnRepo, bankRepo, com.example.p2p.core.network.ApiClient.exchangeApi, notifRepo
                    )
                )
                MarketScreen(
                    viewModel = vm,
                    userName = userName,
                    currentUserId = currentUserId,
                    onNavigateToNotifications = {
                        vm.loadUnreadCount()
                        navController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToTransaction = { txnId -> navController.navigate(Screen.Transaction.createRoute(txnId)) },
                    onNavigateToAddBankAccount = { navController.navigate(Screen.BankAccounts.route) }
                )
            }

            composable(Screen.Publish.route) {
                val offerRepo = com.example.p2p.data.repository.OfferRepositoryImpl(com.example.p2p.core.network.ApiClient.offerApi)
                val bankRepo  = com.example.p2p.data.repository.BankAccountRepositoryImpl(com.example.p2p.core.network.ApiClient.bankAccountsApi)
                val vm: com.example.p2p.presentation.offer.PublishViewModel = viewModel(
                    factory = com.example.p2p.presentation.offer.PublishViewModel.Factory(
                        offerRepo, com.example.p2p.core.network.ApiClient.exchangeApi, bankRepo
                    )
                )
                PublishScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBankAccounts = { navController.navigate(Screen.BankAccounts.route) }
                )
            }

            composable(Screen.History.route) {
                val txnRepo = com.example.p2p.data.repository.TransactionRepositoryImpl(com.example.p2p.core.network.ApiClient.transactionApi)
                val vm: com.example.p2p.presentation.history.HistoryViewModel = viewModel(factory = com.example.p2p.presentation.history.HistoryViewModel.Factory(txnRepo))
                var historyUserId by remember { mutableStateOf("") }
                LaunchedEffect(Unit) { historyUserId = tokenManager.getUserId() ?: "" }
                HistoryScreen(
                    viewModel = vm,
                    currentUserId = historyUserId,
                    onBack = { navController.popBackStack() },
                    onNavigateToTransaction = { txnId -> navController.navigate(Screen.Transaction.createRoute(txnId)) },
                    onNavigateToTransactionDetail = { txnId -> navController.navigate(Screen.TransactionDetail.createRoute(txnId)) },
                    onNavigateToPending = { navController.navigate(Screen.Vendor.route) }
                )
            }

            composable(
                route = Screen.Rating.route,
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.StringType },
                    navArgument("score") { type = NavType.IntType; defaultValue = 5 }
                )
            ) { backStack ->
                val ratingRepo = com.example.p2p.data.repository.RatingRepositoryImpl(com.example.p2p.core.network.ApiClient.ratingApi)
                val vm: com.example.p2p.presentation.rating.RatingViewModel = viewModel(factory = com.example.p2p.presentation.rating.RatingViewModel.Factory(ratingRepo))
                val id = backStack.arguments?.getString("transactionId") ?: ""
                val score = backStack.arguments?.getInt("score") ?: 5
                RatingScreen(
                    transactionId = id,
                    defaultScore = score,
                    viewModel = vm,
                    onSuccess = {
                        navController.navigate(Screen.Market.route) {
                            popUpTo(Screen.Market.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Screen.Market.route) {
                            popUpTo(Screen.Market.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Transaction.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStack ->
                val txnRepo = com.example.p2p.data.repository.TransactionRepositoryImpl(com.example.p2p.core.network.ApiClient.transactionApi)
                val vm: com.example.p2p.presentation.transaction.TransactionViewModel = viewModel(factory = com.example.p2p.presentation.transaction.TransactionViewModel.Factory(txnRepo))
                val id = backStack.arguments?.getString("transactionId") ?: ""
                var txnCurrentUserId by remember { mutableStateOf("") }
                LaunchedEffect(Unit) { txnCurrentUserId = tokenManager.getUserId() ?: "" }
                TransactionScreen(
                    transactionId = id,
                    currentUserId = txnCurrentUserId,
                    viewModel = vm,
                    onNavigateToDispute = { txnId -> navController.navigate(Screen.RegisterDispute.createRoute(txnId)) },
                    onNavigateToReceipt = { txnId -> navController.navigate(Screen.Receipt.createRoute(txnId)) },
                    onNavigateToRating = { txnId, score -> navController.navigate(Screen.Rating.createRoute(txnId, score)) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Receipt.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStack ->
                val txnRepo = com.example.p2p.data.repository.TransactionRepositoryImpl(com.example.p2p.core.network.ApiClient.transactionApi)
                val vm: com.example.p2p.presentation.transaction.TransactionViewModel = viewModel(factory = com.example.p2p.presentation.transaction.TransactionViewModel.Factory(txnRepo))
                val id = backStack.arguments?.getString("transactionId") ?: ""
                ReceiptScreen(
                    transactionId = id,
                    viewModel = vm,
                    onNavigateToRating = { txnId -> navController.navigate(Screen.Rating.createRoute(txnId)) },
                    onNavigateToMarket = {
                        navController.navigate(Screen.Market.route) {
                            popUpTo(Screen.Market.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.TransactionDetail.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("transactionId") ?: ""
                val txnRepo = com.example.p2p.data.repository.TransactionRepositoryImpl(com.example.p2p.core.network.ApiClient.transactionApi)
                val vm: com.example.p2p.presentation.transaction.TransactionViewModel = viewModel(factory = com.example.p2p.presentation.transaction.TransactionViewModel.Factory(txnRepo))
                TransactionDetailScreen(
                    transactionId = id,
                    viewModel = vm,
                    onNavigateToDispute = { txnId -> navController.navigate(Screen.RegisterDispute.createRoute(txnId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Profile ──────────────────────────────────────────────────────────
            composable(Screen.Profile.route) {
                val userRepo = com.example.p2p.data.repository.UserRepositoryImpl(com.example.p2p.core.network.ApiClient.userApi)
                val notifRepo = NotificationRepositoryImpl(com.example.p2p.core.network.ApiClient.notificationApi)
                val vm: com.example.p2p.presentation.profile.ProfileViewModel = viewModel(
                    factory = com.example.p2p.presentation.profile.ProfileViewModel.Factory(userRepo, notifRepo)
                )
                ProfileScreen(
                    viewModel = vm,
                    onNavigate = { route -> navController.navigate(route) },
                    onLogout = {
                        scope.launch {
                            tokenManager.clearSession()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                val userRepo = com.example.p2p.data.repository.UserRepositoryImpl(com.example.p2p.core.network.ApiClient.userApi)
                val vm: EditProfileViewModel = viewModel(factory = EditProfileViewModel.Factory(userRepo))
                EditProfileScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.BankAccounts.route) {
                val bankRepo = com.example.p2p.data.repository.BankAccountRepositoryImpl(com.example.p2p.core.network.ApiClient.bankAccountsApi)

                // Asegúrate de que el ViewModel se cree así, con el tokenManager que ya tienes arriba
                val vm: com.example.p2p.presentation.bank_accounts.BankAccountsViewModel = viewModel(
                    factory = com.example.p2p.presentation.bank_accounts.BankAccountsViewModel.Factory(bankRepo, tokenManager)
                )

                // El Screen debe ir en su propia línea
                BankAccountsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                val notifRepo = NotificationRepositoryImpl(com.example.p2p.core.network.ApiClient.notificationApi)
                val vm: NotificationsViewModel = viewModel(factory = NotificationsViewModel.Factory(notifRepo))
                NotificationsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Reviews.route) {
                val vm: ReviewsViewModel = viewModel(
                    factory = ReviewsViewModel.Factory(com.example.p2p.core.network.ApiClient.ratingApi)
                )
                ReviewsScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.MyOffers.route) {
                val repo = com.example.p2p.data.repository.OfferRepositoryImpl(com.example.p2p.core.network.ApiClient.offerApi)
                val vm: com.example.p2p.presentation.offer.MyOffersViewModel = viewModel(factory = com.example.p2p.presentation.offer.MyOffersViewModel.Factory(repo))
                MyOffersScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onPublishClick = { navController.navigate(Screen.Publish.route) }
                )
            }

            composable(Screen.Complaints.route) {
                val repo = com.example.p2p.data.repository.ComplaintsRepositoryImpl(
                    com.example.p2p.core.network.ApiClient.complaintApi
                )
                val vm: ComplaintsViewModel = viewModel(
                    factory = ComplaintsViewModel.Factory(repo)
                )
                ComplaintsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Disputes ─────────────────────────────────────────────────────────
            composable(Screen.MyDisputes.route) {
                val repo = com.example.p2p.data.repository.DisputeRepositoryImpl(com.example.p2p.core.network.ApiClient.disputeApi)
                val vm: com.example.p2p.presentation.dispute.DisputesViewModel = viewModel(factory = com.example.p2p.presentation.dispute.DisputesViewModel.Factory(repo))
                MyDisputesScreen(
                    viewModel = vm,
                    onNavigate = { route -> navController.navigate(route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.RegisterDispute.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStack ->
                val disputeRepo = com.example.p2p.data.repository.DisputeRepositoryImpl(com.example.p2p.core.network.ApiClient.disputeApi)
                val vm: com.example.p2p.presentation.dispute.DisputesViewModel = viewModel(factory = com.example.p2p.presentation.dispute.DisputesViewModel.Factory(disputeRepo))
                RegisterDisputeScreen(
                    transactionId = backStack.arguments?.getString("transactionId"),
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.DisputeDetail.route,
                arguments = listOf(navArgument("disputeId") { type = NavType.StringType })
            ) { backStack ->
                val repo = com.example.p2p.data.repository.DisputeRepositoryImpl(com.example.p2p.core.network.ApiClient.disputeApi)
                val vm: DisputesViewModel = viewModel(factory = DisputesViewModel.Factory(repo))
                val id = backStack.arguments?.getString("disputeId") ?: ""
                DisputeDetailScreen(
                    disputeId = id,
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Admin ─────────────────────────────────────────────────────────────
            composable(Screen.Admin.route) {
                val adminRepo = com.example.p2p.data.repository.AdminRepositoryImpl(com.example.p2p.core.network.ApiClient.adminApi)
                val vm: AdminViewModel = viewModel(factory = com.example.p2p.presentation.admin.AdminViewModel.Factory(adminRepo))
                AdminScreen(
                    viewModel = vm,
                    onNavigate = { route -> navController.navigate(route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Vendor.route) {
                val txnRepo = com.example.p2p.data.repository.TransactionRepositoryImpl(com.example.p2p.core.network.ApiClient.transactionApi)
                val vm: com.example.p2p.presentation.transaction.TransactionViewModel = viewModel(factory = com.example.p2p.presentation.transaction.TransactionViewModel.Factory(txnRepo))
                VendorInboxScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Legal / Info ──────────────────────────────────────────────────────
            composable(Screen.Terms.route) {
                TermsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Privacy.route) {
                PrivacyScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Help.route) {
                HelpScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    tokenManager: TokenManager,
    onNavigate: (String) -> Unit
) {
    var isVendor by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val role = tokenManager.getUserRole() ?: ""
        isVendor = role != "admin"
    }

    NavigationBar(containerColor = SurfaceColor, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = currentRoute == Screen.Market.route,
            onClick = { onNavigate(Screen.Market.route) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Mercado") },
            label = { Text("Mercado", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = Primary.copy(alpha = 0.12f)
            )
        )
        if (isVendor) {
            NavigationBarItem(
                selected = currentRoute == Screen.Publish.route,
                onClick = { onNavigate(Screen.Publish.route) },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = "Publicar") },
                label = { Text("Publicar", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = Primary.copy(alpha = 0.12f)
                )
            )
        }
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = Primary.copy(alpha = 0.12f)
            )
        )
    }
}
