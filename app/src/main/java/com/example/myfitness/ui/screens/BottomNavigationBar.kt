package com.example.myfitness.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        containerColor = Color(0xFF8671F5),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            selected = navController.currentDestination?.route == "home",
            onClick = { navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }},
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = navController.currentDestination?.route == "sleep",
            onClick = { navController.navigate("sleep") {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }},
            icon = { Icon(Icons.Default.Alarm, contentDescription = "Sleep") },
            label = { Text("Sleep") }
        )
        NavigationBarItem(
            selected = navController.currentDestination?.route == "workout",
            onClick = { navController.navigate("workout") {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }},
            icon = { Icon(Icons.Default.SportsGymnastics, contentDescription = "workout") },
            label = { Text("workout") }
        )
        NavigationBarItem(
            selected = navController.currentDestination?.route == "profile",
            onClick = { navController.navigate("profile") {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }},
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}