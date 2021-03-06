package ca.uqac.sosdoit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import ca.uqac.sosdoit.util.Util;

public class LoginActivity extends AppCompatActivity
{
    private EditText inputEmail, inputPassword;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.la_password);
        progressBar = findViewById(R.id.progress_bar);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String email = pref.getString(getString(R.string.pref_email), "");
        if (!TextUtils.isEmpty(email)) {
            inputPassword.requestFocus();
            inputEmail.setText(email);
        }

        findViewById(R.id.la_btn_login).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                login();
            }
        });

        findViewById(R.id.la_btn_register).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), Util.REGISTRATION_REQUEST);
            }
        });

        findViewById(R.id.la_btn_reset_password).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(LoginActivity.this, ResetPasswordActivity.class), Util.RESET_PASSWORD_REQUEST);
            }
        });

        inputPassword.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Util.REGISTRATION_REQUEST:
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    break;
                case Util.RESET_PASSWORD_REQUEST:
                    inputEmail.setText(pref.getString(getString(R.string.pref_email), ""));
                    Toast.makeText(LoginActivity.this, R.string.msg_login_after_reset_password, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void login()
    {
        final String email = inputEmail.getText().toString().trim();
        final String password = inputPassword.getText().toString();

        boolean exit = false;

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError(getString(R.string.msg_empty_email));
            inputEmail.requestFocus();
            exit = true;
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.setError(getString(R.string.msg_empty_password));

            if (!exit) {
                inputPassword.requestFocus();
            }

            return;
        }

        if (exit) {
            return;
        }

        Util.toggleKeyboard(LoginActivity.this);
        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>()
                {
                    @Override
                    public void onSuccess(AuthResult authResult)
                    {
                        pref.edit().putString(getString(R.string.pref_email), email).apply();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        progressBar.setVisibility(View.GONE);
                        if (pref.getString(getString(R.string.pref_email), "").equals(email) && !TextUtils.isEmpty(email)) {
                            inputPassword.requestFocus();
                        } else {
                            inputEmail.requestFocus();
                        }
                        Util.toggleKeyboard(LoginActivity.this);
                        Toast.makeText(LoginActivity.this, R.string.msg_auth_failed, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
