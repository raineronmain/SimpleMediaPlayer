package me.example

import javafx.geometry.*
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.media.Media
import javafx.scene.media.MediaException
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import javafx.stage.Stage
import javafx.util.Duration
import tornadofx.*
import java.io.File
import kotlin.system.exitProcess

class MainView : View("MediaPlayer") {

	override val root : StackPane by fxml("/fxml/MainView.fxml")

	val mediaPane : StackPane  by fxid()
	val contrPane : AnchorPane by fxid()
	val mediaView : MediaView  by fxid()

	var mediaPlayer: MediaPlayer? = null

	val sliderSeek: Slider by fxid()

	init {

		primaryStage.setOnCloseRequest { cleanUp() }

		mediaView.isPreserveRatio = true

		primaryStage.widthProperty().addListener { _, _, width -> mediaView.fitWidth = width.toDouble()	}
		primaryStage.heightProperty().addListener{ _, _, height-> mediaView.fitHeight= height.toDouble()}

		mediaPane.add( mediaView )

		val args = app.parameters.raw.toTypedArray<String>()

		if ( args.isNotEmpty()) { openFile(File(args.first())) }
	}

//------------------------------------
//	Events defined in MainView.fxml
//------------------------------------

	var xOnClick = 0.0
	var yOnClick = 0.0
	var timeOnClick = 0L
	var scale = 1.0

	fun onDragOver(event: DragEvent) {

		val dragboard = event.dragboard

		when{
			dragboard.hasFiles() -> event.acceptTransferModes(TransferMode.COPY)
			dragboard.hasString()-> event.acceptTransferModes(TransferMode.COPY)
		}
		event.consume()
	}

	fun onDragDropped(event: DragEvent) {

		val dragboard = event.dragboard

		when{
			dragboard.hasFiles() -> openFile( dragboard.files.first() )
			dragboard.hasString()-> println( dragboard.string )
		}
		event.consume()
	}

	fun onMousePress( event: MouseEvent){
		xOnClick = event.x - mediaView.translateX
		yOnClick = event.y - mediaView.translateY
		timeOnClick = System.currentTimeMillis()
	}

	fun onMouseRelease( event: MouseEvent ){

		val timeNow = System.currentTimeMillis()

		if ( (timeNow - timeOnClick) < 500 ){

			when (event.button) {
				MouseButton.PRIMARY -> {
					playerPausePlay() }
				MouseButton.MIDDLE -> {
					scale = 1.0
					mediaView.translateX =  mediaView.x
					mediaView.translateY =  mediaView.y
					mediaView.scaleX = scale
					mediaView.scaleY = scale }
				else -> {}
			}
		}
	}

	fun onMouseDrag(event : MouseEvent){
		mediaView.translateX = mediaView.x + (event.x - xOnClick)
		mediaView.translateY = mediaView.y + (event.y - yOnClick)
	}

	fun onScroll(event: ScrollEvent){

		event.consume()

		if( !event.isControlDown ) {

			var step = 0.0

			when {
				event.deltaY > 0.0 -> step =  3000.0
				event.deltaY < 0.0 -> step = -3000.0
			}

			mediaPlayer?.seek(mediaPlayer?.currentTime?.add( Duration( step )))

		} else {

			when {
				event.deltaY > 0.0 -> scale *= 1.1
				event.deltaY < 0.0 -> scale /= 1.1
			}

			scale = scale.coerceIn( 0.1, 10.0)

			mediaView.scaleX = scale
			mediaView.scaleY = scale

			val scenePos = mediaView.localToScene( Point2D(	event.sceneX, event.sceneY ))

			mediaView.translateX = event.sceneX - scenePos.x
			mediaView.translateY = event.sceneY - scenePos.y
		}
	}

	fun onSeek(){
		mediaPlayer?.seek(mediaPlayer?.totalDuration?.multiply( sliderSeek.value))
	}

	fun onSeekClick(){
		mediaPlayer?.seek( mediaPlayer?.totalDuration?.multiply( sliderSeek.value ))
	}

	fun onKeyPressed( event : KeyEvent ){

		if( event.isControlDown ){
			when( event.code ) {
				KeyCode.RIGHT -> mediaPlayer?.seek(mediaPlayer?.currentTime?.add(Duration(5000.0)))
				KeyCode.LEFT  -> mediaPlayer?.seek(mediaPlayer?.currentTime?.add(Duration(-5000.0)))
				else -> {}
			}
		} else {
			when( event.code ){
				KeyCode.SPACE -> playerPausePlay()
				KeyCode.RIGHT -> mediaPlayer?.seek(mediaPlayer?.currentTime?.add(Duration( 1000.0)))
				KeyCode.LEFT  -> mediaPlayer?.seek(mediaPlayer?.currentTime?.add(Duration(-1000.0)))
				KeyCode.F11	  -> primaryStage.isFullScreen = primaryStage.isFullScreen.not()
				else -> {}
			}
		}
		event.consume()
	}

//------------------------------------

	fun openFile( file : Any ){

		try {

			var media : Media? = null

			media = when( file ){
				is String -> Media(file)
				is File   -> Media(file.toURI().toString())
				else 	  -> return
			}

			mediaPlayer?.stop()
			mediaPlayer?.dispose()

			mediaPlayer = MediaPlayer(media)

			mediaView.mediaPlayer = mediaPlayer

			mediaPlayer?.currentTimeProperty()?.addListener{ _, _, value -> sliderUpdate(value) }

			mediaPlayer?.rate = 1.0

			playerPausePlay()

			contrPane.requestFocus()

		} catch ( ex : MediaException ){
			val alert = Alert( AlertType.ERROR,	"${ex.type}", ButtonType.OK	)
			alert.showAndWait()
		}

		catch ( ex : Exception ){
			val alert = Alert( AlertType.ERROR,	"${ex.message}", ButtonType.OK	)
			alert.showAndWait()
		}
	}

	fun playerPausePlay(){

		when( mediaPlayer?.status ){
			MediaPlayer.Status.PLAYING -> mediaPlayer?.pause()
			else -> mediaPlayer?.play()
		}
	}

	fun sliderUpdate( value : Duration){

		sliderSeek.value = value.toMillis() / (mediaPlayer?.totalDuration?.toMillis() ?: 1.0)
	}

	fun cleanUp() {
		println("bye bye")
		exitProcess(0)
	}

}

class MainApp : App() {
	override val primaryView = MainView::class
	override fun start(stage: Stage) {
		stage.icons.add(Image("/gfx/icon.png"))
		stage.fullScreenExitHint = ""
		super.start(stage)
	}
}
