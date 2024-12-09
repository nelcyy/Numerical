package com.example.numerical;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class MainActivity extends AppCompatActivity {

    private EditText functionInput, lowerLimitInput, upperLimitInput, toleranceInput;
    private TextView resultOutput;
    private Button calculateButton, resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI elements
        functionInput = findViewById(R.id.editTextText);
        lowerLimitInput = findViewById(R.id.LowerLimit);
        upperLimitInput = findViewById(R.id.UpperLimit);
        toleranceInput = findViewById(R.id.Tolerance);
        resultOutput = findViewById(R.id.Result);
        calculateButton = findViewById(R.id.calculate);
        resetButton = findViewById(R.id.Reset);

        resetButton.setVisibility(View.GONE);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCalculation();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                functionInput.setText("");
                lowerLimitInput.setText("");
                upperLimitInput.setText("");
                toleranceInput.setText("");
                resultOutput.setText("");
                resetButton.setVisibility(View.GONE);
                resultOutput.setVisibility(View.GONE);
            }
        });

        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetButton.setVisibility(View.VISIBLE);
                resultOutput.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        functionInput.addTextChangedListener(inputWatcher);
        lowerLimitInput.addTextChangedListener(inputWatcher);
        upperLimitInput.addTextChangedListener(inputWatcher);
        toleranceInput.addTextChangedListener(inputWatcher);
    }

    private void performCalculation() {
        try {
            String function = functionInput.getText().toString();
            double lowerLimit = Double.parseDouble(lowerLimitInput.getText().toString());
            double upperLimit = Double.parseDouble(upperLimitInput.getText().toString());
            double tolerance = Double.parseDouble(toleranceInput.getText().toString());

            if (lowerLimit >= upperLimit) {
                resultOutput.setText("Error: Lower limit must be less than upper limit.");
                resultOutput.setVisibility(View.VISIBLE);
                return;
            }

            // Calculate the true value of the integral
            double trueValue = calculateTrueIntegral(function, lowerLimit, upperLimit);

            // Determine the number of subintervals required for both rules
            int midpointSubintervals = findRequiredSubintervals(function, lowerLimit, upperLimit, trueValue, tolerance, true);
            int trapezoidSubintervals = findRequiredSubintervals(function, lowerLimit, upperLimit, trueValue, tolerance, false);

            // Calculate areas for both methods
            double midpointArea = calculateMidpointRule(function, lowerLimit, upperLimit, midpointSubintervals);
            double trapezoidArea = calculateTrapezoidRule(function, lowerLimit, upperLimit, trapezoidSubintervals);

            // Calculate their respective true errors
            double midpointError = Math.abs(trueValue - midpointArea);
            double trapezoidError = Math.abs(trueValue - trapezoidArea);

            // Determine which method performs better
            String betterMethod = "";
            if (midpointError < trapezoidError) {
                betterMethod = "Midpoint Rule is better with a smaller error.";
            } else if (trapezoidError < midpointError) {
                betterMethod = "Trapezoid Rule is better with a smaller error.";
            } else {
                betterMethod = "Both methods have the same error.";
            }

            // Display the results
            String result = "True Value: " + trueValue + "\n\n" +
                    "Midpoint Rule:\n" +
                    "  - Required Subintervals: " + midpointSubintervals + "\n" +
                    "  - Area: " + midpointArea + "\n" +
                    "  - Error: " + midpointError + "\n\n" +
                    "Trapezoid Rule:\n" +
                    "  - Required Subintervals: " + trapezoidSubintervals + "\n" +
                    "  - Area: " + trapezoidArea + "\n" +
                    "  - Error: " + trapezoidError + "\n\n" +
                    betterMethod;

            resultOutput.setText(result);
            resultOutput.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            resultOutput.setText("Error: " + e.getMessage());
            resultOutput.setVisibility(View.VISIBLE);
        }
    }

    private int findRequiredSubintervals(String function, double lower, double upper, double trueValue, double tolerance, boolean useMidpoint) {
        int subintervals = 1;
        double error;

        do {
            double area = useMidpoint ? calculateMidpointRule(function, lower, upper, subintervals)
                    : calculateTrapezoidRule(function, lower, upper, subintervals);
            error = Math.abs(trueValue - area);
            subintervals++;
        } while (error > tolerance);

        return subintervals - 1;
    }

    private double calculateMidpointRule(String function, double lower, double upper, int n) {
        double width = (upper - lower) / n;
        double area = 0.0;

        for (int i = 0; i < n; i++) {
            double midpoint = lower + (i + 0.5) * width;
            area += evaluateFunction(function, midpoint);
        }

        return area * width;
    }

    private double calculateTrapezoidRule(String function, double lower, double upper, int n) {
        double width = (upper - lower) / n;
        double area = 0.5 * (evaluateFunction(function, lower) + evaluateFunction(function, upper));

        for (int i = 1; i < n; i++) {
            double x = lower + i * width;
            area += evaluateFunction(function, x);
        }
        return area * width;
    }

    private double calculateTrueIntegral(String function, double lower, double upper) {
        int n = 1000; // Ensure n is even for Simpson's Rule
        if (n % 2 != 0) {
            n++;
        }

        double h = (upper - lower) / n; // Step size
        double integral = evaluateFunction(function, lower) + evaluateFunction(function, upper);

        // Simpson's Rule loop
        for (int i = 1; i < n; i++) {
            double x = lower + i * h;
            if (i % 2 == 0) { // Even index
                integral += 2 * evaluateFunction(function, x);
            } else { // Odd index
                integral += 4 * evaluateFunction(function, x);
            }
        }

        integral *= h / 3.0; // Multiply by h/3 to complete the formula
        return integral;
    }


    private double evaluateFunction(String function, double x) {
        try {
            Expression expression = new ExpressionBuilder(function)
                    .variables("x")
                    .build()
                    .setVariable("x", x);
            return expression.evaluate();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid function: " + function);
        }
    }
}
