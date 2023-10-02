package it.ipzs.androidpidproviderdemo.cie

import android.view.ViewTreeObserver
import it.ipzs.androidpidproviderdemo.databinding.CardNfcReadBinding

internal class CIEAccessHelper(private val binding: CardNfcReadBinding) {

    private var translationYCieScan = 0F

    fun attachToView() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                translationYCieScan = binding.root.measuredHeight.toFloat() +
                        20
                showCieScanView()
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun dismissCieScanView(){
        binding.root.animate().translationY(0F)
    }

    fun showCieScanView() {
        binding.root.animate().translationY(-translationYCieScan)
    }
}