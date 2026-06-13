package com.zenflow.brain.detox.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenflow.brain.detox.di.appViewModel

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = appViewModel { AuthViewModel(it) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthenticated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Zenflow",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 40.sp
        )
        Text(
            text = "Brain Detox",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = if (uiState.isLoginMode) "Welcome Back" else "Create Account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (uiState.isLoginMode) {
                    viewModel.login(email, password)
                } else {
                    viewModel.signup(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(if (uiState.isLoginMode) "Login" else "Sign Up")
            }
        }

        TextButton(onClick = viewModel::toggleMode) {
            Text(if (uiState.isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Login")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalDivider()
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = viewModel::enterAsGuest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue as Guest")
        }
    }
}
