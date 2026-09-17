package com.aminmart.passwordmanager.ui.components

import com.aminmart.passwordmanager.domain.model.PasswordCategory

val PasswordCategory.displayName: String
    get() = when (this) {
        PasswordCategory.SOCIAL -> "Social"
        PasswordCategory.EMAIL -> "Email"
        PasswordCategory.SHOPPING -> "Shopping"
        PasswordCategory.FINANCE -> "Finance"
        PasswordCategory.ENTERTAINMENT -> "Entertainment"
        PasswordCategory.WORK -> "Work"
        PasswordCategory.OTHER -> "Other"
    }

val PasswordCategory.icon: String
    get() = when (this) {
        PasswordCategory.SOCIAL -> "📱"
        PasswordCategory.EMAIL -> "📧"
        PasswordCategory.SHOPPING -> "🛒"
        PasswordCategory.FINANCE -> "💰"
        PasswordCategory.ENTERTAINMENT -> "🎬"
        PasswordCategory.WORK -> "💼"
        PasswordCategory.OTHER -> "🔐"
    }
