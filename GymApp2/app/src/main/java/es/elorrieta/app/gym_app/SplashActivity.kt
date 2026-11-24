package es.elorrieta.app.gym_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val cajaLogo = findViewById<FrameLayout>(R.id.cajaLogo)
        val imgLogo = findViewById<ImageView>(R.id.imgLogo)

        val animCajaEntrada = AnimationUtils.loadAnimation(this, R.anim.caja_entrada)
        val animLogoAparecer = AnimationUtils.loadAnimation(this, R.anim.logo_aparecer)
        val animLevantar = AnimationUtils.loadAnimation(this, R.anim.logo_levantar)
        val animCajaExpandir = AnimationUtils.loadAnimation(this, R.anim.caja_expandir)

        // 1. La caja entra -> aparece el logo dentro
        animCajaEntrada.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                imgLogo.visibility = View.VISIBLE
                imgLogo.startAnimation(animLogoAparecer)
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })

        // 2. El logo aparece -> hace 2 repeticiones
        animLogoAparecer.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                imgLogo.startAnimation(animLevantar)
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })

        // 3. Termina de levantar -> la caja llena la pantalla
        animLevantar.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                imgLogo.visibility = View.INVISIBLE
                cajaLogo.startAnimation(animCajaExpandir)
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })

        // 4. La caja llena la pantalla -> siguiente pantalla
        animCajaExpandir.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                irAlLogin()
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })

        cajaLogo.startAnimation(animCajaEntrada)
    }

    private fun irAlLogin() {
        // Cuando exista LoginActivity, cambiar MainActivity por LoginActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}