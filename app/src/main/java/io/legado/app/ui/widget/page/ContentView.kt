package io.legado.app.ui.widget.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import io.legado.app.R
import io.legado.app.constant.AppConst.TIME_FORMAT
import io.legado.app.help.ImageLoader
import io.legado.app.help.ReadBookConfig
import io.legado.app.utils.*
import kotlinx.android.synthetic.main.view_book_page.view.content_text_view
import kotlinx.android.synthetic.main.view_book_page.view.page_panel
import kotlinx.android.synthetic.main.view_book_page.view.top_bar
import kotlinx.android.synthetic.main.view_book_page.view.tv_bottom_left
import kotlinx.android.synthetic.main.view_book_page.view.tv_bottom_right
import kotlinx.android.synthetic.main.view_book_page.view.tv_top_left
import kotlinx.android.synthetic.main.view_book_page.view.tv_top_right
import kotlinx.android.synthetic.main.view_book_page_scroll.view.*
import org.jetbrains.anko.matchParent
import java.util.*


class ContentView : FrameLayout {
    var callBack: CallBack? = null
    private var isScroll: Boolean = false
    private var pageSize: Int = 0
    private val bgImage: AppCompatImageView = AppCompatImageView(context)
        .apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

    constructor(context: Context) : super(context) {
        this.isScroll = true
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {
        //设置背景防止切换背景时文字重叠
        setBackgroundColor(context.getCompatColor(R.color.background))
        addView(bgImage, LayoutParams(matchParent, matchParent))
        if (isScroll) {
            inflate(context, R.layout.view_book_page_scroll, this)
        } else {
            inflate(context, R.layout.view_book_page, this)
        }
        top_bar.layoutParams.height = context.getStatusBarHeight()
        upStyle()
        upTime()
        content_text_view.customSelectionActionModeCallback =
            ContentSelectActionCallback(content_text_view)
        page_scroll_view?.scrollListener = object : PageScrollView.OnScrollListener {
            override fun onScroll(scrollY: Int) {
                content_text_view.layout?.getLineForVertical(scrollY)?.let { line ->
                    callBack?.scrollToLine(line)
                }
            }
        }
    }

    fun upStyle() {
        ReadBookConfig.getConfig().apply {
            val pt = if (context.getPrefBoolean("hideStatusBar", false)) {
                top_bar.visible()
                0
            } else {
                top_bar.gone()
                context.getStatusBarHeight()
            }
            page_panel.setPadding(paddingLeft.dp, pt, paddingRight.dp, 0)
            content_text_view.setPadding(0, paddingTop.dp, 0, paddingBottom.dp)
            content_text_view.textSize = textSize.toFloat()
            content_text_view.setLineSpacing(lineSpacingExtra.toFloat(), lineSpacingMultiplier)
            content_text_view.letterSpacing = letterSpacing
            content_text_view.paint.isFakeBoldText = textBold
            textColor().let {
                content_text_view.setTextColor(it)
                tv_top_left.setTextColor(it)
                tv_top_right.setTextColor(it)
                tv_bottom_left.setTextColor(it)
                tv_bottom_right.setTextColor(it)
            }
        }
        context.getPrefString("readBookFont")?.let {
            if (it.isNotEmpty()) {
                content_text_view.typeface = Typeface.createFromFile(it)
            } else {
                content_text_view.typeface = Typeface.DEFAULT
            }
        }
    }

    fun setBg(bg: Drawable?) {
        //all supported
        ImageLoader.load(context, bg)
            .centerCrop()
            .setAsDrawable(bgImage)
    }

    fun upTime() {
        tv_top_left.text = TIME_FORMAT.format(Date(System.currentTimeMillis()))
    }

    fun upBattery(battery: Int) {
        tv_top_right.text = context.getString(R.string.battery_show, battery)
    }

    fun setContent(textPage: TextPage?) {
        content_text_view.text = textPage?.text
        tv_bottom_left.text = textPage?.title
        pageSize = textPage?.pageSize ?: 0
        setPageIndex(textPage?.index)
    }

    @SuppressLint("SetTextI18n")
    fun setPageIndex(pageIndex: Int?) {
        pageIndex?.let {
            tv_bottom_right.text = "${pageIndex.plus(1)}/${pageSize}"
        }
    }

    fun isTextSelected(): Boolean {
        return content_text_view.selectionEnd - content_text_view.selectionStart != 0
    }

    fun contentTextView(): ContentTextView? {
        return content_text_view
    }

    fun scrollTo(pos: Int?) {
        if (pos != null) {
            page_scroll_view?.post {
                page_scroll_view?.scrollTo(0, content_text_view.layout.getLineTop(pos))
            }
        }
    }

    interface CallBack {
        fun scrollToLine(line: Int)
    }
}