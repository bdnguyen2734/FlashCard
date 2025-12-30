package com.example.flashcard.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flashcard.R;
import com.example.flashcard.data.DataManager;
import com.example.flashcard.utils.InputValidator;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPass, edtConfirm;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Ánh xạ View
        edtEmail = findViewById(R.id.edt_email);
        edtPass = findViewById(R.id.edt_password);
        edtConfirm = findViewById(R.id.edt_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.text_link_login);

        // 2. Xử lý sự kiện bấm nút Đăng Ký
        btnRegister.setOnClickListener(v -> handleRegister());

        // 3. Chuyển sang màn hình Login
        tvLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        // Lấy dữ liệu và xóa khoảng trắng thừa đầu đuôi
        String email = (edtEmail.getText() != null) ? edtEmail.getText().toString().trim() : "";
        String pass = (edtPass.getText() != null) ? edtPass.getText().toString().trim() : "";
        String confirm = (edtConfirm.getText() != null) ? edtConfirm.getText().toString().trim() : "";
        // --- BẮT ĐẦU KIỂM TRA (VALIDATION) ---
        // 1. Kiểm tra Email
        if (!InputValidator.isValidEmail(email)) {
            edtEmail.setError("Email không đúng định dạng (ví dụ: abc@gmail.com)");
            edtEmail.requestFocus(); // Đưa con trỏ về ô này
            return;
        }
        // 2. Kiểm tra độ dài mật khẩu
        if (!InputValidator.isPasswordLongEnough(pass)) {
            edtPass.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtPass.requestFocus();
            return;
        }
        // 3. (Tuỳ chọn) Kiểm tra xem có số không (Logic ký tự)
        if (!InputValidator.hasNumber(pass)) {
            edtPass.setError("Mật khẩu phải chứa ít nhất 1 chữ số để bảo mật");
            edtPass.requestFocus();
            return;
        }
        // 4. Kiểm tra xác nhận mật khẩu
        if (!InputValidator.isPasswordMatch(pass, confirm)) {
            edtConfirm.setError("Mật khẩu xác nhận không khớp");
            edtConfirm.requestFocus();
            return;
        }
        // --- NẾU TẤT CẢ ĐỀU ĐÚNG THÌ MỚI GỌI DATAMANAGER ---
        boolean isSuccess = DataManager.registerUser(this, email, pass);
        if (isSuccess) {
            Toast.makeText(this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show();
            // Đóng màn hình đăng ký, quay về Login
            finish();
        } else {
            // Trường hợp này thường là do Email đã tồn tại trong Database
            Toast.makeText(this, "Đăng ký thất bại. Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
        }
    }
}