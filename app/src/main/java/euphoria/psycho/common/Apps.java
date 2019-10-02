package euphoria.psycho.common;

import android.app.Application;
import android.os.StrictMode;

public class Apps extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Contexts.setContext(this);
        //Crashs.getInstance().init(this, "发生了错误，详情请查看日记。", "");
    }
}
