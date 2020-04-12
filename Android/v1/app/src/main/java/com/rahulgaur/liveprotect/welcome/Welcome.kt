package com.rahulgaur.liveprotect.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rahulgaur.liveprotect.R
import com.rahulgaur.liveprotect.home.MainActivity
import kotlinx.android.synthetic.main.activity_welcome.*


class Welcome : AppCompatActivity() {

    companion object {
        private const val TAG: String = "Welcome"
        private const val RC_SIGN_IN: Int = 1
    }

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        configureGoogleSignInAndFirebase()

        welcomeGoogleButton.setOnClickListener { signIn() }
    }

    private fun configureGoogleSignInAndFirebase() {
        mGoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient =
            GoogleSignIn.getClient(this, mGoogleSignInOptions)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account =
                    task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "exception in google sign in : ${e.localizedMessage}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "firebase sign in failed ${it.exception!!.localizedMessage}")
                }
            }
    }
}
