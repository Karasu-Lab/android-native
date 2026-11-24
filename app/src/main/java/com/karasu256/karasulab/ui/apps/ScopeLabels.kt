package com.karasu256.karasulab.ui.apps

import com.karasu256.karasulab.R

fun scopeLabel(scope: String): Int? = when (scope) {
    "openid" -> R.string.scope_openid
    "profile" -> R.string.scope_profile
    "email" -> R.string.scope_email
    "offline_access" -> R.string.scope_offline_access
    "read:profile", "read:users:profile" -> R.string.scope_read_profile
    "write:profile", "write:users:profile" -> R.string.scope_write_profile
    "read:github:user" -> R.string.scope_read_github_user
    "read:github:avatar" -> R.string.scope_read_github_avatar
    "read:github:repositories" -> R.string.scope_read_github_repositories
    else -> null
}
