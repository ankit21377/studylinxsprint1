package com.example.studylinx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.White
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

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegisterBody()
        }
    }
}

@Composable
fun RegisterBody(){
    var firstname by remember {mutableStateOf("")}
    var lastname by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var visibility by remember {mutableStateOf(false)}
    var confirm by remember {mutableStateOf("")}
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues = padding)
                .background(White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                    contentDescription = null,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo1),
                    contentDescription = null,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically){
                Text("Create an Account",
                    style = TextStyle(color = Color(0xFF67A1E4),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp))
            }
            Row {
                Card(
                    modifier = Modifier
                        .height(320.dp)
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF67A1E4))) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 24.dp
                            ) // Add padding inside the card
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp), // More modern way to add space
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            1
                            // --- First Name Field ---
                            OutlinedTextField(
                                value = firstname,
                                onValueChange = { data -> firstname = data },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("First/MiddleName") },
                                modifier = Modifier.weight(1f), // <-- KEY CHANGE
                                singleLine = true, // Good for names
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = White,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )

                            // --- Last Name Field ---
                            OutlinedTextField(
                                value = lastname,
                                onValueChange = { data -> lastname = data },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Lastname") },
                                modifier = Modifier.weight(1f), // <-- KEY CHANGE
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { data ->
                                    email = data
                                },
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("abc@gmail.com")
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = White,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )

                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { data ->
                                    password = data
                                },
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (!visibility) PasswordVisualTransformation() else VisualTransformation.None,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        visibility = !visibility
                                    }) {
                                        Icon(
                                            painter = if (visibility) painterResource(R.drawable.baseline_visibility_24)
                                            else
                                                painterResource(R.drawable.baseline_visibility_off_24),

                                            contentDescription = null
                                        )
                                    }

                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("Password")
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = White,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )

                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = confirm,
                                onValueChange = { data ->
                                    confirm = data
                                },

                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (!visibility) PasswordVisualTransformation() else VisualTransformation.None,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        visibility = !visibility
                                    }) {
                                        Icon(
                                            painter = if (visibility)
                                                painterResource(R.drawable.baseline_visibility_24)
                                            else
                                                painterResource(R.drawable.baseline_visibility_off_24),
                                            contentDescription = null
                                        )
                                    }

                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("Confirm Password")
                                },
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


            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                        .padding(horizontal = 40.dp, vertical = 20.dp),
                ) { Text("Sign Up") }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center

            ){
                Text(
                    buildAnnotatedString {
                        append("Already have an account?")
                        withStyle(style = SpanStyle(color = Blue)){
                            append(" Sign In")
                        }
                    },
                )

            }



        }
    }

}

