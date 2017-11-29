package com.nalsil.tensorflowsimapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class CustomLinkMovementMethod extends LinkMovementMethod {

    private static Context movementContext;

    private static CustomLinkMovementMethod linkMovementMethod = new CustomLinkMovementMethod();

    public boolean onTouchEvent(android.widget.TextView widget, android.text.Spannable buffer, android.view.MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0) {
                String url = link[0].getURL();
                if (url.startsWith("http")) {
                    //Log.d("Link", url);
                    //Toast.makeText(movementContext, "Link was clicked", Toast.LENGTH_LONG).show();

                    Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    movementContext.startActivity(browser);
                } else if (url.startsWith("fragment://")) {
                    String strFragment = url.replace("fragment://", "");
                    EventBusEvents.MessageEvent msgEvent = new EventBusEvents.MessageEvent();
                    msgEvent.strNavigationItem = strFragment;
                    EventBus.getDefault().post(msgEvent);
                    
                } else if (url.startsWith("tel")) {
                    Log.d("Link", url);
                    Toast.makeText(movementContext, "Tel was clicked", Toast.LENGTH_LONG).show();
                } else if (url.startsWith("mailto")) {
                    Log.d("Link", url);
                    Toast.makeText(movementContext, "Mail link was clicked", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public static android.text.method.MovementMethod getInstance(Context c) {
        movementContext = c;
        return linkMovementMethod;
    }
}