package com.lainovic.tomtom.straycat.ui

import android.widget.Toast

fun showToast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}