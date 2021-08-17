import com.install4j.api.actions.AbstractInstallOrUninstallAction;
import com.install4j.api.context.*;

/**
 * A sample action that can be also used for the uninstaller.
 *
 * The class SampleActionBeanInfo in the same package defines how the bean
 * is handled in the install4j IDE.
 */
public class SampleAction extends AbstractInstallOrUninstallAction {

    public static final int DETAIL_NONE = 1;
    public static final int DETAIL_PERCENT = 2;
    public static final int DETAIL_COUNTER = 3;

    private boolean fail = false;
    private String message = "Doing sample stuff";
    private int detail = DETAIL_NONE;

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public String getMessage() {
        // call replaceVariables to resolve installer variables
        return replaceVariables(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getDetail() {
        return detail;
    }

    public void setDetail(int detail) {
        this.detail = detail;
    }

    private boolean execute(Context context) {
        ProgressInterface progressInterface = context.getProgressInterface();
        progressInterface.setStatusMessage(getMessage());
        for (int i=0; i<=100; i++) {
            progressInterface.setPercentCompleted(i);
            switch (detail) {
                case DETAIL_PERCENT:
                    progressInterface.setDetailMessage("" + i + " % completed");
                    break;
                case DETAIL_COUNTER:
                    progressInterface.setDetailMessage("Counted up to " + i);
                    break;
            }

            // do nothing
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        progressInterface.setStatusMessage("");
        return !isFail();
    }

    @Override
    public boolean install(InstallerContext context) throws UserCanceledException {
        return execute(context);
    }

    @Override
    public boolean uninstall(UninstallerContext context) throws UserCanceledException {
        return execute(context);
    }

    @Override
    public void rollback(InstallerContext context) {
        ProgressInterface progressInterface = context.getProgressInterface();
        progressInterface.setStatusMessage("Rolling back sample stuff");
        for (int i=0; i<=100; i++) {
            progressInterface.setPercentCompleted(i);

            // do nothing
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        progressInterface.setStatusMessage("");
    }

}
