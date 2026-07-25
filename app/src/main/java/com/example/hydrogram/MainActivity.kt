package com.example.hydrogram

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hydrogram.domain.usecase.StartTrackingPresenceUseCase
import com.example.hydrogram.presentation.navigation.RootNavGraph
import com.example.hydrogram.ui.theme.HydrogramTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var startTrackingPresenceUseCase: StartTrackingPresenceUseCase

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if(currentUid != null) {
            Log.d("MainActivity", "tracking started, currentUid: $currentUid")
            startTrackingPresenceUseCase(uid = currentUid)
        }

        enableEdgeToEdge()
        try {
            val info = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )

            info.signingInfo?.apkContentsSigners?.forEach { cert ->
                val md = java.security.MessageDigest.getInstance("SHA-1")
                val sha1 = md.digest(cert.toByteArray())
                    .joinToString(":") { "%02X".format(it) }
                Log.e("SHA_CHECK", "SHA-1: $sha1")

                val md256 = java.security.MessageDigest.getInstance("SHA-256")
                val sha256 = md256.digest(cert.toByteArray())
                    .joinToString(":") { "%02X".format(it) }
                Log.e("SHA_CHECK", "SHA-256: $sha256")
            }
        } catch (e: Exception) {
            Log.e("SHA_CHECK", "Error", e)
        }
        setContent {
            RootNavGraph()
        }
    }
}
