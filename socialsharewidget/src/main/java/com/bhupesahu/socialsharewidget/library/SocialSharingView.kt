package com.bhupesahu.socialsharewidget.library

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.bhupesahu.socialsharewidget.library.utils.CommonUtil.OnIntentPreparedCallback
import com.bhupesahu.socialsharewidget.library.utils.CommonUtil.getShareIntent
import com.bhupesahu.socialsharewidget.library.utils.CommonUtil.showToast
import java.util.*

class SocialSharingView : LinearLayout, View.OnClickListener {
    private val packages = arrayOf(
        "com.facebook.katana",
        "com.facebook.lite",
        "com.twitter.android",
        "org.telegram.messenger",
        "com.whatsapp"
    )
    private var title: String? = null
    private var description: String? = null
    private var imgUrl: String? = null
    private var platformMap: LinkedHashMap<Int, SharePlatform>? = null
    override fun onClick(view: View) {
        getShareIntent(
            context, title!!, description, imgUrl,
            object : OnIntentPreparedCallback {
                override fun onIntentPrepared(intent: Intent) {
                    when (view.tag as SharePlatform) {
                        SharePlatform.PLATFORM_FACEBOOK -> intent.setPackage(
                            packages[0]
                        )
                        SharePlatform.PLATFORM_TWITTER -> intent.setPackage(packages[2])
                        SharePlatform.PLATFORM_TELEGRAM -> intent.setPackage(packages[3])
                        SharePlatform.PLATFORM_WHATSAPP -> intent.setPackage(packages[4])
                    }
                    try {
                        context.startActivity(intent)
                    } catch (anfex: ActivityNotFoundException) {
                        if (packages[0].equals(intent.getPackage(), ignoreCase = true)) {
                            intent.setPackage(packages[1])
                            try {
                                context.startActivity(intent)
                            } catch (anfex2: ActivityNotFoundException) {
                                openPlayStoreLink(packages[0])
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                                showToast(context, resources.getString(R.string.error_no_app_found))
                            }
                        } else {
                            openPlayStoreLink(intent.getPackage())
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        showToast(context, resources.getString(R.string.error_no_app_found))
                    }
                }
            })
    }

    private fun openPlayStoreLink(packageName: String?) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        "market://details?id="
                                + packageName
                    )
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    internal enum class SharePlatform {
        PLATFORM_MORE, PLATFORM_FACEBOOK, PLATFORM_TWITTER, PLATFORM_TELEGRAM, PLATFORM_WHATSAPP
    }

    constructor(context: Context?) : super(context) {
        initUI(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initUI(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initUI(attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initUI(attrs)
    }

    private fun initUI(attrs: AttributeSet?) {
        parseAttributes(attrs)
        inflateViews()
        bindViews()
    }

    private fun parseAttributes(attrs: AttributeSet?) {
//        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.EmptyView);
//        try {
//            attrDrawable = attributes.getDrawable(R.styleable.EmptyView_image);
//            attrText = attributes.getString(R.styleable.EmptyView_text);
//            attrPrimaryActionText = attributes.getString(R.styleable.EmptyView_actionLabelPrimary);
//            attrContentId = attributes.getResourceId(R.styleable.EmptyView_contentLayout, View.NO_ID);
//            attrShowPrimaryAction = attributes.getBoolean(R.styleable.EmptyView_showPrimaryAction, true);
//        } finally {
//            attributes.recycle();
//        }
    }

    private fun inflateViews() {}
    private fun bindViews() {
        val iconHeightWidth = resources.getDimension(R.dimen._22sdp).toInt()
        val iconSpacing = resources.getDimension(R.dimen._8sdp).toInt()
        val params = LayoutParams(iconHeightWidth, iconHeightWidth)
        if (orientation == HORIZONTAL) params.setMargins(
            0,
            0,
            iconSpacing,
            0
        ) else params.setMargins(0, 0, 0, iconSpacing)
        for ((key, value) in platformMap!!) {
            val imgActivityIcon = ImageView(context)
            imgActivityIcon.layoutParams = params
            imgActivityIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, key,
                    null
                )
            )
            imgActivityIcon.tag = value
            imgActivityIcon.setOnClickListener(this)
            addView(imgActivityIcon)
        }
    }

    fun setShareContent(
        title: String?,
        description: String?,
        imgUrl: String?
    ) {
        this.title = title
        this.description = description
        this.imgUrl = imgUrl
    }

    init {
        platformMap = LinkedHashMap()
        platformMap?.let { it[R.drawable.ic_share_more] = SharePlatform.PLATFORM_MORE }
        platformMap?.let { it[R.drawable.ic_share_fb] = SharePlatform.PLATFORM_FACEBOOK }
        platformMap?.let { it[R.drawable.ic_share_twitter] = SharePlatform.PLATFORM_TWITTER }
        platformMap?.let { it[R.drawable.ic_share_telegram] = SharePlatform.PLATFORM_TELEGRAM }
        platformMap?.let { it[R.drawable.ic_share_whatsapp] = SharePlatform.PLATFORM_WHATSAPP }
    }
}