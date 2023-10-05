package com.clk.musicplayerkotlin.view.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.clk.musicplayerkotlin.OnMainCallBack
import com.clk.musicplayerkotlin.R
import com.clk.musicplayerkotlin.view.fragment.BaseFragment

abstract class BaseActivity<V : ViewBinding, M : ViewModel> : AppCompatActivity(), OnClickListener,
    OnMainCallBack {
    protected lateinit var binding: V
    protected lateinit var model: M
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initViewBinding()
        model = ViewModelProvider(this)[initViewModel()]
        setContentView(binding.root)
        initViews()
    }

    abstract fun initViews()

    abstract fun initViewModel(): Class<M>


    abstract fun initViewBinding(): V

    override fun onClick(v: View) {
        v.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_in))
        clickView(v)
    }

    protected open fun clickView(v: View) {
        // do nothing
    }

    protected open fun notify(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    protected open fun notify(id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }

    override fun showFragment(tag: String, data: Any?, isBacked: Boolean) {
        try {
            val clazz = Class.forName(tag)
            val constructor = clazz.getConstructor()
            val fragment: BaseFragment<*, *> = constructor.newInstance() as BaseFragment<*, *>
            fragment.setCallback(this)
            fragment.setData(data)
            val trans = supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            ).replace(R.id.ln_main, fragment, tag)
            if (isBacked) {
                trans.addToBackStack(null)
            }
            trans.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun backToPrevious() {
        onBackPressed()
    }
}

