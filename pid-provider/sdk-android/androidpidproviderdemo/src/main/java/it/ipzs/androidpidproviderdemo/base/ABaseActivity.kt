package it.ipzs.androidpidproviderdemo.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class ABaseActivity<VB : ViewBinding> : AppCompatActivity(){

    protected val binding: VB by lazy { setBinding() }

    protected abstract fun setBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

}