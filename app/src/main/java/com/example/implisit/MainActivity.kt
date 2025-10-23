package com.example.implisit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.implisit.ui.theme.IMPLISITTheme

class MainActivity : ComponentActivity() {

    // Helper function untuk meluncurkan Intent dengan pengecekan
    private fun launchIntent(intent: Intent, failureMessage: String) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, failureMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // --- Intent Functions ---
    private fun launchCamera() = launchIntent(
        Intent(MediaStore.ACTION_IMAGE_CAPTURE),
        "Aplikasi Kamera tidak ditemukan!"
    )

    private fun launchDialer() {
        // ACTION_DIAL tidak memerlukan pengecekan resolveActivity di banyak kasus
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:")))
    }

    private fun launchWebLink(url: String) = launchIntent(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)),
        "Browser Web tidak ditemukan!"
    )

    private fun launchInstagram() {
        var intent: Intent? = Intent(Intent.ACTION_VIEW, Uri.parse("instagram://feed"))

        if (intent?.resolveActivity(packageManager) == null) {
            // Fallback ke web browser
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/"))
        }

        intent?.let { startActivity(it) } ?: run {
            Toast.makeText(this, "Gagal membuka Instagram.", Toast.LENGTH_LONG).show()
        }
    }
    // -------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() tidak diperlukan karena Scaffold sudah menggunakan fillMaxSize()
        setContent {
            IMPLISITTheme {
                Scaffold(Modifier.fillMaxSize()) { innerPadding ->
                    ImplicitIntentScreen(
                        onCameraClick = ::launchCamera,
                        onCallClick = ::launchDialer,
                        onLinkClick = { launchWebLink("https://chatgpt.com") },
                        onFreeFeatureClick = ::launchInstagram,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// --- Composable Functions (UI) yang Diringkas ---

@Composable
fun ImplicitIntentScreen(
    onCameraClick: () -> Unit,
    onCallClick: () -> Unit,
    onLinkClick: () -> Unit,
    onFreeFeatureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Penggunaan IntentButton Diringkas
        IntentButton("📸 Kamera", onCameraClick)
        IntentButton("📞 Telepon", onCallClick)
        IntentButton("🔗 Link Web", onLinkClick)
        IntentButton("📱 Instagram", onFreeFeatureClick)
    }
}

@Composable
fun IntentButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(70.dp)
    ) {
        Text(text)
    }
}