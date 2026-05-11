package com.subscriptiontracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subscriptiontracker.ui.addedit.AddEditScreen
import com.subscriptiontracker.ui.addedit.AddEditViewModel
import com.subscriptiontracker.ui.detail.DetailScreen
import com.subscriptiontracker.ui.detail.DetailViewModel
import com.subscriptiontracker.ui.home.HomeScreen
import com.subscriptiontracker.ui.home.HomeViewModel
import com.subscriptiontracker.ui.theme.SubscriptionTrackerTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — either way app continues */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)
        requestNotificationPermission()
        setContent {
            SubscriptionTrackerTheme {
                SubscriptionTrackerNavGraph(deepLinkId = pendingDeepLinkId)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private var pendingDeepLinkId: Long? = null

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme == "subscriptiontracker" && uri.host == "detail") {
            pendingDeepLinkId = uri.lastPathSegment?.toLongOrNull()
        }
    }
}

@Composable
fun SubscriptionTrackerNavGraph(deepLinkId: Long? = null) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val addEditViewModel: AddEditViewModel = viewModel()
    val detailViewModel: DetailViewModel = viewModel()

    androidx.compose.runtime.LaunchedEffect(deepLinkId) {
        deepLinkId?.let { navController.navigate("detail/$it") }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onAddClick = {
                    addEditViewModel.resetForNew()
                    navController.navigate("addEdit/0")
                },
                onDetailClick = { id ->
                    navController.navigate("detail/$id")
                }
            )
        }

        composable(
            route = "addEdit/{subscriptionId}",
            arguments = listOf(
                navArgument("subscriptionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: 0L
            AddEditScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = addEditViewModel
            )
        }

        composable(
            route = "detail/{subscriptionId}",
            arguments = listOf(
                navArgument("subscriptionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: 0L
            DetailScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateEdit = { id ->
                    navController.navigate("addEdit/$id")
                },
                viewModel = detailViewModel
            )
        }
    }
}
