package euphoria.psycho.todo;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;

import euphoria.psycho.common.Activities;
import euphoria.psycho.common.EditTexts;

public class CalculatorActivity extends Activities {
    private EditText mEditText;
    private TextView mTextView;

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_calculator);
        mEditText = findViewById(R.id.edit);
        mTextView = findViewById(R.id.summary);
        click(v -> {
            String add = null;
            switch (v.getId()) {
                case R.id.add:
                    add = "+";
                    break;
                case R.id.subtract:
                    add = "-";
                    break;
                case R.id.multiplicate:
                    add = "*";
                    break;
                case R.id.divide:
                    add = "/";
                    break;
            }
            EditTexts.paste(mEditText, add);
        }, R.id.add, R.id.subtract, R.id.subtract, R.id.multiplicate);

        click(R.id.evaluate, v -> {

            try {
                JexlEngine engine = new JexlBuilder().create();
                JexlExpression e = engine.createExpression(mEditText.getText().toString());
                Object o = e.evaluate(null);
                if (o instanceof Integer)
                    mTextView.setText(String.format("%s = %d\n", mEditText.getText().toString(), (Integer) o));
            } catch (JexlException t) {
                Toast.makeText(v.getContext(), t.getInfo().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected String[] needPermissions() {
        return new String[0];
    }

    @Override
    protected int requestCodePermissions() {
        return 0;
    }
}
