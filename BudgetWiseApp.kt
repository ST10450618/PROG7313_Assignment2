package com.budgetwise.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class annotated with @HiltAndroidApp.
 * This triggers Hilt's code generation and initialises the dependency injection
 * container for the entire app. Without this annotation, @Inject fields in
 * Activities, ViewModels, and Repositories would not be resolved at runtime.
 */
@HiltAndroidApp
class BudgetWiseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("BudgetWiseApp", "Application initialised — Hilt DI container ready")
    }
}