package com.example.model

import androidx.compose.ui.graphics.Color

enum class Section(
    val title: String,
    val accentColor: Color,
    val routeKey: String
) {
    HOME(title = "主页", accentColor = Color.White, routeKey = "section1"),
    CHARACTERS(title = "角色", accentColor = Color(0xFFFF90B9), routeKey = "section2"),
    MUSIC(title = "音乐", accentColor = Color(0xFFFFF475), routeKey = "section3"),
    PHOTOS(title = "相册", accentColor = Color(0xFF75BFFF), routeKey = "section4"),
    ABOUT(title = "关于", accentColor = Color(0xFFFF5252), routeKey = "section5")
}

data class CharacterProfile(
    val name: String,
    val romaji: String,
    val instrument: String,
    val cv: String,
    val cvRomaji: String,
    val accentColor: Color,
    val lines: List<String>,
    val description: String,
    val artAssetPath: String,
    val posterAssetPath: String,
    val headAssetPath: String
)

data class Track(
    val index: Int,
    val playlistName: String,
    val name: String,
    val durationLabel: String,
    val albumAssetPath: String,
    val audioAssetFileName: String,
    val singer: String,
    val bvid: String
)

data class Producer(
    val name: String,
    val avatarAssetPath: String,
    val accentColor: Color,
    val description: String
)
