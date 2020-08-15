package com.example.assetmanager;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.annotation.Nullable;
import kotlin.Unit;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.plaid.link.Plaid;
import com.plaid.linkbase.models.configuration.LinkConfiguration;
import com.plaid.linkbase.models.configuration.PlaidProduct;
import com.plaid.linkbase.models.connection.LinkConnection;
import com.plaid.linkbase.models.connection.PlaidLinkResultHandler;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class AddFinances extends AppCompatActivity {
    private static final int LINK_REQUEST_CODE = 1;
    private TextView result;
    private TextView completed;

    @SuppressLint({"StringFormatMatches", "StringFormatInvalid"})
    private PlaidLinkResultHandler plaidLinkResultHandler = new PlaidLinkResultHandler(
            LINK_REQUEST_CODE,
            linkConnection -> {
                // Handle onSuccess
                LinkConnection.LinkConnectionMetadata metadata = linkConnection.getLinkConnectionMetadata();

                RequestQueue requestQueue = Volley.newRequestQueue(AddFinances.this);
                String url = "http://192.168.0.148:5000/api/plaid/accounts/add";
                JSONObject postparams = new JSONObject();
                userId u = userId.getInstance();

                try {
                    postparams.put("userId", u.getId());
                    postparams.put("public_token", linkConnection.getPublicToken());
                    postparams.put("institutionId", metadata.institutionId);
                    postparams.put("institutionName", metadata.institutionName);
                    postparams.put("accountName", metadata.getAccounts().get(0).accountName);
                    postparams.put("accountType", metadata.getAccounts().get(0).accountType);
                    postparams.put("accountSubtype", metadata.getAccounts().get(0).getAccountSubType());
                } catch (JSONException e) {
                    result.setText("Could not put postparams: " + e.toString());
                }

                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, postparams,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    result.setText(getString(
                                            R.string.success,
                                            response.getString("institutionName"),
                                            response.getString("accountName")));
                                } catch (JSONException e) {
                                    result.setText(getString(
                                            R.string.success,
                                            "null",
                                            "null"));
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        result.setText("Error with Server: " + error.toString());
                    }
                });
                requestQueue.add(jsonRequest);
                return Unit.INSTANCE;
            },
            linkCancellation -> {
                // Handle onCancelled
                result.setText(getString(
                        R.string.content_cancelled,
                        linkCancellation.getInstitutionId(),
                        linkCancellation.getInstitutionName(),
                        linkCancellation.getLinkSessionId(),
                        linkCancellation.getStatus()));
                return Unit.INSTANCE;
            },
            plaidApiError -> {
                // Handle onExit
                result.setText(getString(
                        R.string.content_exit,
                        plaidApiError.getDisplayMessage(),
                        plaidApiError.getErrorCode(),
                        plaidApiError.getErrorMessage(),
                        plaidApiError.getLinkExitMetadata().getInstitutionId(),
                        plaidApiError.getLinkExitMetadata().getInstitutionName(),
                        plaidApiError.getLinkExitMetadata().getStatus()));
                return Unit.INSTANCE;
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfinances);
        result = findViewById(R.id.result);

        View accountLink = findViewById(R.id.accountLink);
        accountLink.setOnClickListener(view -> {
            setOptionalEventListener();
            openLink();
        });

        completed = findViewById(R.id.completed);
        completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddFinances.this, MainActivity.class));
            }
        });
    }


    private void setOptionalEventListener() {
        Plaid.setLinkEventListener(linkEvent -> {
            Log.i("Event", linkEvent.toString());
            return Unit.INSTANCE;
        });
    }

//opens Plaid Link to sign into all accounts
    private void openLink() {
        ArrayList<PlaidProduct> products = new ArrayList<>();
        products.add(PlaidProduct.TRANSACTIONS);
        Plaid.openLink(
                this,
                new LinkConfiguration.Builder("AssetManager", products).build(),
        LINK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!plaidLinkResultHandler.onActivityResult(requestCode, resultCode, data)) {
            Log.i(this.getClass().getSimpleName(), "Not handled");
        }
    }



}
