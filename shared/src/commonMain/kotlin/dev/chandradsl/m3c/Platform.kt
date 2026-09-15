package dev.chandradsl.m3c

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform