package com.zenflow.brain.detox.util

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.client.http.InputStreamContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.Collections

class GoogleDriveHelper(private val context: Context) {

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun getSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    suspend fun uploadBackup(jsonContent: String): Boolean = withContext(Dispatchers.IO) {
        val account = getSignedInAccount() ?: return@withContext false
        val driveService = getDriveService(account)

        val metadata = File()
            .setName("zenflow_backup.json")
            .setParents(Collections.singletonList("appDataFolder"))

        val contentStream = InputStreamContent(
            "application/json",
            ByteArrayInputStream(jsonContent.toByteArray())
        )

        try {
            // Find existing backup to update or create new
            val existingFiles = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'zenflow_backup.json'")
                .execute()
                .files

            if (existingFiles.isNullOrEmpty()) {
                driveService.files().create(metadata, contentStream).execute()
            } else {
                driveService.files().update(existingFiles[0].id, null, contentStream).execute()
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("GoogleDriveHelper", "Upload failed: ${e.message}", e)
            false
        }
    }

    suspend fun downloadBackup(): String? = withContext(Dispatchers.IO) {
        val account = getSignedInAccount() ?: return@withContext null
        val driveService = getDriveService(account)

        try {
            val existingFiles = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'zenflow_backup.json'")
                .execute()
                .files

            if (existingFiles.isNullOrEmpty()) return@withContext null

            val fileId = existingFiles[0].id
            val outputStream = java.io.ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singletonList(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Zenflow Brain Detox")
            .build()
    }

    fun signOut() {
        googleSignInClient.signOut()
    }
}
