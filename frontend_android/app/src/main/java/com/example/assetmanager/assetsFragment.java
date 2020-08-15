package com.example.assetmanager;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class assetsFragment extends Fragment {

    private TextView assetsTV;
    private JSONObject accountsLink = new JSONObject();

    HashMap<Integer, Integer> incomeMap = new HashMap<>();
    HashMap<Integer, Integer> expenseMap = new HashMap<>();
    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    DateTime localDate;

    private CombinedChart chart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_assets, container, false);

        assetsTV = (TextView) v.findViewById(R.id.txtAssets);

        chart = v.findViewById(R.id.combinedtest);
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);

        //get accounts
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String url1 = "http://192.168.0.148:5000/api/plaid/accounts";
        JSONObject postparams = new JSONObject();
        userId u = userId.getInstance();
        JodaTimeAndroid.init(getActivity());
        localDate = new DateTime();
        localDate = localDate.withTimeAtStartOfDay();

        try {
            postparams.put("userId", u.getId());
        } catch (JSONException e) {
            assetsTV.setText("Could not put postparams: " + e.toString());
        }

        CustomJsonArrayRequest jsonRequest = new CustomJsonArrayRequest(Request.Method.POST, url1, postparams, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    accountsLink.put("accounts", response);
                    assetsTV.setText("Loading ...");
                } catch (JSONException e) {
                    assetsTV.setText("Could not add to global variable");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                assetsTV.setText(error.toString());
            }
        });
        requestQueue.add(jsonRequest);

        //get transactions from those accounts
        String url2 = "http://192.168.0.148:5000/api/plaid/accounts/transactions";
        CustomJsonArrayRequest jsonRequest2 = new CustomJsonArrayRequest(Request.Method.POST, url2, accountsLink, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response != null) {
                    Log.d("Number of Accounts", Integer.toString(response.length()));
                    try {
                        for (int i =0; i < response.length(); i ++) {
                            Log.d("Account Name", response.getJSONObject(i).getString("accountName"));
                            JSONArray transactiontemp = response.getJSONObject(i).getJSONArray("transactions");
                            for (int j=0; j<transactiontemp.length(); j++) {
                                int amount = transactiontemp.getJSONObject(j).getInt("amount");
                                DateTime date = formatter.parseDateTime(transactiontemp.getJSONObject(j).getString("date")).withTimeAtStartOfDay();
                                //Log.d("amount for: " + j, Integer.toString(amount) + " on date: " + date);
                                if (amount >= 0) {
                                    if (incomeMap.get(date) != null) {
                                        int amounttemp = incomeMap.get(date);
                                        incomeMap.put(30 - Days.daysBetween(date, localDate).getDays(), amounttemp+amount);
                                    } else {
                                        incomeMap.put(30 - Days.daysBetween(date, localDate).getDays(), amount);
                                    }
                                } else {
                                    if (expenseMap.get(date) != null) {
                                        int amounttemp = expenseMap.get(date);
                                        expenseMap.put(30 - Days.daysBetween(date, localDate).getDays(), amounttemp+amount);
                                    } else {
                                        expenseMap.put(30 - Days.daysBetween(date, localDate).getDays(), amount);
                                    }
                                }
                            }
                        }

                        assetsTV.setText("Income: " + incomeMap.toString() + "\n Expenses: " + expenseMap.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("Income", incomeMap.toString());
                Log.d("Expenses", expenseMap.toString());
                Log.d("income, 16", Integer.toString(incomeMap.get(16)));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                assetsTV.setText(error.toString());
            }
        });
        requestQueue.add(jsonRequest2);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Start Graph
        //String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d("Local Date", localDate.toString());

        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.BUBBLE, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });

        Legend l = chart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        String[] mMonths = new String[] {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"
        };

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return mMonths[(int) value % mMonths.length];
            }
        });

        CombinedData data = new CombinedData();

        //data.setData(generateLineData());
        data.setData(generateBarData());

        xAxis.setAxisMaximum(data.getXMax() + 0.25f);

        chart.setData(data);
        chart.invalidate();


    }

    private BarData generateBarData() {


        ArrayList<BarEntry> entries1 = new ArrayList<>();
        ArrayList<BarEntry> entries2 = new ArrayList<>();
        entries1 = getBarEnteriesIncome(entries1);
        entries2 = getBarEnteriesExpenses(entries2);


        Log.d("entries1", entries1.toString());

        BarDataSet set1 = new BarDataSet(entries1, "Income");
        set1.setColor(Color.rgb(0, 0, 255));
        set1.setValueTextColor(Color.rgb(0, 0, 255));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        BarDataSet set2 = new BarDataSet(entries1, "Income");
        set2.setColor(Color.rgb(0, 255, 0));
        set2.setValueTextColor(Color.rgb(0, 255, 0));
        set2.setValueTextSize(10f);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);

        float groupSpace = 0.06f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.45f; // x2 dataset
        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"

        BarData d = new BarData(set1, set2);
        d.setBarWidth(barWidth);

        // make this BarData object grouped
        d.groupBars(0, groupSpace, barSpace); // start at x = 0

        return d;
    }

    private ArrayList<BarEntry> getBarEnteriesIncome(ArrayList<BarEntry> entries1){
        for (int index = 0; index <= 30; index++) {
            Log.d("incomemap.get", Integer.toString(index) + ": " + Integer.toString(incomeMap.get(index)));
            if (incomeMap.get(index) != null) {
                entries1.add(new BarEntry(index, incomeMap.get(index)));
            } else {
                entries1.add(new BarEntry(index, 0));
            }
        }
        return  entries1;
    }

    private ArrayList<BarEntry> getBarEnteriesExpenses(ArrayList<BarEntry> entries1){
        for (int index = 0; index <= 30; index++) {
            if (expenseMap.get(index) != null) {
                entries1.add(new BarEntry(index, expenseMap.get(index)));
            } else {
                entries1.add(new BarEntry(index, 0));
            }
        }
        return  entries1;
    }

}


