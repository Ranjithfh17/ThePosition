package com.fh.theposition.depinjection

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.fh.theposition.util.GPSTracker
import com.fh.theposition.util.MainPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.ZonedDateTime
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideMainPreferences(@ApplicationContext app: Context) = MainPreference(app)

    @Singleton
    @Provides
    fun provideGpsTracker(@ApplicationContext app: Context) = GPSTracker(app)


    @RequiresApi(Build.VERSION_CODES.O)
    @Singleton
    @Provides
    fun provideZonedDateTime(): ZonedDateTime =
        ZonedDateTime.now()

}