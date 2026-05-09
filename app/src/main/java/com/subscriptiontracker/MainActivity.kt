package com.subscriptiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubscriptionTrackerTheme {
                SubscriptionTrackerNavGraph()
            }
        }
    }
}

@Composable
fun SubscriptionTrackerNavGraph() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val addEditViewModel: AddEditViewModel = viewModel()
    val detailViewModel: DetailViewModel = viewModel()

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
                onEditClick = { id ->
                    navController.navigate("addEdit/$id")
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
