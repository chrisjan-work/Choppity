package com.lairofpixies.choppity

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.lairofpixies.choppity.ui.theme.ChoppityTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChoppityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImagePickerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun DisplayPickedImage(imageUri: Uri?) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Picked Image",
                    modifier = Modifier.fillMaxSize(), // Adjust size as needed
                    contentScale = ContentScale.Fit // Or other ContentScale options
                )
            } else {
                // Optionally display a placeholder or message when no image is selected
                // Example:
                // Text("No image selected", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }


    @Composable
    fun ImagePickerScreen(modifier: Modifier = Modifier) {
        var imageUri by remember { mutableStateOf<Uri?>(null) }

        val pickImageLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                imageUri = uri
            }

        Column(modifier = modifier) {
            Button(onClick = { pickImageLauncher.launch(MIMETYPE_IMAGE) }) {
                Text("Pick Image")
            }

            DisplayPickedImage(imageUri)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewImagePickerScreen() {
        ImagePickerScreen()
    }

    companion object {
        const val MIMETYPE_IMAGE = "image/*"
    }
}