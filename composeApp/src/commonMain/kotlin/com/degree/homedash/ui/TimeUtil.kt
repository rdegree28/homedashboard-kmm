package com.degree.homedash.ui

/** The device's current UTC offset in seconds (e.g. -18000 for US Eastern). Used to bucket
 *  history timestamps into local days for the chart's day axis. */
expect fun localUtcOffsetSeconds(): Int
