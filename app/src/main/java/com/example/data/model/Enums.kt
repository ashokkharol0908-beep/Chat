package com.example.data.model

enum class ChatType {
    DIRECT,
    GROUP
}

enum class MessageType {
    TEXT,
    VOICE,
    IMAGE,
    MEME,
    VIBE_CHALLENGE,
    CHAOS_SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MoodType(val emoji: String, val title: String, val colorHex: Long) {
    HAPPY("😎", "Vibing", 0xFF10B981),
    CHILL("☕", "Chilling", 0xFF06B6D4),
    LOCKED_IN("🧠", "Locked In", 0xFFA855F7),
    PARTY("🎉", "Partying", 0xFFF43F5E),
    SLEEPY("😴", "Sleepy", 0xFF6366F1),
    CHAOS("😈", "Menace", 0xFFEC4899),
    HUNGRY("🍕", "Starving", 0xFFF59E0B),
    GHOST("👻", "In Invisible Mode", 0xFF94A3B8)
}

enum class VibeType(val label: String, val icon: String) {
    DARE("Dare", "🔥"),
    TRUTH("Truth", "🔮"),
    ROAST("Roast", "💀"),
    COMPLIMENT("Compliment", "✨")
}
