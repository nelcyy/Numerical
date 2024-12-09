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

    private EditText functionInput, lowerLimitInput, upperLimitInput, subintervalInput;
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
        subintervalInput = findViewById(R.id.Subinterval);
        resultOutput = findViewById(R.id.Result);
        calculateButton = findViewById(R.id.calculate);
        resetButton = findViewById(R.id.Reset);

        // Initially set Reset button visibility to GONE
        resetButton.setVisibility(View.GONE);

        // Set onClick listener for the calculate button
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCalculation();
            }
        });

        // Add TextWatcher to detect input changes and show Reset button
        functionInput.addTextChangedListener(createTextWatcher());
        lowerLimitInput.addTextChangedListener(createTextWatcher());
        upperLimitInput.addTextChangedListener(createTextWatcher());
        subintervalInput.addTextChangedListener(createTextWatcher());

        // Set onClick listener for the Reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear all input fields when the Reset button is clicked
                functionInput.setText("");
                lowerLimitInput.setText("");
                upperLimitInput.setText("");
                subintervalInput.setText("");

                // Hide the Reset button after clearing the fields
                resetButton.setVisibility(View.GONE);
                resultOutput.setVisibility(View.GONE);
            }
        });
    }

    // Method to create a TextWatcher that will show the Reset button if any EditText has input
    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Check if any EditText has text and show the Reset button
                if (functionInput.getText().length() > 0 ||
                        lowerLimitInput.getText().length() > 0 ||
                        upperLimitInput.getText().length() > 0 ||
                        subintervalInput.getText().length() > 0) {
                    resetButton.setVisibility(View.VISIBLE);
                } else {
                    resetButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };
    }

    private void performCalculation() {
        try {
            // Get user input
            String function = functionInput.getText().toString();
            double lowerLimit = Double.parseDouble(lowerLimitInput.getText().toString());
            double upperLimit = Double.parseDouble(upperLimitInput.getText().toString());
            int subintervals = Integer.parseInt(subintervalInput.getText().toString());

            // Calculate true values using Midpoint and Trapezoid Rules with high accuracy
            double trueValueMidpoint = calculateTrueIntegralMidpoint(function, lowerLimit, upperLimit);
            double trueValueTrapezoid = calculateTrueIntegralTrapezoid(function, lowerLimit, upperLimit);

            // Perform calculations with user-defined subintervals
            double midpointArea = calculateMidpointRule(function, lowerLimit, upperLimit, subintervals);
            double trapezoidArea = calculateTrapezoidRule(function, lowerLimit, upperLimit, subintervals);

            // Calculate true errors
            double midpointError = Math.abs(trueValueMidpoint - midpointArea);
            double trapezoidError = Math.abs(trueValueTrapezoid - trapezoidArea);

            // Determine which method is better
            String betterMethod;
            if (midpointError < trapezoidError) {
                betterMethod = "Midpoint Rule provides a better estimation.";
            } else {
                betterMethod = "Trapezoid Rule provides a better estimation.";
            }

            // Display results
            String result = "True Value (Midpoint Rule): " + trueValueMidpoint + "\n" +
                    "True Value (Trapezoid Rule): " + trueValueTrapezoid + "\n\n" +
                    "Midpoint Rule Area: " + midpointArea + "\n" +
                    "Trapezoid Rule Area: " + trapezoidArea + "\n\n" +
                    "Midpoint True Error: " + midpointError + "\n" +
                    "Trapezoid True Error: " + trapezoidError + "\n\n" +
                    betterMethod;

            resultOutput.setText(result);
            resultOutput.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            resultOutput.setText("Error in input or calculation: " + e.getMessage());
        }
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

    // High-accuracy true integral using the Midpoint Rule
    private double calculateTrueIntegralMidpoint(String function, double lower, double upper) {
        int steps = 1000; // High number of steps for better accuracy
        double width = (upper - lower) / steps;
        double integral = 0.0;

        for (int i = 0; i < steps; i++) {
            double midpoint = lower + (i + 0.5) * width;
            integral += evaluateFunction(function, midpoint);
        }

        return integral * width;
    }

    // High-accuracy true integral using the Trapezoid Rule
    private double calculateTrueIntegralTrapezoid(String function, double lower, double upper) {
        int steps = 1000; // High number of steps for better accuracy
        double width = (upper - lower) / steps;
        double integral = 0.0;

        for (int i = 0; i < steps; i++) {
            double x1 = lower + i * width;
            double x2 = lower + (i + 1) * width;
            integral += 0.5 * (evaluateFunction(function, x1) + evaluateFunction(function, x2)) * width;
        }

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