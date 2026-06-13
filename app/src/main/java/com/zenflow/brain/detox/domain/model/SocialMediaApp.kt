package com.zenflow.brain.detox.domain.model

data class SocialMediaApp(
    val packageName: String,
    val displayName: String,
    val iconResName: String? = null,
)

object SocialMediaCatalog {
    val defaultApps = listOf(
        SocialMediaApp("com.instagram.android", "Instagram"),
        SocialMediaApp("com.facebook.katana", "Facebook"),
        SocialMediaApp("com.zhiliaoapp.musically", "TikTok"),
        SocialMediaApp("com.twitter.android", "X (Twitter)"),
        SocialMediaApp("com.snapchat.android", "Snapchat"),
        SocialMediaApp("com.reddit.frontpage", "Reddit"),
        SocialMediaApp("com.pinterest", "Pinterest"),
        SocialMediaApp("com.google.android.youtube", "YouTube"),
        SocialMediaApp("com.whatsapp", "WhatsApp"),
        SocialMediaApp("org.telegram.messenger", "Telegram"),
        SocialMediaApp("com.discord", "Discord"),
        SocialMediaApp("com.linkedin.android", "LinkedIn"),
    )

    val presetTimerMinutes = listOf(5, 15, 30, 60, 120)
}
