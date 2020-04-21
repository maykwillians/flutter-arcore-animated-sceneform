package com.example.flutteranimatedsceneform

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

class ArAnimatedActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private lateinit var arFragment: ArFragment
    private var modelAnimator: ModelAnimator? = null
    private var anchor1: Anchor? = null
    private var anchor2: Anchor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_animated)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        // Para ocultar o efeito default de scanner do Arcore no display
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)

        // Para ignorar a renderização do plano
        arFragment.arSceneView.planeRenderer.isEnabled = false

        arFragment.arSceneView.scene.addOnUpdateListener(this)
    }

    // Criando a base de dados das imagens aumentadas (bitmaps: PNG, JPG)
    fun setupDatabase(config: Config, session: Session) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.teia)
        val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.campo)
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("teia", bitmap, 6F)
        augmentedImageDatabase.addImage("campo", bitmap2, 2F)
        config.augmentedImageDatabase = augmentedImageDatabase
    }

    // Renderizando o modelo 3D na tela
    private fun createModel(anchor: Anchor, modelPath: String, animationLoopTimer: Long) {
        ModelRenderable
                .builder()
                .setSource(this, Uri.parse(modelPath))
                .build()
                .thenAccept {modelRenderable: ModelRenderable ->
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.renderable = modelRenderable
                    arFragment.arSceneView.scene.addChild(anchorNode)

                    animateModel(modelRenderable, animationLoopTimer)
                }
    }

    private fun animateModel(modelRenderable: ModelRenderable, animationLoopTimer: Long) {

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {

                if(modelAnimator != null && modelAnimator!!.isRunning) {
                    modelAnimator!!.end()
                }
                
                val animationData = modelRenderable.getAnimationData(0)
                modelAnimator = ModelAnimator(animationData, modelRenderable)
                modelAnimator!!.start()

                mainHandler.postDelayed(this, animationLoopTimer)
            }
        })
    }

    override fun onUpdate(p0: FrameTime?) {
        val frame = arFragment.arSceneView.arFrame
        val images: Collection<AugmentedImage> = frame!!.getUpdatedTrackables(AugmentedImage::class.java)

        for(image in images) {
            if(image.trackingState == TrackingState.TRACKING) {
                if(image.name == "teia" && anchor1 == null) {
                    anchor1 = image.createAnchor(image.centerPose)
                    createModel(anchor1!!, "spider_3.sfb", 2500L)
                } else if(image.name == "campo" && anchor2 == null) {
                    anchor2 = image.createAnchor(image.centerPose)
                    createModel(anchor2!!, "skeletal.sfb", 7000L)
                }
            }
        }
    }
}