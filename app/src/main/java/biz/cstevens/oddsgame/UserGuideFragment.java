package biz.cstevens.oddsgame;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class UserGuideFragment extends Fragment {
    private WebView webView;

    public static UserGuideFragment newInstance() {
        return new UserGuideFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_user_guide, container, false);

        webView = view.findViewById(R.id.web_view);
        webView.loadUrl("file:///android_asset/user_guide.html");

        return view;
    }
}
