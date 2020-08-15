package com.example.assetmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    
    private Button login;
    private EditText emailId, passwordId;
    private TextView signUpLink;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailId = findViewById(R.id.email);
        passwordId = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signUpLink = findViewById(R.id.signUpLink);
        
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailId.getText().toString();
                String password = passwordId.getText().toString();
                if (email.isEmpty() && password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Both fields are empty!", Toast.LENGTH_SHORT).show();
                } else if(email.isEmpty()) {
                    emailId.setError("Please enter email");
                    emailId.requestFocus();
                } else if (password.isEmpty()) {
                    passwordId.setError("Please enter password");
                    passwordId.requestFocus();
                } else if(!(email.isEmpty() && password.isEmpty())) {
                    RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                    String url = "http://192.168.0.148:5000/api/users/login";
                    JSONObject postparams = new JSONObject();
                    try {
                        postparams.put("email", email);
                        postparams.put("password", password);
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this, "Could not enter credentials", Toast.LENGTH_SHORT).show();
                    }

                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, postparams,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    userId u = userId.getInstance();
                                    String name = "";
                                    String success = "";
                                    try {
                                        success = response.getString("success");
                                        name = response.getString("name");
                                        u.setId(response.getString("userId"));
                                    } catch (JSONException e) {
                                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                    }
                                    if (success=="true") {
                                        Toast.makeText(LoginActivity.this, "Welcome back " + name, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(jsonRequest);

                } else {
                    Toast.makeText(LoginActivity.this, "Error Occurred.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

    }

}
