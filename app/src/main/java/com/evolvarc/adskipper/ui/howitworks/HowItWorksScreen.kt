package com.evolvarc.adskipper.ui.howitworks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evolvarc.adskipper.R
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme

data class FAQItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowItWorksScreen(
    onNavigateUp: () -> Unit
) {
    val faqItems = listOf(
        FAQItem(
            question = "What does AdSkipper do?",
            answer = "AdSkipper automatically clicks the \"Skip Ad\" button on YouTube videos for you. When a skippable ad appears and the skip button becomes available (usually after 5 seconds), our app instantly clicks it so you don't have to. It doesn't block ads—it just automates what you would do manually, saving you time and interruptions."
        ),
        FAQItem(
            question = "How does AdSkipper work?",
            answer = "AdSkipper uses Android's Accessibility Service to monitor your screen when YouTube is open. When it detects the \"Skip Ad\" button appears, it automatically taps it for you. The app only activates when you're watching YouTube and goes into sleep mode when you close the app, saving battery. Think of it as a tiny robot that presses skip for you!"
        ),
        FAQItem(
            question = "Is AdSkipper safe? Does it collect my data?",
            answer = "Absolutely safe and 100% private. AdSkipper collects ZERO data. Everything runs locally on your device—no internet connection needed, no data sent to servers, and no tracking. The app is also open source on GitHub, so anyone can verify the code. We only monitor YouTube and nothing else. Your privacy is our priority."
        ),
        FAQItem(
            question = "Why does AdSkipper need Accessibility Permission?",
            answer = "Accessibility Permission is required for the app to \"see\" your screen and detect the Skip Ad button, then perform the tap action. This is the only way Android allows apps to interact with other apps. We ONLY use this permission for YouTube and only to click the skip button—nothing else. Google approves apps using Accessibility Services for legitimate purposes like ours."
        ),
        FAQItem(
            question = "Will this drain my battery?",
            answer = "No! AdSkipper is designed to be battery-efficient. It only actively monitors when YouTube is open and automatically goes into idle mode when you close YouTube or switch apps. When idle, it uses minimal resources. Our smart detection system also throttles checks to once every 500ms, preventing unnecessary CPU usage."
        ),
        FAQItem(
            question = "Does AdSkipper block YouTube ads?",
            answer = "No, AdSkipper does NOT block ads. Ads still play for 5 seconds (or however long before the skip button appears), which helps support content creators. We only automate clicking the skip button when it becomes available. This means creators still get ad revenue, and you get to skip faster. It's a win-win approach that respects YouTube's ecosystem."
        ),
        FAQItem(
            question = "Do I need to manually enable it every time?",
            answer = "No! Enable once, works forever. After you grant Accessibility Permission (one-time setup), AdSkipper automatically detects whenever you open YouTube and starts monitoring for ads. You never need to touch the app again—it works seamlessly in the background. Just open YouTube and enjoy ad-free interruptions!"
        ),
        FAQItem(
            question = "Will YouTube ban my account for using this?",
            answer = "Extremely unlikely. AdSkipper doesn't modify YouTube's app, block ads, or violate their API terms. We simply automate tapping the skip button—something you can already do manually. Unlike ad blockers that YouTube fights against, skip automation tools have coexisted without issues. However, use any third-party tool at your own discretion. We recommend being aware of YouTube's Terms of Service."
        ),
        FAQItem(
            question = "What if the app stops working after a YouTube update?",
            answer = "YouTube occasionally changes their button IDs, which can temporarily affect detection. We've built 5 fallback detection methods to handle this. If you notice ads not being skipped, check for an app update in the Play Store—we typically release fixes within 24-48 hours of YouTube UI changes. You can also check our GitHub for real-time updates."
        ),
        FAQItem(
            question = "Can I use AdSkipper on multiple devices?",
            answer = "Yes! AdSkipper works independently on each device. Simply install the app from Play Store on each device and enable Accessibility Permission. Your skip stats (ads skipped, time saved) are stored locally per device and don't sync between devices. Each device operates independently with full functionality."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.how_it_works_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            faqItems.forEachIndexed { index, faqItem ->
                ExpandableFAQCard(
                    number = index + 1,
                    question = faqItem.question,
                    answer = faqItem.answer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExpandableFAQCard(
    number: Int,
    question: String,
    answer: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Question Number Circle
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = number.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Question Text
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isExpanded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )

                // Expand/Collapse Icon
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Rounded.ExpandLess
                    } else {
                        Icons.Rounded.ExpandMore
                    },
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isExpanded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Answer - Animated Visibility
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp)),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExpanded) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HowItWorksScreenPreview() {
    AdSkipperTheme {
        HowItWorksScreen(onNavigateUp = {})
    }
}

