package com.example.studylinx

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studylinx.repo.UserRepoImpl

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LoginBody() }
    }
}

@Composable
fun LoginBody() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repo = remember { UserRepoImpl() }

    fun doLogin() {
        val e = email.trim()
        val p = password.trim()

        if (e.isEmpty()) {
            Toast.makeText(context, "Email is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            Toast.makeText(context, "Enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }
        if (p.isEmpty()) {
            Toast.makeText(context, "Password is required", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        repo.login(e, p) { success, message ->
            isLoading = false

            if (success) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                val intent = Intent(context, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White)
        ) {

            // ✅ back arrow now goes back if needed (optional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                        contentDescription = "Back"
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(painter = painterResource(R.drawable.logo1), contentDescription = null)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Log in to your account",
                    style = TextStyle(
                        color = Color(0xFF67A1E4),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                )
            }

            Row {
                Card(
                    modifier = Modifier
                        .height(240.dp)
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF67A1E4))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email"),
                            placeholder = { Text("abc@gmail.com") },
                            singleLine = true,
                            enabled = !isLoading,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = White,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (!visibility) PasswordVisualTransformation()
                            else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(
                                    onClick = { visibility = !visibility },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        painter = if (visibility)
                                            painterResource(R.drawable.baseline_visibility_24)
                                        else painterResource(R.drawable.baseline_visibility_off_24),
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password"),
                            placeholder = { Text("Password") },
                            singleLine = true,
                            enabled = !isLoading,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = White,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { doLogin() },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 40.dp, vertical = 20.dp)
                        .testTag("login"),
                ) {
                    Text(if (isLoading) "Signing in..." else "Sign in")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val annotatedString = buildAnnotatedString {
                    append("Don't have an account? ")
                    pushStringAnnotation(tag = "SignUp", annotation = "SignUp")
                    withStyle(style = SpanStyle(color = Blue)) { append("Sign Up") }
                    pop()
                }

                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations("SignUp", offset, offset)
                            .firstOrNull()?.let {
                                context.startActivity(Intent(context, RegisterActivity::class.java))
                            }
                    }
                )
            }
        }
    }
}
