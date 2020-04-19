package com.example.flutteranimatedsceneform

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_ar_animated.*

class ArAnimatedActivity : AppCompatActivity() {

    private var modelAnimator: ModelAnimator? = null

    private var i = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_animated)

        val arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            createModel(hitResult.createAnchor(), arFragment)
        }
    }

    private fun createModel(anchor: Anchor, arFragment: ArFragment) {

        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("skeletal.sfb"))
                .build()
                .thenAccept { t: ModelRenderable ->
                    val anchorNode = AnchorNode(anchor)
                    val skeletonNode = SkeletonNode()
                    skeletonNode.setParent(anchorNode)
                    skeletonNode.renderable = t

                    arFragment.arSceneView.scene.addChild(anchorNode)

                    button.setOnClickListener {
                        animateModel(t)
                    }
                }
    }

    private fun animateModel(modelRenderable: ModelRenderable) {
        if(modelAnimator != null && modelAnimator!!.isRunning) {
            modelAnimator!!.end()
        }
        val animationCount = modelRenderable.animationDataCount

        if(i == animationCount) {
            i = 0
        }

        val animationData = modelRenderable.getAnimationData(i)
        modelAnimator = ModelAnimator(animationData, modelRenderable)
        modelAnimator!!.start()
        i++
    }
}