package euphoria.psycho.todo;

import android.app.Activity;

import euphoria.psycho.common.Activities;

public class CommonActivity extends Activities {

    @Override
    protected void initialize() {
        NativeUtils.youdaoDictionary("goo");
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
