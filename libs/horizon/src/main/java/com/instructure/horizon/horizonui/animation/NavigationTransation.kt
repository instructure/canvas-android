/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.horizonui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.instructure.horizon.navigation.animationRules

private const val animatedAlpha: Float = 0.0f
private const val animatedScale: Float = 0.8f

private val slideEnterTransition = slideInHorizontally(initialOffsetX = { it }) +
        fadeIn(initialAlpha = animatedAlpha)

private val slideExitTransition = slideOutHorizontally(targetOffsetX = { -it }) +
        fadeOut(targetAlpha = animatedAlpha)

private val slidePopEnterTransition = slideInHorizontally(initialOffsetX = { -it }) +
        fadeIn(initialAlpha = animatedAlpha)

private val slidePopExitTransition = slideOutHorizontally(targetOffsetX = { it / 2 }) +
        fadeOut(targetAlpha = animatedAlpha)

private val scaleEnterTransition = fadeIn(initialAlpha = animatedAlpha) +
        scaleIn(initialScale = animatedScale)

private val scaleExitTransition = ExitTransition.None

private val scalePopEnterTransition = EnterTransition.None

private val scalePopExitTransition = fadeOut(targetAlpha = animatedAlpha) +
        scaleOut(targetScale = animatedScale)

enum class NavigationTransitionAnimation(
    val enterTransition: EnterTransition,
    val exitTransition: ExitTransition,
    val popEnterTransition: EnterTransition,
    val popExitTransition: ExitTransition
) {
    SLIDE(
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition
    ),
    SCALE(
        enterTransition = scaleEnterTransition,
        exitTransition = scaleExitTransition,
        popEnterTransition = scalePopEnterTransition,
        popExitTransition = scalePopExitTransition
    )
}


fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
    defaultTransitionStyle: NavigationTransitionAnimation = NavigationTransitionAnimation.SLIDE
): EnterTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return (findAnimationStyle(fromRoute, toRoute) ?: defaultTransitionStyle).enterTransition
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
    defaultTransitionStyle: NavigationTransitionAnimation = NavigationTransitionAnimation.SLIDE
): ExitTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return (findAnimationStyle(fromRoute, toRoute) ?: defaultTransitionStyle).exitTransition
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(
    defaultTransitionStyle: NavigationTransitionAnimation = NavigationTransitionAnimation.SLIDE
): EnterTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return (findAnimationStyle(toRoute, fromRoute) ?: defaultTransitionStyle).popEnterTransition
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(
    defaultTransitionStyle: NavigationTransitionAnimation = NavigationTransitionAnimation.SLIDE
): ExitTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return (findAnimationStyle(toRoute, fromRoute) ?: defaultTransitionStyle).popExitTransition
}

private fun findAnimationStyle(fromRoute: String?, toRoute: String?): NavigationTransitionAnimation? {
    return animationRules.firstOrNull { rule ->
        val fromMatches = rule.from == null || fromRoute?.startsWith(rule.from) == true
        val toMatches = rule.to == null || toRoute?.startsWith(rule.to) == true
        fromMatches && toMatches
    }?.style
}