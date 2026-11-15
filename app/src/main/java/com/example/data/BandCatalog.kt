package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.CharacterProfile
import com.example.model.Producer
import com.example.model.Track

object BandCatalog {

    val characters: List<CharacterProfile> = listOf(
        CharacterProfile(
            name = "后藤一里",
            romaji = "GOTOU  HITORI  ",
            instrument = "Gt.",
            cv = "青山吉能",
            cvRomaji = "Yoshino Aoyama",
            accentColor = Color(0xFFD65484),
            lines = listOf("绝对不要！", "不想工作！！", "好可怕！社会好可怕！！"),
            description = "极度怕生、性格内向的高中一年级学生。是结束乐队的吉他主奏。虽然性格内向，却因憧憬能在乐队活动中闪闪发光而开始学习吉他。实力是货真价实的，但无法在乐队演出或人前好好发挥。每次开口说话前必定会先冒出一声“啊…”。",
            artAssetPath = "images/立绘_后藤一里.png",
            posterAssetPath = "images/海报_后藤一里.png",
            headAssetPath = "images/大头_后藤一里.webp"
        ),
        CharacterProfile(
            name = "伊地知虹夏",
            romaji = "IJICHI  NIJIKA  ",
            instrument = "Dr.",
            cv = "铃代纱弓",
            cvRomaji = "Sayumi Suzushiro",
            accentColor = Color(0xFFF9B000),
            lines = listOf("我的梦想", "或许在不远的将来", "就能实现..."),
            description = "活力满满、开朗的高中二年级学生，结束乐队的鼓手。作为乐队核心成员，总是热心地照顾每位新加入的伙伴。她是Livehouse「STARRY」店长星歌的妹妹，对这家Livehouse怀有特殊的感情。",
            artAssetPath = "images/立绘_伊地知虹夏.png",
            posterAssetPath = "images/海报_伊地知虹夏.png",
            headAssetPath = "images/大头_伊地知虹夏.webp"
        ),
        CharacterProfile(
            name = "山田凉",
            romaji = "YAMADA  RYO  ",
            instrument = "Ba.",
            cv = "水野朔",
            cvRomaji = "Saku Mizuno",
            accentColor = Color(0xFF99D6F1),
            lines = listOf("分散各异的人类个性", "汇聚在一起", "便成为了一种音乐"),
            description = "孤高冷静的高中二年级学生，结束乐队的贝斯手。虹夏的挚友。兴趣爱好脱离世俗，被人称作“怪人”反而会开心。虽然家境优渥，但在乐器上挥霍无度，总是陷入经济危机。偶尔会靠吃杂草充饥",
            artAssetPath = "images/立绘_山田凉.png",
            posterAssetPath = "images/海报_山田凉.png",
            headAssetPath = "images/大头_山田凉.webp"
        ),
        CharacterProfile(
            name = "喜多郁代",
            romaji = "KITA  IKUYO  ",
            instrument = "Gt.Vo.",
            cv = "长谷川育美",
            cvRomaji = "Ikumi Hasegawa",
            accentColor = Color(0xFFCE2525),
            lines = listOf("如果这么厉害的后藤同学", "愿意教导我的话", "或许就能提起干劲了…"),
            description = "开朗受欢迎的高中一年级学生，结束乐队的主唱兼吉他手。天生擅长交际，即使初次见面也能毫不怯场地笑着搭话，是典型的现充。对凉抱有憧憬之情，偶尔会因这份感情过度热情。经常给伊地知星歌（イソスタ）发送照片。",
            artAssetPath = "images/立绘_喜多郁代.png",
            posterAssetPath = "images/海报_喜多郁代.png",
            headAssetPath = "images/大头_喜多郁代.webp"
        )
    )

    val playlistNames: List<String> = listOf("結束バンド", "バンド")

