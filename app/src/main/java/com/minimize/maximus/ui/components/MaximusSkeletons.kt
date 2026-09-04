package com.minimize.maximus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minimize.maximus.ui.theme.MaximusShapes

/**
 * Generic Shimmer Box for skeleton placeholders
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .shimmer()
    )
}

/**
 * Skeleton Loader for History Screen Workout Cards
 */
@Composable
fun HistoryListSkeletonView(
    modifier: Modifier = Modifier,
    count: Int = 3
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(count) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 18.dp))
                            ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 12.dp))
                        }
                        ShimmerBox(
                            modifier = Modifier.size(width = 60.dp, height = 24.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Stat Pills Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShimmerBox(
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ShimmerBox(
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ShimmerBox(
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Skeleton Loader for Home Screen Dashboard Graph & PRs
 */
@Composable
fun HomeDashboardSkeletonView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Volume Graph Card Skeleton
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 18.dp))
                    ShimmerBox(
                        modifier = Modifier.size(width = 70.dp, height = 24.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // PR Section Skeleton
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier.weight(1f).height(80.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                ShimmerBox(
                    modifier = Modifier.weight(1f).height(80.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

/**
 * Skeleton Loader for Home Screen Top Mini Stat Cards
 */
@Composable
fun HomeTopStatsSkeletonView(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShimmerBox(modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(16.dp))
        ShimmerBox(modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(16.dp))
        ShimmerBox(modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(16.dp))
    }
}

/**
 * Skeleton Loader for Profile Screen Quick Stats
 */
@Composable
fun ProfileStatsSkeletonView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShimmerBox(modifier = Modifier.weight(1f).height(88.dp), shape = RoundedCornerShape(18.dp))
            ShimmerBox(modifier = Modifier.weight(1f).height(88.dp), shape = RoundedCornerShape(18.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShimmerBox(modifier = Modifier.weight(1f).height(88.dp), shape = RoundedCornerShape(18.dp))
            ShimmerBox(modifier = Modifier.weight(1f).height(88.dp), shape = RoundedCornerShape(18.dp))
        }
    }
}

/**
 * Skeleton Loader for Profile Screen Achievements
 */
@Composable
fun ProfileAchievementsSkeletonView(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(3) {
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

/**
 * Full Skeleton Loader for Home Screen Initial Cold Start
 */
@Composable
fun HomeScreenFullSkeletonView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting Shimmer
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(modifier = Modifier.size(width = 180.dp, height = 24.dp), shape = MaximusShapes.Pill)
            ShimmerBox(modifier = Modifier.size(width = 240.dp, height = 16.dp), shape = MaximusShapes.Pill)
        }

        // Weekday Bubble Row Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(7) {
                ShimmerBox(
                    modifier = Modifier
                        .size(width = 44.dp, height = 64.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Today's Hero Card Shimmer
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 14.dp), shape = MaximusShapes.Pill)
                    ShimmerBox(modifier = Modifier.size(width = 200.dp, height = 24.dp), shape = MaximusShapes.Pill)
                    ShimmerBox(modifier = Modifier.size(width = 150.dp, height = 14.dp), shape = MaximusShapes.Pill)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = MaximusShapes.Pill
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .size(48.dp),
                        shape = MaximusShapes.Pill
                    )
                }
            }
        }

        // Action Buttons Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = MaximusShapes.Pill
            )
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = MaximusShapes.Pill
            )
        }
    }
}
