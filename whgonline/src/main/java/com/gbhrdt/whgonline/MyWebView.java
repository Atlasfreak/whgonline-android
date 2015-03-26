package com.gbhrdt.whgonline;

import android.content.Context;
import android.view.MotionEvent;
import android.webkit.WebView;

public class MyWebView extends WebView
{
    public MyWebView(Context context)
    {
        super(context);
    }

    // Note this!
    @Override
    public boolean onCheckIsTextEditor()
    {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                if (!hasFocus())
                    requestFocus();
                break;
        }

        return super.onTouchEvent(ev);
    }
}