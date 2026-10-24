package com.example.docscanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { ScannerScreen() } } }
    }
}

private fun scansDir(a: Activity) = File(a.filesDir, "scans").apply { mkdirs() }
private fun listScans(a: Activity) =
    scansDir(a).listFiles { f -> f.extension == "pdf" }?.sortedByDescending { it.lastModified() } ?: emptyList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen() {
    val activity = LocalContext.current as Activity
    var files by remember { mutableStateOf(listScans(activity)) }
    var renaming by remember { mutableStateOf<File?>(null) }
    var newName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(50)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL) // crop, rotate, filters, cleaning
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(options) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            GmsDocumentScanningResult.fromActivityResultIntent(r.data)?.pdf?.uri?.let { uri ->
                val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                val out = File(scansDir(activity), "Scan_$stamp.pdf")
                activity.contentResolver.openInputStream(uri)?.use { i -> out.outputStream().use { i.copyTo(it) } }
                files = listScans(activity)
            }
        }
    }

    fun startScan() {
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { launcher.launch(IntentSenderRequest.Builder(it).build()) }
            .addOnFailureListener { error = it.message }
    }

    fun uriFor(f: File): Uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", f)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Doc Scanner") }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { startScan() }) { Text("＋ Scan") } }
    ) { pad ->
        if (files.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("No scans yet. Tap Scan to begin.")
            }
        } else LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(files, key = { it.name }) { f ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(f.nameWithoutExtension, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${f.length() / 1024} KB · " + SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(f.lastModified())),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = {
                                activity.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uriFor(f), "application/pdf")
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                            }) { Text("Open") }
                            TextButton(onClick = {
                                activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("application/pdf")
                                    .putExtra(Intent.EXTRA_STREAM, uriFor(f)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), "Share PDF"))
                            }) { Text("Share") }
                            TextButton(onClick = { renaming = f; newName = f.nameWithoutExtension }) { Text("Rename") }
                            TextButton(onClick = { f.delete(); files = listScans(activity) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }

    renaming?.let { f ->
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text("Rename") },
            text = { OutlinedTextField(newName, { newName = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    val clean = newName.replace(Regex("[^A-Za-z0-9 _-]"), "").trim()
                    if (clean.isNotEmpty()) f.renameTo(File(f.parentFile, "$clean.pdf"))
                    renaming = null; files = listScans(activity)
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renaming = null }) { Text("Cancel") } }
        )
    }
    error?.let {
        AlertDialog(onDismissRequest = { error = null }, confirmButton = { TextButton(onClick = { error = null }) { Text("OK") } },
            title = { Text("Scanner unavailable") }, text = { Text(it) })
    }
}
