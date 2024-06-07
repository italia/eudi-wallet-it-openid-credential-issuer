package it.ipzs.androidpidproviderdemo.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


internal abstract class ABaseFragment<T : ViewBinding>: Fragment() {

    val binding: T by lazy { setBinding() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    protected abstract fun setBinding(): T
}