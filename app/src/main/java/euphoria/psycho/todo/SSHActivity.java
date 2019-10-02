package euphoria.psycho.todo;

import android.Manifest;
import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3FileHandle;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import euphoria.psycho.common.Activities;
import euphoria.psycho.common.Clipboards;
import euphoria.psycho.common.Contexts;
import euphoria.psycho.common.EditTexts;
import euphoria.psycho.common.Interfaces;
import euphoria.psycho.common.Logs;
import euphoria.psycho.common.Strings;
import euphoria.psycho.common.Threads;

public class SSHActivity extends Activities {
    private static final int BUFFERSIZE = 1024 * 8;
    private String mUserName;
    private String mServer;
    private String mPassword;
    private EditText mEditText;
    private TextView mTextView;

    private void actionAddClipboard() {
        CharSequence s = Contexts.getText();
        if (!Strings.isNullOrWhiteSpace(s))
            Clipboards.getInstance().insert(s.toString());
    }

    private void actionClipboard() {

        Clipboards.getInstance().
                show(findViewById(R.id.container), (parent, view, position, id) -> EditTexts.paste(mEditText, Clipboards.getInstance().fetchStrings().get(position)));
    }

    private void executeCommand(String command) {

        Threads.postOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                executeCommand(mServer, mUserName, mPassword, command, new Interfaces.Listener<String>() {
                    @Override
                    public void onFailure(String reson) {
                        Threads.postOnUiThread(() -> Contexts.toast(reson));
                    }

                    @Override
                    public void onSuccess(String s) {
                        Threads.postOnUiThread(() -> mTextView.setText(s));
                    }
                });
            }
        });
    }

    private void loadUserInfos() {
        mUserName = getPreferences().getString(SettingsFragment.KEY_SSH_USERNAME, null);
        mServer = getPreferences().getString(SettingsFragment.KEY_SSH_SERVER_ADDRESS, null);
        mPassword = getPreferences().getString(SettingsFragment.KEY_SSH_PASSWORD, null);

    }

    private static void executeCommand(String hostname,
                                       String username,
                                       String password,
                                       String command, Interfaces.Listener<String> listener) {

        try {
            /* Create a connection instance */

            Connection conn = new Connection(hostname);

            /* Now connect */

            conn.connect();

            /* Authenticate.
             * If you get an IOException saying something like
             * "Authentication method password not supported by the server at this stage."
             * then please check the FAQ.
             */

            boolean isAuthenticated = conn.authenticateWithPassword(username, password);

            if (!isAuthenticated && listener != null) {
                listener.onFailure("Authentication failed.");
                return;
            }

            /* Create a session */

            Session sess = conn.openSession();

            sess.execCommand(command);


            /*
             * This basic example does not handle stderr, which is sometimes dangerous
             * (please read the FAQ).
             */

            InputStream stdout = new StreamGobbler(sess.getStdout());

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                stringBuilder.append(line).append('\n');
            }

            /* Show exit status, if available (otherwise "null") */

            System.out.println("ExitCode: " + sess.getExitStatus());

            /* Close this session */

            sess.close();

            /* Close the connection */

            conn.close();

            if (listener != null) {
                listener.onSuccess(stringBuilder.toString());
            }

        } catch (IOException e) {
            if (listener != null) listener.onSuccess(e.toString());
        }
    }

    public static void uploadDatabase(Context context, File file, String targetPath, Interfaces.Listener<String> listener) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = preferences.getString(SettingsFragment.KEY_SSH_USERNAME, null);
        String hostname = preferences.getString(SettingsFragment.KEY_SSH_SERVER_ADDRESS, null);
        String password = preferences.getString(SettingsFragment.KEY_SSH_PASSWORD, null);
        Connection conn = new Connection(hostname);

        try {
            conn.connect();


            boolean isAuthenticated = conn.authenticateWithPassword(username, password);

            if (!isAuthenticated && listener != null) {
                listener.onFailure("Authentication failed.");
                return;
            }

            SFTPv3Client client = new SFTPv3Client(conn);
            SFTPv3FileHandle sftPv3FileHandle = client.createFileTruncate(targetPath);
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[BUFFERSIZE];
            int n;
            int length = 0;
            while ((n = fis.read(b)) != -1) {
                client.write(sftPv3FileHandle, length, b, 0, n);
                length += n;
            }
            client.closeFile(sftPv3FileHandle);
            fis.close();
            if (listener != null) listener.onSuccess(targetPath);
        } catch (IOException e) {
            if (listener != null) listener.onFailure(e.toString());
        }
    }

    public static void downloadDatabase(Context context, File targetFile, String sourcePath, Interfaces.Listener<String> listener) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = preferences.getString(SettingsFragment.KEY_SSH_USERNAME, null);
        String hostname = preferences.getString(SettingsFragment.KEY_SSH_SERVER_ADDRESS, null);
        String password = preferences.getString(SettingsFragment.KEY_SSH_PASSWORD, null);
        Connection conn = new Connection(hostname);

        try {
            conn.connect();


            boolean isAuthenticated = conn.authenticateWithPassword(username, password);

            if (!isAuthenticated && listener != null) {
                listener.onFailure("Authentication failed.");
                return;
            }
            FileOutputStream out = new FileOutputStream(targetFile);
            SFTPv3Client client = new SFTPv3Client(conn);
            SFTPv3FileHandle sftPv3FileHandle = client.openFileRO(sourcePath);
            byte[] cache = new byte[BUFFERSIZE];
            int i = 0;
            int offset = 0;
            while ((i = client.read(sftPv3FileHandle, offset, cache, 0, 1024)) != -1) {
                out.write(cache, 0, i);
                offset += i;
            }
            if (listener != null) listener.onFailure(targetFile.getAbsolutePath());

        } catch (IOException e) {
            if (listener != null) listener.onFailure(e.toString());
        }
    }

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_ssh);
        mEditText = findViewById(R.id.edit);
        mTextView = findViewById(R.id.summary);

        loadUserInfos();
        Logs.d(mUserName, mServer, mPassword);
//        Intent settings = new Intent(this, SettingsActivity.class);
//        startActivity(settings);

        click(R.id.execute_command, v -> {
            if (!EditTexts.isWhitespace(mEditText))
                executeCommand(mEditText.getText().toString());
        });
    }

    @Override
    protected String[] needPermissions() {
        return new String[]{
                permission.INTERNET,
                permission.WRITE_EXTERNAL_STORAGE
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ssh, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clipboard:
                actionClipboard();
                return true;
            case R.id.action_add_clipboard:
                actionAddClipboard();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Clipboards.newInstance(this,
                new File(Environment.getExternalStorageDirectory(),
                        "database_ssh.db").getAbsolutePath());
    }

    @Override
    protected int requestCodePermissions() {
        return 0;
    }
}
