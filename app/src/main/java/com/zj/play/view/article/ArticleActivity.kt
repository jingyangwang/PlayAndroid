package com.zj.play.view.article

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.zj.core.util.showToast
import com.zj.core.view.BaseActivity
import com.zj.play.R
import kotlinx.android.synthetic.main.activity_article.*


const val PAGE_NAME = "PAGE_NAME"
const val PAGE_URL = "PAGE_URL"
const val PAGE_ID = "PAGE_ID"
const val IS_COLLECTION = "IS_COLLECTION"

class ArticleActivity : BaseActivity(), View.OnClickListener {

    override fun getLayoutId(): Int {
        return R.layout.activity_article
    }

    private var pageName = ""
    private var pageUrl = ""
    private var pageId = -1
    private var isCollection = -1
    private lateinit var bottomDialogIvCollect: ImageView
    private lateinit var bottomDialogTvCollect: TextView
    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun initData() {
        pageName = intent.getStringExtra(PAGE_NAME) ?: ""
        pageUrl = intent.getStringExtra(PAGE_URL) ?: ""
        pageId = intent.getIntExtra(PAGE_ID, -1)
        isCollection = intent.getIntExtra(IS_COLLECTION, -1)
        articleTxtTitle.text = Html.fromHtml(pageName)
        articleWebView.loadUrl(pageUrl)
    }

    override fun initView() {
        window.setFormat(PixelFormat.TRANSLUCENT)
        articleImgBack.setOnClickListener(this)
        articleImgRight.setOnClickListener(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && articleWebView.canGoBack()) {
            articleWebView.goBack() //返回上个页面
            return true
        }
        return super.onKeyDown(keyCode, event) //退出H5界面
    }

    private fun setBottomDialog() {
        bottomSheetDialog = BottomSheetDialog(this)
        val behavior: BottomSheetBehavior<*> = bottomSheetDialog!!.behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView: View = bottomSheetDialog?.layoutInflater!!.inflate(
            R.layout.dialog_bottom_sheet,
            null
        )
        val bottomDialogLlCollect =
            dialogView.findViewById<LinearLayout>(R.id.bottomDialogLlCollect)
        bottomDialogIvCollect = dialogView.findViewById(R.id.bottomDialogIvCollect)
        bottomDialogTvCollect = dialogView.findViewById(R.id.bottomDialogTvCollect)

        val bottomDialogLlCopy = dialogView.findViewById<LinearLayout>(R.id.bottomDialogLlCopy)
        val bottomDialogLlBrowser =
            dialogView.findViewById<LinearLayout>(R.id.bottomDialogLlBrowser)
        val bottomDialogLlShare = dialogView.findViewById<LinearLayout>(R.id.bottomDialogLlShare)
        val bottomDialogLlReload = dialogView.findViewById<LinearLayout>(R.id.bottomDialogLlReload)
        if (isCollection == 1) {
            bottomDialogIvCollect.setImageResource(R.drawable.ic_favorite_black_24dp)
            bottomDialogTvCollect.text = "取消收藏"
        } else {
            bottomDialogIvCollect.setImageResource(R.drawable.ic_favorite_border_black_24dp)
            bottomDialogTvCollect.text = "收藏"
        }
        bottomDialogLlCollect.setOnClickListener(this)
        bottomDialogLlCopy.setOnClickListener(this)
        bottomDialogLlBrowser.setOnClickListener(this)
        bottomDialogLlShare.setOnClickListener(this)
        bottomDialogLlReload.setOnClickListener(this)
        bottomSheetDialog!!.setContentView(dialogView) //给布局设置透明背景色
        ((dialogView.parent) as View).setBackgroundColor(Color.TRANSPARENT)
        bottomSheetDialog!!.show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.articleImgBack -> {
                if (articleWebView.canGoBack()) {
                    //返回上个页面
                    articleWebView.goBack()
                    return
                } else {
                    finish()
                }
            }
            R.id.articleImgRight -> {
                setBottomDialog()
            }
            R.id.bottomDialogLlCollect -> {
                bottomSheetDialog?.dismiss()
                if (isCollection == -1 || pageId == -1) {
                    showToast("当前页面不可收藏")
                    return
                }
                ArticleUtils.setCollect(isCollection == 1, pageId,this)
                if (isCollection != 1) {
                    isCollection = 1;
                    bottomDialogIvCollect.setImageResource(R.drawable.ic_favorite_black_24dp)
                    bottomDialogTvCollect.text = "取消收藏"
                } else {
                    isCollection = 0;
                    bottomDialogIvCollect.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                    bottomDialogTvCollect.text = "收藏"
                }
            }
            R.id.bottomDialogLlCopy -> {
                bottomSheetDialog?.dismiss()
                ArticleUtils.copyToClipboard(this, pageUrl)
                showToast("复制成功，可直接进行粘贴")
            }
            R.id.bottomDialogLlBrowser -> {
                bottomSheetDialog?.dismiss()
                ArticleUtils.jumpBrowser(this, pageUrl)
            }
            R.id.bottomDialogLlShare -> {
                bottomSheetDialog?.dismiss()
                ArticleUtils.shareUrl(this, pageUrl, pageName)
            }
            R.id.bottomDialogLlReload -> {
                bottomSheetDialog?.dismiss()
                articleWebView.reload()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomSheetDialog?.cancel()
    }

    companion object {
        fun actionStart(
            context: Context,
            pageName: String,
            pageUrl: String,
            pageId: Int = -1,
            isCollection: Int = -1
        ) {
            val intent = Intent(context, ArticleActivity::class.java).apply {
                putExtra(PAGE_NAME, pageName)
                putExtra(PAGE_URL, pageUrl)
                putExtra(PAGE_ID, pageId)
                putExtra(IS_COLLECTION, isCollection)
            }
            context.startActivity(intent)
        }
    }

}
