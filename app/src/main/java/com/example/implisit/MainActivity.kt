package com.example.implisit

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.implisit.ui.theme.IMPLISITTheme
import java.lang.IllegalStateException

class MainActivity : ComponentActivity() {

    // --- ActivityResultLaunchers ---
    // Launcher untuk mengambil gambar. Hasilnya adalah boolean 'isSuccess'.
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            // latestTmpUri akan berisi URI dari file yang baru saja disimpan
            Toast.makeText(this, "Foto berhasil disimpan di Galeri", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher untuk meminta izin. Hasilnya adalah boolean 'isGranted'.
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Izin diberikan, panggil fungsi internal untuk meluncurkan kamera
            launchCameraInternal()
        } else {
            // Izin ditolak, beri tahu pengguna
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    // Variabel untuk menyimpan URI gambar sementara
    private var latestTmpUri: Uri? = null
    // --- End of ActivityResultLaunchers ---


    // Helper function untuk meluncurkan Intent dengan pengecekan
    private fun launchIntent(intent: Intent, failureMessage: String) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, failureMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // --- Intent Functions ---
    // Fungsi ini sekarang akan memeriksa izin sebelum meluncurkan kamera
    private fun launchCamera() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> {
                // Izin sudah ada, langsung luncurkan kamera
                launchCameraInternal()
            }
            else -> {
                // Izin belum ada, minta izin terlebih dahulu
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Fungsi ini hanya akan dipanggil setelah izin kamera dikonfirmasi.
    private fun launchCameraInternal() {
        try {
            latestTmpUri = getImageUri()
            // Gunakan .let untuk memastikan URI tidak null sebelum meluncurkan launcher
            latestTmpUri?.let { uri ->
                takePictureLauncher.launch(uri)
            }
        } catch (e: IllegalStateException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    // Membuat URI untuk file gambar baru di galeri
    private fun getImageUri(): Uri {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ImplisitApp") // Simpan di folder Pictures/ImplisitApp
        }
        // Menyisipkan entri baru dan mendapatkan URI-nya
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Gagal membuat URI Gambar")
    }


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
