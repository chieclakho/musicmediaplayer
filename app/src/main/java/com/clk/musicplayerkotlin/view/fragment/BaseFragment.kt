package com.clk.musicplayerkotlin.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.clk.musicplayerkotlin.OnMainCallBack
import com.clk.musicplayerkotlin.view.viewmodel.BaseViewModel

abstract class BaseFragment<V : ViewBinding, M : BaseViewModel> : Fragment(), OnClickListener {
    protected lateinit var mContext: Context
    protected lateinit var binding: V
    protected lateinit var viewModel: M
    protected lateinit var callBack: OnMainCallBack
    protected var mData: Any? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initViewBinding(inflater, container)
        viewModel = ViewModelProvider(this)[getClassViewModel()]
        initViews()
        return binding.root
    }

     final override fun onClick(v: View) {
        v.startAnimation( AnimationUtils.loadAnimation( mContext, androidx.appcompat.R.anim.abc_fade_in))
        clickView(v)
    }

    fun setCallback(callBack: OnMainCallBack) {
        this.callBack = callBack
    }

    fun setData(data: Any?) {
        this.mData = data
    }

  open  fun clickView(v: View) {
        // do nothing
    }

    abstract fun initViews()

    abstract fun getClassViewModel(): Class<M>

    abstract fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?): V
    open fun notify(msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    open fun notify(id: Int) {
        Toast.makeText(context, id.toString() + "", Toast.LENGTH_SHORT).show()
    }
}