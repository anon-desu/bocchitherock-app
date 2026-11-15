package com.example

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.example.data.AuthRepository
import com.example.data.local.SessionPreferences
import com.example.data.remote.CloudflareApiService
import com.example.playback.MusicPlayerController

class BocchiApplication : Application(), ImageLoaderFactory {

    val sessionPreferences: SessionPreferences by lazy { SessionPreferences(this) }
    val cloudflareApiService: CloudflareApiService by lazy { CloudflareApiService.create() }
    val authRepository: AuthRepository by lazy {
        AuthRepository(cloudflareApiService, sessionPreferences)
    }
    val musicPlayerController: MusicPlayerController by lazy {
        MusicPlayerController(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
