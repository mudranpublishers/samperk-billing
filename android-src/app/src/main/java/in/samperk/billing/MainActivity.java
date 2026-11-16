package in.samperk.billing;

import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;

/**
 * Samperk Billing — Capacitor host activity.
 *
 * Android's WebView has no window.print(), so the page calls
 * AndroidPrint.print() (see doPrint() in the app) and this class hands the
 * rendered page to Android's PrintManager. Every printer the device can
 * reach shows up there: Wi-Fi / Mopria printers, USB-OTG printers, any
 * Bluetooth thermal printer whose app registers a print service, and
 * "Save as PDF".
 */
public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new PrintBridge(webView), "AndroidPrint");
    }

    public class PrintBridge {
        private final WebView webView;

        PrintBridge(WebView webView) {
            this.webView = webView;
        }

        @JavascriptInterface
        public void print() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
                    PrintManager printManager =
                            (PrintManager) getSystemService(PRINT_SERVICE);
                    if (printManager == null) return;
                    String jobName = "Samperk Billing document";
                    PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter(jobName);
                    PrintAttributes attrs = new PrintAttributes.Builder()
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build();
                    printManager.print(jobName, adapter, attrs);
                }
            });
        }
    }
}
