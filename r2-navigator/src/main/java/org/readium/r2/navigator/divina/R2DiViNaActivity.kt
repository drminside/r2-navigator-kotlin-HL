/*
 * Module: r2-navigator-kotlin
 * Developers: Aferdita Muriqi
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.navigator.divina

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewClientCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.readium.r2.navigator.R
import org.readium.r2.navigator.R2ActivityListener
import org.readium.r2.navigator.R2BasicWebView
import org.readium.r2.shared.Publication
import kotlin.coroutines.CoroutineContext


open class R2DiViNaActivity : AppCompatActivity(), CoroutineScope, R2ActivityListener {

    /**
     * Context of this scope.
     */
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override lateinit var preferences: SharedPreferences
    override lateinit var publication: Publication
    override lateinit var publicationIdentifier: String
    override lateinit var publicationPath: String
    override lateinit var publicationFileName: String

    lateinit var divinaWebView: R2BasicWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_r2_divina)

        preferences = getSharedPreferences("org.readium.r2.settings", Context.MODE_PRIVATE)
        divinaWebView = findViewById(R.id.divinaWebView)
        divinaWebView.activity = this
        divinaWebView.listener = this

        publicationPath = intent.getStringExtra("publicationPath")
        publication = intent.getSerializableExtra("publication") as Publication
        publicationFileName = intent.getStringExtra("publicationFileName")

        publicationIdentifier = publication.metadata.identifier
        title = publication.metadata.title

        toggleActionBar()

        // Set up divinaWebView to enable JavaScript and access to local URLs
        divinaWebView.settings.javaScriptEnabled = true
        divinaWebView.settings.allowFileAccess = true
        divinaWebView.settings.allowFileAccessFromFileURLs = true
        divinaWebView.webViewClient = object : WebViewClientCompat() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Define the JS toggleMenu function that will call Android's toggleActionBar
//                divinaWebView.evaluateJavascript("window.androidObj = function AndroidClass(){};", null)
//                divinaWebView.evaluateJavascript("window.androidObj.toggleMenu = function() { Android.toggleMenu() };", null)

                // Now launch the DiViNa player for the folderPath = publicationPath
                divinaWebView.evaluateJavascript("if (player) { player.openDiViNaFromPath('${publicationPath}'); };", null)
            }
        }
        divinaWebView.loadUrl("file:///android_asset/divina/divinaPlayer.html")
        divinaWebView.addJavascriptInterface(divinaWebView, "Android")

    }

    override fun toggleActionBar() {
        launch {
            if (supportActionBar!!.isShowing) {
                divinaWebView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
            } else {
                divinaWebView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        divinaWebView.evaluateJavascript("if (player) { player.destroy(); };", null)
    }
}

