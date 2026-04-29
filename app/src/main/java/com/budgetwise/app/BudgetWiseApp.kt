package com.budgetwise.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for BudgetWise.
 *
 * @HiltAndroidApp triggers Hilt's code generation and creates the application-level
 * DI component (SingletonComponent). This annotation is MANDATORY — without it,
 * Hilt cannot inject any dependencies anywhere in the app.
 *
 * Declared in AndroidManifest.xml:
 *   android:name=".BudgetWiseApp"
 *
 * The Application subclass itself is empty — all initialization is handled by
 * the Hilt component and the individual modules (DatabaseModule, AppModule, etc.).
 */
@HiltAndroidApp
class BudgetWiseApp : Application()
