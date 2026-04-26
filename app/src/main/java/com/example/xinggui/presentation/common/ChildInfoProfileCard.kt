package com.example.xinggui.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xinggui.R
import com.example.xinggui.ui.theme.IosCard
import com.example.xinggui.ui.theme.IosSeparator

@Composable
fun ChildInfoProfileCard(
    childName: String,
    age: Int,
    interventionDuration: String,
    modifier: Modifier = Modifier
) {
    val profileInfo = remember(childName, age, interventionDuration) {
        buildProfileDisplayInfo(
            childName = childName,
            age = age,
            interventionDuration = interventionDuration
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        border = BorderStroke(1.dp, IosSeparator.copy(alpha = 0.82f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = ChildProfileAssets.decorLeftStar),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 10.dp, y = (-10).dp)
            )
            Image(
                painter = painterResource(id = ChildProfileAssets.decorRightStar),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-14).dp, y = (-12).dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = ChildProfileAssets.profilePhoto),
                    contentDescription = "儿童头像",
                    modifier = Modifier
                        .size(width = 104.dp, height = 136.dp)
                        .clip(RoundedCornerShape(52.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChildInfoLine(
                        text = profileInfo.nameLine,
                        fontSize = 22,
                        lineHeight = 26,
                        fontWeight = FontWeight.Black
                    )
                    ChildInfoLine(text = profileInfo.birthDateLine)
                    ChildInfoLine(text = profileInfo.ageLine)
                    ChildInfoLine(text = profileInfo.interventionDurationLine)
                }
            }
        }
    }
}

@Composable
private fun ChildInfoLine(
    text: String,
    fontSize: Int = 18,
    lineHeight: Int = 23,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Text(
        text = text,
        color = if (fontSize >= 22) Color(0xFF0E1A2A) else Color(0xFF1A1A1A),
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        lineHeight = lineHeight.sp,
        fontFamily = FontFamily.SansSerif
    )
}

private object ChildProfileAssets {
    val profilePhoto = R.drawable.report_ref_a21e3f22c0330add844d0bbc5855d710
    val decorLeftStar = R.drawable.report_ref_755f917ee7c2d0ea710d17e80388515e
    val decorRightStar = R.drawable.report_ref_928735bc81fe83f919803074a6ac915c
}
