package com.voidsamuraj.lumbze

sealed class Screen(val route:String) {
    object MainScreen:Screen("mains_screen")
    object StartScreen:Screen("start_screen")
    object StatsScreen:Screen("stats_screen")
}