package euphoria.psycho.common;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
public class Dialogs {
    public static Dialog showDialog(Context context, String hint, final Listener listener) {
        final EditText editText = new EditText(context);
        editText.setText(hint);
        AlertDialog.Builder builder = new Builder(context);
        builder.setView(editText);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) listener.onPositiveClicked(editText.getText());
                dialog.dismiss();
            }
        }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.show();
    }
    public interface Listener {
        void onPositiveClicked(CharSequence text);
    }
}