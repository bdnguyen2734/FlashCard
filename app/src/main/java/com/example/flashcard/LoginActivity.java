package com.example.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
// QUAN TRỌNG: Import thư viện Material Design
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    // Khai báo đúng kiểu TextInputEditText
    private TextInputEditText edtEmail, edtPass;
    private Button btnLogin, btnGoogle;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtEmail = findViewById(R.id.edit_email_login);
        edtPass = findViewById(R.id.edit_password_login);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google_signin);
        tvRegister = findViewById(R.id.text_link_register);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Sự kiện Click
        btnLogin.setOnClickListener(v -> handleNormalLogin());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleNormalLogin() {
        // Lấy text an toàn từ TextInputEditText
        String email = (edtEmail.getText() != null) ? edtEmail.getText().toString().trim() : "";
        String pass = (edtPass.getText() != null) ? edtPass.getText().toString().trim() : "";

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            return;
        }
        if (pass.isEmpty() || pass.length() < 6) {
            edtPass.setError("Mật khẩu phải từ 6 ký tự");
            return;
        }

        if (DataManager.checkLogin(this, email, pass)) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    // Gọi hàm DataManager
                    boolean success = DataManager.loginOrRegisterWithGoogle(this, account.getEmail());
                    if (success) {
                        Toast.makeText(this, "Xin chào " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập Google thất bại (Code: " + e.getStatusCode() + ")", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}