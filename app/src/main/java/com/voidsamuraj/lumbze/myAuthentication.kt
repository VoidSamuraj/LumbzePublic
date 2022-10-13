package com.voidsamuraj.lumbze

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MyAuthentication(clientId:String, activity: MainActivity) {

    private  var auth: FirebaseAuth = Firebase.auth
    private var oneTapClient: SignInClient? = null
    private var signInRequest: BeginSignInRequest? = null
    private var mActivity:MainActivity?=null
    private var currentUser:FirebaseUser? = auth.currentUser

    fun getUser():FirebaseUser?=currentUser
    fun signOut()=auth.signOut()
    fun firebaseOnStartSetup(ifUserLogged:()->Unit){
        currentUser = auth.currentUser
        if(currentUser!=null) {
            Log.d("FIREBASE", "signInWithToken:success")
            ifUserLogged()
        }
    }
    init{
        //one tap
        mActivity=activity
        oneTapClient = Identity.getSignInClient(activity)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(clientId)
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

    }

    fun login(){
        //one tap
        oneTapClient!!.beginSignIn(signInRequest!!)
            .addOnSuccessListener(mActivity as Activity) { result ->
                try {
                    startIntentSenderForResult(
                        mActivity!!,
                        result.pendingIntent.intentSender, 2137,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("ONE_CLICK", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(mActivity as Activity) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d("ONE_CLICK", ""+e.localizedMessage)
            }
    }
    fun onFirebaseResult(requestCode:Int,data:Intent?,onSignSuccess:()->Unit){
        when (requestCode) {
            2137 -> {
                try {
                    val credential = oneTapClient!!.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val password = credential.password
                    when {
                        idToken != null -> {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(mActivity as Activity) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("FIREBASE", "signInWithCredential:success")
                                        currentUser=auth.currentUser
                                        onSignSuccess()

                                        // updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("FIREBASE", "signInWithCredential:failure", task.exception)
                                        //updateUI(null)
                                    }
                                }


                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            Log.v("ONE_CLICK", "Got ID token.")
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.v("ONE_CLICK", "Got password.")
                        }
                        else -> {
                            Log.e("ONE_CLICK", "No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    Log.e("ON_ACTIVITY_RESULT", e.toString())
                }
            }
        }
    }
}
