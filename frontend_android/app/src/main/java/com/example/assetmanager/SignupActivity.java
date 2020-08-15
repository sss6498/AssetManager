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


public class SignupActivity extends AppCompatActivity {

    private Button signUp;
    private EditText name, email2Id, password2Id, password2ConfirmId;
    private TextView signInLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.name);
        email2Id = findViewById(R.id.email2);
        password2Id = findViewById(R.id.password2);
        password2ConfirmId = findViewById(R.id.passwordConfirm);
        signUp = findViewById(R.id.signUp);
        signInLink = findViewById(R.id.signInLink);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = email2Id.getText().toString();
                String password = password2Id.getText().toString();
                if (email.isEmpty() && password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Both fields are empty!", Toast.LENGTH_SHORT).show();
                } else if(email.isEmpty()) {
                    email2Id.setError("Please enter email");
                    email2Id.requestFocus();
                } else if (password.isEmpty()) {
                    password2Id.setError("Please enter password");
                    password2Id.requestFocus();
                } else if(!(email.isEmpty() && password.isEmpty())) {

                    RequestQueue requestQueue = Volley.newRequestQueue(SignupActivity.this);
                    String url = "http://192.168.0.148:5000/api/users/register";
                    JSONObject postparams = new JSONObject();
                    try {
                        postparams.put("name", name.getText().toString());
                        postparams.put("email", email2Id.getText().toString());
                        postparams.put("password", password2Id.getText().toString());
                        postparams.put("password2", password2ConfirmId.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, postparams,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    userId u = userId.getInstance();
                                    String name = "";
                                    try {
                                        u.setId(response.getString("_id"));
                                        name = response.getString("name");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(SignupActivity.this, "Welcome " + name, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignupActivity.this, AddFinances.class));
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(SignupActivity.this, "Account already exists!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(jsonRequest);

                } else {
                    Toast.makeText(SignupActivity.this, "Error Occurred.", Toast.LENGTH_SHORT).show();
                }

            }
        });


        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });
    }
}