    val playlists: Map<String, List<Track>> = mapOf(
        "結束バンド" to listOf(
            Track(1, "結束バンド", "青春コンプレックス", "03:23", "albums/結束バンド.jpg", "青春コンプレックス.mp3", "结束バンド", "BV1HT411N7FP"),
            Track(2, "結束バンド", "ひとりぼっち東京", "03:52", "albums/ひとりぼっち東京.jpg", "ひとりぼっち東京.mp3", "结束バンド", "BV1V84y1v766"),
            Track(3, "結束バンド", "Distortion!!", "03:23", "albums/Distortion!!.jpg", "Distortion!!.mp3", "结束バンド", "BV1ng411h71y"),
            Track(4, "結束バンド", "ひみつ基地", "03:52", "albums/ひみつ基地 - 結束バンド.jpg", "ひみつ基地 - 結束バンド.mp3", "结束バンド", "BV14wkzYPEHG"),
            Track(5, "結束バンド", "ギターと孤独と蒼い惑星", "03:48", "albums/ギターと孤独と蒼い惑星.jpg", "ギターと孤独と蒼い惑星.mp3", "结束バンド", "BV1sv4y1U7j6"),
            Track(6, "結束バンド", "ラブソングが歌えない", "03:09", "albums/ラブソングが歌えない - 結束バンド.jpg", "ラブソングが歌えない - 結束バンド.mp3", "结束バンド", "BV1o24y1U7gR"),
            Track(7, "結束バンド", "あのバンド", "03:33", "albums/あのバンド.jpg", "あのバンド.mp3", "结束バンド", "BV1v24y1C735"),
            Track(8, "結束バンド", "カラカラ", "04:25", "albums/カラカラ.jpg", "カラカラ.mp3", "结束バンド", "BV1ae4y127K5"),
            Track(9, "結束バンド", "小さな海", "03:43", "albums/結束バンド.jpg", "小さな海.mp3", "结束バンド", "BV1UG4y1J7mg"),
            Track(10, "結束バンド", "なにが悪い", "03:47", "albums/なにが悪い.jpg", "なにが悪い.mp3", "结束バンド", "BV1jK411o7Q5"),
            Track(11, "結束バンド", "忘れてやらない", "03:43", "albums/忘れてやらない.jpg", "忘れてやらない.mp3", "结束バンド", "BV1pM41117Yr"),
            Track(12, "結束バンド", "星座になれたら", "04:18", "albums/星座になれたら.jpg", "星座になれたら.mp3", "结束バンド", "BV1u8411H7yA"),
            Track(13, "結束バンド", "フラッシュバッカー", "04:35", "albums/フラッシュバッカー - 結束バンド.jpg", "フラッシュバッカー - 結束バンド.mp3", "结束バンド", "BV15v4y1B7ZQ"),
            Track(14, "結束バンド", "転がる岩、君に朝が降る", "04:31", "albums/転がる岩、君に朝が降る.jpg", "転がる岩、君に朝が降る.mp3", "结束バンド", "BV1nV4y1w74h"),
            Track(15, "結束バンド", "milky way", "03:32", "albums/We will.png", "milky way.mp3", "结束バンド", "BV1mVpGewEfz"),
            Track(16, "結束バンド", "光の中へ", "04:18", "albums/光の中へ.jpg", "光の中へ.mp3", "结束バンド", "BV1ik4y1s7Sf"),
            Track(17, "結束バンド", "月並みに辉け", "04:07", "albums/月並みに輝け - 結束バンド.jpg", "月並みに輝け - 結束バンド.mp3", "结束バンド", "BV1wb421v7Mb"),
            Track(18, "結束バンド", "ドッペルゲンガー", "03:39", "albums/ドッペルゲンガー - 結束バンド.jpg", "ドッペルゲンガー - 結束バンド.mp3", "结束バンド", "BV1hS411w773"),
            Track(19, "結束バンド", "Re；Re；", "05:07", "albums/Re；Re； - 結束バンド.jpg", "Re；Re； - 結束バンド.mp3", "结束バンド", "BV1YDVYzeE39"),
            Track(20, "結束バンド", "僕と三原色", "03:39", "albums/僕と三原色 - 結束バンド.jpg", "僕と三原色 - 結束バンド.mp3", "结束バンド", "BV1xn4y1f7GJ")
        ),
        "バンド" to listOf(
            Track(1, "バンド", "Connected Sky", "02:06", "albums/Connected Sky.jpg", "Connected Sky.mp3", "KARUT", "BV1X64y1A71s"),
            Track(2, "バンド", "Cagayake!GIRLS", "04:10", "albums/Cagayake!GIRLS - 放課後ティータイム.jpg", "Cagayake!GIRLS - 放課後ティータイム.mp3", "放課後ティータイム", "BV1V54y167fr"),
            Track(3, "バンド", "天使にふれたよ！", "04:41", "albums/天使にふれたよ! - 放課後ティータイム.jpg", "天使にふれたよ! - 放課後ティータイム.mp3", "放課後ティータイム", "BV1prnQebEwm"),
            Track(4, "バンド", "春日影 - MyGO!!!!!", "04:16", "albums/春日影 - MyGO!!!!!.png", "春日影 - MyGO!!!!!.mp3", "MyGO!!!!!", "BV1JHLUz4EUy")
        )
    )

    val galleryPhotos: List<String> = listOf(
        "images/p_1.jpeg", "images/p_2.jpeg", "images/p_3.jpeg",
        "images/p_4.gif", "images/p_5.jpeg", "images/p_6.jpeg",
        "images/p_7.jpeg", "images/p_8.jpeg", "images/p_9.jpeg",
        "images/p_10.jpeg", "images/p_11.jpeg", "images/p_12.jpeg",
        "images/p_13.jpeg", "images/p_14.jpeg", "images/p_15.jpeg",
        "images/p_16.jpeg", "images/p_17.gif", "images/p_18.gif",
        "images/p_19.gif", "images/p_20.gif", "images/p_21.gif",
        "images/p_22.gif", "images/p_23.jpeg"
    )

    fun buildGalleryRows(): List<List<String>> {
        val master = galleryPhotos.shuffled()
        fun rotate(offset: Int): List<String> {
            val k = offset % master.size
            return master.drop(k) + master.take(k)
        }
        return listOf(
            master,
            rotate(8),
            rotate(16)
        )
    }

    val producers: List<Producer> = listOf(
        Producer(
            name = "Sudoria",
            avatarAssetPath = "images/Sudoria.jpg",
            accentColor = Color(0xFFD65484),
            description = "Bocchi the Rock！！！"
        ),
        Producer(
            name = "Alola",
            avatarAssetPath = "images/Alola.jpg",
            accentColor = Color(0xFFCE2525),
            description = "那我问你，有耳朵不能体现耋，没耳朵不能体现耄，那耄耋娘化是什么样子？"
        ),
        Producer(
            name = "Daxi",
            avatarAssetPath = "images/Daxi.jpg",
            accentColor = Color(0xFF99D6F1),
            description = "关注永雏塔菲喵，关注永雏塔菲谢谢喵"
        ),
        Producer(
            name = "Rquars",
            avatarAssetPath = "images/Rquars.jpg",
            accentColor = Color(0xFFF9B000),
            description = "什么时候才能长高..."
        )
    )

    val loginBackgrounds: List<String> = listOf(
        "images/LoginImage1.jpg",
        "images/LoginImage3.jpg"
    )

    val loginTargetImages: List<String> = listOf(
        "images/target-image1.png",
        "images/target-image2.png",
        "images/target-image3.png",
        "images/target-image4.png",
        "images/target-image5.png",
        "images/target-image6.png"
    )
}
