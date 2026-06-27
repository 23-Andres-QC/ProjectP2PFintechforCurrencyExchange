package com.example.p2p.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.p2p.core.network.ApiClient
import com.example.p2p.core.security.TokenManager
import com.example.p2p.data.repository.AdminRepositoryImpl
import com.example.p2p.data.repository.AuthRepositoryImpl
import com.example.p2p.data.repository.BankAccountRepositoryImpl
import com.example.p2p.data.repository.ComplaintsRepositoryImpl
import com.example.p2p.data.repository.DisputeRepositoryImpl
import com.example.p2p.data.repository.NotificationRepositoryImpl
import com.example.p2p.data.repository.OfferRepositoryImpl
import com.example.p2p.data.repository.RatingRepositoryImpl
import com.example.p2p.data.repository.TransactionRepositoryImpl
import com.example.p2p.data.repository.UserRepositoryImpl
import com.example.p2p.presentation.about.AboutScreen
import com.example.p2p.presentation.admin.AdminScreen
import com.example.p2p.presentation.admin.AdminViewModel
import com.example.p2p.presentation.auth.ForgotPasswordScreen
import com.example.p2p.presentation.auth.LoginScreen
import com.example.p2p.presentation.auth.LoginViewModel
import com.example.p2p.presentation.auth.RegisterScreen
import com.example.p2p.presentation.auth.RegisterViewModel
import com.example.p2p.presentation.bank_accounts.BankAccountsScreen
import com.example.p2p.presentation.bank_accounts.BankAccountsViewModel
import com.example.p2p.presentation.complaints.ComplaintsScreen
import com.example.p2p.presentation.complaints.ComplaintsViewModel
import com.example.p2p.presentation.dispute.DisputeDetailScreen
import com.example.p2p.presentation.dispute.DisputesViewModel
import com.example.p2p.presentation.dispute.MyDisputesScreen
import com.example.p2p.presentation.dispute.RegisterDisputeScreen
import com.example.p2p.presentation.help.HelpScreen
import com.example.p2p.presentation.history.HistoryScreen
import com.example.p2p.presentation.history.HistoryViewModel
import com.example.p2p.presentation.kyc.KycScreen
import com.example.p2p.presentation.kyc.KycViewModel
import com.example.p2p.presentation.legal.PrivacyScreen
import com.example.p2p.presentation.legal.TermsScreen
import com.example.p2p.presentation.market.MarketScreen
import com.example.p2p.presentation.market.MarketViewModel
import com.example.p2p.presentation.notifications.NotificationsScreen
import com.example.p2p.presentation.notifications.NotificationsViewModel
import com.example.p2p.presentation.offer.MyOffersScreen
import com.example.p2p.presentation.offer.MyOffersViewModel
import com.example.p2p.presentation.offer.PublishScreen
import com.example.p2p.presentation.offer.PublishViewModel
import com.example.p2p.presentation.pending.PendingScreen
import com.example.p2p.presentation.profile.EditProfileScreen
import com.example.p2p.presentation.profile.EditProfileViewModel
import com.example.p2p.presentation.profile.ProfileScreen
import com.example.p2p.presentation.profile.ProfileViewModel
import com.example.p2p.presentation.rating.RatingScreen
import com.example.p2p.presentation.rating.RatingViewModel
import com.example.p2p.presentation.receipt.ReceiptScreen
import com.example.p2p.presentation.reviews.ReviewsScreen
import com.example.p2p.presentation.reviews.ReviewsViewModel
import com.example.p2p.presentation.transaction.TransactionDetailScreen
import com.example.p2p.presentation.transaction.TransactionScreen
import com.example.p2p.presentation.transaction.TransactionViewModel
import com.example.p2p.ui.theme.BackgroundApp
import com.example.p2p.ui.theme.BorderColor
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.TextMuted
import kotlinx.coroutines.launch

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
        containerColor = BackgroundApp,
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    tokenManager = tokenManager
                ) { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Market.route) { inclusive = false }
                        launchSingleTop = true
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
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPass.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                val authRepo = AuthRepositoryImpl(tokenManager)
                val userRepo = UserRepositoryImpl(ApiClient.userApi)
                val vm: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory(authRepo))
                val kycVm: KycViewModel = viewModel(factory = KycViewModel.Factory(userRepo))
                RegisterScreen(
                    viewModel = vm,
                    kycViewModel = kycVm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Market.route) {
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
                val userRepo = UserRepositoryImpl(ApiClient.userApi)
                val kycVm: KycViewModel = viewModel(factory = KycViewModel.Factory(userRepo))
                KycScreen(
                    viewModel = kycVm,
                    onNavigateBack = {
                        navController.navigate(Screen.Market.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Screen.Market.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Market.route) {
                var userName by remember { mutableStateOf("Usuario") }
                var currentUserId by remember { mutableStateOf("") }
                LaunchedEffect(Unit) {
                    userName = tokenManager.getUserName() ?: "Usuario"
                    currentUserId = tokenManager.getUserId() ?: ""
                }

                val offerRepo  = OfferRepositoryImpl(ApiClient.offerApi)
                val txnRepo    = TransactionRepositoryImpl(ApiClient.transactionApi)
                val bankRepo   = BankAccountRepositoryImpl(ApiClient.bankAccountsApi)
                val notifRepo  = NotificationRepositoryImpl(ApiClient.notificationApi)
                val vm: MarketViewModel = viewModel(
                    factory = MarketViewModel.Factory(offerRepo, txnRepo, bankRepo, ApiClient.exchangeApi, notifRepo)
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
                    onNavigateToPending = { navController.navigate(Screen.Pending.route) },
                    onNavigateToAddBankAccount = { navController.navigate(Screen.BankAccounts.route) }
                )
            }

            composable(Screen.Pending.route) {
                val txnRepo = TransactionRepositoryImpl(ApiClient.transactionApi)
                val vm: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory(txnRepo))
                var pendingUserId by remember { mutableStateOf("") }
                LaunchedEffect(Unit) { pendingUserId = tokenManager.getUserId() ?: "" }
                PendingScreen(
                    viewModel = vm,
                    currentUserId = pendingUserId,
                    onNavigateToTransaction = { txnId -> navController.navigate(Screen.Transaction.createRoute(txnId)) }
                )
            }

            composable(Screen.Publish.route) {
                val offerRepo = OfferRepositoryImpl(ApiClient.offerApi)
                val bankRepo  = BankAccountRepositoryImpl(ApiClient.bankAccountsApi)
                val vm: PublishViewModel = viewModel(
                    factory = PublishViewModel.Factory(offerRepo, ApiClient.exchangeApi, bankRepo)
                )
                PublishScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBankAccounts = { navController.navigate(Screen.BankAccounts.route) }
                )
            }

            composable(
                route = Screen.History.route,
                arguments = listOf(
                    navArgument("filter") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { backStack ->
                val initialFilter = backStack.arguments?.getInt("filter") ?: 0
                val txnRepo = TransactionRepositoryImpl(ApiClient.transactionApi)
                val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(txnRepo))
                var historyUserId by remember { mutableStateOf("") }
                LaunchedEffect(Unit) { historyUserId = tokenManager.getUserId() ?: "" }
                HistoryScreen(
                    viewModel = vm,
                    currentUserId = historyUserId,
                    initialFilter = initialFilter,
                    onBack = { navController.popBackStack() },
                    onNavigateToTransaction = { txnId -> navController.navigate(Screen.Transaction.createRoute(txnId)) },
                    onNavigateToTransactionDetail = { txnId -> navController.navigate(Screen.TransactionDetail.createRoute(txnId)) },
                    onNavigateToPending = { navController.navigate(Screen.Pending.route) }
                )
            }

            composable(
                route = Screen.Rating.route,
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.StringType },
                    navArgument("score") { type = NavType.IntType; defaultValue = 5 }
                )
            ) { backStack ->
                val ratingRepo = RatingRepositoryImpl(ApiClient.ratingApi)
                val txnRepo = TransactionRepositoryImpl(ApiClient.transactionApi)
                val vm: RatingViewModel = viewModel(factory = RatingViewModel.Factory(ratingRepo, txnRepo))
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
                val txnRepo = TransactionRepositoryImpl(ApiClient.transactionApi)
                val vm: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory(txnRepo))
                val ratingRepo = RatingRepositoryImpl(ApiClient.ratingApi)
                val ratingVm: RatingViewModel = viewModel(factory = RatingViewModel.Factory(ratingRepo, txnRepo))
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
                    onSubmitRating = { score, comment, onSuccess, onError ->
                        ratingVm.submitRating(
                            transactionId = id,
                            score = score,
                            comment = comment,
                            onSuccess = onSuccess,
                            onError = onError
                        )
                    },
                    onNavigateBack = {
                        val currentStatus = vm.uiState.value.transaction?.status
                        if (currentStatus in listOf("pending", "accepted", "voucher_uploaded")) {
                            navController.navigate(Screen.History.createRoute(2)) {
                                popUpTo(Screen.Market.route) { inclusive = false }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(
                route = Screen.Receipt.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStack ->
                val txnRepo = TransactionRepositoryImpl(ApiClient.transactionApi)
                val vm: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory(txnRepo))
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
                val txnRepo = TransactionRepositoryImpl(ApiClient.transactionApi)
                val vm: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory(txnRepo))
                TransactionDetailScreen(
                    transactionId = id,
                    viewModel = vm,
                    onNavigateToDispute = { txnId -> navController.navigate(Screen.RegisterDispute.createRoute(txnId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                val userRepo = UserRepositoryImpl(ApiClient.userApi)
                val notifRepo = NotificationRepositoryImpl(ApiClient.notificationApi)
                val vm: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(userRepo, notifRepo)
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
                val userRepo = UserRepositoryImpl(ApiClient.userApi)
                val vm: EditProfileViewModel = viewModel(factory = EditProfileViewModel.Factory(userRepo))
                EditProfileScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.BankAccounts.route) {
                val bankRepo = BankAccountRepositoryImpl(ApiClient.bankAccountsApi)
                val vm: BankAccountsViewModel = viewModel(
                    factory = BankAccountsViewModel.Factory(bankRepo, tokenManager)
                )
                BankAccountsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                val notifRepo = NotificationRepositoryImpl(ApiClient.notificationApi)
                val vm: NotificationsViewModel = viewModel(factory = NotificationsViewModel.Factory(notifRepo))
                NotificationsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Reviews.route) {
                val vm: ReviewsViewModel = viewModel(
                    factory = ReviewsViewModel.Factory(ApiClient.ratingApi)
                )
                ReviewsScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.MyOffers.route) {
                val repo = OfferRepositoryImpl(ApiClient.offerApi)
                val vm: MyOffersViewModel = viewModel(factory = MyOffersViewModel.Factory(repo))
                MyOffersScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onPublishClick = { navController.navigate(Screen.Publish.route) }
                )
            }

            composable(Screen.Complaints.route) {
                val repo = ComplaintsRepositoryImpl(ApiClient.complaintApi)
                val vm: ComplaintsViewModel = viewModel(
                    factory = ComplaintsViewModel.Factory(repo)
                )
                ComplaintsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.MyDisputes.route) {
                val repo = DisputeRepositoryImpl(ApiClient.disputeApi)
                val vm: DisputesViewModel = viewModel(factory = DisputesViewModel.Factory(repo))
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
                val disputeRepo = DisputeRepositoryImpl(ApiClient.disputeApi)
                val vm: DisputesViewModel = viewModel(factory = DisputesViewModel.Factory(disputeRepo))
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
                val repo = DisputeRepositoryImpl(ApiClient.disputeApi)
                val vm: DisputesViewModel = viewModel(factory = DisputesViewModel.Factory(repo))
                val id = backStack.arguments?.getString("disputeId") ?: ""
                DisputeDetailScreen(
                    disputeId = id,
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Admin.route) {
                val adminRepo = AdminRepositoryImpl(ApiClient.adminApi)
                val vm: AdminViewModel = viewModel(factory = AdminViewModel.Factory(adminRepo))
                AdminScreen(
                    viewModel = vm,
                    onNavigate = { route -> navController.navigate(route) },
                    onBack = { navController.popBackStack() }
                )
            }

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

    Column {
        HorizontalDivider(color = BorderColor, thickness = 1.dp)
        NavigationBar(containerColor = SurfaceColor, tonalElevation = 0.dp) {
            NavigationBarItem(
                selected = currentRoute == Screen.Market.route,
                onClick = { onNavigate(Screen.Market.route) },
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Mercado") },
                label = {
                    Text(
                        "Mercado",
                        fontSize = 11.sp,
                        fontWeight = if (currentRoute == Screen.Market.route) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = Primary
                )
            )
            if (isVendor) {
                NavigationBarItem(
                    selected = currentRoute == Screen.Pending.route,
                    onClick = { onNavigate(Screen.Pending.route) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Pendientes") },
                    label = {
                        Text(
                            "Pendientes",
                            fontSize = 11.sp,
                            fontWeight = if (currentRoute == Screen.Pending.route) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Primary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Primary
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Publish.route,
                    onClick = { onNavigate(Screen.Publish.route) },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Publicar") },
                    label = {
                        Text(
                            "Publicar",
                            fontSize = 11.sp,
                            fontWeight = if (currentRoute == Screen.Publish.route) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Primary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Primary
                    )
                )
            }
            NavigationBarItem(
                selected = currentRoute == Screen.Profile.route,
                onClick = { onNavigate(Screen.Profile.route) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                label = {
                    Text(
                        "Perfil",
                        fontSize = 11.sp,
                        fontWeight = if (currentRoute == Screen.Profile.route) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = Primary
                )
            )
        }
    }
}
