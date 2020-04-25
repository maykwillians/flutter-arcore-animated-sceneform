package com.example.flutteranimatedsceneform

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.RawRes
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_ar_animated.*
import com.google.ar.core.AugmentedImageDatabase

class ArAnimatedActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private lateinit var arFragment: ArFragment
    private var modelAnimator: ModelAnimator? = null
    private var anchor1: Anchor? = null
    private var anchor2: Anchor? = null
    private var anchorNode: AnchorNode? = null
    private var initialVector: Vector3? = null
    private var xInitialPos = 0F
    private var yInitialPos = 0F
    private var updatedVector: Vector3? = null
    private var xUpdatedPos = 0F
    private var yUpdatedPos = 0F
    private var mediaPlayer: MediaPlayer? = null
    private var inArea = false
    private var iniciou = false
    private var mainHandler: Handler? = null
    private lateinit var augmentedImageDatabase: AugmentedImageDatabase

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
        val inputStream = this.assets.open("myimages.imgdb")
        augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, inputStream)
        config.augmentedImageDatabase = augmentedImageDatabase
    }

    // Renderizando o modelo 3D na tela
    private fun createModel(anchor: Anchor, modelPath: String, animationLoopTimer: Long, @RawRes sound: Int = 0) {
        ModelRenderable
                .builder()
                .setSource(this, Uri.parse(modelPath))
                .build()
                .thenAccept {modelRenderable: ModelRenderable ->

                    anchorNode = AnchorNode(anchor)

                    initialVector = anchorNode!!.worldPosition
                    xInitialPos = arFragment.arSceneView.scene.camera.worldToScreenPoint(initialVector).x
                    yInitialPos = arFragment.arSceneView.scene.camera.worldToScreenPoint(initialVector).y

                    anchorNode!!.renderable = modelRenderable
                    arFragment.arSceneView.scene.addChild(anchorNode)

                    setupSound(sound)
                    animateModel(modelRenderable, animationLoopTimer)

                    iniciou = true
                }
    }

    private fun animateModel(modelRenderable: ModelRenderable, animationLoopTimer: Long) {
        val animationData = modelRenderable.getAnimationData(0)
        modelAnimator = ModelAnimator(animationData, modelRenderable)
        resumeAnimation(animationLoopTimer)
    }

    private fun resumeAnimation(animationLoopTimer: Long) {
        mainHandler = Handler(Looper.getMainLooper())
        mainHandler!!.post(object : Runnable {
            override fun run() {
                if(!modelAnimator!!.isRunning) {
                    modelAnimator!!.start()
                }
                playSound()
                mainHandler!!.postDelayed(this, animationLoopTimer)
            }
        })
    }

    private fun stopAnimation() {
        if(modelAnimator!!.isRunning) {
            modelAnimator!!.end()
        }
        pauseSound()
        mainHandler!!.removeMessages(0)
    }

    private fun setupSound(@RawRes sound: Int) {
        if (sound != 0) {
            mediaPlayer = MediaPlayer.create(this, sound)
        }
    }

    private fun playSound() {
        if(mediaPlayer != null) {
            mediaPlayer!!.start()
        }
    }

    private fun pauseSound() {
        if(mediaPlayer != null) {
            mediaPlayer!!.pause()
        }
    }

    private fun stopSound() {
        if(mediaPlayer != null) {
            mediaPlayer!!.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(modelAnimator != null && modelAnimator!!.isRunning) {
            modelAnimator!!.end()
        }
        if(mediaPlayer != null && mediaPlayer!!.isPlaying) {
            stopSound()
        }
        if(mainHandler != null) {
            mainHandler!!.removeMessages(0)
        }
    }

    private fun detectLostCameraArea() {
        if(iniciou) {
            // Esses valores vai depender do tamanho das imagens e do objeto 3d (posso parametrizar eles)
            if(xUpdatedPos >= xInitialPos + 450F || xUpdatedPos <= xInitialPos - 600F || yUpdatedPos >= yInitialPos + 850F || yUpdatedPos <= yInitialPos - 600F) {
                if (inArea) {
                    view.visibility = VISIBLE
                    stopAnimation()
                    inArea = false
                }
            } else {
                if (!inArea) {
                    view.visibility = GONE
                    resumeAnimation(2500L)
                    inArea = true
                }
            }
        }
    }

    override fun onUpdate(p0: FrameTime?) {
        val frame = arFragment.arSceneView.arFrame
        val images: Collection<AugmentedImage> = frame!!.getUpdatedTrackables(AugmentedImage::class.java)

        if(anchorNode != null) {
            updatedVector = anchorNode!!.worldPosition
            xUpdatedPos = arFragment.arSceneView.scene.camera.worldToScreenPoint(updatedVector).x
            yUpdatedPos = arFragment.arSceneView.scene.camera.worldToScreenPoint(updatedVector).y
        }

        detectLostCameraArea()

        for(image in images) {
            if(image.trackingState == TrackingState.TRACKING) {
                if((image.name == "1" ||
                                image.name == "3" ||
                                image.name == "5" ||
                                image.name == "7" ||
                                image.name == "9") && anchor1 == null) {
                    anchor1 = image.createAnchor(image.centerPose)
                    createModel(anchor1!!, "spider_3.sfb", 2500L, R.raw.spider)
                }
                if((image.name == "2" ||
                                image.name == "4" ||
                                image.name == "6" ||
                                image.name == "8" ||
                                image.name == "10") && anchor2 == null) {
                    anchor2 = image.createAnchor(image.centerPose)
                    createModel(anchor2!!, "skeletal.sfb", 7000L)
                }
            }
        }
    }
}