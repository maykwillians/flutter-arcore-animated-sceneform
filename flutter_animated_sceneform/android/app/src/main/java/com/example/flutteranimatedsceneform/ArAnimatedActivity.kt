package com.example.flutteranimatedsceneform

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_ar_animated.*

class ArAnimatedActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private lateinit var arFragment: ArFragment
    private var modelAnimator: ModelAnimator? = null
    private var anchor1: Anchor? = null
    private var anchor2: Anchor? = null

    private var i = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_animated)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.planeRenderer.isEnabled = false
        arFragment.planeDiscoveryController.setInstructionView(null)
        arFragment.arSceneView.scene.addOnUpdateListener(this)
    }

    fun setupDatabase(config: Config, session: Session) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.teia)
        val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.campo)
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("teia", bitmap, 6F)
        augmentedImageDatabase.addImage("campo", bitmap2, 2F)
        config.augmentedImageDatabase = augmentedImageDatabase
    }

    private fun createModel(anchor: Anchor, arFragment: ArFragment) {

        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("spider_3.sfb"))
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

        if(animationCount != 0) {
            if(i == animationCount) {
                i = 0
            }

            val animationData = modelRenderable.getAnimationData(i)
            modelAnimator = ModelAnimator(animationData, modelRenderable)
            modelAnimator!!.start()
            i++
        }
    }

    private fun placeModel(modelRenderable: ModelRenderable, anchor: Anchor) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.renderable = modelRenderable
        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    private fun createModel2(anchor: Anchor) {
        ModelRenderable.Builder()
                .setSource(this, Uri.parse("skeletal.sfb"))
                .build()
                .thenAccept { t: ModelRenderable -> placeModel(t, anchor ) }
    }

    override fun onUpdate(p0: FrameTime?) {
        val frame = arFragment.arSceneView.arFrame
        val images: Collection<AugmentedImage> = frame!!.getUpdatedTrackables(AugmentedImage::class.java)

        for(image in images) {
            if(image.trackingState == TrackingState.TRACKING) {
                if(image.name == "teia" && anchor1 == null) {
                    anchor1 = image.createAnchor(image.centerPose)
                    createModel(anchor1!!, arFragment)
                } else if(image.name == "campo" && anchor2 == null) {
                    anchor2 = image.createAnchor(image.centerPose)
                    createModel2(anchor2!!)
                }
            }
        }
    }
}