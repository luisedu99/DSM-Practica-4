package sv.edu.udb.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailTV, passwordTV;
    private Button loginBtn, registerBtn;
    private SignInButton loginGoogle;
    private LoginButton loginButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    /**
     * Agregando información para el
     * OneTap de Google
     */
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    // ...
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    // ...

    private static final String EMAIL = "email";

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * Obteniendo la instancia de Firebase
         */
        mAuth=FirebaseAuth.getInstance();

        /**
         * Instancia de Facebook
         */
        callbackManager = CallbackManager.Factory.create();

        /**
         * Se inicializa la UI del login
         */
        initializeUI();

        /**
         * Se inicializa el OneTap
         */
        //setTap();

        /**
         * Listener de click en botón de registro
         * unicamente cambia de Activity
         */
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        /**
         * Listener de click en botón de Registro
         */
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUserAccount();
            }
        });

        /**
         * Listener de Login con Google
         */
        loginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail().build();
                gsc = GoogleSignIn.getClient(getApplicationContext(),gso);
                startActivityForResult(gsc.getSignInIntent(),100);
            }
        });

        /**
         * Listener de Login de Facebook
         */
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
                Toast.makeText(LoginActivity.this, "Facebook hecho!"+loginResult.getAccessToken(), Toast.LENGTH_SHORT).show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Facebook cancelado!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Toast.makeText(LoginActivity.this, "Facebook falló!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("TOKEN", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LOGRADO: ", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FALLO", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /**
     * On Activity Result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                task.getResult(ApiException.class);

                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();

            }catch (ApiException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Algo salio mal",Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Funcion para que funcione el OneTap
     */
    public void setTap(){
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        //.setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
            @Override
            public void onSuccess(BeginSignInResult beginSignInResult) {
                try {
                    startIntentSenderForResult(
                            beginSignInResult.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                            null, 0, 0, 0);
                }catch (IntentSender.SendIntentException e){
                    Log.e("Error en OneTap: ", "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // No Google Accounts found. Just continue presenting the signed-out UI.
                Log.d("Peligro: ", e.getLocalizedMessage());
            }
        });
    }

    /**
     * Al inicializar la aplicación se verifica si ya habia
     * una sesión iniciada
     */
    /*public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Toast.makeText(LoginActivity.this, "Inicio de Sesión Exitoso ", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);

        Intent intent=new Intent(LoginActivity.this,DashboardActivity.class);
        startActivity(intent);
    }*/

    private void loginUserAccount(){
        progressBar.setVisibility(View.VISIBLE);

        String email,password;
        email=emailTV.getText().toString();
        password=passwordTV.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(),"Porfavor ingrese su correo...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Porfavor ingrese su contraseña...", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Inicio de Sesión Exitoso", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                    Intent intent=new Intent(LoginActivity.this,DashboardActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(LoginActivity.this, "El Inicio de Sesión ha fallado! Porfavor, volver a intentar", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initializeUI(){
        /**
         * EditText para agregar correo y
         * contraseña de correo electronico
         */
        emailTV=findViewById(R.id.email);
        passwordTV=findViewById(R.id.password);

        /**
         * Lo minimo requerido para un login
         * con correo y contraseña
         */
        loginBtn=findViewById(R.id.login);
        registerBtn=findViewById(R.id.register);
        progressBar=findViewById(R.id.progressBar);

        /**
         * Botón de Login de Google
         */
        loginGoogle=findViewById(R.id.sign_in_button);

        /**
         * Botón de Login de Facebook
         */
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
    }
}