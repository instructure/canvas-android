package com.instructure.pandautils.utils

sealed class ScreenState {
    data object Loading : ScreenState()
    data object Error : ScreenState()
    data object Empty : ScreenState()
    data object Content : ScreenState()
}