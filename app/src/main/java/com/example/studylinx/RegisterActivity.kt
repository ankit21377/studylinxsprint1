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
import com.example.studylinx.model.UserModel
import com.example.studylinx.repo.UserRepoImpl
import com.example.studylinx.viewmodel.UserViewModel

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RegisterBody() }
    }
}

@Composable
fun RegisterBody() {

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as RegisterActivity
    val terms = true

    fun goToLoginClearStack() {
        val i = Intent(context, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(i)
        activity.finish()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { goToLoginClearStack() }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                        contentDescription = "Back to Login",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_title"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Create an Account",
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
                        .height(320.dp)
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF67A1E4))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedTextField(
                                value = firstname,
                                onValueChange = { firstname = it },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("First/MiddleName") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reg_firstname"),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = White,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )

                            OutlinedTextField(
                                value = lastname,
                                onValueChange = { lastname = it },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Lastname") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reg_lastname"),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = White,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_email"),
                            placeholder = { Text("abc@gmail.com") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = White,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (!visibility) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(onClick = { visibility = !visibility }) {
                                    Icon(
                                        painter = if (visibility) painterResource(R.drawable.baseline_visibility_24)
                                        else painterResource(R.drawable.baseline_visibility_off_24),
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password"),
                            placeholder = { Text("Password") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = White,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confirm,
                            onValueChange = { confirm = it },
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (!visibility) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(onClick = { visibility = !visibility }) {
                                    Icon(
                                        painter = if (visibility) painterResource(R.drawable.baseline_visibility_24)
                                        else painterResource(R.drawable.baseline_visibility_off_24),
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_confirm"),
                            placeholder = { Text("Confirm Password") },
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
                    onClick = {
                        if (!terms) {
                            Toast.makeText(context, "Please agree to terms & conditions", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password != confirm) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Email and password required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            Toast.makeText(context, "Enter a valid email", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        userViewModel.register(email.trim(), password.trim()) { success, message, userId ->
                            if (success && userId != null) {
                                val model = UserModel(
                                    userId = userId,
                                    firstname = firstname.trim(),
                                    lastname = lastname.trim(),
                                    email = email.trim(),
                                    password = "" // ✅ don’t store plain password
                                )

                                userViewModel.addUserToDatabase(userId, model) { ok, msg ->
                                    if (ok) {
                                        Toast.makeText(context, msg ?: "Registered successfully", Toast.LENGTH_SHORT).show()
                                        goToLoginClearStack()
                                    } else {
                                        Toast.makeText(context, msg ?: "Failed to save user", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 40.dp, vertical = 20.dp)
                        .testTag("reg_signup"),
                ) {
                    Text("Sign Up")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val annotatedString = buildAnnotatedString {
                    append("have an account? ")
                    pushStringAnnotation(tag = "SignIn", annotation = "SignIn")
                    withStyle(style = SpanStyle(color = Blue)) { append("Sign In") }
                    pop()
                }
                ClickableText(
                    modifier = Modifier.testTag("reg_sign_in_link"),
                    text = annotatedString,
                    onClick = { goToLoginClearStack() }
                )
            }
        }
    }
}
