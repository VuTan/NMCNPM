package com.example.myandroidproject.customer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myandroidproject.ConnectionDBSQLite;
import com.example.myandroidproject.helpers.StringHelper;
import com.example.myandroidproject.R;
import com.example.myandroidproject.utils.Constraint;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {
    private TextView goBackLogin;
    private EditText firstName, lastName, emailSignUp, password, passwordConfirm;
    private MaterialButton signUpButton;
    private ConnectionDBSQLite DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        goBackLogin = findViewById(R.id.go_back_login);
        goBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        firstName = findViewById(R.id.first_Name);
        lastName = findViewById(R.id.last_name);
        emailSignUp = findViewById(R.id.email_Sign_Up);
        password = findViewById(R.id.password_Sign_Up);
        passwordConfirm = findViewById(R.id.password_Sign_Up_Confirm);

        // Hook Sign Up Button
        signUpButton = findViewById(R.id.sign_Up);

//        signUpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (firstName.getText().toString().trim().isEmpty()) {
//                    Toast.makeText(SignUpActivity.this, "Enter First Name", Toast.LENGTH_SHORT).show();
//                } else if (lastName.getText().toString().trim().isEmpty()) {
//                    Toast.makeText(SignUpActivity.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
//                } else if (emailSignUp.getText().toString().trim().isEmpty()) {
//                    Toast.makeText(SignUpActivity.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
//                } else if (password.getText().toString().trim().isEmpty()) {
//                    Toast.makeText(SignUpActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
//                } else if (!password.getText().toString().trim().equals(passwordConfirm.getText().toString().trim())) {
//                    Toast.makeText(SignUpActivity.this, "Enter valid password", Toast.LENGTH_SHORT).show();
//                } else {
//                    if (emailChecker(emailSignUp.getText().toString().trim())) {
//                        createUser(emailSignUp.getText().toString().trim(), password.getText().toString().trim());
//                    } else {
//                        Toast.makeText(SignUpActivity.this, "Enter valid email", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processFormFields();
            }
        });

    }
    public void goToHome(View view) {
        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToSignInAct(View view) {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void processFormFields() {

        if (!validationFirstName()
                || !validationLastName()
                || !validationEmail()
                || !validationPasswordAndPassConfirm()) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(SignUpActivity.this);

        String url = Constraint.URL_BE + "/api/v1/user/register";


        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("firstname", firstName.getText().toString());
            jsonBody.put("lastname", lastName.getText().toString());
            jsonBody.put("email", emailSignUp.getText().toString());
            jsonBody.put("password", password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                firstName.setText(null);
                lastName.setText(null);
                emailSignUp.setText(null);
                password.setText(null);
                passwordConfirm.setText(null);
                Toast.makeText(SignUpActivity.this, "Đăng ký thành công !!!", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
                Toast.makeText(SignUpActivity.this, "Đăng ký thất bại !!!", Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public boolean validationFirstName() {
        String firstname = firstName.getText().toString();
        if (firstname.isEmpty()) {
            firstName.setError("Tên không được để trống.");
            return false;
        } else {
            firstName.setError(null);
            return true;
        }
    }
    public boolean validationLastName() {
        String lastname = lastName.getText().toString();
        if (lastname.isEmpty()) {
            lastName.setError("Họ không được để trống.");
            return false;
        } else {
            lastName.setError(null);
            return true;
        }
    }
    public boolean validationEmail() {
        String email = emailSignUp.getText().toString();
        if (email.isEmpty()) {
            emailSignUp.setError("Email không được để trống.");
            return false;
        } else if (!StringHelper.regexEmailValidationPattern(email)) {
            emailSignUp.setError("Vui lòng nhập email hợp lệ.");
            return false;
        } else {
            emailSignUp.setError(null);
            return true;
        }
    }

    public boolean validationPasswordAndPassConfirm() {
        String pass = password.getText().toString();
        String passConf = passwordConfirm.getText().toString();
        if (pass.isEmpty()) {
            password.setError("Mật khẩu không được để trống.");
            return false;
        } else if (!pass.equals(passConf)) {
            password.setError("Mật khẩu không khớp !!!");
            return false;
        } else if (passConf.isEmpty()) {
            passwordConfirm.setError("Xác nhận mật khẩu không được để trống.");
            return false;
        } else {
            password.setError(null);
            passwordConfirm.setError(null);
            return true;
        }
    }
}