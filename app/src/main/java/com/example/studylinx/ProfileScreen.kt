package com.example.studylinx

import android.R.attr.contentDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .padding(all = 30.dp)
                        .fillMaxWidth()
                        .background(Color.Blue),
                    horizontalArrangement = Arrangement.End
                ) {

                        Icon(
                            painter = painterResource(id = R.drawable.baseline_edit_24),
                            null,

                            tint = Color.White

                        )
                }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Blue),
                        horizontalArrangement = Arrangement.Center,
                    ) {


                        Text(
                            "My Profile", style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                color = Color.White
                            )
                        )
                    }


            }

        }
    }

