package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.os.Bundle

class AssistantActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // load fragment for this view
        setMainContent(AssistantFragment())

    }
}